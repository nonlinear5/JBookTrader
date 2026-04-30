package com.jbooktrader.platform.ibhandler;

/**
 * This class represents immutable objects which encapsulate the order execution information.
 *
 * @author Eugene Kononov
 */
public class OrderExecution {
    private final int orderID;
    private final String instrument;
    private final int quantity;
    private final String side;
    private final double aveFillPrice;

    public OrderExecution(int orderID, String instrument, int quantity, String side, double aveFillPrice) {
        this.orderID = orderID;
        this.instrument = instrument;
        this.quantity = quantity;
        this.side = side;
        this.aveFillPrice = aveFillPrice;
    }

    /**
     * Returns the ID of the executed order.
     *
     * @return A unique integer, assigned by the API
     */
    public int getOrderID() {
        return orderID;
    }

    /**
     * Returns the instrument of the executed order.
     *
     * @return A symbol which uniquely identifies a CME future in the IB "symbol" format, e.g. "ESZ5"
     */
    public String getInstrument() {
        return instrument;
    }

    /**
     * Returns the total executed quantity.
     *
     * @return A positive integer, corresponding to the number of contracts executed
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Returns the transaction side, either "BOT" or "SLD" for the executed order.
     *
     * @return transaction side
     */
    public String getSide() {
        return side;
    }

    /**
     * Returns the average price at which the order was filled. For example, suppose that an order
     * to buy 100 contracts is submitted. The execution was as follows: 50 contracts bought at $100.00,
     * 30 contracts bought at $100.10, and 20 contracts bought at $100.20. Then the average fill price is:
     * (50 * $100.00 + 30 * $100.10 + 20 * $100.20) / 100 = $100.07
     *
     * @return Average "fill" price of the executed order.
     */
    public double getAverageFillPrice() {
        return aveFillPrice;
    }
}
