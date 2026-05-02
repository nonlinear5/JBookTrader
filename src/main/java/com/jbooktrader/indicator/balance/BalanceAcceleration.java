package com.jbooktrader.indicator.balance;

import com.jbooktrader.platform.indicator.Indicator;

/**
 * Balance derivatives
 *
 * @author Eugene Kononov
 */
public class BalanceAcceleration extends Indicator {
    private final double fastMult, intermMult;
    private double fastAve, intermAve;

    public BalanceAcceleration(int period) {
        super(period);
        fastMult = 2.0 / (period + 1);
        intermMult = 2.0 / (2 * period + 1);
    }

    @Override
    public void calculate() {
        double balance = marketBook.getSnapshot().getBalance();
        fastAve += (balance - fastAve) * fastMult;
        double velocity = balance - fastAve;
        intermAve += (velocity - intermAve) * intermMult;
        value = velocity - intermAve;
    }

    @Override
    public void reset() {
        fastAve = intermAve = 0;
    }
}
