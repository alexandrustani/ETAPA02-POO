package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import java.util.ArrayList;
import java.util.HashMap;
import org.poo.exchangeRates.ExchangeRates;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Split command class.
 */
@Data
final class SplitCommand {
    private CommandInput split;
    private HashMap<String, Boolean> accountsForSplit;

    /**
     * Constructor for the SplitCommand class.
     * @param newCommand - the command to be split
     */
    SplitCommand(final CommandInput newCommand) {
        this.setSplit(newCommand);

        this.setAccountsForSplit(new HashMap<>());

        for (String account : newCommand.getAccounts()) {
            accountsForSplit.put(account, false);
        }
    }
}


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

    private static final ArrayList<SplitCommand> COMMANDS = new ArrayList<>();

    /**
     * Checks if the accounts can pay the specified amounts.
     * @param accounts - the list of accounts to check
     * @param amountPerAccount - the list of amounts to be paid from each account
     * @param exchangeRates - the list of exchange rates for each account
     * @return the account with insufficient funds, or null if all accounts can pay
     */
    public Account canPay(final ArrayList<Account> accounts,
                          final ArrayList<Double> amountPerAccount,
                          final ArrayList<Double> exchangeRates) {
        for (int i = 0; i < accounts.size(); i++) {
            if (accounts.get(i).getBalance()
                    < amountPerAccount.get(i) * exchangeRates.get(i)) {
                return accounts.get(i);
            }
        }

        return null;
    }

    /**
     * Accepts the payment.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void acceptPayment(final CommandInput command, final ArrayList<User> users,
                              final ArrayNode output) {
        SplitCommand commandToBeDone = null;
        User neededUser = null;

        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                neededUser = user;
            }
        }

        if (neededUser == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode();

            commandNode.put("command", "acceptSplitPayment");
            ObjectNode error = mapper.createObjectNode();
            error.put("description", "User not found");
            error.put("timestamp", command.getTimestamp());
            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);

            return;
        }

        for (SplitCommand splitCommand : COMMANDS) {
            for (Account account : neededUser.getAccounts()) {
                if (splitCommand.getAccountsForSplit().get(account.getAccountIBAN()) != null
                        && !splitCommand.getAccountsForSplit().get(account.getAccountIBAN())
                    && splitCommand.getSplit().getSplitPaymentType()
                        .equals(command.getSplitPaymentType())) {
                    splitCommand.getAccountsForSplit().put(account.getAccountIBAN(), true);
                }
            }

            if (splitCommand.getAccountsForSplit().values().
                    stream().allMatch(Boolean::booleanValue)
                    && splitCommand.getSplit().getSplitPaymentType()
                    .equals(command.getSplitPaymentType())) {
                commandToBeDone = splitCommand;

                break;
            }
        }

        if (commandToBeDone != null) {
            doSplitPayment(commandToBeDone.getSplit(), users);
            COMMANDS.remove(commandToBeDone);
        }
    }

    /**
     * Rejects the payment.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void rejectPayment(final CommandInput command, final ArrayList<User> users,
                              final ArrayNode output) {
        SplitCommand commandToBeDone = null;
        User neededUser = null;

        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                neededUser = user;
            }
        }

        if (neededUser == null) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandNode = mapper.createObjectNode();

            commandNode.put("command", "rejectSplitPayment");
            ObjectNode error = mapper.createObjectNode();
            error.put("description", "User not found");
            error.put("timestamp", command.getTimestamp());
            commandNode.set("output", error);
            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);

            return;
        }

        for (SplitCommand splitCommand : COMMANDS) {
            for (Account account : neededUser.getAccounts()) {
                if (splitCommand.getAccountsForSplit().containsKey(account.getAccountIBAN())
                    && splitCommand.getSplit().getSplitPaymentType()
                        .equals(command.getSplitPaymentType())) {
                    commandToBeDone = splitCommand;

                    break;
                }
            }
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        if (commandToBeDone != null) {
            COMMANDS.remove(commandToBeDone);
        } else {
            return;
        }

        if (commandToBeDone.getSplit().getSplitPaymentType().equals("equal")) {
            transaction.put("amount", commandToBeDone.getSplit().getAmount()
                    / commandToBeDone.getSplit().getAccounts().size());
        } else {
            transaction.set("amountForUsers", mapper.
                    valueToTree(commandToBeDone.getSplit().getAmountForUsers()));
        }
        transaction.put("currency", commandToBeDone.getSplit().getCurrency());
        transaction.put("description", String.format("Split payment of %.2f %s",
                commandToBeDone.getSplit().getAmount(), commandToBeDone.getSplit().getCurrency()));
        transaction.set("involvedAccounts",
                mapper.valueToTree(commandToBeDone.getSplit().getAccounts()));
        transaction.put("timestamp", commandToBeDone.getSplit().getTimestamp());
        transaction.put("splitPaymentType", commandToBeDone.getSplit().getSplitPaymentType());
        transaction.put("error", "One user rejected the payment.");

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (String accountIBAN : commandToBeDone.getSplit().getAccounts()) {
                    if (account.getAccountIBAN().equals(accountIBAN)) {
                        account.addTransaction(transaction);
                    }
                }
            }
        }
    }

    /**
     * Execute the splitPayment command.
     * @param command - the command to be executed
     */
    public void execute(final CommandInput command,
                        final ArrayList<User> users,
                        final ArrayNode output) {
        switch (command.getCommand()) {
            case "splitPayment" -> {
                COMMANDS.add(new SplitCommand(command));
            }

            case "acceptSplitPayment" -> {
                acceptPayment(command, users, output);
            }

            case "rejectSplitPayment" -> {
                rejectPayment(command, users, output);
            }

            default -> {
                System.out.println("Invalid command");
            }
        }
    }

    /**
     * Executes the split payment command.
     * @param command - the command to be executed
     * @param users - the list of users
     */
    public void doSplitPayment(final CommandInput command, final ArrayList<User> users) {
        ArrayList<User> usersToPay = new ArrayList<>();

        ArrayList<Account> accountsToPay = new ArrayList<>();

        ArrayList<Double> amountsPerUser = new ArrayList<>();

        double neededAmountPerAccount = command.getAmount() / command.getAccounts().size();

        if (command.getSplitPaymentType().equals("equal")) {
            for (int i = 0; i < command.getAccounts().size(); i++) {
                amountsPerUser.add(neededAmountPerAccount);
            }
        } else {
            amountsPerUser.addAll(command.getAmountForUsers());
        }

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
            exchangeRates.add(ExchangeRates.findCurrency(command.getCurrency(),
                                                         account.getCurrency()));
        }

        Account insufficientFunds = canPay(accountsToPay, amountsPerUser, exchangeRates);

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        if (command.getSplitPaymentType().equals("equal")) {
            transaction.put("amount", neededAmountPerAccount);
        } else {
            transaction.set("amountForUsers", mapper.valueToTree(command.getAmountForUsers()));
        }
        transaction.put("currency", command.getCurrency());
        transaction.put("description", String.format("Split payment of %.2f %s",
                command.getAmount(), command.getCurrency()));
        transaction.set("involvedAccounts", mapper.valueToTree(command.getAccounts()));
        transaction.put("timestamp", command.getTimestamp());
        transaction.put("splitPaymentType", command.getSplitPaymentType());

        if (insufficientFunds != null) {
            transaction.put("error",
                    String.format("Account %s has insufficient funds for a split payment.",
                                  insufficientFunds.getAccountIBAN()));

            for (Account account : accountsToPay) {
                account.addTransaction(transaction);
            }

            return;
        }

        for (int i = 0; i < accountsToPay.size(); i++) {
            accountsToPay.get(i).subtractAmountFromBalance(amountsPerUser.get(i)
                                                            * exchangeRates.get(i));

            accountsToPay.get(i).addTransaction(transaction);
        }
    }

    /**
     * Resets the split COMMANDS list by clearing all entries.
     */
    public static void resetCommands() {
        COMMANDS.clear();
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
