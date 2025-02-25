

package org.jfree.data.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.KeyedObjects2D;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;


public class DefaultMultiValueCategoryDataset<R extends Comparable<R>, C extends Comparable<C>>
        extends AbstractDataset implements MultiValueCategoryDataset<R, C>, 
        RangeInfo, PublicCloneable {

    
    protected KeyedObjects2D data;

    
    private Number minimumRangeValue;

    
    private Number maximumRangeValue;

    
    private Range rangeBounds;

    
    public DefaultMultiValueCategoryDataset() {
        this.data = new KeyedObjects2D();
        this.minimumRangeValue = null;
        this.maximumRangeValue = null;
        this.rangeBounds = new Range(0.0, 0.0);
    }

    
    public void add(List<? extends Number> values, R rowKey, C columnKey) {

        Args.nullNotPermitted(values, "values");
        Args.nullNotPermitted(rowKey, "rowKey");
        Args.nullNotPermitted(columnKey, "columnKey");
        List<Double> vlist = new ArrayList<>(values.size());
        for (Number v : values) {
            if (v != null && !Double.isNaN(v.doubleValue())) {
                vlist.add(v.doubleValue());
            }
        }
        Collections.sort(vlist);
        this.data.addObject(vlist, rowKey, columnKey);

        if (vlist.size() > 0) {
            double maxval = Double.NEGATIVE_INFINITY;
            double minval = Double.POSITIVE_INFINITY;
            for (int i = 0; i < vlist.size(); i++) {
                Number n = (Number) vlist.get(i);
                double v = n.doubleValue();
                minval = Math.min(minval, v);
                maxval = Math.max(maxval, v);
            }

            // update the cached range values...
            if (this.maximumRangeValue == null) {
                this.maximumRangeValue = maxval;
            }
            else if (maxval > this.maximumRangeValue.doubleValue()) {
                this.maximumRangeValue = maxval;
            }

            if (this.minimumRangeValue == null) {
                this.minimumRangeValue = minval;
            }
            else if (minval < this.minimumRangeValue.doubleValue()) {
                this.minimumRangeValue = minval;
            }
            this.rangeBounds = new Range(this.minimumRangeValue.doubleValue(),
                    this.maximumRangeValue.doubleValue());
        }

        fireDatasetChanged();
    }

    
    @Override
    public List<? extends Number> getValues(int row, int column) {
        List values = (List) this.data.getObject(row, column);
        if (values != null) {
            return Collections.unmodifiableList(values);
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    
    @Override
    public List<? extends Number> getValues(Comparable rowKey, Comparable columnKey) {
        return Collections.unmodifiableList((List) this.data.getObject(rowKey,
                columnKey));
    }

    
    @Override
    public Number getValue(Comparable row, Comparable column) {
        List l = (List) this.data.getObject(row, column);
        double average = 0.0d;
        int count = 0;
        if (l != null && l.size() > 0) {
            for (int i = 0; i < l.size(); i++) {
                Number n = (Number) l.get(i);
                average += n.doubleValue();
                count += 1;
            }
            if (count > 0) {
                average = average / count;
            }
        }
        if (count == 0) {
            return null;
        }
        return average;
    }

    
    @Override
    public Number getValue(int row, int column) {
        List l = (List) this.data.getObject(row, column);
        double average = 0.0d;
        int count = 0;
        if (l != null && l.size() > 0) {
            for (int i = 0; i < l.size(); i++) {
                Number n = (Number) l.get(i);
                average += n.doubleValue();
                count += 1;
            }
            if (count > 0) {
                average = average / count;
            }
        }
        if (count == 0) {
            return null;
        }
        return average;
    }

    
    @Override
    public int getColumnIndex(Comparable key) {
        return this.data.getColumnIndex(key);
    }

    
    @Override
    public C getColumnKey(int column) {
        return (C) this.data.getColumnKey(column);
    }

    
    @Override
    public List getColumnKeys() {
        return this.data.getColumnKeys();
    }

    
    @Override
    public int getRowIndex(Comparable key) {
        return this.data.getRowIndex(key);
    }

    
    @Override
    public R getRowKey(int row) {
        return (R) this.data.getRowKey(row);
    }

    
    @Override
    public List getRowKeys() {
        return this.data.getRowKeys();
    }

    
    @Override
    public int getRowCount() {
        return this.data.getRowCount();
    }

    
    @Override
    public int getColumnCount() {
        return this.data.getColumnCount();
    }

    
    @Override
    public double getRangeLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.minimumRangeValue != null) {
            result = this.minimumRangeValue.doubleValue();
        }
        return result;
    }

    
    @Override
    public double getRangeUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        if (this.maximumRangeValue != null) {
            result = this.maximumRangeValue.doubleValue();
        }
        return result;
    }

    
    @Override
    public Range getRangeBounds(boolean includeInterval) {
        return this.rangeBounds;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultMultiValueCategoryDataset)) {
            return false;
        }
        DefaultMultiValueCategoryDataset that
                = (DefaultMultiValueCategoryDataset) obj;
        return this.data.equals(that.data);
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 37 * hash + Objects.hashCode(this.data);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultMultiValueCategoryDataset clone
                = (DefaultMultiValueCategoryDataset) super.clone();
        clone.data = (KeyedObjects2D) this.data.clone();
        return clone;
    }
}
