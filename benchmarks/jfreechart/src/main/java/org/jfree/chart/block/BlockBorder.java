

package org.jfree.chart.block;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;


public class BlockBorder implements BlockFrame, Serializable {

    
    private static final long serialVersionUID = 4961579220410228283L;

    
    public static final BlockBorder NONE = new BlockBorder(
            RectangleInsets.ZERO_INSETS, Color.WHITE);

    
    private final RectangleInsets insets;

    
    private transient Paint paint;

    
    public BlockBorder() {
        this(Color.BLACK);
    }

    
    public BlockBorder(Paint paint) {
        this(new RectangleInsets(1, 1, 1, 1), paint);
    }

    
    public BlockBorder(double top, double left, double bottom, double right) {
        this(new RectangleInsets(top, left, bottom, right), Color.BLACK);
    }

    
    public BlockBorder(double top, double left, double bottom, double right,
                       Paint paint) {
        this(new RectangleInsets(top, left, bottom, right), paint);
    }

    
    public BlockBorder(RectangleInsets insets, Paint paint) {
        Args.nullNotPermitted(insets, "insets");
        Args.nullNotPermitted(paint, "paint");
        this.insets = insets;
        this.paint = paint;
    }

    
    @Override
    public RectangleInsets getInsets() {
        return this.insets;
    }

    
    public Paint getPaint() {
        return this.paint;
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        // this default implementation will just fill the available
        // border space with a single color
        double t = this.insets.calculateTopInset(area.getHeight());
        double b = this.insets.calculateBottomInset(area.getHeight());
        double l = this.insets.calculateLeftInset(area.getWidth());
        double r = this.insets.calculateRightInset(area.getWidth());
        double x = area.getX();
        double y = area.getY();
        double w = area.getWidth();
        double h = area.getHeight();
        g2.setPaint(this.paint);
        Rectangle2D rect = new Rectangle2D.Double();
        if (t > 0.0) {
            rect.setRect(x, y, w, t);
            g2.fill(rect);
        }
        if (b > 0.0) {
            rect.setRect(x, y + h - b, w, b);
            g2.fill(rect);
        }
        if (l > 0.0) {
            rect.setRect(x, y, l, h);
            g2.fill(rect);
        }
        if (r > 0.0) {
            rect.setRect(x + w - r, y, r, h);
            g2.fill(rect);
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BlockBorder)) {
            return false;
        }
        BlockBorder that = (BlockBorder) obj;
        if (!this.insets.equals(that.insets)) {
            return false;
        }
        if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
        }
        return true;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.paint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtils.readPaint(stream);
    }

}
