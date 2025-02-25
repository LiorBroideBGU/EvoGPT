

package org.jfree.chart.plot.dial;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;


public class StandardDialScale extends AbstractDialLayer implements DialScale,
        Cloneable, PublicCloneable, Serializable {

    
    static final long serialVersionUID = 3715644629665918516L;

    
    private double lowerBound;

    
    private double upperBound;

    
    private double startAngle;

    
    private double extent;

    
    private double tickRadius;

    
    private double majorTickIncrement;

    
    private double majorTickLength;

    
    private transient Paint majorTickPaint;

    
    private transient Stroke majorTickStroke;

    
    private int minorTickCount;

    
    private double minorTickLength;

    
    private transient Paint minorTickPaint;

    
    private transient Stroke minorTickStroke;

    
    private double tickLabelOffset;

    
    private Font tickLabelFont;

    
    private boolean tickLabelsVisible;

    
    private NumberFormat tickLabelFormatter;

    
    private boolean firstTickLabelVisible;

    
    private transient Paint tickLabelPaint;

    
    public StandardDialScale() {
        this(0.0, 100.0, 175, -170, 10.0, 4);
    }

    
    public StandardDialScale(double lowerBound, double upperBound,
            double startAngle, double extent, double majorTickIncrement,
            int minorTickCount) {
        if (majorTickIncrement <= 0.0) {
            throw new IllegalArgumentException(
                    "Requires 'majorTickIncrement' > 0.");
        }
        this.startAngle = startAngle;
        this.extent = extent;
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
        this.tickRadius = 0.70;
        this.tickLabelsVisible = true;
        this.tickLabelFormatter = new DecimalFormat("0.0");
        this.firstTickLabelVisible = true;
        this.tickLabelFont = new Font("Dialog", Font.BOLD, 16);
        this.tickLabelPaint = Color.BLUE;
        this.tickLabelOffset = 0.10;
        this.majorTickIncrement = majorTickIncrement;
        this.majorTickLength = 0.04;
        this.majorTickPaint = Color.BLACK;
        this.majorTickStroke = new BasicStroke(3.0f);
        this.minorTickCount = minorTickCount;
        this.minorTickLength = 0.02;
        this.minorTickPaint = Color.BLACK;
        this.minorTickStroke = new BasicStroke(1.0f);
    }

    
    public double getLowerBound() {
        return this.lowerBound;
    }

    
    public void setLowerBound(double lower) {
        this.lowerBound = lower;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getUpperBound() {
        return this.upperBound;
    }

    
    public void setUpperBound(double upper) {
        this.upperBound = upper;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getStartAngle() {
        return this.startAngle;
    }

    
    public void setStartAngle(double angle) {
        this.startAngle = angle;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getExtent() {
        return this.extent;
    }

    
    public void setExtent(double extent) {
        this.extent = extent;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getTickRadius() {
        return this.tickRadius;
    }

    
    public void setTickRadius(double radius) {
        if (radius <= 0.0) {
            throw new IllegalArgumentException(
                    "The 'radius' must be positive.");
        }
        this.tickRadius = radius;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getMajorTickIncrement() {
        return this.majorTickIncrement;
    }

    
    public void setMajorTickIncrement(double increment) {
        if (increment <= 0.0) {
            throw new IllegalArgumentException(
                    "The 'increment' must be positive.");
        }
        this.majorTickIncrement = increment;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getMajorTickLength() {
        return this.majorTickLength;
    }

    
    public void setMajorTickLength(double length) {
        if (length < 0.0) {
            throw new IllegalArgumentException("Negative 'length' argument.");
        }
        this.majorTickLength = length;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public Paint getMajorTickPaint() {
        return this.majorTickPaint;
    }

    
    public void setMajorTickPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.majorTickPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public Stroke getMajorTickStroke() {
        return this.majorTickStroke;
    }

    
    public void setMajorTickStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.majorTickStroke = stroke;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public int getMinorTickCount() {
        return this.minorTickCount;
    }

    
    public void setMinorTickCount(int count) {
        if (count < 0) {
            throw new IllegalArgumentException(
                    "The 'count' cannot be negative.");
        }
        this.minorTickCount = count;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getMinorTickLength() {
        return this.minorTickLength;
    }

    
    public void setMinorTickLength(double length) {
        if (length < 0.0) {
            throw new IllegalArgumentException("Negative 'length' argument.");
        }
        this.minorTickLength = length;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public Paint getMinorTickPaint() {
        return this.minorTickPaint;
    }

    
    public void setMinorTickPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.minorTickPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public Stroke getMinorTickStroke() {
        return this.minorTickStroke;
    }

    
    public void setMinorTickStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.minorTickStroke = stroke;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public double getTickLabelOffset() {
        return this.tickLabelOffset;
    }

    
    public void setTickLabelOffset(double offset) {
        this.tickLabelOffset = offset;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }

    
    public void setTickLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.tickLabelFont = font;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }

    
    public void setTickLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.tickLabelPaint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public boolean getTickLabelsVisible() {
        return this.tickLabelsVisible;
    }

    
    public void setTickLabelsVisible(boolean visible) {
        this.tickLabelsVisible = visible;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public NumberFormat getTickLabelFormatter() {
        return this.tickLabelFormatter;
    }

    
    public void setTickLabelFormatter(NumberFormat formatter) {
        Args.nullNotPermitted(formatter, "formatter");
        this.tickLabelFormatter = formatter;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public boolean getFirstTickLabelVisible() {
        return this.firstTickLabelVisible;
    }

    
    public void setFirstTickLabelVisible(boolean visible) {
        this.firstTickLabelVisible = visible;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    @Override
    public boolean isClippedToWindow() {
        return true;
    }

    
    @Override
    public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
            Rectangle2D view) {

        Rectangle2D arcRect = DialPlot.rectangleByRadius(frame,
                this.tickRadius, this.tickRadius);
        Rectangle2D arcRectMajor = DialPlot.rectangleByRadius(frame,
                this.tickRadius - this.majorTickLength,
                this.tickRadius - this.majorTickLength);
        Rectangle2D arcRectMinor = arcRect;
        if (this.minorTickCount > 0 && this.minorTickLength > 0.0) {
            arcRectMinor = DialPlot.rectangleByRadius(frame,
                    this.tickRadius - this.minorTickLength,
                    this.tickRadius - this.minorTickLength);
        }
        Rectangle2D arcRectForLabels = DialPlot.rectangleByRadius(frame,
                this.tickRadius - this.tickLabelOffset,
                this.tickRadius - this.tickLabelOffset);

        boolean firstLabel = true;

        Arc2D arc = new Arc2D.Double();
        Line2D workingLine = new Line2D.Double();
        for (double v = this.lowerBound; v <= this.upperBound;
                v += this.majorTickIncrement) {
            arc.setArc(arcRect, this.startAngle, valueToAngle(v)
                    - this.startAngle, Arc2D.OPEN);
            Point2D pt0 = arc.getEndPoint();
            arc.setArc(arcRectMajor, this.startAngle, valueToAngle(v)
                    - this.startAngle, Arc2D.OPEN);
            Point2D pt1 = arc.getEndPoint();
            g2.setPaint(this.majorTickPaint);
            g2.setStroke(this.majorTickStroke);
            workingLine.setLine(pt0, pt1);
            g2.draw(workingLine);
            arc.setArc(arcRectForLabels, this.startAngle, valueToAngle(v)
                    - this.startAngle, Arc2D.OPEN);
            Point2D pt2 = arc.getEndPoint();

            if (this.tickLabelsVisible) {
                if (!firstLabel || this.firstTickLabelVisible) {
                    g2.setFont(this.tickLabelFont);
                    g2.setPaint(this.tickLabelPaint);
                    TextUtils.drawAlignedString(
                            this.tickLabelFormatter.format(v), g2,
                            (float) pt2.getX(), (float) pt2.getY(),
                            TextAnchor.CENTER);
                }
            }
            firstLabel = false;

            // now do the minor tick marks
            if (this.minorTickCount > 0 && this.minorTickLength > 0.0) {
                double minorTickIncrement = this.majorTickIncrement
                        / (this.minorTickCount + 1);
                for (int i = 0; i < this.minorTickCount; i++) {
                    double vv = v + ((i + 1) * minorTickIncrement);
                    if (vv >= this.upperBound) {
                        break;
                    }
                    double angle = valueToAngle(vv);

                    arc.setArc(arcRect, this.startAngle, angle
                            - this.startAngle, Arc2D.OPEN);
                    pt0 = arc.getEndPoint();
                    arc.setArc(arcRectMinor, this.startAngle, angle
                            - this.startAngle, Arc2D.OPEN);
                    Point2D pt3 = arc.getEndPoint();
                    g2.setStroke(this.minorTickStroke);
                    g2.setPaint(this.minorTickPaint);
                    workingLine.setLine(pt0, pt3);
                    g2.draw(workingLine);
                }
            }

        }
    }

    
    @Override
    public double valueToAngle(double value) {
        double range = this.upperBound - this.lowerBound;
        double unit = this.extent / range;
        return this.startAngle + unit * (value - this.lowerBound);
    }

    
    @Override
    public double angleToValue(double angle) {
        double range = this.upperBound - this.lowerBound;
        double unit = range / this.extent;
        return (angle - this.startAngle) * unit;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardDialScale)) {
            return false;
        }
        StandardDialScale that = (StandardDialScale) obj;
        if (this.lowerBound != that.lowerBound) {
            return false;
        }
        if (this.upperBound != that.upperBound) {
            return false;
        }
        if (this.startAngle != that.startAngle) {
            return false;
        }
        if (this.extent != that.extent) {
            return false;
        }
        if (this.tickRadius != that.tickRadius) {
            return false;
        }
        if (this.majorTickIncrement != that.majorTickIncrement) {
            return false;
        }
        if (this.majorTickLength != that.majorTickLength) {
            return false;
        }
        if (!PaintUtils.equal(this.majorTickPaint, that.majorTickPaint)) {
            return false;
        }
        if (!this.majorTickStroke.equals(that.majorTickStroke)) {
            return false;
        }
        if (this.minorTickCount != that.minorTickCount) {
            return false;
        }
        if (this.minorTickLength != that.minorTickLength) {
            return false;
        }
        if (!PaintUtils.equal(this.minorTickPaint, that.minorTickPaint)) {
            return false;
        }
        if (!this.minorTickStroke.equals(that.minorTickStroke)) {
            return false;
        }
        if (this.tickLabelsVisible != that.tickLabelsVisible) {
            return false;
        }
        if (this.tickLabelOffset != that.tickLabelOffset) {
            return false;
        }
        if (!this.tickLabelFont.equals(that.tickLabelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int result = 193;
        // lowerBound
        long temp = Double.doubleToLongBits(this.lowerBound);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // upperBound
        temp = Double.doubleToLongBits(this.upperBound);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // startAngle
        temp = Double.doubleToLongBits(this.startAngle);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // extent
        temp = Double.doubleToLongBits(this.extent);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // tickRadius
        temp = Double.doubleToLongBits(this.tickRadius);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        // majorTickIncrement
        // majorTickLength
        // majorTickPaint
        // majorTickStroke
        // minorTickCount
        // minorTickLength
        // minorTickPaint
        // minorTickStroke
        // tickLabelOffset
        // tickLabelFont
        // tickLabelsVisible
        // tickLabelFormatter
        // firstTickLabelsVisible
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.majorTickPaint, stream);
        SerialUtils.writeStroke(this.majorTickStroke, stream);
        SerialUtils.writePaint(this.minorTickPaint, stream);
        SerialUtils.writeStroke(this.minorTickStroke, stream);
        SerialUtils.writePaint(this.tickLabelPaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.majorTickPaint = SerialUtils.readPaint(stream);
        this.majorTickStroke = SerialUtils.readStroke(stream);
        this.minorTickPaint = SerialUtils.readPaint(stream);
        this.minorTickStroke = SerialUtils.readStroke(stream);
        this.tickLabelPaint = SerialUtils.readPaint(stream);
    }

}
