

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.internal.LineUtils;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.xy.XYDataset;


public class XYStepRenderer extends XYLineAndShapeRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -8918141928884796108L;

    
    private double stepPoint = 1.0d;

    
    public XYStepRenderer() {
        this(null, null);
    }

    
    public XYStepRenderer(XYToolTipGenerator toolTipGenerator,
                          XYURLGenerator urlGenerator) {
        super();
        setDefaultToolTipGenerator(toolTipGenerator);
        setURLGenerator(urlGenerator);
        setDefaultShapesVisible(false);
    }

    
    public double getStepPoint() {
        return this.stepPoint;
    }

    
    public void setStepPoint(double stepPoint) {
        if (stepPoint < 0.0d || stepPoint > 1.0d) {
            throw new IllegalArgumentException(
                    "Requires stepPoint in [0.0;1.0]");
        }
        this.stepPoint = stepPoint;
        fireChangeEvent();
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

        PlotOrientation orientation = plot.getOrientation();

        Paint seriesPaint = getItemPaint(series, item);
        Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(seriesPaint);
        g2.setStroke(seriesStroke);

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = (Double.isNaN(y1) ? Double.NaN
                : rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation));

        if (pass == 0 && item > 0) {
            // get the previous data point...
            double x0 = dataset.getXValue(series, item - 1);
            double y0 = dataset.getYValue(series, item - 1);
            double transX0 = domainAxis.valueToJava2D(x0, dataArea,
                    xAxisLocation);
            double transY0 = (Double.isNaN(y0) ? Double.NaN
                    : rangeAxis.valueToJava2D(y0, dataArea, yAxisLocation));

            if (orientation == PlotOrientation.HORIZONTAL) {
                if (transY0 == transY1) {
                    // this represents the situation
                    // for drawing a horizontal bar.
                    drawLine(g2, state.workingLine, transY0, transX0, transY1,
                            transX1, dataArea);
                }
                else {  //this handles the need to perform a 'step'.

                    // calculate the step point
                    double transXs = transX0 + (getStepPoint()
                            * (transX1 - transX0));
                    drawLine(g2, state.workingLine, transY0, transX0, transY0,
                            transXs, dataArea);
                    drawLine(g2, state.workingLine, transY0, transXs, transY1,
                            transXs, dataArea);
                    drawLine(g2, state.workingLine, transY1, transXs, transY1,
                            transX1, dataArea);
                }
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                if (transY0 == transY1) { // this represents the situation
                                          // for drawing a horizontal bar.
                    drawLine(g2, state.workingLine, transX0, transY0, transX1,
                            transY1, dataArea);
                }
                else {  //this handles the need to perform a 'step'.
                    // calculate the step point
                    double transXs = transX0 + (getStepPoint()
                            * (transX1 - transX0));
                    drawLine(g2, state.workingLine, transX0, transY0, transXs,
                            transY0, dataArea);
                    drawLine(g2, state.workingLine, transXs, transY0, transXs,
                            transY1, dataArea);
                    drawLine(g2, state.workingLine, transXs, transY1, transX1,
                            transY1, dataArea);
                }
            }

            // submit this data item as a candidate for the crosshair point
            int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(crosshairState, x1, y1, datasetIndex,
                    transX1, transY1, orientation);

            // collect entity and tool tip information...
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                if (orientation == PlotOrientation.HORIZONTAL) {
                    addEntity(entities, null, dataset, series, item, transY1,
                            transX1);
                } else {
                    addEntity(entities, null, dataset, series, item, transX1,
                            transY1);
                }
            }

        }

        if (pass == 1) {
            // draw the item label if there is one...
            if (isItemLabelVisible(series, item)) {
                double xx = transX1;
                double yy = transY1;
                if (orientation == PlotOrientation.HORIZONTAL) {
                    xx = transY1;
                    yy = transX1;
                }
                drawItemLabel(g2, orientation, dataset, series, item, xx, yy,
                        (y1 < 0.0));
            }
        }
    }

    
    private void drawLine(Graphics2D g2, Line2D line, double x0, double y0,
            double x1, double y1, Rectangle2D dataArea) {
        if (Double.isNaN(x0) || Double.isNaN(x1) || Double.isNaN(y0)
                || Double.isNaN(y1)) {
            return;
        }
        line.setLine(x0, y0, x1, y1);
        boolean visible = LineUtils.clipLine(line, dataArea);
        if (visible) {
            g2.draw(line);
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYLineAndShapeRenderer)) {
            return false;
        }
        XYStepRenderer that = (XYStepRenderer) obj;
        if (this.stepPoint != that.stepPoint) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        return HashUtils.hashCode(super.hashCode(), this.stepPoint);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
