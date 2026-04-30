package com.jbooktrader.platform.chart;

/**
 * Encapsulates the price/indicator bar information.
 *
 * @author Eugene Kononov
 */
class Bar {
    private final long time;
    private final double open;
    private double high, low, close;

    private Bar(long time, double open, double high, double low, double close) {
        this.time = time;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    Bar(long time, double value) {
        this(time, value, value, value, value);
    }

    @Override
    public String toString() {
        return " time: " + time + " open: " + open + " high: " + high +
                " low: " + low + " close: " + close;
    }

    public double getOpen() {
        return open;
    }

    double getHigh() {
        return high;
    }

    void setHigh(double high) {
        this.high = high;
    }

    double getLow() {
        return low;
    }

    void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public long getTime() {
        return time;
    }

}
