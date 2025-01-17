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
     * Create an account based on the account type.
     * @param command the command input
     * @return the account
     */
    public static Account createAccount(final CommandInput command,
                                        final ArrayList<Commerciant> myCommerciants,
                                        final User user) {
        return switch (command.getAccountType()) {
            case "savings" -> new SavingsAccount(command.getCurrency(),
                                                        command.getInterestRate(),
                                                        myCommerciants,
                                                        user);
            case "classic", "business" -> new ClassicAccount(command.getCurrency(),
                                                             myCommerciants, user);
            default -> throw new IllegalArgumentException("Invalid account type");
        };
    }
}
