package com.jbooktrader.platform.ibhandler;

import java.util.*;

/**
 * @author Eugene Kononov
 */
class OrderKeeper {
    private final Map<Integer, OpenOrder> openOrders;

    OrderKeeper() {
        openOrders = new HashMap<>();
    }

    public synchronized void add(OpenOrder openOrder) {
        openOrders.put(openOrder.getId(), openOrder);
    }

    synchronized boolean hasOpenOrders() {
        return !openOrders.isEmpty();
    }

    synchronized OpenOrder getOpenOrder(int orderID) {
        return openOrders.get(orderID);
    }

    synchronized void removeOpenOrder(int orderID) {
        openOrders.remove(orderID);
    }

}
