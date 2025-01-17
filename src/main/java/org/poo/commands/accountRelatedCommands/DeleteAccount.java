package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import org.poo.user.User;
import org.poo.utils.Utils;

/**
 * Delete account command class.
 */
@Data
public final class DeleteAccount implements VisitableCommand {
    /**
     * Empty constructor
     */
    public DeleteAccount() {

    }

    /**
     * Execute the deleteAccount command.
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
                    break;
                }
            }
        }

        if (neededUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandNode = mapper.createObjectNode();

        commandNode.put("command", "deleteAccount");

        if (neededAccount.getBalance() == Utils.INITIAL_BALANCE) {
            neededUser.getAccounts().remove(neededAccount);
            ObjectNode success = mapper.createObjectNode();
            success.put("success", "Account deleted");
            success.put("timestamp", command.getTimestamp());
            commandNode.set("output", success);
        } else {
            ObjectNode error = mapper.createObjectNode();
            error.put("error",
                    "Account couldn't be deleted - see org.poo.transactions for details");
            error.put("timestamp", command.getTimestamp());
            commandNode.set("output", error);

            ObjectNode transaction = mapper.createObjectNode();
            transaction.put("timestamp", command.getTimestamp());
            transaction.put("description",
                         "Account couldn't be deleted - there are funds remaining");

            neededAccount.addTransaction(transaction);
        }

        commandNode.put("timestamp", command.getTimestamp());
        output.add(commandNode);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
