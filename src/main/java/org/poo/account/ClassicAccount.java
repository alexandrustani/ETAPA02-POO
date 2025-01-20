package org.poo.account;
import lombok.Getter;
import lombok.Setter;
import org.poo.commerciants.Commerciant;
import org.poo.user.User;
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
     * @param myCommerciants for the account
     * @param owner for the account
     */
    public ClassicAccount(final String currency,
                          final ArrayList<Commerciant> myCommerciants,
                          final User owner) {
        super(currency, Utils.CLASSIC, myCommerciants, owner);
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
