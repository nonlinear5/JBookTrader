package com.jbooktrader.platform.ibhandler;

/**
 * @author Eugene Kononov
 */
class OpenOrder {
    private final int id;
    private final String strategyName;
    private final int quantity;

    OpenOrder(int id, String strategyName, int quantity) {
        this.id = id;
        this.strategyName = strategyName;
        this.quantity = quantity;
    }

    int getId() {
        return id;
    }

    String getStrategyName() {
        return strategyName;
    }

    int getQuantity() {
        return quantity;
    }

}
