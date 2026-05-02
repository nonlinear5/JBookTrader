package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.TimedValue;

import java.util.List;

/**
 * @author Eugene Kononov
 */
public class KellyEvaluator extends FunctionEvaluator {

    public KellyEvaluator(List<TimedValue> tradeReturns, String kernelName) {
        super(tradeReturns, kernelName);
    }

    @Override
    public double evaluate(double leverage) {
        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += Math.log1p(leverage * r);
        }

        return sum;
    }


}
