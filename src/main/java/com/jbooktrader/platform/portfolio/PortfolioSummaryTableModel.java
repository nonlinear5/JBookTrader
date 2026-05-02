package com.jbooktrader.platform.portfolio;

import javax.swing.*;
import java.util.AbstractMap;
import java.util.EnumMap;

/**
 * @author Eugene Kononov
 */
public class PortfolioSummaryTableModel extends PortfolioStrategiesTableModel {

    public PortfolioSummaryTableModel(boolean addStrategies) {
        super(addStrategies);
        init();
    }

    protected void init() {
        addSummary();
    }

    public void setResults(final int trades, final double netProfit, final double maxDrawdown, final double optimalGrowth, final double pi, final double apd, final double aveDuration) {
        SwingUtilities.invokeLater(() -> {
            int rowIndex = 0;
            AbstractMap<Column, Object> row = new EnumMap<>(Column.class);

            row.put(Column.Trades, df0.format(trades));
            row.put(Column.AveDuration, df0.format(aveDuration));
            row.put(Column.MaxDD, df0.format(maxDrawdown));
            row.put(Column.APD, df2.format(apd));
            row.put(Column.OG, df2.format(optimalGrowth));
            row.put(Column.PI, df2.format(pi));
            row.put(Column.NetProfit, df0.format(netProfit));

            updateRow(rowIndex, row);
            fireTableDataChanged();
        });
    }

    protected void addSummary() {
        Object[] row = new Object[getColumnCount()];
        row[Column.Include.ordinal()] = Boolean.TRUE;
        row[Column.Strategy.ordinal()] = "Portfolio";
        String symbol = "N/A";
        row[Column.Symbol.ordinal()] = symbol;
        addRow(row);
    }
}
