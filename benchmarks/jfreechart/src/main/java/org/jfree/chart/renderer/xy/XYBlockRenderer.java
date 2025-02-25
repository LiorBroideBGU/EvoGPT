

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.api.RectangleAnchor;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;


public class XYBlockRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    
    private double blockWidth = 1.0;

    
    private double blockHeight = 1.0;

    
    private RectangleAnchor blockAnchor = RectangleAnchor.CENTER;

    
    private double xOffset;

    
    private double yOffset;

    
    private PaintScale paintScale;
    
    
    private boolean drawOutlines;

    
    private boolean useOutlinePaint;

    
    public XYBlockRenderer() {
        updateOffsets();
        this.paintScale = new LookupPaintScale();
        this.drawOutlines = true;
        this.useOutlinePaint = false; // use item paint by default
    }

    
    public double getBlockWidth() {
        return this.blockWidth;
    }

    
    public void setBlockWidth(double width) {
        if (width <= 0.0) {
            throw new IllegalArgumentException(
                    "The 'width' argument must be > 0.0");
        }
        this.blockWidth = width;
        updateOffsets();
        fireChangeEvent();
    }

    
    public double getBlockHeight() {
        return this.blockHeight;
    }

    
    public void setBlockHeight(double height) {
        if (height <= 0.0) {
            throw new IllegalArgumentException(
                    "The 'height' argument must be > 0.0");
        }
        this.blockHeight = height;
        updateOffsets();
        fireChangeEvent();
    }

    
    public RectangleAnchor getBlockAnchor() {
        return this.blockAnchor;
    }

    
    public void setBlockAnchor(RectangleAnchor anchor) {
        Args.nullNotPermitted(anchor, "anchor");
        if (this.blockAnchor.equals(anchor)) {
            return;  // no change
        }
        this.blockAnchor = anchor;
        updateOffsets();
        fireChangeEvent();
    }

    
    public PaintScale getPaintScale() {
        return this.paintScale;
    }

    
    public void setPaintScale(PaintScale scale) {
        Args.nullNotPermitted(scale, "scale");
        this.paintScale = scale;
        fireChangeEvent();
    }

    
    public boolean getDrawOutlines() {
        return this.drawOutlines;
    }

    
    public void setDrawOutlines(boolean flag) {
        this.drawOutlines = flag;
        fireChangeEvent();
    }

    
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }

    
    public void setUseOutlinePaint(boolean flag) {
        this.useOutlinePaint = flag;
        fireChangeEvent();
    }

    
    private void updateOffsets() {
        if (this.blockAnchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            this.xOffset = 0.0;
            this.yOffset = 0.0;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.BOTTOM)) {
            this.xOffset = -this.blockWidth / 2.0;
            this.yOffset = 0.0;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            this.xOffset = -this.blockWidth;
            this.yOffset = 0.0;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.LEFT)) {
            this.xOffset = 0.0;
            this.yOffset = -this.blockHeight / 2.0;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.CENTER)) {
            this.xOffset = -this.blockWidth / 2.0;
            this.yOffset = -this.blockHeight / 2.0;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.RIGHT)) {
            this.xOffset = -this.blockWidth;
            this.yOffset = -this.blockHeight / 2.0;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.TOP_LEFT)) {
            this.xOffset = 0.0;
            this.yOffset = -this.blockHeight;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.TOP)) {
            this.xOffset = -this.blockWidth / 2.0;
            this.yOffset = -this.blockHeight;
        }
        else if (this.blockAnchor.equals(RectangleAnchor.TOP_RIGHT)) {
            this.xOffset = -this.blockWidth;
            this.yOffset = -this.blockHeight;
        }
    }

    
    @Override
    public Range findDomainBounds(XYDataset dataset) {
        if (dataset == null) {
            return null;
        }
        Range r = DatasetUtils.findDomainBounds(dataset, false);
        if (r == null) {
            return null;
        }
        return new Range(r.getLowerBound() + this.xOffset,
                         r.getUpperBound() + this.blockWidth + this.xOffset);
    }

    
    @Override
    public Range findRangeBounds(XYDataset dataset) {
        if (dataset != null) {
            Range r = DatasetUtils.findRangeBounds(dataset, false);
            if (r == null) {
                return null;
            }
            else {
                return new Range(r.getLowerBound() + this.yOffset,
                        r.getUpperBound() + this.blockHeight + this.yOffset);
            }
        }
        else {
            return null;
        }
    }

    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        double x = dataset.getXValue(series, item);
        double y = dataset.getYValue(series, item);
        double z = 0.0;
        if (dataset instanceof XYZDataset) {
            z = ((XYZDataset) dataset).getZValue(series, item);
        }

        Paint p = this.paintScale.getPaint(z);
        double xx0 = domainAxis.valueToJava2D(x + this.xOffset, dataArea,
                plot.getDomainAxisEdge());
        double yy0 = rangeAxis.valueToJava2D(y + this.yOffset, dataArea,
                plot.getRangeAxisEdge());
        double xx1 = domainAxis.valueToJava2D(x + this.blockWidth
                + this.xOffset, dataArea, plot.getDomainAxisEdge());
        double yy1 = rangeAxis.valueToJava2D(y + this.blockHeight
                + this.yOffset, dataArea, plot.getRangeAxisEdge());
        Rectangle2D block;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation.equals(PlotOrientation.HORIZONTAL)) {
            block = new Rectangle2D.Double(Math.min(yy0, yy1),
                    Math.min(xx0, xx1), Math.abs(yy1 - yy0),
                    Math.abs(xx0 - xx1));
        }
        else {
            block = new Rectangle2D.Double(Math.min(xx0, xx1),
                    Math.min(yy0, yy1), Math.abs(xx1 - xx0),
                    Math.abs(yy1 - yy0));
        }
        g2.setPaint(p);
        g2.fill(block);
        if (getDrawOutlines()) {
            if (getUseOutlinePaint()) {
                g2.setPaint(getItemOutlinePaint(series, item));
            }
            g2.setStroke(lookupSeriesOutlineStroke(series));
            g2.draw(block);
        }

        if (isItemLabelVisible(series, item)) {
            drawItemLabel(g2, orientation, dataset, series, item, 
                    block.getCenterX(), block.getCenterY(), y < 0.0);
        }

        int datasetIndex = plot.indexOf(dataset);
        double transX = domainAxis.valueToJava2D(x, dataArea,
                plot.getDomainAxisEdge());
        double transY = rangeAxis.valueToJava2D(y, dataArea,
                plot.getRangeAxisEdge());        
        updateCrosshairValues(crosshairState, x, y, datasetIndex,
                transX, transY, orientation);

        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
            addEntity(entities, block, dataset, series, item, 
                    block.getCenterX(), block.getCenterY());
        }

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYBlockRenderer)) {
            return false;
        }
        XYBlockRenderer that = (XYBlockRenderer) obj;
        if (this.blockHeight != that.blockHeight) {
            return false;
        }
        if (this.blockWidth != that.blockWidth) {
            return false;
        }
        if (!this.blockAnchor.equals(that.blockAnchor)) {
            return false;
        }
        if (!this.paintScale.equals(that.paintScale)) {
            return false;
        }
        if (this.drawOutlines != that.drawOutlines) {
            return false;
        }
        if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        XYBlockRenderer clone = (XYBlockRenderer) super.clone();
        if (this.paintScale instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.paintScale;
            clone.paintScale = (PaintScale) pc.clone();
        }
        return clone;
    }

}
