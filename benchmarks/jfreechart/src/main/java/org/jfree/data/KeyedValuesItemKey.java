

package org.jfree.data;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.internal.Args;


public class KeyedValuesItemKey<K extends Comparable<K>> implements ItemKey, 
        Serializable {
    
    
    K key;
    
    
    public KeyedValuesItemKey(K key) {
        Args.nullNotPermitted(key, "key");
        this.key = key;
    }
    
    
    public K getKey() {
        return this.key;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyedValuesItemKey)) {
            return false;
        }
        KeyedValuesItemKey that = (KeyedValuesItemKey) obj;
        if (!this.key.equals(that.key)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 23 * hash + Objects.hashCode(this.key);
        return hash;
    }
    
    @Override
    public String toJSONString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"key\": \"").append(this.key.toString()).append("\"}");
        return sb.toString();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("KeyedValuesItemKey[");
        sb.append(this.key.toString());
        sb.append("]");
        return sb.toString();
    }
}
