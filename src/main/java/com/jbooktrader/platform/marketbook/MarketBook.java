package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.model.Dispatcher;

/**
 * Holds history of market snapshots for a trading instrument.
 *
 * @author Eugene Kononov
 */
public class MarketBook {
    private static final long GAP_SIZE = 60 * 60 * 1000;// 1 hour
    private static final long limitTimeMinutes = 15;// 15 minutes
    private MarketSnapshot marketSnapshot;
    private String contract;
    private double lastMidPrice;
    private long lastTimePriceChanged;
    private boolean isLocked;

    public boolean isEmpty() {
        return marketSnapshot == null;
    }

    public String getContract() {
        return contract;
    }

    public boolean isGapping(MarketSnapshot newMarketSnapshot) {
        return !isEmpty() && (newMarketSnapshot.getTime() - marketSnapshot.getTime() > GAP_SIZE);
    }

    public MarketSnapshot getSnapshot() {
        return marketSnapshot;
    }

    public void setSnapshot(MarketSnapshot marketSnapshot) {
        double midPrice = (marketSnapshot.getBid() + marketSnapshot.getAsk()) / 2d;
        long time = marketSnapshot.getTime();

        if (midPrice != lastMidPrice) {
            lastTimePriceChanged = time;
            lastMidPrice = midPrice;
            if (isLocked) {
                isLocked = false;
                Dispatcher.getInstance().getEventReport().report("MarketBook", "market is unlocked, midprice is " + midPrice);
            }
        } else {
            long timeElapsed = time - lastTimePriceChanged;
            long minutesElapsed = timeElapsed / (1000L * 60L);
            if (minutesElapsed >= limitTimeMinutes) {
                if (!isLocked) {
                    isLocked = true;
                    Dispatcher.getInstance().getEventReport().report("MarketBook", "market is locked, midprice is " + midPrice);
                }
            }
        }
        this.marketSnapshot = marketSnapshot;
        contract = marketSnapshot.getContract();
    }

    public boolean isLocked() {
        return isLocked;
    }
}
