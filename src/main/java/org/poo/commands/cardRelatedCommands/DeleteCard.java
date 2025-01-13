package org.poo.commands.cardRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.card.Card;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

/**
 * Command to delete a card.
 */
@Data
public final class DeleteCard implements VisitableCommand {
    /**
     * Empty constructor
     */
    public DeleteCard() {

    }

    /**
     * Execute the deleteCard command.
     * @param command - the command to be executed
     * @param users - the list of users
     */
    public void execute(final CommandInput command, final ArrayList<User> users) {
        User neededUser = null;
        Account neededAccount = null;
        Card neededCard = null;

        for (User user : users) {
            for (Account account : user.getAccounts()) {
                for (Card card : account.getCards()) {
                    if (card.getCardNumber().equals(command.getCardNumber())) {
                        neededUser = user;
                        neededCard = card;
                        neededAccount = account;
                        break;
                    }
                }
            }
        }

        if (neededUser == null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode transaction = mapper.createObjectNode();

        transaction.put("account", neededAccount.getAccountIBAN());
        transaction.put("card", neededCard.getCardNumber());
        transaction.put("cardHolder", neededUser.getEmail());
        transaction.put("description", "The card has been destroyed");
        transaction.put("timestamp", command.getTimestamp());

        neededAccount.addTransaction(transaction);
        neededUser.addTransaction(transaction);

        neededAccount.getCards().remove(neededCard);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
