

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.data.ItemKey;


public class XYItemKey<S extends Comparable<S>> implements ItemKey, 
        Comparable<XYItemKey<S>>, Serializable {
    
    
    private final S seriesKey;
    
    
    private final int itemIndex;
    
    
    public XYItemKey(S seriesKey, int itemIndex) {
        Args.nullNotPermitted(seriesKey, "seriesKey");
        this.seriesKey = seriesKey;
        this.itemIndex = itemIndex;
    }
    
    
    public S getSeriesKey() {
        return this.seriesKey;
    }
    
    
    public int getItemIndex() {
        return this.itemIndex;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYItemKey)) {
            return false;
        }
        XYItemKey that = (XYItemKey) obj;
        if (!this.seriesKey.equals(that.seriesKey)) {
            return false;
        }
        if (this.itemIndex != that.itemIndex) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.seriesKey);
        hash = 41 * hash + this.itemIndex;
        return hash;
    }

    @Override
    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"seriesKey\": \"").append(this.seriesKey.toString());
        sb.append("\", ");
        sb.append("\"itemIndex\": ").append(this.itemIndex).append("}");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("XYItemKey[seriesKey=");
        sb.append(this.seriesKey.toString()).append(",item=");
        sb.append(itemIndex);
        sb.append("]");
        return sb.toString();
    }

    @Override
    public int compareTo(XYItemKey<S> key) {
        int result = this.seriesKey.compareTo(key.seriesKey);
        if (result == 0) {
            result = this.itemIndex - key.itemIndex;
        }
        return result;
    }

}

