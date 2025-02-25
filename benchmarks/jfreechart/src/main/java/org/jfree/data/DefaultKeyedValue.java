

package org.jfree.data;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;


public class DefaultKeyedValue<K extends Comparable<K>> 
        implements KeyedValue<K>, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -7388924517460437712L;

    
    private final K key;

    
    private Number value;

    
    public DefaultKeyedValue(K key, Number value) {
        Args.nullNotPermitted(key, "key");
        this.key = key;
        this.value = value;
    }

    
    @Override
    public K getKey() {
        return this.key;
    }

    
    @Override
    public Number getValue() {
        return this.value;
    }

    
    public synchronized void setValue(Number value) {
        this.value = value;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultKeyedValue)) {
            return false;
        }
        DefaultKeyedValue<K> that = (DefaultKeyedValue) obj;
        if (!this.key.equals(that.key)) {
            return false;
        }
        return Objects.equals(this.value, that.value);
    }

    
    @Override
    public int hashCode() {
        int result;
        result = (this.key != null ? this.key.hashCode() : 0);
        result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return (DefaultKeyedValue) super.clone();
    }

    
    @Override
    public String toString() {
        return "(" + this.key.toString() + ", " + this.value.toString() + ")";
    }

}
