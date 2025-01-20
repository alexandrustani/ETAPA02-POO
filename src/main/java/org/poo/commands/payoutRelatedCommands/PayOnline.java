package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Setter;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.account.ClassicAccount;
import org.poo.card.Card;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.commerciants.Commerciant;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.fileio.CommandInput;

import java.util.ArrayList;

import org.poo.account.CommerciantsDetails;
import org.poo.user.User;
import org.poo.utils.Utils;

/**
 * Command to pay online.
 */
@Setter
public final class PayOnline implements VisitableCommand {
    /**
     * Empty constructor
     */
    public PayOnline() {
    }

    private CashbackStrategy cashbackStrategy;

    /**
     * Create the transactions for an error.
     * @param command - the command to be executed
     * @param neededUser - the user that made the payment
     * @param neededCard - the card used for the payment
     * @param output - the output array
     * @param neededAccount - the account from which the payment is made
     * @return true if an error occurred, false otherwise
     */
    public boolean createErrorTransactions(final CommandInput command,
                                          final User neededUser,
                                          final Card neededCard,
                                          final ArrayNode output,
                                          final Account neededAccount) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandNode = mapper.createObjectNode();

        commandNode.put("command", "payOnline");
        ObjectNode error = mapper.createObjectNode();
        error.put("description", "Card not found");
        error.put("timestamp", command.getTimestamp());

        commandNode.set("output", error);
        commandNode.put("timestamp", command.getTimestamp());

        if (neededAccount == null) {
            output.add(commandNode);

            return true;
        }

        if (neededCard.getCardStatus().equals("frozen")) {
            ObjectNode transaction = mapper.createObjectNode();

            transaction.put("timestamp", command.getTimestamp());
            transaction.put("description", "The card is frozen");

            neededAccount.addTransaction(transaction);

            return true;
        }

        if (!neededAccount.getOwner().equals(neededUser)
            && !neededAccount.getAccountType().equals("business")) {
            output.add(commandNode);

            return true;
        }

        if (neededAccount.getAccountType().equals("business")) {
            BusinessAccount businessAccount = (BusinessAccount) neededAccount;

            if (!businessAccount.getOwner().equals(neededUser)
                    && !businessAccount.getEmployees().containsKey(neededUser)) {
                output.add(commandNode);

                return true;
            }
        }

