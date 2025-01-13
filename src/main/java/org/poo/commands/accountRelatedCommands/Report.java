package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * Class that represents the reporrt command
 */
@Data
public final class Report implements VisitableCommand {
    /**
     * Empty constructor
     */
    public Report() {
    }

    /**
     * Execute the report command
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                               final ArrayNode output) {

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();

        commandOutput.put("command", command.getCommand());

        Account neededAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    neededAccount = account;
                }
            }
        }

        if (neededAccount == null) {
            ObjectNode error = mapper.createObjectNode();
            error.put("description", "Account not found");
            error.put("timestamp", command.getTimestamp());
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
            if (command.getStartTimestamp() <=  transaction.get("timestamp").asInt()
                && transaction.get("timestamp").asInt() <= command.getEndTimestamp()) {
                transactions.add(transaction);
            }
        }

        accountDetails.set("transactions", transactions);
        commandOutput.set("output", accountDetails);
        commandOutput.put("timestamp", command.getTimestamp());

        output.add(commandOutput);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
