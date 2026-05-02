package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.preferences.JBTPreferences;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.strategy.Strategy;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author Eugene Kononov
 */
public class GradientOptimizerRunner extends OptimizerRunner {
    private static final double goldenRatio = (Math.sqrt(5) + 1) / 2;
    private final int dimensions;
    private final StrategyParams startingParams;
    private final PerformanceMetric performanceMetric;
    private final boolean strictParameterBounds;

    GradientOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) {
        super(optimizerDialog, strategy, params);
        dimensions = params.size();
        startingParams = new StrategyParams(strategyParams);
        performanceMetric = optimizerDialog.getPerformanceMetric();
        strictParameterBounds = optimizerDialog.getParameterBounds().equals("Strict");
    }

    private double[] getCentroid() {
        double[] centroid = new double[dimensions];

        if (optimizationResults.isEmpty()) {
            for (int paramIndex = 0; paramIndex < dimensions; paramIndex++) {
                centroid[paramIndex] = startingParams.get(paramIndex).getMiddle();
            }
        } else {
            double sumOfPerformance = 0;


            double min, max;
            min = max = optimizationResults.get(0).get(performanceMetric);
            for (OptimizationResult optimizationResult : optimizationResults) {
                double performanceValue = optimizationResult.get(performanceMetric);
                if (performanceValue > max) {
                    max = performanceValue;
                } else if (performanceValue < min) {
                    min = performanceValue;
                }
            }

            if (min < 0) {
                min = 0;
            }

            double range = max - min;
            double beta = 2;


            for (OptimizationResult optimizationResult : optimizationResults) {

                StrategyParams params = optimizationResult.getParams();
                double performanceValue = optimizationResult.get(performanceMetric);
                if (performanceValue > 0 && performanceValue > min) {
                    double x = (performanceValue - min) / range;
                    //System.out.println(x);
                    performanceValue = 1 / (1 + Math.pow(x / (1 - x), -beta));
                    sumOfPerformance += performanceValue;

                    for (int paramIndex = 0; paramIndex < dimensions; paramIndex++) {
                        int paramValue = params.get(paramIndex).getValue();
                        centroid[paramIndex] += paramValue * performanceValue;
                    }
                }
            }

            for (int paramIndex = 0; paramIndex < dimensions; paramIndex++) {
                centroid[paramIndex] /= sumOfPerformance;
            }

        }

        return centroid;
    }

    @Override
    public void optimize() {
        double[] ranges = new double[dimensions];

        PreferencesHolder prefs = PreferencesHolder.getInstance();
        int partsPerDimension = prefs.getInt(JBTPreferences.DivideAndConquerCoverage);

        double[] centroid = getCentroid();
        for (int paramIndex = 0; paramIndex < dimensions; paramIndex++) {
            ranges[paramIndex] = startingParams.get(paramIndex).getRange();
        }

        double maxRange;
        Set<String> uniqueParams = new HashSet<>();
        System.out.println("---------------------");

        do {

            StrategyParams params = new StrategyParams(strategyParams);

            // define samples in the neightborhhoid
            for (int paramIndex = 0; paramIndex < dimensions; paramIndex++) {
                StrategyParam startingParam = startingParams.get(paramIndex);

                double displacement = ranges[paramIndex] / 2d;
                double center = centroid[paramIndex];
                double min = Math.floor(center - displacement);
                double max = Math.ceil(center + displacement);

                if (strictParameterBounds || startingParam.getRange() == 0) {
                    min = Math.max(min, startingParam.getMin());
                    max = Math.min(max, startingParam.getMax());
                }

                StrategyParam param = params.get(paramIndex);
                param.setMin((int) min);
                param.setMax((int) max);

                int step = Math.max(1, param.getRange() / (partsPerDimension - 1));
                param.setStep(step);
            }


            LinkedList<StrategyParams> tasks = new LinkedList<>(getTasks(params, uniqueParams));
            if (tasks.isEmpty()) {
                break;
            }

            passNumber++;
            execute(tasks, passNumber);

            if (cancelled.get()) {
                return;
            }

            if (optimizationResults.isEmpty()) {
                throw new RuntimeException("No strategies found within the specified parameter boundaries.");
            }

            centroid = getCentroid();
            optimizerDialog.setCentroid(centroid);

            System.out.print("pass #" + String.format("%d", passNumber) + ": ");
            System.out.print("centroid: ");
            for (double d : centroid) {
                System.out.printf("%.2f", d);
                System.out.print(", ");
            }

            System.out.println();

            maxRange = 0;
            for (int paramIndex = 0; paramIndex < dimensions; paramIndex++) {
                ranges[paramIndex] /= goldenRatio;
                maxRange = Math.max(maxRange, ranges[paramIndex]);
            }

        } while (maxRange > 1);
    }
}
