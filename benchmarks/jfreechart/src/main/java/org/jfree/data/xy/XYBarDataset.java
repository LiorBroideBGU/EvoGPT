

package org.jfree.data.xy;

import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;


public class XYBarDataset<S extends Comparable<S>> 
        extends AbstractIntervalXYDataset<S>
        implements IntervalXYDataset<S>, DatasetChangeListener, PublicCloneable {

    
    private XYDataset<S> underlying;

    
    private double barWidth;

    
    public XYBarDataset(XYDataset<S> underlying, double barWidth) {
        this.underlying = underlying;
        this.underlying.addChangeListener(this);
        this.barWidth = barWidth;
    }

    
    public XYDataset<S> getUnderlyingDataset() {
        return this.underlying;
    }

    
    public double getBarWidth() {
        return this.barWidth;
    }

    
    public void setBarWidth(double barWidth) {
        this.barWidth = barWidth;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    @Override
    public int getSeriesCount() {
        return this.underlying.getSeriesCount();
    }

    
    @Override
    public S getSeriesKey(int series) {
        return this.underlying.getSeriesKey(series);
    }

    
    @Override
    public int getItemCount(int series) {
        return this.underlying.getItemCount(series);
    }

    
    @Override
    public Number getX(int series, int item) {
        return this.underlying.getX(series, item);
    }

    
    @Override
    public double getXValue(int series, int item) {
        return this.underlying.getXValue(series, item);
    }

    
    @Override
    public Number getY(int series, int item) {
        return this.underlying.getY(series, item);
    }

    
    @Override
    public double getYValue(int series, int item) {
        return this.underlying.getYValue(series, item);
    }

    
    @Override
    public Number getStartX(int series, int item) {
        Number result = null;
        Number xnum = this.underlying.getX(series, item);
        if (xnum != null) {
             result = xnum.doubleValue() - this.barWidth / 2.0;
        }
        return result;
    }

    
    @Override
    public double getStartXValue(int series, int item) {
        return getXValue(series, item) - this.barWidth / 2.0;
    }

    
    @Override
    public Number getEndX(int series, int item) {
        Number result = null;
        Number xnum = this.underlying.getX(series, item);
        if (xnum != null) {
             result = xnum.doubleValue() + this.barWidth / 2.0;
        }
        return result;
    }

    
    @Override
    public double getEndXValue(int series, int item) {
        return getXValue(series, item) + this.barWidth / 2.0;
    }

    
    @Override
    public Number getStartY(int series, int item) {
        return this.underlying.getY(series, item);
    }

    
    @Override
    public double getStartYValue(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        return this.underlying.getY(series, item);
    }

    
    @Override
    public double getEndYValue(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public void datasetChanged(DatasetChangeEvent event) {
        notifyListeners(event);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYBarDataset)) {
            return false;
        }
        XYBarDataset<S> that = (XYBarDataset) obj;
        if (!this.underlying.equals(that.underlying)) {
            return false;
        }
        if (this.barWidth != that.barWidth) {
            return false;
        }
        return true;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        XYBarDataset clone = (XYBarDataset) super.clone();
        if (this.underlying instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.underlying;
            clone.underlying = (XYDataset) pc.clone();
        }
        return clone;
    }

}
