package org.poo.commands.payoutRelatedCommands;

import lombok.Data;
import org.poo.account.Account;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.utils.Utils;

/**
 * Cashback strategy based on the spending threshold.
 */

@Data
public final class SpendingThresholdCashback implements CashbackStrategy {
    /**
     * Empty constructor.
     */
    public SpendingThresholdCashback() {

    }

    @Override
    public void applyCashback(final CommandInput command, final Account neededAccount,
                              final User neededUser, final Commerciant neededCommerciant,
                              final double neededExchangeRate) {
        double rateToRon;
        if (command.getCurrency() == null) {
            rateToRon = ExchangeRates.findCurrency(neededAccount.getCurrency(), "RON");
        } else {
            rateToRon = ExchangeRates.findCurrency(command.getCurrency(), "RON");
        }

        double myCashback = neededAccount.getCashbackAmount() + command.getAmount() * rateToRon;

        String type = neededCommerciant.getType();

        for (Commerciant commerciant : neededAccount.getTransPerCommerciant().keySet()) {
            if (neededAccount.getTransPerCommerciant().get(commerciant) >= Utils.FOOD_TRANSACTIONS
                    && type.equals("Food")
                    && !neededAccount.getCashbacks().get(type)) {
                neededAccount.addAmountToBalance(Utils.FOOD_CASHBACK
                        * neededExchangeRate * command.getAmount());
                neededAccount.gotThisCommerciantCashback(type);
                break;
            }

            if (neededAccount.getTransPerCommerciant().get(commerciant)
                    >= Utils.CLOTHES_TRANSACTIONS
                    && type.equals("Clothes")
                    && !neededAccount.getCashbacks().get(type)) {
                neededAccount.addAmountToBalance(Utils.CLOTHES_CASHBACK
                        * neededExchangeRate * command.getAmount());
                neededAccount.gotThisCommerciantCashback(type);
                break;
            }

            if (neededAccount.getTransPerCommerciant().get(commerciant) >= Utils.TECH_TRANSACTIONS
                    && type.equals("Tech")
                    && !neededAccount.getCashbacks().get(type)) {
                neededAccount.addAmountToBalance(Utils.TECH_CASHBACK
                        * neededExchangeRate * command.getAmount());
                neededAccount.gotThisCommerciantCashback(type);
                break;
            }
        }

        if (myCashback >= Utils.SMALL_LIMIT && myCashback < Utils.MEDIUM_LIMIT) {
            applyCashbackForPlan(neededAccount, neededUser, Utils.SMALL_STUDENT_RATE,
                                 Utils.SMALL_SILVER_RATE, Utils.SMALL_GOLD_RATE,
                                 command.getAmount(), neededExchangeRate);
        } else if (myCashback >= Utils.MEDIUM_LIMIT && myCashback < Utils.LARGE_LIMIT) {
            applyCashbackForPlan(neededAccount, neededUser, Utils.MEDIUM_STUDENT_RATE,
                                 Utils.MEDIUM_SILVER_RATE, Utils.MEDIUM_GOLD_RATE,
                                 command.getAmount(), neededExchangeRate);
        } else if (myCashback >= Utils.LARGE_LIMIT) {
            applyCashbackForPlan(neededAccount, neededUser, Utils.LARGE_STUDENT_RATE,
                                 Utils.LARGE_SILVER_RATE, Utils.LARGE_GOLD_RATE,
                                 command.getAmount(), neededExchangeRate);
        }

        neededAccount.addAmountToCashback(command.getAmount() * rateToRon);
    }

    /**
     * Apply the cashback for the user's plan.
     * @param neededAccount - the account from which the payment is made
     * @param neededUser - the user that made the payment
     * @param studentRate - the student rate
     * @param silverRate - the silver rate
     * @param goldRate - the gold rate
     * @param amount - the amount of the payment
     * @param exchangeRate - the exchange rate
     */
    private void applyCashbackForPlan(final Account neededAccount, final User neededUser,
                                      final double studentRate, final double silverRate,
                                      final double goldRate, final double amount,
                                      final double exchangeRate) {
        switch (neededUser.getPlan()) {
            case "student", "standard" ->
                neededAccount.
                    addAmountToBalance(studentRate
                                        * exchangeRate * amount);
            case "silver" ->
                neededAccount.addAmountToBalance(silverRate * exchangeRate * amount);
            case "gold" ->
                neededAccount.addAmountToBalance(goldRate * exchangeRate * amount);
            default -> throw new IllegalArgumentException("No plan like this.");
        }
    }
}

