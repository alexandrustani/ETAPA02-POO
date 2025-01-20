package org.poo.commands.accountRelatedCommands;

import org.poo.account.BusinessAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

public final class AddNewBusinessAssociate implements VisitableCommand {
    /**
     * Empty constructor
     */
    public AddNewBusinessAssociate() {
    }

    /**
     * Execute the addNewBusinessAssociate command
     * @param command to execute
     * @param users to add new business associate
     */
    public void execute(final CommandInput command, final ArrayList<User> users) {
        User neededUser = users.stream()
                .filter(user -> user.getEmail().equals(command.getEmail()))
                .findFirst()
                .orElse(null);

        if (neededUser == null) {
            return;
        }

        BusinessAccount neededAccount = users.stream()
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountIBAN().equals(command.getAccount())
                        && account.getAccountType().equals("business"))
                .map(account -> (BusinessAccount) account)
                .findFirst()
                .orElse(null);

        if (neededAccount == null) {
            return;
        }

        if (neededAccount.getEmployees().containsKey(neededUser)
            || neededAccount.getOwner().equals(neededUser)) {
            return;
        }

        neededAccount.addEmployee(neededUser, command.getRole());
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
