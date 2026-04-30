package com.jbooktrader.platform.portfolio;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.strategy.*;

import java.util.*;

/**
 * @author Eugene Kononov
 */

public class PortfolioManager {
    protected final Dispatcher dispatcher;
    private final double maximumLeverage;
    protected double accountValue, excessLiquidity;

    public PortfolioManager(double maximumLeverage) {
        this.maximumLeverage = maximumLeverage;
        dispatcher = Dispatcher.getInstance();
    }

    public void updateOnTrade(double tradeProfit) {
        accountValue += tradeProfit;
    }

    public double getAccountValue() {
        return accountValue;
    }

    public void setAccountValue(double accountValue) {
        this.accountValue = accountValue;
    }

    public void setExcessLiquidity(double excessLiquidity) {
        this.excessLiquidity = excessLiquidity;
    }

    public boolean isWithinMaxLeverage() {
        double valueOfAllPositions = 0;
        Collection<Strategy> strategies = dispatcher.getOrderManager().getAssistant().getAllStrategies();

        for (Strategy strategy : strategies) {
            double strategyPositionValue = Math.abs(strategy.getPositionValue());
            valueOfAllPositions += strategyPositionValue;
        }

        double leverage = valueOfAllPositions / accountValue;
        return leverage <= maximumLeverage;

    }
}
