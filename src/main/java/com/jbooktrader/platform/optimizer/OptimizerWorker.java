package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.ordermanager.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;
import java.util.concurrent.*;

/**
 * @author Eugene Kononov
 */
public class OptimizerWorker implements Callable<List<OptimizationResult>> {
    private final static long progressUpdateIterations = 10000L;
    private final OptimizerRunner optimizerRunner;
    private final List<StrategyParams> tasks;
    private final OrderManagerAssistant orderManagerAssistant;

    OptimizerWorker(OptimizerRunner optimizerRunner, List<StrategyParams> tasks) {
        this.optimizerRunner = optimizerRunner;
        this.tasks = tasks;
        orderManagerAssistant = Dispatcher.getInstance().getOrderManager().getAssistant();
    }

    public List<OptimizationResult> call() {
        List<OptimizationResult> optimizationResults = new ArrayList<>();
        if (optimizerRunner.isCancelled()) {
            return optimizationResults;
        }


        MarketBook marketBook = new MarketBook();
        IndicatorManager indicatorManager = new IndicatorManager();
        List<Strategy> strategies = new ArrayList<>(tasks.size());

        for (StrategyParams params : tasks) {
            Strategy strategy = optimizerRunner.getStrategyInstance(params);
            strategy.setMarketBook(marketBook);
            strategy.setIndicatorManager(indicatorManager);
            strategy.setIndicators();
            strategies.add(strategy);
        }

        TradingSchedule tradingSchedule = strategies.get(0).getTradingSchedule();
        List<MarketSnapshot> snapshots = optimizerRunner.getSnapshots();
        long snapshotsCount = snapshots.size();
        long strategiesCount = strategies.size();


        long iterationsDelta = 0;
        for (int count = 0; count < snapshotsCount; count++) {
            MarketSnapshot marketSnapshot = snapshots.get(count);
            marketBook.setSnapshot(marketSnapshot);
            boolean hasValidIndicators = indicatorManager.updateIndicators();

            boolean isInSchedule = tradingSchedule.contains(marketSnapshot.getTime());
            if (count < snapshotsCount - 1) {
                isInSchedule = isInSchedule && !marketBook.isGapping(snapshots.get(count + 1));
            }

            isInSchedule = isInSchedule && hasValidIndicators;
            for (Strategy strategy : strategies) {
                if (strategy.getPositionManager().getCurrentPosition() != 0) {
                    strategy.getPerformanceManager().updateMetrics();
                }

                if (!isInSchedule) {
                    strategy.goFlat();
                } else {
                    strategy.onBookSnapshot();
                }
                orderManagerAssistant.trade(strategy);
            }

            iterationsDelta += strategiesCount;
            if (iterationsDelta >= progressUpdateIterations) {
                if (optimizerRunner.isCancelled()) {
                    return optimizationResults;
                }

                optimizerRunner.iterationsCompleted(iterationsDelta);
                iterationsDelta = 0;
            }
        }

        optimizerRunner.iterationsCompleted(iterationsDelta);
        int minTrades = optimizerRunner.getMinTrades();
        String inclusionCriteria = optimizerRunner.getInclusionCriteria();

        for (Strategy strategy : strategies) {
            strategy.closePosition();
            PerformanceManager performanceManager = strategy.getPerformanceManager();
            performanceManager.updateAtEnd();
            if (performanceManager.getTrades() >= minTrades) {
                if (inclusionCriteria.equals("All strategies") || performanceManager.getNetProfit() > 0) {
                    OptimizationResult optimizationResult = new OptimizationResult(strategy.getParams(), performanceManager);
                    optimizationResults.add(optimizationResult);
                }
            }
        }

        return optimizationResults;
    }
}
