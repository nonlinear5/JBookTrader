package com.jbooktrader.indicator.combo;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.marketbook.MarketSnapshot;

/**
 * Volatility-adjusted price/balance tensor which incorporates the "fair price" concept
 *
 * @author Eugene Kononov
 */
public class TensorEqualizer extends Indicator {

    private final double alpha, priceScale;

    private double alphaCount;

    private double slowPriceSum, slowBalanceSum;

    private double slowTensionSum;
    private double slowVarianceSum;
    private double tension, sigmaTension;

    public TensorEqualizer(int slowPeriod, int priceScale) {
        super(slowPeriod, priceScale);
        // make sure to avoid the integer division
        alpha = 1 - 2.0 / (slowPeriod + 1);
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

        alphaCount = 1 + alpha * alphaCount;


        // slow price
        slowPriceSum = price + alpha * slowPriceSum;
        double slowPrice = slowPriceSum / alphaCount;

        // slow balance
        slowBalanceSum = balance + alpha * slowBalanceSum;
        double slowBalance = slowBalanceSum / alphaCount;

        // balance velocity
        double balanceVelocity = balance - slowBalance;

        // price velocity
        double priceVelocity = 100 * priceScale * ((price / slowPrice) - 1);

        // tension: the strength and the direction of the price-balance tensor
        tension = balanceVelocity - priceVelocity;

        // slow tension
        slowTensionSum = tension + alpha * slowTensionSum;
        double slowTension = slowTensionSum / alphaCount;

        // instant variance
        double instantVariance = Math.pow(tension - slowTension, 2);

        // slow variance
        slowVarianceSum = instantVariance + alpha * slowVarianceSum;
        double variance = slowVarianceSum / alphaCount;

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
        alphaCount = 0;
        tension = 0;
        slowPriceSum = slowBalanceSum = slowTensionSum = slowVarianceSum = 0;
    }
}
