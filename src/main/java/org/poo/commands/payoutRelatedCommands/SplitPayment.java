package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;
import org.poo.exchangeRates.ExchangeRates;
import com.fasterxml.jackson.databind.node.ObjectNode;


/**
 * Split payment command class.
 */
@Data
public final class SplitPayment implements VisitableCommand {
    /**
     * Empty constructor
     */
    public SplitPayment() {

    }

    /**
     * Check if all the account can pay the amount.
     * @param accounts - the list of accounts
     * @param amountPerAccount - the amount to be paid
     * by the accounts
     * @return the Account with insufficient funds
     */
    public Account canPay(final ArrayList<Account> accounts, final Double amountPerAccount,
                                 final ArrayList<Double> exchangeRates) {
        Account insufficientFunds = null;

        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getBalance() < amountPerAccount * exchangeRates.get(i)) {
                insufficientFunds = accounts.get(i);
            }
        }

        return insufficientFunds;
    }

    /**
     * Execute the splitPayment command.
     * @param command - the command to be executed
     */
    public void execute(final CommandInput command,
                                final ArrayList<User> users) {
        ArrayList<User> usersToPay = new ArrayList<>();

        ArrayList<Account> accountsToPay = new ArrayList<>();

        double neededAmountPerAccount = command.getAmount() / command.getAccounts().size();

        ArrayList<Double> exchangeRates = new ArrayList<>();

        for (String accountIBAN : command.getAccounts()) {
            users.forEach(user -> user.getAccounts().forEach(account -> {
                if (account.getAccountIBAN().equals(accountIBAN)) {
                    usersToPay.add(user);
                    accountsToPay.add(account);
                }
            }));
        }

        for (Account account : accountsToPay) {
            if (account.getCurrency().equals(command.getCurrency())) {
                exchangeRates.add(1.0);
            } else {
                exchangeRates.add(ExchangeRates.findCurrency(command.getCurrency(),
                        account.getCurrency()));
            }
        }

        Account insufficientFunds = canPay(accountsToPay, neededAmountPerAccount, exchangeRates);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        transaction.put("amount", neededAmountPerAccount);
        transaction.put("currency", command.getCurrency());
        transaction.put("description", String.format("Split payment of %.2f %s",
                command.getAmount(), command.getCurrency()));
        transaction.set("involvedAccounts", mapper.valueToTree(command.getAccounts()));
        transaction.put("timestamp", command.getTimestamp());

        if (insufficientFunds != null) {
            transaction.put("error",
                            String.format("Account %s has insufficient funds for a split payment.",
                                            insufficientFunds.getAccountIBAN()));

            for (User user : usersToPay) {
                user.addTransaction(transaction);
            }

            for (Account account : accountsToPay) {
                account.addTransaction(transaction);
            }

            return;
        }

        for (int i = 0; i < accountsToPay.size(); i++) {
            accountsToPay.get(i).subtractAmountFromBalance(neededAmountPerAccount
                                                            * exchangeRates.get(i));

            usersToPay.get(i).addTransaction(transaction);
            accountsToPay.get(i).addTransaction(transaction);
        }
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
