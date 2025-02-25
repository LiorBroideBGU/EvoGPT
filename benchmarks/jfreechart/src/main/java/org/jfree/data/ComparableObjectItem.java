

package org.jfree.data;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.internal.Args;


public class ComparableObjectItem implements Comparable<ComparableObjectItem>, 
        Cloneable, Serializable {

    
    private static final long serialVersionUID = 2751513470325494890L;

    
    private Comparable x;

    
    private Object obj;

    
    public ComparableObjectItem(Comparable x, Object y) {
        Args.nullNotPermitted(x, "x");
        this.x = x;
        this.obj = y;
    }

    
    protected Comparable getComparable() {
        return this.x;
    }

    
    protected Object getObject() {
        return this.obj;
    }

    
    protected void setObject(Object y) {
        this.obj = y;
    }

    
    @Override
    public int compareTo(ComparableObjectItem other) {
        return this.x.compareTo(other.getComparable());
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ComparableObjectItem)) {
            return false;
        }
        ComparableObjectItem that = (ComparableObjectItem) obj;
        if (!this.x.equals(that.x)) {
            return false;
        }
        if (!Objects.equals(this.obj, that.obj)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = this.x.hashCode();
        result = 29 * result + (this.obj != null ? this.obj.hashCode() : 0);
        return result;
    }

}
