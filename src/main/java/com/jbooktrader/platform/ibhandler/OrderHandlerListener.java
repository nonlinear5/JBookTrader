package com.jbooktrader.platform.ibhandler;

import java.util.*;

/**
 * This is the interface for the implementing class which acts as a "listener" to all Order Handler events.
 * The listener must be registered with Order Handler in the following manner:
 * <blockquote><pre>
 *     OrderHandlerListener listener = new OrderHandlerListenerSample();
 *     OrderHandler orderHandler = new OrderHandler(listener);
 * </pre></blockquote>
 * Order Handler communicates its events to the API user by invoking the appropriate methods of the object which
 * implements this interface. These invocations run in the API thread (as opposed to the user thread). More formally,
 * the interaction model follows the request/response paradigm, where the requests and the responses are asynchronous
 * relative to each other.
 *
 * @author Eugene Kononov
 */
public interface OrderHandlerListener {
    /**
     * This callback fires when a previously submitted order is fully executed, i.e. the quantity executed (filled)
     * equals to the quantity submitted. Partial fills are not reported using this callback.
     *
     * @param strategyName   The strategy associated with this order
     * @param orderExecution an {@link OrderExecution} object containing the order execution details
     */
    void onOrderExecution(String strategyName, OrderExecution orderExecution);

    /**
     * This callback fires when an order is rejected. The likely causes include the following:
     * <ul>
     * <li>Instrument or exchange are invalid
     * <li>Order size exceeds the available account margin
     * <li>Order violates precautionary settings for the account
     * <li>The instrument is approaching the "physical delivery" date
     * <li>The specified instrument is currently not being traded
     * </ul>
     * See <a href="https://www.interactivebrokers.com/en/software/api/apiguide/tables/api_message_codes.htm">complete list of IB API rror codes</a>
     *
     * @param strategyName     The strategy associated with this order
     * @param orderID          The ID of the rejected order
     * @param errorCode        The error code associated with the rejection action
     * @param rejectionMessage The reason for order rejection
     */
    void onOrderRejection(String strategyName, int orderID, int errorCode, String rejectionMessage);

    /**
     * This callback fires when an order is overfilled. This occurs when the quantity executed exceeds the
     * quantity submitted. This has been observed to occasionally happen in the paper accounts.
     *
     * @param strategyName     The strategy associated with this order
     * @param orderID          The ID of the overfilled order
     * @param executedQuantity The executed quantity
     */
    void onOrderOverfill(String strategyName, int orderID, int executedQuantity);

    /**
     * This callback fires if an order was submitted, but was neither executed, rejected, or overfilled within
     * the specified period of time, called the "open order timeout period". A market order is typically expected
     * to be either executed or rejected within 250 milliseconds. A timeout indicates an unexpected circumstance
     * such as the problem at the exchange, or the problem on the IB side.
     *
     * @param strategyName The strategy associated with this order
     * @param orderID      The ID of the order which timed out
     */
    void onOrderTimeout(String strategyName, int orderID);

    /**
     * This callback fires when account portfolio (the collection of currently open positions) changes.
     * The portfolio is represented by a {@link Map} where the key is the instrument, and the value is the position in
     * that instrument. A positive value indicates a long position, and a negative value represents a short position.
     * For example, the following portfolio
     * <p>
     * {ESZ5=10, CLZ5=-20}
     * <p>
     * indicates that the account is long 10 ES contracts, and short 20 CL contracts.
     *
     * @param portfolio The collection of currently open positions
     */
    void onPortfolioUpdate(Map<String, Integer> portfolio);

    /**
     * This callback fires when account value changes. This happens when:
     * <ul>
     * <li>The position size in any instrument has changed because the position was opened, closed, increased, or reduced
     * <li>The market value of an open position has changed because the price of the underlying instrument has changed
     * </ul>
     *
     * @param tag   account attribute
     * @param value account value associated with the account attribute
     */
    void onAccountUpdate(String tag, double value);

    /**
     * This callback fires when an informational message is received from the exchange or from the IB. These messages
     * are typically about particular instruments which are temporarily unavalable for trading due to technical problems
     * at the exchange where those instruments are traded. Here is an example of a bulletin:
     *
     * <i>To CBOE2 traders: Mon Oct 19 10:16:54 2015 EST CBOE2 is currently unavailable for trading due to technical
     * problems at the exchange.</i>
     *
     * @param msgId        The identifier associated with the news bulletin
     * @param msgType      The news bulletin type
     * @param message      The news bulletin message
     * @param origExchange The exchange associated with the news bulletin
     */
    void onNewsBulletin(int msgId, int msgType, String message, String origExchange);

    /**
     * This callback fires when IB API generates an error.
     *
     * @param error Description of the error
     */
    void onError(String error);

    /**
     * This callback fires when Order Handler takes an action which can be logged. Order Handler does not have its own
     * Logger, but it does raise logging events via this callback. The user is expected to process these logging
     * events with any standard logger (such as such as such as Apache Commons logging, log4j, logback, java.util.logging),
     * or with a custom logger.
     *
     * @param reporter   The reporter of the message
     * @param logMessage Message to be logged
     */
    void onLog(String reporter, String logMessage);
}
