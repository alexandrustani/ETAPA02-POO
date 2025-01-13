package org.poo.commands.payoutRelatedCommands;


import org.poo.account.Account;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

public interface CashbackStrategy {
    /**
     * Apply the cashback strategy.
     * @param command - the command to be executed
     * @param neededAccount - the account from which the payment is made
     * @param neededUser - the user that made the payment
     * @param neededCommerciant - the commerciant from which the payment is made
     * @param neededExchangeRate - the exchange rate
     */
    void applyCashback(CommandInput command, Account neededAccount, User neededUser,
                       Commerciant neededCommerciant, double neededExchangeRate);
}

