

package org.jfree.chart.renderer.xy;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.ShapeUtils;
import org.jfree.data.Range;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;


public class YIntervalRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -2951586537224143260L;

    
    private XYItemLabelGenerator additionalItemLabelGenerator;

    
    public YIntervalRenderer() {
        super();
        this.additionalItemLabelGenerator = null;
    }

    
    public XYItemLabelGenerator getAdditionalItemLabelGenerator() {
        return this.additionalItemLabelGenerator;
    }

    
    public void setAdditionalItemLabelGenerator(
            XYItemLabelGenerator generator) {
        this.additionalItemLabelGenerator = generator;
        fireChangeEvent();
    }

    
    @Override
    public Range findRangeBounds(XYDataset dataset) {
        return findRangeBounds(dataset, true);
    }

    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(series, item)) {
            return;
        }

        // setup for collecting optional entity info...
        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

        double x = intervalDataset.getXValue(series, item);
        double yLow   = intervalDataset.getStartYValue(series, item);
        double yHigh  = intervalDataset.getEndYValue(series, item);

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        double xx = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
        double yyLow = rangeAxis.valueToJava2D(yLow, dataArea, yAxisLocation);
        double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea, yAxisLocation);

        Paint p = getItemPaint(series, item);
        Stroke s = getItemStroke(series, item);

        Line2D line = null;
        Shape shape = getItemShape(series, item);
        Shape top = null;
        Shape bottom = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            line = new Line2D.Double(yyLow, xx, yyHigh, xx);
            top = ShapeUtils.createTranslatedShape(shape, yyHigh, xx);
            bottom = ShapeUtils.createTranslatedShape(shape, yyLow, xx);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            line = new Line2D.Double(xx, yyLow, xx, yyHigh);
            top = ShapeUtils.createTranslatedShape(shape, xx, yyHigh);
            bottom = ShapeUtils.createTranslatedShape(shape, xx, yyLow);
        } else {
            throw new IllegalStateException();
        }
        g2.setPaint(p);
        g2.setStroke(s);
        g2.draw(line);

        g2.fill(top);
        g2.fill(bottom);

        // for item labels, we have a special case because there is the
        // possibility to draw (a) the regular item label near to just the
        // upper y-value, or (b) the regular item label near the upper y-value
        // PLUS an additional item label near the lower y-value.
        if (isItemLabelVisible(series, item)) {
            drawItemLabel(g2, orientation, dataset, series, item, xx, yyHigh,
                    false);
            drawAdditionalItemLabel(g2, orientation, dataset, series, item,
                    xx, yyLow);
        }

        // add an entity for the item...
        Shape hotspot = ShapeUtils.createLineRegion(line, 4.0f);
        if (entities != null && hotspot.intersects(dataArea)) {
            addEntity(entities, hotspot, dataset, series, item, 0.0, 0.0);
        }

    }

    
    private void drawAdditionalItemLabel(Graphics2D g2,
            PlotOrientation orientation, XYDataset dataset, int series,
            int item, double x, double y) {

        if (this.additionalItemLabelGenerator == null) {
            return;
        }

        Font labelFont = getItemLabelFont(series, item);
        Paint paint = getItemLabelPaint(series, item);
        g2.setFont(labelFont);
        g2.setPaint(paint);
        String label = this.additionalItemLabelGenerator.generateLabel(dataset,
                series, item);

        ItemLabelPosition position = getNegativeItemLabelPosition(series, item);
        Point2D anchorPoint = calculateLabelAnchorPoint(
                position.getItemLabelAnchor(), x, y, orientation);
        TextUtils.drawRotatedString(label, g2,
                (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                position.getTextAnchor(), position.getAngle(),
                position.getRotationAnchor());
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof YIntervalRenderer)) {
            return false;
        }
        YIntervalRenderer that = (YIntervalRenderer) obj;
        if (!Objects.equals(this.additionalItemLabelGenerator, that.additionalItemLabelGenerator)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
