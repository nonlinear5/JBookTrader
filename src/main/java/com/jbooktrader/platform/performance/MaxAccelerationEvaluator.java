package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class MaxAccelerationEvaluator extends FunctionEvaluator {

    public MaxAccelerationEvaluator(List<TimedValue> tradeReturns, String kernelName) {
        super(tradeReturns, kernelName);
    }

    @Override
    public double evaluate(double leverage) {
        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += Math.pow(r, 2) / Math.pow(1 + leverage * r, 2);
        }

        return -sum;
    }


}
