package org.poo.commands.accountRelatedCommands;

import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * Command to set the alias of an account.
 */
@Data
public final class SetAlias implements VisitableCommand {
    /**
     * Empty constructor
     */
    public SetAlias() {

    }

    /**
     * Execute the setAlias command.
     * @param command - the command to be executed
     * @param users - the list of users
     */
    public void execute(final CommandInput command,
                               final ArrayList<User> users) {
        User neededUser = null;
        Account neededAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())
                    && user.getEmail().equals(command.getEmail())) {
                    neededAccount = account;
                    neededUser = user;
                    break;
                }
            }
        }

        if (neededUser == null) {
            return;
        }

        neededAccount.setAlias(command.getAlias());
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
