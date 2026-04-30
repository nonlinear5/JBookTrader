package com.jbooktrader.platform.schedule;

import java.util.*;

/**
 * TradingSchedule defines the time period during which a strategy can trade.
 * Trading can start after the "startTime". Open positions will be closed
 * at the "endTime". The "startTime" and "endTime" times must be specified
 * in the military time format.
 * <p>
 * Example: A strategy defines the following trading schedule:
 * tradingSchedule = new TradingSchedule("9:35", "15:45", "America/New_York");
 * Then the following trading time line is formed:
 * -- start trading at 9:35 EST
 * -- close open positions at 15:45 EST
 *
 * @author Eugene Kononov
 */
public class TradingSchedule {
    private static final String LINE_SEP = System.getProperty("line.separator");
    private final TimeZone tz;
    private final Calendar startCalendar, endCalendar, nowCalendar;
    private final String text;
    private long start, end;

    public TradingSchedule(String startTime, String endTime, String timeZone) {
        tz = TimeZone.getTimeZone(timeZone);
        if (!tz.getID().equals(timeZone)) {
            String msg = "The specified time zone " + "\"" + timeZone + "\"" + " is invalid." + LINE_SEP;
            msg += "Examples of valid time zones: " + " America/New_York, Europe/London, Asia/Singapore.";
            throw new RuntimeException(msg);
        }

        nowCalendar = Calendar.getInstance(tz);
        startCalendar = getCalendar(startTime);
        endCalendar = getCalendar(endTime);

        if (!endCalendar.after(startCalendar)) {
            String msg = "End time must be after the start time in trading schedule.";
            throw new RuntimeException(msg);
        }

        text = startTime + " to " + endTime + " (" + timeZone + ")";
    }

    public TimeZone getTimeZone() {
        return tz;
    }


    public boolean contains(long time) {
        if (time > end) {
            updateCalendars(time);
        }

        return time >= start && time < end;
    }

    public long getRemainingTime(long time) {
        if (time > end) {
            updateCalendars(time);
        }

        return end - time;
    }


    private void updateCalendars(long time) {
        nowCalendar.setTimeInMillis(time);

        int daysForward = 0;
        while (nowCalendar.after(endCalendar)) {
            endCalendar.add(Calendar.DAY_OF_YEAR, 1);
            daysForward++;
        }

        startCalendar.add(Calendar.DAY_OF_YEAR, daysForward);
        start = startCalendar.getTimeInMillis();
        end = endCalendar.getTimeInMillis();
    }

    private Calendar getCalendar(String time) {
        Calendar calendar = Calendar.getInstance(tz);

        StringTokenizer st = new StringTokenizer(time, ":");
        int tokens = st.countTokens();
        if (tokens != 2) {
            String msg = "Time " + time + " does not conform to the HH:MM format in trading schedule.";
            throw new RuntimeException(msg);
        }

        int hours, minutes;

        String hourToken = st.nextToken();
        try {
            hours = Integer.parseInt(hourToken);
        } catch (NumberFormatException nfe) {
            String msg = hourToken + " in " + time + " can not be parsed as hours in trading schedule.";
            throw new RuntimeException(msg);
        }

        String minuteToken = st.nextToken();
        try {
            minutes = Integer.parseInt(minuteToken);
        } catch (NumberFormatException nfe) {
            String msg = minuteToken + " in " + time + " can not be parsed as minutes in trading schedule.";
            throw new RuntimeException(msg);
        }

        if (hours < 0 || hours > 23) {
            String msg = "Specified hours: " + hours + ". Number of hours must be in the [0..23] range in trading schedule.";
            throw new RuntimeException(msg);
        }

        if (minutes < 0 || minutes > 59) {
            String msg = "Specified minutes: " + minutes + ". Number of minutes must be in the [0..59] range in trading schedule.";
            throw new RuntimeException(msg);
        }

        calendar.set(Calendar.YEAR, 2008); // has to be before the first timestamp in the data file
        calendar.set(Calendar.HOUR_OF_DAY, hours);
        calendar.set(Calendar.MINUTE, minutes);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    @Override
    public String toString() {
        return text;
    }

}
