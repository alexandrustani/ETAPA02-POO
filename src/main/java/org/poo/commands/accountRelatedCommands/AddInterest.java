package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.account.SavingsAccount;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * Add interest command class.
 */
@Data
public final class AddInterest implements VisitableCommand {
    /**
     * Empty constructor
     */
    public AddInterest() {
    }

    /**
     * Execute the addInterest command.
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output array
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                               final ArrayNode output) {
        Account neededAccount = null;

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandNode = mapper.createObjectNode();

        commandNode.put("command", "addInterest");

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())) {
                    neededAccount = account;
                    break;
                }
            }
        }

        if (neededAccount == null) {
            return;
        }

        if (!neededAccount.getAccountType().equals("savings")) {
            ObjectNode error = mapper.createObjectNode();
            error.put("description", "This is not a savings account");
            error.put("timestamp", command.getTimestamp());

            commandNode.set("output", error);

            commandNode.put("timestamp", command.getTimestamp());

            output.add(commandNode);

            return;
        }

        ObjectNode transactionNode = mapper.createObjectNode()
                .put("timestamp", command.getTimestamp())
                .put("description", "Interest rate income")
                .put("amount", neededAccount.getBalance()
                                        * ((SavingsAccount) neededAccount).getInterestRate())
                        .put("currency", neededAccount.getCurrency());

        neededAccount.addTransaction(transactionNode);

        ((SavingsAccount) neededAccount).addInterestRate();
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
