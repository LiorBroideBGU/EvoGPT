

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.general.DatasetChangeEvent;


public class VectorSeriesCollection<S extends Comparable<S>> 
        extends AbstractXYDataset<S>
        implements VectorXYDataset<S>, PublicCloneable, Serializable {

    
    private List<VectorSeries<S>> data;

    
    public VectorSeriesCollection() {
        this.data = new ArrayList<>();
    }

    
    public void addSeries(VectorSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    public boolean removeSeries(VectorSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        boolean removed = this.data.remove(series);
        if (removed) {
            series.removeChangeListener(this);
            fireDatasetChanged();
        }
        return removed;
    }

    
    public void removeAllSeries() {

        // deregister the collection as a change listener to each series in the
        // collection
        for (VectorSeries<S> series : this.data) {
            series.removeChangeListener(this);
        }

        // remove all the series from the collection and notify listeners.
        this.data.clear();
        fireDatasetChanged();

    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    public VectorSeries<S> getSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        return this.data.get(series);
    }

    
    @Override
    public S getSeriesKey(int series) {
        // defer argument checking
        return getSeries(series).getKey();
    }

    
    public int indexOf(VectorSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        return this.data.indexOf(series);
    }

    
    @Override
    public int getItemCount(int series) {
        // defer argument checking
        return getSeries(series).getItemCount();
    }

    
    @Override
    public double getXValue(int series, int item) {
        VectorSeries<S> s = this.data.get(series);
        VectorDataItem di = (VectorDataItem) s.getDataItem(item);
        return di.getXValue();
    }

    
    @Override
    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    
    @Override
    public double getYValue(int series, int item) {
        VectorSeries<S> s = this.data.get(series);
        VectorDataItem di = (VectorDataItem) s.getDataItem(item);
        return di.getYValue();
    }

    
    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public Vector getVector(int series, int item) {
        VectorSeries<S> s = this.data.get(series);
        VectorDataItem di = (VectorDataItem) s.getDataItem(item);
        return di.getVector();
    }

    
    @Override
    public double getVectorXValue(int series, int item) {
        VectorSeries<S> s = this.data.get(series);
        VectorDataItem di = (VectorDataItem) s.getDataItem(item);
        return di.getVectorX();
    }

    
    @Override
    public double getVectorYValue(int series, int item) {
        VectorSeries<S> s = this.data.get(series);
        VectorDataItem di = (VectorDataItem) s.getDataItem(item);
        return di.getVectorY();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof VectorSeriesCollection)) {
            return false;
        }
        VectorSeriesCollection<S> that = (VectorSeriesCollection<S>) obj;
        return Objects.equals(this.data, that.data);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        VectorSeriesCollection<S> clone
                = (VectorSeriesCollection<S>) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        return clone;
    }

}
