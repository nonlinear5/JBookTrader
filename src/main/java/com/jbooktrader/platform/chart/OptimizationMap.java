package com.jbooktrader.platform.chart;

import com.jbooktrader.platform.optimizer.*;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import com.jbooktrader.platform.util.ui.SpringUtilities;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.title.PaintScaleLegend;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.jbooktrader.platform.preferences.JBTPreferences.OptimizationMapHeight;
import static com.jbooktrader.platform.preferences.JBTPreferences.OptimizationMapWidth;


/**
 * Contour plot of optimization results
 *
 * @author Eugene Kononov
 */
public class OptimizationMap {
    private final PreferencesHolder prefs;
    private final PerformanceMetric performanceMetric;
    private final OptimizerDialog optimizerDialog;
    private final List<OptimizationResult> optimizationResults;
    private JFreeChart chart;
    private JComboBox<String> horizontalCombo, verticalCombo;
    private JComboBox colorMapCombo;
    private double min, max;
    private ChartPanel chartPanel;

    public OptimizationMap(OptimizerDialog optimizerDialog, List<OptimizationResult> optimizationResults) {
        prefs = PreferencesHolder.getInstance();
        this.optimizerDialog = optimizerDialog;
        this.optimizationResults = new ArrayList<>(optimizationResults);
        this.performanceMetric = optimizerDialog.getPerformanceMetric();
        chart = createChart();
    }

