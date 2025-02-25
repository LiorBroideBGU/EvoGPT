

package org.jfree.data.xy;

import org.jfree.data.ComparableObjectItem;
import org.jfree.data.ComparableObjectSeries;
import org.jfree.data.general.SeriesChangeEvent;


public class VectorSeries<S extends Comparable<S>> extends ComparableObjectSeries<S> {

    
    public VectorSeries(S key) {
        this(key, false, true);
    }

    
    public VectorSeries(S key, boolean autoSort, boolean allowDuplicateXValues) {
        super(key, autoSort, allowDuplicateXValues);
    }

    
    public void add(double x, double y, double deltaX, double deltaY) {
        add(new VectorDataItem(x, y, deltaX, deltaY), true);
    }
    
    
    public void add(VectorDataItem item, boolean notify) {
        super.add(item, notify);
    }

    
    @Override
    public ComparableObjectItem remove(int index) {
        VectorDataItem result = (VectorDataItem) this.data.remove(index);
        fireSeriesChanged();
        return result;
    }

    
    public double getXValue(int index) {
        VectorDataItem item = (VectorDataItem) this.getDataItem(index);
        return item.getXValue();
    }

    
    public double getYValue(int index) {
        VectorDataItem item = (VectorDataItem) getDataItem(index);
        return item.getYValue();
    }

    
    public double getVectorXValue(int index) {
        VectorDataItem item = (VectorDataItem) getDataItem(index);
        return item.getVectorX();
    }

    
    public double getVectorYValue(int index) {
        VectorDataItem item = (VectorDataItem) getDataItem(index);
        return item.getVectorY();
    }

    
    @Override
    public ComparableObjectItem getDataItem(int index) {
        // overridden to make public
        return super.getDataItem(index);
    }

}
