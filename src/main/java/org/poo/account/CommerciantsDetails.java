package org.poo.account;

import lombok.Getter;
import lombok.Setter;

/**
 * Class that represents the commerciants details.
 */
@Getter
@Setter
public final class CommerciantsDetails {
    private String name;
    private Double amount;

    /**
     * Constructor for CommerciantsDetails.
     * @param name - the name of the commerciant
     * @param amount - the amount to be paid
     */
    public CommerciantsDetails(final String name, final double amount) {
        this.setName(name);
        this.setAmount(amount);
    }

    /**
     * Add amount to the commerciant.
     * @param amountToAdd - the amount to be added
     */
    public void addAmount(final double amountToAdd) {
        this.setAmount(this.getAmount() + amountToAdd);
    }
}
