package com.jbooktrader.platform.test.ib;


import com.ib.client.*;
import com.jbooktrader.platform.ibhandler.*;
import com.jbooktrader.platform.model.*;
import org.junit.*;

import java.io.*;

/**
 * @author Eugene Kononov
 */
public class OrderHandlerTest {
    private static final String host = "127.0.0.1";
    private static final int port = 4002;
    private final static int openOrderTimeoutSeconds = 30;
    private static OrderHandler orderHandler;
    private static OrderHandlerTestListener orderHandlerTestListener;
    private final String instrument = "ESU8";
    private final String exchange = "CME";

    @BeforeClass
    public static void setup() throws IOException {
        Dispatcher.getInstance().init();
        orderHandlerTestListener = new OrderHandlerTestListener();
        orderHandler = new OrderHandler(orderHandlerTestListener, openOrderTimeoutSeconds);
        String account = orderHandler.connect(host, port, 1, "edemo");
        if (!account.startsWith("D")) {
            orderHandler.disconnect();
            throw new RuntimeException("A simulated account is required to run the tests");
        }
    }

    @AfterClass
    public static void tearDown() {
        orderHandler.disconnect();
    }

    // helper method: places an order to buy, and validates the order execution
    private void buy(String exchange, String instrument, int quantity) {
        orderHandlerTestListener.resetValidator();
        orderHandler.submitMarketOrder("test", exchange, instrument, Types.Action.BUY, quantity);
        Validator validator = orderHandlerTestListener.getValidator();
        Assert.assertNotEquals("Order ID may not be 0", 0, validator.getOrderId());
        Assert.assertEquals("Instruments do not match", instrument, validator.getInstrument());
        Assert.assertEquals("Quantities do not match", quantity, validator.getQuantity());
        Assert.assertEquals("Transaction types do not match", "BOT", validator.getTransactionType());
        Assert.assertNotEquals("Average fill price may not be 0", 0, validator.getAveFillPrice());
        Assert.assertEquals("Error codes do not match", 0, validator.getErrorCode());
        Assert.assertNull("Error messages do not match", validator.getErrorMessage());
    }

    // helper method: places an order to sell, and validates the order execution
    private void sell(String exchange, String instrument, int quantity) {
        orderHandlerTestListener.resetValidator();
        orderHandler.submitMarketOrder("test", exchange, instrument, Types.Action.SELL, quantity);
        Validator validator = orderHandlerTestListener.getValidator();
        Assert.assertNotEquals("Order ID may not be 0", 0, validator.getOrderId());
        Assert.assertEquals("Instruments do not match", instrument, validator.getInstrument());
        Assert.assertEquals("Quantities do not match", quantity, validator.getQuantity());
        Assert.assertEquals("Transaction types do not match", "SLD", validator.getTransactionType());
        Assert.assertNotEquals("Average fill may not be 0", 0, validator.getAveFillPrice());
        Assert.assertEquals("Error codes do not match", 0, validator.getErrorCode());
        Assert.assertNull("Error messages do not match", validator.getErrorMessage());
    }

    // helper method: places an order to buy, and expects an error code and error message
    private void buyAndExpectFail(String exchange, String instrument, int quantity, int expectedErrorCode, String excpectedErrorMessage) {
        orderHandlerTestListener.resetValidator();
        orderHandler.submitMarketOrder("test", exchange, instrument, Types.Action.BUY, quantity);
        Validator validator = orderHandlerTestListener.getValidator();
        Assert.assertEquals(expectedErrorCode, validator.getErrorCode());
        if (excpectedErrorMessage != null) {
            Assert.assertEquals(excpectedErrorMessage, validator.getErrorMessage());
        }
    }

    // helper method: places an order to buy, and expects an error code and error message
    private void buyAndExpectFail(String exchange, String instrument, int quantity, int expectedErrorCode) {
        buyAndExpectFail(exchange, instrument, quantity, expectedErrorCode, null);
    }

