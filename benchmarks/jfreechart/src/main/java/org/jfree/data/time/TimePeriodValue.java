

package org.jfree.data.time;

import java.io.Serializable;
import org.jfree.chart.internal.Args;


public class TimePeriodValue implements Cloneable, Serializable {

    
    private static final long serialVersionUID = 3390443360845711275L;

    
    private TimePeriod period;

    
    private Number value;

    
    public TimePeriodValue(TimePeriod period, Number value) {
        Args.nullNotPermitted(period, "period");
        this.period = period;
        this.value = value;
    }

    
    public TimePeriodValue(TimePeriod period, double value) {
        this(period, Double.valueOf(value));
    }

    
    public TimePeriod getPeriod() {
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
        if (!(obj instanceof TimePeriodValue)) {
            return false;
        }

        TimePeriodValue timePeriodValue = (TimePeriodValue) obj;

        if (this.period != null ? !this.period.equals(timePeriodValue.period)
                : timePeriodValue.period != null) {
            return false;
        }
        if (this.value != null ? !this.value.equals(timePeriodValue.value)
                : timePeriodValue.value != null) {
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
    public Object clone() {
        Object clone = null;
        try {
            clone = super.clone();
        }
        catch (CloneNotSupportedException e) { // won't get here...
            throw new RuntimeException(e);
        }
        return clone;
    }

    
    @Override
    public String toString() {
        return "TimePeriodValue[" + getPeriod() + "," + getValue() + "]";
    }

}
