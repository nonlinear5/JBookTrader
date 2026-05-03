package com.jbooktrader.platform.optimizer;


import com.jbooktrader.platform.backtest.ProgressListener;
import com.jbooktrader.platform.chart.OptimizationMap;
import com.jbooktrader.platform.dialog.JBTDialog;
import com.jbooktrader.platform.marketbook.MarketSnapshotFilter;
import com.jbooktrader.platform.model.Dispatcher;
import com.jbooktrader.platform.performance.KernelEvaluator;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ui.MessageDialog;
import com.jbooktrader.platform.util.ui.SpringUtilities;
import com.jbooktrader.platform.util.ui.TitledSeparator;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Date;
import java.util.List;

import static com.jbooktrader.platform.optimizer.PerformanceMetric.*;
import static com.jbooktrader.platform.preferences.JBTPreferences.*;

/**
 * Dialog to specify options for back testing using a historical data file.
 *
 * @author Eugene Kononov
 */
public class OptimizerDialog extends JBTDialog implements ProgressListener {
    private final PreferencesHolder prefs;
    private final String strategyName;
    private JPanel progressPanel;
    private JButton cancelButton, optimizeButton, optimizationMapButton, closeButton, selectFileButton;
    private JTextField fileNameText, minTradesText;
    private JComboBox<String> performanceMetricCombo, inclusionCriteriaCombo, optimizationMethodCombo, kernelCombo, parameterBoundsCombo;
    private JTextFieldDateEditor fromDateEditor, toDateEditor;
    private JCheckBox useDateRangeCheckBox;
    private JPanel fromDatePanel, toDatePanel;
    private JProgressBar progressBar;
    private JTable resultsTable;
    private TableColumnModel paramTableColumnModel;
    private TableColumn stepColumn;

    private ParamTableModel paramTableModel;
    private Strategy strategy;
    private List<OptimizationResult> optimizationResults;
    private double[] centroid;
    private OptimizerRunner optimizerRunner;

    public OptimizerDialog(JFrame parent, String strategyName) {
        super(parent);
        prefs = PreferencesHolder.getInstance();
        this.strategyName = strategyName;
        init();
        assignListeners();
        initParams();
    }

    public double[] getCentroid() {
        return centroid;
    }

    public void setCentroid(double[] centroid) {
        this.centroid = centroid;
    }

    @Override
    public void setProgress(long count, long iterations, String text) {
        SwingUtilities.invokeLater(() -> {
            if (iterations != 0) {
                int percent = (int) (100 * count / iterations);
                progressBar.setValue(percent);
                progressBar.setString(text);
            }
        });
    }

    @Override
    public void setProgress(String progressText) {
        SwingUtilities.invokeLater(() -> {
            progressBar.setValue(0);
            progressBar.setString(progressText);
        });
    }

    @Override
    public boolean isCancelled() {
        return optimizerRunner == null || optimizerRunner.isCancelled();
    }

    public void enableProgress() {
        progressBar.setValue(0);
        progressPanel.setVisible(true);
        optimizeButton.setEnabled(false);
        cancelButton.setEnabled(true);
        getRootPane().setDefaultButton(cancelButton);
    }

    public void signalCompleted() {
        SwingUtilities.invokeLater(() -> {
            progressPanel.setVisible(false);
            optimizeButton.setEnabled(true);
            cancelButton.setEnabled(false);
            getRootPane().setDefaultButton(optimizationMapButton);
        });
    }

    private void setOptions() {
        String historicalFileName = fileNameText.getText();

        File file = new File(historicalFileName);
        if (!file.exists()) {
            fileNameText.requestFocus();
            String msg = "Historical file " + "\"" + historicalFileName + "\"" + " does not exist.";
            throw new RuntimeException(msg);
        }

        try {
            int minTrades = Integer.parseInt(minTradesText.getText());
            if (minTrades < 2) {
                minTradesText.requestFocus();
                throw new RuntimeException("\"" + "Minimum trades" + "\"" + " must be greater or equal to 2.");
            }
        } catch (NumberFormatException nfe) {
            minTradesText.requestFocus();
            throw new RuntimeException("\"" + "Minimum trades" + "\"" + " must be an integer.");
        }
    }

    private void setParamTableColumns() {
        int optimizationMethod = optimizationMethodCombo.getSelectedIndex();
        int columnCount = paramTableColumnModel.getColumnCount();
        if (optimizationMethod == 0) {
            if (columnCount == 3) {
                paramTableColumnModel.addColumn(stepColumn);
            }
        } else {
            if (columnCount == 4) {
                paramTableColumnModel.removeColumn(stepColumn);
            }
        }
    }

