

package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
import org.jfree.data.general.SeriesChangeEvent;


public class YIntervalSeries<S extends Comparable<S>> extends ComparableObjectSeries<S> {

    
    public YIntervalSeries(S key) {
        this(key, true, true);
    }

    
    public YIntervalSeries(S key, boolean autoSort,
            boolean allowDuplicateXValues) {
        super(key, autoSort, allowDuplicateXValues);
    }

    
    public void add(double x, double y, double yLow, double yHigh) {
        add(new YIntervalDataItem(x, y, yLow, yHigh), true);
    }
    
    
    public void add(YIntervalDataItem item, boolean notify) {
        super.add(item, notify);
    }

    
    public Number getX(int index) {
        YIntervalDataItem item = (YIntervalDataItem) getDataItem(index);
        return item.getX();
    }

    
    public double getYValue(int index) {
        YIntervalDataItem item = (YIntervalDataItem) getDataItem(index);
        return item.getYValue();
    }

    
    public double getYLowValue(int index) {
        YIntervalDataItem item = (YIntervalDataItem) getDataItem(index);
        return item.getYLowValue();
    }

    
    public double getYHighValue(int index) {
        YIntervalDataItem item = (YIntervalDataItem) getDataItem(index);
        return item.getYHighValue();
    }

    
    @Override
    public ComparableObjectItem getDataItem(int index) {
        return super.getDataItem(index);
    }

}
