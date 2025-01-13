package org.poo.account;

import lombok.Data;
import org.poo.fileio.CommandInput;

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
    public static Account createAccount(final CommandInput command) {
        return switch (command.getAccountType()) {
            case "savings" -> new SavingsAccount(command.getCurrency(),
                                                        command.getInterestRate());
            case "classic", "business" -> new ClassicAccount(command.getCurrency());
            default -> throw new IllegalArgumentException("Invalid account type");
        };
    }
}
