package com.jbooktrader.platform.ibhandler;

import java.util.concurrent.*;

/**
 * @author Eugene Kononov
 */
class OrderIdFactory {
    private final Semaphore orderIdSemaphore;
    private int nextOrderID;

    OrderIdFactory() {
        orderIdSemaphore = new Semaphore(1);
    }

    boolean acquireNextOrderID() {
        try {
            return orderIdSemaphore.tryAcquire();
        } catch (Exception e) {
            return false;
        }
    }

    int getNextOrderID() {
        return nextOrderID;
    }

    void setNextOrderID(int nextOrderID) {
        this.nextOrderID = nextOrderID;
        orderIdSemaphore.release();
    }

    void incrementOrderID() {
        nextOrderID++;
    }

}
