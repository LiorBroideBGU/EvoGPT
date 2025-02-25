

package org.jfree.data.time;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.Args;

import org.jfree.data.DomainInfo;
import org.jfree.data.Range;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;


public class TimePeriodValuesCollection extends AbstractIntervalXYDataset
        implements IntervalXYDataset, DomainInfo, Serializable {

    
    private static final long serialVersionUID = -3077934065236454199L;

    
    private List<TimePeriodValues> data;

    
    private TimePeriodAnchor xPosition;

    
    public TimePeriodValuesCollection() {
        this((TimePeriodValues) null);
    }

    
    public TimePeriodValuesCollection(TimePeriodValues series) {
        this.data = new ArrayList<>();
        this.xPosition = TimePeriodAnchor.MIDDLE;
        if (series != null) {
            this.data.add(series);
            series.addChangeListener(this);
        }
    }

    
    public TimePeriodAnchor getXPosition() {
        return this.xPosition;
    }

    
    public void setXPosition(TimePeriodAnchor position) {
        Args.nullNotPermitted(position, "position");
        this.xPosition = position;
    }

    
    @Override
    public int getSeriesCount() {
        return this.data.size();
    }

    
    public TimePeriodValues getSeries(int series) {
        Args.requireInRange(series, "series", 0, getSeriesCount() - 1);
        if ((series < 0) || (series >= getSeriesCount())) {
            throw new IllegalArgumentException("Index 'series' out of range.");
        }
        return this.data.get(series);
    }

    
    @Override
    public Comparable getSeriesKey(int series) {
        // defer argument checking
        return getSeries(series).getKey();
    }

    
    public void addSeries(TimePeriodValues series) {
        Args.nullNotPermitted(series, "series");
        this.data.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }

    
    public void removeSeries(TimePeriodValues series) {
        Args.nullNotPermitted(series, "series");
        this.data.remove(series);
        series.removeChangeListener(this);
        fireDatasetChanged();

    }

    
    public void removeSeries(int index) {
        TimePeriodValues series = getSeries(index);
        if (series != null) {
            removeSeries(series);
        }
    }

    
    @Override
    public int getItemCount(int series) {
        return getSeries(series).getItemCount();
    }

    
    @Override
    public Number getX(int series, int item) {
        TimePeriodValues ts = (TimePeriodValues) this.data.get(series);
        TimePeriodValue dp = ts.getDataItem(item);
        TimePeriod period = dp.getPeriod();
        return getX(period);
    }

    
    private long getX(TimePeriod period) {

        if (this.xPosition == TimePeriodAnchor.START) {
            return period.getStart().getTime();
        }
        else if (this.xPosition == TimePeriodAnchor.MIDDLE) {
            return period.getStart().getTime()
                / 2 + period.getEnd().getTime() / 2;
        }
        else if (this.xPosition == TimePeriodAnchor.END) {
            return period.getEnd().getTime();
        }
        else {
            throw new IllegalStateException("TimePeriodAnchor unknown.");
        }

    }

    
    @Override
    public Number getStartX(int series, int item) {
        TimePeriodValues ts = (TimePeriodValues) this.data.get(series);
        TimePeriodValue dp = ts.getDataItem(item);
        return dp.getPeriod().getStart().getTime();
    }

    
    @Override
    public Number getEndX(int series, int item) {
        TimePeriodValues ts = (TimePeriodValues) this.data.get(series);
        TimePeriodValue dp = ts.getDataItem(item);
        return dp.getPeriod().getEnd().getTime();
    }

    
    @Override
    public Number getY(int series, int item) {
        TimePeriodValues ts = (TimePeriodValues) this.data.get(series);
        TimePeriodValue dp = ts.getDataItem(item);
        return dp.getValue();
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
        boolean interval = includeInterval;
        Range result = null;
        Range temp = null;
        for (TimePeriodValues series : this.data) {
            int count = series.getItemCount();
            if (count > 0) {
                TimePeriod start = series.getTimePeriod(
                        series.getMinStartIndex());
                TimePeriod end = series.getTimePeriod(series.getMaxEndIndex());
                if (!interval) {
                    if (this.xPosition == TimePeriodAnchor.START) {
                        TimePeriod maxStart = series.getTimePeriod(
                                series.getMaxStartIndex());
                        temp = new Range(start.getStart().getTime(),
                                maxStart.getStart().getTime());
                    }
                    else if (this.xPosition == TimePeriodAnchor.MIDDLE) {
                        TimePeriod minMiddle = series.getTimePeriod(
                                series.getMinMiddleIndex());
                        long s1 = minMiddle.getStart().getTime();
                        long e1 = minMiddle.getEnd().getTime();
                        TimePeriod maxMiddle = series.getTimePeriod(
                                series.getMaxMiddleIndex());
                        long s2 = maxMiddle.getStart().getTime();
                        long e2 = maxMiddle.getEnd().getTime();
                        temp = new Range(s1 + (e1 - s1) / 2.0,
                                s2 + (e2 - s2) / 2.0);
                    }
                    else if (this.xPosition == TimePeriodAnchor.END) {
                        TimePeriod minEnd = series.getTimePeriod(
                                series.getMinEndIndex());
                        temp = new Range(minEnd.getEnd().getTime(),
                                end.getEnd().getTime());
                    }
                }
                else {
                    temp = new Range(start.getStart().getTime(),
                            end.getEnd().getTime());
                }
                result = Range.combine(result, temp);
            }
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimePeriodValuesCollection)) {
            return false;
        }
        TimePeriodValuesCollection that = (TimePeriodValuesCollection) obj;
        if (this.xPosition != that.xPosition) {
            return false;
        }
        if (!Objects.equals(this.data, that.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.data);
        hash = 83 * hash + Objects.hashCode(this.xPosition);
        return hash;
    }

}
