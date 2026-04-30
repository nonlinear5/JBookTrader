package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.preferences.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class PerformanceEvaluator {
    private final static long millisInYear = 1000L * 60L * 60L * 24L * 365L;
    private final List<TimedValue> tradeReturns;
    private double optimalLeverage, optimalGrowth, pi;

    public PerformanceEvaluator(List<TimedValue> tradeReturns) {
        this.tradeReturns = tradeReturns;
    }

    public double getOptimalLeverage() {
        return optimalLeverage;
    }

    public double getOptimalGrowth() {
        return optimalGrowth;
    }

    public double getPi() {
        return pi;
    }

    public void evaluate() {
        if (tradeReturns.size() <= 2) {
            return;
        }

        boolean hasLosses = false;
        for (TimedValue tradeReturn : tradeReturns) {
            if (tradeReturn.getValue() < 0) {
                hasLosses = true;
                break;
            }
        }

        if (!hasLosses) {
            optimalLeverage = optimalGrowth = pi = Double.POSITIVE_INFINITY;
            return;
        }


        String kernelName = PreferencesHolder.getInstance().get(JBTPreferences.Kernel);

        MaximumSearch search = new MaximumSearch();

        KellyEvaluator kellyEvaluator = new KellyEvaluator(tradeReturns, kernelName);
        double kellyLeverage = search.findArgMax(kellyEvaluator);
        double growth = kellyEvaluator.evaluateLog(kellyLeverage);

        YoudenEvaluator youdenEvaluator = new YoudenEvaluator(tradeReturns, kernelName, growth / kellyLeverage);
        optimalLeverage = search.findArgMax(youdenEvaluator);
        pi = youdenEvaluator.evaluateLog(optimalLeverage);

    }

}
