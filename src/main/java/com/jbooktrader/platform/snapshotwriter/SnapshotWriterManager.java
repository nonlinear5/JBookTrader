package com.jbooktrader.platform.snapshotwriter;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.*;

import java.io.*;
import java.util.*;

/**
 * @author Eugene Kononov
 */
public class SnapshotWriterManager {
    private final Map<String, SnapshotWriter> fileWriters;
    private final String marketDataDir;
    private final TimeFilter timeFilter;

    public SnapshotWriterManager() {
        timeFilter = new TimeFilter(7, 16);
        fileWriters = new HashMap<>();
        marketDataDir = Dispatcher.getInstance().getMarketDataDir();
        File marketDataDirFile = new File(marketDataDir);
        if (!marketDataDirFile.exists()) {
            boolean isCreated = marketDataDirFile.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Could not create directory " + marketDataDir);
            }
        }
    }

    public void saveSnapshot(MarketSnapshot marketSnapshot, String ticker) {
        if (timeFilter.isRecordable(marketSnapshot.getTime())) {
            SnapshotWriter writer = fileWriters.get(ticker);
            if (writer == null) {
                String fileName = marketDataDir + ticker + ".txt";
                writer = new SnapshotWriter(fileName);
                fileWriters.put(ticker, writer);
            }
            writer.write(marketSnapshot);
        }
    }
}

