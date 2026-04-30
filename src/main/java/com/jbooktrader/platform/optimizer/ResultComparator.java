package com.jbooktrader.platform.optimizer;

import java.util.*;

/**
 * Comparator for strategy optimization results.
 *
 * @author Eugene Kononov
 */
public class ResultComparator implements Comparator<OptimizationResult> {
    private final PerformanceMetric performanceMetric;

    public ResultComparator(PerformanceMetric performanceMetric) {
        this.performanceMetric = performanceMetric;
    }

    public int compare(OptimizationResult r1, OptimizationResult r2) {
        return Double.compare(r2.get(performanceMetric), r1.get(performanceMetric));
    }
}
