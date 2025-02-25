

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.ShapeUtils;
import org.jfree.data.xy.XYDataset;


public class XYStepAreaRenderer extends AbstractXYItemRenderer
        implements XYItemRenderer, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -7311560779702649635L;

    
    public static final int SHAPES = 1;

    
    public static final int AREA = 2;

    
    public static final int AREA_AND_SHAPES = 3;

    
    private boolean shapesVisible;

    
    private boolean shapesFilled;

    
    private boolean plotArea;

    
    private boolean showOutline;

    
    protected transient Polygon pArea = null;

    
    private double rangeBase;
    
    
    private double stepPoint;

    
    public XYStepAreaRenderer() {
        this(AREA);
    }

    
    public XYStepAreaRenderer(int type) {
        this(type, null, null);
    }

    
    public XYStepAreaRenderer(int type, XYToolTipGenerator toolTipGenerator,
            XYURLGenerator urlGenerator) {
        super();
        setDefaultToolTipGenerator(toolTipGenerator);
        setURLGenerator(urlGenerator);

        if (type == AREA) {
            this.plotArea = true;
        }
        else if (type == SHAPES) {
            this.shapesVisible = true;
        }
        else if (type == AREA_AND_SHAPES) {
            this.plotArea = true;
            this.shapesVisible = true;
        }
        this.showOutline = false;
        this.stepPoint = 1.0;
    }

    
    public boolean isOutline() {
        return this.showOutline;
    }

    
    public void setOutline(boolean show) {
        this.showOutline = show;
        fireChangeEvent();
    }

    
    public boolean getShapesVisible() {
        return this.shapesVisible;
    }

    
    public void setShapesVisible(boolean flag) {
        this.shapesVisible = flag;
        fireChangeEvent();
    }

    
    public boolean isShapesFilled() {
        return this.shapesFilled;
    }

    
    public void setShapesFilled(boolean filled) {
        this.shapesFilled = filled;
        fireChangeEvent();
    }

    
    public boolean getPlotArea() {
        return this.plotArea;
    }

    
    public void setPlotArea(boolean flag) {
        this.plotArea = flag;
        fireChangeEvent();
    }

    
    public double getRangeBase() {
        return this.rangeBase;
    }

    
    public void setRangeBase(double val) {
        this.rangeBase = val;
        fireChangeEvent();
    }

    
    public double getStepPoint() {
        return stepPoint;
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
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
            XYPlot plot, XYDataset data, PlotRenderingInfo info) {

        XYItemRendererState state = super.initialise(g2, dataArea, plot, data,
                info);
        // disable visible items optimisation - it doesn't work for this
        // renderer...
        state.setProcessVisibleItemsOnly(false);
        return state;

    }

    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, 
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        PlotOrientation orientation = plot.getOrientation();

        // Get the item count for the series, so that we can know which is the
        // end of the series.
        int itemCount = dataset.getItemCount(series);

        Paint paint = getItemPaint(series, item);
        Stroke seriesStroke = getItemStroke(series, item);
        g2.setPaint(paint);
        g2.setStroke(seriesStroke);

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double x = x1;
        double y = Double.isNaN(y1) ? getRangeBase() : y1;
        double transX1 = domainAxis.valueToJava2D(x, dataArea,
                plot.getDomainAxisEdge());
        double transY1 = rangeAxis.valueToJava2D(y, dataArea,
                plot.getRangeAxisEdge());

        // avoid possible sun.dc.pr.PRException: endPath: bad path
        transY1 = restrictValueToDataArea(transY1, plot, dataArea);

        if (this.pArea == null && !Double.isNaN(y1)) {

            // Create a new Area for the series
            this.pArea = new Polygon();

            // start from Y = rangeBase
            double transY2 = rangeAxis.valueToJava2D(getRangeBase(), dataArea,
                    plot.getRangeAxisEdge());

            // avoid possible sun.dc.pr.PRException: endPath: bad path
            transY2 = restrictValueToDataArea(transY2, plot, dataArea);

            // The first point is (x, this.baseYValue)
            if (orientation == PlotOrientation.VERTICAL) {
                this.pArea.addPoint((int) transX1, (int) transY2);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                this.pArea.addPoint((int) transY2, (int) transX1);
            }
        }

        double transX0;
        double transY0;

        double x0;
        double y0;
        if (item > 0) {
            // get the previous data point...
            x0 = dataset.getXValue(series, item - 1);
            y0 = Double.isNaN(y1) ? y1 : dataset.getYValue(series, item - 1);

            x = x0;
            y = Double.isNaN(y0) ? getRangeBase() : y0;
            transX0 = domainAxis.valueToJava2D(x, dataArea,
                    plot.getDomainAxisEdge());
            transY0 = rangeAxis.valueToJava2D(y, dataArea,
                    plot.getRangeAxisEdge());

            // avoid possible sun.dc.pr.PRException: endPath: bad path
            transY0 = restrictValueToDataArea(transY0, plot, dataArea);

            if (Double.isNaN(y1)) {
                // NULL value -> insert point on base line
                // instead of 'step point'
                transX1 = transX0;
                transY0 = transY1;
            }
            if (transY0 != transY1) {
                // not just a horizontal bar but need to perform a 'step'.
                double transXs = transX0 + (getStepPoint()
                        * (transX1 - transX0));
                if (orientation == PlotOrientation.VERTICAL) {
                    this.pArea.addPoint((int) transXs, (int) transY0);
                    this.pArea.addPoint((int) transXs, (int) transY1);
                }
                else if (orientation == PlotOrientation.HORIZONTAL) {
                    this.pArea.addPoint((int) transY0, (int) transXs);
                    this.pArea.addPoint((int) transY1, (int) transXs);
                }
            }
        }

        Shape shape = null;
        if (!Double.isNaN(y1)) {
            // Add each point to Area (x, y)
            if (orientation == PlotOrientation.VERTICAL) {
                this.pArea.addPoint((int) transX1, (int) transY1);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                this.pArea.addPoint((int) transY1, (int) transX1);
            }

            if (getShapesVisible()) {
                shape = getItemShape(series, item);
                if (orientation == PlotOrientation.VERTICAL) {
                    shape = ShapeUtils.createTranslatedShape(shape,
                            transX1, transY1);
                }
                else if (orientation == PlotOrientation.HORIZONTAL) {
                    shape = ShapeUtils.createTranslatedShape(shape,
                            transY1, transX1);
                }
                if (isShapesFilled()) {
                    g2.fill(shape);
                }
                else {
                    g2.draw(shape);
                }
            }
            else {
                if (orientation == PlotOrientation.VERTICAL) {
                    shape = new Rectangle2D.Double(transX1 - 2, transY1 - 2,
                            4.0, 4.0);
                } else if (orientation == PlotOrientation.HORIZONTAL) {
                    shape = new Rectangle2D.Double(transY1 - 2, transX1 - 2,
                            4.0, 4.0);
                }
            }
        }

        // Check if the item is the last item for the series or if it
        // is a NULL value and number of items > 0.  We can't draw an area for
        // a single point.
        if (getPlotArea() && item > 0 && this.pArea != null
                          && (item == (itemCount - 1) || Double.isNaN(y1))) {

            double transY2 = rangeAxis.valueToJava2D(getRangeBase(), dataArea,
                    plot.getRangeAxisEdge());

            // avoid possible sun.dc.pr.PRException: endPath: bad path
            transY2 = restrictValueToDataArea(transY2, plot, dataArea);

            if (orientation == PlotOrientation.VERTICAL) {
                // Add the last point (x,0)
                this.pArea.addPoint((int) transX1, (int) transY2);
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                // Add the last point (x,0)
                this.pArea.addPoint((int) transY2, (int) transX1);
            }

            // fill the polygon
            g2.fill(this.pArea);

            // draw an outline around the Area.
            if (isOutline()) {
                g2.setStroke(plot.getOutlineStroke());
                g2.setPaint(plot.getOutlinePaint());
                g2.draw(this.pArea);
            }

            // start new area when needed (see above)
            this.pArea = null;
        }

        // do we need to update the crosshair values?
        if (!Double.isNaN(y1)) {
            int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(crosshairState, x1, y1, datasetIndex,
                    transX1, transY1, orientation);
        }

        // collect entity and tool tip information...
        EntityCollection entities = state.getEntityCollection();
        if (entities != null && shape != null) {
            addEntity(entities, shape, dataset, series, item, 0.0, 0.0);
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYStepAreaRenderer)) {
            return false;
        }
        XYStepAreaRenderer that = (XYStepAreaRenderer) obj;
        if (this.showOutline != that.showOutline) {
            return false;
        }
        if (this.shapesVisible != that.shapesVisible) {
            return false;
        }
        if (this.shapesFilled != that.shapesFilled) {
            return false;
        }
        if (this.plotArea != that.plotArea) {
            return false;
        }
        if (this.rangeBase != that.rangeBase) {
            return false;
        }
        if (this.stepPoint != that.stepPoint) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    protected static double restrictValueToDataArea(double value,
                                                    XYPlot plot,
                                                    Rectangle2D dataArea) {
        double min = 0;
        double max = 0;
        if (plot.getOrientation() == PlotOrientation.VERTICAL) {
            min = dataArea.getMinY();
            max = dataArea.getMaxY();
        }
        else if (plot.getOrientation() ==  PlotOrientation.HORIZONTAL) {
            min = dataArea.getMinX();
            max = dataArea.getMaxX();
        }
        if (value < min) {
            value = min;
        }
        else if (value > max) {
            value = max;
        }
        return value;
    }

}
