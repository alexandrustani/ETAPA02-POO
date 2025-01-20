package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.account.Account;
import org.poo.utils.Utils;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

/**
 * Withdraw savings command class.
 */

@Data
public final class WithdrawSavings implements VisitableCommand {
    /**
     * Empty constructor
     */
    public WithdrawSavings() {

    }

    /**
     * Check if the user is of age.
     * @param birthDate - the birth date of the user
     * @return true if the user is of age, false otherwise
     */
    private boolean isUserOfAge(final String birthDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDateLocal = LocalDate.parse(birthDate, formatter);
        LocalDate currentDate = LocalDate.now();
        Period age = Period.between(birthDateLocal, currentDate);
        return age.getYears() >= Utils.AGE_LIMIT;
    }

    /**
     * Execute the withdrawSavings command.
     * @param commandInput - the command to be executed
     * @param users - the list of users
     */
    public void execute(final CommandInput commandInput, final ArrayList<User> users) {
        User neededUser;
        Account neededAccount;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();

        neededUser = users.stream()
                .filter(user -> user.getAccounts().stream()
                        .anyMatch(account
                                    -> account.getAccountIBAN().equals(commandInput.getAccount())))
                .findFirst()
                .orElse(null);

        if (neededUser != null) {
            neededAccount = neededUser.getAccounts().stream()
                    .filter(account -> account.getAccountIBAN().equals(commandInput.getAccount()))
                    .findFirst()
                    .orElse(null);
        } else {
            return;
        }

        if (neededAccount == null) {
            return;
        }

        if (!isUserOfAge(neededUser.getBirthday())) {
            commandOutput.put("timestamp", commandInput.getTimestamp());
            commandOutput.put("description", "You don't have the minimum age required.");

            neededAccount.addTransaction(commandOutput);

            return;
        }

        Account classicAccount = neededUser.getAccounts().stream()
                .filter(account -> account.getAccountType().equals("classic")
                        && account.getCurrency().equals(commandInput.getCurrency()))
                .findFirst()
                .orElse(null);

        if (classicAccount == null) {
            ObjectNode error = mapper.createObjectNode()
                    .put("description", "You do not have a classic account.")
                    .put("timestamp", commandInput.getTimestamp());

            neededAccount.addTransaction(error);

            return;
        }

        double exchangeRate = ExchangeRates.
                findCurrency(commandInput.getCurrency(),
                             classicAccount.getCurrency());

        if (neededAccount.getBalance() < commandInput.getAmount() * exchangeRate) {
            ObjectNode transactionNode = mapper.createObjectNode()
                    .put("timestamp", commandInput.getTimestamp())
                    .put("description", "Insufficient funds");

            neededAccount.addTransaction(transactionNode);

            return;
        }

        neededAccount.subtractAmountFromBalance(commandInput.getAmount() * exchangeRate);
        classicAccount.addAmountToBalance(commandInput.getAmount());

        ObjectNode transactionNode = mapper.createObjectNode()
                .put("timestamp", commandInput.getTimestamp())
                .put("description", "Savings withdrawal")
                .put("amount", commandInput.getAmount())
                .put("classicAccountIBAN", classicAccount.getAccountIBAN())
                .put("savingsAccountIBAN", neededAccount.getAccountIBAN());

        neededAccount.addTransaction(transactionNode);
        classicAccount.addTransaction(transactionNode);
    }

    @Override
    public void accept(final CommandVisitor command) {
        command.visit(this);
    }
}
