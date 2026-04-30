package com.jbooktrader.platform.optimizer;

/**
 * @author Eugene Kononov
 */
public enum PerformanceMetric {
    Trades("Trades"), // number of trades
    Duration("Duration"), // average trade duration in minutes
    MaxSL("MSL"), // maximum single loss
    MaxDD("MDD"), // maximum drawdown
    APD("APD"), // average intraday profit to drawdown
    OG("OG"), // optimal growth
    PI("PI"), // performance index
    NetProfit("Net Profit");

    private final String name;

    PerformanceMetric(String name) {
        this.name = name;
    }

    public static PerformanceMetric getColumn(String name) {
        for (PerformanceMetric performanceMetric : values()) {
            if (performanceMetric.name.equals(name)) {
                return performanceMetric;
            }
        }
        return null;
    }

    public String getName() {
        return name;
    }
}
