package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.Utils;

import java.util.ArrayList;

/**
 * Send money command class.
 */
@Data
public final class SendMoney implements VisitableCommand {
    /**
     * Empty constructor
     */
    public SendMoney() {

    }

    /**
     * Execute the sendMoney command.
     * @param command - the command to be executed
     * @param users - the list of users
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayNode output) {
        User sender = null;
        User receiver = null;

        Account senderAccount = null;
        Account receiverAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    sender = user;
                    senderAccount = account;
                }

                if (account.getAlias() != null
                    && account.getAlias().equals(command.getAccount())) {
                    return;
                }

                if (account.getAccountIBAN().equals(command.getReceiver())
                    || (account.getAlias() != null
                        && account.getAlias().equals(command.getReceiver()))) {
                    receiver = user;
                    receiverAccount = account;
                }
            }
        }

        if (sender == null || receiver == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode()
                        .put("command", "sendMoney");

            ObjectNode error = mapper.createObjectNode()
                    .put("description", "User not found")
                    .put("timestamp", command.getTimestamp());

            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);

            return;
        }

        double exchangeRate = ExchangeRates.findCurrency(senderAccount.getCurrency(),
                                                         receiverAccount.getCurrency());

        double toRon = ExchangeRates.findCurrency(senderAccount.getCurrency(), "RON");

        double taxes = Utils.INITIAL_BALANCE;

        switch (sender.getPlan()) {
            case "standard" -> taxes = (Utils.MEDIUM_STUDENT_RATE * command.getAmount());

            case "silver" -> {
                if (command.getAmount() * toRon > Utils.LARGE_LIMIT) {
                    taxes = Utils.SMALL_STUDENT_RATE * command.getAmount();
                }
            }

            default -> taxes = Utils.INITIAL_BALANCE;
        }

        if (senderAccount.getBalance() < command.getAmount() + taxes) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode transaction = mapper.createObjectNode();

            transaction.put("description", "Insufficient funds");
            transaction.put("timestamp", command.getTimestamp());

            senderAccount.addTransaction(transaction);

            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode senderTransaction = mapper.createObjectNode()
                .put("timestamp", command.getTimestamp())
                .put("description", command.getDescription())
                .put("senderIBAN", senderAccount.getAccountIBAN())
                .put("receiverIBAN", receiverAccount.getAccountIBAN())
                .put("amount", command.getAmount() + " " + senderAccount.getCurrency())
                .put("transferType", "sent");

        senderAccount.addTransaction(senderTransaction);

        ObjectNode receiverTransaction = mapper.createObjectNode()
                    .put("timestamp", command.getTimestamp())
                    .put("description", command.getDescription())
                    .put("senderIBAN", senderAccount.getAccountIBAN())
                    .put("receiverIBAN", receiverAccount.getAccountIBAN())
                    .put("amount", (command.getAmount() * exchangeRate) + " "
                            + receiverAccount.getCurrency())
                    .put("transferType", "received");

        receiverAccount.addTransaction(receiverTransaction);

        senderAccount.subtractAmountFromBalance(command.getAmount() + taxes);
        receiverAccount.addAmountToBalance(command.getAmount() * exchangeRate);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
