package org.poo.commands.cardRelatedCommands;

import lombok.Data;
import org.poo.card.Card;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.poo.account.Account;

import java.util.ArrayList;

@Data
public final class CreateCard implements VisitableCommand {
    /**
     * Empty constructor
     */
    public CreateCard() {
    }

    /**
     * Create card type
     * @param command to execute
     * @return card type
     */
    public String createCardType(final CommandInput command) {
        return switch (command.getCommand()) {
            case "createCard" -> "normal";
            case "createOneTimeCard" -> "one-time";
            default ->
                    throw new IllegalStateException("Unexpected Card command: "
                                                    + command.getCommand());
        };
    }

    /**
     * Execute the createCard command
     * @param command to execute
     * @param users to create card
     */
    public void execute(final CommandInput command, final ArrayList<User> users) {
        Account neededAccount = null;
        User neededUser = null;
        String cardType = createCardType(command);

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                if (account.getAccountIBAN().equals(command.getAccount())
                    && user.getEmail().equals(command.getEmail())) {
                    neededAccount = account;
                    neededUser = user;
                    break;
                }
            }
        }

        if (neededAccount == null) {
            return;
        }

        neededAccount.getCards().add(new Card(cardType));

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        transaction.put("account", neededAccount.getAccountIBAN());
        transaction.put("card", neededAccount.getCards().getLast().getCardNumber());
        transaction.put("cardHolder", neededUser.getEmail());
        transaction.put("description", "New card created");
        transaction.put("timestamp", command.getTimestamp());

        neededUser.addTransaction(transaction);
        neededAccount.addTransaction(transaction);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
