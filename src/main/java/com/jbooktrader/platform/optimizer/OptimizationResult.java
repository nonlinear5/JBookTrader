package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.performance.PerformanceManager;


/**
 * Optimization result.
 *
 * @author Eugene Kononov
 */
public class OptimizationResult {
    private final double netProfit, maxSingleLoss, maxDrawdown, optimalGrowth, pi, apd, aveDuration;
    private final int trades;
    private final StrategyParams params;

    OptimizationResult(StrategyParams params, PerformanceManager performanceManager) {
        this.params = params;
        netProfit = performanceManager.getNetProfit();
        maxSingleLoss = performanceManager.getMaxSingleLoss();
        maxDrawdown = performanceManager.getMaxDrawdown();
        trades = performanceManager.getTrades();
        optimalGrowth = performanceManager.getOptimalGrowth();
        pi = performanceManager.getPI();
        apd = performanceManager.getAPD();
        aveDuration = performanceManager.getAveDuration();
    }

    public StrategyParams getParams() {
        return params;
    }

    public double get(PerformanceMetric pm) {
        switch (pm) {
            case Trades:
                return trades;
            case OG:
                return optimalGrowth;
            case PI:
                return pi;
            case APD:
                return apd;
            case MaxSL:
                return maxSingleLoss;
            case MaxDD:
                return maxDrawdown;
            case NetProfit:
                return netProfit;
            case Duration:
                return aveDuration;
        }
        throw new RuntimeException("Performance metric " + pm + " is not recognized.");
    }
}
