package org.poo.ebanking;

import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.poo.commands.commandsCenter.CommandExecutorVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.commands.accountRelatedCommands.AddInterest;
import org.poo.commands.accountRelatedCommands.AddAccount;
import org.poo.commands.accountRelatedCommands.ChangeInterestRate;
import org.poo.commands.accountRelatedCommands.DeleteAccount;
import org.poo.commands.accountRelatedCommands.SetMinimumBalance;
import org.poo.commands.accountRelatedCommands.SetAlias;
import org.poo.commands.accountRelatedCommands.Report;
import org.poo.commands.accountRelatedCommands.SpendingsReport;
import org.poo.commands.cardRelatedCommands.CheckCardStatus;
import org.poo.commands.cardRelatedCommands.CreateCard;
import org.poo.commands.cardRelatedCommands.DeleteCard;
import org.poo.commands.payoutRelatedCommands.AddFunds;
import org.poo.commands.payoutRelatedCommands.CashWithdrawal;
import org.poo.commands.payoutRelatedCommands.PayOnline;
import org.poo.commands.payoutRelatedCommands.SendMoney;
import org.poo.commands.payoutRelatedCommands.SplitPayment;
import org.poo.commands.payoutRelatedCommands.WithdrawSavings;
import org.poo.commands.userRelatedCommands.PrintTransactions;
import org.poo.commands.userRelatedCommands.PrintUsers;
import org.poo.commands.userRelatedCommands.UpgradePlan;
import org.poo.commerciants.Commerciant;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.fileio.CommandInput;
import org.poo.fileio.CommerciantInput;
import org.poo.fileio.ObjectInput;
import org.poo.user.User;

import java.util.ArrayList;

import org.poo.utils.Utils;

/**
 * Ebanking class
 */
@Data
public final class Ebanking {
    private static volatile Ebanking instance = null;
    private static ArrayList<User> users = new ArrayList<>();
    private static ArrayList<Commerciant> commerciants = new ArrayList<>();

    /**
     * Private constructor to prevent instantiation
     */
    private Ebanking() {
    }

    /**
     * Get the singleton instance of Ebanking
     * @return the single instance of Ebanking
     */
    public static Ebanking getInstance() {
        if (instance == null) {
            synchronized (Ebanking.class) {
                if (instance == null) {
                    instance = new Ebanking();
                }
            }
        }
        return instance;
    }

    /**
     * Create users from input
     * @param input - input object where I get the users
     */
    public void createUsers(final ObjectInput input) {
        for (int i = 0; i < input.getUsers().length; i++) {
            User user = new User(input.getUsers()[i].getFirstName(),
                    input.getUsers()[i].getLastName(),
                    input.getUsers()[i].getEmail(),
                    input.getUsers()[i].getBirthDate(),
                    input.getUsers()[i].getOccupation());
            users.add(user);
        }
    }

    /**
     * Create commerciants from input
     * @param input - input object where I get the commerciants
     */
    public void createCommerciants(final ObjectInput input) {
        for (CommerciantInput commerciant : input.getCommerciants()) {
            commerciants.add(new Commerciant(commerciant.getCommerciant(), commerciant.getId(),
                                             commerciant.getAccount(), commerciant.getType(),
                                             commerciant.getCashbackStrategy()));
        }
    }

    /**
     * System method that will be called from the main class
     * where I will call the methods necessary for my problem
     * @param input - input object
     * @param output - output object
     */
    public void system(final ObjectInput input, final ArrayNode output) {
        createUsers(input);
        ExchangeRates.create(input);
        createCommerciants(input);
        Utils.resetRandom();

        CommandExecutorVisitor visitor = new CommandExecutorVisitor(users, output, commerciants);

        for (CommandInput commandInput : input.getCommands()) {
            visitor.setCommandToExecute(commandInput);
            VisitableCommand command = createCommand(commandInput);
            if (command != null) {
                command.accept(visitor);
            }
        }
    }

    /**
     * Create a command based on the input
     * @param commandInput - the input command
     * @return the command to be executed
     */
    private VisitableCommand createCommand(final CommandInput commandInput) {
        return switch (commandInput.getCommand()) {
            case "addAccount" -> new AddAccount();
            case "createCard", "createOneTimeCard" -> new CreateCard();
            case "addFunds" -> new AddFunds();
            case "deleteAccount" -> new DeleteAccount();
            case "deleteCard" -> new DeleteCard();
            case "payOnline" -> new PayOnline();
            case "sendMoney" -> new SendMoney();
            case "printTransactions" -> new PrintTransactions();
            case "setMinBalance" -> new SetMinimumBalance();
            case "checkCardStatus" -> new CheckCardStatus();
            case "changeInterestRate" -> new ChangeInterestRate();
            case "splitPayment", "acceptSplitPayment", "rejectSplitPayment" -> new SplitPayment();
            case "addInterest" -> new AddInterest();
            case "report" -> new Report();
            case "spendingsReport" -> new SpendingsReport();
            case "setAlias" -> new SetAlias();
            case "printUsers" -> new PrintUsers();
            case "withdrawSavings" -> new WithdrawSavings();
            case "upgradePlan" -> new UpgradePlan();
            case "cashWithdrawal" -> new CashWithdrawal();
            default -> null;
        };
    }

    /**
     * Reset the Ebanking instance
     */
    public void reset() {
        users.clear();
        commerciants.clear();
        ExchangeRates.reset();
        Ebanking.instance = null;
        SplitPayment.resetCommands();
    }
}

