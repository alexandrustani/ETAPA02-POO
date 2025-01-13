package org.poo.commands.userRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

public class UpgradePlan implements VisitableCommand {
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayNode output) {
        User neededUser = null;
        Account neededAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    neededUser = user;
                    neededAccount = account;
                }
            }
        }

        if (neededUser == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode()
                    .put("command", "upgradePlan");
            ObjectNode error = mapper.createObjectNode()
                    .put("description", "Account not found")
                    .put("timestamp", command.getTimestamp());
            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);

            return;
        }

        double fromRon = ExchangeRates.findCurrency("RON", neededAccount.getCurrency());

        switch (command.getNewPlanType()) {
            case "silver" -> {
                if (neededUser.getPlan().equals("standard")
                        || neededUser.getPlan().equals("student")) {
                    if (neededAccount.getBalance() < 100 * fromRon) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode error = mapper.createObjectNode()
                                .put("description", "Insufficient funds")
                                .put("timestamp", command.getTimestamp());
                        neededAccount.addTransaction(error);

                        return;
                    }

                    neededAccount.subtractAmountFromBalance(100 * fromRon);
                    neededUser.setPlan("silver");
                }
            }

            case "gold" -> {
                if (neededUser.getPlan().equals("silver")) {
                    if (neededAccount.getBalance() < 250 * fromRon) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode error = mapper.createObjectNode()
                                .put("description", "Insufficient funds")
                                .put("timestamp", command.getTimestamp());
                        neededAccount.addTransaction(error);

                        return;
                    }

                    neededAccount.subtractAmountFromBalance(250 * fromRon);
                    neededUser.setPlan("gold");
                }

                if (neededUser.getPlan().equals("standard")
                        || neededUser.getPlan().equals("student")) {
                    if (neededAccount.getBalance() < 350 * fromRon) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode error = mapper.createObjectNode()
                                .put("description", "Insufficient funds")
                                .put("timestamp", command.getTimestamp());
                        neededAccount.addTransaction(error);

                        return;
                    }
                    neededAccount.subtractAmountFromBalance(350 * fromRon);
                    neededUser.setPlan("gold");
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        neededAccount.addTransaction(mapper.createObjectNode()
                .put("accountIBAN", neededAccount.getAccountIBAN())
                .put("description", "Upgrade plan")
                .put("newPlanType", command.getNewPlanType())
                .put("timestamp", command.getTimestamp()));
    }

    public void accept(CommandVisitor command) {
        command.visit(this);
    }
}
