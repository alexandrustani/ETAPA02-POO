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

import java.util.ArrayList;

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
     */
    public void execute(final CommandInput command, final ArrayList<User> users, final ArrayNode output) {
        User neededUser = null;
        Account neededAccount = null;
        Card neededCard = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(command.getCardNumber())) {
                        neededUser = user;
                        neededAccount = account;
                        neededCard = card;
                    }
                }
            }
        }

        if (neededUser == null) {
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

        double fromRon = ExchangeRates.findCurrency("RON", neededAccount.getCurrency());

        if (neededAccount.getBalance() < command.getAmount() * fromRon) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode transactionNode = mapper.createObjectNode()
                    .put("timestamp", command.getTimestamp())
                    .put("description", "Insufficient funds");

            neededAccount.addTransaction(transactionNode);
            return;
        }

        switch (neededUser.getPlan()) {
            case "standard" -> neededAccount.
                    subtractAmountFromBalance(0.002
                                              * command.getAmount() * fromRon);
            case "silver" -> {
                if (command.getAmount() > 500) {
                    neededAccount.subtractAmountFromBalance(0.001
                                                            * command.getAmount() * fromRon);
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transactionNode = mapper.createObjectNode()
                .put("timestamp", command.getTimestamp())
                .put("description", "Cash withdrawal of " +  command.getAmount())
                .put("amount", command.getAmount());

        neededAccount.addTransaction(transactionNode);

        neededAccount.subtractAmountFromBalance(command.getAmount() * fromRon);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
