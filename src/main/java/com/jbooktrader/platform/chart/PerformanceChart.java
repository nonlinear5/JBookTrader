package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.indicator.Indicator;
import com.jbooktrader.platform.position.Position;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.strategy.Strategy;
import com.jbooktrader.platform.util.ui.SpringUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.RefineryUtilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import static com.jbooktrader.platform.preferences.JBTPreferences.PerformanceChartHeight;
import static com.jbooktrader.platform.preferences.JBTPreferences.PerformanceChartWidth;


/**
 * Multi-plot strategy performance chart which combines price,
 * indicators, executions, and net profit.
 *
 * @author Eugene Kononov
 */
public class PerformanceChart {
    private static final int PRICE_PLOT_WEIGHT = 3;
    private static final Paint BACKGROUND_COLOR = new GradientPaint(0, 0, new Color(0, 0, 176), 0, 0, Color.BLACK);

    private final Strategy strategy;
    private final List<CircledTextAnnotation> annotations = new ArrayList<>();
    private final PreferencesHolder prefs;
    private final List<XYPlot> indicatorPlots;
    private final PerformanceChartData performanceChartData;

    private JFreeChart chart;
    private JFrame chartFrame;
    private CombinedDomainXYPlot combinedPlot;
    private DateAxis dateAxis;
    private XYPlot pricePlot, pnlPlot;
    private JComboBox<String> timeLineCombo, timeZoneCombo;
    private JCheckBox indicatorVisibilityCheck, tradesVisibilityCheck, pnlVisibilityCheck;

    public PerformanceChart(JFrame parent, Strategy strategy) {
        indicatorPlots = new ArrayList<>();
        performanceChartData = strategy.getPerformanceManager().getPerformanceChartData();
        prefs = PreferencesHolder.getInstance();
        this.strategy = strategy;
        createChartFrame(parent);
        registerListeners();
    }

    private void setTimeline() {
        int timeLineType = timeLineCombo.getSelectedIndex();
        MarketTimeLine mtl = new MarketTimeLine(strategy);
        SegmentedTimeline segmentedTimeline = (timeLineType == 0) ? mtl.getAllHours() : mtl.getNormalHours();
        dateAxis.setTimeline(segmentedTimeline);
    }

    private void setTimeZone() {
        int timeZoneType = timeZoneCombo.getSelectedIndex();
        TimeZone tz = (timeZoneType == 0) ? strategy.getTradingSchedule().getTimeZone() : TimeZone.getDefault();
        dateAxis.setTimeZone(tz);
    }

