

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.labels.CrosshairLabelGenerator;
import org.jfree.chart.labels.StandardCrosshairLabelGenerator;
import org.jfree.chart.swing.CrosshairOverlay;
import org.jfree.chart.api.RectangleAnchor;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;


public class Crosshair implements Cloneable, PublicCloneable, Serializable {

    
    private boolean visible;

    
    private double value;

    
    private transient Paint paint;

    
    private transient Stroke stroke;

    
    private boolean labelVisible;

    
    private RectangleAnchor labelAnchor;

    
    private CrosshairLabelGenerator labelGenerator;

    
    private double labelXOffset;

    
    private double labelYOffset;

    
    private Font labelFont;

    
    private transient Paint labelPaint;

    
    private transient Paint labelBackgroundPaint;

    
    private boolean labelOutlineVisible;

    
    private transient Stroke labelOutlineStroke;

    
    private transient Paint labelOutlinePaint;

    
    private transient PropertyChangeSupport pcs;

    
    public Crosshair() {
        this(0.0);
    }

    
    public Crosshair(double value) {
       this(value, Color.BLACK, new BasicStroke(1.0f));
    }

    
    public Crosshair(double value, Paint paint, Stroke stroke) {
        Args.nullNotPermitted(paint, "paint");
        Args.nullNotPermitted(stroke, "stroke");
        this.visible = true;
        this.value = value;
        this.paint = paint;
        this.stroke = stroke;
        this.labelVisible = false;
        this.labelGenerator = new StandardCrosshairLabelGenerator();
        this.labelAnchor = RectangleAnchor.BOTTOM_LEFT;
        this.labelXOffset = 5.0;
        this.labelYOffset = 5.0;
        this.labelFont = new Font("Tahoma", Font.PLAIN, 12);
        this.labelPaint = Color.BLACK;
        this.labelBackgroundPaint = new Color(0, 0, 255, 63);
        this.labelOutlineVisible = true;
        this.labelOutlinePaint = Color.BLACK;
        this.labelOutlineStroke = new BasicStroke(0.5f);
        this.pcs = new PropertyChangeSupport(this);
    }

    
    public boolean isVisible() {
        return this.visible;
    }

    
    public void setVisible(boolean visible) {
        boolean old = this.visible;
        this.visible = visible;
        this.pcs.firePropertyChange("visible", old, visible);
    }

    
    public double getValue() {
        return this.value;
    }

    
    public void setValue(double value) {
        Double oldValue = this.value;
        this.value = value;
        this.pcs.firePropertyChange("value", oldValue, value);
    }

    
    public Paint getPaint() {
        return this.paint;
    }

    
    public void setPaint(Paint paint) {
        Paint old = this.paint;
        this.paint = paint;
        this.pcs.firePropertyChange("paint", old, paint);
    }

    
    public Stroke getStroke() {
        return this.stroke;
    }

    
    public void setStroke(Stroke stroke) {
        Stroke old = this.stroke;
        this.stroke = stroke;
        this.pcs.firePropertyChange("stroke", old, stroke);
    }

    
    public boolean isLabelVisible() {
        return this.labelVisible;
    }

    
    public void setLabelVisible(boolean visible) {
        boolean old = this.labelVisible;
        this.labelVisible = visible;
        this.pcs.firePropertyChange("labelVisible", old, visible);
    }

    
    public CrosshairLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }

    
    public void setLabelGenerator(CrosshairLabelGenerator generator) {
        Args.nullNotPermitted(generator, "generator");
        CrosshairLabelGenerator old = this.labelGenerator;
        this.labelGenerator = generator;
        this.pcs.firePropertyChange("labelGenerator", old, generator);
    }

    
    public RectangleAnchor getLabelAnchor() {
        return this.labelAnchor;
    }

    
    public void setLabelAnchor(RectangleAnchor anchor) {
        RectangleAnchor old = this.labelAnchor;
        this.labelAnchor = anchor;
        this.pcs.firePropertyChange("labelAnchor", old, anchor);
    }

    
    public double getLabelXOffset() {
        return this.labelXOffset;
    }

    
    public void setLabelXOffset(double offset) {
        Double old = this.labelXOffset;
        this.labelXOffset = offset;
        this.pcs.firePropertyChange("labelXOffset", old, offset);
    }

    
    public double getLabelYOffset() {
        return this.labelYOffset;
    }

    
    public void setLabelYOffset(double offset) {
        Double old = this.labelYOffset;
        this.labelYOffset = offset;
        this.pcs.firePropertyChange("labelYOffset", old, offset);
    }

    
    public Font getLabelFont() {
        return this.labelFont;
    }

    
    public void setLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        Font old = this.labelFont;
        this.labelFont = font;
        this.pcs.firePropertyChange("labelFont", old, font);
    }

    
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    
    public void setLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        Paint old = this.labelPaint;
        this.labelPaint = paint;
        this.pcs.firePropertyChange("labelPaint", old, paint);
    }

    
    public Paint getLabelBackgroundPaint() {
        return this.labelBackgroundPaint;
    }

    
    public void setLabelBackgroundPaint(Paint paint) {
        Paint old = this.labelBackgroundPaint;
        this.labelBackgroundPaint = paint;
        this.pcs.firePropertyChange("labelBackgroundPaint", old, paint);
    }

    
    public boolean isLabelOutlineVisible() {
        return this.labelOutlineVisible;
    }

    
    public void setLabelOutlineVisible(boolean visible) {
        boolean old = this.labelOutlineVisible;
        this.labelOutlineVisible = visible;
        this.pcs.firePropertyChange("labelOutlineVisible", old, visible);
    }

    
    public Paint getLabelOutlinePaint() {
        return this.labelOutlinePaint;
    }

    
    public void setLabelOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        Paint old = this.labelOutlinePaint;
        this.labelOutlinePaint = paint;
        this.pcs.firePropertyChange("labelOutlinePaint", old, paint);
    }

    
    public Stroke getLabelOutlineStroke() {
        return this.labelOutlineStroke;
    }

    
    public void setLabelOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        Stroke old = this.labelOutlineStroke;
        this.labelOutlineStroke = stroke;
        this.pcs.firePropertyChange("labelOutlineStroke", old, stroke);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Crosshair)) {
            return false;
        }
        Crosshair that = (Crosshair) obj;
        if (this.visible != that.visible) {
            return false;
        }
        if (this.value != that.value) {
            return false;
        }
        if (!PaintUtils.equal(this.paint, that.paint)) {
            return false;
        }
        if (!this.stroke.equals(that.stroke)) {
            return false;
        }
        if (this.labelVisible != that.labelVisible) {
            return false;
        }
        if (!this.labelGenerator.equals(that.labelGenerator)) {
            return false;
        }
        if (!this.labelAnchor.equals(that.labelAnchor)) {
            return false;
        }
        if (this.labelXOffset != that.labelXOffset) {
            return false;
        }
        if (this.labelYOffset != that.labelYOffset) {
            return false;
        }
        if (!this.labelFont.equals(that.labelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelBackgroundPaint,
                that.labelBackgroundPaint)) {
            return false;
        }
        if (this.labelOutlineVisible != that.labelOutlineVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.labelOutlinePaint,
                that.labelOutlinePaint)) {
            return false;
        }
        if (!this.labelOutlineStroke.equals(that.labelOutlineStroke)) {
            return false;
        }
        return true;  // can't find any difference
    }

    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = HashUtils.hashCode(hash, this.visible);
        hash = HashUtils.hashCode(hash, this.value);
        hash = HashUtils.hashCode(hash, this.paint);
        hash = HashUtils.hashCode(hash, this.stroke);
        hash = HashUtils.hashCode(hash, this.labelVisible);
        hash = HashUtils.hashCode(hash, this.labelAnchor);
        hash = HashUtils.hashCode(hash, this.labelGenerator);
        hash = HashUtils.hashCode(hash, this.labelXOffset);
        hash = HashUtils.hashCode(hash, this.labelYOffset);
        hash = HashUtils.hashCode(hash, this.labelFont);
        hash = HashUtils.hashCode(hash, this.labelPaint);
        hash = HashUtils.hashCode(hash, this.labelBackgroundPaint);
        hash = HashUtils.hashCode(hash, this.labelOutlineVisible);
        hash = HashUtils.hashCode(hash, this.labelOutlineStroke);
        hash = HashUtils.hashCode(hash, this.labelOutlinePaint);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        // FIXME: clone generator
        return super.clone();
    }

    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        this.pcs.addPropertyChangeListener(l);
    }

    
    public void removePropertyChangeListener(PropertyChangeListener l) {
        this.pcs.removePropertyChangeListener(l);
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.paint, stream);
        SerialUtils.writeStroke(this.stroke, stream);
        SerialUtils.writePaint(this.labelPaint, stream);
        SerialUtils.writePaint(this.labelBackgroundPaint, stream);
        SerialUtils.writeStroke(this.labelOutlineStroke, stream);
        SerialUtils.writePaint(this.labelOutlinePaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.paint = SerialUtils.readPaint(stream);
        this.stroke = SerialUtils.readStroke(stream);
        this.labelPaint = SerialUtils.readPaint(stream);
        this.labelBackgroundPaint = SerialUtils.readPaint(stream);
        this.labelOutlineStroke = SerialUtils.readStroke(stream);
        this.labelOutlinePaint = SerialUtils.readPaint(stream);
        this.pcs = new PropertyChangeSupport(this);
    }

}
