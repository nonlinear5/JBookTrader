package com.jbooktrader.platform.performance;


import com.jbooktrader.platform.chart.*;
import com.jbooktrader.platform.commission.*;
import com.jbooktrader.platform.email.*;
import com.jbooktrader.platform.indicator.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * Performance manager evaluates trading strategy performance based on statistics
 * which include various factors, such as net profit, maximum draw down, profit factor, etc.
 *
 * @author Eugene Kononov
 */
public class PerformanceManager {
    private final static Dispatcher dispatcher = Dispatcher.getInstance();
    private final int multiplier;
    private final Commission commission;
    private final Strategy strategy;
    private final List<TimedValue> tradeReturns;
    private PerformanceChartData performanceChartData;
    private int trades, profitableTrades, previousPosition;
    private double tradeCommission, totalCommission;
    private double totalBought, totalSold, positionValue;
    private double tradeProfit, netProfit, closedNetProfit, netProfitAsOfPreviousTrade;
    private double intraTradeDD, cumulativeIntraTradeDD;
    private double peakNetProfit, maxDrawdown, maxSingleLoss, optimalGrowth;
    private boolean isCompletedTrade;
    private long timeInMarket;
    private double optimalLeverage, pi;
    private Trade trade;

    public PerformanceManager(Strategy strategy, int multiplier, Commission commission) {
        this.strategy = strategy;
        this.multiplier = multiplier;
        this.commission = commission;
        tradeReturns = new ArrayList<>();
    }

    public void createPerformanceChartData(BarSize barSize, List<Indicator> indicators) {
        performanceChartData = new PerformanceChartData(barSize, indicators, strategy.getName());
    }

    public PerformanceChartData getPerformanceChartData() {
        return performanceChartData;
    }

    public int getTrades() {
        return trades;
    }

    public double getAveDuration() {
        if (trades == 0) {
            return 0;
        }
        // average number of minutes per trade
        return ((double) timeInMarket / trades) / 60000;
    }

    public boolean getIsCompletedTrade() {
        return isCompletedTrade;
    }

    public double getPercentProfitableTrades() {
        return (trades == 0) ? 0 : (100.0d * profitableTrades / trades);
    }

    public double getAverageProfitPerTrade() {
        return (trades == 0) ? 0 : netProfit / trades;
    }

    public double getMaxDrawdown() {
        return maxDrawdown;
    }

    public double getAPD() {
        if (cumulativeIntraTradeDD == 0) {
            return 0;
        }
        return netProfit / cumulativeIntraTradeDD;
    }

    public double getTradeProfit() {
        return tradeProfit;
    }

    public Commission getCommission() {
        return commission;
    }

    public double getTradeCommission() {
        return tradeCommission;
    }

    public double getNetProfit() {
        return totalSold - totalBought + positionValue - totalCommission;
    }

    public double getClosedNetProfit() {
        return closedNetProfit;
    }

    public double getOptimalGrowth() {
        return optimalGrowth;
    }

    public double getMaxSingleLoss() {
        return Math.abs(maxSingleLoss);
    }

    public void updateAtEnd() {
        if (!tradeReturns.isEmpty()) {
            PerformanceEvaluator performanceEvaluator = new PerformanceEvaluator(tradeReturns);
            performanceEvaluator.evaluate();
            optimalLeverage = performanceEvaluator.getOptimalLeverage();
            if (netProfit > 0) {
                pi = performanceEvaluator.getPi();
            }
            if (pi > 0 && getAPD() > 0) {
                optimalGrowth = pi * getAPD() * Math.pow(tradeReturns.size(), 0.5);
            }
            tradeReturns.clear();
        }
    }

    public double getPI() {
        return pi;
    }

    public double getOptimalLeverage() {
        return optimalLeverage;
    }

    public void updateMetrics() {
        int position = strategy.getPositionManager().getCurrentPosition();
        double price = strategy.getMarketBook().getSnapshot().getPrice();
        positionValue = position * price * multiplier;
        netProfit = totalSold - totalBought + positionValue - totalCommission;
        double pl = netProfit - netProfitAsOfPreviousTrade;
        intraTradeDD = Math.max(intraTradeDD, -pl);


        Mode mode = dispatcher.getMode();
        if (mode == Mode.BackTest) {
            long snapshotTime = strategy.getTime();
            TimedValue tv = new TimedValue(snapshotTime, netProfit);
            performanceChartData.updateStrategyPnL(tv);
            //performanceChartData.updatePortfolioPnL(tv);
        }


    }

    public void updateOnTrade(int quantity, double avgFillPrice, int position, double slippage) {
        long snapshotTime = strategy.getTime();
        if (previousPosition == 0 && position != 0) {
            trade = new Trade(multiplier);
            trade.setEntryTime(snapshotTime);
        }

        if (position == 0) {
            trade.setExitTime(snapshotTime);
        }

        double tradeAmount = avgFillPrice * Math.abs(quantity) * multiplier;
        if (quantity > 0) {
            trade.updateTotalBought(quantity, avgFillPrice, slippage);
            totalBought += tradeAmount;
        } else {
            trade.updateTotalSold(Math.abs(quantity), avgFillPrice, slippage);
            totalSold += tradeAmount;
        }

        tradeCommission = commission.getCommission(Math.abs(quantity), avgFillPrice);
        totalCommission += tradeCommission;

        Mode mode = dispatcher.getMode();
        boolean isNotificationRequired = (mode == Mode.ForwardTest || mode == Mode.Trade || mode == Mode.ForceClose);


        isCompletedTrade = (position == 0);
        if (isCompletedTrade) {
            trades++;

            cumulativeIntraTradeDD += intraTradeDD;
            intraTradeDD = 0;
            positionValue = 0;
            netProfit = totalSold - totalBought - totalCommission;
            closedNetProfit = netProfit;
            tradeProfit = netProfit - netProfitAsOfPreviousTrade;

            netProfitAsOfPreviousTrade = netProfit;

            if (tradeProfit >= 0) {
                profitableTrades++;
            }

            if (tradeProfit < maxSingleLoss) {
                maxSingleLoss = tradeProfit;
            }

            timeInMarket += trade.getTimeInMarket();

            double contractValue = avgFillPrice * multiplier;
            double tradeReturn = tradeProfit / contractValue;

            tradeReturns.add(new TimedValue(snapshotTime, tradeReturn));

            peakNetProfit = Math.max(netProfit, peakNetProfit);
            maxDrawdown = Math.max(maxDrawdown, peakNetProfit - netProfit);


            if (mode == Mode.BackTestAll) {
                performanceChartData.updatePortfolioPnL(new TimedValue(snapshotTime, tradeProfit));
                dispatcher.getPortfolioManager().updateOnTrade(tradeProfit);
            }


            if (isNotificationRequired) {
                TradeNotification tradeNotification = new TradeNotification(strategy, trade, tradeProfit);
                Notifier.getInstance().submit(tradeNotification.getText());
            }
        }

        if (mode == Mode.BackTest) {
            //performanceChartData.updateStrategyPnL(new TimedValue(snapshotTime, netProfit));
        }

        previousPosition = position;
    }
}
