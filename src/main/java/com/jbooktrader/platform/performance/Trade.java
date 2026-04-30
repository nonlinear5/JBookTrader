package com.jbooktrader.platform.performance;

/**
 * @author Eugene Kononov
 */
public class Trade {
    private final int contractMultiplier;
    private int quantityBought, quantitySold;
    private double totalBought, totalSold;
    private long entryTime, exitTime;
    private double slippageBoughtPoints, slippageSoldPoints;

    public Trade(int contractMultiplier) {
        this.contractMultiplier = contractMultiplier;
    }

    public void updateTotalBought(int quantityBought, double averagePrice, double slippageBoughtPoints) {
        this.quantityBought += quantityBought;
        totalBought += quantityBought * averagePrice;
        this.slippageBoughtPoints = slippageBoughtPoints;
    }

    public void updateTotalSold(int quantitySold, double averagePrice, double slippageSoldPoints) {
        this.quantitySold += quantitySold;
        totalSold += quantitySold * averagePrice;
        this.slippageSoldPoints = slippageSoldPoints;
    }

    public double getSlippagePoints() {
        return slippageBoughtPoints + slippageSoldPoints;
    }

    public double getSlippageAmount() {
        return contractMultiplier * (quantityBought * slippageBoughtPoints + quantitySold * slippageSoldPoints);
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public long getExitTime() {
        return exitTime;
    }

    public void setExitTime(long exitTime) {
        this.exitTime = exitTime;
    }

    public double getAverageBoughtPrice() {
        return totalBought / quantityBought;
    }

    public double getAverageSoldPrice() {
        return totalSold / quantitySold;
    }

    public long getTimeInMarket() {
        return exitTime - entryTime;
    }

    public int getQuantityBought() {
        return quantityBought;
    }

    public int getQuantitySold() {
        return quantitySold;
    }
}
