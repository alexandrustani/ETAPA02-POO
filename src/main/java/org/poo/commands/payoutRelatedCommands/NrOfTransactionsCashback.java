package org.poo.commands.payoutRelatedCommands;

import org.poo.account.Account;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

public final class NrOfTransactionsCashback implements CashbackStrategy {
    /**
     * Apply the cashback strategy.
     * @param command - the command to be executed
     * @param neededAccount - the account from which the payment is made
     * @param neededUser - the user that made the payment
     * @param neededCommerciant - the commerciant from which the payment is made
     * @param neededExchangeRate - the exchange rate
     */
    @Override
    public void applyCashback(final CommandInput command, final Account neededAccount, final User neededUser,
                              final Commerciant neededCommerciant, final double neededExchangeRate) {
        if (neededAccount.getNrOfTransactions() >= 2 && neededCommerciant.getType().equals("Food") &&
                !neededAccount.getCashbacks().get(2)) {
            neededAccount.addAmountToBalance(0.02 * neededExchangeRate * command.getAmount());
            neededAccount.getCashbacks().put(2, true);
        }

        if (neededAccount.getNrOfTransactions() >= 5 && neededCommerciant.getType().equals("Clothes") &&
                !neededAccount.getCashbacks().get(5)) {
            neededAccount.addAmountToBalance(0.05 * neededExchangeRate * command.getAmount());
            neededAccount.getCashbacks().put(5, true);
        }

        if (neededAccount.getNrOfTransactions() >= 10 && neededCommerciant.getType().equals("Tech") &&
                !neededAccount.getCashbacks().get(10)) {
            neededAccount.addAmountToBalance(0.1 * neededExchangeRate * command.getAmount());
            neededAccount.getCashbacks().put(10, true);
        }

        neededAccount.incrementTransactions();
    }
}
