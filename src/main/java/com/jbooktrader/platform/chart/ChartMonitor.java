package com.jbooktrader.platform.chart;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import java.awt.*;

/**
 * @author Eugene Kononov
 */
public class ChartMonitor extends ChartPanel {
    public ChartMonitor(JFreeChart chart) {
        super(chart, true);
    }

    @Override
    public void paint(Graphics g) {
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        super.paint(g);
        setCursor(Cursor.getDefaultCursor());
    }
}
