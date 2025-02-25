

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.Range;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;


public class ClusteredXYBarRenderer extends XYBarRenderer
        implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 5864462149177133147L;

    
    private boolean centerBarAtStartValue;

    
    public ClusteredXYBarRenderer() {
        this(0.0, false);
    }

    
    public ClusteredXYBarRenderer(double margin, 
            boolean centerBarAtStartValue) {
        super(margin);
        this.centerBarAtStartValue = centerBarAtStartValue;
    }

    
    @Override
    public int getPassCount() {
        return 2;
    }

    
    @Override
    public Range findDomainBounds(XYDataset dataset) {
        if (dataset == null) {
            return null;
        }
        // need to handle cluster centering as a special case
        if (this.centerBarAtStartValue) {
            return findDomainBoundsWithOffset((IntervalXYDataset) dataset);
        } else {
            return super.findDomainBounds(dataset);
        }
    }

    
    protected Range findDomainBoundsWithOffset(IntervalXYDataset dataset) {
        Args.nullNotPermitted(dataset, "dataset");
        double minimum = Double.POSITIVE_INFINITY;
        double maximum = Double.NEGATIVE_INFINITY;
        int seriesCount = dataset.getSeriesCount();
        double lvalue;
        double uvalue;
        for (int series = 0; series < seriesCount; series++) {
            int itemCount = dataset.getItemCount(series);
            for (int item = 0; item < itemCount; item++) {
                lvalue = dataset.getStartXValue(series, item);
                uvalue = dataset.getEndXValue(series, item);
                double offset = (uvalue - lvalue) / 2.0;
                lvalue = lvalue - offset;
                uvalue = uvalue - offset;
                minimum = Math.min(minimum, lvalue);
                maximum = Math.max(maximum, uvalue);
            }
        }

        if (minimum > maximum) {
            return null;
        } else {
            return new Range(minimum, maximum);
        }
    }

    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        if (!getItemVisible(series, item)) {
            return;
        }

        IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;

        double y0;
        double y1;
        if (getUseYInterval()) {
            y0 = intervalDataset.getStartYValue(series, item);
            y1 = intervalDataset.getEndYValue(series, item);
        } else {
            y0 = getBase();
            y1 = intervalDataset.getYValue(series, item);
        }
        if (Double.isNaN(y0) || Double.isNaN(y1)) {
            return;
        }

        double yy0 = rangeAxis.valueToJava2D(y0, dataArea,
                plot.getRangeAxisEdge());
        double yy1 = rangeAxis.valueToJava2D(y1, dataArea,
                plot.getRangeAxisEdge());

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        double x0 = intervalDataset.getStartXValue(series, item);
        double xx0 = domainAxis.valueToJava2D(x0, dataArea, xAxisLocation);

        double x1 = intervalDataset.getEndXValue(series, item);
        double xx1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);

        double intervalW = xx1 - xx0;  // this may be negative
        double baseX = xx0;
        if (this.centerBarAtStartValue) {
            baseX = baseX - intervalW / 2.0;
        }
        double m = getMargin();
        if (m > 0.0) {
            double cut = intervalW * getMargin();
            intervalW = intervalW - cut;
            baseX = baseX + (cut / 2);
        }

        double intervalH = Math.abs(yy0 - yy1);  // we don't need the sign

        PlotOrientation orientation = plot.getOrientation();

        ArrayList<Integer> visibleSeries = new ArrayList<>();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            if (isSeriesVisible(i)) {
                visibleSeries.add(i);
            }
        }

        int numSeries = visibleSeries.size();
        double seriesBarWidth = intervalW / numSeries;  // may be negative
        int visibleSeriesIndex = visibleSeries.indexOf(series);

        Rectangle2D bar = null;
        if (orientation == PlotOrientation.HORIZONTAL) {
            double barY0 = baseX + (seriesBarWidth * visibleSeriesIndex);
            double barY1 = barY0 + seriesBarWidth;
            double rx = Math.min(yy0, yy1);
            double rw = intervalH;
            double ry = Math.min(barY0, barY1);
            double rh = Math.abs(barY1 - barY0);
            bar = new Rectangle2D.Double(rx, ry, rw, rh);
        } else if (orientation == PlotOrientation.VERTICAL) {
            double barX0 = baseX + (seriesBarWidth * visibleSeriesIndex);
            double barX1 = barX0 + seriesBarWidth;
            double rx = Math.min(barX0, barX1);
            double rw = Math.abs(barX1 - barX0);
            double ry = Math.min(yy0, yy1);
            double rh = intervalH;
            bar = new Rectangle2D.Double(rx, ry, rw, rh);
        } else {
            throw new IllegalStateException();
        }
        boolean positive = (y1 > 0.0);
        boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if (orientation == PlotOrientation.HORIZONTAL) {
            if (positive && inverted || !positive && !inverted) {
                barBase = RectangleEdge.RIGHT;
            } else {
                barBase = RectangleEdge.LEFT;
            }
        } else {
            if (positive && !inverted || !positive && inverted) {
                barBase = RectangleEdge.BOTTOM;
            } else {
                barBase = RectangleEdge.TOP;
            }
        }
        if (pass == 0 && getShadowsVisible()) {
            getBarPainter().paintBarShadow(g2, this, series, item, bar, barBase,
                    !getUseYInterval());
        }
        if (pass == 1) {
            getBarPainter().paintBar(g2, this, series, item, bar, barBase);

            if (isItemLabelVisible(series, item)) {
                XYItemLabelGenerator generator = getItemLabelGenerator(series,
                        item);
                drawItemLabel(g2, dataset, series, item, plot, generator, bar,
                        y1 < 0.0);
            }

            // add an entity for the item...
            if (info != null) {
                EntityCollection entities
                        = info.getOwner().getEntityCollection();
                if (entities != null) {
                    addEntity(entities, bar, dataset, series, item,
                            bar.getCenterX(), bar.getCenterY());
                }
            }
        }

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ClusteredXYBarRenderer)) {
            return false;
        }
        ClusteredXYBarRenderer that = (ClusteredXYBarRenderer) obj;
        if (this.centerBarAtStartValue != that.centerBarAtStartValue) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
