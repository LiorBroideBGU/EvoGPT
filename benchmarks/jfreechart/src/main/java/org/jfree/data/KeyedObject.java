

package org.jfree.data;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.api.PublicCloneable;


public class KeyedObject<K extends Comparable<K>> implements Cloneable, 
        PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 2677930479256885863L;

    
    private K key;

    
    private Object object;

    
    public KeyedObject(K key, Object object) {
        this.key = key;
        this.object = object;
    }

    
    public K getKey() {
        return this.key;
    }

    
    public Object getObject() {
        return this.object;
    }

    
    public void setObject(Object object) {
        this.object = object;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        KeyedObject clone = (KeyedObject) super.clone();
        if (this.object instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.object;
            clone.object = pc.clone();
        }
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }

        if (!(obj instanceof KeyedObject)) {
            return false;
        }
        KeyedObject that = (KeyedObject) obj;
        if (!Objects.equals(this.key, that.key)) {
            return false;
        }

        if (!Objects.equals(this.object, that.object)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.key);
        hash = 47 * hash + Objects.hashCode(this.object);
        return hash;
    }

}
