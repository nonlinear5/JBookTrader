package com.jbooktrader.platform.optimizer;

import org.jfree.chart.annotations.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.text.*;
import org.jfree.ui.*;

import java.awt.*;
import java.awt.geom.*;

/**
 * Defines the shape of the marker which shows top parameters in optimization maps.
 *
 * @author Eugene Kononov
 */
public class OptimizationMapAnnotation extends AbstractXYAnnotation {
    private final int radius;
    private final double x, y;
    private final String text;

    public OptimizationMapAnnotation(int radius, double x, double y, String text) {
        this.radius = radius;
        this.x = x;
        this.y = y;
        this.text = text;
    }

    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea, ValueAxis domainAxis, ValueAxis rangeAxis, int rendererIndex, PlotRenderingInfo info) {
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(plot.getRangeAxisLocation(), orientation);

        double anchorX = domainAxis.valueToJava2D(x, dataArea, domainEdge);
        double anchorY = rangeAxis.valueToJava2D(y, dataArea, rangeEdge);

        double shapeX = anchorX - radius;
        double shapeY = anchorY - radius;

        double width = radius * 2.0;
        double height = radius * 2.0;

        g2.setColor(Color.LIGHT_GRAY);
        g2.fill(new Ellipse2D.Double(shapeX, shapeY, width, height));

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.25f));
        g2.draw(new Ellipse2D.Double(shapeX, shapeY, width, height));

        TextUtilities.drawAlignedString(text, g2, (float) anchorX, (float) anchorY, TextAnchor.CENTER);
    }
}
