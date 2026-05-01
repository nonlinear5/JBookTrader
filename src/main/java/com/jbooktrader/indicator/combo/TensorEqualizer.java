package com.jbooktrader.indicator.combo;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketbook.MarketSnapshot;

/**
 * Volatility-adjusted price/balance tensor which incorporates the "fair price" concept
 *
 * @author Eugene Kononov
 */
public class TensorEqualizer extends Indicator {

    private final double alpha1, priceScale;

    private double alpha1Count;

    private double slowPriceSum, slowBalanceSum;

    private double slowTensionSum;
    private double slowVarianceSum;
    private double tension, sigmaTension;

    public TensorEqualizer(int slowPeriod, int priceScale) {
        super(slowPeriod, priceScale);
        // make sure to avoid the integer division
        alpha1 = 1 - 2.0 / (slowPeriod + 1);
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


        // slow price
        slowPriceSum = price + alpha1 * slowPriceSum;
        double slowPrice = slowPriceSum / alpha1Count;

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
        slowVarianceSum = instantVariance + alpha1 * slowVarianceSum;
        double variance = slowVarianceSum / alpha1Count;

        // vollatility-adjusted tension
        sigmaTension = 100 * ((tension - slowTension) / Math.sqrt(variance));

        // this is used for displaying the value of the indicator on the chart
       value = sigmaTension;
    }

    public double getSigmaTension() {
        return sigmaTension;
    }

    public double getTension() {
        return tension;
    }

    @Override
    public void reset() {
        alpha1Count = 0;
        tension = 0;
        slowPriceSum = slowBalanceSum = slowTensionSum = slowVarianceSum = 0;
    }
}
