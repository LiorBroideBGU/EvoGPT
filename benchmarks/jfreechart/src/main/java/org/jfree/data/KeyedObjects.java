

package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;


public class KeyedObjects<K extends Comparable<K>> implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 1321582394193530984L;

    
    private List<KeyedObject<K>> data;

    
    public KeyedObjects() {
        this.data = new ArrayList<>();
    }

    
    public int getItemCount() {
        return this.data.size();
    }

    
    public Object getObject(int item) {
        Object result = null;
        KeyedObject kobj = this.data.get(item);
        if (kobj != null) {
            result = kobj.getObject();
        }
        return result;
    }

    
    public K getKey(int index) {
        K result = null;
        KeyedObject<K> item = this.data.get(index);
        if (item != null) {
            result = item.getKey();
        }
        return result;
    }

    
    public int getIndex(K key) {
        Args.nullNotPermitted(key, "key");
        int i = 0;
        for (KeyedObject ko : this.data) {
            if (ko.getKey().equals(key)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    
    public List<K> getKeys() {
        List<K> result = new ArrayList<>();
        for (KeyedObject<K> ko : this.data) {
            result.add(ko.getKey());
        }
        return result;
    }

    
    public Object getObject(K key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("The key (" + key
                    + ") is not recognised.");
        }
        return getObject(index);
    }

    
    public void addObject(K key, Object object) {
        setObject(key, object);
    }

    
    public void setObject(K key, Object object) {
        int keyIndex = getIndex(key);
        if (keyIndex >= 0) {
            KeyedObject ko = this.data.get(keyIndex);
            ko.setObject(object);
        }
        else {
            KeyedObject ko = new KeyedObject(key, object);
            this.data.add(ko);
        }
    }

    
    public void insertValue(int position, K key, Object value) {
        if (position < 0 || position > this.data.size()) {
            throw new IllegalArgumentException("'position' out of bounds.");
        }
        Args.nullNotPermitted(key, "key");
        int pos = getIndex(key);
        if (pos >= 0) {
            this.data.remove(pos);
        }
        KeyedObject item = new KeyedObject(key, value);
        if (position <= this.data.size()) {
            this.data.add(position, item);
        } else {
            this.data.add(item);
        }
    }

    
    public void removeValue(int index) {
        this.data.remove(index);
    }

    
    public void removeValue(K key) {
        // defer argument checking
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("The key (" + key.toString()
                    + ") is not recognised.");
        }
        removeValue(index);
    }

    
    public void clear() {
        this.data.clear();
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        KeyedObjects clone = (KeyedObjects) super.clone();
        clone.data = new ArrayList();
        for (KeyedObject ko : this.data) {
            clone.data.add((KeyedObject) ko.clone());
        }
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyedObjects)) {
            return false;
        }
        KeyedObjects that = (KeyedObjects) obj;
        int count = getItemCount();
        if (count != that.getItemCount()) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            Comparable k1 = getKey(i);
            Comparable k2 = that.getKey(i);
            if (!k1.equals(k2)) {
                return false;
            }
            Object o1 = getObject(i);
            Object o2 = that.getObject(i);
            if (o1 == null) {
                if (o2 != null) {
                    return false;
                }
            }
            else {
                if (!o1.equals(o2)) {
                    return false;
                }
            }
        }
        return true;

    }

    
    @Override
    public int hashCode() {
        return (this.data != null ? this.data.hashCode() : 0);
    }

}
