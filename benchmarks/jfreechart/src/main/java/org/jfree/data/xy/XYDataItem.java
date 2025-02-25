

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.internal.Args;


public class XYDataItem implements Cloneable, Comparable<XYDataItem>, Serializable {

    
    private static final long serialVersionUID = 2751513470325494890L;

    
    private Number x;

    
    private Number y;

    
    public XYDataItem(Number x, Number y) {
        Args.nullNotPermitted(x, "x");
        this.x = x;
        this.y = y;
    }

    
    public XYDataItem(double x, double y) {
        this(Double.valueOf(x), Double.valueOf(y));
    }

    
    public Number getX() {
        return this.x;
    }

    
    public double getXValue() {
        // this.x is not allowed to be null...
        return this.x.doubleValue();
    }

    
    public Number getY() {
        return this.y;
    }

    
    public double getYValue() {
        double result = Double.NaN;
        if (this.y != null) {
            result = this.y.doubleValue();
        }
        return result;
    }

    
    public void setY(double y) {
        setY(Double.valueOf(y));
    }

    
    public void setY(Number y) {
        this.y = y;
    }

    
    @Override
    public int compareTo(XYDataItem other) {
        int result;
        double compare = this.x.doubleValue() - other.getX().doubleValue();
        if (compare > 0.0) {
            result = 1;
        } else {
            if (compare < 0.0) {
                result = -1;
            } else {
                result = 0;
            }
        }
        return result;
    }

    
    @Override
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        }
        catch (CloneNotSupportedException e) { // won't get here...
            e.printStackTrace();
        }
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYDataItem)) {
            return false;
        }
        XYDataItem that = (XYDataItem) obj;
        if (!this.x.equals(that.x)) {
            return false;
        }
        if (!Objects.equals(this.y, that.y)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = this.x.hashCode();
        result = 29 * result + (this.y != null ? this.y.hashCode() : 0);
        return result;
    }

    
    @Override
    public String toString() {
        return "[" + getXValue() + ", " + getYValue() + "]";
    }

}
