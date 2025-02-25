

package org.jfree.data.time;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.data.DomainInfo;
import org.jfree.data.DomainOrder;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.Series;
import org.jfree.data.xy.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.io.Serializable;
import java.util.*;


public class TimeSeriesCollection<S extends Comparable<S>> 
        extends AbstractIntervalXYDataset
        implements XYDataset, IntervalXYDataset, DomainInfo, XYDomainInfo,
        XYRangeInfo, VetoableChangeListener, Serializable {

    
    private static final long serialVersionUID = 834149929022371137L;

    
    private List<TimeSeries<S>> data;

    
    private Calendar workingCalendar;

    
    private TimePeriodAnchor xPosition;

    
    public TimeSeriesCollection() {
        this(null, TimeZone.getDefault());
    }

    
    public TimeSeriesCollection(TimeZone zone) {
        // FIXME: need a locale as well as a timezone
        this(null, zone);
    }

    
    public TimeSeriesCollection(TimeSeries<S> series) {
        this(series, TimeZone.getDefault());
    }

    
    public TimeSeriesCollection(TimeSeries<S> series, TimeZone zone) {
        // FIXME:  need a locale as well as a timezone
        if (zone == null) {
            zone = TimeZone.getDefault();
        }
        this.workingCalendar = Calendar.getInstance(zone);
        this.data = new ArrayList<>();
        if (series != null) {
            this.data.add(series);
            series.addChangeListener(this);
        }
        this.xPosition = TimePeriodAnchor.START;
    }

    
    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }

    
    public TimePeriodAnchor getXPosition() {
        return this.xPosition;
    }

    
    public void setXPosition(TimePeriodAnchor anchor) {
        Args.nullNotPermitted(anchor, "anchor");
        this.xPosition = anchor;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    public List<TimeSeries<S>> getSeries() {
        return Collections.unmodifiableList(this.data);
    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    public int indexOf(TimeSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        return this.data.indexOf(series);
    }

    
    public TimeSeries<S> getSeries(int series) {
        Args.requireInRange(series, "series", 0, getSeriesCount() - 1);
        return this.data.get(series);
    }

    
    public TimeSeries<S> getSeries(S key) {
        for (TimeSeries series : this.data) {
            if (series.getKey() != null && series.getKey().equals(key)) {
                return series;
            }
        }
        return null;
    }

    
    @Override
    public Comparable getSeriesKey(int series) {
        // check arguments...delegated
        // fetch the series name...
        return getSeries(series).getKey();
    }

    
    public int getSeriesIndex(Comparable key) {
        Args.nullNotPermitted(key, "key");
        int seriesCount = getSeriesCount();
        for (int i = 0; i < seriesCount; i++) {
            TimeSeries<S> series = this.data.get(i);
            if (key.equals(series.getKey())) {
                return i;
            }
        }
        return -1;
    }

    
    public void addSeries(TimeSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    public void removeSeries(TimeSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        this.data.remove(series);
        series.removeChangeListener(this);
        fireDatasetChanged();
    }

    
    public void removeSeries(int index) {
        TimeSeries<S> series = getSeries(index);
        if (series != null) {
            removeSeries(series);
        }
    }

    
    public void removeAllSeries() {

        // deregister the collection as a change listener to each series in the
        // collection
        for (TimeSeries<S> series : this.data) {
            series.removeChangeListener(this);
        }

        // remove all the series from the collection and notify listeners.
        this.data.clear();
        fireDatasetChanged();
    }

    
    @Override
    public int getItemCount(int series) {
        return getSeries(series).getItemCount();
    }

    
    @Override
    public double getXValue(int series, int item) {
        TimeSeries<S> s = this.data.get(series);
        RegularTimePeriod period = s.getTimePeriod(item);
        return getX(period);
    }

    
    @Override
    public Number getX(int series, int item) {
        TimeSeries<S> ts = this.data.get(series);
        RegularTimePeriod period = ts.getTimePeriod(item);
        return getX(period);
    }

    
    protected synchronized long getX(RegularTimePeriod period) {
        long result = 0L;
        if (this.xPosition == TimePeriodAnchor.START) {
            result = period.getFirstMillisecond(this.workingCalendar);
        }
        else if (this.xPosition == TimePeriodAnchor.MIDDLE) {
            result = period.getMiddleMillisecond(this.workingCalendar);
        }
        else if (this.xPosition == TimePeriodAnchor.END) {
            result = period.getLastMillisecond(this.workingCalendar);
        }
        return result;
    }

    
    @Override
    public synchronized Number getStartX(int series, int item) {
        TimeSeries<S> ts = this.data.get(series);
        return ts.getTimePeriod(item).getFirstMillisecond(this.workingCalendar);
    }

    
    @Override
    public synchronized Number getEndX(int series, int item) {
        TimeSeries<S> ts = this.data.get(series);
        return ts.getTimePeriod(item).getLastMillisecond(this.workingCalendar);
    }

    
    @Override
    public Number getY(int series, int item) {
        TimeSeries<S> ts = this.data.get(series);
        return ts.getValue(item);
    }

    
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }


    
    public int[] getSurroundingItems(int series, long milliseconds) {
        int[] result = new int[] {-1, -1};
        TimeSeries<S> timeSeries = getSeries(series);
        for (int i = 0; i < timeSeries.getItemCount(); i++) {
            Number x = getX(series, i);
            long m = x.longValue();
            if (m <= milliseconds) {
                result[0] = i;
            }
            if (m >= milliseconds) {
                result[1] = i;
                break;
            }
        }
        return result;
    }

    
    @Override
    public double getDomainLowerBound(boolean includeInterval) {
        double result = Double.NaN;
        Range r = getDomainBounds(includeInterval);
        if (r != null) {
            result = r.getLowerBound();
        }
        return result;
    }

    
    @Override
    public double getDomainUpperBound(boolean includeInterval) {
        double result = Double.NaN;
        Range r = getDomainBounds(includeInterval);
        if (r != null) {
            result = r.getUpperBound();
        }
        return result;
    }

    
    @Override
    public Range getDomainBounds(boolean includeInterval) {
        Range result = null;
        for (TimeSeries<S> series : this.data) {
            int count = series.getItemCount();
            if (count > 0) {
                RegularTimePeriod start = series.getTimePeriod(0);
                RegularTimePeriod end = series.getTimePeriod(count - 1);
                Range temp;
                if (!includeInterval) {
                    temp = new Range(getX(start), getX(end));
                }
                else {
                    temp = new Range(
                            start.getFirstMillisecond(this.workingCalendar),
                            end.getLastMillisecond(this.workingCalendar));
                }
                result = Range.combine(result, temp);
            }
        }
        return result;
    }

    
    @Override
    public Range getDomainBounds(List visibleSeriesKeys,
            boolean includeInterval) {
        Range result = null;
        for (Object visibleSeriesKey : visibleSeriesKeys) {
            Comparable seriesKey = (Comparable) visibleSeriesKey;
            TimeSeries<S> series = getSeries((S) seriesKey);
            int count = series.getItemCount();
            if (count > 0) {
                RegularTimePeriod start = series.getTimePeriod(0);
                RegularTimePeriod end = series.getTimePeriod(count - 1);
                Range temp;
                if (!includeInterval) {
                    temp = new Range(getX(start), getX(end));
                }
                else {
                    temp = new Range(
                            start.getFirstMillisecond(this.workingCalendar),
                            end.getLastMillisecond(this.workingCalendar));
                }
                result = Range.combine(result, temp);
            }
        }
        return result;
    }

    
    public Range getRangeBounds(boolean includeInterval) {
        Range result = null;
        for (TimeSeries<S> series : this.data) {
            Range r = new Range(series.getMinY(), series.getMaxY());
            result = Range.combineIgnoringNaN(result, r);
        }
        return result;
    }

    
    @Override
    public Range getRangeBounds(List visibleSeriesKeys, Range xRange,
            boolean includeInterval) {
        Range result = null;
        for (Object visibleSeriesKey : visibleSeriesKeys) {
            Comparable seriesKey = (Comparable) visibleSeriesKey;
            TimeSeries<S> series = getSeries((S) seriesKey);
            Range r = series.findValueRange(xRange, this.xPosition,
                    this.workingCalendar);
            result = Range.combineIgnoringNaN(result, r);
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
        Series s = (Series) e.getSource();
        if (getSeriesIndex(s.getKey()) == -1) {
            throw new IllegalStateException("Receiving events from a series " +
                    "that does not belong to this collection.");
        }
        // check if the new series name already exists for another series
        Comparable key = (Comparable) e.getNewValue();
        if (getSeriesIndex(key) >= 0) {
            throw new PropertyVetoException("Duplicate key2", e);
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimeSeriesCollection)) {
            return false;
        }
        TimeSeriesCollection that = (TimeSeriesCollection) obj;
        if (this.xPosition != that.xPosition) {
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
        result = this.data.hashCode();
        result = 29 * result + (this.workingCalendar != null
                ? this.workingCalendar.hashCode() : 0);
        result = 29 * result + (this.xPosition != null
                ? this.xPosition.hashCode() : 0);
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        TimeSeriesCollection clone = (TimeSeriesCollection) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        clone.workingCalendar = (Calendar) this.workingCalendar.clone();
        return clone;
    }

}
