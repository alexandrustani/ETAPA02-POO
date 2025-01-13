package org.poo.commands.commandsCenter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.Data;
import org.poo.commands.accountRelatedCommands.AddAccount;
import org.poo.commands.accountRelatedCommands.AddInterest;
import org.poo.commands.accountRelatedCommands.ChangeInterestRate;
import org.poo.commands.accountRelatedCommands.DeleteAccount;
import org.poo.commands.accountRelatedCommands.SetMinimumBalance;
import org.poo.commands.accountRelatedCommands.Report;
import org.poo.commands.accountRelatedCommands.SetAlias;
import org.poo.commands.accountRelatedCommands.SpendingsReport;
import org.poo.commands.cardRelatedCommands.CheckCardStatus;
import org.poo.commands.cardRelatedCommands.CreateCard;
import org.poo.commands.cardRelatedCommands.DeleteCard;
import org.poo.commands.payoutRelatedCommands.*;
import org.poo.commands.userRelatedCommands.PrintTransactions;
import org.poo.commands.userRelatedCommands.PrintUsers;
import org.poo.commands.userRelatedCommands.UpgradePlan;
import org.poo.commerciants.Commerciant;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;

@Data
public final class CommandExecutorVisitor implements CommandVisitor{
    private final ArrayList<User> users;
    private final ArrayNode output;
    private CommandInput commandToExecute;
    private final ArrayList<Commerciant> commerciants;
    private final ObjectMapper mapper = new ObjectMapper();

    public CommandExecutorVisitor(final ArrayList<User> users, final ArrayNode output,
                                  final ArrayList<Commerciant> commerciants) {
        this.users = users;
        this.output = output;
        this.commerciants = commerciants;
    }

    @Override
    public void visit(final AddInterest addInterest) {
        addInterest.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final AddAccount addAccount) {
        addAccount.execute(commandToExecute, users);
    }

    @Override
    public void visit(final CreateCard createCard) {
        createCard.execute(commandToExecute, users);
    }

    @Override
    public void visit(final ChangeInterestRate changeInterestRate) {
        changeInterestRate.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final DeleteAccount deleteAccount) {
        deleteAccount.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final DeleteCard deleteCard) {
        deleteCard.execute(commandToExecute, users);
    }

    @Override
    public void visit(final Report report) {
        report.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final SendMoney sendMoney) {
        sendMoney.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final SetAlias setAlias) {
        setAlias.execute(commandToExecute, users);
    }

    @Override
    public void visit(final SetMinimumBalance setMinimumBalance) {
        setMinimumBalance.execute(commandToExecute, users);
    }

    @Override
    public void visit(final SpendingsReport spendingsReport) {
        spendingsReport.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final SplitPayment splitPayment) {
        splitPayment.execute(commandToExecute, users);
    }

    @Override
    public void visit(final PayOnline payOnline) {
        payOnline.execute(commandToExecute, users, output, commerciants);
    }

    @Override
    public void visit(final PrintTransactions printTransactions) {
        printTransactions.execute(commandToExecute, users, output, mapper);
    }

    @Override
    public void visit(final PrintUsers printUsers) {
        printUsers.execute(users, output, commandToExecute.getTimestamp());
    }

    @Override
    public void visit(final AddFunds addFunds) {
        addFunds.execute(commandToExecute, users);
    }

    @Override
    public void visit(CheckCardStatus checkCardStatus) {
        checkCardStatus.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(WithdrawSavings withdrawSavings) {
        withdrawSavings.execute(commandToExecute, users);
    }

    @Override
    public void visit(UpgradePlan upgradePlan) {
        upgradePlan.execute(commandToExecute, users, output);
    }

    @Override
    public void visit(final CashWithdrawal cashWithdrawal) {
        cashWithdrawal.execute(commandToExecute, users, output);
    }
}
