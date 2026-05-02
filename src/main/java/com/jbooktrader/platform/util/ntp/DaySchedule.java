package com.jbooktrader.platform.util.ntp;

import com.jbooktrader.platform.schedule.HolidaySchedule;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Eugene Kononov
 */
public class DaySchedule {
    private static final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("America/New_York"));
    private final static long millisIn12hours = 12L * 60L * 60L * 1000L;
    private final HolidaySchedule holidaySchedule;
    private final NTPClock ntpClock;
    private long lastResetTime;

    public DaySchedule() {
        holidaySchedule = new HolidaySchedule();
        ntpClock = NTPClock.getInstance();
    }

    public synchronized boolean isTradingDay() {
        cal.setTimeInMillis(ntpClock.getTime());
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        boolean isTradingDay = (dayOfWeek != Calendar.SATURDAY) && (dayOfWeek != Calendar.SUNDAY);
        boolean isHoliday = holidaySchedule.getHolidayOrEarlyClose(cal.getTimeInMillis()) != null;
        return (!isHoliday && isTradingDay);
    }


    public synchronized boolean isEndOfTradingDay() {
        int hour = getHourOfDay();
        boolean isTradingDay = isTradingDay();
        return hour >= 16 && isTradingDay;
    }

    public synchronized boolean isTradingPeriod() {
        boolean isTradingDay = isTradingDay();
        int hour = getHourOfDay();
        boolean isTradingTime = (hour >= 7) && (hour < 16);

        return (isTradingDay && isTradingTime);
    }

    public synchronized boolean isResetTime() {
        int hour = getHourOfDay();

        if (hour == 7) {
            long timeNow = ntpClock.getTime();
            long timeElapsedSinceLastReset = timeNow - lastResetTime;
            if (timeElapsedSinceLastReset > millisIn12hours) {
                lastResetTime = timeNow;
                return true;
            }
        }

        return false;
    }


    public synchronized int getHourOfDay() {
        cal.setTimeInMillis(ntpClock.getTime());
        return cal.get(Calendar.HOUR_OF_DAY);
    }
}
