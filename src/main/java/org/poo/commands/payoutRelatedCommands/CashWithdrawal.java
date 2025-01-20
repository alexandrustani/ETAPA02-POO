package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.Utils;

import java.util.ArrayList;

/**
 * Cash withdrawal command class.
 */
public final class CashWithdrawal implements VisitableCommand {
    /**
     * Empty constructor
     */
    public CashWithdrawal() {
    }

    /**
     * Execute the cashWithdrawal command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayNode output) {
        User neededUser = users.stream()
                .filter(user -> user.getEmail().equals(command.getEmail()))
                .findFirst()
                .orElse(null);

        if (neededUser == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode();

            commandNode.put("command", "cashWithdrawal");
            ObjectNode error = mapper.createObjectNode();

            error.put("description", "User not found");

            error.put("timestamp", command.getTimestamp());
            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);
            return;
        }

        Account neededAccount = null;
        Card neededCard = null;

        for (var account : neededUser.getAccounts()) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(command.getCardNumber())) {
                    neededCard = card;
                    neededAccount = account;
                    break;
                }
            }
        }

        if (neededAccount == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode();

            commandNode.put("command", "cashWithdrawal");
            ObjectNode error = mapper.createObjectNode();

            error.put("description", "Card not found");

            error.put("timestamp", command.getTimestamp());
            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);
            return;
        }

        if (neededCard.getCardStatus().equals("frozen")) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode();

            commandNode.put("command", "cashWithdrawal");
            ObjectNode error = mapper.createObjectNode();

            error.put("description", "Card is frozen");

            error.put("timestamp", command.getTimestamp());
            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);
            return;
        }

        double fromRon = ExchangeRates.findCurrency("RON", neededAccount.getCurrency());
        double taxes = Utils.INITIAL_BALANCE;

        switch (neededUser.getPlan()) {
            case "standard" -> taxes = Utils.MEDIUM_STUDENT_RATE * command.getAmount() * fromRon;
            case "silver" -> {
                if (command.getAmount() > Utils.LARGE_LIMIT) {
                    taxes = Utils.SMALL_STUDENT_RATE * command.getAmount() * fromRon;
                }
            }
            default -> taxes = Utils.INITIAL_BALANCE;
        }

        if (neededAccount.getBalance() < command.getAmount() * fromRon + taxes) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode transactionNode = mapper.createObjectNode()
                    .put("timestamp", command.getTimestamp())
                    .put("description", "Insufficient funds");

            neededAccount.addTransaction(transactionNode);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.createObjectNode()
                .put("timestamp", command.getTimestamp())
                .put("description", "Cash withdrawal of " +  command.getAmount())
                .put("amount", command.getAmount());

        neededAccount.addTransaction(transactionNode);

        neededAccount.subtractAmountFromBalance(command.getAmount() * fromRon + taxes);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
