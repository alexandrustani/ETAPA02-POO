package org.poo.commerciants;

import lombok.Data;

@Data
public final class Commerciant {
    private String name;
    private Integer id;
    private String account;
    private String type;
    private String cashbackStrategy;
    private double balance;

    /**
     * Constructor for Commerciant
     * @param name of the commerciant
     * @param id of the commerciant
     * @param account of the commerciant
     * @param type of the commerciant
     * @param cashbackStrategy of the commerciant
     */
    public Commerciant(final String name, final Integer id, final String account,
                       final String type, final String cashbackStrategy) {
        setId(id);
        setName(name);
        setAccount(account);
        setType(type);
        setCashbackStrategy(cashbackStrategy);
        setBalance(0);
    }
}
