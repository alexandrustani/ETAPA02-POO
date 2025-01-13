package org.poo.exchangeRates;

import lombok.Data;
import org.poo.fileio.ObjectInput;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Class that represents the exchange rates.
 */
@Data
public final class ExchangeRates {
    /**
     * Utility class.
     */
    private ExchangeRates() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static Map<String, Map<String, Double>> exchangeRates = new HashMap<>();

    /**
     * Create exchange rates from input
     * @param input - input object where I get the exchange rates
     */
    public static void create(final ObjectInput input) {
        for (int i = 0; i < input.getExchangeRates().length; i++) {
            String from = input.getExchangeRates()[i].getFrom();
            String to = input.getExchangeRates()[i].getTo();
            double rate = input.getExchangeRates()[i].getRate();
            exchangeRates
                    .computeIfAbsent(from, k -> new HashMap<>())
                    .put(to, rate);

            exchangeRates
                    .computeIfAbsent(to, k -> new HashMap<>())
                    .put(from, 1 / rate);
        }
    }

    /**
     * Reset the exchange rates.
     */
    public static void reset() {
        exchangeRates.clear();
    }

    /**
     * Returns the exchange rate between two currencies, using direct or indirect conversions.
     * @param fromCurrency the currency to convert from
     * @param toCurrency the currency to convert to
     * @return the exchange rate, or -1.0 if no rate exists
     */
    public static Double findCurrency(final String fromCurrency, final String toCurrency) {
        if (fromCurrency.equals(toCurrency)) {
            return 1.0;
        }

        if (exchangeRates.containsKey(fromCurrency)
            && exchangeRates.get(fromCurrency).containsKey(toCurrency)) {
            return exchangeRates.get(fromCurrency).get(toCurrency);
        }

        /*
          Perform a BFS to find the exchange rate between the two currencies
         */
        Map<String, Double> visited = new HashMap<>();
        visited.put(fromCurrency, 1.0);
        Queue<String> queue = new LinkedList<>();
        queue.add(fromCurrency);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            double currentRate = visited.get(current);

            if (exchangeRates.containsKey(current)) {
                for (Map.Entry<String, Double> neighbor : exchangeRates.get(current).entrySet()) {
                    String nextCurrency = neighbor.getKey();
                    double nextRate = neighbor.getValue();

                    if (!visited.containsKey(nextCurrency)) {
                        visited.put(nextCurrency, currentRate * nextRate);
                        queue.add(nextCurrency);

                        /*
                          If the next currency is the target currency, return the exchange rate
                         */
                        if (nextCurrency.equals(toCurrency)) {
                            return visited.get(nextCurrency);
                        }
                    }
                }
            }
        }

        /*
          If no path exists between the two currencies, return -1.0
         */
        return -1.0;
    }
}
