

package org.jfree.chart.annotations;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;


public class XYTextAnnotation extends AbstractXYAnnotation
        implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -2946063342782506328L;

    
    public static final Font DEFAULT_FONT = new Font("SansSerif", Font.PLAIN,
            10);

    
    public static final Paint DEFAULT_PAINT = Color.BLACK;

    
    public static final TextAnchor DEFAULT_TEXT_ANCHOR = TextAnchor.CENTER;

    
    public static final TextAnchor DEFAULT_ROTATION_ANCHOR = TextAnchor.CENTER;

    
    public static final double DEFAULT_ROTATION_ANGLE = 0.0;

    
    private String text;

    
    private Font font;

    
    private transient Paint paint;

    
    private double x;

    
    private double y;

    
    private TextAnchor textAnchor;

    
    private TextAnchor rotationAnchor;

    
    private double rotationAngle;

    
    private transient Paint backgroundPaint;

    
    private boolean outlineVisible;

    
    private transient Paint outlinePaint;

    
    private transient Stroke outlineStroke;

    
    public XYTextAnnotation(String text, double x, double y) {
        super();
        Args.nullNotPermitted(text, "text");
        Args.requireFinite(x, "x");
        Args.requireFinite(y, "y");
        this.text = text;
        this.font = DEFAULT_FONT;
        this.paint = DEFAULT_PAINT;
        this.x = x;
        this.y = y;
        this.textAnchor = DEFAULT_TEXT_ANCHOR;
        this.rotationAnchor = DEFAULT_ROTATION_ANCHOR;
        this.rotationAngle = DEFAULT_ROTATION_ANGLE;

        // by default the outline and background won't be visible
        this.backgroundPaint = null;
        this.outlineVisible = false;
        this.outlinePaint = Color.BLACK;
        this.outlineStroke = new BasicStroke(0.5f);
    }

    
    public String getText() {
        return this.text;
    }

    
    public void setText(String text) {
        Args.nullNotPermitted(text, "text");
        this.text = text;
        fireAnnotationChanged();
    }

    
    public Font getFont() {
        return this.font;
    }

    
    public void setFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.font = font;
        fireAnnotationChanged();
    }

    
    public Paint getPaint() {
        return this.paint;
    }

    
    public void setPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.paint = paint;
        fireAnnotationChanged();
    }

    
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }

    
    public void setTextAnchor(TextAnchor anchor) {
        Args.nullNotPermitted(anchor, "anchor");
        this.textAnchor = anchor;
        fireAnnotationChanged();
    }

    
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }

    
    public void setRotationAnchor(TextAnchor anchor) {
        Args.nullNotPermitted(anchor, "anchor");
        this.rotationAnchor = anchor;
        fireAnnotationChanged();
    }

    
    public double getRotationAngle() {
        return this.rotationAngle;
    }

    
    public void setRotationAngle(double angle) {
        this.rotationAngle = angle;
        fireAnnotationChanged();
    }

    
    public double getX() {
        return this.x;
    }

    
    public void setX(double x) {
        Args.requireFinite(x, "x");
        this.x = x;
        fireAnnotationChanged();
    }

    
    public double getY() {
        return this.y;
    }

    
    public void setY(double y) {
        Args.requireFinite(y, "y");
        this.y = y;
        fireAnnotationChanged();
    }

    
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    
    public void setBackgroundPaint(Paint paint) {
        this.backgroundPaint = paint;
        fireAnnotationChanged();
    }

    
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    
    public void setOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.outlinePaint = paint;
        fireAnnotationChanged();
    }

    
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    
    public void setOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.outlineStroke = stroke;
        fireAnnotationChanged();
    }

    
    public boolean isOutlineVisible() {
        return this.outlineVisible;
    }

    
    public void setOutlineVisible(boolean visible) {
        this.outlineVisible = visible;
        fireAnnotationChanged();
    }

    
    @Override
    public void draw(Graphics2D g2, XYPlot plot, Rectangle2D dataArea,
                     ValueAxis domainAxis, ValueAxis rangeAxis,
                     int rendererIndex, PlotRenderingInfo info) {

        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);

        float anchorX = (float) domainAxis.valueToJava2D(
                this.x, dataArea, domainEdge);
        float anchorY = (float) rangeAxis.valueToJava2D(
                this.y, dataArea, rangeEdge);

        if (orientation == PlotOrientation.HORIZONTAL) {
            float tempAnchor = anchorX;
            anchorX = anchorY;
            anchorY = tempAnchor;
        }

        g2.setFont(getFont());
        Shape hotspot = TextUtils.calculateRotatedStringBounds(
                getText(), g2, anchorX, anchorY, getTextAnchor(),
                getRotationAngle(), getRotationAnchor());
        if (this.backgroundPaint != null) {
            g2.setPaint(this.backgroundPaint);
            g2.fill(hotspot);
        }
        g2.setPaint(getPaint());
        TextUtils.drawRotatedString(getText(), g2, anchorX, anchorY,
                getTextAnchor(), getRotationAngle(), getRotationAnchor());
        if (this.outlineVisible) {
            g2.setStroke(this.outlineStroke);
            g2.setPaint(this.outlinePaint);
            g2.draw(hotspot);
        }

        String toolTip = getToolTipText();
        String url = getURL();
        if (toolTip != null || url != null) {
            addEntity(info, hotspot, rendererIndex, toolTip, url);
        }

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYTextAnnotation)) {
            return false;
        }
        XYTextAnnotation that = (XYTextAnnotation) obj;
        if (!this.text.equals(that.text)) {
            return false;
        }
        if (this.x != that.x) {
            return false;
        }
        if (this.y != that.y) {
            return false;
        }
        if (!this.font.equals(that.font)) {
            return false;
        }
        if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
        }
        if (!this.rotationAnchor.equals(that.rotationAnchor)) {
            return false;
        }
        if (this.rotationAngle != that.rotationAngle) {
            return false;
        }
        if (!this.textAnchor.equals(that.textAnchor)) {
            return false;
        }
        if (this.outlineVisible != that.outlineVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!(this.outlineStroke.equals(that.outlineStroke))) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + this.text.hashCode();
        result = 37 * result + this.font.hashCode();
        result = 37 * result + HashUtils.hashCodeForPaint(this.paint);
        long temp = Double.doubleToLongBits(this.x);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.y);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        result = 37 * result + this.textAnchor.hashCode();
        result = 37 * result + this.rotationAnchor.hashCode();
        temp = Double.doubleToLongBits(this.rotationAngle);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.paint, stream);
        SerialUtils.writePaint(this.backgroundPaint, stream);
        SerialUtils.writePaint(this.outlinePaint, stream);
        SerialUtils.writeStroke(this.outlineStroke, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtils.readPaint(stream);
        this.backgroundPaint = SerialUtils.readPaint(stream);
        this.outlinePaint = SerialUtils.readPaint(stream);
        this.outlineStroke = SerialUtils.readStroke(stream);
    }

}
