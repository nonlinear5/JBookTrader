package com.jbooktrader.indicator.price;

import com.jbooktrader.platform.indicator.*;

/**
 * Normalized velocity of price
 *
 * @author Eugene Kononov
 */
public class LogPriceVelocity extends Indicator {
    private final double fastMultiplier, slowMultiplier;
    private double fast, slow;

    public LogPriceVelocity(int fastPeriod, int slowPeriod) {
        super(fastPeriod, slowPeriod);
        fastMultiplier = 2.0 / (fastPeriod + 1.0);
        slowMultiplier = 2.0 / (slowPeriod + 1.0);
    }

    @Override
    public void calculate() {
        double price = marketBook.getSnapshot().getPrice();
        fast += (price - fast) * fastMultiplier;
        slow += (price - slow) * slowMultiplier;
        value = 1000 * Math.log(fast / slow);
    }

    @Override
    public void reset() {
        fast = slow = marketBook.getSnapshot().getPrice();
    }
}
