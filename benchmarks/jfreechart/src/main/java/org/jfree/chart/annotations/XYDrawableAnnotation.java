

package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.Drawable;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;


public class XYDrawableAnnotation extends AbstractXYAnnotation
        implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -6540812859722691020L;

    
    private double drawScaleFactor;

    
    private double x;

    
    private double y;

    
    private double displayWidth;

    
    private double displayHeight;

    
    private Drawable drawable;

    
    public XYDrawableAnnotation(double x, double y, double width, double height,
                                Drawable drawable) {
        this(x, y, width, height, 1.0, drawable);
    }

    
    public XYDrawableAnnotation(double x, double y, double displayWidth,
            double displayHeight, double drawScaleFactor, Drawable drawable) {
        super();
        Args.nullNotPermitted(drawable, "drawable");
        Args.requireFinite(x, "x");
        Args.requireFinite(y, "y");
        Args.requireFinite(displayWidth, "displayWidth");
        Args.requireFinite(displayHeight, "displayHeight");
        Args.requireFinite(drawScaleFactor, "drawScaleFactor");
        this.x = x;
        this.y = y;
        this.displayWidth = displayWidth;
        this.displayHeight = displayHeight;
        this.drawScaleFactor = drawScaleFactor;
        this.drawable = drawable;
    }

    
    public double getX() {
        return x;
    }

    
    public double getY() {
        return y;
    }

    
    public double getDisplayWidth() {
        return displayWidth;
    }

    
    public double getDisplayHeight() {
        return displayHeight;
    }

    
    public double getDrawScaleFactor() {
        return drawScaleFactor;
    }

    
    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
                     ValueAxis domainAxis, ValueAxis rangeAxis,
                     int rendererIndex,
                     PlotRenderingInfo info) {

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);
        float j2DX = (float) domainAxis.valueToJava2D(this.x, dataArea,
                domainEdge);
        float j2DY = (float) rangeAxis.valueToJava2D(this.y, dataArea,
                rangeEdge);
        Rectangle2D displayArea = new Rectangle2D.Double(
                j2DX - this.displayWidth / 2.0,
                j2DY - this.displayHeight / 2.0, this.displayWidth,
                this.displayHeight);

        // here we change the AffineTransform so we can draw the annotation
        // to a larger area and scale it down into the display area
        // afterwards, the original transform is restored
        AffineTransform savedTransform = g2.getTransform();
        Rectangle2D drawArea = new Rectangle2D.Double(0.0, 0.0,
                this.displayWidth * this.drawScaleFactor,
                this.displayHeight * this.drawScaleFactor);

        g2.scale(1 / this.drawScaleFactor, 1 / this.drawScaleFactor);
        g2.translate((j2DX - this.displayWidth / 2.0) * this.drawScaleFactor,
                (j2DY - this.displayHeight / 2.0) * this.drawScaleFactor);
        this.drawable.draw(g2, drawArea);
        g2.setTransform(savedTransform);
        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) {
            addEntity(info, displayArea, rendererIndex, toolTip, url);
        }

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) { // simple case
            return true;
        }
        // now try to reject equality...
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof XYDrawableAnnotation)) {
            return false;
        }
        XYDrawableAnnotation that = (XYDrawableAnnotation) obj;
        if (this.x != that.x) {
            return false;
        }
        if (this.y != that.y) {
            return false;
        }
        if (this.displayWidth != that.displayWidth) {
            return false;
        }
        if (this.displayHeight != that.displayHeight) {
            return false;
        }
        if (this.drawScaleFactor != that.drawScaleFactor) {
            return false;
        }
        if (!Objects.equals(this.drawable, that.drawable)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(this.x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.displayWidth);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.displayHeight);
        result = 29 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
