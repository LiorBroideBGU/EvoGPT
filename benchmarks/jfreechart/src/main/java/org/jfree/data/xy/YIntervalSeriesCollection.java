

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.general.DatasetChangeEvent;


public class YIntervalSeriesCollection<S extends Comparable<S>> 
        extends AbstractIntervalXYDataset<S>
        implements IntervalXYDataset<S>, PublicCloneable, Serializable {

    
    private List<YIntervalSeries> data;

    
    public YIntervalSeriesCollection() {
        this.data = new ArrayList<>();
    }

    
    public void addSeries(YIntervalSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    public YIntervalSeries<S> getSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        return this.data.get(series);
    }

    
    @Override
    public S getSeriesKey(int series) {
        // defer argument checking
        return getSeries(series).getKey();
    }

    
    @Override
    public int getItemCount(int series) {
        // defer argument checking
        return getSeries(series).getItemCount();
    }

    
    @Override
    public Number getX(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getX(item);
    }

    
    @Override
    public double getYValue(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getYValue(item);
    }

    
    @Override
    public double getStartYValue(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getYLowValue(item);
    }

    
    @Override
    public double getEndYValue(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getYHighValue(item);
    }

    
    @Override
    public Number getY(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getYValue(item);
    }

    
    @Override
    public Number getStartX(int series, int item) {
        return getX(series, item);
    }

    
    @Override
    public Number getEndX(int series, int item) {
        return getX(series, item);
    }

    
    @Override
    public Number getStartY(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getYLowValue(item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        YIntervalSeries s = this.data.get(series);
        return s.getYHighValue(item);
    }

    
    public void removeSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        YIntervalSeries ts = this.data.get(series);
        ts.removeChangeListener(this);
        this.data.remove(series);
        fireDatasetChanged();
    }

    
    public void removeSeries(YIntervalSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        if (this.data.contains(series)) {
            series.removeChangeListener(this);
            this.data.remove(series);
            fireDatasetChanged();
        }
    }

    
    public void removeAllSeries() {
        // Unregister the collection as a change listener to each series in
        // the collection.
        for (YIntervalSeries series : this.data) {
          series.removeChangeListener(this);
        }
        this.data.clear();
        fireDatasetChanged();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof YIntervalSeriesCollection)) {
            return false;
        }
        YIntervalSeriesCollection<S> that = (YIntervalSeriesCollection) obj;
        return Objects.equals(this.data, that.data);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        YIntervalSeriesCollection<S> clone
                = (YIntervalSeriesCollection) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        return clone;
    }

}
