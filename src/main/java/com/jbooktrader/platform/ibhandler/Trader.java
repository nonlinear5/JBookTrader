package com.jbooktrader.platform.ibhandler;

import com.ib.client.Contract;
import com.ib.client.ContractDetails;
import com.ib.client.Decimal;
import com.ib.client.Execution;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class acts as a "wrapper" in the IB's API terminology.
 *
 * @author Eugene Kononov
 */
class Trader extends EWrapperAdapter {
    private final TraderAssistant traderAssistant;
    private final OrderKeeper orderKeeper;
    private final OrderHandlerListener orderHandlerListener;
    private final Map<String, Integer> portfolio;
    private final MarketDataHandler marketDataHandler;
    private final List<String> contracts;
    private String previousErrorMessage;
    private String netLiquidation;

    Trader(OrderHandlerListener orderHandlerListener, int orderTimeout) {
        this.orderHandlerListener = orderHandlerListener;
        this.orderKeeper = new OrderKeeper();
        portfolio = new ConcurrentHashMap<>();
        traderAssistant = new TraderAssistant(this, orderKeeper, orderHandlerListener, orderTimeout);
        marketDataHandler = new MarketDataHandler(traderAssistant.getSocket());
        contracts = new ArrayList<>();
    }

    public MarketDataHandler getMarketDataHandler() {
        return marketDataHandler;
    }

    TraderAssistant getAssistant() {
        return traderAssistant;
    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
        try {
            orderHandlerListener.onNewsBulletin(msgId, msgType, message, origExchange);
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("IB Bulletin", msg);
        }
    }

