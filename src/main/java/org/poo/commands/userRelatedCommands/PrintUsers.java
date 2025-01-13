package org.poo.commands.userRelatedCommands;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.user.User;
import org.poo.account.Account;
import org.poo.card.Card;

import java.util.ArrayList;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;

/**
 * Print users command
 */
@Data
public final class PrintUsers implements VisitableCommand {
    /**
     * Empty constructor
     */
    public PrintUsers() {

    }

    /**
     * Execute the printUsers command
     * @param listOfUsers - list of users
     * @param output - output array
     * @param timestamp - timestamp
     */
    public void execute(final ArrayList<User> listOfUsers, final ArrayNode output,
                               final int timestamp) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode command = mapper.createObjectNode();

        command.put("command", "printUsers");

        ArrayNode users = mapper.createArrayNode();
        for (User user : listOfUsers) {
            ObjectNode userNode = mapper.createObjectNode();

            userNode.put("firstName", user.getFirstName());
            userNode.put("lastName", user.getLastName());
            userNode.put("email", user.getEmail());

            ArrayNode accounts = mapper.createArrayNode();

            for (Account acc : user.getAccounts()) {
                ObjectNode account = mapper.createObjectNode();
                account.put("IBAN", acc.getAccountIBAN());
                account.put("balance", acc.getBalance());
                account.put("currency", acc.getCurrency());
                account.put("type", acc.getAccountType());
                ArrayNode cards = mapper.createArrayNode();
                for (Card card : acc.getCards()) {
                    ObjectNode cardNode = mapper.createObjectNode();
                    cardNode.put("cardNumber", card.getCardNumber());
                    cardNode.put("status", card.getCardStatus());
                    cards.add(cardNode);
                }
                account.set("cards", cards);
                accounts.add(account);
            }

            userNode.set("accounts", accounts);

            users.add(userNode);
        }
        command.set("output", users);
        command.put("timestamp", timestamp);
        output.add(command);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
