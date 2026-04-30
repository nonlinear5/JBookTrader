package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class ExponentialEvaluator extends FunctionEvaluator {

    public ExponentialEvaluator(List<TimedValue> tradeReturns, String kernelName) {
        super(tradeReturns, kernelName);
    }


    public double evaluate(double leverage) {
        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += (1 - Math.exp(-4 * leverage * r));
        }

        return sum;
    }

}
