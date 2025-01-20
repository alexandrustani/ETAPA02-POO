package org.poo.commands.accountRelatedCommands;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Data;
import org.poo.account.BusinessAccount;
import org.poo.account.CommerciantsDetails;
import org.poo.commands.commandsCenter.CommandVisitor;
import org.poo.commands.commandsCenter.VisitableCommand;
import org.poo.fileio.CommandInput;
import org.poo.user.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * DetailsTransactionReport class
 */
@Data
final class DetailsTransactionReport {
    private double spending;
    private double deposit;

    /**
     * Constructor for DetailsTransactionReport
     * @param spending - the spending
     * @param deposit - the deposit
     */
    DetailsTransactionReport(final double spending, final double deposit) {
        this.setDeposit(deposit);
        this.setSpending(spending);
    }
}

/**
 * DetailsCommerciantReport class
 */
@Data
final class DetailsCommerciantReport {
    private ArrayList<String> managers;
    private ArrayList<String> employees;
    private double totalReceived;

    /**
     * Constructor for DetailsCommerciantReport
     */
    DetailsCommerciantReport() {
        setManagers(new ArrayList<>());
        setEmployees(new ArrayList<>());
        setTotalReceived(0);
    }
}

/**
 * BusinessReport class
 */
@Data
public final class BusinessReport implements VisitableCommand {
    /**
     * Empty constructor
     */
    public BusinessReport() {
    }

    /**
     * Get business account
     * @param command - the command to be executed
     * @param users - the list of users
     * @return the business account
     */
    public BusinessAccount getBusinessAccount(final CommandInput command,
                                              final ArrayList<User> users) {
        return users.stream()
                .flatMap(user -> user.getAccounts().stream())
                .filter(account -> account.getAccountIBAN().equals(command.getAccount())
                        && account.getAccountType().equals("business"))
                .map(account -> (BusinessAccount) account)
                .findFirst()
                .orElse(null);
    }

