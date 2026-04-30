package com.jbooktrader.platform.test.ib;

/**
 * @author Eugene Kononov
 */
class Validator {
    private String instrument;
    private int quantity;
    private String transactionType;
    private double aveFillPrice;
    private int errorCode;
    private String errorMessage;
    private int orderId;

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public double getAveFillPrice() {
        return aveFillPrice;
    }

    public void setAveFillPrice(double aveFillPrice) {
        this.aveFillPrice = aveFillPrice;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }
}