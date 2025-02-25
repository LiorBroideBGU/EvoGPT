

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.internal.Args;


public class TimeSeriesDataItem implements Cloneable, 
        Comparable<TimeSeriesDataItem>, Serializable {

    
    private static final long serialVersionUID = -2235346966016401302L;

    
    private RegularTimePeriod period;

    
    private Number value;

    
    public TimeSeriesDataItem(RegularTimePeriod period, Number value) {
        Args.nullNotPermitted(period, "period");
        this.period = period;
        this.value = value;
    }

    
    public TimeSeriesDataItem(RegularTimePeriod period, double value) {
        this(period, Double.valueOf(value));
    }

    
    public RegularTimePeriod getPeriod() {
        return this.period;
    }

    
    public Number getValue() {
        return this.value;
    }

    
    public void setValue(Number value) {
        this.value = value;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof TimeSeriesDataItem)) {
            return false;
        }
        TimeSeriesDataItem that = (TimeSeriesDataItem) obj;
        if (!Objects.equals(this.period, that.period)) {
            return false;
        }
        if (!Objects.equals(this.value, that.value)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = (this.period != null ? this.period.hashCode() : 0);
        result = 29 * result + (this.value != null ? this.value.hashCode() : 0);
        return result;
    }

    
    @Override
    public int compareTo(TimeSeriesDataItem other) {
        return getPeriod().compareTo(other.getPeriod());
    }

    
    @Override
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        }
        catch (CloneNotSupportedException e) { // won't get here...
            e.printStackTrace();
        }
        return clone;
    }

}
