package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.commerciants.Commerciant;
import org.poo.user.User;
import org.poo.utils.Utils;

import java.util.ArrayList;

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
     * @param commerciants for the account
     * @param owner for the account
     */
    public SavingsAccount(final String currency, final double interestRate,
                          final ArrayList<Commerciant> commerciants, final User owner) {
        super(currency, Utils.SAVINGS, commerciants, owner);
        this.setInterestRate(interestRate);
    }

    @Override
    public void addInterestRate() {
        this.setBalance(this.getBalance() + this.getBalance() * this.getInterestRate());
    }
}