    /**
     * Report transaction
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output
     */
    public void reportTransaction(final CommandInput command, final ArrayList<User> users,
                                  final ArrayNode output) {
        BusinessAccount neededAccount =  getBusinessAccount(command, users);

        if (neededAccount == null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();

        commandOutput.put("command", command.getCommand());

        ObjectNode report = mapper.createObjectNode()
                .put("balance", neededAccount.getBalance())
                .put("IBAN", neededAccount.getAccountIBAN())
                .put("currency", neededAccount.getCurrency())
                .put("spending limit", neededAccount.getSpendingLimit())
                .put("deposit limit", neededAccount.getDepositLimit());

        LinkedHashMap<User, DetailsTransactionReport> detailsPerUser = new LinkedHashMap<>();

        double totalSpending = 0;
        double totalDeposit = 0;

        for (User user : neededAccount.getEmployees().keySet()) {
            double depositPerUser = 0;
            double spendingPerUser = 0;

            for (Integer timestamp : neededAccount.getDepositPerEmployee().get(user).keySet()) {
                if (timestamp >= command.getStartTimestamp()
                        && timestamp <= command.getEndTimestamp()) {
                    depositPerUser += neededAccount.getDepositPerEmployee()
                                                            .get(user).get(timestamp);
                }
            }

            for (Integer timestamp : neededAccount.getSpendingPerEmployee().get(user).keySet()) {
                if (timestamp >= command.getStartTimestamp()
                        && timestamp <= command.getEndTimestamp()) {
                    spendingPerUser += neededAccount.getSpendingPerEmployee()
                                                            .get(user).get(timestamp).getAmount();
                }
            }

            totalDeposit += depositPerUser;
            totalSpending += spendingPerUser;
            detailsPerUser.put(user, new DetailsTransactionReport(spendingPerUser,
                                                                  depositPerUser));
        }

        ArrayNode managers = mapper.createArrayNode();
        ArrayNode employees = mapper.createArrayNode();

        for (User user : detailsPerUser.keySet()) {
            ObjectNode userNode = mapper.createObjectNode()
                    .put("username", user.getLastName() + " " + user.getFirstName())
                    .put("deposited", detailsPerUser.get(user).getDeposit())
                    .put("spent", detailsPerUser.get(user).getSpending());

            if (neededAccount.getEmployees().get(user).equals("manager")) {
                managers.add(userNode);
            } else {
                employees.add(userNode);
            }
        }

        report.set("managers", managers);
        report.set("employees", employees);
        report.put("total deposited", totalDeposit);
        report.put("total spent", totalSpending);
        report.put("statistics type", command.getType());
        commandOutput.set("output", report);
        commandOutput.put("timestamp", command.getTimestamp());

        output.add(commandOutput);
    }

    /**
     * Execute the command
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output
     */
    public void execute(final CommandInput command, final ArrayList<User> users,
                        final ArrayNode output) {
        switch (command.getType()) {
            case "transaction" -> reportTransaction(command, users, output);
            case "commerciant" -> reportCommerciant(command, users, output);
            default -> throw new IllegalArgumentException("Not a valid businessReport type");
        }
    }

    /**
     * Report commerciant
     * @param command - the command to be executed
     * @param users - the list of users
     * @param output - the output
     */
    public void reportCommerciant(final CommandInput command, final ArrayList<User> users,
                                  final ArrayNode output) {
        BusinessAccount neededAccount =  getBusinessAccount(command, users);

        if (neededAccount == null) {
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        ObjectNode commandOutput = mapper.createObjectNode();
        commandOutput.put("command", command.getCommand());

        ArrayNode commerciants = mapper.createArrayNode();

        ObjectNode report = mapper.createObjectNode()
                .put("IBAN", command.getAccount())
                .put("balance", neededAccount.getBalance())
                .put("currency", neededAccount.getCurrency())
                .put("spending limit", neededAccount.getSpendingLimit())
                .put("deposit limit", neededAccount.getDepositLimit())
                .put("statistics type", command.getType());

        TreeMap<String, DetailsCommerciantReport> commerciantDetails = new TreeMap<>();

        neededAccount.getEmployees().forEach((user, role) -> {
            Map<Integer, CommerciantsDetails> userSpending
                    = neededAccount.getSpendingPerEmployee().get(user);

            if (userSpending == null) {
                return;
            }

            String userName = user.getLastName() + " " + user.getFirstName();

            userSpending.entrySet().stream()
                    .filter(entry -> entry.getKey()
                            >= command.getStartTimestamp()
                            && entry.getKey() <= command.getEndTimestamp())
                    .forEach(entry -> {
                        CommerciantsDetails details = entry.getValue();
                        DetailsCommerciantReport commerciantReport = commerciantDetails
                                .computeIfAbsent(details.getName(),
                                          k -> new DetailsCommerciantReport());

                        commerciantReport.setTotalReceived(
                                commerciantReport.getTotalReceived() + details.getAmount());

                        if ("manager".equals(role)) {
                            commerciantReport.getManagers().add(userName);
                        } else {
                            commerciantReport.getEmployees().add(userName);
                        }
                    });
        });

        commerciantDetails.forEach((commerciantName, details) -> {
            ObjectNode commerciantNode = mapper.createObjectNode();
            commerciantNode.put("commerciant", commerciantName);
            commerciantNode.put("total received", details.getTotalReceived());

            Collections.sort(details.getManagers());
            ArrayNode managersNode = mapper.createArrayNode();
            details.getManagers().forEach(managersNode::add);
            commerciantNode.set("managers", managersNode);

            Collections.sort(details.getEmployees());
            ArrayNode employeesNode = mapper.createArrayNode();
            details.getEmployees().forEach(employeesNode::add);
            commerciantNode.set("employees", employeesNode);

            commerciants.add(commerciantNode);
        });

        report.set("commerciants", commerciants);
        commandOutput.set("output", report);
        commandOutput.put("timestamp", command.getTimestamp());

        output.add(commandOutput);
    }

    @Override
    public void accept(final CommandVisitor commandVisitor) {
        commandVisitor.visit(this);
    }
}
