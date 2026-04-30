package com.jbooktrader.platform.email;

import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.format.*;

import java.text.*;
import java.util.*;

/**
 * @author Eugene Kononov
 */
public class TradeNotification {
    private final static DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);
    private final static DecimalFormat df1 = NumberFormatterFactory.getNumberFormatter(1);
    private final static DecimalFormat df6 = NumberFormatterFactory.getNumberFormatter(6);
    private final Strategy strategy;
    private final Trade trade;
    private final double tradeProfit;

    public TradeNotification(Strategy strategy, Trade trade, double tradeProfit) {
        this.strategy = strategy;
        this.trade = trade;
        this.tradeProfit = tradeProfit;
    }

    public synchronized String getText() {
        long secondsInMarket = trade.getTimeInMarket() / 1000;
        String timeInMarket = (secondsInMarket < 60) ? secondsInMarket + " seconds" : df1.format(secondsInMarket / 60d) + " minutes";

        StringBuilder msg = new StringBuilder();
        msg.append("This is a trade notification:");
        msg.append("<br>");
        msg.append("<table border=\"1\" cellpadding=\"2\" cellspacing=\"0\" width=100%>");
        msg.append("<tr>").append("<td>Strategy</td><td>").append(strategy.getName()).append("</td></tr>");
        msg.append("<tr>").append("<td>Contract</td><td>").append(strategy.getMarketBook().getSnapshot().getContract()).append("</td></tr>");
        msg.append("<tr>").append("<td>Entry date/time</td><td>").append(new Date(trade.getEntryTime())).append("</td></tr>");
        msg.append("<tr>").append("<td>Exit date/time</td><td>").append(new Date(trade.getExitTime())).append("</td></tr>");
        msg.append("<tr>").append("<td>Duration</td><td>").append(timeInMarket).append("</td></tr>");
        msg.append("<tr>").append("<td>Quantity bought</td><td>").append(trade.getQuantityBought()).append("</td></tr>");
        msg.append("<tr>").append("<td>Quantity sold</td><td>").append(trade.getQuantitySold()).append("</td></tr>");
        msg.append("<tr>").append("<td>Average bought price</td><td>").append(df6.format(trade.getAverageBoughtPrice())).append("</td></tr>");
        msg.append("<tr>").append("<td>Average sold price</td><td>").append(df6.format(trade.getAverageSoldPrice())).append("</td></tr>");
        msg.append("<tr>").append("<td>Slippage (points)</td><td>").append(df6.format(trade.getSlippagePoints())).append("</td></tr>");
        msg.append("<tr>").append("<td>Slippage (amount)</td><td>").append(df0.format(trade.getSlippageAmount())).append("</td></tr>");
        msg.append("<tr>").append("<td>P&L</td><td>").append(df0.format(tradeProfit)).append("</td></tr>");
        msg.append("</table>");
        return msg.toString();
    }
}
