package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class PowerEvaluatorHalfKelly extends FunctionEvaluator {

    public PowerEvaluatorHalfKelly(List<TimedValue> tradeReturns, String kernelName) {
        super(tradeReturns, kernelName);
    }


    @Override
    public double evaluate(double leverage) {
        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += (1 - Math.pow(1 + leverage * r, -1));
        }

        return sum;
    }


}
