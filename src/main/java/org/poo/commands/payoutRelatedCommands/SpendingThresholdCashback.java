package org.poo.commands.payoutRelatedCommands;

import org.poo.account.Account;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.exchangeRates.ExchangeRates;

public final class SpendingThresholdCashback implements CashbackStrategy {
    @Override
    public void applyCashback(final CommandInput command, final Account neededAccount, final User neededUser,
                              final Commerciant neededCommerciant, final double neededExchangeRate) {
        double rateToRon = ExchangeRates.findCurrency(command.getCurrency(), "RON");
        double myCashback = neededAccount.getCashbackAmount() + command.getAmount() * rateToRon;

        if (myCashback >= 100 && myCashback < 300) {
            applyCashbackForPlan(neededAccount, neededUser, 0.001, 0.003, 0.005,
                                 command.getAmount(), neededExchangeRate);
        } else if (myCashback >= 300 && myCashback < 500) {
            applyCashbackForPlan(neededAccount, neededUser, 0.002, 0.004, 0.055,
                                 command.getAmount(), neededExchangeRate);
        } else if (myCashback >= 500) {
            applyCashbackForPlan(neededAccount, neededUser, 0.025, 0.005, 0.07,
                                 command.getAmount(), neededExchangeRate);
        }
    }

    /**
     *
     * @param neededAccount -
     * @param neededUser -
     * @param studentRate -
     * @param silverRate -
     * @param goldRate -
     * @param amount -
     * @param exchangeRate -
     */
    private void applyCashbackForPlan(Account neededAccount, User neededUser, double studentRate, double silverRate, double goldRate, double amount, double exchangeRate) {
        switch (neededUser.getPlan()) {
            case "student", "standard" -> {
                neededAccount.
                        addAmountToBalance(studentRate * exchangeRate * amount);
            }
            case "silver" -> {
                neededAccount.addAmountToBalance(silverRate * exchangeRate * amount);
            }
            case "gold" -> {
                neededAccount.addAmountToBalance(goldRate * exchangeRate * amount);
            }
        }
    }
}

