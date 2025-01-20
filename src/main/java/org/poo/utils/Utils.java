package org.poo.utils;

import java.util.Random;

public final class Utils {
    private Utils() {
        // Checkstyle error free constructor
    }

    private static final int IBAN_SEED = 1;
    private static final int CARD_SEED = 2;
    private static final int DIGIT_BOUND = 10;
    private static final int DIGIT_GENERATION = 16;
    private static final String RO_STR = "RO";
    private static final String POO_STR = "POOB";
    public static final double INITIAL_BALANCE = 0;
    public static final String CLASSIC = "classic";
    public static final String SAVINGS = "savings";
    public static final String ACTIVE = "active";
    public static final double FOOD_CASHBACK = 0.02;
    public static final double CLOTHES_CASHBACK = 0.05;
    public static final double TECH_CASHBACK = 0.1;
    public static final double FOOD_TRANSACTIONS = 2;
    public static final double CLOTHES_TRANSACTIONS = 5;
    public static final double FEE_STUDENT_OR_STANDARD_TO_SILVER = 100;
    public static final double FEE_SILVER_TO_GOLD = 250;
    public static final double FEE_STUDENT_OR_STANDARD_TO_GOLD = 350;
    public static final double TECH_TRANSACTIONS = 10;
    public static final double SMALL_LIMIT = 100;
    public static final double MEDIUM_LIMIT = 300;
    public static final double LARGE_LIMIT = 500;
    public static final double WARNING_LIMIT = 30;
    public static final double SMALL_STUDENT_RATE = 0.001;
    public static final double MEDIUM_STUDENT_RATE = 0.002;
    public static final double LARGE_STUDENT_RATE = 0.0025;
    public static final double SMALL_SILVER_RATE = 0.003;
    public static final double MEDIUM_SILVER_RATE = 0.004;
    public static final double LARGE_SILVER_RATE = 0.005;
    public static final double SMALL_GOLD_RATE = 0.005;
    public static final double MEDIUM_GOLD_RATE = 0.0055;
    public static final double LARGE_GOLD_RATE = 0.007;
    public static final double AGE_LIMIT = 21;
    public static final String BUSINESS_ACCOUNT = "business";
    public static final double EMPLOYEE_LIMIT_FEE = 500;
    private static Random ibanRandom = new Random(IBAN_SEED);
    private static Random cardRandom = new Random(CARD_SEED);

    /**
     * Utility method for generating an IBAN code.
     *
     * @return the IBAN as String
     */
    public static String generateIBAN() {
        StringBuilder sb = new StringBuilder(RO_STR);
        for (int i = 0; i < RO_STR.length(); i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        sb.append(POO_STR);
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(ibanRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Utility method for generating a card number.
     *
     * @return the card number as String
     */
    public static String generateCardNumber() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < DIGIT_GENERATION; i++) {
            sb.append(cardRandom.nextInt(DIGIT_BOUND));
        }

        return sb.toString();
    }

    /**
     * Resets the seeds between runs.
     */
    public static void resetRandom() {
        ibanRandom = new Random(IBAN_SEED);
        cardRandom = new Random(CARD_SEED);
    }
}
