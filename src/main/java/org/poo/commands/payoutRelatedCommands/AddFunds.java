package org.poo.commands.payoutRelatedCommands;


import lombok.Data;
import org.poo.account.Account;
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
     * Executes the command.
     */
    public void execute(final CommandInput command, final ArrayList<User> users) {
        Account neededAccount = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    neededAccount = account;
                    break;
                }
            }
        }

        if (neededAccount == null) {
            throw new IllegalArgumentException("Account not found");
        }

        neededAccount.addAmountToBalance(command.getAmount());
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
