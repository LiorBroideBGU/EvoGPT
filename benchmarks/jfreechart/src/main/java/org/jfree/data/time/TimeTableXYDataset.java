

package org.jfree.data.time;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;


public class TimeTableXYDataset extends AbstractIntervalXYDataset
        implements Cloneable, PublicCloneable, IntervalXYDataset, DomainInfo,
                   TableXYDataset {

    
    private DefaultKeyedValues2D values;

    
    private boolean domainIsPointsInTime;

    
    private TimePeriodAnchor xPosition;

    
    private Calendar workingCalendar;

    
    public TimeTableXYDataset() {
        // defer argument checking
        this(TimeZone.getDefault(), Locale.getDefault());
    }

    
    public TimeTableXYDataset(TimeZone zone) {
        // defer argument checking
        this(zone, Locale.getDefault());
    }

    
    public TimeTableXYDataset(TimeZone zone, Locale locale) {
        Args.nullNotPermitted(zone, "zone");
        Args.nullNotPermitted(locale, "locale");
        this.values = new DefaultKeyedValues2D(true);
        this.workingCalendar = Calendar.getInstance(zone, locale);
        this.xPosition = TimePeriodAnchor.START;
    }

    
    public boolean getDomainIsPointsInTime() {
        return this.domainIsPointsInTime;
    }

    
    public void setDomainIsPointsInTime(boolean flag) {
        this.domainIsPointsInTime = flag;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    public TimePeriodAnchor getXPosition() {
        return this.xPosition;
    }

    
    public void setXPosition(TimePeriodAnchor anchor) {
        Args.nullNotPermitted(anchor, "anchor");
        this.xPosition = anchor;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    public void add(TimePeriod period, double y, Comparable seriesName) {
        add(period, y, seriesName, true);
    }

    
    public void add(TimePeriod period, Number y, Comparable seriesName,
                    boolean notify) {
        // here's a quirk - the API has been defined in terms of a plain
        // TimePeriod, which cannot make use of the timezone and locale
        // specified in the constructor...so we only do the time zone
        // pegging if the period is an instanceof RegularTimePeriod
        if (period instanceof RegularTimePeriod) {
            RegularTimePeriod p = (RegularTimePeriod) period;
            p.peg(this.workingCalendar);
        }
        this.values.addValue(y, period, seriesName);
        if (notify) {
            fireDatasetChanged();
        }
    }

    
    public void remove(TimePeriod period, Comparable seriesName) {
        remove(period, seriesName, true);
    }

    
    public void remove(TimePeriod period, Comparable seriesName,
            boolean notify) {
        this.values.removeValue(period, seriesName);
        if (notify) {
            fireDatasetChanged();
        }
    }

    
    public void clear() {
        if (this.values.getRowCount() > 0) {
            this.values.clear();
            fireDatasetChanged();
        }
    }

    
    public TimePeriod getTimePeriod(int item) {
        return (TimePeriod) this.values.getRowKey(item);
    }

    
    @Override
    public int getItemCount() {
        return this.values.getRowCount();
    }

    
    @Override
    public int getItemCount(int series) {
        return getItemCount();
    }

    
    @Override
    public int getSeriesCount() {
        return this.values.getColumnCount();
    }

    
    @Override
    public Comparable getSeriesKey(int series) {
        return this.values.getColumnKey(series);
    }

    
    @Override
    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    
    @Override
    public double getXValue(int series, int item) {
        TimePeriod period = (TimePeriod) this.values.getRowKey(item);
        return getXValue(period);
    }

    
    @Override
    public Number getStartX(int series, int item) {
        return getStartXValue(series, item);
    }

    
    @Override
    public double getStartXValue(int series, int item) {
        TimePeriod period = (TimePeriod) this.values.getRowKey(item);
        return period.getStart().getTime();
    }

    
    @Override
    public Number getEndX(int series, int item) {
        return getEndXValue(series, item);
    }

    
    @Override
    public double getEndXValue(int series, int item) {
        TimePeriod period = (TimePeriod) this.values.getRowKey(item);
        return period.getEnd().getTime();
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

    
    private long getXValue(TimePeriod period) {
        long result = 0L;
        if (this.xPosition == TimePeriodAnchor.START) {
            result = period.getStart().getTime();
        }
        else if (this.xPosition == TimePeriodAnchor.MIDDLE) {
            long t0 = period.getStart().getTime();
            long t1 = period.getEnd().getTime();
            result = t0 + (t1 - t0) / 2L;
        }
        else if (this.xPosition == TimePeriodAnchor.END) {
            result = period.getEnd().getTime();
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
        List keys = this.values.getRowKeys();
        if (keys.isEmpty()) {
            return null;
        }

        TimePeriod first = (TimePeriod) keys.get(0);
        TimePeriod last = (TimePeriod) keys.get(keys.size() - 1);

        if (!includeInterval || this.domainIsPointsInTime) {
            return new Range(getXValue(first), getXValue(last));
        }
        else {
            return new Range(first.getStart().getTime(),
                    last.getEnd().getTime());
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimeTableXYDataset)) {
            return false;
        }
        TimeTableXYDataset that = (TimeTableXYDataset) obj;
        if (this.domainIsPointsInTime != that.domainIsPointsInTime) {
            return false;
        }
        if (this.xPosition != that.xPosition) {
            return false;
        }
        if (!this.workingCalendar.getTimeZone().equals(
            that.workingCalendar.getTimeZone())
        ) {
            return false;
        }
        if (!this.values.equals(that.values)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 19 * hash + Objects.hashCode( this.values );
        hash = 19 * hash + ( this.domainIsPointsInTime ? 1 : 0 );
        hash = 19 * hash + Objects.hashCode( this.xPosition );
        hash = 19 * hash + Objects.hashCode( this.workingCalendar );
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        TimeTableXYDataset clone = (TimeTableXYDataset) super.clone();
        clone.values = (DefaultKeyedValues2D) this.values.clone();
        clone.workingCalendar = (Calendar) this.workingCalendar.clone();
        return clone;
    }

}
