

package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.xy.AbstractIntervalXYDataset;
import org.jfree.data.xy.IntervalXYDataset;


public class SimpleHistogramDataset<K extends Comparable<K>> 
        extends AbstractIntervalXYDataset implements IntervalXYDataset, 
        Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 7997996479768018443L;

    
    private K key;

    
    private List<SimpleHistogramBin> bins;

    
    private boolean adjustForBinSize;

    
    public SimpleHistogramDataset(K key) {
        Args.nullNotPermitted(key, "key");
        this.key = key;
        this.bins = new ArrayList<>();
        this.adjustForBinSize = true;
    }

    
    public boolean getAdjustForBinSize() {
        return this.adjustForBinSize;
    }

    
    public void setAdjustForBinSize(boolean adjust) {
        this.adjustForBinSize = adjust;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    @Override
    public int getSeriesCount() {
        return 1;
    }

    
    @Override
    public K getSeriesKey(int series) {
        return this.key;
    }

    
    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.ASCENDING;
    }

    
    @Override
    public int getItemCount(int series) {
        return this.bins.size();
    }

    
    public void addBin(SimpleHistogramBin binToAdd) {
        // check that the new bin doesn't overlap with any existing bin
        for (SimpleHistogramBin bin : this.bins) {
            if (binToAdd.overlapsWith(bin)) {
                throw new RuntimeException("Overlapping bin");
            }
        }
        this.bins.add(binToAdd);
        Collections.sort(this.bins);
    }

    
    public void addObservation(double value) {
        addObservation(value, true);
    }

    
    public void addObservation(double value, boolean notify) {
        boolean placed = false;
        Iterator iterator = this.bins.iterator();
        while (iterator.hasNext() && !placed) {
            SimpleHistogramBin bin = (SimpleHistogramBin) iterator.next();
            if (bin.accepts(value)) {
                bin.setItemCount(bin.getItemCount() + 1);
                placed = true;
            }
        }
        if (!placed) {
            throw new RuntimeException("No bin.");
        }
        if (notify) {
            notifyListeners(new DatasetChangeEvent(this, this));
        }
    }

    
    public void addObservations(double[] values) {
        for (int i = 0; i < values.length; i++) {
            addObservation(values[i], false);
        }
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    public void clearObservations() {
        for (SimpleHistogramBin bin : this.bins) {
            bin.setItemCount(0);
        }
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    public void removeAllBins() {
        this.bins = new ArrayList<>();
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    @Override
    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    
    @Override
    public double getXValue(int series, int item) {
        SimpleHistogramBin bin = this.bins.get(item);
        return (bin.getLowerBound() + bin.getUpperBound()) / 2.0;
    }

    
    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public double getYValue(int series, int item) {
        SimpleHistogramBin bin = this.bins.get(item);
        if (this.adjustForBinSize) {
            return bin.getItemCount()
                   / (bin.getUpperBound() - bin.getLowerBound());
        }
        else {
            return bin.getItemCount();
        }
    }

    
    @Override
    public Number getStartX(int series, int item) {
        return getStartXValue(series, item);
    }

    
    @Override
    public double getStartXValue(int series, int item) {
        SimpleHistogramBin bin = this.bins.get(item);
        return bin.getLowerBound();
    }

    
    @Override
    public Number getEndX(int series, int item) {
        return getEndXValue(series, item);
    }

    
    @Override
    public double getEndXValue(int series, int item) {
        SimpleHistogramBin bin = this.bins.get(item);
        return bin.getUpperBound();
    }

    
    @Override
    public Number getStartY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public double getStartYValue(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public Number getEndY(int series, int item) {
        return getY(series, item);
    }

    
    @Override
    public double getEndYValue(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SimpleHistogramDataset)) {
            return false;
        }
        SimpleHistogramDataset that = (SimpleHistogramDataset) obj;
        if (!this.key.equals(that.key)) {
            return false;
        }
        if (this.adjustForBinSize != that.adjustForBinSize) {
            return false;
        }
        if (!this.bins.equals(that.bins)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 11 * hash + Objects.hashCode(this.key);
        hash = 11 * hash + Objects.hashCode(this.bins);
        hash = 11 * hash + (this.adjustForBinSize ? 1 : 0);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        SimpleHistogramDataset clone = (SimpleHistogramDataset) super.clone();
        clone.bins = CloneUtils.cloneList(this.bins);
        return clone;
    }

}
