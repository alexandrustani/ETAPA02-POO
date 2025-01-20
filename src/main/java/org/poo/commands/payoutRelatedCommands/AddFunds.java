package org.poo.commands.payoutRelatedCommands;


import lombok.Data;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;
import org.poo.user.User;

/**
 * Add funds to an account.
 */
@Data
public final class AddFunds implements VisitableCommand {
    /**
     * Empty constructor
     */
    public AddFunds() {

    }

    /**
     * Execute the addFunds command.
     * @param command - the command to be executed
     * @param users - the list of users
     */
    public void execute(final CommandInput command, final ArrayList<User> users) {
        Account neededAccount = users.stream()
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountIBAN().equals(command.getAccount()))
                .findFirst()
                .orElse(null);

        User neededUser = users.stream()
                .filter(user -> user.getEmail().equals(command.getEmail()))
                .findFirst()
                .orElse(null);

        if (neededAccount == null || neededUser == null) {
            return;
        }

        if (!neededAccount.getAccountType().equals("business")) {
            neededAccount.addAmountToBalance(command.getAmount());
        } else {
            businessCase((BusinessAccount) neededAccount, command.getAmount(), neededUser,
                         command);
        }
    }

    /**
     * Business case for adding funds to an account.
     * @param neededAccount - the account to add funds to
     * @param amount - the amount to be added
     * @param neededUser - the user that wants to add funds
     * @param command - the command to be executed
     */
    private void businessCase(final BusinessAccount neededAccount,
                              final double amount, final User neededUser,
                              final CommandInput command) {
        if (neededAccount.getOwner().equals(neededUser)) {
            neededAccount.addAmountToBalance(amount);
            return;
        }

        if (neededAccount.getEmployees().containsKey(neededUser)) {
            String role = neededAccount.getEmployees().get(neededUser);
            if (role.equals("employee")) {
                if (command.getAmount() > neededAccount.getDepositLimit()) {
                    return;
                }
            }

            neededAccount.addAmountToBalance(amount);
            neededAccount.getDepositPerEmployee().get(neededUser).put(command.getTimestamp(),
                                                                        amount);
        }
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
