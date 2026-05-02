package com.jbooktrader.platform.portfolio;


import com.jbooktrader.platform.backtest.BackTestFileReader;
import com.jbooktrader.platform.chart.BarSize;
import com.jbooktrader.platform.indicator.IndicatorManager;
import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.ModelListener.Event;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.schedule.TradingSchedule;
import com.jbooktrader.platform.strategy.Strategy;

import java.io.IOException;
import java.util.List;

/**
 * @author Eugene Kononov
 */
public class PortfolioBackTester {
    private final List<Strategy> strategies;
    private final PortfolioBackTestDialog pod;

    public PortfolioBackTester(List<Strategy> strategies, PortfolioBackTestDialog pod) {
        this.strategies = strategies;
        this.pod = pod;
    }

    public void execute() throws IOException {
        pod.clear();
        for (Strategy strategy : strategies) {
            executeStrategy(strategy);
            pod.update(strategy);
        }
    }

    private void executeStrategy(Strategy strategy) {
        String symbol = strategy.getTicker();
        String marketDataDir = Dispatcher.getInstance().getMarketDataDir();

        String fileName = marketDataDir + symbol.toUpperCase() + ".txt";

        BackTestFileReader backTestFileReader = new BackTestFileReader(fileName, pod.getDateFilter());
        List<MarketSnapshot> snapshots = backTestFileReader.load(pod);
        PerformanceManager performanceManager = strategy.getPerformanceManager();

        MarketBook marketBook = strategy.getMarketBook();
        IndicatorManager indicatorManager = strategy.getIndicatorManager();
        performanceManager.createPerformanceChartData(BarSize.Hour1, indicatorManager.getIndicators());
        TradingSchedule tradingSchedule = strategy.getTradingSchedule();

        long snapshotsCount = snapshots.size();
        for (int count = 0; count < snapshotsCount; count++) {
            MarketSnapshot marketSnapshot = snapshots.get(count);
            marketBook.setSnapshot(marketSnapshot);

            boolean hasValidIndicators = indicatorManager.updateIndicators();
            long instant = marketSnapshot.getTime();

            boolean isInSchedule = tradingSchedule.contains(instant);
            if (count < snapshotsCount - 1) {
                isInSchedule = isInSchedule && !marketBook.isGapping(snapshots.get(count + 1));
            }

            isInSchedule = isInSchedule && hasValidIndicators;
            strategy.processInstant(isInSchedule);

            if (count % 100000 == 0) {
                pod.setProgress(count, snapshotsCount, "Running back test");
                if (pod.isCancelled()) {
                    break;
                }
            }
        }

        strategy.closePosition();
        strategy.getPerformanceManager().updateAtEnd();
        Dispatcher.getInstance().fireModelChanged(Event.StrategyUpdate, strategy);
    }
}
