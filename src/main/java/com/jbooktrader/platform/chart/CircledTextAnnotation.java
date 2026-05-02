package com.jbooktrader.platform.chart;

import org.jfree.chart.annotations.AbstractXYAnnotation;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.ui.RectangleEdge;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;

/**
 * Defines the shape of the markers which show strategy positions on the
 * performance chart. In this implementation, the shape of a marker is a
 * solid circle whose color indicates the position taken (long, short, or flat)
 *
 * @author Eugene Kononov
 */
public class CircledTextAnnotation extends AbstractXYAnnotation {
    private static final int radius = 5;
    private final Color fillColor;
    private final double x, y;

    CircledTextAnnotation(int quantity, double x, double y) {
        this.x = x;
        this.y = y;
        fillColor = (quantity > 0) ? Color.GREEN : ((quantity < 0) ? Color.RED : Color.YELLOW);
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

        double anchorX = domainAxis.valueToJava2D(x, dataArea, domainEdge);
        double anchorY = rangeAxis.valueToJava2D(y, dataArea, rangeEdge);

        anchorX -= radius;
        anchorY -= radius;
        double width = radius * 2.0;
        double height = radius * 2.0;

        g2.setColor(fillColor);
        g2.fill(new Ellipse2D.Double(anchorX, anchorY, width, height));

        g2.setPaint(Color.GRAY);
        g2.draw(new Ellipse2D.Double(anchorX, anchorY, width, height));

    }
}