    // helper method: places an order to sell, and expects an error code and error message
    private void sellAndExpectFail(String exchange, String instrument, int quantity, int expectedErrorCode, String excpectedErrorMessage) {
        orderHandlerTestListener.resetValidator();
        orderHandler.submitMarketOrder("test", exchange, instrument, Types.Action.SELL, quantity);
        Validator validator = orderHandlerTestListener.getValidator();
        Assert.assertEquals(expectedErrorCode, validator.getErrorCode());
        if (excpectedErrorMessage != null) {
            Assert.assertEquals(excpectedErrorMessage, validator.getErrorMessage());
        }
    }

    // helper method: places an order to sell, and expects an error code
    private void sellAndExpectFail(String exchange, String instrument, int quantity, int expectedErrorCode) {
        sellAndExpectFail(exchange, instrument, quantity, expectedErrorCode, null);
    }

    @Test
    public void simpleTest() {
        int quantity = 1;
        buy(exchange, instrument, quantity);
        sell(exchange, instrument, quantity);
    }

    @Test
    public void sellShortTest() {
        int quantity = 20;
        sell(exchange, instrument, quantity);
        buy(exchange, instrument, quantity);
    }


    @Test
    public void smallQuantityTest() {
        int quantity = 1;
        for (int trial = 1; trial <= 10; trial++) {
            buy(exchange, instrument, quantity);
            sell(exchange, instrument, quantity);
        }
    }

    @Test
    public void variableQuantityTest() {
        for (int quantity = 1; quantity <= 10; quantity++) {
            buy(exchange, instrument, quantity);
            sell(exchange, instrument, quantity);
        }
    }

    @Test
    public void largeQuantityTest() {
        int quantity = 25;
        for (int trial = 1; trial <= 5; trial++) {
            buy(exchange, instrument, quantity);
            sell(exchange, instrument, quantity);
        }
    }

    @Test
    public void invalidExchangeTest() {
        int expectedErrorCode = 200;
        String expectedErrorMessage = "Invalid destination exchange specified";
        buyAndExpectFail("BAD_EXCHANGE", instrument, 1, expectedErrorCode, expectedErrorMessage);
    }

    @Test
    public void invalidInstrumentTest() {
        int expectedErrorCode = 200;
        String expectedErrorMessage = "No security definition has been found for the request";
        buyAndExpectFail(exchange, "BAD_INSTRUMENT", 1, expectedErrorCode, expectedErrorMessage);
    }

    @Test
    public void zeroQuantityTest() {
        int expectedErrorCode = 321;
        buyAndExpectFail(exchange, instrument, 0, expectedErrorCode);
        sellAndExpectFail(exchange, instrument, 0, expectedErrorCode);
    }

    @Test
    public void negativeQuantityTest() {
        int expectedErrorCode = 434;
        buyAndExpectFail(exchange, instrument, -1, expectedErrorCode);
        sellAndExpectFail(exchange, instrument, -1, expectedErrorCode);
    }


    @Test
    public void crossSideTest() {
        buy(exchange, instrument, 10); // 10 long now
        sell(exchange, instrument, 20); // 10 short now
        buy(exchange, instrument, 10); // flat now

        sell(exchange, instrument, 10); // 10 short now
        buy(exchange, instrument, 20); // 10 long now
        sell(exchange, instrument, 10); // flat now
    }


    @Test
    public void expiredContractTest() {
        int expectedErrorCode = 200;
        String expectedErrorMessage = "No security definition has been found for the request";
        buyAndExpectFail(exchange, "ESU7", 1, expectedErrorCode, expectedErrorMessage);
    }

    @Test
    public void nymexTest() {
        for (int quantity = 1; quantity <= 10; quantity++) {
            buy("Nymex", "CLV8", quantity);
            sell("Nymex", "CLV8", quantity);
        }
    }

    @Test
    public void accountMarginTest() {
        int expectedErrorCode = 201;
        buyAndExpectFail(exchange, instrument, 500, expectedErrorCode);
    }

    @Test
    public void stressTest() {
        int quantity = 1;
        for (int trial = 1; trial <= 50; trial++) {
            buy(exchange, instrument, quantity);
            sell(exchange, instrument, quantity);
        }
    }


}