    public JDialog getChartFrame() {
        JDialog chartFrame = new JDialog(optimizerDialog);
        chartFrame.setTitle("Optimization Map - " + optimizerDialog.getStrategyName());
        chartFrame.setModal(true);

        JPanel northPanel = new JPanel(new SpringLayout());
        JPanel centerPanel = new JPanel(new SpringLayout());
        JPanel chartOptionsPanel = new JPanel(new SpringLayout());

        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        TitledBorder border = BorderFactory.createTitledBorder(etchedBorder, "Optimization Map Options");
        border.setTitlePosition(TitledBorder.TOP);
        chartOptionsPanel.setBorder(border);

        JLabel horizontalLabel = new JLabel("Horizontal:", SwingConstants.TRAILING);
        horizontalCombo = new JComboBox<>();
        horizontalLabel.setLabelFor(horizontalCombo);

        JLabel verticalLabel = new JLabel("Vertical:", SwingConstants.TRAILING);
        verticalCombo = new JComboBox<>();
        verticalLabel.setLabelFor(verticalCombo);

        StrategyParams params = optimizationResults.get(0).getParams();
        for (StrategyParam param : params.getAll()) {
            horizontalCombo.addItem(param.getName());
            verticalCombo.addItem(param.getName());
        }

        horizontalCombo.setSelectedIndex(0);
        verticalCombo.setSelectedIndex(1);

        JLabel colorMapLabel = new JLabel("Color map:", SwingConstants.TRAILING);
        colorMapCombo = new JComboBox<>(new String[]{"Heat", "Gray"});
        colorMapLabel.setLabelFor(colorMapCombo);

        chartOptionsPanel.add(horizontalLabel);
        chartOptionsPanel.add(horizontalCombo);
        chartOptionsPanel.add(verticalLabel);
        chartOptionsPanel.add(verticalCombo);
        chartOptionsPanel.add(colorMapLabel);
        chartOptionsPanel.add(colorMapCombo);

        SpringUtilities.makeOneLineGrid(chartOptionsPanel);
        northPanel.add(chartOptionsPanel);
        SpringUtilities.makeTopOneLineGrid(northPanel);

        chartPanel = new ChartPanel(chart);
        TitledBorder chartBorder = BorderFactory.createTitledBorder(etchedBorder, "Optimization Map");
        chartBorder.setTitlePosition(TitledBorder.TOP);
        chartPanel.setBorder(chartBorder);

        centerPanel.add(chartPanel);
        SpringUtilities.makeOneLineGrid(centerPanel);

        int chartWidth = prefs.getInt(OptimizationMapWidth);
        int chartHeight = prefs.getInt(OptimizationMapHeight);


        chartFrame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                prefs.set(OptimizationMapWidth, chartFrame.getWidth());
                prefs.set(OptimizationMapHeight, chartFrame.getHeight());
                chartFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            }
        });

        horizontalCombo.addActionListener(e -> repaint());

        verticalCombo.addActionListener(e -> repaint());

        colorMapCombo.addActionListener(e -> repaint());


        repaint();
        chartFrame.getContentPane().add(northPanel, BorderLayout.NORTH);
        chartFrame.getContentPane().add(centerPanel, BorderLayout.CENTER);
        chartFrame.getContentPane().setMinimumSize(new Dimension(720, 550));
        chartFrame.pack();
        chartFrame.setSize(chartWidth, chartHeight);
        chartFrame.setLocationRelativeTo(null);


        return chartFrame;
    }

    private void repaint() {
        chart = createChart();
        chartPanel.setChart(chart);
    }

    private double getMetric(OptimizationResult optimizationResult) {
        return optimizationResult.get(performanceMetric);
    }

    private XYZDataset createOptimizationDataset() {
        Map<String, Double> values = new HashMap<>();
        int xParameterIndex = (horizontalCombo == null) ? 0 : horizontalCombo.getSelectedIndex();
        int yParameterIndex = (verticalCombo == null) ? 1 : verticalCombo.getSelectedIndex();

        int index = 0;
        min = max = getMetric(optimizationResults.get(index));
        int size = optimizationResults.size();
        double[] x = new double[size];
        double[] y = new double[size];
        double[] z = new double[size];

        for (OptimizationResult optimizationResult : optimizationResults) {
            StrategyParams params = optimizationResult.getParams();

            x[index] = params.get(xParameterIndex).getValue();
            y[index] = params.get(yParameterIndex).getValue();
            z[index] = getMetric(optimizationResult);

            String key = x[index] + "," + y[index];
            Double value = values.get(key);

            if (value != null) {
                z[index] = Math.max(value, z[index]);
            }
            values.put(key, z[index]);

            min = Math.min(min, z[index]);
            max = Math.max(max, z[index]);
            index++;
        }

        DefaultXYZDataset dataset = new DefaultXYZDataset();
        dataset.addSeries("optimization", new double[][]{x, y, z});

        return dataset;
    }


    private OptimizationMapAnnotation createCentroid() {
        int xParameterIndex = (horizontalCombo == null) ? 0 : horizontalCombo.getSelectedIndex();
        int yParameterIndex = (verticalCombo == null) ? 1 : verticalCombo.getSelectedIndex();
        double[] centroid = optimizerDialog.getCentroid();
        double x = centroid[xParameterIndex];
        double y = centroid[yParameterIndex];
        return new OptimizationMapAnnotation(13, x, y, "CM");
    }


    private OptimizationMapAnnotation createTopResult() {
        ResultComparator resultComparator = new ResultComparator(performanceMetric);
        optimizationResults.sort(resultComparator);

        StrategyParams params = optimizationResults.get(0).getParams();
        double[] topResult = new double[params.size()];

        for (int paramIndex = 0; paramIndex < params.size(); paramIndex++) {
            int paramValue = params.get(paramIndex).getValue();
            topResult[paramIndex] += paramValue;
        }

        int xParameterIndex = (horizontalCombo == null) ? 0 : horizontalCombo.getSelectedIndex();
        int yParameterIndex = (verticalCombo == null) ? 1 : verticalCombo.getSelectedIndex();
        double x = topResult[xParameterIndex];
        double y = topResult[yParameterIndex];
        return new OptimizationMapAnnotation(13, x, y, "TR");
    }


    private JFreeChart createChart() {
        XYZDataset dataset = createOptimizationDataset();

        NumberAxis xAxis = new NumberAxis();
        xAxis.setAutoRangeIncludesZero(false);
        xAxis.setLowerMargin(0);
        xAxis.setUpperMargin(0);
        NumberAxis yAxis = new NumberAxis();
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0);
        yAxis.setUpperMargin(0);

        xAxis.setLabel(horizontalCombo == null ? null : (String) horizontalCombo.getSelectedItem());
        yAxis.setLabel(verticalCombo == null ? null : (String) verticalCombo.getSelectedItem());


        XYBlockRenderer renderer = new XYBlockRenderer();
        int paintScaleIndex = (colorMapCombo == null) ? 0 : colorMapCombo.getSelectedIndex();
        PaintScale paintScale = (paintScaleIndex == 0) ? new HeatPaintScale() : new GrayPaintScale();
        renderer.setPaintScale(paintScale);

        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);

        OptimizationMapAnnotation topResultAnnotation = createTopResult();
        plot.addAnnotation(topResultAnnotation);
        if (optimizerDialog.getCentroid() != null) {
            OptimizationMapAnnotation centroidAnnotation = createCentroid();
            plot.addAnnotation(centroidAnnotation);
        }

        chart = new JFreeChart(plot);
        chart.removeLegend();
        chart.getPlot().setOutlineStroke(new BasicStroke(1.0f));

        NumberAxis scaleAxis = new NumberAxis(performanceMetric.getName());
        PaintScaleLegend legend = new PaintScaleLegend(paintScale, scaleAxis);
        legend.setFrame(new BlockBorder(Color.GRAY));
        legend.setPadding(new RectangleInsets(5, 5, 5, 5));
        legend.setMargin(new RectangleInsets(4, 6, 40, 6));
        legend.setPosition(RectangleEdge.RIGHT);
        chart.addSubtitle(legend);


        return chart;
    }

    private abstract class PaintScaleAdapter implements PaintScale {
        public double getUpperBound() {
            return max;
        }

        public double getLowerBound() {
            return (min == max) ? min * 0.99 : min;
        }
    }

    private class HeatPaintScale extends PaintScaleAdapter {
        public Paint getPaint(double value) {
            double normalizedValue = (value - min) / (max - min);
            double saturation = Math.max(0.1, Math.abs(2 * normalizedValue - 1));
            double red = 0;
            double blue = 0.7;
            double hue = blue - normalizedValue * (blue - red);
            return Color.getHSBColor((float) hue, (float) saturation, 1);
        }
    }

    private class GrayPaintScale extends PaintScaleAdapter {
        public Paint getPaint(double value) {
            double normalizedValue = value - min;
            double clrs = 255.0 / (max - min);
            int color = (int) (255 - normalizedValue * clrs);
            return new Color(color, color, color, 255);
        }
    }
}
