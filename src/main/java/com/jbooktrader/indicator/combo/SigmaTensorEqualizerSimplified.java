package com.jbooktrader.indicator.combo;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketbook.MarketSnapshot;

/**
 * Volatility-adjusted price/balance tensor which incorporates the "fair price" concept
 *
 * @author Eugene Kononov
 */
public class SigmaTensorEqualizerSimplified extends Indicator {
    private final static long magnifier1 = 100000;
    private final static long magnifier2 = 100;
    private final double alpha1, alpha3, priceScale;

    private double alpha1Count, alpha3Count;
    private double fairPriceForce;
    private double slowPriceSum, slowBalanceSum;

    private double slowTensionSum;
    private double slowVarianceSum;
    private double tension, sigmaTension;

    public SigmaTensorEqualizerSimplified(int slowPeriod, int priceScale) {
        super(slowPeriod, priceScale);
        // make sure to avoid the integer division
        alpha1 = 1 - 2.0 / (slowPeriod + 1);
        alpha3 = 1 - 2.0 / (slowPeriod * 2 + 1);

        this.priceScale = priceScale;
    }

    /*
     * EMA calculation reference: https://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
     */
    @Override
    public void calculate() {
        // get the most recent balance and price
        MarketSnapshot snapshot = marketBook.getSnapshot();
        double balance = snapshot.getBalance();
        double price = snapshot.getPrice();

        alpha1Count = 1 + alpha1 * alpha1Count;
        alpha3Count = 1 + alpha3 * alpha3Count;

        // slow price
        slowPriceSum = price + alpha1 * slowPriceSum;
        double slowPrice = slowPriceSum / alpha1Count;


        // fair price force
        fairPriceForce = magnifier1 * (slowPrice / price - 1);


        // slow balance
        slowBalanceSum = balance + alpha1 * slowBalanceSum;
        double slowBalance = slowBalanceSum / alpha1Count;

        // balance velocity
        double balanceVelocity = balance - slowBalance;

        // price velocity
        double priceVelocity = 100 * priceScale * ((price / slowPrice) - 1);

        // tension: the strength and the direction of the price-balance tensor
        tension = balanceVelocity - priceVelocity;

        // slow tension
        slowTensionSum = tension + alpha1 * slowTensionSum;
        double slowTension = slowTensionSum / alpha1Count;

        // instant variance
        double instantVariance = Math.pow(tension - slowTension, 2);

        // slow variance
        slowVarianceSum = instantVariance + alpha3 * slowVarianceSum;
        double variance = slowVarianceSum / alpha3Count;

        // vollatility-adjusted tension
        sigmaTension = magnifier2 * ((tension - slowTension) / Math.sqrt(variance));

        // this is used for displaying the value of the indicator on the chart
       value = sigmaTension;
    }

    public double getSigmaTension() {
        return sigmaTension;
    }

    public double getFairPriceForce() {
        return fairPriceForce;
    }

    public double getTension() {
        return tension;
    }

    @Override
    public void reset() {
        alpha1Count = alpha3Count = 0;
        tension = 0;
        slowPriceSum = slowBalanceSum = slowTensionSum = slowVarianceSum = 0;
    }
}
