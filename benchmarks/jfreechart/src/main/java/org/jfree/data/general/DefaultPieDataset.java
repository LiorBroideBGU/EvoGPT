

package org.jfree.data.general;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.api.SortOrder;

import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.UnknownKeyException;


public class DefaultPieDataset<K extends Comparable<K>> extends AbstractDataset
        implements PieDataset<K>, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 2904745139106540618L;

    
    private DefaultKeyedValues<K> data;

    
    public DefaultPieDataset() {
        this.data = new DefaultKeyedValues<>();
    }

    
    public DefaultPieDataset(KeyedValues<K> source) {
        Args.nullNotPermitted(source, "source");
        this.data = new DefaultKeyedValues<>();
        for (int i = 0; i < source.getItemCount(); i++) {
            this.data.addValue(source.getKey(i), source.getValue(i));
        }
    }

    
    @Override
    public int getItemCount() {
        return this.data.getItemCount();
    }

    
    @Override
    public List<K> getKeys() {
        return Collections.unmodifiableList(this.data.getKeys());
    }

    
    @Override
    public K getKey(int item) {
        return this.data.getKey(item);
    }

    
    @Override
    public int getIndex(K key) {
        return this.data.getIndex(key);
    }

    
    @Override
    public Number getValue(int item) {
        return this.data.getValue(item);
    }

    
    @Override
    public Number getValue(K key) {
        Args.nullNotPermitted(key, "key");
        return this.data.getValue(key);
    }

    
    public void setValue(K key, Number value) {
        this.data.setValue(key, value);
        fireDatasetChanged();
    }

    
    public void setValue(K key, double value) {
        setValue(key, Double.valueOf(value));
    }

    
    public void insertValue(int position, K key, double value) {
        insertValue(position, key, Double.valueOf(value));
    }

    
    public void insertValue(int position, K key, Number value) {
        this.data.insertValue(position, key, value);
        fireDatasetChanged();
    }

    
    public void remove(K key) {
        this.data.removeValue(key);
        fireDatasetChanged();
    }

    
    public void clear() {
        if (getItemCount() > 0) {
            this.data.clear();
            fireDatasetChanged();
        }
    }

    
    public void sortByKeys(SortOrder order) {
        this.data.sortByKeys(order);
        fireDatasetChanged();
    }

    
    public void sortByValues(SortOrder order) {
        this.data.sortByValues(order);
        fireDatasetChanged();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof PieDataset)) {
            return false;
        }
        PieDataset<K> that = (PieDataset) obj;
        int count = getItemCount();
        if (that.getItemCount() != count) {
            return false;
        }

        for (int i = 0; i < count; i++) {
            K k1 = getKey(i);
            K k2 = that.getKey(i);
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
        return this.data.hashCode();
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultPieDataset<K> clone = (DefaultPieDataset) super.clone();
        clone.data = CloneUtils.clone(this.data);
        return clone;
    }

}
