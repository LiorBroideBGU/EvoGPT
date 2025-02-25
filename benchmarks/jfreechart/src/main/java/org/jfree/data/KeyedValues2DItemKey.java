

package org.jfree.data;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.internal.Args;


public class KeyedValues2DItemKey<R extends Comparable<R>, 
        C extends Comparable<C>> implements ItemKey, 
        Comparable<KeyedValues2DItemKey<R, C>>, Serializable {
    
    
    R rowKey;
    
    
    C columnKey;
    
    
    public KeyedValues2DItemKey(R rowKey, C columnKey) {
        Args.nullNotPermitted(rowKey, "rowKey");
        Args.nullNotPermitted(columnKey, "columnKey");
        this.rowKey = rowKey;
        this.columnKey = columnKey;
    }
    
    
    public R getRowKey() {
        return this.rowKey;
    }
    
    
    public C getColumnKey() {
        return this.columnKey;
    }
    
    @Override
    public int compareTo(KeyedValues2DItemKey<R, C> key) {
        int result = this.rowKey.compareTo(key.rowKey);
        if (result == 0) {
            result = this.columnKey.compareTo(key.columnKey);
        }
        return result;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyedValues2DItemKey)) {
            return false;
        }
        KeyedValues2DItemKey that = (KeyedValues2DItemKey) obj;
        if (!this.rowKey.equals(that.rowKey)) {
            return false;
        }
        if (!this.columnKey.equals(that.columnKey)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.rowKey);
        hash = 17 * hash + Objects.hashCode(this.columnKey);
        return hash;
    }

    @Override
    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"rowKey\": \"").append(this.rowKey.toString());
        sb.append("\", ");
        sb.append("\"columnKey\": \"").append(this.columnKey.toString());
        sb.append("\"}");
        return sb.toString();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Values2DItemKey[row=");
        sb.append(rowKey.toString()).append(",column=");
        sb.append(columnKey.toString());
        sb.append("]");
        return sb.toString();
    }

}