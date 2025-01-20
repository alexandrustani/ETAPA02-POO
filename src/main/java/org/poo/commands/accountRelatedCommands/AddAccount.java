package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import org.poo.account.FactoryOfAccount;
import org.poo.commands.commandsCenter.CommandVisitor;


import java.util.ArrayList;

/**
 * Add account to user
 */
@Data
public final class AddAccount implements VisitableCommand {
    /**
     * Empty constructor
     */
    public AddAccount() {
    }

    /**
     * Execute the addAccount command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param myCommerciants - the list of commerciants
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayList<Commerciant> myCommerciants) {
        User neededUser = null;

        for (User user : users) {
            if (user.getEmail().equals(command.getEmail())) {
                neededUser = user;
                break;
            }
        }

        if (neededUser == null) {
            throw new IllegalArgumentException("User not found");
        }

        neededUser.addAccount(FactoryOfAccount.createAccount(command, myCommerciants, neededUser));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        transaction.put("timestamp", command.getTimestamp());
        transaction.put("description", "New account created");

        neededUser.getAccounts().getLast().addTransaction(transaction);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
