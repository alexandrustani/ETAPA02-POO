package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * ChangeSpendingLimit command class.
 */
@Data
public final class ChangeSpendingLimit implements VisitableCommand {
    /**
     * Empty constructor.
     */
    public ChangeSpendingLimit() {
    }

    /**
     * Execute the changeSpendingLimit command.
     * @param commandInput - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void execute(final CommandInput commandInput, final ArrayList<User> users,
                        final ArrayNode output) {
        User neededUser = users.stream()
                .filter(user -> user.getEmail().equals(commandInput.getEmail()))
                .findFirst()
                .orElse(null);

        if (neededUser == null) {
            return;
        }

        Account neededAccount = users.stream()
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountIBAN().equals(commandInput.getAccount()))
                .findFirst()
                .orElse(null);

        if (neededAccount == null) {
            return;
        }

        if (!neededAccount.getAccountType().equals("business")) {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", "changeSpendingLimit");

            ObjectNode error = mapper.createObjectNode();
            error.put("description", "This is not a business account");
            error.put("timestamp", commandInput.getTimestamp());

            commandOutput.set("output", error);
            commandOutput.put("timestamp", commandInput.getTimestamp());

            output.add(commandOutput);

            return;
        }

        if (neededAccount.getOwner().equals(neededUser)) {
            ((BusinessAccount) neededAccount).setSpendingLimit(commandInput.getAmount());
        } else {
            ObjectMapper mapper = new ObjectMapper();
            ObjectNode commandOutput = mapper.createObjectNode();
            commandOutput.put("command", "changeSpendingLimit");

            ObjectNode error = mapper.createObjectNode();
            error.put("description", "You must be owner in order to change spending limit.");
            error.put("timestamp", commandInput.getTimestamp());

            commandOutput.set("output", error);
            commandOutput.put("timestamp", commandInput.getTimestamp());

            output.add(commandOutput);
        }
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
