package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class CentroidOptimizerRunner extends OptimizerRunner {
    private static final double goldenRatio = (Math.sqrt(5) + 1) / 2;
    private final int dimensions;
    private final StrategyParams startingParams;
    private final PerformanceMetric performanceMetric;
    private final boolean strictParameterBounds;

    CentroidOptimizerRunner(OptimizerDialog optimizerDialog, Strategy strategy, StrategyParams params) {
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

            int cutoff = (int) (optimizationResults.size() * (2 - goldenRatio));
            int counter = 0;
            for (OptimizationResult optimizationResult : optimizationResults) {
                counter++;
                if (counter > cutoff) {
                    break;
                }

                StrategyParams params = optimizationResult.getParams();
                double performanceValue = optimizationResult.get(performanceMetric);
                if (performanceValue > 0) {
                    performanceValue = Math.pow(performanceValue, 2);
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
                System.out.print(String.format("%.2f", d));
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
