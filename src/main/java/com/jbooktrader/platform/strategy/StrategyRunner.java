package com.jbooktrader.platform.strategy;

import com.jbooktrader.platform.email.Notifier;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.Mode;
import com.jbooktrader.platform.ordermanager.OrderManagerAssistant;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.report.EventReport;
import com.jbooktrader.platform.util.ntp.DaySchedule;
import com.jbooktrader.platform.util.ntp.NTPClock;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.jbooktrader.platform.preferences.JBTPreferences.MarketDataTimeoutSeconds;

/**
 * @author Eugene Kononov
 */
public class StrategyRunner {
    private final BlockingQueue<MarketSnapshot> queue;
    private final List<Strategy> strategies;
    private final Dispatcher dispatcher;
    private final OrderManagerAssistant orderManagerAssistant;
    private final EventReport eventReport;
    private final DaySchedule daySchedule;
    private final long marketDataTimeoutSeconds;
    private final ExecutorService service;
    private final NTPClock ntpClock;
    private final Timer timer;
    private long timeOfMostRecentSnapshot;

    public StrategyRunner(BlockingQueue<MarketSnapshot> queue) {
        this.queue = queue;
        dispatcher = Dispatcher.getInstance();
        orderManagerAssistant = dispatcher.getOrderManager().getAssistant();
        dispatcher.setQueue(queue);
        eventReport = dispatcher.getEventReport();
        daySchedule = Dispatcher.getInstance().getDaySchedule();
        strategies = new CopyOnWriteArrayList<>();
        service = Executors.newSingleThreadExecutor();
        service.submit(new Runner());
        ntpClock = NTPClock.getInstance();
        timer = new Timer();
        marketDataTimeoutSeconds = Long.parseLong(PreferencesHolder.getInstance().get(MarketDataTimeoutSeconds));
        long periodMillis = marketDataTimeoutSeconds * 1000L;
        timer.scheduleAtFixedRate(new MarketDataTimeoutTask(), periodMillis, periodMillis);
        eventReport.report("StrategyRunner", "StrategyRunner started");
    }

    public void shutDown() {
        MarketSnapshot marketSnapshot = new MarketSnapshot(); // special end-of-stream snapshot
        queue.offer(marketSnapshot);
        timer.cancel();
        service.shutdown();
    }

    private void process(MarketSnapshot marketSnapshot) {
        boolean isResetTime = daySchedule.isResetTime();

        String ticker = marketSnapshot.getContract();
        for (Strategy strategy : strategies) {
            String strategyTicker = strategy.getTicker();
            if (ticker.startsWith(strategyTicker)) {
                if (isResetTime) {
                    strategy.getIndicatorManager().resetIndicators();
                    eventReport.report(strategy.getName(), "Indicators have been reset");
                }
                strategy.getMarketBook().setSnapshot(marketSnapshot);
                strategy.process();
            }
        }
    }

    public void addListener(Strategy strategy) {
        strategies.add(strategy);
    }

    private class MarketDataTimeoutTask extends TimerTask {
        @Override
        public void run() {
            long secondsPassed = (ntpClock.getTime() - timeOfMostRecentSnapshot) / 1000;
            if (daySchedule.isTradingPeriod() && (dispatcher.getMode() != Mode.ForceClose) && secondsPassed > marketDataTimeoutSeconds) {
                String reason = "market data has not been updated for longer than " + marketDataTimeoutSeconds + " seconds";
                orderManagerAssistant.forceClose(reason);
                strategies.forEach(Strategy::process);
            }
        }
    }

    private class Runner implements Runnable {
        @Override
        public void run() {
            MarketSnapshot marketSnapshot;
            try {
                while (!(marketSnapshot = queue.take()).isEndOfStream()) {
                    try {
                        timeOfMostRecentSnapshot = marketSnapshot.getTime();
                        process(marketSnapshot);
                        orderManagerAssistant.checkPortfolio();
                    } catch (Exception e) {
                        eventReport.report(e);
                        Notifier.getInstance().submit(e.getMessage());
                        String reason = "critical exception occured: " + e;
                        orderManagerAssistant.forceClose(reason);
                    }
                }
            } catch (InterruptedException e) {
                eventReport.report(e);
                Notifier.getInstance().submit(e.getMessage());
                String reason = "critical exception occured: " + e;
                orderManagerAssistant.forceClose(reason);
            }
        }
    }

}



