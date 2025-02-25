

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;


public class AxisEntity extends ChartEntity {

    
    private static final long serialVersionUID = -4445994133561919083L;
                  //same as for ChartEntity!

    
    private final Axis axis;

    
    public AxisEntity(Shape area, Axis axis) {
        // defer argument checks...
        this(area, axis, null);
    }

    
    public AxisEntity(Shape area, Axis axis, String toolTipText) {
        // defer argument checks...
        this(area, axis, toolTipText, null);
    }

    
    public AxisEntity(Shape area, Axis axis, String toolTipText,
            String urlText) {
        super(area, toolTipText, urlText);
        Args.nullNotPermitted(axis, "axis");
        this.axis = axis;
    }

    
    public Axis getAxis() {
        return this.axis;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("AxisEntity: ");
        sb.append("tooltip = ");
        sb.append(getToolTipText());
        return sb.toString();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AxisEntity)) {
            return false;
        }
        AxisEntity that = (AxisEntity) obj;
        if (!getArea().equals(that.getArea())) {
            return false;
        }
        if (!Objects.equals(getToolTipText(), that.getToolTipText())) {
            return false;
        }
        if (!Objects.equals(getURLText(), that.getURLText())) {
            return false;
        }
        if (!(this.axis.equals(that.axis))) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = 39;
        result = HashUtils.hashCode(result, getToolTipText());
        result = HashUtils.hashCode(result, getURLText());
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeShape(getArea(), stream);
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        setArea(SerialUtils.readShape(stream));
    }

}