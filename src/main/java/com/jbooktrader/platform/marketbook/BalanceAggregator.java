package com.jbooktrader.platform.marketbook;

import com.jbooktrader.platform.marketdepth.*;

/**
 * @author Eugene Kononov
 */
public class BalanceAggregator {
    private long samples;
    private double balancesSum;

    public void clear() {
        balancesSum = samples = 0;
    }

    public boolean isEmpty() {
        return samples == 0;
    }

    public double getBalance() {
        return 100d * (balancesSum / samples);
    }

    public void aggregate(MarketDepthModel bids, MarketDepthModel asks) {
        int cumulativeBid = bids.getCumulativeSize();
        int cumulativeAsk = asks.getCumulativeSize();
        double balance = (cumulativeBid - cumulativeAsk) / ((double) (cumulativeBid + cumulativeAsk));

        balancesSum += balance;
        samples++;
    }
}
