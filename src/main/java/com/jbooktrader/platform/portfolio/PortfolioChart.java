package com.jbooktrader.platform.portfolio;

import com.jbooktrader.platform.chart.ChartMonitor;
import com.jbooktrader.platform.chart.DateScrollBar;
import com.jbooktrader.platform.preferences.JBTPreferences;
import com.jbooktrader.platform.preferences.PreferencesHolder;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.util.TimeZone;


/**
 * Multi-plot strategy performance chart which combines price,
 * indicators, executions, and net profit.
 *
 * @author Eugene Kononov
 */
public class PortfolioChart {
    private JPanel chartPanel;
    private TimeSeriesCollection profitAndLossCollection, portfolioProfitAndLossCollection;
    private JFreeChart chart;
    private CombinedDomainXYPlot combinedPlot;
    private DateAxis dateAxis;
    private XYPlot pnlPlot, portfolioPlot;
    private LegendTitle legendTitle;


    public PortfolioChart() {
        createChartFrame();
    }

    private void setTimeline() {
        dateAxis.setTimeline(new SegmentedTimeline(SegmentedTimeline.DAY_SEGMENT_SIZE, 7, 0));
    }

    private void setTimeZone() {
        dateAxis.setTimeZone(TimeZone.getDefault());
    }

    private void createChartFrame() {
        Border etchedBorder = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        chartPanel = new JPanel(new BorderLayout());

        TitledBorder chartBorder = BorderFactory.createTitledBorder(etchedBorder, "");
        chartBorder.setTitlePosition(TitledBorder.TOP);
        chartPanel.setBorder(chartBorder);

        createChart();
        new DateScrollBar(combinedPlot);
        ChartMonitor chartMonitor = new ChartMonitor(chart);
        chartMonitor.setRangeZoomable(false);
        chartPanel.add(chartMonitor, BorderLayout.CENTER);
    }

    public void update(TimeSeries profitAndLoss) {
        profitAndLossCollection.addSeries(profitAndLoss);
    }

    public void updatePortfolio(TimeSeries profitAndLoss) {
        portfolioProfitAndLossCollection.addSeries(profitAndLoss);
    }

    public void clear() {
        profitAndLossCollection.removeAllSeries();
        portfolioProfitAndLossCollection.removeAllSeries();
    }

    public void enableNotifications() {
        chart.setNotify(true);
    }

    public void disableNotifications() {
        chart.setNotify(false);
    }


    public void setWeight(int weight) {
        pnlPlot.setWeight(weight);
    }

    public void setBackgound(String colorOption) {
        Color color = null;
        if (colorOption.equalsIgnoreCase("green")) {
            color = Color.GREEN;
        } else if (colorOption.equalsIgnoreCase("blue")) {
            color = Color.BLUE;
        } else if (colorOption.equalsIgnoreCase("white")) {
            color = Color.WHITE;
        }
        pnlPlot.setBackgroundImage(null);
        portfolioPlot.setBackgroundImage(null);
        pnlPlot.setBackgroundPaint(color);
        portfolioPlot.setBackgroundPaint(color);
    }

    public void setBackgoundTransparency(int percent) {
        float alpha = percent / 100f;
        pnlPlot.setBackgroundAlpha(alpha);
        portfolioPlot.setBackgroundAlpha(alpha);
    }

    public void setYaxisLocation(String axisLocation) {
        AxisLocation al;
        if (axisLocation.equalsIgnoreCase("left")) {
            al = AxisLocation.TOP_OR_LEFT;
        } else if (axisLocation.equalsIgnoreCase("right")) {
            al = AxisLocation.TOP_OR_RIGHT;
        } else {
            throw new RuntimeException("Axis location option " + axisLocation + " is not recognized.");
        }

        pnlPlot.setRangeAxisLocation(al);
        portfolioPlot.setRangeAxisLocation(al);
    }

    public void setLegend(boolean showLegend) {
        if (showLegend) {
            chart.addLegend(legendTitle);
            legendTitle.setPosition(RectangleEdge.TOP);
            legendTitle.setBackgroundPaint(chartPanel.getBackground());
            legendTitle.setFrame(BlockBorder.NONE);
        } else {
            chart.removeLegend();
        }
    }

    private void createChart() {
        dateAxis = new DateAxis();
        combinedPlot = new CombinedDomainXYPlot(dateAxis);
        combinedPlot.setGap(5);
        combinedPlot.setOrientation(PlotOrientation.VERTICAL);

        profitAndLossCollection = new TimeSeriesCollection();
        portfolioProfitAndLossCollection = new TimeSeriesCollection();
        NumberAxis pnlAxis = new NumberAxis("Net profit (strategies)");
        NumberAxis portfolioAxis = new NumberAxis("Net profit (portfolio)");

        pnlAxis.setAutoRangeIncludesZero(false);
        StandardXYItemRenderer pnlRenderer1 = new StandardXYItemRenderer();
        pnlRenderer1.setBaseStroke(new BasicStroke(2));
        pnlRenderer1.setAutoPopulateSeriesStroke(false);
        pnlPlot = new XYPlot(profitAndLossCollection, dateAxis, pnlAxis, pnlRenderer1);

        StandardXYItemRenderer pnlRenderer2 = new StandardXYItemRenderer();
        pnlRenderer2.setBaseStroke(new BasicStroke(2));
        pnlRenderer2.setAutoPopulateSeriesStroke(false);
        pnlRenderer2.setSeriesPaint(0, Color.BLACK);
        portfolioPlot = new XYPlot(portfolioProfitAndLossCollection, dateAxis, portfolioAxis, pnlRenderer2);

        combinedPlot.add(pnlPlot);
        combinedPlot.add(portfolioPlot);
        combinedPlot.setDomainAxis(dateAxis);

        setTimeline();
        setTimeZone();

        chart = new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, combinedPlot, true);
        legendTitle = chart.getLegend();
        boolean showLegend = Boolean.parseBoolean(PreferencesHolder.getInstance().get(JBTPreferences.ShowLegend));
        chart.removeLegend();
        setLegend(showLegend);
    }

    public JPanel getChart() {
        return chartPanel;
    }

}