    @Override
    public void execDetails(int requestId, Contract contract, Execution execution) {
        try {
            int orderId = execution.orderId();
            OpenOrder openOrder = orderKeeper.getOpenOrder(orderId);
            if (openOrder == null) {
                return;
            }

            long quantity = execution.shares().longValue();
            int cumulativeQuantity = (int) execution.cumQty().longValue();
            double avePrice = execution.avgPrice();

            int openOrderQuantity = openOrder.getQuantity();
            String strategyName = openOrder.getStrategyName();
            String msg = "Order " + orderId + ": Executed ";
            msg += "[quantity: " + quantity;
            msg += ", cumulative quantity: " + cumulativeQuantity;
            msg += ", average price: " + avePrice + "]";
            orderHandlerListener.onLog(strategyName, msg);

            boolean isFilled = (cumulativeQuantity == openOrderQuantity);
            if (isFilled) {
                msg = "Order " + orderId + ": Filled ";
                msg += "[cumulative quantity: " + cumulativeQuantity + "]";
                orderHandlerListener.onLog(strategyName, msg);
                OrderExecution orderExecution = new OrderExecution(orderId, contract.localSymbol(), cumulativeQuantity, execution.side(), avePrice);
                orderHandlerListener.onOrderExecution(strategyName, orderExecution);
                orderKeeper.removeOpenOrder(orderId);
            }

            boolean isOverFilled = cumulativeQuantity > openOrderQuantity;
            if (isOverFilled) {
                int overFillQuantity = Math.abs(cumulativeQuantity - openOrderQuantity);
                msg = "Order " + orderId + " was overfilled by " + overFillQuantity + " contracts. ";
                orderHandlerListener.onLog(strategyName, msg);
                orderHandlerListener.onOrderOverfill(strategyName, orderId, cumulativeQuantity);
            }
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void managedAccounts(String accountsList) {
        try {
            orderHandlerListener.onLog("IB", "Account(s): " + accountsList);
            String targetAccount = traderAssistant.getTargetAccount();
            String[] accounts = accountsList.split(",");
            if (targetAccount.equals("edemo")) {
                String demoAccount = accounts[0];
                if (demoAccount.startsWith("D") || demoAccount.startsWith("d")) {
                    traderAssistant.releaseAccount(demoAccount);
                }
            } else {
                for (String account : accounts) {
                    if (account.equals(targetAccount)) {
                        traderAssistant.releaseAccount(targetAccount);
                        break;
                    }
                }
            }
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {
        try {
            if (key.equals("NetLiquidation")) {
                if (value != null && !value.equals(netLiquidation)) {
                    netLiquidation = value;
                    orderHandlerListener.onAccountUpdate(key, Double.parseDouble(netLiquidation));
                }
            }
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }


    @Override
    public void updatePortfolio(Contract contract, Decimal position, double marketPrice, double marketValue,
                                double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {
        try {
            String portfolioBefore = portfolio.toString();
            if (position.longValue() == 0) {
                portfolio.remove(contract.localSymbol());
            } else {
                portfolio.put(contract.localSymbol(), (int) position.longValue());
            }

            if (!portfolioBefore.equals(portfolio.toString())) {
                orderHandlerListener.onPortfolioUpdate(portfolio);
            }
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        orderHandlerListener.onLog("Contract", contractDetails.contract().toString());
        contracts.add(contractDetails.contract().localSymbol());
    }

    @Override
    public void contractDetailsEnd(int reqId) {
        marketDataHandler.subscribe(reqId, contracts);
    }

    @Override
    public void error(Exception e) {
        try {
            if (traderAssistant.isConnected()) {
                String msg = extractStackTrace(e);
                orderHandlerListener.onLog("Critical exception", msg);
                orderHandlerListener.onError(msg);
            }
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void error(String error) {
        try {
            orderHandlerListener.onLog("Critical error", error);
            orderHandlerListener.onError(error);
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    //@Override
    @Override
    public void error(int id, long errorTime, int errorCode, String errorMsg, String advancedOrderRejectJson) {
        try {
            String msg = "[Id: " + id + ", code: " + errorCode + ", msg: " + errorMsg + "]";
            if (msg.equals(previousErrorMessage)) {
                // ignore duplicate error messages
                return;
            }
            previousErrorMessage = msg;
            orderHandlerListener.onLog("IB", msg);

            switch (errorCode) {
                case 317: // Market depth data has been reset
                    marketDataHandler.reset(id);
                    break;
                case 1100: // Connectivity between IB and TWS has been lost.
                    traderAssistant.setConnectedToIB(false);
                    break;
                case 1101: // Connectivity between IB and TWS has been restored, market data lost.
                case 1102: // Connectivity between IB and TWS has been restored, market data maintained.
                    traderAssistant.setConnectedToIB(true);
                    if (orderKeeper.hasOpenOrders()) {
                        orderHandlerListener.onLog("OrderHandler", "Checking for order executions while TWS/IBG was disconnected from the IB server.");
                        traderAssistant.requestExecutions();
                    }
                    break;
                default:
                    OpenOrder openOrder = orderKeeper.getOpenOrder(id);
                    if (openOrder != null) { // Has error been sent with regards to the placed order?
                        orderKeeper.removeOpenOrder(id);
                        orderHandlerListener.onOrderRejection(openOrder.getStrategyName(), id, errorCode, errorMsg);
                    }
                    break;
            }
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void nextValidId(int orderId) {
        try {
            traderAssistant.setNextOrderID(orderId);
            orderHandlerListener.onLog("IB", "Next order ID: " + orderId);
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }


    @Override
    public void tickSize(int tickerId, int field, Decimal size) {
        try {
            marketDataHandler.tickSize(tickerId, field, (int) size.longValue());
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation,
                               int side, double price, Decimal size) {
        try {
            marketDataHandler.updateMktDepth(tickerId, position, operation, side, price, (int) size.longValue());
        } catch (Exception e) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(e);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    @Override
    public void connectionClosed() {
        try {
            orderHandlerListener.onLog("IB", "Connection between API client and TWS/IBG was closed.");
        } catch (Throwable t) {
            // Do not allow the call stack to unwind to the socket -- it will cause disconnects
            String msg = extractStackTrace(t);
            orderHandlerListener.onLog("OrderHandler", msg);
        }
    }

    private String extractStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        pw.close();
        return sw.toString();
    }
}
