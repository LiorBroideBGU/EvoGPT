

package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
import org.jfree.data.general.SeriesChangeEvent;


public class XYIntervalSeries<S extends Comparable<S>> 
        extends ComparableObjectSeries<S> {

    
    public XYIntervalSeries(S key) {
        this(key, true, true);
    }

    
    public XYIntervalSeries(S key, boolean autoSort, 
            boolean allowDuplicateXValues) {
        super(key, autoSort, allowDuplicateXValues);
    }

    
    public void add(double x, double xLow, double xHigh, double y, double yLow,
            double yHigh) {
        add(new XYIntervalDataItem(x, xLow, xHigh, y, yLow, yHigh), true);
    }
    
    
    public void add(XYIntervalDataItem item, boolean notify) {
        super.add(item, notify);
    }

    
    public Number getX(int index) {
        XYIntervalDataItem item = (XYIntervalDataItem) getDataItem(index);
        return item.getX();
    }

    
    public double getXLowValue(int index) {
        XYIntervalDataItem item = (XYIntervalDataItem) getDataItem(index);
        return item.getXLowValue();
    }

    
    public double getXHighValue(int index) {
        XYIntervalDataItem item = (XYIntervalDataItem) getDataItem(index);
        return item.getXHighValue();
    }

    
    public double getYValue(int index) {
        XYIntervalDataItem item = (XYIntervalDataItem) getDataItem(index);
        return item.getYValue();
    }

    
    public double getYLowValue(int index) {
        XYIntervalDataItem item = (XYIntervalDataItem) getDataItem(index);
        return item.getYLowValue();
    }

    
    public double getYHighValue(int index) {
        XYIntervalDataItem item = (XYIntervalDataItem) getDataItem(index);
        return item.getYHighValue();
    }

    
    @Override
    public ComparableObjectItem getDataItem(int index) {
        return super.getDataItem(index);
    }

}
