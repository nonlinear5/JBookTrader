package com.jbooktrader.platform.schedule;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class EndOfYearSchedule {
    private static EndOfYearSchedule instance;
    private final Calendar cal;

    // private constructor for non-instantiability
    private EndOfYearSchedule() {
        TimeZone timeZone = TimeZone.getTimeZone("America/New_York");
        cal = Calendar.getInstance(timeZone);
    }

    public static synchronized EndOfYearSchedule getInstance() {
        if (instance == null) {
            instance = new EndOfYearSchedule();
        }
        return instance;
    }

    public synchronized boolean isEndOfYear(long timeNow) {
        cal.setTimeInMillis(timeNow);
        int month = cal.get(Calendar.MONTH);
        if (month == Calendar.DECEMBER) {
            int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
            return dayOfMonth >= 25;
        }

        return false;
    }
}
