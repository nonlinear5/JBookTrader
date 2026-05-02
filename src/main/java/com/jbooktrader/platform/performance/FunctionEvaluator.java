package com.jbooktrader.platform.performance;

import com.jbooktrader.platform.chart.TimedValue;

import java.util.List;

/**
 * @author Eugene Kononov
 */
public abstract class FunctionEvaluator {
    protected final List<TimedValue> tradeReturns;
    protected final KernelEvaluator kernelEvaluator;
    protected final long endTime, elapsedTime;

    public FunctionEvaluator(List<TimedValue> tradeReturns, String kernelName) {
        this.tradeReturns = tradeReturns;
        long startTime = tradeReturns.get(0).getTime();
        endTime = tradeReturns.get(tradeReturns.size() - 1).getTime();
        elapsedTime = endTime - startTime;
        KernelEvaluator.KernelType kernelType = KernelEvaluator.getKernelTypeByName(kernelName);
        kernelEvaluator = new KernelEvaluator(kernelType);
    }

    public abstract double evaluate(double leverage);

    protected double getWeightedReturn(TimedValue tradeReturn) {
        double distance = (endTime - tradeReturn.getTime()) / (double) elapsedTime;
        double kernelWeight = kernelEvaluator.getWeight(distance);
        return kernelWeight * tradeReturn.getValue();
    }

    public double evaluateLog(double leverage) {
        double sum = 0;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            sum += Math.log1p(r * leverage);
        }

        return sum;
    }


    public double getMaxLeverage() {
        double largestLoss = 0;
        //double totalReturn = 1;
        for (TimedValue tradeReturn : tradeReturns) {
            double r = getWeightedReturn(tradeReturn);
            //totalReturn *= (1 + r);
            if (r < largestLoss) {
                largestLoss = r;
            }
        }


        return -1 / largestLoss;

    }


}
