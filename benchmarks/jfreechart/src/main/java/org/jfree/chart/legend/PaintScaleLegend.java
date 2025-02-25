

package org.jfree.chart.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.event.AxisChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.title.Title;
import org.jfree.data.Range;


public class PaintScaleLegend extends Title implements AxisChangeListener,
        PublicCloneable {

    
    static final long serialVersionUID = -1365146490993227503L;

    
    private PaintScale scale;

    
    private ValueAxis axis;

    
    private AxisLocation axisLocation;

    
    private double axisOffset;

    
    private double stripWidth;

    
    private boolean stripOutlineVisible;

    
    private transient Paint stripOutlinePaint;

    
    private transient Stroke stripOutlineStroke;

    
    private transient Paint backgroundPaint;

    
    private int subdivisions;

    
    public PaintScaleLegend(PaintScale scale, ValueAxis axis) {
        Args.nullNotPermitted(axis, "axis");
        this.scale = scale;
        this.axis = axis;
        this.axis.addChangeListener(this);
        this.axisLocation = AxisLocation.BOTTOM_OR_LEFT;
        this.axisOffset = 0.0;
        this.axis.setRange(scale.getLowerBound(), scale.getUpperBound());
        this.stripWidth = 15.0;
        this.stripOutlineVisible = true;
        this.stripOutlinePaint = Color.GRAY;
        this.stripOutlineStroke = new BasicStroke(0.5f);
        this.backgroundPaint = Color.WHITE;
        this.subdivisions = 100;
    }

    
    public PaintScale getScale() {
        return this.scale;
    }

    
    public void setScale(PaintScale scale) {
        Args.nullNotPermitted(scale, "scale");
        this.scale = scale;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public ValueAxis getAxis() {
        return this.axis;
    }

    
    public void setAxis(ValueAxis axis) {
        Args.nullNotPermitted(axis, "axis");
        this.axis.removeChangeListener(this);
        this.axis = axis;
        this.axis.addChangeListener(this);
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public AxisLocation getAxisLocation() {
        return this.axisLocation;
    }

    
    public void setAxisLocation(AxisLocation location) {
        Args.nullNotPermitted(location, "location");
        this.axisLocation = location;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public double getAxisOffset() {
        return this.axisOffset;
    }

    
    public void setAxisOffset(double offset) {
        this.axisOffset = offset;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public double getStripWidth() {
        return this.stripWidth;
    }

    
    public void setStripWidth(double width) {
        this.stripWidth = width;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public boolean isStripOutlineVisible() {
        return this.stripOutlineVisible;
    }

    
    public void setStripOutlineVisible(boolean visible) {
        this.stripOutlineVisible = visible;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public Paint getStripOutlinePaint() {
        return this.stripOutlinePaint;
    }

    
    public void setStripOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.stripOutlinePaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public Stroke getStripOutlineStroke() {
        return this.stripOutlineStroke;
    }

    
    public void setStripOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.stripOutlineStroke = stroke;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    
    public void setBackgroundPaint(Paint paint) {
        this.backgroundPaint = paint;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    public int getSubdivisionCount() {
        return this.subdivisions;
    }

    
    public void setSubdivisionCount(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Requires 'count' > 0.");
        }
        this.subdivisions = count;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    @Override
    public void axisChanged(AxisChangeEvent event) {
        if (this.axis == event.getAxis()) {
            notifyListeners(new TitleChangeEvent(this));
        }
    }

    
    @Override
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        RectangleConstraint cc = toContentConstraint(constraint);
        LengthConstraintType w = cc.getWidthConstraintType();
        LengthConstraintType h = cc.getHeightConstraintType();
        Size2D contentSize = null;
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = new Size2D(getWidth(), getHeight());
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeRR(g2, cc.getWidthRange(),
                        cc.getHeightRange());
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        assert contentSize != null; // suppress compiler warning
        return new Size2D(calculateTotalWidth(contentSize.getWidth()),
                calculateTotalHeight(contentSize.getHeight()));
    }

    
    protected Size2D arrangeRR(Graphics2D g2, Range widthRange,
            Range heightRange) {

        RectangleEdge position = getPosition();
        if (position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {


            float maxWidth = (float) widthRange.getUpperBound();

            // determine the space required for the axis
            AxisSpace space = this.axis.reserveSpace(g2, null,
                    new Rectangle2D.Double(0, 0, maxWidth, 100),
                    RectangleEdge.BOTTOM, null);

            return new Size2D(maxWidth, this.stripWidth + this.axisOffset
                    + space.getTop() + space.getBottom());
        }
        else if (position == RectangleEdge.LEFT || position
                == RectangleEdge.RIGHT) {
            float maxHeight = (float) heightRange.getUpperBound();
            AxisSpace space = this.axis.reserveSpace(g2, null,
                    new Rectangle2D.Double(0, 0, 100, maxHeight),
                    RectangleEdge.RIGHT, null);
            return new Size2D(this.stripWidth + this.axisOffset
                    + space.getLeft() + space.getRight(), maxHeight);
        }
        else {
            throw new RuntimeException("Unrecognised position.");
        }
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        draw(g2, area, null);
    }

    
    @Override
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        Rectangle2D target = (Rectangle2D) area.clone();
        target = trimMargin(target);
        if (this.backgroundPaint != null) {
            g2.setPaint(this.backgroundPaint);
            g2.fill(target);
        }
        getFrame().draw(g2, target);
        getFrame().getInsets().trim(target);
        target = trimPadding(target);
        double base = this.axis.getLowerBound();
        double increment = this.axis.getRange().getLength() / this.subdivisions;
        Rectangle2D r = new Rectangle2D.Double();

        if (RectangleEdge.isTopOrBottom(getPosition())) {
            RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(
                    this.axisLocation, PlotOrientation.HORIZONTAL);
            if (axisEdge == RectangleEdge.TOP) {
                for (int i = 0; i < this.subdivisions; i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = this.axis.valueToJava2D(v, target,
                            RectangleEdge.TOP);
                    double vv1 = this.axis.valueToJava2D(v + increment, target,
                            RectangleEdge.TOP);
                    double ww = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(Math.min(vv0, vv1), target.getMaxY()
                            - this.stripWidth, ww, this.stripWidth);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(this.stripOutlinePaint);
                    g2.setStroke(this.stripOutlineStroke);
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMaxY() - this.stripWidth,
                            target.getWidth(), this.stripWidth));
                }
                this.axis.draw(g2, target.getMaxY() - this.stripWidth
                        - this.axisOffset, target, target, RectangleEdge.TOP,
                        null);
            }
            else if (axisEdge == RectangleEdge.BOTTOM) {
                for (int i = 0; i < this.subdivisions; i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = this.axis.valueToJava2D(v, target,
                            RectangleEdge.BOTTOM);
                    double vv1 = this.axis.valueToJava2D(v + increment, target,
                            RectangleEdge.BOTTOM);
                    double ww = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(Math.min(vv0, vv1), target.getMinY(), ww,
                            this.stripWidth);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(this.stripOutlinePaint);
                    g2.setStroke(this.stripOutlineStroke);
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMinY(), target.getWidth(),
                            this.stripWidth));
                }
                this.axis.draw(g2, target.getMinY() + this.stripWidth
                        + this.axisOffset, target, target,
                        RectangleEdge.BOTTOM, null);
            }
        }
        else {
            RectangleEdge axisEdge = Plot.resolveRangeAxisLocation(
                    this.axisLocation, PlotOrientation.VERTICAL);
            if (axisEdge == RectangleEdge.LEFT) {
                for (int i = 0; i < this.subdivisions; i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = this.axis.valueToJava2D(v, target,
                            RectangleEdge.LEFT);
                    double vv1 = this.axis.valueToJava2D(v + increment, target,
                            RectangleEdge.LEFT);
                    double hh = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(target.getMaxX() - this.stripWidth,
                            Math.min(vv0, vv1), this.stripWidth, hh);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(this.stripOutlinePaint);
                    g2.setStroke(this.stripOutlineStroke);
                    g2.draw(new Rectangle2D.Double(target.getMaxX()
                            - this.stripWidth, target.getMinY(), 
                            this.stripWidth, target.getHeight()));
                }
                this.axis.draw(g2, target.getMaxX() - this.stripWidth
                        - this.axisOffset, target, target, RectangleEdge.LEFT,
                        null);
            }
            else if (axisEdge == RectangleEdge.RIGHT) {
                for (int i = 0; i < this.subdivisions; i++) {
                    double v = base + (i * increment);
                    Paint p = this.scale.getPaint(v);
                    double vv0 = this.axis.valueToJava2D(v, target,
                            RectangleEdge.LEFT);
                    double vv1 = this.axis.valueToJava2D(v + increment, target,
                            RectangleEdge.LEFT);
                    double hh = Math.abs(vv1 - vv0) + 1.0;
                    r.setRect(target.getMinX(), Math.min(vv0, vv1),
                            this.stripWidth, hh);
                    g2.setPaint(p);
                    g2.fill(r);
                }
                if (isStripOutlineVisible()) {
                    g2.setPaint(this.stripOutlinePaint);
                    g2.setStroke(this.stripOutlineStroke);
                    g2.draw(new Rectangle2D.Double(target.getMinX(),
                            target.getMinY(), this.stripWidth,
                            target.getHeight()));
                }
                this.axis.draw(g2, target.getMinX() + this.stripWidth
                        + this.axisOffset, target, target, RectangleEdge.RIGHT,
                        null);
            }
        }
        return null;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PaintScaleLegend)) {
            return false;
        }
        PaintScaleLegend that = (PaintScaleLegend) obj;
        if (!this.scale.equals(that.scale)) {
            return false;
        }
        if (!this.axis.equals(that.axis)) {
            return false;
        }
        if (!this.axisLocation.equals(that.axisLocation)) {
            return false;
        }
        if (this.axisOffset != that.axisOffset) {
            return false;
        }
        if (this.stripWidth != that.stripWidth) {
            return false;
        }
        if (this.stripOutlineVisible != that.stripOutlineVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.stripOutlinePaint,
                that.stripOutlinePaint)) {
            return false;
        }
        if (!this.stripOutlineStroke.equals(that.stripOutlineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.backgroundPaint, that.backgroundPaint)) {
            return false;
        }
        if (this.subdivisions != that.subdivisions) {
            return false;
        }
        return super.equals(obj);
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.backgroundPaint, stream);
        SerialUtils.writePaint(this.stripOutlinePaint, stream);
        SerialUtils.writeStroke(this.stripOutlineStroke, stream);
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.backgroundPaint = SerialUtils.readPaint(stream);
        this.stripOutlinePaint = SerialUtils.readPaint(stream);
        this.stripOutlineStroke = SerialUtils.readStroke(stream);
    }

}
