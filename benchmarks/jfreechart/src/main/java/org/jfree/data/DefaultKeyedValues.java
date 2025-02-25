

package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.api.SortOrder;


public class DefaultKeyedValues<K extends Comparable<K>> 
        implements KeyedValues<K>, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 8468154364608194797L;

    
    private List<K> keys;

    
    private List<Number> values;

    
    private Map<K, Integer> indexMap;

  
    public DefaultKeyedValues() {
        this.keys = new ArrayList<>();
        this.values = new ArrayList<>();
        this.indexMap = new HashMap<>();
    }

    
    @Override
    public int getItemCount() {
        return this.indexMap.size();
    }

    
    @Override
    public Number getValue(int item) {
        return this.values.get(item);
    }

    
    @Override
    public K getKey(int index) {
        return this.keys.get(index);
    }

    
    @Override
    public int getIndex(K key) {
        Args.nullNotPermitted(key, "key");
        final Integer i = this.indexMap.get(key);
        if (i == null) {
            return -1;  // key not found
        }
        return i;
    }

    
    @Override
    public List<K> getKeys() {
        return new ArrayList<>(this.keys);
    }

    
    @Override
    public Number getValue(K key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("Key not found: " + key);
        }
        return getValue(index);
    }

    
    public void addValue(K key, double value) {
        addValue(key, Double.valueOf(value));
    }

    
    public void addValue(K key, Number value) {
        setValue(key, value);
    }

    
    public void setValue(K key, double value) {
        setValue(key, Double.valueOf(value));
    }

    
    public void setValue(K key, Number value) {
        Args.nullNotPermitted(key, "key");
        int keyIndex = getIndex(key);
        if (keyIndex >= 0) {
            this.keys.set(keyIndex, key);
            this.values.set(keyIndex, value);
        }
        else {
            this.keys.add(key);
            this.values.add(value);
            this.indexMap.put(key, this.keys.size() - 1);
        }
    }

    
    public void insertValue(int position, K key, double value) {
        insertValue(position, key, Double.valueOf(value));
    }

    
    public void insertValue(int position, K key, Number value) {
        if (position < 0 || position > getItemCount()) {
            throw new IllegalArgumentException("'position' out of bounds.");
        }
        Args.nullNotPermitted(key, "key");
        int pos = getIndex(key);
        if (pos == position) {
            this.keys.set(pos, key);
            this.values.set(pos, value);
        }
        else {
            if (pos >= 0) {
                this.keys.remove(pos);
                this.values.remove(pos);
            }

            this.keys.add(position, key);
            this.values.add(position, value);
            rebuildIndex();
        }
    }

    
    private void rebuildIndex () {
        this.indexMap.clear();
        for (int i = 0; i < this.keys.size(); i++) {
            final K key = this.keys.get(i);
            this.indexMap.put(key, i);
        }
    }

    
    public void removeValue(int index) {
        this.keys.remove(index);
        this.values.remove(index);
        rebuildIndex();
    }

    
    public void removeValue(K key) {
        int index = getIndex(key);
        if (index < 0) {
            throw new UnknownKeyException("The key (" + key
                    + ") is not recognised.");
        }
        removeValue(index);
    }

    
    public void clear() {
        this.keys.clear();
        this.values.clear();
        this.indexMap.clear();
    }

    
    public void sortByKeys(SortOrder order) {
        final int size = this.keys.size();
        final DefaultKeyedValue<K>[] data = new DefaultKeyedValue[size];

        for (int i = 0; i < size; i++) {
            data[i] = new DefaultKeyedValue(this.keys.get(i), this.values.get(i));
        }

        Comparator comparator = new KeyedValueComparator(
                KeyedValueComparatorType.BY_KEY, order);
        Arrays.sort(data, comparator);
        clear();

        for (int i = 0; i < data.length; i++) {
            final DefaultKeyedValue<K> value = data[i];
            addValue(value.getKey(), value.getValue());
        }
    }

    
    public void sortByValues(SortOrder order) {
        final int size = this.keys.size();
        final DefaultKeyedValue[] data = new DefaultKeyedValue[size];
        for (int i = 0; i < size; i++) {
            data[i] = new DefaultKeyedValue((Comparable) this.keys.get(i),
                    (Number) this.values.get(i));
        }

        Comparator comparator = new KeyedValueComparator(
                KeyedValueComparatorType.BY_VALUE, order);
        Arrays.sort(data, comparator);

        clear();
        for (int i = 0; i < data.length; i++) {
            final DefaultKeyedValue<K> value = data[i];
            addValue(value.getKey(), value.getValue());
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof KeyedValues)) {
            return false;
        }

        KeyedValues that = (KeyedValues) obj;
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
            Number v1 = getValue(i);
            Number v2 = that.getValue(i);
            if (v1 == null) {
                if (v2 != null) {
                    return false;
                }
            }
            else {
                if (!v1.equals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        return (this.keys != null ? this.keys.hashCode() : 0);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultKeyedValues clone = (DefaultKeyedValues) super.clone();
        clone.keys = new ArrayList<>(this.keys);
        clone.values = new ArrayList<>(this.values);
        clone.indexMap = new HashMap(this.indexMap);
        return clone;
    }

}
