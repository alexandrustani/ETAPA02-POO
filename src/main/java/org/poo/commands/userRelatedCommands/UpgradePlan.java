package org.poo.commands.userRelatedCommands;

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
 * Upgrade plan command class.
 */

@Data
public final class UpgradePlan implements VisitableCommand {
    /**
     * Empty constructor
     */
    public UpgradePlan() {

    }

    /**
     * Execute the upgradePlan command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
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

        if (neededUser.getPlan().equals(command.getNewPlanType())) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode transaction = mapper.createObjectNode()
                    .put("description", "The user already has the "
                            + command.getNewPlanType() + " plan.")
                    .put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction);

            return;
        }

        double fromRon = ExchangeRates.findCurrency("RON", neededAccount.getCurrency());
        switch (command.getNewPlanType()) {
            case "silver" -> {
                if (neededUser.getPlan().equals("standard")
                        || neededUser.getPlan().equals("student")) {
                    if (neededAccount.getBalance()
                            < Utils.FEE_STUDENT_OR_STANDARD_TO_SILVER * fromRon) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode error = mapper.createObjectNode()
                                .put("description", "Insufficient funds")
                                .put("timestamp", command.getTimestamp());
                        neededAccount.addTransaction(error);

                        return;
                    }

                    neededAccount.
                            subtractAmountFromBalance(Utils.FEE_STUDENT_OR_STANDARD_TO_SILVER
                                                        * fromRon);
                    neededUser.setPlan("silver");
                }
            }

            case "gold" -> {
                if (neededUser.getPlan().equals("silver")) {
                    if (neededAccount.getBalance() < Utils.FEE_SILVER_TO_GOLD * fromRon) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode error = mapper.createObjectNode()
                                .put("description", "Insufficient funds")
                                .put("timestamp", command.getTimestamp());
                        neededAccount.addTransaction(error);

                        return;
                    }

                    neededAccount.subtractAmountFromBalance(Utils.FEE_SILVER_TO_GOLD * fromRon);
                    neededUser.setPlan("gold");
                }

                if (neededUser.getPlan().equals("standard")
                        || neededUser.getPlan().equals("student")) {
                    if (neededAccount.getBalance()
                            < Utils.FEE_STUDENT_OR_STANDARD_TO_GOLD * fromRon) {
                        ObjectMapper mapper = new ObjectMapper();
                        ObjectNode error = mapper.createObjectNode()
                                .put("description", "Insufficient funds")
                                .put("timestamp", command.getTimestamp());
                        neededAccount.addTransaction(error);

                        return;
                    }

                    neededAccount.
                            subtractAmountFromBalance(Utils.FEE_STUDENT_OR_STANDARD_TO_GOLD
                                                        * fromRon);
                    neededUser.setPlan("gold");
                }
            }

            default -> {
                return;
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        neededAccount.addTransaction(mapper.createObjectNode()
                .put("accountIBAN", neededAccount.getAccountIBAN())
                .put("description", "Upgrade plan")
                .put("newPlanType", command.getNewPlanType())
                .put("timestamp", command.getTimestamp()));
    }

    @Override
    public void accept(final CommandVisitor command) {
        command.visit(this);
    }
}
