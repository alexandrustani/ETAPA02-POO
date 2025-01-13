package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.account.ClassicAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.account.CommerciantsDetails;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Spendings report Command class
 */
@Data
public final class SpendingsReport implements VisitableCommand {
    /**
     * Empty constructor
     */
    public SpendingsReport() {

    }

    public void addCommerciantsDetails(final ArrayList<CommerciantsDetails> neededCommerciants,
                                       final CommerciantsDetails commerciant) {
        neededCommerciants.stream()
                .filter(cd -> cd.getName().equals(commerciant.getName()))
                .findFirst()
                .ifPresentOrElse(
                        cd -> cd.setAmount(cd.getAmount() + commerciant.getAmount()),
                        () -> neededCommerciants.add(commerciant)
                );
    }

    /**
     * Executes the spendingsReport command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                               final ArrayNode output) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();

        commandOutput.put("command", command.getCommand());

        User neededUser = null;
        Account neededAccount = null;
        ArrayList<CommerciantsDetails> neededCommerciants = new ArrayList<>();

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    neededAccount = account;
                    neededUser = user;
                }
            }
        }

        if (neededUser == null) {
            ObjectNode error = mapper.createObjectNode();
            error.put("description", "Account not found");
            error.put("timestamp", command.getTimestamp());
            commandOutput.set("output", error);
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
            return;
        }

        if (neededAccount.getAccountType().equals("savings")) {
            ObjectNode error = mapper.createObjectNode();
            error.put("error", "This kind of report is not supported for a saving account");
            commandOutput.set("output", error);
            commandOutput.put("timestamp", command.getTimestamp());
            output.add(commandOutput);
            return;
        }

        ObjectNode accountDetails = mapper.createObjectNode();

        accountDetails.put("IBAN", neededAccount.getAccountIBAN());
        accountDetails.put("balance", neededAccount.getBalance());
        accountDetails.put("currency", neededAccount.getCurrency());

        ArrayNode transactions = mapper.createArrayNode();

        for (ObjectNode transaction : neededAccount.getAccountTransactions()) {
            if ((command.getStartTimestamp() <=  transaction.get("timestamp").asInt()
                    && transaction.get("timestamp").asInt() <= command.getEndTimestamp())
                    && transaction.has("commerciant")) {
                transactions.add(transaction);
                addCommerciantsDetails(neededCommerciants,
                        new CommerciantsDetails(transaction.get("commerciant").asText(),
                                transaction.get("amount").asDouble()));
            }
        }

        accountDetails.set("transactions", transactions);
        commandOutput.set("output", accountDetails);
        commandOutput.put("timestamp", command.getTimestamp());

        neededCommerciants.sort(Comparator.comparing(CommerciantsDetails::getName));

        ArrayNode commerciants = mapper.createArrayNode();

        for (CommerciantsDetails commerciant : neededCommerciants) {
            ObjectNode detailsOfCommerciant = mapper.createObjectNode();
            detailsOfCommerciant.put("commerciant", commerciant.getName());
            detailsOfCommerciant.put("total", commerciant.getAmount());
            commerciants.add(detailsOfCommerciant);
        }

        accountDetails.set("commerciants", commerciants);

        commandOutput.set("output", accountDetails);

        commandOutput.put("timestamp", command.getTimestamp());

        output.add(commandOutput);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