    private void registerListeners() {
        timeLineCombo.addActionListener(e -> setTimeline());

        timeZoneCombo.addActionListener(e -> setTimeZone());

        indicatorVisibilityCheck.addActionListener(e -> {
            if (indicatorVisibilityCheck.isSelected()) {
                if (pnlVisibilityCheck.isSelected()) {
                    combinedPlot.remove(pnlPlot);
                }
                indicatorPlots.forEach(combinedPlot::add);
                if (pnlVisibilityCheck.isSelected()) {
                    combinedPlot.add(pnlPlot);
                }
            } else {
                indicatorPlots.forEach(combinedPlot::remove);
            }
        });

        pnlVisibilityCheck.addActionListener(e -> {
            if (pnlVisibilityCheck.isSelected()) {
                combinedPlot.add(pnlPlot);
            } else {
                combinedPlot.remove(pnlPlot);
            }
        });

        tradesVisibilityCheck.addActionListener(e -> {
            boolean show = tradesVisibilityCheck.isSelected();
            for (CircledTextAnnotation annotation : annotations) {
                if (show) {
                    pricePlot.addAnnotation(annotation);
                } else {
                    pricePlot.removeAnnotation(annotation);
                }
            }
        });

        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                prefs.set(PerformanceChartWidth, chartFrame.getWidth());
                prefs.set(PerformanceChartHeight, chartFrame.getHeight());
                chartFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            }
        });

    }


    private void createChartFrame(JFrame parent) {
        chartFrame = new JFrame("Strategy Performance Chart - " + strategy);
        chartFrame.setIconImage(parent.getIconImage());

        JPanel chartOptionsPanel = new JPanel(new SpringLayout());
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder chartOptionsBorder = BorderFactory.createTitledBorder(etchedBorder, "Chart Options");
        chartOptionsBorder.setTitlePosition(TitledBorder.TOP);
        chartOptionsPanel.setBorder(chartOptionsBorder);

        JLabel timeLineLabel = new JLabel("Timeline:", SwingConstants.TRAILING);
        timeLineCombo = new JComboBox<>(new String[]{"All Hours", "Trading Hours"});
        timeLineLabel.setLabelFor(timeLineCombo);

        JLabel timeZoneLabel = new JLabel("Time Zone:", SwingConstants.TRAILING);
        timeZoneCombo = new JComboBox<>(new String[]{"Exchange", "Local"});
        timeZoneLabel.setLabelFor(timeZoneCombo);

        JLabel visibilityLabel = new JLabel("Show:");
        indicatorVisibilityCheck = new JCheckBox("Indicators", true);
        tradesVisibilityCheck = new JCheckBox("Trades", true);
        pnlVisibilityCheck = new JCheckBox("Net Profit", true);

        chartOptionsPanel.add(timeLineLabel);
        chartOptionsPanel.add(timeLineCombo);
        chartOptionsPanel.add(timeZoneLabel);
        chartOptionsPanel.add(timeZoneCombo);
        chartOptionsPanel.add(visibilityLabel);
        chartOptionsPanel.add(tradesVisibilityCheck);
        chartOptionsPanel.add(indicatorVisibilityCheck);
        chartOptionsPanel.add(pnlVisibilityCheck);

        SpringUtilities.makeOneLineGrid(chartOptionsPanel);
        JPanel northPanel = new JPanel(new SpringLayout());
        northPanel.add(chartOptionsPanel);
        SpringUtilities.makeTopOneLineGrid(northPanel);


        JPanel centerPanel = new JPanel(new SpringLayout());

        JPanel chartPanel = new JPanel(new BorderLayout());
        TitledBorder chartBorder = BorderFactory.createTitledBorder(etchedBorder, "Performance Chart");
        chartBorder.setTitlePosition(TitledBorder.TOP);
        chartPanel.setBorder(chartBorder);

        JPanel scrollBarPanel = new JPanel(new BorderLayout());
        createChart();
        DateScrollBar dateScrollBar = new DateScrollBar(combinedPlot);
        scrollBarPanel.add(dateScrollBar);

        ChartMonitor chartMonitor = new ChartMonitor(chart);

        chartMonitor.setRangeZoomable(false);

        chartPanel.add(chartMonitor, BorderLayout.CENTER);
        chartPanel.add(scrollBarPanel, BorderLayout.SOUTH);

        centerPanel.add(chartPanel);
        SpringUtilities.makeOneLineGrid(centerPanel);


        Container contentPane = chartFrame.getContentPane();
        contentPane.add(northPanel, BorderLayout.NORTH);
        contentPane.add(centerPanel, BorderLayout.CENTER);
        chartFrame.pack();
        int chartWidth = prefs.getInt(PerformanceChartWidth);
        int chartHeight = prefs.getInt(PerformanceChartHeight);
        chartFrame.setSize(chartWidth, chartHeight);
        RefineryUtilities.centerFrameOnScreen(chartFrame);
    }


    private void createChart() {
        CandlestickRenderer candleRenderer = new CandlestickRenderer(3);
        candleRenderer.setDrawVolume(false);
        candleRenderer.setAutoWidthMethod(CandlestickRenderer.WIDTHMETHOD_AVERAGE);
        candleRenderer.setUpPaint(Color.GREEN);
        candleRenderer.setDownPaint(Color.RED);
        candleRenderer.setSeriesPaint(0, new Color(250, 240, 150));
        candleRenderer.setBaseStroke(new BasicStroke(1));

        dateAxis = new DateAxis();

        // parent plot
        combinedPlot = new CombinedDomainXYPlot(dateAxis);
        combinedPlot.setGap(10.0);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        // price plot
        OHLCDataset priceDataset = performanceChartData.getPriceDataset();
        NumberAxis priceAxis = new NumberAxis("Price");
        priceAxis.setAutoRangeIncludesZero(false);
        pricePlot = new XYPlot(priceDataset, dateAxis, priceAxis, candleRenderer);
        pricePlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pricePlot, PRICE_PLOT_WEIGHT);

        // indicator plots
        for (Indicator indicator : strategy.getIndicatorManager().getIndicators()) {
            NumberAxis indicatorAxis = new NumberAxis(indicator.getKey());
            indicatorAxis.setLabelFont(new Font("Arial Narrow", Font.PLAIN, 11));
            OHLCDataset ds = performanceChartData.getIndicatorDataset(indicator);
            XYPlot indicatorPlot = new XYPlot(ds, dateAxis, indicatorAxis, candleRenderer);
            indicatorPlot.setBackgroundPaint(BACKGROUND_COLOR);
            combinedPlot.add(indicatorPlot);
            indicatorPlots.add(indicatorPlot);
        }

        // positions plot
        for (Position position : strategy.getPositionManager().getPositionsHistory()) {
            long time = position.getTime();
            double aveFill = position.getAvgFillPrice();
            int quantity = position.getPosition();
            CircledTextAnnotation trade = new CircledTextAnnotation(quantity, time, aveFill);
            pricePlot.addAnnotation(trade);
            annotations.add(trade);
        }

        // Net profit plot
        OHLCDataset netProfitDataset = performanceChartData.getStrategyNetProfitDataset();
        NumberAxis pnlAxis = new NumberAxis("Net Profit");
        pnlAxis.setAutoRangeIncludesZero(false);
        pnlPlot = new XYPlot(netProfitDataset, dateAxis, pnlAxis, candleRenderer);
        pnlPlot.setBackgroundPaint(BACKGROUND_COLOR);
        combinedPlot.add(pnlPlot);

        combinedPlot.setDomainAxis(dateAxis);
        dateAxis.setLowerMargin(0.01);
        dateAxis.setUpperMargin(0.01);

        setTimeline();
        setTimeZone();

        // Finally, create the chart
        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, false);
    }

    public JFrame getChart() {
        return chartFrame;
    }

}
