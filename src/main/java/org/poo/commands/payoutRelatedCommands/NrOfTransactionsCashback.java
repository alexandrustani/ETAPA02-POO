package org.poo.commands.payoutRelatedCommands;

import org.poo.account.Account;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.utils.Utils;

/**
 * Cashback strategy based on the number of transactions.
 */
public final class NrOfTransactionsCashback implements CashbackStrategy {
    /**
     * Empty constructor.
     */
    public NrOfTransactionsCashback() {

    }

    @Override
    public void applyCashback(final CommandInput command, final Account neededAccount,
                              final User neededUser, final Commerciant neededCommerciant,
                              final double neededExchangeRate) {
        neededAccount.incrementTransactions(neededCommerciant);
        String type = neededCommerciant.getType();

        if (neededAccount.getTransPerCommerciant().get(neededCommerciant) > Utils.FOOD_TRANSACTIONS
            && type.equals("Food")
            && !neededAccount.getCashbacks().get(type)) {
            neededAccount.addAmountToBalance(Utils.FOOD_CASHBACK
                                             * neededExchangeRate * command.getAmount());
            neededAccount.gotThisCommerciantCashback(type);
        }

        if (neededAccount.getTransPerCommerciant().get(neededCommerciant)
                > Utils.CLOTHES_TRANSACTIONS
                && type.equals("Clothes")
                && !neededAccount.getCashbacks().get(type)) {
            neededAccount.addAmountToBalance(Utils.CLOTHES_CASHBACK
                                             * neededExchangeRate * command.getAmount());
            System.out.println("Clothes cashback: " + Utils.CLOTHES_CASHBACK);
            neededAccount.gotThisCommerciantCashback(type);
        }

        if (neededAccount.getTransPerCommerciant().get(neededCommerciant) > Utils.TECH_TRANSACTIONS
                && neededCommerciant.getType().equals("Tech")
                && !neededAccount.getCashbacks().get(type)) {
            neededAccount.addAmountToBalance(Utils.TECH_CASHBACK
                                             * neededExchangeRate * command.getAmount());
            neededAccount.gotThisCommerciantCashback(type);
        }
    }
}
