package com.jbooktrader.platform.marketdepth;

/**
 * @author Eugene Kononov
 */
class MarketDepthItem {
    private double price;
    private int size;

    MarketDepthItem(double price, int size) {
        this.price = price;
        this.size = size;
    }

    public void set(double price, int size) {
        this.price = price;
        this.size = size;
    }

    public double getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }
}
