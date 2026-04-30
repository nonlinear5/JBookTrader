package com.jbooktrader.platform.backtest;


import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.schedule.*;
import com.jbooktrader.platform.strategy.*;

import java.io.*;
import java.util.*;

/**
 * This class is responsible for running the strategy using historical market data
 *
 * @author Eugene Kononov
 */
class BackTester {
    private final Strategy strategy;
    private final BackTestFileReader backTestFileReader;
    private final BackTestDialog backTestDialog;

    BackTester(Strategy strategy, BackTestFileReader backTestFileReader, BackTestDialog backTestDialog) {
        this.strategy = strategy;
        this.backTestFileReader = backTestFileReader;
        this.backTestDialog = backTestDialog;
    }

    void execute()  {
        List<MarketSnapshot> snapshots = backTestFileReader.load(backTestDialog);
        PerformanceManager performanceManager = strategy.getPerformanceManager();

        MarketBook marketBook = strategy.getMarketBook();
        IndicatorManager indicatorManager = strategy.getIndicatorManager();
        performanceManager.createPerformanceChartData(backTestDialog.getBarSize(), indicatorManager.getIndicators());

        List<Indicator> indicators = indicatorManager.getIndicators();
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();
        PerformanceChartData performanceChartData = performanceManager.getPerformanceChartData();

        long snapshotsCount = snapshots.size();
        for (int count = 0; count < snapshotsCount; count++) {
            MarketSnapshot marketSnapshot = snapshots.get(count);
            marketBook.setSnapshot(marketSnapshot);

            performanceChartData.update(marketSnapshot);
            boolean hasValidIndicators = indicatorManager.updateIndicators();
            long instant = marketSnapshot.getTime();

            boolean isInSchedule = tradingSchedule.contains(instant);
            if (count < snapshotsCount - 1) {
                isInSchedule = isInSchedule && !marketBook.isGapping(snapshots.get(count + 1));
            }

            isInSchedule = isInSchedule && hasValidIndicators;
            strategy.processInstant(isInSchedule);
            if (hasValidIndicators) {
                performanceChartData.update(indicators, instant);
            }

            if (count % 100000 == 0) {
                backTestDialog.setProgress(count, snapshotsCount, "Running back test");
                if (backTestDialog.isCancelled()) {
                    break;
                }
            }
        }

        if (!backTestDialog.isCancelled()) {
            // go flat at the end of the test period to finalize the run
            strategy.closePosition();
            performanceManager.updateAtEnd();
            Dispatcher.getInstance().fireModelChanged(Event.StrategyUpdate, strategy);
        }
    }
}
