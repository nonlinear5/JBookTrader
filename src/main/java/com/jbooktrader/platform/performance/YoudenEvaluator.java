package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.TimedValue;

import java.util.List;

/**
 * @author Eugene Kononov
 */
public class YoudenEvaluator extends FunctionEvaluator {
    private final double indifferenceRatio;

    public YoudenEvaluator(List<TimedValue> tradeReturns, String kernelName, double indifferenceRatio) {
        super(tradeReturns, kernelName);
        this.indifferenceRatio = indifferenceRatio;
    }


    @Override
    public double evaluate(double leverage) {

        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += Math.log1p(leverage * r);
        }

        double indifferencePoint = leverage * indifferenceRatio;

        //double fractionGrowth = sum;
        return sum - indifferencePoint;

    }

}
