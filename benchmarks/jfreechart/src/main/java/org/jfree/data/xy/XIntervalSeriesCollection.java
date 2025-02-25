

package org.jfree.data.xy;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.general.DatasetChangeEvent;


public class XIntervalSeriesCollection<S extends Comparable<S>> 
        extends AbstractIntervalXYDataset
        implements IntervalXYDataset, PublicCloneable, Serializable {

    
    private List<XIntervalSeries<S>> data;

    
    public XIntervalSeriesCollection() {
        this.data = new ArrayList<>();
    }

    
    public void addSeries(XIntervalSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    public XIntervalSeries<S> getSeries(int series) {
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
        XIntervalSeries<S> s = this.data.get(series);
        XIntervalDataItem di = (XIntervalDataItem) s.getDataItem(item);
        return di.getX();
    }

    
    @Override
    public double getStartXValue(int series, int item) {
        XIntervalSeries<S> s = this.data.get(series);
        return s.getXLowValue(item);
    }

    
    @Override
    public double getEndXValue(int series, int item) {
        XIntervalSeries<S> s = this.data.get(series);
        return s.getXHighValue(item);
    }

    
    @Override
    public double getYValue(int series, int item) {
        XIntervalSeries<S> s = this.data.get(series);
        return s.getYValue(item);
    }

    
    @Override
    public Number getY(int series, int item) {
        XIntervalSeries<S> s = this.data.get(series);
        XIntervalDataItem di = (XIntervalDataItem) s.getDataItem(item);
        return di.getYValue();
    }

    
    @Override
    public Number getStartX(int series, int item) {
        XIntervalSeries<S> s = this.data.get(series);
        XIntervalDataItem di = (XIntervalDataItem) s.getDataItem(item);
        return di.getXLowValue();
    }

    
    @Override
    public Number getEndX(int series, int item) {
        XIntervalSeries<S> s = this.data.get(series);
        XIntervalDataItem di = (XIntervalDataItem) s.getDataItem(item);
        return di.getXHighValue();
    }

    
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    
    public void removeSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        XIntervalSeries ts = this.data.get(series);
        ts.removeChangeListener(this);
        this.data.remove(series);
        fireDatasetChanged();
    }

    
    public void removeSeries(XIntervalSeries<S> series) {
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
        for (XIntervalSeries series : this.data) {
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
        if (!(obj instanceof XIntervalSeriesCollection)) {
            return false;
        }
        XIntervalSeriesCollection<S> that = (XIntervalSeriesCollection<S>) obj;
        return Objects.equals(this.data, that.data);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        XIntervalSeriesCollection clone
                = (XIntervalSeriesCollection) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        return clone;
    }


    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        for (XIntervalSeries<S> item : this.data) {
            XIntervalSeries<S> series = item;
            series.addChangeListener(this);
        }
    }
}
