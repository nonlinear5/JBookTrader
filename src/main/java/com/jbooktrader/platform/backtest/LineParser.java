package com.jbooktrader.platform.backtest;

import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.marketbook.MarketSnapshotFilter;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author Eugene Kononov
 */
public class LineParser {
    private static final int COLUMNS = 6;
    private final MarketSnapshotFilter filter;
    private DateTimeFormatter formatter;
    private long previousTime, time;
    private String previousDateTimeWithoutSeconds;

    LineParser(MarketSnapshotFilter filter) {
        this.filter = filter;
    }

    private boolean isMarketDepth(String line) {
        boolean isComment = line.startsWith("#");
        boolean isProperty = line.contains("=");
        boolean isBlankLine = (line.trim().length() == 0);
        return !(isComment || isProperty || isBlankLine);
    }

    public MarketSnapshot process(String line) {
        if (isMarketDepth(line)) {
            MarketSnapshot marketSnapshot = toMarketDepth(line);
            previousTime = time;
            if (filter == null || filter.contains(time * 1000)) {
                return marketSnapshot;
            }
        } else if (line.startsWith("timeZone")) {
            String timeZonePropertyValue = line.substring(line.indexOf('=') + 1);
            ZoneId tz = ZoneId.of(timeZonePropertyValue);
            formatter = DateTimeFormatter.ofPattern("MMddyyHHmmss").withZone(tz);
        }

        return null;
    }

    private MarketSnapshot toMarketDepth(String line) {
        String[] tokens = line.split(",");

        if (tokens.length != COLUMNS) {
            String msg = "The line should contain exactly " + COLUMNS + " comma-separated columns.";
            msg += "\n" + line;
            throw new RuntimeException(msg);
        }

        if (formatter == null) {
            String msg = "Property " + "\"timeZone\"" + " is not defined in the data file.";
            throw new RuntimeException(msg);
        }

        String dateTime = tokens[0] + tokens[1];
        String dateTimeWithoutSeconds = dateTime.substring(0, 10);

        if (dateTimeWithoutSeconds.equals(previousDateTimeWithoutSeconds)) {
            // only seconds need to be set
            int seconds = Integer.parseInt(dateTime.substring(10));
            long previousSeconds = previousTime % 60;
            time = previousTime + (seconds - previousSeconds);
        } else {
            ZonedDateTime dt = ZonedDateTime.parse(dateTime, formatter);
            time = dt.toEpochSecond();
            previousDateTimeWithoutSeconds = dateTimeWithoutSeconds;
        }

        if (time <= previousTime) {
            String msg = "Timestamp of a snapshot is before or the same as the timestamp of the previous snapshot.";
            msg += "\n" + line;
            throw new RuntimeException(msg);
        }

        double balance = Double.parseDouble(tokens[2]);
        double bid = Double.parseDouble(tokens[3]);
        double ask = Double.parseDouble(tokens[4]);

        int volume = Integer.parseInt(tokens[5]);
        if (volume < 0) {
            String msg = "Volume must be a positive integer";
            throw new RuntimeException(msg);
        }

        return new MarketSnapshot(time * 1000, balance, bid, ask, volume);
    }
}