        return false;
    }

    /**
     * Create the transactions for a successful payment for a business account.
     * @param command - the command to be executed
     * @param neededUser - the user that made the payment
     * @param neededAccount - the account from which the payment is made
     * @param neededCard - the card used for the payment
     * @param neededExchangeRate - the exchange rate between the account currency
     *                              and the payment currency
     * @param neededCommerciant - the commerciant to which the payment is made
     */
    public void businessCase(final CommandInput command, final User neededUser,
                             final BusinessAccount neededAccount, final Card neededCard,
                             final double neededExchangeRate, final Commerciant neededCommerciant) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        String plan = neededAccount.getOwner().getPlan();

        double toRon = ExchangeRates.findCurrency(command.getCurrency(), "RON");
        double taxes = Utils.INITIAL_BALANCE;

        switch (plan) {
            case "standard" -> taxes = (Utils.MEDIUM_STUDENT_RATE
                    * command.getAmount()) * neededExchangeRate;
            case "silver" -> {
                if (command.getAmount() * toRon > Utils.LARGE_LIMIT) {
                    taxes = (Utils.SMALL_STUDENT_RATE * command.getAmount()) * neededExchangeRate;
                }
            }
            default -> taxes = Utils.INITIAL_BALANCE;
        }

        if (neededAccount.getBalance() < (command.getAmount() * neededExchangeRate + taxes)) {
            transaction.put("description", "Insufficient funds");
            transaction.put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction);

            return;
        }

        if (neededAccount.getOwner().equals(neededUser)) {
            neededAccount.subtractAmountFromBalance(command.getAmount()
                                                    * neededExchangeRate + taxes);
        } else {
            String role = neededAccount.getEmployees().get(neededUser);

            if (command.getAmount() * neededExchangeRate + taxes > neededAccount.getSpendingLimit()
                && role.equals("employee")) {
                return;
            }

            neededAccount.subtractAmountFromBalance(command.getAmount()
                                                    * neededExchangeRate + taxes);
            neededAccount.getSpendingPerEmployee().
                    get(neededUser).put(command.getTimestamp(),
                                        new CommerciantsDetails(command.getCommerciant(),
                                command.getAmount() * neededExchangeRate));
        }

        transaction.put("amount", command.getAmount() * neededExchangeRate + taxes);
        transaction.put("commerciant", command.getCommerciant());

        transaction.put("description", "Card payment");
        transaction.put("timestamp", command.getTimestamp());

        neededAccount.addTransaction(transaction);

        if (neededCommerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            cashbackStrategy = new NrOfTransactionsCashback();
        } else {
            cashbackStrategy = new SpendingThresholdCashback();
        }

        double balance = neededAccount.getBalance();
        neededAccount.getOwner().checkTransactions(command.getAmount() * toRon,
                                                    neededAccount.getAccountIBAN(),
                                                    command.getTimestamp());

        cashbackStrategy.applyCashback(command,
                                        neededAccount, neededAccount.getOwner(), neededCommerciant,
                                        neededExchangeRate);


        if (neededCard.getCardType().equals("one-time")) {
            ObjectNode transaction1 = mapper.createObjectNode();

            transaction1.put("account", neededAccount.getAccountIBAN());
            transaction1.put("card", neededCard.getCardNumber());
            transaction1.put("cardHolder", neededUser.getEmail());
            transaction1.put("description", "The card has been destroyed");
            transaction1.put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction1);

            neededAccount.generateNewCardNumber(neededCard);

            neededAccount.getCardsOfEmployees().put(neededUser, neededCard);

            ObjectNode transaction2 = mapper.createObjectNode();

            transaction2.put("account", neededAccount.getAccountIBAN());
            transaction2.put("card", neededAccount.getCards().getLast().getCardNumber());
            transaction2.put("cardHolder", neededUser.getEmail());
            transaction2.put("description", "New card created");
            transaction2.put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction2);
        }
    }

    /**
     * Create the transactions for a successful payment.
     * @param command - the command to be executed
     * @param neededUser - the user that made the payment
     * @param neededAccount - the account from which the payment is made
     * @param neededCard - the card used for the payment
     * @param neededExchangeRate - the exchange rate between the account currency
     *                              and the payment currency
     */
    public void createSuccesTransactions(final CommandInput command, final User neededUser,
                                        final Account neededAccount,
                                        final Card neededCard,
                                        final double neededExchangeRate,
                                         final Commerciant neededCommerciant) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        if (neededAccount.getAccountType().equals("business")) {
            businessCase(command, neededUser, (BusinessAccount) neededAccount, neededCard,
                         neededExchangeRate, neededCommerciant);
            return;
        }

        double toRon = ExchangeRates.findCurrency(command.getCurrency(), "RON");
        double taxes = Utils.INITIAL_BALANCE;

        switch (neededUser.getPlan()) {
            case "standard" -> taxes = (Utils.MEDIUM_STUDENT_RATE
                    * command.getAmount()) * neededExchangeRate;
            case "silver" -> {
                if (command.getAmount() * toRon > Utils.LARGE_LIMIT) {
                    taxes = (Utils.SMALL_STUDENT_RATE * command.getAmount()) * neededExchangeRate;
                }
            }
            default -> taxes = Utils.INITIAL_BALANCE;
        }

        if (neededAccount.getBalance() < (command.getAmount() * neededExchangeRate + taxes)) {
            transaction.put("description", "Insufficient funds");
            transaction.put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction);

            return;
        }

        transaction.put("amount", command.getAmount() * neededExchangeRate);
        transaction.put("commerciant", command.getCommerciant());
        transaction.put("description", "Card payment");
        transaction.put("timestamp", command.getTimestamp());

        neededAccount.addTransaction(transaction);

        CommerciantsDetails commerciant = new CommerciantsDetails(command.getCommerciant(),
                command.getAmount()
                        * neededExchangeRate);
        if (neededAccount.getAccountType().equals("classic")) {
            ((ClassicAccount) neededAccount).addCommerciant(commerciant);
        }

        if (neededCommerciant.getCashbackStrategy().equals("nrOfTransactions")) {
            cashbackStrategy = new NrOfTransactionsCashback();
        } else {
            cashbackStrategy = new SpendingThresholdCashback();
        }

        neededUser.checkTransactions(command.getAmount() * toRon,
                                        neededAccount.getAccountIBAN(),
                                        command.getTimestamp());


        cashbackStrategy.applyCashback(command, neededAccount, neededUser, neededCommerciant,
                                        neededExchangeRate);

        neededAccount.subtractAmountFromBalance(command.getAmount() * neededExchangeRate + taxes);

        if (neededCard.getCardType().equals("one-time")) {
            ObjectNode transaction1 = mapper.createObjectNode();

            transaction1.put("account", neededAccount.getAccountIBAN());
            transaction1.put("card", neededCard.getCardNumber());
            transaction1.put("cardHolder", neededUser.getEmail());
            transaction1.put("description", "The card has been destroyed");
            transaction1.put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction1);

            neededAccount.generateNewCardNumber(neededCard);

            ObjectNode transaction2 = mapper.createObjectNode();

            transaction2.put("account", neededAccount.getAccountIBAN());
            transaction2.put("card", neededAccount.getCards().getLast().getCardNumber());
            transaction2.put("cardHolder", neededUser.getEmail());
            transaction2.put("description", "New card created");
            transaction2.put("timestamp", command.getTimestamp());

            neededAccount.addTransaction(transaction2);
        }
    }

    /**
     * Execute the payOnline command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayNode output, final ArrayList<Commerciant> commerciants) {
        Card neededCard = null;
        Account neededAccount = null;
        User neededUser;

        if (command.getAmount() <= 0) {
            return;
        }

        neededUser = users.stream()
                .filter(user -> user.getEmail().equals(command.getEmail()))
                .findFirst()
                .orElse(null);

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(command.getCardNumber())) {
                        neededCard = card;
                        neededAccount = account;
                    }
                }
            }
        }

        if (createErrorTransactions(command, neededUser, neededCard, output, neededAccount)) {
            return;
        }

        assert neededAccount != null;
        double neededExchangeRate = ExchangeRates.findCurrency(command.getCurrency(),
                                                                neededAccount.getCurrency());

        Commerciant neededCommerciant = commerciants.stream()
                .filter(commerciant -> command.getCommerciant().equals(commerciant.getName()))
                .findFirst()
                .orElse(null);

        if (neededCommerciant == null) {
            return;
        }

        createSuccesTransactions(command, neededUser, neededAccount, neededCard,
                                 neededExchangeRate, neededCommerciant);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
