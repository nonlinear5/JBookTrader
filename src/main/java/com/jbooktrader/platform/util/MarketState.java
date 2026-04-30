package com.jbooktrader.platform.util;

public class MarketState {
    private final double price, priceVelocity, balanceVelocity, tension, sigmaTension, sigma;
    private final long time;

    public MarketState(long time, double price, double priceVelocity, double balanceVelocity, double tension, double sigmaTension, double sigma) {
        this.time = time;
        this.price = price;
        this.priceVelocity = priceVelocity;
        this.balanceVelocity = balanceVelocity;
        this.tension = tension;
        this.sigmaTension = sigmaTension;
        this.sigma = sigma;
    }

    public double getPriceVelocity() {
        return priceVelocity;
    }

    public double getBalanceVelocity() {
        return balanceVelocity;
    }

    public double getTension() {
        return tension;
    }

    public double getSigma() {
        return sigma;
    }

    public double getPrice() {
        return price;
    }

    public long getTime() {
        return time;
    }

    public double getSigmaTension() {
        return sigmaTension;
    }

    @Override
    public String toString() {
        return "MarketState{" +
                //"price=" + price +
                ", priceVelocity: " + (int) priceVelocity +
                ", balanceVelocity: " + (int) balanceVelocity +
                ", tension: " + (int) tension +
                ", sigma: " + (int) sigma +
                //", time=" + time +
                '}';
    }
}
