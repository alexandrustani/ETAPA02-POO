package org.poo.account;

import lombok.Getter;
import lombok.Setter;
import org.poo.card.Card;
import org.poo.commerciants.Commerciant;
import org.poo.exchangeRates.ExchangeRates;
import org.poo.user.User;
import org.poo.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * BusinessAccount class
 */
@Getter
@Setter
public final class BusinessAccount extends Account implements SpecialAccountFunctions {
    private LinkedHashMap<User, String> employees;
    private Double spendingLimit;
    private Double depositLimit;
    private HashMap<User, Map<Integer, CommerciantsDetails>> spendingPerEmployee;
    private HashMap<User, Map<Integer, Double>> depositPerEmployee;
    private HashMap<User, Card> cardsOfEmployees;

    /**
     * Constructor for BusinessAccount
     * @param currency for the account
     * @param commerciants for the account
     * @param owner for the account
     */
    public BusinessAccount(final String currency,
                           final ArrayList<Commerciant> commerciants,
                           final User owner) {
        super(currency, Utils.BUSINESS_ACCOUNT, commerciants, owner);
        setEmployees(new LinkedHashMap<>());
        setSpendingPerEmployee(new HashMap<>());
        setDepositPerEmployee(new HashMap<>());
        setCardsOfEmployees(new HashMap<>());

        double fromRon = ExchangeRates.findCurrency("RON", currency);

        setSpendingLimit(Utils.EMPLOYEE_LIMIT_FEE * fromRon);
        setDepositLimit(Utils.EMPLOYEE_LIMIT_FEE * fromRon);
    }

    @Override
    public void addEmployee(final User employee, final String role) {
        employees.put(employee, role);
        spendingPerEmployee.put(employee, new LinkedHashMap<>());
        depositPerEmployee.put(employee, new LinkedHashMap<>());
    }
}
