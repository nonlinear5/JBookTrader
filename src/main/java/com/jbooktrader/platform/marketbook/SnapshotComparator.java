package com.jbooktrader.platform.marketbook;

import java.util.*;

/**
 * Comparator for market snapshots.
 *
 * @author Eugene Kononov
 */
public class SnapshotComparator implements Comparator<MarketSnapshot> {
    public int compare(MarketSnapshot s1, MarketSnapshot s2) {
        return Long.compare(s1.getTime(), s2.getTime());
    }
}
