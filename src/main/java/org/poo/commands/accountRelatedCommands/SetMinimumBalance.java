package org.poo.commands.accountRelatedCommands;

import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * Command to set the minimum balance.
 */
@Data
public final class SetMinimumBalance implements VisitableCommand {
    /**
     * Empty constructor
     */
    public SetMinimumBalance() {

    }

    /**
     * Execute the setMinimumBalance command.
     * @param command - the command to be executed
     * @param users - the list of users
     */
    public void execute(final CommandInput command, final ArrayList<User> users) {
        User neededUser = users.stream()
                .filter(user -> user.getEmail().equals(command.getEmail()))
                .findFirst()
                .orElse(null);

        if (neededUser == null) {
            return;
        }

        Account neededAccount = neededUser.getAccounts().stream()
                .filter(account -> account.getAccountIBAN().equals(command.getAccount()))
                .findFirst()
                .orElse(null);

        if (neededAccount == null) {
            return;
        }

        neededAccount.setMinimumBalance(command.getMinBalance());
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
