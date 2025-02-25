

package org.jfree.chart.plot.dial;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;


public class DialBackground extends AbstractDialLayer implements DialLayer,
        Cloneable, PublicCloneable, Serializable {

    
    static final long serialVersionUID = -9019069533317612375L;

    
    private transient Paint paint;

    
    private GradientPaintTransformer gradientPaintTransformer;

    
    public DialBackground() {
        this(Color.WHITE);
    }

    
    public DialBackground(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.paint = paint;
        this.gradientPaintTransformer = new StandardGradientPaintTransformer();
    }

    
    public Paint getPaint() {
        return this.paint;
    }

    
    public void setPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.paint = paint;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }

    
    public void setGradientPaintTransformer(GradientPaintTransformer t) {
        Args.nullNotPermitted(t, "t");
        this.gradientPaintTransformer = t;
        notifyListeners(new DialLayerChangeEvent(this));
    }

    
    @Override
    public boolean isClippedToWindow() {
        return true;
    }

    
    @Override
    public void draw(Graphics2D g2, DialPlot plot, Rectangle2D frame,
            Rectangle2D view) {

        Paint p = this.paint;
        if (p instanceof GradientPaint) {
            p = this.gradientPaintTransformer.transform((GradientPaint) p,
                    view);
        }
        g2.setPaint(p);
        g2.fill(view);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DialBackground)) {
            return false;
        }
        DialBackground that = (DialBackground) obj;
        if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
        }
        if (!this.gradientPaintTransformer.equals(
                that.gradientPaintTransformer)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int result = 193;
        result = 37 * result + HashUtils.hashCodeForPaint(this.paint);
        result = 37 * result + this.gradientPaintTransformer.hashCode();
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
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
