package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.*;

/**
 * Calculates the volatility of the book balance.
 * <p>
 * The volatility is represented by the standard deviation, which is calculated using the
 * Knuth-Welford algorihm. Reference: https://en.wikipedia.org/wiki/Algorithms_for_calculating_variance
 *
 * @author Eugene Kononov
 */
public class BalanceSigma extends Indicator {
    private double mean, m2;
    private int samples;

    public BalanceSigma() {
        super(0);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        samples++;
        double delta = balance - mean;
        mean += delta / samples;
        m2 += delta * (balance - mean);
        if (samples >= 2) {
            value = Math.sqrt(m2 / (samples - 1));
        }
    }

    @Override
    public void reset() {
        value = mean = m2 = samples = 0;
    }
}
