package org.poo.commands.payoutRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.account.Account;

import java.time.LocalDate;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


@Data
public final class WithdrawSavings implements VisitableCommand {
    public WithdrawSavings() {}

    private boolean isUserOfAge(final String birthDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate birthDateLocal = LocalDate.parse(birthDate, formatter);
        LocalDate currentDate = LocalDate.now();
        Period age = Period.between(birthDateLocal, currentDate);
        return age.getYears() >= 21;
    }

    public void execute(final CommandInput commandInput, final ArrayList<User> users) {
        User neededUser = null;
        Account neededAccount = null;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();

        neededUser = users.stream()
                .filter(user -> user.getAccounts().stream()
                        .anyMatch(account -> account.getAccountIBAN().equals(commandInput.getAccount())))
                .findFirst()
                .orElse(null);

        if (neededUser != null) {
            neededAccount = neededUser.getAccounts().stream()
                    .filter(account -> account.getAccountIBAN().equals(commandInput.getAccount()))
                    .findFirst()
                    .orElse(null);
        }

        if (neededUser == null) {
            return;
        }

        if (!isUserOfAge(neededUser.getBirthday())) {
            commandOutput.put("timestamp", commandInput.getTimestamp());
            commandOutput.put("description", "You don't have the minimum age required.");

            neededAccount.addTransaction(commandOutput);

            return;
        }

        Account classicAccount = neededUser.getAccounts().stream()
                .filter(account -> account.getAccountType().equals("classic"))
                .findFirst()
                .orElse(null);

        if (classicAccount == null) {
            ObjectNode error = mapper.createObjectNode()
                    .put("description", "You do not have a classic account.")
                    .put("timestamp", commandInput.getTimestamp());

            neededAccount.addTransaction(error);

            return;
        }
    }

    @Override
    public void accept(CommandVisitor command) {
        command.visit(this);
    }
}
