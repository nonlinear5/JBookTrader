package com.jbooktrader.platform.model;

import com.jbooktrader.platform.marketbook.*;
import com.jbooktrader.platform.performance.*;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.format.*;

import java.text.*;
import java.util.*;

import static com.jbooktrader.platform.model.StrategyTableColumn.*;

/**
 * @author Eugene Kononov
 */
public class StrategyTableModel extends TableDataModel {
    private final DecimalFormat df0, df2, df6;
    private final Dispatcher dispatcher;

    public StrategyTableModel() {
        StrategyTableColumn[] columns = StrategyTableColumn.values();
        ArrayList<String> allColumns = new ArrayList<>();
        for (StrategyTableColumn column : columns) {
            allColumns.add(column.getColumnName());
        }
        setSchema(allColumns.toArray(new String[columns.length]));
        dispatcher = Dispatcher.getInstance();
        df0 = NumberFormatterFactory.getNumberFormatter(0);
        df2 = NumberFormatterFactory.getNumberFormatter(2);
        df6 = NumberFormatterFactory.getNumberFormatter(6);
    }

    Strategy getStrategyForRow(int row) {
        String strategyName = (String) getRow(row)[Strategy.ordinal()];
        return dispatcher.getStrategy(strategyName);
    }

    private int getRowForStrategy(Strategy strategy) {
        int selectedRow = -1;
        int rowCount = getRowCount();
        for (int row = 0; row < rowCount; row++) {
            String name = (String) getRow(row)[Strategy.ordinal()];
            if (name.equals(strategy.getName())) {
                selectedRow = row;
                break;
            }
        }
        return selectedRow;
    }

    public void update(Strategy strategy) {
        int rowIndex = getRowForStrategy(strategy);
        AbstractMap<StrategyTableColumn, Object> row = new EnumMap<>(StrategyTableColumn.class);

        MarketBook marketBook = strategy.getMarketBook();
        if (!marketBook.isEmpty()) {
            MarketSnapshot lastMarketSnapshot = marketBook.getSnapshot();
            row.put(Bid, df6.format(lastMarketSnapshot.getBid()));
            row.put(Ask, df6.format(lastMarketSnapshot.getAsk()));
            row.put(DepthBalance, df0.format(lastMarketSnapshot.getBalance()));
            row.put(Contract, marketBook.getContract());
        }

        row.put(Position, strategy.getPositionManager().getCurrentPosition());

        PerformanceManager pm = strategy.getPerformanceManager();
        row.put(Trades, df0.format(pm.getTrades()));
        row.put(AveDuration, df0.format(pm.getAveDuration()));
        row.put(MaxSL, df0.format(pm.getMaxSingleLoss()));
        row.put(MaxDD, df0.format(pm.getMaxDrawdown()));
        row.put(APD, df2.format(pm.getAPD()));
        row.put(OG, df2.format(pm.getOptimalGrowth()));
        row.put(PI, df2.format(pm.getPI()));
        row.put(NetProfit, df0.format(pm.getNetProfit()));

        updateRow(rowIndex, row);
    }

    public void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Strategy.ordinal()] = strategy.getName();
        String symbol = strategy.getTicker();
        row[Ticker.ordinal()] = symbol;
        addRow(row);
    }
}
