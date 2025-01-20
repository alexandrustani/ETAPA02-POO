package org.poo.commands.cardRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.Account;
import org.poo.account.BusinessAccount;
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
        User neededUser = users.stream()
                .filter(user -> user.getEmail().equals(command.getEmail()))
                .findFirst()
                .orElse(null);

        if (neededUser == null) {
            return;
        }

        Account neededAccount = null;
        Card neededCard = null;

        for (Account account : neededUser.getAccounts()) {
            for (Card card : account.getCards()) {
                if (card.getCardNumber().equals(command.getCardNumber())) {
                    neededAccount = account;
                    neededCard = card;
                    break;
                }
            }
        }

        if (neededAccount == null) {
            return;
        }

        if (neededAccount.getBalance() > 0) {
            return;
        }

        if (neededAccount.getAccountType().equals("business")
            && !((BusinessAccount) neededAccount).getCardsOfEmployees().containsKey(neededUser)
            && !((BusinessAccount) neededAccount)
                    .getEmployees().get(neededUser).equals("employee")) {
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

        neededAccount.getCards().remove(neededCard);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
