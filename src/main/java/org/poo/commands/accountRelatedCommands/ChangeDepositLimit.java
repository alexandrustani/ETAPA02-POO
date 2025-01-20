package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.BusinessAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

public final class ChangeDepositLimit implements VisitableCommand {
    /**
     * Empty constructor.
     */
    public ChangeDepositLimit() {

    }

    /**
     * Execute the command.
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

        BusinessAccount neededAccount = neededUser.getAccounts().stream()
                .filter(account -> account.getAccountIBAN().equals(commandInput.getAccount())
                        && account.getAccountType().equals("business"))
                .map(account -> (BusinessAccount) account)
                .findFirst()
                .orElse(null);

        if (neededAccount == null) {
            ObjectNode commandOutput = getJsonNodes(commandInput);

            output.add(commandOutput);
            return;
        }

        neededAccount.setDepositLimit(commandInput.getAmount());
    }

    /**
     * Get the JSON nodes.
     * @param commandInput - the command input
     * @return the JSON nodes
     */
    private static ObjectNode getJsonNodes(final CommandInput commandInput) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();
        commandOutput.put("command", "changeDepositLimit");

        ObjectNode error = mapper.createObjectNode();
        error.put("description", "You must be owner in order to change deposit limit.");
        error.put("timestamp", commandInput.getTimestamp());

        commandOutput.set("output", error);
        commandOutput.put("timestamp", commandInput.getTimestamp());
        return commandOutput;
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
