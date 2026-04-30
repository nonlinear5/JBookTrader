package com.jbooktrader.platform.ibhandler;


import com.ib.client.*;
import com.jbooktrader.platform.marketbook.*;

import java.util.concurrent.*;

/**
 * The main API class which serves as the entry point for API users. This class can be thought as a "facade",
 * which isolates the Order Handler user from the internal complexities of order handling.
 *
 * @author Eugene Kononov
 */
public class OrderHandler {
    private final TraderAssistant traderAssistant;
    private final MarketDataHandler marketDataHandler;

    /**
     * Constructs an instance of Order Handler.
     *
     * @param listener     An instance of the class which implements the {@link OrderHandlerListener} interface
     * @param orderTimeout Duration of time (in seconds) which must elapse before an open order times out. An
     *                     open order timeout occurs when a submitted order was neither rejected, cancelled,
     *                     or executed within a specified period of time.
     */
    public OrderHandler(OrderHandlerListener listener, int orderTimeout) {
        Trader trader = new Trader(listener, orderTimeout);
        traderAssistant = trader.getAssistant();
        marketDataHandler = trader.getMarketDataHandler();
    }

    /**
     * Establishes a connection between Order Handler and TWS/IBG, using IB API.
     *
     * @param host     The host name (or IP address) exposed by TWS/IBG
     * @param port     The port exposed by TWS/IBG
     * @param clientID The ID of the client connecting to IB
     * @param account  The IB account to connect to (e.g. DU12345). A special value, "edemo" is also accepted,
     *                 and can be used for connecting to dynamically created "demo" accounts
     * @return The account to which the connections was established
     */
    public String connect(String host, int port, int clientID, String account) {
        return traderAssistant.connect(host, port, clientID, account);
    }

    public void subscribe(Contract contract) {
        marketDataHandler.subscribe(contract);
    }

    public void setQueue(BlockingQueue<MarketSnapshot> queue) {
        marketDataHandler.setQueue(queue);
    }

    /**
     * Returns a flag indicating whether there are any open orders which are pending execution
     *
     * @return true if there are any open orders, and false otherwise
     */
    public boolean hasOpenOrders() {
        return traderAssistant.hasOpenOrders();
    }

    /**
     * Disconnects Order Handler from IB API, and disconnects IB API from TWS/IBG. After this call, Order Handler is
     * not usable until connected again.
     */
    public void disconnect() {
        marketDataHandler.unsubscribe();
        traderAssistant.disconnect();
    }

    /**
     * Places a market order to buy/sell an instrument, and returns immediately. This method return does not indicate
     * order execution, as the order may be rejected/cancelled by the broker. The order execution, order rejection,
     * and order errors (if any) are disseminated via the callback methods in the {@link OrderHandlerListener} interface.
     * <p>
     * Example: place a market order to buy 10 S&amp;P 500 e-mini contracts on the Globex exchange, expiring in December 2015.
     * </p>
     * <blockquote><pre>
     * orderHandler.submitMarketOrder("TestStrategy", "CME", "ESZ5", Types.Action.BUY, 10);
     * </pre></blockquote>
     *
     * @param strategyName The name of the strategy which submits this order
     * @param exchange     The exchange where the instrument is traded (e.g. Globex, Nymex, Comex)
     * @param instrument   A symbol which uniquely identifies a CME future in the IB "symbol" format, e.g. "ESZ5"
     * @param quantity     A positive integer, corresponding to the number of contracts to buy/sell
     * @param orderAction  The direction of the order, either {@link Types.Action#BUY} or {@link Types.Action#SELL}
     */
    public void submitMarketOrder(String strategyName, String exchange, String instrument, Types.Action orderAction, int quantity) {
        traderAssistant.placeMarketOrder(strategyName, exchange, instrument, orderAction, quantity);
    }

}
