

package org.jfree.data.xy;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.gantt.TaskSeries;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.Series;


public class XYSeriesCollection<S extends Comparable<S>> 
        extends AbstractIntervalXYDataset<S>
        implements IntervalXYDataset<S>, DomainInfo, RangeInfo, 
        VetoableChangeListener, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -7590013825931496766L;

    
    private List<XYSeries<S>> data;

    
    private IntervalXYDelegate intervalDelegate;

    
    public XYSeriesCollection() {
        this(null);
    }

    
    public XYSeriesCollection(XYSeries<S> series) {
        this.data = new ArrayList<>();
        this.intervalDelegate = new IntervalXYDelegate(this, false);
        addChangeListener(this.intervalDelegate);
        if (series != null) {
            this.data.add(series);
            series.addChangeListener(this);
        }
    }

    
    @Override
    public DomainOrder getDomainOrder() {
        int seriesCount = getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries<S> s = getSeries(i);
            if (!s.getAutoSort()) {
                return DomainOrder.NONE;  // we can't be sure of the order
            }
        }
        return DomainOrder.ASCENDING;
    }

    
    public void addSeries(XYSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        if (getSeriesIndex(series.getKey()) >= 0) {
            throw new IllegalArgumentException(
                "This dataset already contains a series with the key " 
                + series.getKey());
        }
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    public void removeSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        XYSeries<S> s = this.data.get(series);
        if (s != null) {
            removeSeries(s);
        }
    }

    
    public void removeSeries(XYSeries<S> series) {
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
        for (XYSeries<S> series : this.data) {
            series.removeChangeListener(this);
        }

        // Remove all the series from the collection and notify listeners.
        this.data.clear();
        fireDatasetChanged();
    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    public List<XYSeries<S>> getSeries() {
        try {
            return CloneUtils.clone(this.data);
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException("Unexpected exception in JFreeChart - please file a bug report.");
        }
    }

    
    public int indexOf(XYSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        return this.data.indexOf(series);
    }

    
    public XYSeries<S> getSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        return this.data.get(series);
    }

    
    public XYSeries<S> getSeries(S key) {
        Args.nullNotPermitted(key, "key");
        for (XYSeries<S> series : this.data) {
            if (key.equals(series.getKey())) {
                return series;
            }
        }
        throw new UnknownKeyException("Key not found: " + key);
    }

    
    @Override
    public S getSeriesKey(int series) {
        // defer argument checking
        return getSeries(series).getKey();
    }

    
    public int getSeriesIndex(S key) {
        Args.nullNotPermitted(key, "key");
        int seriesCount = getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            XYSeries<S> series = this.data.get(i);
            if (key.equals(series.getKey())) {
                return i;
            }
        }
        return -1;
    }

    
    @Override
    public int getItemCount(int series) {
        // defer argument checking
        return getSeries(series).getItemCount();
    }

    
    @Override
    public Number getX(int series, int item) {
        XYSeries<S> s = this.data.get(series);
        return s.getX(item);
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
    public Number getY(int series, int index) {
        XYSeries<S> s = this.data.get(series);
        return s.getY(index);
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYSeriesCollection)) {
            return false;
        }
        XYSeriesCollection that = (XYSeriesCollection) obj;
        if (!this.intervalDelegate.equals(that.intervalDelegate)) {
            return false;
        }
        return Objects.equals(this.data, that.data);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        XYSeriesCollection clone = (XYSeriesCollection) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        clone.intervalDelegate
                = (IntervalXYDelegate) this.intervalDelegate.clone();
        return clone;
    }

    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = HashUtils.hashCode(hash, this.intervalDelegate);
        hash = HashUtils.hashCode(hash, this.data);
        return hash;
    }

    
    @Override
    public double getDomainLowerBound(boolean includeInterval) {
        if (includeInterval) {
            return this.intervalDelegate.getDomainLowerBound(includeInterval);
        }
        double result = Double.NaN;
        int seriesCount = getSeriesCount();
        for (int s = 0; s < seriesCount; s++) {
            XYSeries<S> series = getSeries(s);
            double lowX = series.getMinX();
            if (Double.isNaN(result)) {
                result = lowX;
            }
            else {
                if (!Double.isNaN(lowX)) {
                    result = Math.min(result, lowX);
                }
            }
        }
        return result;
    }

    
    @Override
    public double getDomainUpperBound(boolean includeInterval) {
        if (includeInterval) {
            return this.intervalDelegate.getDomainUpperBound(includeInterval);
        }
        else {
            double result = Double.NaN;
            int seriesCount = getSeriesCount();
            for (int s = 0; s < seriesCount; s++) {
                XYSeries<S> series = getSeries(s);
                double hiX = series.getMaxX();
                if (Double.isNaN(result)) {
                    result = hiX;
                }
                else {
                    if (!Double.isNaN(hiX)) {
                        result = Math.max(result, hiX);
                    }
                }
            }
            return result;
        }
    }

    
    @Override
    public Range getDomainBounds(boolean includeInterval) {
        if (includeInterval) {
            return this.intervalDelegate.getDomainBounds(includeInterval);
        }
        else {
            double lower = Double.POSITIVE_INFINITY;
            double upper = Double.NEGATIVE_INFINITY;
            int seriesCount = getSeriesCount();
            for (int s = 0; s < seriesCount; s++) {
                XYSeries<S> series = getSeries(s);
                double minX = series.getMinX();
                if (!Double.isNaN(minX)) {
                    lower = Math.min(lower, minX);
                }
                double maxX = series.getMaxX();
                if (!Double.isNaN(maxX)) {
                    upper = Math.max(upper, maxX);
                }
            }
            if (lower > upper) {
                return null;
            }
            else {
                return new Range(lower, upper);
            }
        }
    }

    
    public double getIntervalWidth() {
        return this.intervalDelegate.getIntervalWidth();
    }

    
    public void setIntervalWidth(double width) {
        if (width < 0.0) {
            throw new IllegalArgumentException("Negative 'width' argument.");
        }
        this.intervalDelegate.setFixedIntervalWidth(width);
        fireDatasetChanged();
    }

    
    public double getIntervalPositionFactor() {
        return this.intervalDelegate.getIntervalPositionFactor();
    }

    
    public void setIntervalPositionFactor(double factor) {
        this.intervalDelegate.setIntervalPositionFactor(factor);
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
    public Range getRangeBounds(boolean includeInterval) {
        double lower = Double.POSITIVE_INFINITY;
        double upper = Double.NEGATIVE_INFINITY;
        int seriesCount = getSeriesCount();
        for (int s = 0; s < seriesCount; s++) {
            XYSeries<S> series = getSeries(s);
            double minY = series.getMinY();
            if (!Double.isNaN(minY)) {
                lower = Math.min(lower, minY);
            }
            double maxY = series.getMaxY();
            if (!Double.isNaN(maxY)) {
                upper = Math.max(upper, maxY);
            }
        }
        if (lower > upper) {
            return null;
        }
        else {
            return new Range(lower, upper);
        }
    }

    
    @Override
    public double getRangeLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        int seriesCount = getSeriesCount();
        for (int s = 0; s < seriesCount; s++) {
            XYSeries<S> series = getSeries(s);
            double lowY = series.getMinY();
            if (Double.isNaN(result)) {
                result = lowY;
            }
            else {
                if (!Double.isNaN(lowY)) {
                    result = Math.min(result, lowY);
                }
            }
        }
        return result;
    }

    
    @Override
    public double getRangeUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        int seriesCount = getSeriesCount();
        for (int s = 0; s < seriesCount; s++) {
            XYSeries<S> series = getSeries(s);
            double hiY = series.getMaxY();
            if (Double.isNaN(result)) {
                result = hiY;
            }
            else {
                if (!Double.isNaN(hiY)) {
                    result = Math.max(result, hiY);
                }
            }
        }
        return result;
    }

    
    @Override
    public void vetoableChange(PropertyChangeEvent e)
            throws PropertyVetoException {
        // if it is not the series name, then we have no interest
        if (!"Key".equals(e.getPropertyName())) {
            return;
        }
        
        // to be defensive, let's check that the source series does in fact
        // belong to this collection
        Series<S> s = (Series) e.getSource();
        if (getSeriesIndex(s.getKey()) == -1) {
            throw new IllegalStateException("Receiving events from a series " +
                    "that does not belong to this collection.");
        }
        // check if the new series name already exists for another series
        S key = (S) e.getNewValue();
        if (getSeriesIndex(key) >= 0) {
            throw new PropertyVetoException("Duplicate key2", e);
        }
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        for (Object item : this.data) {
            XYSeries<S> series = (XYSeries<S>) item;
            series.addChangeListener(this);
        }
    }
}
