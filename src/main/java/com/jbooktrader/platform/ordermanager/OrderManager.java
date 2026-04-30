package com.jbooktrader.platform.ordermanager;

import com.jbooktrader.platform.email.*;
import com.jbooktrader.platform.ibhandler.*;
import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.position.*;
import com.jbooktrader.platform.report.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
public class OrderManager implements OrderHandlerListener {
    private final EventReport eventReport;
    private final OrderManagerAssistant orderManagerAssistant;
    private final Dispatcher dispatcher;
    private final Notifier notifier;

    public OrderManager() {
        orderManagerAssistant = new OrderManagerAssistant();
        dispatcher = Dispatcher.getInstance();
        eventReport = dispatcher.getEventReport();
        notifier = Notifier.getInstance();
    }

    public OrderManagerAssistant getAssistant() {
        return orderManagerAssistant;
    }

    public void shutDown() {
        orderManagerAssistant.shutDown();
    }

    @Override
    public void onAccountUpdate(String accountAttribute, double value) {
        if (accountAttribute.equalsIgnoreCase("ExcessLiquidity")) {
            dispatcher.getPortfolioManager().setExcessLiquidity(value);
        }

        if (accountAttribute.equalsIgnoreCase("NetLiquidation")) {
            eventReport.report("IB Account", "NetLiquidation: " + value);
            dispatcher.getPortfolioManager().setAccountValue(value);
            dispatcher.fireModelChanged(ModelListener.Event.SystemStatusUpdate);
        }
    }

    @Override
    public void onNewsBulletin(int msgId, int msgType, String message, String exchange) {
        if (message.contains("NOTIFICATION REGARDING SPECIAL LABELING")) {
            return; // ignore this type of message
        }

        String newsBulletin = "IB bulletin: [<i>Id</i>: " + msgId + ", <i>Type</i>: " + msgType + ", <i>Exchange</i>: " + exchange + ", <i>Msg</i>: " + message + "]";
        eventReport.report("IB bulletin", newsBulletin);
        notifier.submit(newsBulletin);
    }

    @Override
    public void onOrderExecution(String strategyName, OrderExecution orderExecution) {
        Strategy strategy = dispatcher.getStrategy(strategyName);
        PositionManager positionManager = strategy.getPositionManager();
        positionManager.update(orderExecution);
    }

    @Override
    public void onOrderRejection(String strategyName, int orderID, int errorCode, String errorMessage) {
        if (dispatcher.getMode() != Mode.ForceClose) {
            String reason = "Order " + orderID + " was rejected with message:  " + errorMessage;
            orderManagerAssistant.forceClose(reason);
        }
    }

    @Override
    public void onError(String error) {
        if (dispatcher.getMode() != Mode.ForceClose) {
            orderManagerAssistant.forceClose(error);
        }
    }

    @Override
    public void onOrderOverfill(String strategyName, int orderID, int executedQuantity) {
        if (dispatcher.getMode() != Mode.ForceClose) {
            String msg = "Order " + orderID + ": Overfilled. Executed quantity: " + executedQuantity;
            orderManagerAssistant.forceClose(msg);
        }
    }

    @Override
    public void onOrderTimeout(String strategyName, int orderID) {
        String msg = "Order " + orderID + ": timed out.";
        if (dispatcher.getMode() != Mode.ForceClose) {
            orderManagerAssistant.forceClose(msg);
        }
    }

    @Override
    public void onPortfolioUpdate(Map<String, Integer> portfolio) {
        orderManagerAssistant.setPortfolio(portfolio);
        dispatcher.fireModelChanged(ModelListener.Event.SystemStatusUpdate);
        eventReport.report("IB Account", "Portfolio: " + portfolio);
    }

    @Override
    public void onLog(String reporter, String message) {
        eventReport.report(reporter, message);
    }
}
