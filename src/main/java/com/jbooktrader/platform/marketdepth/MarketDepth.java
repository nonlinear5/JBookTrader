package com.jbooktrader.platform.marketdepth;

import com.ib.client.Contract;
import com.jbooktrader.platform.marketbook.BalanceAggregator;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.report.EventReport;
import com.jbooktrader.platform.util.ntp.DaySchedule;
import com.jbooktrader.platform.util.ntp.NTPClock;

import java.util.HashMap;
import java.util.Map;

/**
 * Holds history of market snapshots for a trading instrument.
 *
 * @author Eugene Kononov
 */
public class MarketDepth {
    private final static int OPERATION_INSERT = 0;
    private final static int OPERATION_UPDATE = 1;
    private final static int OPERATION_DELETE = 2;

    private final static int SIDE_ASK = 0;
    private final static int SIDE_BID = 1;

    private final static long millisInHour = 1000L * 60L * 60L;

    private final static int maxDepth = 10;
    private final NTPClock ntpclock;
    private final MarketDepthModel bids, asks;
    private final BalanceAggregator balanceAggregator;

    private final Contract contract;
    private final Map<Integer, Integer> volumes;
    private final Map<Integer, String> localSymbols;
    private final String symbol;
    private final EventReport eventReport;
    private final DaySchedule daySchedule;

    private String localSymbol;
    private double bestBidPrice, bestAskPrice;
    private int previousVolume;
    private int depthRequestId;
    private long lastSnapshotSeconds;
    private int mostLiquidId;
    private long lastResetTime;

    public MarketDepth(Contract contract) {
        this.contract = contract;
        bids = new MarketDepthModel();
        asks = new MarketDepthModel();
        balanceAggregator = new BalanceAggregator();
        symbol = contract.symbol();
        volumes = new HashMap<>();
        localSymbols = new HashMap<>();
        ntpclock = Dispatcher.getInstance().getNTPClock();
        eventReport = Dispatcher.getInstance().getEventReport();
        daySchedule = Dispatcher.getInstance().getDaySchedule();
    }

    public String getSymbol() {
        return symbol;
    }

    public Contract getContract() {
        return contract;
    }

    public void addLocalSymbol(int id, String localSymbol) {
        localSymbols.put(id, localSymbol);
    }

    public String getLocalSymbol(int id) {
        return localSymbols.get(id);
    }

    public synchronized int getDepthRequestId() {
        return depthRequestId;
    }

    public synchronized void setDepthRequestId(int depthRequestId) {
        this.depthRequestId = depthRequestId;
    }

    public synchronized int processVolume(int tickerId, int volume) {
        volumes.put(tickerId, volume);
        long timeNow = ntpclock.getTime();

        int hourOfDay = daySchedule.getHourOfDay();
        if (hourOfDay == 18) {
            long hoursSinceLastRestTime = (timeNow - lastResetTime) / millisInHour;
            if (hoursSinceLastRestTime > 2) {
                volumes.clear();
                lastResetTime = timeNow;
                eventReport.report("MarketDepth", "Volumes have been reset");
            }
        }

        boolean isTradingPeriod = daySchedule.isTradingPeriod();
        if (!isTradingPeriod) {
            int maxVolume = 0;
            int newMostLiquidId = 0;
            for (Map.Entry<Integer, Integer> entry : volumes.entrySet()) {
                int vol = entry.getValue();
                if (vol > maxVolume) {
                    maxVolume = vol;
                    newMostLiquidId = entry.getKey();
                }
            }
            if (newMostLiquidId != 0 && newMostLiquidId != mostLiquidId) {
                mostLiquidId = newMostLiquidId;
                localSymbol = localSymbols.get(mostLiquidId);
                return mostLiquidId;
            }
        }

        return -1;
    }

    public synchronized void reset() {
        bids.reset();
        asks.reset();
    }

    public synchronized void update(int position, int operation, int side, double price, int size) {

        if (position < 0 || position >= maxDepth) {
            eventReport.report("MarketDepth", "Invalid position: " + position);
            return;
        }

        if (side != SIDE_BID && side != SIDE_ASK) {
            eventReport.report("MarketDepth", "Unrecognized side: " + side);
            return;
        }

        MarketDepthModel model = (side == SIDE_BID) ? bids : asks;

        if (operation == OPERATION_INSERT) {
            model.insert(position, price, size);
        } else if (operation == OPERATION_UPDATE) {
            model.update(position, price, size);
        } else if (operation == OPERATION_DELETE) {
            model.delete(position);
        } else {
            eventReport.report("MarketDepth", "Invalid operation: " + operation);
            return;
        }

        if (isValidDepth()) {
            balanceAggregator.aggregate(bids, asks);
            bestBidPrice = bids.getBestPrice();
            bestAskPrice = asks.getBestPrice();
        }
    }


    private boolean isValidDepth() {
        if (bids.getSize() != maxDepth || asks.getSize() != maxDepth) {
            return false;
        }

        if (bids.getBestPrice() >= asks.getBestPrice()) {
            return false;
        }

        return bids.hasValidBidStructure() && asks.hasValidAskStructure();
    }

    public synchronized MarketSnapshot takeMarketSnapshot() {
        if (balanceAggregator.isEmpty()) {
            return null;
        }

        if (!volumes.containsKey(mostLiquidId)) {
            return null;
        }

        long timeSeconds = ntpclock.getTime() / 1000L; // drop the millis
        if (timeSeconds <= lastSnapshotSeconds) {
            return null;
        }

        lastSnapshotSeconds = timeSeconds;

        // one second volume
        int volume = volumes.get(mostLiquidId);
        int oneSecondVolume = (previousVolume == 0) ? 0 : Math.max(0, volume - previousVolume);
        previousVolume = volume;

        double averageBalance = balanceAggregator.getBalance();
        balanceAggregator.clear();

        // finally, create the snapshot
        return new MarketSnapshot(localSymbol, timeSeconds * 1000L, averageBalance, bestBidPrice, bestAskPrice, oneSecondVolume);
    }
}