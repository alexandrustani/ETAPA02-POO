package org.poo.account;

import lombok.Data;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * Factory class to create an account.
 */
@Data
public final class FactoryOfAccount {
    /**
     * Private constructor to avoid instantiation.
     */
    private FactoryOfAccount() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Create an account based on the command input.
     * @param command - the command input
     * @param myCommerciants - the list of commerciants
     * @param user - the user
     * @return the created account
     */
    public static Account createAccount(final CommandInput command,
                                        final ArrayList<Commerciant> myCommerciants,
                                        final User user) {
        return switch (command.getAccountType()) {
            case "savings" -> new SavingsAccount(command.getCurrency(),
                                                        command.getInterestRate(),
                                                        myCommerciants,
                                                        user);
            case "classic" -> new ClassicAccount(command.getCurrency(),
                                                             myCommerciants, user);

            case "business" -> new BusinessAccount(command.getCurrency(),
                                                        myCommerciants, user);
            default -> throw new IllegalArgumentException("Invalid account type");
        };
    }
}
