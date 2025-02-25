

package org.jfree.data.xy;

import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;


public class CategoryTableXYDataset extends AbstractIntervalXYDataset
        implements TableXYDataset, IntervalXYDataset, DomainInfo,
                   PublicCloneable {

    
    private DefaultKeyedValues2D values;

    
    private IntervalXYDelegate intervalDelegate;

    
    public CategoryTableXYDataset() {
        this.values = new DefaultKeyedValues2D(true);
        this.intervalDelegate = new IntervalXYDelegate(this);
        addChangeListener(this.intervalDelegate);
    }

    
    public void add(double x, double y, String seriesName) {
        add(x, y, seriesName, true);
    }

    
    public void add(Number x, Number y, String seriesName, boolean notify) {
        this.values.addValue(y, (Comparable<?>) x, seriesName);
        if (notify) {
            fireDatasetChanged();
        }
    }

    
    public void remove(double x, String seriesName) {
        remove(x, seriesName, true);
    }

    
    public void remove(Number x, String seriesName, boolean notify) {
        this.values.removeValue((Comparable) x, seriesName);
        if (notify) {
            fireDatasetChanged();
        }
    }

    
    public void clear() {
        this.values.clear();
        fireDatasetChanged();
    }

    
    @Override
    public int getSeriesCount() {
        return this.values.getColumnCount();
    }

    
    @Override
    public Comparable<?> getSeriesKey(int series) {
        return this.values.getColumnKey(series);
    }

    
    @Override
    public int getItemCount() {
        return this.values.getRowCount();
    }

    
    @Override
    public int getItemCount(int series) {
        return getItemCount();  // all series have the same number of items in
                                // this dataset
    }

    
    @Override
    public Number getX(int series, int item) {
        return (Number) this.values.getRowKey(item);
    }

    
    @Override
    public Number getStartX(int series, int item) {
        return this.intervalDelegate.getStartX(series, item);
    }

    
    @Override
    public Number getEndX(int series, int item) {
        return this.intervalDelegate.getEndX(series, item);
    }

    
    @Override
    public Number getY(int series, int item) {
        return this.values.getValue(item, series);
    }

    
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public double getDomainLowerBound(boolean includeInterval) {
        return this.intervalDelegate.getDomainLowerBound(includeInterval);
    }

    
    @Override
    public double getDomainUpperBound(boolean includeInterval) {
        return this.intervalDelegate.getDomainUpperBound(includeInterval);
    }

    
    @Override
    public Range getDomainBounds(boolean includeInterval) {
        if (includeInterval) {
            return this.intervalDelegate.getDomainBounds(includeInterval);
        }
        else {
            return DatasetUtils.iterateDomainBounds(this, includeInterval);
        }
    }

    
    public double getIntervalPositionFactor() {
        return this.intervalDelegate.getIntervalPositionFactor();
    }

    
    public void setIntervalPositionFactor(double d) {
        this.intervalDelegate.setIntervalPositionFactor(d);
        fireDatasetChanged();
    }

    
    public double getIntervalWidth() {
        return this.intervalDelegate.getIntervalWidth();
    }

    
    public void setIntervalWidth(double d) {
        this.intervalDelegate.setFixedIntervalWidth(d);
        fireDatasetChanged();
    }

    
    public boolean isAutoWidth() {
        return this.intervalDelegate.isAutoWidth();
    }

    
    public void setAutoWidth(boolean b) {
        this.intervalDelegate.setAutoWidth(b);
        fireDatasetChanged();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CategoryTableXYDataset)) {
            return false;
        }
        CategoryTableXYDataset that = (CategoryTableXYDataset) obj;
        if (!this.intervalDelegate.equals(that.intervalDelegate)) {
            return false;
        }
        if (!this.values.equals(that.values)) {
            return false;
        }
        return true;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        CategoryTableXYDataset clone = (CategoryTableXYDataset) super.clone();
        clone.values = (DefaultKeyedValues2D) this.values.clone();
        clone.intervalDelegate = new IntervalXYDelegate(clone);
        // need to configure the intervalDelegate to match the original
        clone.intervalDelegate.setFixedIntervalWidth(getIntervalWidth());
        clone.intervalDelegate.setAutoWidth(isAutoWidth());
        clone.intervalDelegate.setIntervalPositionFactor(
                getIntervalPositionFactor());
        return clone;
    }

}
