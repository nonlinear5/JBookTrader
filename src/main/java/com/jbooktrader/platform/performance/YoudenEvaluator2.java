package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class YoudenEvaluator2 extends FunctionEvaluator {
    private final double maxLeverage, maxGrowth;

    public YoudenEvaluator2(List<TimedValue> tradeReturns, String kernelName, double maxLeverage, double maxGrowth) {
        super(tradeReturns, kernelName);
        this.maxLeverage = maxLeverage;
        this.maxGrowth = maxGrowth;
    }


    @Override
    public double evaluate(double leverage) {

        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += Math.log1p(leverage * r);
        }

        double fractionOfGrowth = sum / maxGrowth;
        double fractionOfRisk = leverage / maxLeverage;

        return fractionOfGrowth - fractionOfRisk;

    }

}
