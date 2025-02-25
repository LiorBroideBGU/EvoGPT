

package org.jfree.data.time;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;


public class FixedMillisecond extends RegularTimePeriod
        implements Serializable {

    
    private static final long serialVersionUID = 7867521484545646931L;

    
    private final long time;

    
    public FixedMillisecond() {
        this(System.currentTimeMillis());
    }

    
    public FixedMillisecond(long millisecond) {
        this.time = millisecond;
    }

    
    public FixedMillisecond(Date time) {
        this(time.getTime());
    }

    
    public Date getTime() {
        return new Date(this.time);
    }

    
    @Override
    public void peg(Calendar calendar) {
        // nothing to do
    }

    
    @Override
    public RegularTimePeriod previous() {
        RegularTimePeriod result = null;
        long t = this.time;
        if (t != Long.MIN_VALUE) {
            result = new FixedMillisecond(t - 1);
        }
        return result;
    }

    
    @Override
    public RegularTimePeriod next() {
        RegularTimePeriod result = null;
        long t = this.time;
        if (t != Long.MAX_VALUE) {
            result = new FixedMillisecond(t + 1);
        }
        return result;
    }

    
    @Override
    public boolean equals(Object object) {
        if (object instanceof FixedMillisecond) {
            FixedMillisecond m = (FixedMillisecond) object;
            return this.time == m.getFirstMillisecond();
        }
        else {
            return false;
        }

    }

    
    @Override
    public int hashCode() {
        return (int) this.time;
    }

    
    @Override
    public int compareTo(Object o1) {

        int result;
        long difference;

        // CASE 1 : Comparing to another Second object
        // -------------------------------------------
        if (o1 instanceof FixedMillisecond) {
            FixedMillisecond t1 = (FixedMillisecond) o1;
            difference = this.time - t1.time;
            if (difference > 0) {
                result = 1;
            }
            else {
                if (difference < 0) {
                   result = -1;
                }
                else {
                    result = 0;
                }
            }
        }

        // CASE 2 : Comparing to another TimePeriod object
        // -----------------------------------------------
        else if (o1 instanceof RegularTimePeriod) {
            // more difficult case - evaluate later...
            result = 0;
        }

        // CASE 3 : Comparing to a non-TimePeriod object
        // ---------------------------------------------
        else {
            // consider time periods to be ordered after general objects
            result = 1;
        }

        return result;

    }

    
    @Override
    public long getFirstMillisecond() {
        return this.time;
    }


    
    @Override
    public long getFirstMillisecond(Calendar calendar) {
        return this.time;
    }

    
    @Override
    public long getLastMillisecond() {
        return this.time;
    }

    
    @Override
    public long getLastMillisecond(Calendar calendar) {
        return this.time;
    }

    
    @Override
    public long getMiddleMillisecond() {
        return this.time;
    }

    
    @Override
    public long getMiddleMillisecond(Calendar calendar) {
        return this.time;
    }

    
    @Override
    public long getSerialIndex() {
        return this.time;
    }

}
