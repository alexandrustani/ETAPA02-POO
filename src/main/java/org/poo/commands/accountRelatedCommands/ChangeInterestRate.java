package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.SavingsAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.user.User;
import org.poo.fileio.CommandInput;
import org.poo.account.Account;

import java.util.ArrayList;

public final class ChangeInterestRate implements VisitableCommand {
    /**
     * Empty constructor
     */
    public ChangeInterestRate() {

    }

    /**
     * Executes the command.
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                               final ArrayNode output) {
        User neededUser = null;
        Account neededAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    neededAccount =  account;
                    neededUser = user;
                    break;
                }
            }
        }

        if (neededUser == null) {
            return;
        }

        if (neededAccount.getAccountType().equals("savings")) {
            ((SavingsAccount) neededAccount).setInterestRate(command.getInterestRate());
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode transaction = mapper.createObjectNode();

            transaction.put("timestamp", command.getTimestamp());
            transaction.put("description", "Interest rate of the account changed to "
                            + command.getInterestRate());

            neededAccount.addTransaction(transaction);

            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();

        commandOutput.put("command", "changeInterestRate");

        ObjectNode error = mapper.createObjectNode();
        error.put("timestamp", command.getTimestamp());
        error.put("description", "This is not a savings account");

        commandOutput.set("output", error);
        commandOutput.put("timestamp", command.getTimestamp());

        output.add(commandOutput);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
