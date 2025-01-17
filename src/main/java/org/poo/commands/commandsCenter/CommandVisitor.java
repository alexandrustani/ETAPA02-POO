package org.poo.commands.commandsCenter;

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

/**
 * Interface for the CommandVisitor.
 */
public interface CommandVisitor {
    /**
     * Visits an AddAccount command.
     * @param addAccount - the AddAccount command to visit
     */
    void visit(AddAccount addAccount);

    /**
     * Visits a CreateCard command.
     * @param createCard - the CreateCard command to visit
     */
    void visit(CreateCard createCard);

    /**
     * Visits a AddInterest command.
     * @param addInterest - the AddInterest command to visit
     */
    void visit(AddInterest addInterest);

    /**
     * Visits a ChangeInterestRate command
     * @param changeInterestRate - the ChangeInterestRate command to visit
     */
    void visit(ChangeInterestRate changeInterestRate);

    /**
     * Visits a DeleteAccount command.
     * @param deleteAccount - the DeleteAccount command to visit
     */
    void visit(DeleteAccount deleteAccount);

    /**
     * Visits a DeleteCard command.
     * @param deleteCard - the DeleteCard command to visit
     */
    void visit(DeleteCard deleteCard);

    /**
     * Visits a Report command.
     * @param report - the Report command to visit
     */
    void visit(Report report);

    /**
     * Visits a SendMoney command.
     * @param sendMoney - the SendMoney command to visit
     */
    void visit(SendMoney sendMoney);

    /**
     * Visits a SetAlias command.
     * @param setAlias - the SetAlias command to visit
     */
    void visit(SetAlias setAlias);

    /**
     * Visits a SetMinimumBalance command.
     * @param setMinimumBalance - the SetMinimumBalance command to visit
     */
    void visit(SetMinimumBalance setMinimumBalance);

    /**
     * Visits a SpendingsReport command.
     * @param spendingsReport - the SpendingsReport command to visit
     */
    void visit(SpendingsReport spendingsReport);

    /**
     * Visits a SplitPayment command.
     * @param splitPayment - the SplitPayment command to visit
     */
    void visit(SplitPayment splitPayment);

    /**
     * Visits a PayOnline command.
     * @param payOnline - the PayOnline command to visit
     */
    void visit(PayOnline payOnline);

    /**
     * Visits a PrintTransactions command.
     * @param printTransactions - the PrintTransactions command to visit
     */
    void visit(PrintTransactions printTransactions);

    /**
     * Visits a PrintUsers command.
     * @param printUsers - the PrintUsers command to visit
     */
    void visit(PrintUsers printUsers);

    /**
     * Visits an AddFunds command.
     * @param addFunds - the AddFunds command to visit
     */
    void visit(AddFunds addFunds);

    /**
     * Visits a CheckCardStatus command.
     * @param checkCardStatus - the CheckCardStatus command to visit
     */
    void visit(CheckCardStatus checkCardStatus);

    /**
     * Visits WithdrawSavings command
     * @param withdrawSavings - the WithdrawSavings command to visit
     */
    void visit(WithdrawSavings withdrawSavings);

    /**
     * Visits a UpgradePlan command.
     * @param upgradePlan - the UpgradePlan command to visit
     */
    void visit(UpgradePlan upgradePlan);

    /**
     * Visits a CashWithdrawal command.
     * @param cashWithdrawal - the CashWithdrawal command to visit
     */
    void visit(CashWithdrawal cashWithdrawal);
}
