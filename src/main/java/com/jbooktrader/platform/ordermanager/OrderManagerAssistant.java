package com.jbooktrader.platform.ordermanager;

import com.ib.client.Contract;
import com.ib.client.Types;
import com.jbooktrader.platform.email.Notifier;
import com.jbooktrader.platform.ibhandler.OrderExecution;
import com.jbooktrader.platform.ibhandler.OrderHandler;
import com.jbooktrader.platform.indicator.IndicatorManager;
import com.jbooktrader.platform.marketbook.MarketBook;
import com.jbooktrader.platform.marketbook.MarketSnapshot;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.Mode;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.portfolio.PortfolioManager;
import com.jbooktrader.platform.position.PositionManager;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.report.EventReport;
import com.jbooktrader.platform.startup.JBookTrader;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.strategy.StrategyRunner;
import com.jbooktrader.platform.util.format.NumberFormatterFactory;
import com.jbooktrader.platform.util.ui.MessageDialog;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class OrderManagerAssistant {
    private final Map<Integer, Strategy> strategies;
    private final Map<String, MarketBook> marketBooks;
    private final EventReport eventReport;
    private final Dispatcher dispatcher;
    private final BlockingQueue<MarketSnapshot> messageQueue;
    private final Map<String, Integer> portfolio;
    private final DecimalFormat df0;
    private final String account;
    private final PortfolioManager portfolioManager;
    private OrderHandler orderHandler;
    private int nextStrategyID;
    private StrategyRunner strategyRunner;
    private boolean needsPortfolioCheck;

    public OrderManagerAssistant() {
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        portfolioManager = dispatcher.getPortfolioManager();
        strategies = new HashMap<>();
        marketBooks = new HashMap<>();
        portfolio = new ConcurrentHashMap<>();
        messageQueue = new LinkedBlockingQueue<>();
        df0 = NumberFormatterFactory.getNumberFormatter(0, true);
        account = PreferencesHolder.getInstance().get(Account);
    }

    public void setOrderHandler(OrderHandler orderHandler) {
        this.orderHandler = orderHandler;
    }

    public void setPortfolio(Map<String, Integer> portfolio) {
        this.portfolio.clear();
        this.portfolio.putAll(portfolio);
    }

    public Collection<Strategy> getAllStrategies() {
        return strategies.values();
    }

    public void connect() {
        if (account == null || account.isEmpty()) {
            String message = "IB account is not specified in Configure|Preferences|TWS|Account.";
            eventReport.report(JBookTrader.APP_NAME, message);
            MessageDialog.showMessage(message);
            return;
        }

        if (dispatcher.isRealAccount()) {
            String lineSep = System.getProperty("line.separator");
            String warning = "About to connect to a live IB account " + account + ". ";
            warning += "Running " + JBookTrader.APP_NAME + " in trading mode against a live" + lineSep;
            warning += "account may cause significant losses in your account. ";
            warning += "Are you sure you want to proceed?";
            int response = JOptionPane.showConfirmDialog(null, warning, JBookTrader.APP_NAME, JOptionPane.YES_NO_OPTION);
            if (response == JOptionPane.NO_OPTION) {
                return;
            }
        }

        PreferencesHolder prefs = PreferencesHolder.getInstance();
        String host = prefs.get(Host);
        int port = prefs.getInt(Port);
        int clientID = prefs.getInt(ClientID);

        orderHandler.connect(host, port, clientID, account);
    }

    private String makeInstrument(Strategy strategy) {
        String instrument = strategy.getTicker();
        Contract contract = strategy.getContract();
        if (contract.currency() != null) {
            instrument += "-" + contract.currency();
        }
        if (contract.exchange() != null) {
            instrument += "-" + contract.exchange();
        }
        if (contract.secType() != null) {
            instrument += "-" + contract.secType();
        }

        return instrument;
    }

    public synchronized MarketBook createMarketBook(Strategy strategy) {
        String instrument = makeInstrument(strategy);
        return marketBooks.computeIfAbsent(instrument, k -> new MarketBook());
    }


    public synchronized void addStrategy(Strategy strategy) {
        if (strategies.containsValue(strategy)) {
            throw new RuntimeException("Strategy " + strategy.getName() + " is already running.");
        }


        Mode mode = dispatcher.getMode();
        strategy.setIndicatorManager(new IndicatorManager());
        strategy.setIndicators();
        nextStrategyID++;
        strategies.put(nextStrategyID, strategy);

        if (mode == Mode.ForwardTest || mode == Mode.Trade) {
            if (strategyRunner == null) {
                strategyRunner = new StrategyRunner(messageQueue);
            }
            strategyRunner.addListener(strategy);
            orderHandler.subscribe(strategy.getContract());
        }
    }

    public synchronized void clearAllStrategies() {
        strategies.clear();
        marketBooks.clear();
        dispatcher.createStrategies();
    }

    public synchronized void shutDown() {
        if (strategyRunner != null) {
            strategyRunner.shutDown();
        }
    }

    public void trade(Strategy strategy) {
        Mode mode = dispatcher.getMode();
        if (mode == Mode.Trade || mode == Mode.ForceClose) {
            if (orderHandler.hasOpenOrders()) {
                return;
            }
        }

        PositionManager positionManager = strategy.getPositionManager();
        int currentPosition = positionManager.getCurrentPosition();
        int targetPosition = positionManager.getTargetPosition();
        int delta = targetPosition - currentPosition;
        if (delta == 0) {
            return;
        }

        if (mode == Mode.Trade || mode == Mode.ForwardTest || mode == Mode.ForceClose) {
            boolean isExposureIncreasing = Math.abs(targetPosition) > Math.abs(currentPosition);
            if (isExposureIncreasing && !portfolioManager.isWithinMaxLeverage()) {
                return;
            }
        }

        int quantity = Math.abs(delta);

        MarketSnapshot snapshot = strategy.getMarketBook().getSnapshot();
        Types.Action orderAction = (delta > 0) ? Types.Action.BUY : Types.Action.SELL;
        double expectedFillPrice = (orderAction == Types.Action.BUY) ? snapshot.getAsk() : snapshot.getBid();
        positionManager.setExpectedFillPrice(expectedFillPrice);
        String contractName = snapshot.getContract();
        String exchange = strategy.getContract().exchange();

        if (mode == Mode.Trade || mode == Mode.ForceClose) {
            orderHandler.submitMarketOrder(strategy.getName(), exchange, contractName, orderAction, quantity);
        } else {
            String transactionType = (orderAction == Types.Action.BUY) ? "BOT" : "SLD";
            OrderExecution orderExecution = new OrderExecution(0, contractName, quantity, transactionType, expectedFillPrice);
            positionManager.update(orderExecution);
        }
    }

    public String getSystemStatus() {
        long profit = 0, trades = 0;
        for (Strategy strategy : strategies.values()) {
            PerformanceManager pm = strategy.getPerformanceManager();
            profit += pm.getClosedNetProfit();
            trades += pm.getTrades();
        }

        String accountBalance = df0.format(dispatcher.getPortfolioManager().getAccountValue());
        String accountStatus = "Account: " + account;
        String tradesStatus = "Trades: " + trades + ", P&L: " + profit;
        String portfolioStatus = "Portfolio: " + (portfolio.isEmpty() ? "Flat" : portfolio.toString());
        String balanceStatus = "Balance: " + accountBalance;

        return "[" + accountStatus + ", " + tradesStatus + ", " + portfolioStatus + ", " + balanceStatus + "]";
    }

    public void forceClose(String reason) {
        Mode mode = dispatcher.getMode();
        if (mode == Mode.Trade || mode == Mode.ForwardTest) {
            String msg = "Setting operational mode to ForceClose because " + reason + ".";
            Notifier.getInstance().submit(msg);
            eventReport.report(JBookTrader.APP_NAME, msg);
            dispatcher.setMode(Mode.ForceClose);
        }
    }

    public void checkPortfolio() {
        if (dispatcher.getDaySchedule().isEndOfTradingDay()) {
            if (needsPortfolioCheck) {
                String msg;
                if (portfolio.isEmpty()) {
                    String accountBalance = df0.format(dispatcher.getPortfolioManager().getAccountValue());
                    msg = "End of trading day. Portfolio: " + portfolio + ". Account balance: " + accountBalance + ".";
                } else {
                    msg = "End of trading day. TS should be flat, but account has open positions: " + portfolio + ".";
                }
                eventReport.report("StrategyRunner", msg);
                Notifier.getInstance().submit(msg);
                needsPortfolioCheck = false;
            }
        } else {
            needsPortfolioCheck = true;
        }
    }
}
