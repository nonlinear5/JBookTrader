package com.jbooktrader.platform.ibhandler;

import com.ib.client.*;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.report.EventReport;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;


/**
 * @author Eugene Kononov
 */
class TraderAssistant {
    private final Semaphore accountSemaphore;
    private final OrderHandlerListener orderHandlerListener;
    private final OrderKeeper orderKeeper;
    private final OrderIdFactory orderIdFactory;
    private final EClientSocket socket;
    private final EReaderSignal signal;
    private final int openOrderTimeoutMillis;
    private String account;
    private String targetAccount;
    private boolean isConnectedToIB;
    private ExecutorService service;
    private final EventReport eventReport;

    TraderAssistant(Trader trader, OrderKeeper orderKeeper, OrderHandlerListener orderHandlerListener, int orderTimeout) {
        signal = new EJavaSignal();
        socket = new EClientSocket(trader, signal);
        this.orderKeeper = orderKeeper;
        this.orderHandlerListener = orderHandlerListener;
        this.openOrderTimeoutMillis = orderTimeout * 1000;
        orderIdFactory = new OrderIdFactory();
        accountSemaphore = new Semaphore(0);
        eventReport = Dispatcher.getInstance().getEventReport();
    }

    boolean hasOpenOrders() {
        return orderKeeper.hasOpenOrders();
    }

    boolean isConnected() {
        return socket.isConnected();
    }

    void setConnectedToIB(boolean isConnectedToIB) {
        this.isConnectedToIB = isConnectedToIB;
    }

    String getTargetAccount() {
        return targetAccount;
    }

    public EClientSocket getSocket() {
        return socket;
    }


    String connect(String host, int port, int clientID, String targetAccount) {
        if (socket.isConnected()) {
            throw new RuntimeException("Already connected to TWS/IBG.");
        }

        account = targetAccount;
        orderHandlerListener.onLog("OrderHandler", "Connecting to TWS/IBG");
        this.targetAccount = targetAccount;
        socket.eConnect(host, port, clientID);

        if (!socket.isConnected()) {
            String msg = "Could not connect to TWS/IBG. Make sure that TWS/IBG is running ";
            msg += "and is allowed to accept connections on host " + host + ", port " + port + ".";
            throw new RuntimeException(msg);
        }
        orderHandlerListener.onLog("OrderHandler", "Connected to TWS/IBG, server version: " + socket.serverVersion());
        EReader reader = new EReader(socket, signal);
        reader.start();


        service = Executors.newSingleThreadExecutor();
        service.execute(() -> {
            while (socket.isConnected()) {
                signal.waitForSignal();
                try {
                    reader.processMsgs();
                } catch (Exception e) {
                    orderHandlerListener.onLog("TraderAssistant", "EReader exception: " + e.getMessage());
                }
            }
        });


        boolean orderIdAcquired = orderIdFactory.acquireNextOrderID();
        if (!orderIdAcquired) {
            eventReport.report("TraderAssistant", "Failed to acquire order ID semaphore.");
            disconnect();
            throw new RuntimeException("Could not acquire next order ID.");
        }

        boolean accountNameAcquired = acquireAccountInfo();
        if (!accountNameAcquired) {
            eventReport.report("TraderAssistant", "Failed to acquire account info semaphore.");
            disconnect();
            throw new RuntimeException("Could not retrieve information for account: [" + targetAccount + "]");
        }

        socket.setServerLogLevel(3); // IB Log levels: 1=SYSTEM 2=ERROR 3=WARNING 4=INFORMATION 5=DETAIL
        socket.reqNewsBulletins(true);

        orderHandlerListener.onLog("TraderAssistant", "Selected account: " + targetAccount);
        socket.reqAccountUpdates(true, targetAccount);
        setConnectedToIB(true);
        return targetAccount;
    }

    void disconnect() {
        if (socket.isConnected()) {
            socket.eDisconnect();
        }
        if (service != null) {
            service.shutdown();
        }
    }

    void setNextOrderID(int orderId) {
        orderIdFactory.setNextOrderID(orderId);
    }

    /**
     * While TWS/IBG were disconnected from the IB server, some order executions may have occurred.
     * To detect executions, request them explicitly after the reconnection.
     */
    void requestExecutions() {
        try {
            ExecutionFilter executionFilter = new ExecutionFilter();
            socket.reqExecutions(-1, executionFilter);
        } catch (Throwable t) {
            // Do not allow exceptions come back to the socket -- it will cause disconnects
            orderHandlerListener.onLog("TraderAssistant", t.getMessage());
        }
    }

    void releaseAccount(String account) {
        this.account = account;
        accountSemaphore.release();
    }

    private boolean acquireAccountInfo() {
        try {
            return accountSemaphore.tryAcquire(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    void placeMarketOrder(String strategyName, String exchange, String symbol, Types.Action orderAction, int quantity) {
        if (!isConnectedToIB || !socket.isConnected()) {
            return;
        }
        if (orderKeeper.hasOpenOrders()) {
            return;
        }

        Order order = new Order();
        order.overridePercentageConstraints(true);
        order.action(orderAction);
        Decimal d = Decimal.get(quantity);
        order.totalQuantity(d);
        order.orderType(OrderType.MKT);
        order.account(account);

        Contract contract = new Contract();
        contract.secType(Types.SecType.FUT);
        contract.exchange(exchange);
        contract.localSymbol(symbol);

        int orderID = orderIdFactory.getNextOrderID();
        orderKeeper.add(new OpenOrder(orderID, strategyName, quantity));

        Timer timer = new Timer();
        timer.schedule(new OpenOrderTask(orderID), openOrderTimeoutMillis);

        socket.placeOrder(orderID, contract, order);
        String msg = "Order " + orderID + ": Submitted ";
        msg += "[action: " + orderAction.getApiString().toLowerCase() + ", quantity: " + quantity + ", instrument: " + symbol + "]";
        orderHandlerListener.onLog(strategyName, msg);
        orderIdFactory.incrementOrderID();
    }

    // inner class
    private class OpenOrderTask extends TimerTask {
        private final int orderID;

        OpenOrderTask(int orderID) {
            this.orderID = orderID;
        }

        @Override
        public void run() {
            OpenOrder openOrder = orderKeeper.getOpenOrder(orderID);
            if (openOrder != null) {
                String strategyName = openOrder.getStrategyName();
                orderHandlerListener.onOrderTimeout(strategyName, orderID);
            }
        }
    }
}
