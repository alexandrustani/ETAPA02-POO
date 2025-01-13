package org.poo.account;

/**
 * Interface that represents a commerciant.
 */
public interface SpecialAccountFunctions {
    /**
     * Add a transaction to the account.
     * @param commerciant - the transaction to be added
     */
    default void addCommerciant(final CommerciantsDetails commerciant) {
        throw new UnsupportedOperationException("Not for this kind of account");
    }

    /**
     * Get a certain commerciant.
     * @param name - the name of the commerciant
     * @return the commerciant
     */
    default CommerciantsDetails getACertainCommerciant(final String name) {
        throw new UnsupportedOperationException("Not for this kind of account");
    }

    /**
     * Add Interest rate to the account.
     */
    default void addInterestRate() {
        throw new UnsupportedOperationException("Not for this kind of account");
    }
}
