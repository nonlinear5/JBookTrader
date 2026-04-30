package com.jbooktrader.platform.indicator;

import com.jbooktrader.platform.marketbook.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class IndicatorManager {
    private static final long GAP_SIZE = 5 * 60 * 1000; // 5 minutes
    private static final long MIN_SAMPLE_SIZE = 180 * 60; // 3 hours worth of samples
    private final List<Indicator> indicators;
    private MarketBook marketBook;
    private long previousSnapshotTime;
    private long samples;

    public IndicatorManager() {
        indicators = new ArrayList<>();
    }

    public Indicator addIndicator(Indicator newIndicator) {
        String key = newIndicator.getKey();
        for (Indicator indicator : indicators) {
            if (key.equals(indicator.getKey())) {
                return indicator;
            }
        }

        indicators.add(newIndicator);
        newIndicator.setMarketBook(marketBook);

        return newIndicator;
    }

    public void setMarketBook(MarketBook marketBook) {
        this.marketBook = marketBook;
        for (Indicator indicator : indicators) {
            indicator.setMarketBook(marketBook);
        }
    }

    public List<Indicator> getIndicators() {
        return indicators;
    }

    public void resetIndicators() {
        samples = 0;
        if (marketBook != null && !marketBook.isEmpty()) {
            for (Indicator indicator : indicators) {
                indicator.reset();
            }
        }
    }


    public boolean updateIndicators() {
        MarketSnapshot snapshot = marketBook.getSnapshot();

        if (snapshot == null) {
            return false;
        }

        long lastSnapshotTime = snapshot.getTime();
        samples++;

        if (marketBook.isLocked() || (lastSnapshotTime - previousSnapshotTime > GAP_SIZE)) {
            resetIndicators();
        }

        previousSnapshotTime = lastSnapshotTime;

        for (Indicator indicator : indicators) {
            indicator.calculate();
        }

        return (samples >= MIN_SAMPLE_SIZE) ;
    }
}
