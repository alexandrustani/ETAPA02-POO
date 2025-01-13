package org.poo.account;
import lombok.Getter;
import lombok.Setter;
import org.poo.utils.Utils;

import java.util.ArrayList;

/**
 * ClassicAccount class
 */
@Getter
@Setter
public final class ClassicAccount extends Account implements SpecialAccountFunctions {
    private ArrayList<CommerciantsDetails> commerciants;

    /**
     * Constructor for ClassicAccount
     * @param currency for the account
     */
    public ClassicAccount(final String currency) {
        super(currency, Utils.CLASSIC);
        this.setCommerciants(new ArrayList<>());
    }

    @Override
    public void addCommerciant(final CommerciantsDetails commerciant) {
        for (CommerciantsDetails comm : this.getCommerciants()) {
            if (comm.getName().equals(commerciant.getName())) {
                comm.addAmount(commerciant.getAmount());
                return;
            }
        }

        this.getCommerciants().add(commerciant);
    }

    @Override
    public CommerciantsDetails getACertainCommerciant(final String commerciantName) {
        for (CommerciantsDetails commerciant : this.getCommerciants()) {
            if (commerciant.getName().equals(commerciantName)) {
                return commerciant;
            }
        }

        return null;
    }
}
