package com.jbooktrader.platform.util.ui;

import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.report.EventReport;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jbooktrader.platform.preferences.JBTPreferences.SessionExitTime;

public class Scheduler {
    private final EventReport eventReport;

    public Scheduler(EventReport eventReport) {
        this.eventReport = eventReport;
    }

    public void start() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

        PreferencesHolder prefs = PreferencesHolder.getInstance();
        String time = prefs.get(SessionExitTime);
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        // we want exit happen on the next day, at the designated time
        LocalDateTime targetTime = LocalDateTime.now().plusDays(1).withHour(hour).withMinute(minute);

        long delay = Duration.between(LocalDateTime.now(), targetTime).toMillis();
        if (delay > 0) {
            eventReport.report("Scheduler", "JBookTrader will exit at " + targetTime.toLocalTime());
            scheduler.schedule(() -> {
                eventReport.report("Scheduler", "Scheduled exit executed.");
                System.exit(0);
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            eventReport.report("Scheduler", "Could not schedule exit because delay=" + delay);
        }
        scheduler.shutdown();
    }
}