    private void assignListeners() {
        optimizeButton.addActionListener(e -> {
            try {
                prefs.set(OptimizerWindowWidth, getSize().width);
                prefs.set(OptimizerWindowHeight, getSize().height);
                prefs.set(DataFileName, fileNameText.getText());
                prefs.set(OptimizerMinTrades, minTradesText.getText());
                prefs.set(OptimizerPerformanceMetric, performanceMetricCombo.getSelectedItem());
                prefs.set(Kernel, kernelCombo.getSelectedItem());
                prefs.set(InclusionCriteria, inclusionCriteriaCombo.getSelectedItem());
                prefs.set(OptimizerMethod, optimizationMethodCombo.getSelectedItem());
                prefs.set(DateRangeStart, fromDateEditor.getText());
                prefs.set(DateRangeEnd, toDateEditor.getText());
                prefs.set(UseDateRange, (Boolean.toString(useDateRangeCheckBox.isSelected())));
                prefs.set(ParameterBounds, (parameterBoundsCombo.getSelectedItem()));

                setOptions();
                StrategyParams params = paramTableModel.getParams();

                int optimizationMethod = optimizationMethodCombo.getSelectedIndex();
                if (optimizationMethod == 0) {
                    optimizerRunner = new BruteForceOptimizerRunner(OptimizerDialog.this, strategy, params);
                } else if (optimizationMethod == 1) {
                    optimizerRunner = new DivideAndConquerOptimizerRunner(OptimizerDialog.this, strategy, params);
                } else if (optimizationMethod == 2) {
                    optimizerRunner = new CentroidOptimizerRunner(OptimizerDialog.this, strategy, params);
                } else if (optimizationMethod == 3) {
                    optimizerRunner = new GradientOptimizerRunner(OptimizerDialog.this, strategy, params);
                }


                new Thread(optimizerRunner).start();
            } catch (Exception ex) {
                MessageDialog.showException(ex);
            }
        });

        useDateRangeCheckBox.addActionListener(e -> {
            boolean useDateRange = useDateRangeCheckBox.isSelected();
            fromDatePanel.setEnabled(useDateRange);
            toDatePanel.setEnabled(useDateRange);
        });

        optimizationMapButton.addActionListener(e -> {
            try {
                if (optimizationResults == null || optimizationResults.isEmpty()) {
                    MessageDialog.showMessage("There are no optimization results to map.");
                    return;
                }
                setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                OptimizationMap optimizationMap = new OptimizationMap(OptimizerDialog.this, optimizationResults);
                JDialog chartFrame = optimizationMap.getChartFrame();
                chartFrame.setVisible(true);
            } catch (Exception ex) {
                MessageDialog.showException(ex);
            } finally {
                setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            }
        });

        optimizationMethodCombo.addActionListener(e -> setParamTableColumns());


        closeButton.addActionListener(e -> {
            if (optimizerRunner != null) {
                closeButton.setEnabled(false);
                optimizerRunner.cancel();
            }
            dispose();
        });

        cancelButton.addActionListener(e -> {
            if (optimizerRunner != null) {
                cancelButton.setEnabled(false);
                optimizerRunner.cancel();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (optimizerRunner != null) {
                    optimizerRunner.cancel();
                }
                dispose();
            }
        });

        selectFileButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser(Dispatcher.getInstance().getMarketDataDir());
            fileChooser.setDialogTitle("Select Historical Data File");

            String filename = getFileName();
            if (!filename.isEmpty()) {
                fileChooser.setSelectedFile(new File(filename));
            }

            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                fileNameText.setText(file.getAbsolutePath());
            }
        });
    }


    private void init() {
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setTitle("Strategy Optimizer - " + strategyName);

        getContentPane().setLayout(new BorderLayout());

        JPanel northPanel = new JPanel(new SpringLayout());
        JPanel centerPanel = new JPanel(new SpringLayout());
        JPanel southPanel = new JPanel(new BorderLayout());

        // strategy panel and its components
        JPanel filenamePanel = new JPanel(new SpringLayout());

        JLabel fileNameLabel = new JLabel("Data file:", SwingConstants.TRAILING);
        fileNameText = new JTextField();
        fileNameText.setText(prefs.get(DataFileName));
        selectFileButton = new JButton("Browse...");

        fileNameLabel.setLabelFor(fileNameText);

        filenamePanel.add(fileNameLabel);
        filenamePanel.add(fileNameText);
        filenamePanel.add(selectFileButton);
        SpringUtilities.makeOneLineGrid(filenamePanel, 8);

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
        // end of historical data range filter panel

        // strategy parameters panel and its components
        JPanel strategyParamPanel = new JPanel(new SpringLayout());
        JScrollPane paramScrollPane = new JScrollPane();
        paramTableModel = new ParamTableModel();
        JTable paramTable = new JTable(paramTableModel);
        paramTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultTableCellRenderer renderer = (DefaultTableCellRenderer) paramTable.getDefaultRenderer(String.class);
        renderer.setHorizontalAlignment(SwingConstants.LEFT);
        ((JLabel) paramTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        paramTableColumnModel = paramTable.getColumnModel();
        stepColumn = paramTableColumnModel.getColumn(3);


        paramScrollPane.getViewport().add(paramTable);
        paramScrollPane.setPreferredSize(new Dimension(0, 135));
        strategyParamPanel.add(paramScrollPane);
        SpringUtilities.makeOneLineGrid(strategyParamPanel, 8);

        // optimization options panel and its components
        JPanel optimizationOptionsPanel = new JPanel(new SpringLayout());

        JLabel optimizationMethodLabel = new JLabel("Search method:");
        optimizationMethodCombo = new JComboBox<>(new String[]{"Brute force", "Divide & Conquer", "Centroid", "Gradient"});
        String optimizerMethod = prefs.get(OptimizerMethod);
        optimizationMethodCombo.setSelectedItem(optimizerMethod);
        optimizationMethodLabel.setLabelFor(optimizationMethodCombo);
        optimizationOptionsPanel.add(optimizationMethodLabel);
        optimizationOptionsPanel.add(optimizationMethodCombo);

        JLabel parameterBoundsLabel = new JLabel("Parameter bounds:");
        parameterBoundsCombo = new JComboBox<>(new String[]{"Lenient", "Strict"});
        parameterBoundsCombo.setSelectedItem(prefs.get(ParameterBounds));
        parameterBoundsLabel.setLabelFor(parameterBoundsCombo);
        optimizationOptionsPanel.add(parameterBoundsLabel);
        optimizationOptionsPanel.add(parameterBoundsCombo);

        JLabel performanceMetricLabel = new JLabel("Performance metric:");
        String[] performanceMetric = new String[]{NetProfit.getName(), OG.getName(), PI.getName(), APD.getName()};
        performanceMetricCombo = new JComboBox<>(performanceMetric);
        performanceMetricCombo.setSelectedItem(prefs.get(OptimizerPerformanceMetric));
        performanceMetricLabel.setLabelFor(performanceMetricCombo);
        optimizationOptionsPanel.add(performanceMetricLabel);
        optimizationOptionsPanel.add(performanceMetricCombo);


        JLabel kernelLabel = new JLabel("Kernel:");
        kernelCombo = new JComboBox<>();
        for (KernelEvaluator.KernelType kernelType : KernelEvaluator.KernelType.values()) {
            kernelCombo.addItem(kernelType.name());
        }
        kernelCombo.setSelectedItem(prefs.get(Kernel));
        kernelLabel.setLabelFor(kernelCombo);
        optimizationOptionsPanel.add(kernelLabel);
        optimizationOptionsPanel.add(kernelCombo);


        JLabel inclusionCriteriaLabel = new JLabel("Inclusion criteria:");
        inclusionCriteriaCombo = new JComboBox<>(new String[]{"All strategies", "Profitable strategies"});
        inclusionCriteriaLabel.setLabelFor(inclusionCriteriaCombo);
        optimizationOptionsPanel.add(inclusionCriteriaLabel);
        optimizationOptionsPanel.add(inclusionCriteriaCombo);
        inclusionCriteriaCombo.setSelectedItem(prefs.get(InclusionCriteria));


        JLabel minTradesLabel = new JLabel("Min trades:");
        minTradesText = new JTextField();

        minTradesText.setText(prefs.get(OptimizerMinTrades));
        minTradesLabel.setLabelFor(minTradesText);
        optimizationOptionsPanel.add(minTradesLabel);
        optimizationOptionsPanel.add(minTradesText);

        JButton advancedOptionsButton = new JButton("Advanced...");
        optimizationOptionsPanel.add(advancedOptionsButton);
        advancedOptionsButton.addActionListener(e -> new AdvancedOptimizationOptionsDialog((JFrame) getParent()));


        SpringUtilities.makeOneLineGrid(optimizationOptionsPanel, 8);

        northPanel.add(new TitledSeparator(new JLabel("Historical data")));
        northPanel.add(filenamePanel);
        northPanel.add(dateRangePanel);
        northPanel.add(new TitledSeparator(new JLabel("Strategy parameters")));
        northPanel.add(strategyParamPanel);
        northPanel.add(new TitledSeparator(new JLabel("Optimization options")));
        northPanel.add(optimizationOptionsPanel);
        northPanel.add(new TitledSeparator(new JLabel("Optimization results")));
        SpringUtilities.makeCompactGrid(northPanel, 8, 1, 8, 12, 0, 8);

        JScrollPane resultsScrollPane = new JScrollPane();
        centerPanel.add(resultsScrollPane);
        SpringUtilities.makeCompactGrid(centerPanel, 1, 1, 7, 0, 8, 0);

        resultsTable = new JTable();
        resultsTable.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setShowGrid(false);
        DefaultTableCellRenderer resultsTableRenderer = (DefaultTableCellRenderer) resultsTable.getDefaultRenderer(String.class);
        resultsTableRenderer.setHorizontalAlignment(SwingConstants.LEFT);
        ((JLabel) resultsTable.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(JLabel.LEFT);

        resultsScrollPane.getViewport().add(resultsTable);

        progressBar = new JProgressBar();
        Font font = centerPanel.getFont().deriveFont(Font.BOLD);
        progressBar.setFont(font);
        progressBar.setStringPainted(true);

        optimizeButton = new JButton("Optimize");
        optimizeButton.setMnemonic('O');

        optimizationMapButton = new JButton("Optimization Map");
        optimizationMapButton.setMnemonic('M');


        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);

        closeButton = new JButton("Close");

        FlowLayout flowLayout = new FlowLayout(FlowLayout.CENTER, 5, 12);
        JPanel buttonsPanel = new JPanel(flowLayout);
        buttonsPanel.add(optimizeButton);
        buttonsPanel.add(optimizationMapButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.add(closeButton);

        progressPanel = new JPanel(new SpringLayout());
        progressPanel.add(progressBar);
        progressPanel.setVisible(false);
        SpringUtilities.makeCompactGrid(progressPanel, 1, 1, 8, 8, 8, 0);

        southPanel.add(progressPanel, BorderLayout.NORTH);
        southPanel.add(buttonsPanel, BorderLayout.SOUTH);

        getContentPane().add(northPanel, BorderLayout.NORTH);
        getContentPane().add(centerPanel, BorderLayout.CENTER);
        getContentPane().add(southPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(optimizeButton);
        setMinimumSize(new Dimension(950, 500));

        pack();
        int width = prefs.getInt(OptimizerWindowWidth);
        int height = prefs.getInt(OptimizerWindowHeight);
        setSize(width, height);
        setLocationRelativeTo(null);
    }

    private void initParams() {
        try {
            strategy = Dispatcher.getInstance().getStrategy(strategyName);
            paramTableModel.setParams(strategy.getParams());
            setParamTableColumns();
            ResultsTableModel model = new ResultsTableModel(strategy);
            resultsTable.setModel(model);
            resultsTable.setRowSorter(new TableRowSorter<>(model));
        } catch (Exception e) {
            MessageDialog.showException(e);
        }
    }

    void setResults(List<OptimizationResult> optimizationResults) {
        this.optimizationResults = optimizationResults;
        ((ResultsTableModel) resultsTable.getModel()).setResults(optimizationResults);
    }


    void showMessage(final String msg) {
        SwingUtilities.invokeLater(() -> MessageDialog.showMessage(msg));
    }

    String getFileName() {
        return fileNameText.getText();
    }

    int getMinTrades() {
        return Integer.parseInt(minTradesText.getText());
    }

    public String getStrategyName() {
        return strategyName;
    }

    String getInclusionCriteria() {
        return (String) inclusionCriteriaCombo.getSelectedItem();
    }

    public String getParameterBounds() {
        return (String) parameterBoundsCombo.getSelectedItem();
    }

    public PerformanceMetric getPerformanceMetric() {
        String selectedItem = (String) performanceMetricCombo.getSelectedItem();
        return PerformanceMetric.getColumn(selectedItem);
    }

    MarketSnapshotFilter getDateFilter() {
        MarketSnapshotFilter filter = null;
        if (useDateRangeCheckBox.isSelected()) {
            filter = new MarketSnapshotFilter(fromDateEditor, toDateEditor);
        }
        return filter;
    }
}
