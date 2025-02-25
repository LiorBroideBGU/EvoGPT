

package org.jfree.data.time;

import org.jfree.chart.internal.Args;
import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class TimePeriodValues<S extends Comparable<S>> extends Series<S> 
        implements Serializable {

    
    static final long serialVersionUID = -2210593619794989709L;

    
    private List<TimePeriodValue> data;

    
    private int minStartIndex = -1;
    
    
    private int maxStartIndex = -1;
    
    
    private int minMiddleIndex = -1;
    
    
    private int maxMiddleIndex = -1;
    
    
    private int minEndIndex = -1;
    
    
    private int maxEndIndex = -1;

    
    public TimePeriodValues(S name) {
        super(name);
        this.data = new ArrayList<>();
    }

    
    @Override
    public int getItemCount() {
        return this.data.size();
    }

    
    public TimePeriodValue getDataItem(int index) {
        return this.data.get(index);
    }

    
    public TimePeriod getTimePeriod(int index) {
        return getDataItem(index).getPeriod();
    }

    
    public Number getValue(int index) {
        return getDataItem(index).getValue();
    }

    
    public void add(TimePeriodValue item) {
        Args.nullNotPermitted(item, "item");
        this.data.add(item);
        updateBounds(item.getPeriod(), this.data.size() - 1);
        fireSeriesChanged();
    }
    
    
    private void updateBounds(TimePeriod period, int index) {
        
        long start = period.getStart().getTime();
        long end = period.getEnd().getTime();
        long middle = start + ((end - start) / 2);

        if (this.minStartIndex >= 0) {
            long minStart = getDataItem(this.minStartIndex).getPeriod()
                .getStart().getTime();
            if (start < minStart) {
                this.minStartIndex = index;           
            }
        }
        else {
            this.minStartIndex = index;
        }
        
        if (this.maxStartIndex >= 0) {
            long maxStart = getDataItem(this.maxStartIndex).getPeriod()
                .getStart().getTime();
            if (start > maxStart) {
                this.maxStartIndex = index;           
            }
        }
        else {
            this.maxStartIndex = index;
        }
        
        if (this.minMiddleIndex >= 0) {
            long s = getDataItem(this.minMiddleIndex).getPeriod().getStart()
                .getTime();
            long e = getDataItem(this.minMiddleIndex).getPeriod().getEnd()
                .getTime();
            long minMiddle = s + (e - s) / 2;
            if (middle < minMiddle) {
                this.minMiddleIndex = index;           
            }
        }
        else {
            this.minMiddleIndex = index;
        }
        
        if (this.maxMiddleIndex >= 0) {
            long s = getDataItem(this.maxMiddleIndex).getPeriod().getStart()
                .getTime();
            long e = getDataItem(this.maxMiddleIndex).getPeriod().getEnd()
                .getTime();
            long maxMiddle = s + (e - s) / 2;
            if (middle > maxMiddle) {
                this.maxMiddleIndex = index;           
            }
        }
        else {
            this.maxMiddleIndex = index;
        }
        
        if (this.minEndIndex >= 0) {
            long minEnd = getDataItem(this.minEndIndex).getPeriod().getEnd()
                .getTime();
            if (end < minEnd) {
                this.minEndIndex = index;           
            }
        }
        else {
            this.minEndIndex = index;
        }
       
        if (this.maxEndIndex >= 0) {
            long maxEnd = getDataItem(this.maxEndIndex).getPeriod().getEnd()
                .getTime();
            if (end > maxEnd) {
                this.maxEndIndex = index;           
            }
        }
        else {
            this.maxEndIndex = index;
        }
        
    }
    
    
    private void recalculateBounds() {
        this.minStartIndex = -1;
        this.minMiddleIndex = -1;
        this.minEndIndex = -1;
        this.maxStartIndex = -1;
        this.maxMiddleIndex = -1;
        this.maxEndIndex = -1;
        for (int i = 0; i < this.data.size(); i++) {
            TimePeriodValue tpv = this.data.get(i);
            updateBounds(tpv.getPeriod(), i);
        }
    }

    
    public void add(TimePeriod period, double value) {
        TimePeriodValue item = new TimePeriodValue(period, value);
        add(item);
    }

    
    public void add(TimePeriod period, Number value) {
        TimePeriodValue item = new TimePeriodValue(period, value);
        add(item);
    }

    
    public void update(int index, Number value) {
        TimePeriodValue item = getDataItem(index);
        item.setValue(value);
        fireSeriesChanged();
    }

    
    public void delete(int start, int end) {
        for (int i = 0; i <= (end - start); i++) {
            this.data.remove(start);
        }
        recalculateBounds();
        fireSeriesChanged();
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TimePeriodValues)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        TimePeriodValues that = (TimePeriodValues) obj;
        int count = getItemCount();
        if (count != that.getItemCount()) {
            return false;
        }
        for (int i = 0; i < count; i++) {
            if (!getDataItem(i).equals(that.getDataItem(i))) {
                return false;
            }
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = this.data.hashCode();
        result = 29 * result + this.minStartIndex;
        result = 29 * result + this.maxStartIndex;
        result = 29 * result + this.minMiddleIndex;
        result = 29 * result + this.maxMiddleIndex;
        result = 29 * result + this.minEndIndex;
        result = 29 * result + this.maxEndIndex;
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        Object clone = createCopy(0, getItemCount() - 1);
        return clone;
    }

    
    public TimePeriodValues createCopy(int start, int end) 
        throws CloneNotSupportedException {

        TimePeriodValues copy = (TimePeriodValues) super.clone();

        copy.data = new ArrayList<>();
        if (this.data.size() > 0) {
            for (int index = start; index <= end; index++) {
                TimePeriodValue item = this.data.get(index);
                TimePeriodValue clone = (TimePeriodValue) item.clone();
                try {
                    copy.add(clone);
                }
                catch (SeriesException e) {
                    System.err.println("Failed to add cloned item.");
                }
            }
        }
        return copy;

    }
    
    
    public int getMinStartIndex() {
        return this.minStartIndex;
    }
    
    
    public int getMaxStartIndex() {
        return this.maxStartIndex;
    }

    
    public int getMinMiddleIndex() {
        return this.minMiddleIndex;
    }
    
    
    public int getMaxMiddleIndex() {
        return this.maxMiddleIndex;
    }

    
    public int getMinEndIndex() {
        return this.minEndIndex;
    }
    
    
    public int getMaxEndIndex() {
        return this.maxEndIndex;
    }

}
