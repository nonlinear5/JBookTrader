package com.jbooktrader.platform.ibhandler;

import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * @author Eugene Kononov
 */
class OrderIdFactory {
    private final Semaphore orderIdSemaphore;
    private int nextOrderID;

    OrderIdFactory() {
        orderIdSemaphore = new Semaphore(0);
    }

    boolean acquireNextOrderID() {
        try {
            return orderIdSemaphore.tryAcquire(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw new RuntimeException("Failed to acquire next order ID", e);
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
