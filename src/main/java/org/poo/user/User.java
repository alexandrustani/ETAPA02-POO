package org.poo.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.utils.Utils;

import java.util.ArrayList;

/**
 * Class that represents the user.
 */
@Getter
@Setter
public class User {
    private String firstName;
    private String lastName;
    private String email;
    private ArrayList<Account> accounts;
    private String birthday;
    private String occupation;
    private String plan;
    private Integer nrOfExpensiveTransactions;

    /**
     * Constructor for User
     * @param firstName for the user
     * @param lastName for the user
     * @param email for the user
     */
    public User(final String firstName, final String lastName, final String email,
                final String birthday, final String occupation) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
        this.setEmail(email);
        this.setAccounts(new ArrayList<>());
        this.setBirthday(birthday);
        this.setOccupation(occupation);
        this.setNrOfExpensiveTransactions(0);

        if (occupation.equals("student")) {
            this.setPlan("student");
        } else {
            this.setPlan("standard");
        }
    }

    /**
     * Add account to user
     * @param account to add
     */
    public void addAccount(final Account account) {
        this.getAccounts().add(account);
    }

    /**
     * Increment the number of expensive transactions
     */
    public void checkTransactions(final double amount,
                                  final String accountIBAN, final Integer timestamp) {
        if (amount >= Utils.MEDIUM_LIMIT) {
            this.setNrOfExpensiveTransactions(this.getNrOfExpensiveTransactions() + 1);
        }

        if (this.getPlan().equals("silver")
                && this.getNrOfExpensiveTransactions() >= Utils.CLOTHES_TRANSACTIONS) {
            this.setPlan("gold");

            for (Account account : this.getAccounts()) {
                if (account.getAccountIBAN().equals(accountIBAN)) {
                    ObjectMapper mapper = new ObjectMapper();
                    ObjectNode transaction = mapper.createObjectNode()
                        .put("accountIBAN", accountIBAN)
                            .put("newPlanType", "gold")
                            .put("timestamp", timestamp)
                            .put("description", "Upgrade plan");

                    account.addTransaction(transaction);

                    break;
                }
            }
        }
    }
}
