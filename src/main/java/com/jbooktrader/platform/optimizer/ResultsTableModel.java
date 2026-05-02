package com.jbooktrader.platform.optimizer;

import com.jbooktrader.platform.model.TableDataModel;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.format.NumberFormatterFactory;

import javax.swing.*;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static com.jbooktrader.platform.optimizer.PerformanceMetric.*;

/**
 * Optimization results table model
 *
 * @author Eugene Kononov
 */
public class ResultsTableModel extends TableDataModel {
    private final static int maxRows = 1000;
    private final static DecimalFormat df2 = NumberFormatterFactory.getNumberFormatter(2);
    private final static DecimalFormat df0 = NumberFormatterFactory.getNumberFormatter(0);

    public ResultsTableModel(Strategy strategy) {
        List<String> columnNames = new LinkedList<>();
        for (StrategyParam param : strategy.getParams().getAll()) {
            columnNames.add(param.getName());
        }

        for (PerformanceMetric performanceMetric : PerformanceMetric.values()) {
            columnNames.add(performanceMetric.getName());
        }

        setSchema(columnNames.toArray(new String[0]));
    }

    @Override
    public Class<?> getColumnClass(int c) {
        return DoubleNumericString.class;
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    public void setResults(final List<OptimizationResult> optimizationResults) {
        SwingUtilities.invokeLater(() -> {
            rows.clear();
            Iterator<OptimizationResult> iterator = optimizationResults.iterator();
            while (iterator.hasNext() && (rows.size() < maxRows)) {
                OptimizationResult optimizationResult = iterator.next();

                Object[] item = new Object[getColumnCount() + 1];
                StrategyParams params = optimizationResult.getParams();

                int column = 0;
                for (StrategyParam param : params.getAll()) {
                    item[column] = param.getValue();
                    column++;
                }


                item[column + Trades.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(Trades)));
                item[column + Duration.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(Duration)));
                item[column + MaxSL.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(MaxSL)));
                item[column + MaxDD.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(MaxDD)));
                item[column + APD.ordinal()] = new DoubleNumericString(df2.format(optimizationResult.get(APD)));
                item[column + OG.ordinal()] = new DoubleNumericString(df2.format(optimizationResult.get(OG)));
                item[column + PI.ordinal()] = new DoubleNumericString(df2.format(optimizationResult.get(PI)));
                item[column + NetProfit.ordinal()] = new DoubleNumericString(df0.format(optimizationResult.get(NetProfit)));

                rows.add(item);
            }
            fireTableDataChanged();
        });
    }
}
