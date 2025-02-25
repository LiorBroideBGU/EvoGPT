

package org.jfree.chart.block;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;


public class LineBorder implements BlockFrame, Serializable {

    
    static final long serialVersionUID = 4630356736707233924L;

    
    private transient Paint paint;

    
    private transient Stroke stroke;

    
    private final RectangleInsets insets;

    
    public LineBorder() {
        this(Color.BLACK, new BasicStroke(1.0f), new RectangleInsets(1.0, 1.0,
                1.0, 1.0));
    }

    
    public LineBorder(Paint paint, Stroke stroke, RectangleInsets insets) {
        Args.nullNotPermitted(paint, "paint");
        Args.nullNotPermitted(stroke, "stroke");
        Args.nullNotPermitted(insets, "insets");
        this.paint = paint;
        this.stroke = stroke;
        this.insets = insets;
    }

    
    public Paint getPaint() {
        return this.paint;
    }

    
    @Override
    public RectangleInsets getInsets() {
        return this.insets;
    }

    
    public Stroke getStroke() {
        return this.stroke;
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        double w = area.getWidth();
        double h = area.getHeight();
        // if the area has zero height or width, we shouldn't draw anything
        if (w <= 0.0 || h <= 0.0) {
            return;
        }
        double t = this.insets.calculateTopInset(h);
        double b = this.insets.calculateBottomInset(h);
        double l = this.insets.calculateLeftInset(w);
        double r = this.insets.calculateRightInset(w);
        double x = area.getX();
        double y = area.getY();
        double x0 = x + l / 2.0;
        double x1 = x + w - r / 2.0;
        double y0 = y + h - b / 2.0;
        double y1 = y + t / 2.0;
        g2.setPaint(getPaint());
        g2.setStroke(getStroke());
        Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_NORMALIZE);
        Line2D line = new Line2D.Double();
        if (t > 0.0) {
            line.setLine(x0, y1, x1, y1);
            g2.draw(line);
        }
        if (b > 0.0) {
            line.setLine(x0, y0, x1, y0);
            g2.draw(line);
        }
        if (l > 0.0) {
            line.setLine(x0, y0, x0, y1);
            g2.draw(line);
        }
        if (r > 0.0) {
            line.setLine(x1, y0, x1, y1);
            g2.draw(line);
        }
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LineBorder)) {
            return false;
        }
        LineBorder that = (LineBorder) obj;
        if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
        }
        if (!Objects.equals(this.stroke, that.stroke)) {
            return false;
        }
        if (!this.insets.equals(that.insets)) {
            return false;
        }
        return true;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.paint, stream);
        SerialUtils.writeStroke(this.stroke, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtils.readPaint(stream);
        this.stroke = SerialUtils.readStroke(stream);
    }
}


