package com.jbooktrader.platform.marketbook;


/**
 * @author Eugene Kononov
 */
public class MarketSnapshot {
    private final long time;
    private final double balance, bid, ask;
    private final int volume;
    private final String contract;

    public MarketSnapshot(long time, double balance, double bid, double ask, int volume) {
        this(null, time, balance, bid, ask, volume);
    }

    public MarketSnapshot(String contract, long time, double balance, double bid, double ask, int volume) {
        this.contract = contract;
        this.time = time;
        this.balance = balance;
        this.bid = bid;
        this.ask = ask;
        this.volume = volume;
    }

    // special end-of-stream snapshot
    public MarketSnapshot() {
        this(null, -1, 0, 0, 0, 0);
    }

    public boolean isEndOfStream() {
        return time == -1;
    }


    public double getBalance() {
        return balance;
    }

    public long getTime() {
        return time;
    }

    public double getBid() {
        return bid;
    }

    public double getAsk() {
        return ask;
    }

    public double getPrice() {
        return (bid + ask) / 2d;
    }

    public int getVolume() {
        return volume;
    }

    public String getContract() {
        return contract;
    }

}
