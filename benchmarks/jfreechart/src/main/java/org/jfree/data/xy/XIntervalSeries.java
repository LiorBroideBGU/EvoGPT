

package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
import org.jfree.data.general.SeriesChangeEvent;


public class XIntervalSeries<S extends Comparable<S>> 
        extends ComparableObjectSeries<S> {

    
    public XIntervalSeries(S key) {
        this(key, true, true);
    }

    
    public XIntervalSeries(S key, boolean autoSort, 
            boolean allowDuplicateXValues) {
        super(key, autoSort, allowDuplicateXValues);
    }

    
    public void add(double x, double xLow, double xHigh, double y) {
        add(new XIntervalDataItem(x, xLow, xHigh, y), true);
    }

    
    public void add(XIntervalDataItem item, boolean notify) {
        super.add(item, notify);
    }

    
    public Number getX(int index) {
        XIntervalDataItem item = (XIntervalDataItem) getDataItem(index);
        return item.getX();
    }

    
    public double getXLowValue(int index) {
        XIntervalDataItem item = (XIntervalDataItem) getDataItem(index);
        return item.getXLowValue();
    }

    
    public double getXHighValue(int index) {
        XIntervalDataItem item = (XIntervalDataItem) getDataItem(index);
        return item.getXHighValue();
    }

    
    public double getYValue(int index) {
        XIntervalDataItem item = (XIntervalDataItem) getDataItem(index);
        return item.getYValue();
    }

    
    @Override
    public ComparableObjectItem getDataItem(int index) {
        return super.getDataItem(index);
    }

}
