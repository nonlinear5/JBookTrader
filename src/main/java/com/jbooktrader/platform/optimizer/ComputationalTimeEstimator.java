package com.jbooktrader.platform.optimizer;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * "Remaining time" estimator for long-running computational processes, such as strategy optimization.
 *
 * @author Eugene Kononov
 */
class ComputationalTimeEstimator {
    private static final long MILLIS_IN_DAY = 1000 * 60 * 60 * 24;
    private final long startTime;
    private final SimpleDateFormat sdf;
    private final int availableProcessors;
    private final long totalIterations;
    private long updates;

    ComputationalTimeEstimator(long totalIterations, int availableProcessors) {
        this.totalIterations = totalIterations;
        this.availableProcessors = availableProcessors;
        startTime = System.currentTimeMillis();
        sdf = new SimpleDateFormat("HH:mm:ss");
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    String getTimeLeft(long completedIterations) {
        updates++;
        if (updates < availableProcessors) {
            return "calculating...";
        }

        long elapsedTime = System.currentTimeMillis() - startTime;
        double millisPerIteration = (double) elapsedTime / completedIterations;
        long remainingMillis = (long) (millisPerIteration * (totalIterations - completedIterations));
        long remainingDays = remainingMillis / MILLIS_IN_DAY;

        return (remainingDays == 0) ? sdf.format(remainingMillis) : "more than " + remainingDays + " days";
    }
}
