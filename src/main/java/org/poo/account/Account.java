package org.poo.account;

import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.card.Card;
import org.poo.commerciants.Commerciant;
import org.poo.user.User;
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
    private User owner;
    private ArrayList<ObjectNode> accountTransactions;
    private String alias;
    private Map<Commerciant, Integer> transPerCommerciant;
    private Map<String, Boolean> cashbacks;
    private double cashbackAmount;

    /**
     * Constructor for Account
     * @param currency for the account
     * @param accountType for the account
     */
    public Account(final String currency, final String accountType,
                    final ArrayList<Commerciant> commerciants, final User owner) {
        setAccountIBAN(Utils.generateIBAN());
        setBalance(Utils.INITIAL_BALANCE);
        setCurrency(currency);
        setOwner(owner);
        setAccountType(accountType);
        setCards(new ArrayList<>());
        setAccountTransactions(new ArrayList<>());
        setAlias(null);
        setTransPerCommerciant(new HashMap<>());
        setCashbacks(new HashMap<>());

        for (Commerciant commerciant : commerciants) {
            if (commerciant.getCashbackStrategy().equals("nrOfTransactions")) {
                getTransPerCommerciant().put(commerciant, 0);
            }
        }

        getCashbacks().put("Food", false);
        getCashbacks().put("Clothes", false);
        getCashbacks().put("Tech", false);

        setCashbackAmount(0);
    }

    /**
     * Add transaction to account
     * @param transaction to add
     */
    public void addTransaction(final ObjectNode transaction) {
        for (ObjectNode accountTransaction : accountTransactions) {
            if (accountTransaction.get("timestamp").equals(transaction.get("timestamp"))
                    && accountTransaction.get("description").
                    equals(transaction.get("description"))) {
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
     * Add cashback to account
     * @param cashback to add
     */
    public void addAmountToCashback(final double cashback) {
        this.setCashbackAmount(this.getCashbackAmount() + cashback);
    }

    /**
     * Increment transaction for a specified Commerciant
     * @param comm - the specified commerciant
     */
    public void incrementTransactions(final Commerciant comm) {
        getTransPerCommerciant().put(comm, getTransPerCommerciant().get(comm) + 1);
    }

    /**
     * Put true if I used this commerciant casback to specified number of Transactions
     * @param type - the specified commerciant
     */
    public void gotThisCommerciantCashback(final String type) {
        getCashbacks().put(type, true);
    }
}
