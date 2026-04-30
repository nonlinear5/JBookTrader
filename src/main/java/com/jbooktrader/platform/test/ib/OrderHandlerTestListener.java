package com.jbooktrader.platform.test.ib;

import com.jbooktrader.platform.ibhandler.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */
class OrderHandlerTestListener implements OrderHandlerListener {
    private final Validator validator;
    private boolean readyToValidate;

    public OrderHandlerTestListener() {
        validator = new Validator();
    }

    public Validator getValidator() {
        while (!readyToValidate) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return validator;
    }

    public void resetValidator() {
        readyToValidate = false;
        validator.setOrderId(0);
        validator.setInstrument(null);
        validator.setQuantity(0);
        validator.setTransactionType(null);
        validator.setAveFillPrice(0);
        validator.setErrorCode(0);
        validator.setErrorMessage(null);
    }

    @Override
    public void onOrderExecution(String strategyName, OrderExecution orderExecution) {
        validator.setOrderId(orderExecution.getOrderID());
        validator.setInstrument(orderExecution.getInstrument());
        validator.setQuantity(orderExecution.getQuantity());
        String transactionType = orderExecution.getSide();
        validator.setTransactionType(transactionType);
        validator.setAveFillPrice(orderExecution.getAverageFillPrice());
        validator.setErrorCode(0);
        validator.setErrorMessage(null);
        readyToValidate = true;
    }

    @Override
    public void onOrderRejection(String strategyName, int orderID, int errorCode, String errorMessage) {
        String msg = "order " + orderID + " is rejected. Message: " + errorMessage;
        onLog(strategyName, msg);
        validator.setErrorCode(errorCode);
        validator.setErrorMessage(errorMessage);
        readyToValidate = true;
    }

    @Override
    public void onOrderOverfill(String strategyName, int orderID, int executedQuantity) {
        String msg = "order " + orderID + " is overfilled. Executed quantity: " + executedQuantity;
        onLog(strategyName, msg);
        validator.setErrorCode(0);
        validator.setErrorMessage(msg);
        readyToValidate = true;
    }

    @Override
    public void onOrderTimeout(String strategyName, int orderID) {
        String msg = "order " + orderID + " timed out.";
        onLog(strategyName, msg);
        readyToValidate = true;
    }

    @Override
    public void onPortfolioUpdate(Map<String, Integer> portfolio) {
        onLog("IB", "Portfolio: " + portfolio.toString());
    }

    @Override
    public void onAccountUpdate(String tag, double value) {
        String msg = tag + ":" + value;
        onLog("OrderHandler", msg);
    }

    @Override
    public void onNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        String msg = "News: [Id: " + msgId + ", type: " + msgType + ", message: " + message + ", exchange: " + origExchange + "]";
        onLog("IB bulletin", msg);
    }

    @Override
    public void onError(String error) {
        readyToValidate = true;
    }

    @Override
    public void onLog(String reporter, String message) {
        System.out.println(message);
    }
}