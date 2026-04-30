package com.jbooktrader.platform.report;

import com.jbooktrader.platform.model.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.*;
import com.jbooktrader.platform.util.format.*;

import java.text.*;
import java.util.*;

/**
 * @author Eugene Kononov
 */
public class StatusReport {
    private static final DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);

    public StringBuilder generate() {
        StringBuilder report = new StringBuilder();
        report.append("\r\nStrategy, Ticker, Trades, NetProfit\r\n");

        Dispatcher dispatcher = Dispatcher.getInstance();
        List<Strategy> strategies = new ArrayList<>(dispatcher.getOrderManager().getAssistant().getAllStrategies());
        Collections.sort(strategies);

        for (Strategy strategy : strategies) {
            PerformanceManager pm = strategy.getPerformanceManager();
            report.append(strategy.getName()).append(", ");
            report.append(strategy.getTicker()).append(", ");
            report.append(pm.getTrades()).append(", ");
            report.append(df0.format(pm.getNetProfit()));
            report.append("\r\n");
        }

        return report;
    }
}

