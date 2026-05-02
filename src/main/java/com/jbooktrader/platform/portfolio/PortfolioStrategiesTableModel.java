package com.jbooktrader.platform.portfolio;

import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.model.TableDataModel;
import com.jbooktrader.platform.performance.PerformanceManager;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.format.NumberFormatterFactory;

import java.text.DecimalFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * Strategy parameters table model.
 *
 * @author Eugene Kononov
 */
public class PortfolioStrategiesTableModel extends TableDataModel {
    protected final DecimalFormat df0, df2, df1;

    public PortfolioStrategiesTableModel(boolean addStrategies) {
        Column[] columns = Column.values();
        ArrayList<String> allColumns = new ArrayList<>();
        for (Column column : columns) {
            allColumns.add(column.getColumnName());
        }
        String[] schema = allColumns.toArray(new String[0]);
        setSchema(schema);

        df0 = NumberFormatterFactory.getNumberFormatter(0);
        df1 = NumberFormatterFactory.getNumberFormatter(1);
        df2 = NumberFormatterFactory.getNumberFormatter(2);

        if (addStrategies) {
            Dispatcher.getInstance().getStrategies().forEach(this::addStrategy);
        }
    }

    public Class getColumnClass(int column) {
        if (column == 0) {
            return Boolean.class;
        } else {
            return String.class;
        }
    }

    public boolean isCellEditable(int row, int column) {
        return column == 0;
    }

    public List<String> getIncludedStrategies() {
        List<String> includedStrategies = new ArrayList<>();
        int rowCount = getRowCount();
        for (int row = 0; row < rowCount; row++) {
            if (getIsIncluded(row)) {
                String name = getStrategyNameForRow(row);
                includedStrategies.add(name);
            }
        }

        return includedStrategies;

    }

    private int getRowForStrategy(Strategy strategy) {
        int selectedRow = -1;
        int rowCount = getRowCount();
        for (int row = 0; row < rowCount; row++) {
            String name = getStrategyNameForRow(row);
            if (name.equals(strategy.getName())) {
                selectedRow = row;
                break;
            }
        }
        return selectedRow;
    }

    public void update(Strategy strategy) {
        int rowIndex = getRowForStrategy(strategy);
        AbstractMap<Column, Object> row = new EnumMap<>(Column.class);


        PerformanceManager pm = strategy.getPerformanceManager();
        row.put(Column.Trades, df0.format(pm.getTrades()));
        row.put(Column.AveDuration, df0.format(pm.getAveDuration()));
        row.put(Column.MaxSL, df0.format(pm.getMaxSingleLoss()));
        row.put(Column.MaxDD, df0.format(pm.getMaxDrawdown()));
        row.put(Column.APD, df2.format(pm.getAPD()));
        row.put(Column.OG, df2.format(pm.getOptimalGrowth()));
        row.put(Column.PI, df2.format(pm.getPI()));
        row.put(Column.OL, df1.format(pm.getOptimalLeverage()));
        row.put(Column.NetProfit, df0.format(pm.getNetProfit()));

        updateRow(rowIndex, row);
    }

    public void clear() {
        int rowCount = getRowCount();
        int colCount = getColumnCount();
        for (int row = 0; row < rowCount; row++) {
            for (int col = 3; col < colCount; col++) {
                setValueAt(null, row, col);
            }
        }
    }

    private String getStrategyNameForRow(int row) {
        return (String) getRow(row)[Column.Strategy.ordinal()];
    }

    private boolean getIsIncluded(int row) {
        return (Boolean) getRow(row)[Column.Include.ordinal()];
    }

    private void addStrategy(Strategy strategy) {
        Object[] row = new Object[getColumnCount()];
        row[Column.Include.ordinal()] = Boolean.TRUE;
        row[Column.Strategy.ordinal()] = strategy.getName();
        String symbol = strategy.getTicker();
        row[Column.Symbol.ordinal()] = symbol;
        addRow(row);
    }

    public enum Column {
        Include("Include"),
        Strategy("Strategy"),
        Symbol("Ticker"),
        Trades("Trades"),
        AveDuration("Duration"),
        MaxSL("MSL"),
        MaxDD("MDD"),
        APD("APD"),
        OG("OG"),
        PI("PI"),
        OL("OL"),
        NetProfit("Net Profit");

        private final String columnName;

        Column(String columnName) {
            this.columnName = columnName;
        }

        public String getColumnName() {
            return columnName;
        }

    }

}
