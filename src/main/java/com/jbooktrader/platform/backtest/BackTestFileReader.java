package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Reads and validates a data file containing historical market depth records.
 * The data file is used for back testing and optimization of trading strategies.
 *
 * @author Eugene Kononov
 */
public class BackTestFileReader {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private static final int lineSeparatorSize = LINE_SEP.length();
    private static List<MarketSnapshot> snapshots;
    private static String cacheKey;
    private final MarketSnapshotFilter filter;
    private final String fileName;
    private final long fileSize;

    public BackTestFileReader(String fileName, MarketSnapshotFilter filter) {
        this.fileName = fileName;
        this.filter = filter;

        File file = new File(fileName);
        if (!file.exists()) {
            throw new RuntimeException("Could not find file: " + fileName);
        }
        fileSize = file.length();
    }


    public List<MarketSnapshot> load(ProgressListener progressListener) {
        String key = fileName + "," + fileSize;
        if (filter != null) {
            key += ", " + filter.toString();
        }
        if (key.equals(cacheKey) && !snapshots.isEmpty()) {
            return snapshots;
        }

        snapshots = new ArrayList<>();
        long sizeRead = 0, linesRead = 0;
        LineParser lineParser = new LineParser(filter);
        String line;

        try (BufferedReader reader = Files.newBufferedReader(Paths.get(fileName))) {
            while ((line = reader.readLine()) != null) {
                sizeRead += (line.length() + lineSeparatorSize);
                linesRead++;
                MarketSnapshot snapshot = lineParser.process(line);
                if (snapshot != null) {
                    snapshots.add(snapshot);
                }
                if (linesRead % 100000 == 0) {
                    progressListener.setProgress(sizeRead, fileSize, "Loading historical data file");
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

        cacheKey = key;
        return snapshots;
    }
}
