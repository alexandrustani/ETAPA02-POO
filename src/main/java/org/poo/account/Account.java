package org.poo.account;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.card.Card;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Data
public class Account {
    private String accountIBAN;
    private ArrayList<Card> cards;
    private double minimumBalance;
    private String accountType;
    private double balance;
    private String currency;
    private ArrayList<ObjectNode> accountTransactions;
    private String alias;
    private Integer nrOfTransactions;
    private Map<Integer, Boolean> cashbacks;
    private double cashbackAmount;

    /**
     * Constructor for Account
     * @param currency for the account
     * @param accountType for the account
     */
    public Account(final String currency, final String accountType) {
        this.setAccountIBAN(Utils.generateIBAN());
        this.setBalance(Utils.INITIAL_BALANCE);
        this.setCurrency(currency);
        this.setAccountType(accountType);
        this.setCards(new ArrayList<>());
        this.setAccountTransactions(new ArrayList<>());
        this.setAlias(null);
        this.setNrOfTransactions(0);
        this.setCashbacks(new HashMap<>());

        this.getCashbacks().put(2, false);
        this.getCashbacks().put(5, false);
        this.getCashbacks().put(10, false);
        this.setCashbackAmount(0);
    }

    /**
     * Add transaction to account
     * @param transaction to add
     */
    public void addTransaction(final ObjectNode transaction) {
        for (ObjectNode accountTransaction : accountTransactions) {
            if (accountTransaction.equals(transaction)) {
                return;
            }
        }

        accountTransactions.add(transaction);
    }

    /**
     * Add amount to balance
     * @param amountToAdd to add
     */
    public void addAmountToBalance(final double amountToAdd) {
        this.balance += amountToAdd;
    }

    /**
     * Subtract amount from balance
     * @param amountToSubtract to subtract
     */
    public void subtractAmountFromBalance(final double amountToSubtract) {
        this.balance -= amountToSubtract;
    }

    /**
     * Generate a new card number for the needed card
     * @param neededCard - the card for which the card number is to be generated
     */
    public void generateNewCardNumber(final Card neededCard) {
        for (Card card : cards) {
            if (neededCard.getCardNumber().equals(card.getCardNumber())) {
                card.setCardNumber(Utils.generateCardNumber());
            }
        }
    }

    /**
     * Increment the number of transactions
     */
    public void incrementTransactions() {
        this.setNrOfTransactions(this.getNrOfTransactions() + 1);
    }

    /**
     * Add cashback to account
     * @param cashback to add
     */
    public void addAmountToCashback(final double cashback) {
        this.setCashbackAmount(this.getCashbackAmount() + cashback);
    }
}
