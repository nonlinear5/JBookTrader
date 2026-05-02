package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.TimedValue;

import java.util.List;

/**
 * @author Eugene Kononov
 */
public class PowerEvaluator extends FunctionEvaluator {

    public PowerEvaluator(List<TimedValue> tradeReturns, String kernelName) {
        super(tradeReturns, kernelName);
    }


    @Override
    public double evaluate(double leverage) {
        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += (1 - Math.pow(1 + leverage * r, -3));
        }

        return sum;
    }

}
