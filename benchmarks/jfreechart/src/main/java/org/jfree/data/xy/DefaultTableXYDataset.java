

package org.jfree.data.xy;

import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.SeriesChangeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;


public class DefaultTableXYDataset<S extends Comparable<S>> 
        extends AbstractIntervalXYDataset<S>
        implements TableXYDataset<S>, IntervalXYDataset<S>, DomainInfo,
                   PublicCloneable {

    
    private List<XYSeries<S>> data = null;

    
    private HashSet xPoints = null;

    
    private boolean propagateEvents = true;

    
    private boolean autoPrune = false;

    
    private IntervalXYDelegate intervalDelegate;

    
    public DefaultTableXYDataset() {
        this(false);
    }

    
    public DefaultTableXYDataset(boolean autoPrune) {
        this.autoPrune = autoPrune;
        this.data = new ArrayList<>();
        this.xPoints = new HashSet();
        this.intervalDelegate = new IntervalXYDelegate(this, false);
        addChangeListener(this.intervalDelegate);
    }

    
    public boolean isAutoPrune() {
        return this.autoPrune;
    }

    
    public void addSeries(XYSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        if (series.getAllowDuplicateXValues()) {
            throw new IllegalArgumentException(
                "Cannot accept XYSeries that allow duplicate values. "
                + "Use XYSeries(seriesName, <sort>, false) constructor."
            );
        }
        updateXPoints(series);
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    private void updateXPoints(XYSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        HashSet seriesXPoints = new HashSet();
        boolean savedState = this.propagateEvents;
        this.propagateEvents = false;
        for (int itemNo = 0; itemNo < series.getItemCount(); itemNo++) {
            Number xValue = series.getX(itemNo);
            seriesXPoints.add(xValue);
            if (!this.xPoints.contains(xValue)) {
                this.xPoints.add(xValue);
                int seriesCount = this.data.size();
                for (int seriesNo = 0; seriesNo < seriesCount; seriesNo++) {
                    XYSeries<S> dataSeries = this.data.get(seriesNo);
                    if (!dataSeries.equals(series)) {
                        dataSeries.add(xValue, null);
                    }
                }
            }
        }
        for (Object point : this.xPoints) {
            Number xPoint = (Number) point;
            if (!seriesXPoints.contains(xPoint)) {
                series.add(xPoint, null);
            }
        }
        this.propagateEvents = savedState;
    }

    
    public void updateXPoints() {
        this.propagateEvents = false;
        for (int s = 0; s < this.data.size(); s++) {
            updateXPoints(this.data.get(s));
        }
        if (this.autoPrune) {
            prune();
        }
        this.propagateEvents = true;
    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    @Override
    public int getItemCount() {
        if (this.xPoints == null) {
            return 0;
        }
        else {
            return this.xPoints.size();
        }
    }

    
    public XYSeries<S> getSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        return this.data.get(series);
    }

    
    @Override
    public S getSeriesKey(int series) {
        // check arguments...delegated
        return getSeries(series).getKey();
    }

    
    @Override
    public int getItemCount(int series) {
        // check arguments...delegated
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

    
    public void removeAllSeries() {

        // Unregister the collection as a change listener to each series in
        // the collection.
        for (XYSeries<S> series : this.data) {
            series.removeChangeListener(this);
        }

        // Remove all the series from the collection and notify listeners.
        this.data.clear();
        this.xPoints.clear();
        fireDatasetChanged();
    }

    
    public void removeSeries(XYSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        if (this.data.contains(series)) {
            series.removeChangeListener(this);
            this.data.remove(series);
            if (this.data.isEmpty()) {
                this.xPoints.clear();
            }
            fireDatasetChanged();
        }
    }

    
    public void removeSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);

        // fetch the series, remove the change listener, then remove the series.
        XYSeries<S> s = this.data.get(series);
        s.removeChangeListener(this);
        this.data.remove(series);
        if (this.data.isEmpty()) {
            this.xPoints.clear();
        }
        else if (this.autoPrune) {
            prune();
        }
        fireDatasetChanged();

    }

    
    public void removeAllValuesForX(Number x) {
        Args.nullNotPermitted(x, "x");
        boolean savedState = this.propagateEvents;
        this.propagateEvents = false;
        for (int s = 0; s < this.data.size(); s++) {
            XYSeries<S> series = this.data.get(s);
            series.remove(x);
        }
        this.propagateEvents = savedState;
        this.xPoints.remove(x);
        fireDatasetChanged();
    }

    
    protected boolean canPrune(Number x) {
        for (int s = 0; s < this.data.size(); s++) {
            XYSeries<S> series = this.data.get(s);
            if (series.getY(series.indexOf(x)) != null) {
                return false;
            }
        }
        return true;
    }

    
    public void prune() {
        HashSet hs = (HashSet) this.xPoints.clone();
        for (Object h : hs) {
            Number x = (Number) h;
            if (canPrune(x)) {
                removeAllValuesForX(x);
            }
        }
    }

    
    @Override
    public void seriesChanged(SeriesChangeEvent event) {
        if (this.propagateEvents) {
            updateXPoints();
            fireDatasetChanged();
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultTableXYDataset)) {
            return false;
        }
        DefaultTableXYDataset that = (DefaultTableXYDataset) obj;
        if (this.autoPrune != that.autoPrune) {
            return false;
        }
        if (this.propagateEvents != that.propagateEvents) {
            return false;
        }
        if (!this.intervalDelegate.equals(that.intervalDelegate)) {
            return false;
        }
        if (!Objects.equals(this.data, that.data)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = (this.data != null ? this.data.hashCode() : 0);
        result = 29 * result
                 + (this.xPoints != null ? this.xPoints.hashCode() : 0);
        result = 29 * result + (this.propagateEvents ? 1 : 0);
        result = 29 * result + (this.autoPrune ? 1 : 0);
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultTableXYDataset clone = (DefaultTableXYDataset) super.clone();
        int seriesCount = this.data.size();
        clone.data = new ArrayList<>(seriesCount);
        for (XYSeries<S> series : this.data) {
            clone.data.add(CloneUtils.clone(series));
        }

        clone.intervalDelegate = new IntervalXYDelegate(clone);
        // need to configure the intervalDelegate to match the original
        clone.intervalDelegate.setFixedIntervalWidth(getIntervalWidth());
        clone.intervalDelegate.setAutoWidth(isAutoWidth());
        clone.intervalDelegate.setIntervalPositionFactor(
                getIntervalPositionFactor());
        clone.updateXPoints();
        return clone;
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

}
