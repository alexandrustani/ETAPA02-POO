package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

/**
 * SavingsAccount class
 */
@Setter
@Getter
public final class SavingsAccount extends Account implements SpecialAccountFunctions {
    private double interestRate;

    /**
     * Constructor for SavingsAccount
     * @param currency for the account
     * @param interestRate for the account
     */
    public SavingsAccount(final String currency, final double interestRate) {
        super(currency, Utils.SAVINGS);
        this.setInterestRate(interestRate);
    }

    @Override
    public void addInterestRate() {
        this.setBalance(this.getBalance() + this.getBalance() * this.getInterestRate());
    }
}
