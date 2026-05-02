package com.jbooktrader.platform.portfolio;


import com.jbooktrader.platform.backtest.ProgressListener;
import com.jbooktrader.platform.dialog.JBTDialog;
import com.jbooktrader.platform.marketbook.MarketSnapshotFilter;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ui.MessageDialog;
import com.jbooktrader.platform.util.ui.SpringUtilities;
import com.jbooktrader.platform.util.ui.TitledSeparator;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * @author Eugene Kononov
 */
public class PortfolioBackTestDialog extends JBTDialog implements ProgressListener {
    private final PreferencesHolder prefs;
    private JButton optimizeButton, cancelButton;
    private JTextFieldDateEditor fromDateEditor, toDateEditor;
    private JCheckBox useDateRangeCheckBox;
    private JPanel fromDatePanel, toDatePanel;
    private JProgressBar progressBar;
    private JComboBox<String> upperChartWeightCombo;
    private JComboBox<String> chartYaxisLocationCombo;
    private JCheckBox showLegendCheck;
    private PortfolioChart portfolioChart;
    private PortfolioStrategiesTableModel portfolioStrategiesTableModel;
    private PortfolioSummaryTableModel portfolioSummaryTableModel;

    public PortfolioBackTestDialog(JFrame parent) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        init();
        assignListeners();
        pack();
        int width = prefs.getInt(PotfolioOptimizerWindowWidth);
        int height = prefs.getInt(PotfolioOptimizerWindowHeight);
        setSize(width, height);
        setLocationRelativeTo(parent);
        optimizeButton.requestFocus();
        setVisible(true);
    }

    @Override
    public void setProgress(long count, long iterations, String text) {
        int percent = (int) (100 * (count / (double) iterations));
        progressBar.setValue(percent);
        progressBar.setString(text + ": " + percent + "% completed");
    }

    @Override
    public void setProgress(String progressText) {
        progressBar.setValue(0);
        progressBar.setString(progressText);
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    public void enableProgress() {
        progressBar.setValue(0);
        progressBar.setVisible(true);
        progressBar.requestFocus();
        optimizeButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    public void signalCompleted() {
        SwingUtilities.invokeLater(() -> {
            progressBar.setVisible(false);
            optimizeButton.setEnabled(true);
            cancelButton.setEnabled(true);
            optimizeButton.requestFocus();
        });
    }

    public void setPortfolioResults(int trades, double netProfit, double maxDrawdown, double optimalGrowth, double pi, double apd, double aveDuration) {
        portfolioSummaryTableModel.setResults(trades, netProfit, maxDrawdown, optimalGrowth, pi, apd, aveDuration);
    }

    public void update(Strategy strategy) {
        portfolioStrategiesTableModel.update(strategy);
    }

    public void clear() {
        portfolioStrategiesTableModel.clear();
    }

    private void assignListeners() {
        optimizeButton.addActionListener(e -> {
            try {
                prefs.set(PotfolioOptimizerWindowWidth, getSize().width);
                prefs.set(PotfolioOptimizerWindowHeight, getSize().height);
                prefs.set(DateRangeStart, fromDateEditor.getText());
                prefs.set(DateRangeEnd, toDateEditor.getText());
                prefs.set(UseDateRange, (Boolean.toString(useDateRangeCheckBox.isSelected())));
                prefs.set(ShowLegend, (Boolean.toString(showLegendCheck.isSelected())));
                prefs.set(UpperChartWeight, upperChartWeightCombo.getSelectedItem());
                prefs.set(ChartYaxisLocation, chartYaxisLocationCombo.getSelectedItem());

                List<String> includedNames = portfolioStrategiesTableModel.getIncludedStrategies();
                Dispatcher dispatcher = Dispatcher.getInstance();
                List<Strategy> strategies = includedNames.stream().map(dispatcher::getStrategy).collect(Collectors.toList());
                PorfolioBackTestRunner r = new PorfolioBackTestRunner(PortfolioBackTestDialog.this, strategies, portfolioChart);
                new Thread(r).start();
            } catch (Exception ex) {
                MessageDialog.showException(ex);
            }
        });

        useDateRangeCheckBox.addActionListener(e -> {
            boolean useDateRange = useDateRangeCheckBox.isSelected();
            fromDatePanel.setEnabled(useDateRange);
            toDatePanel.setEnabled(useDateRange);
        });


        cancelButton.addActionListener(e -> dispose());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }
        });


        showLegendCheck.addActionListener(e -> portfolioChart.setLegend(showLegendCheck.isSelected()));


        upperChartWeightCombo.addActionListener(e -> {
            String upperWeight = (String) upperChartWeightCombo.getSelectedItem();
            if (upperWeight != null) {
                portfolioChart.setWeight(Integer.parseInt(upperWeight));
            }
        });

        chartYaxisLocationCombo.addActionListener(e -> {
            String axisLocation = (String) chartYaxisLocationCombo.getSelectedItem();
            portfolioChart.setYaxisLocation(axisLocation);
        });

    }


    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Portfolio Backtest");

        getContentPane().setLayout(new BorderLayout());
        JPanel northPanel = new JPanel(new SpringLayout());

        // historical data range filter panel
        JPanel dateRangePanel = new JPanel(new SpringLayout());
        String dateFormat = "MMMMM d, yyyy";
        useDateRangeCheckBox = new JCheckBox("Use date range from:", prefs.get(UseDateRange).equals("true"));
        dateRangePanel.add(useDateRangeCheckBox);

        // From date
        fromDateEditor = new JTextFieldDateEditor();
        fromDatePanel = new JDateChooser(new Date(), dateFormat, fromDateEditor);
        fromDateEditor.setText(prefs.get(DateRangeStart));
        fromDatePanel.add(fromDateEditor);
        dateRangePanel.add(fromDatePanel);

        // To date
        JLabel toLabel = new JLabel("to:");
        toDateEditor = new JTextFieldDateEditor();
        toDatePanel = new JDateChooser(new Date(), dateFormat, toDateEditor);
        toDateEditor.setText(prefs.get(DateRangeEnd));
        toLabel.setLabelFor(toDatePanel);
        dateRangePanel.add(toLabel);
        toDatePanel.add(toDateEditor);

        boolean useDateRange = useDateRangeCheckBox.isSelected();
        fromDatePanel.setEnabled(useDateRange);
        toDatePanel.setEnabled(useDateRange);

        dateRangePanel.add(toDatePanel);
        SpringUtilities.makeOneLineGrid(dateRangePanel, 8);

        fromDateEditor.setForeground(Color.BLACK);
        toDateEditor.setForeground(Color.BLACK);


        // strategy parameters panel and its components
        JPanel strategyParamPanel = new JPanel(new BorderLayout());
        JScrollPane paramScrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        portfolioStrategiesTableModel = new PortfolioStrategiesTableModel(true);
        final JTable portfolioStrategiesTable = new JTable(portfolioStrategiesTableModel);
        TableColumnModel columnModel = portfolioStrategiesTable.getColumnModel();
        columnModel.getColumn(1).setPreferredWidth(175);


        ((JComponent) portfolioStrategiesTable.getDefaultRenderer(Boolean.class)).setOpaque(true);

        portfolioStrategiesTable.setRowSelectionAllowed(false);
        portfolioStrategiesTable.setColumnSelectionAllowed(false);


        portfolioSummaryTableModel = new PortfolioSummaryTableModel(false);
        final JTable footer = new JTable(portfolioSummaryTableModel);
        footer.setRowSelectionAllowed(false);
        paramScrollPane.getViewport().add(portfolioStrategiesTable);
        paramScrollPane.setPreferredSize(new Dimension(0, 157));

        JScrollPane paramScrollPane2 = new JScrollPane();
        paramScrollPane2.getViewport().add(footer);
        paramScrollPane2.setPreferredSize(new Dimension(0, 25));
        footer.setTableHeader(null);

        footer.setRowSelectionAllowed(false);
        footer.setColumnSelectionAllowed(false);


        portfolioStrategiesTable.getColumnModel().addColumnModelListener(
                new TableColumnModelListener() {

                    @Override
                    public void columnSelectionChanged(ListSelectionEvent e) {
                    }

                    @Override
                    public void columnRemoved(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnMoved(TableColumnModelEvent e) {
                    }

                    @Override
                    public void columnMarginChanged(ChangeEvent e) {
                        final TableColumnModel tableColumnModel = portfolioStrategiesTable.getColumnModel();
                        TableColumnModel footerColumnModel = footer.getColumnModel();
                        for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
                            int w = tableColumnModel.getColumn(i).getWidth();
                            footerColumnModel.getColumn(i).setMinWidth(w);
                            footerColumnModel.getColumn(i).setMaxWidth(w);
                            footerColumnModel.getColumn(i).setPreferredWidth(w);
                        }
                    }

                    @Override
                    public void columnAdded(TableColumnModelEvent e) {
                    }
                });


        strategyParamPanel.add(paramScrollPane, BorderLayout.CENTER);
        strategyParamPanel.add(paramScrollPane2, BorderLayout.SOUTH);
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JPanel(), BorderLayout.EAST);
        p.add(strategyParamPanel, BorderLayout.CENTER);


        // portfolio options
        JLabel upperChartWeightLabel = new JLabel("Upper chart weight:", SwingConstants.TRAILING);
        upperChartWeightCombo = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        upperChartWeightCombo.setSelectedItem(prefs.get(UpperChartWeight));
        upperChartWeightLabel.setLabelFor(upperChartWeightCombo);

        JLabel chartYaxisLocationLabel = new JLabel("Y-axis location:", SwingConstants.TRAILING);
        chartYaxisLocationCombo = new JComboBox<>(new String[]{"Left", "Right"});
        chartYaxisLocationCombo.setSelectedItem(prefs.get(ChartYaxisLocation));
        chartYaxisLocationLabel.setLabelFor(chartYaxisLocationCombo);

        showLegendCheck = new JCheckBox("Show legend");
        showLegendCheck.setSelected(Boolean.parseBoolean(prefs.get(ShowLegend)));


        JPanel chartOptionsPanel = new JPanel(new SpringLayout());

        chartOptionsPanel.add(chartYaxisLocationLabel);
        chartOptionsPanel.add(chartYaxisLocationCombo);
        chartOptionsPanel.add(upperChartWeightLabel);
        chartOptionsPanel.add(upperChartWeightCombo);
        chartOptionsPanel.add(showLegendCheck);
        SpringUtilities.makeOneLineGrid(chartOptionsPanel, 9);

        northPanel.add(new TitledSeparator(new JLabel("Historical date range")));
        northPanel.add(dateRangePanel);
        northPanel.add(new TitledSeparator(new JLabel("Portfolio strategies")));
        northPanel.add(p);
        northPanel.add(new TitledSeparator(new JLabel("Portfolio performance")));
        northPanel.add(chartOptionsPanel);
        SpringUtilities.makeCompactGrid(northPanel, 6, 1, 8, 12, 0, 8);


        portfolioChart = new PortfolioChart();
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);

        JPanel chartPanel = portfolioChart.getChart();
        JPanel centerPanel = new JPanel(new SpringLayout());
        centerPanel.add(chartPanel);
        centerPanel.add(progressBar);
        SpringUtilities.makeCompactGrid(centerPanel, 2, 1, 8, 0, 8, 0);

        optimizeButton = new JButton("Optimize");
        optimizeButton.setMnemonic('O');
        cancelButton = new JButton("Cancel");

        JPanel buttonsPanel = new JPanel(new FlowLayout());
        buttonsPanel.add(optimizeButton);
        buttonsPanel.add(cancelButton);
        progressBar.setVisible(false);

        getContentPane().add(northPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(optimizeButton);
        setMinimumSize(new Dimension(950, 500));

        int weight = Integer.parseInt((String) upperChartWeightCombo.getSelectedItem());
        portfolioChart.setWeight(weight);
        portfolioChart.setBackgound("White");
        portfolioChart.setBackgoundTransparency(25);
        String axisLocation = (String) chartYaxisLocationCombo.getSelectedItem();
        portfolioChart.setYaxisLocation(axisLocation);
    }

    public MarketSnapshotFilter getDateFilter() {
        if (useDateRangeCheckBox.isSelected()) {
            return new MarketSnapshotFilter(fromDateEditor, toDateEditor);
        }
        return null;
    }

}
