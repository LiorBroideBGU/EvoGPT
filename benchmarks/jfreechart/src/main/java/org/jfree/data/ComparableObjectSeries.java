

package org.jfree.data;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class ComparableObjectSeries<K extends Comparable<K>> extends Series<K>
        implements Cloneable, Serializable {

    
    protected List<ComparableObjectItem> data;

    
    private int maximumItemCount = Integer.MAX_VALUE;

    
    private boolean autoSort;

    
    private boolean allowDuplicateXValues;

    
    public ComparableObjectSeries(K key) {
        this(key, true, true);
    }

    
    public ComparableObjectSeries(K key, boolean autoSort,
            boolean allowDuplicateXValues) {
        super(key);
        this.data = new ArrayList<>();
        this.autoSort = autoSort;
        this.allowDuplicateXValues = allowDuplicateXValues;
    }

    
    public boolean getAutoSort() {
        return this.autoSort;
    }

    
    public boolean getAllowDuplicateXValues() {
        return this.allowDuplicateXValues;
    }

    
    @Override
    public int getItemCount() {
        return this.data.size();
    }

    
    public int getMaximumItemCount() {
        return this.maximumItemCount;
    }

    
    public void setMaximumItemCount(int maximum) {
        this.maximumItemCount = maximum;
        boolean dataRemoved = false;
        while (this.data.size() > maximum) {
            this.data.remove(0);
            dataRemoved = true;
        }
        if (dataRemoved) {
            fireSeriesChanged();
        }
    }

    
    protected void add(Comparable<?> x, Object y) {
        // argument checking delegated...
        add(x, y, true);
    }

    
    protected void add(Comparable<?> x, Object y, boolean notify) {
        // delegate argument checking to XYDataItem...
        ComparableObjectItem item = new ComparableObjectItem(x, y);
        add(item, notify);
    }

    
    protected void add(ComparableObjectItem item, boolean notify) {

        Args.nullNotPermitted(item, "item");
        if (this.autoSort) {
            int index = Collections.binarySearch(this.data, item);
            if (index < 0) {
                this.data.add(-index - 1, item);
            }
            else {
                if (this.allowDuplicateXValues) {
                    // need to make sure we are adding *after* any duplicates
                    int size = this.data.size();
                    while (index < size
                           && item.compareTo(this.data.get(index)) == 0) {
                        index++;
                    }
                    if (index < this.data.size()) {
                        this.data.add(index, item);
                    }
                    else {
                        this.data.add(item);
                    }
                }
                else {
                    throw new SeriesException("X-value already exists.");
                }
            }
        }
        else {
            if (!this.allowDuplicateXValues) {
                // can't allow duplicate values, so we need to check whether
                // there is an item with the given x-value already
                int index = indexOf(item.getComparable());
                if (index >= 0) {
                    throw new SeriesException("X-value already exists.");
                }
            }
            this.data.add(item);
        }
        if (getItemCount() > this.maximumItemCount) {
            this.data.remove(0);
        }
        if (notify) {
            fireSeriesChanged();
        }
    }

    
    public int indexOf(Comparable<?> x) {
        if (this.autoSort) {
            return Collections.binarySearch(this.data, new ComparableObjectItem(
                    x, null));
        }
        else {
            for (int i = 0; i < this.data.size(); i++) {
                ComparableObjectItem item = this.data.get(i);
                if (item.getComparable().equals(x)) {
                    return i;
                }
            }
            return -1;
        }
    }

    
    protected void update(Comparable<?> x, Object y) {
        int index = indexOf(x);
        if (index < 0) {
            throw new SeriesException("No observation for x = " + x);
        }
        else {
            ComparableObjectItem item = getDataItem(index);
            item.setObject(y);
            fireSeriesChanged();
        }
    }

    
    protected void updateByIndex(int index, Object y) {
        ComparableObjectItem item = getDataItem(index);
        item.setObject(y);
        fireSeriesChanged();
    }

    
    protected ComparableObjectItem getDataItem(int index) {
        return this.data.get(index);
    }

    
    protected void delete(int start, int end) {
        if (end >= start) {
            this.data.subList(start, end + 1).clear();
        }
        fireSeriesChanged();
    }

    
    public void clear() {
        if (this.data.size() > 0) {
            this.data.clear();
            fireSeriesChanged();
        }
    }

    
    protected ComparableObjectItem remove(int index) {
        ComparableObjectItem result = this.data.remove(index);
        fireSeriesChanged();
        return result;
    }

    
    public ComparableObjectItem remove(Comparable<?> x) {
        return remove(indexOf(x));
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ComparableObjectSeries)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        ComparableObjectSeries that = (ComparableObjectSeries) obj;
        if (this.maximumItemCount != that.maximumItemCount) {
            return false;
        }
        if (this.autoSort != that.autoSort) {
            return false;
        }
        if (this.allowDuplicateXValues != that.allowDuplicateXValues) {
            return false;
        }
        if (!Objects.equals(this.data, that.data)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        // it is too slow to look at every data item, so let's just look at
        // the first, middle and last items...
        int count = getItemCount();
        if (count > 0) {
            ComparableObjectItem item = getDataItem(0);
            result = 29 * result + item.hashCode();
        }
        if (count > 1) {
            ComparableObjectItem item = getDataItem(count - 1);
            result = 29 * result + item.hashCode();
        }
        if (count > 2) {
            ComparableObjectItem item = getDataItem(count / 2);
            result = 29 * result + item.hashCode();
        }
        result = 29 * result + this.maximumItemCount;
        result = 29 * result + (this.autoSort ? 1 : 0);
        result = 29 * result + (this.allowDuplicateXValues ? 1 : 0);
        return result;
    }

    
    @Override
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        ComparableObjectSeries<K> clone = (ComparableObjectSeries<K>) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        return clone;
    }

}
