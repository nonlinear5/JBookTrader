package com.jbooktrader.platform.model;


import com.jbooktrader.platform.email.*;
import com.jbooktrader.platform.ibhandler.*;
import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.model.ModelListener.*;
import com.jbooktrader.platform.ordermanager.*;
import com.jbooktrader.platform.portfolio.*;
import com.jbooktrader.platform.preferences.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.startup.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.ntp.*;
import com.jbooktrader.platform.web.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * Acts as the dispatcher of the services.
 *
 * @author Eugene Kononov
 */
public class Dispatcher {
    private static Dispatcher instance;
    private final List<ModelListener> listeners;
    private EventReport eventReport;
    private OrderManager orderManager;
    private PortfolioManager portfolioManager;
    private DaySchedule daySchedule;
    private NTPClock ntpClock;
    private Mode mode;
    private String reportsDir, marketDataDir, resourcesDir;
    private OrderHandler orderHandler;
    private List<Strategy> strategies;

    private Dispatcher() {
        listeners = new ArrayList<>();
    }

    public static synchronized Dispatcher getInstance() {
        if (instance == null) {
            instance = new Dispatcher();
        }
        return instance;
    }

    public void init() throws IOException {
        Path currentRelativePath = Paths.get("");
        String s = currentRelativePath.toAbsolutePath().toString();


        File homeDirFile = new File(s);
        if (!homeDirFile.exists()) {
            throw new RuntimeException("Home directory " + homeDirFile + " does not exist.");
        }
        String homeDir = homeDirFile.getCanonicalPath();

        reportsDir = homeDir + "/reports/";
        File reportsDirFile = new File(reportsDir);
        if (!reportsDirFile.exists()) {
            boolean isCreated = reportsDirFile.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Could not create directory " + reportsDir);
            }
        }

        marketDataDir = homeDir + "/marketData/";
        File marketDataDirFile = new File(marketDataDir);
        if (!marketDataDirFile.exists()) {
            boolean isCreated = marketDataDirFile.mkdir();
            if (!isCreated) {
                throw new RuntimeException("Could not create directory " + marketDataDir);
            }
        }

        resourcesDir = homeDir + "/src/main/resources/";
        createStrategies();
    }

    public void createStrategies() {
        StrategyLoader strategyProvider = new StrategyLoader();
        strategies = strategyProvider.getStrategies();
    }

    public Strategy getStrategy(String strategyName) {
        for (Strategy strategy : strategies) {
            if (strategy.getName().equals(strategyName)) {
                return strategy;
            }
        }

        throw new RuntimeException("Strategy " + strategyName + " does not exist.");
    }

    public List<Strategy> getStrategies() {
        return strategies;
    }

    public String getReportsDir() {
        return reportsDir;
    }

    public String getMarketDataDir() {
        return marketDataDir;
    }

    public String getResourcesDir() {
        return resourcesDir;
    }

    void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void fireModelChanged(Event event, Object value) {
        for (ModelListener listener : listeners) {
            try {
                listener.modelChanged(event, value);
            } catch (Exception e) {
                eventReport.report(e);
            }
        }
    }

    public void fireModelChanged(Event event) {
        fireModelChanged(event, null);
    }


    public synchronized OrderManager getOrderManager() {
        if (orderManager == null) {
            orderManager = new OrderManager();
            int openOrderTimeoutSeconds = PreferencesHolder.getInstance().getInt(OpenOrderTimeoutSeconds);
            orderHandler = new OrderHandler(orderManager, openOrderTimeoutSeconds);
            orderManager.getAssistant().setOrderHandler(orderHandler);
        }
        return orderManager;
    }

    public void setQueue(BlockingQueue<MarketSnapshot> queue) {
        orderHandler.setQueue(queue);
    }

    public boolean isRealAccount() {
        String account = PreferencesHolder.getInstance().get(Account);
        return account != null && !account.equals("edemo") && !account.startsWith("D") && !account.startsWith("d");
    }

    public synchronized PortfolioManager getPortfolioManager() {
        if (portfolioManager == null) {
            PreferencesHolder prefs = PreferencesHolder.getInstance();
            double maxLeverage = prefs.getDouble(MaxLeverage);
            portfolioManager = new PortfolioManager(maxLeverage);
        }
        return portfolioManager;
    }

    public synchronized DaySchedule getDaySchedule() {
        if (daySchedule == null) {
            daySchedule = new DaySchedule();
        }

        return daySchedule;
    }

    public synchronized NTPClock getNTPClock() {
        if (ntpClock == null) {
            ntpClock = NTPClock.getInstance();
        }
        return ntpClock;
    }

    public synchronized boolean hasNTPClock() {
        return (ntpClock != null);
    }


    public EventReport getEventReport() {
        if (eventReport == null) {
            try {
                eventReport = new EventReport();
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
        return eventReport;
    }

    public Mode getMode() {
        return mode;
    }

    public void setMode(Mode mode) {
        if (this.mode == mode) {
            return;
        }

        this.mode = mode;
        eventReport.report(JBookTrader.APP_NAME, "Running mode changed to: " + mode.getName());

        // Disable all reporting when JBT runs in optimization mode. The backtest runs
        // millions of strategies, and the amount of data to report would be enormous.
        if (mode == Mode.Optimization) {
            eventReport.disable();
        } else {
            eventReport.enable();
        }

        if (mode == Mode.Trade || mode == Mode.ForwardTest) {
            orderManager.getAssistant().connect();
            MonitoringServer.start();
        } else if (mode == Mode.BackTest || mode == Mode.BackTestAll || mode == Mode.Optimization) {
            orderHandler.disconnect();
        }

        fireModelChanged(Event.ModeChanged);
    }

    public void exit() {
        MonitoringServer.stop();
        if (orderHandler != null) {
            orderHandler.disconnect();
            orderManager.shutDown();
        }
        Notifier.getInstance().shutdown();
        if (ntpClock != null) {
            ntpClock.shutDown();
        }
    }
}
