package org.poo.commands.userRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.account.Account;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to print transactions.
 */
@Data
public final class PrintTransactions implements VisitableCommand {
    /**
     * Empty constructor
     */
    public PrintTransactions() {
    }

    /**
     * Add user transactions to the transactions list.
     * @param accounts - the list of accounts
     * @param transactions - the list of transactions
     */
    private void addUserTransactions(final ArrayList<Account> accounts,
                                            final ArrayList<ObjectNode> transactions) {
        for (Account account : accounts) {
            transactions.addAll(account.getAccountTransactions());
        }
    }
    /**
     * Execute the printTransactions command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     * @param mapper - the object mapper
     */
    public void execute(final CommandInput command,
                       final ArrayList<User> users, final ArrayNode output,
                       final ObjectMapper mapper) {
        ObjectNode commandOutput = mapper.createObjectNode();

        commandOutput.put("command", "printTransactions");

        ArrayList<ObjectNode> transactions = new ArrayList<>();

        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                addUserTransactions(user.getAccounts(), transactions);
            }
        }

        transactions.sort(Comparator.comparing(transaction -> transaction.get("timestamp")
                .asInt()));

        ArrayNode transactionsArray = mapper.createArrayNode();

        for (ObjectNode transaction : transactions) {
            transactionsArray.add(transaction);
        }

        commandOutput.set("output", transactionsArray);
        commandOutput.put("timestamp", command.getTimestamp());

        output.add(commandOutput);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
