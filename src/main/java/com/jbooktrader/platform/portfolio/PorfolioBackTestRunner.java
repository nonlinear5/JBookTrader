package com.jbooktrader.platform.portfolio;

import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.ordermanager.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.ui.*;
import org.jfree.data.time.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class PorfolioBackTestRunner implements Runnable {
    private final PortfolioBackTestDialog pmd;
    private final List<Strategy> strategies;
    private final PortfolioChart portfolioChart;

    PorfolioBackTestRunner(PortfolioBackTestDialog pmd, List<Strategy> strategies, PortfolioChart portfolioChart) {
        this.pmd = pmd;
        this.strategies = strategies;
        this.portfolioChart = portfolioChart;
        OrderManagerAssistant orderManagerAssistant = Dispatcher.getInstance().getOrderManager().getAssistant();
        orderManagerAssistant.clearAllStrategies();
        for (Strategy strategy : strategies) {
            orderManagerAssistant.addStrategy(strategy);
        }
    }

    public void run() {
        try {
            pmd.enableProgress();
            PortfolioBackTester portfolioBackTester = new PortfolioBackTester(strategies, pmd);
            portfolioBackTester.execute();

            List<TimedValue> cumulativeProfits = new ArrayList<>();
            int trades = 0;
            double aveDuration = 0;
            for (Strategy strategy : strategies) {
                PerformanceManager pm = strategy.getPerformanceManager();
                PerformanceChartData pcd = pm.getPerformanceChartData();
                cumulativeProfits.addAll(pcd.getStrategyProfits());
                trades += pm.getTrades();
                aveDuration += pm.getAveDuration();
            }

            aveDuration /= strategies.size();

            cumulativeProfits.sort(Comparator.comparingLong(TimedValue::getTime));


            TimeSeries netProfit = new TimeSeries("Portfolio");
            double net = 0;
            for (TimedValue profit : cumulativeProfits) {
                net += profit.getValue();
                netProfit.addOrUpdate(new Second(new Date(profit.getTime())), net);
            }
            pmd.setPortfolioResults(trades, net, 0, 0, 0, 0, aveDuration);

            portfolioChart.disableNotifications();
            portfolioChart.clear();
            for (Strategy strategy : strategies) {
                PerformanceChartData pcd = strategy.getPerformanceManager().getPerformanceChartData();
                portfolioChart.update(pcd.getTradeProfitSeries());
            }
            portfolioChart.updatePortfolio(netProfit);
            portfolioChart.enableNotifications();


        } catch (Throwable t) {
            MessageDialog.showException(t);
        } finally {
            pmd.signalCompleted();
        }
    }
}
