package com.jbooktrader.platform.indicator;


import com.jbooktrader.platform.marketbook.MarketBook;

/**
 * Base class for all classes implementing technical indicators.
 *
 * @author Eugene Kononov
 */
public abstract class Indicator {
    private final String key;
    protected MarketBook marketBook;
    protected double value;

    protected Indicator(int... parameters) {
        String name = getClass().getSimpleName();
        if (parameters.length == 0) {
            throw new RuntimeException("No parameters passed from the constructor of indicator " + name);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(name).append("(");
        for (int parameter : parameters) {
            if (sb.length() > name.length() + 1) {
                sb.append(",");
            }
            sb.append(parameter);
        }
        sb.append(")");
        key = sb.toString();
    }

    public abstract void calculate();

    public abstract void reset();

    public String getKey() {
        return key;
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
    }

    public long getTime() {
        return marketBook.getSnapshot().getTime();
    }


    @Override
    public String toString() {
        return "value: " + value;
    }

    public double getValue() {
        return value;
    }
}
