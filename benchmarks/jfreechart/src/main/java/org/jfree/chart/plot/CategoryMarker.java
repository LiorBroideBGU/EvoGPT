

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.event.MarkerChangeEvent;
import org.jfree.chart.api.LengthAdjustmentType;
import org.jfree.chart.internal.Args;


public class CategoryMarker extends Marker implements Cloneable, Serializable {

    
    private Comparable<?> key;

    
    private boolean drawAsLine = false;

    
    public CategoryMarker(Comparable<?> key) {
        this(key, Color.GRAY, new BasicStroke(1.0f));
    }

    
    public CategoryMarker(Comparable<?> key, Paint paint, Stroke stroke) {
        this(key, paint, stroke, paint, stroke, 1.0f);
    }

    
    public CategoryMarker(Comparable<?> key, Paint paint, Stroke stroke,
                          Paint outlinePaint, Stroke outlineStroke,
                          float alpha) {
        super(paint, stroke, outlinePaint, outlineStroke, alpha);
        this.key = key;
        setLabelOffsetType(LengthAdjustmentType.EXPAND);
    }

    
    public Comparable<?> getKey() {
        return this.key;
    }

    
    public void setKey(Comparable<?> key) {
        Args.nullNotPermitted(key, "key");
        this.key = key;
        notifyListeners(new MarkerChangeEvent(this));
    }

    
    public boolean getDrawAsLine() {
        return this.drawAsLine;
    }

    
    public void setDrawAsLine(boolean drawAsLine) {
        this.drawAsLine = drawAsLine;
        notifyListeners(new MarkerChangeEvent(this));
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CategoryMarker)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CategoryMarker that = (CategoryMarker) obj;
        if (!this.key.equals(that.key)) {
            return false;
        }
        if (this.drawAsLine != that.drawAsLine) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.key);
        hash = 89 * hash + (this.drawAsLine ? 1 : 0);
        return hash;
    }

}
