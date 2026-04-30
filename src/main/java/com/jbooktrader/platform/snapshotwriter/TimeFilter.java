package com.jbooktrader.platform.snapshotwriter;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class TimeFilter {
    private final static long secondsInMinute = 60;
    private final static long secondsInHour = secondsInMinute * secondsInMinute;
    private final long recordFromHour, lastRecordedSecondOfDay;
    private final Calendar currentTimeCalendar;

    public TimeFilter(long recordFromHour, long recordToHour) {
        this.recordFromHour = recordFromHour;
        lastRecordedSecondOfDay = recordToHour * secondsInHour;

        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        currentTimeCalendar = Calendar.getInstance(timeZone);
    }

    public boolean isRecordable(long timeInMillis) {
        currentTimeCalendar.setTimeInMillis(timeInMillis);

        int hour = currentTimeCalendar.get(Calendar.HOUR_OF_DAY);
        int minute = currentTimeCalendar.get(Calendar.MINUTE);
        int second = currentTimeCalendar.get(Calendar.SECOND);
        long secondOfDay = hour * secondsInHour + minute * secondsInMinute + second;

        return (hour >= recordFromHour && secondOfDay <= lastRecordedSecondOfDay);
    }
}
