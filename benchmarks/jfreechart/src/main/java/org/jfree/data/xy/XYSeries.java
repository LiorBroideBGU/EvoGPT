

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;

import org.jfree.data.general.Series;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.general.SeriesException;


public class XYSeries<K extends Comparable<K>> extends Series<K> 
        implements Cloneable, Serializable {

    
    static final long serialVersionUID = -5908509288197150436L;

    // In version 0.9.12, in response to several developer requests, I changed
    // the 'data' attribute from 'private' to 'protected', so that others can
    // make subclasses that work directly with the underlying data structure.

    
    protected List<XYDataItem> data;

    
    private int maximumItemCount = Integer.MAX_VALUE;

    
    private boolean autoSort;

    
    private boolean allowDuplicateXValues;

    
    private double minX;

    
    private double maxX;

    
    private double minY;

    
    private double maxY;

    
    public XYSeries(K key) {
        this(key, true, true);
    }

    
    public XYSeries(K key, boolean autoSort) {
        this(key, autoSort, true);
    }

    
    public XYSeries(K key, boolean autoSort, boolean allowDuplicateXValues) {
        super(key);
        this.data = new ArrayList<>();
        this.autoSort = autoSort;
        this.allowDuplicateXValues = allowDuplicateXValues;
        this.minX = Double.NaN;
        this.maxX = Double.NaN;
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
    }

    
    public double getMinX() {
        return this.minX;
    }

    
    public double getMaxX() {
        return this.maxX;
    }

    
    public double getMinY() {
        return this.minY;
    }

    
    public double getMaxY() {
        return this.maxY;
    }

    
    private void updateBoundsForAddedItem(XYDataItem item) {
        double x = item.getXValue();
        this.minX = minIgnoreNaN(this.minX, x);
        this.maxX = maxIgnoreNaN(this.maxX, x);
        if (item.getY() != null) {
            double y = item.getYValue();
            this.minY = minIgnoreNaN(this.minY, y);
            this.maxY = maxIgnoreNaN(this.maxY, y);
        }
    }

    
    private void updateBoundsForRemovedItem(XYDataItem item) {
        boolean itemContributesToXBounds = false;
        boolean itemContributesToYBounds = false;
        double x = item.getXValue();
        if (!Double.isNaN(x)) {
            if (x <= this.minX || x >= this.maxX) {
                itemContributesToXBounds = true;
            }
        }
        if (item.getY() != null) {
            double y = item.getYValue();
            if (!Double.isNaN(y)) {
                if (y <= this.minY || y >= this.maxY) {
                    itemContributesToYBounds = true;
                }
            }
        }
        if (itemContributesToYBounds) {
            findBoundsByIteration();
        }
        else if (itemContributesToXBounds) {
            if (getAutoSort()) {
                this.minX = getX(0).doubleValue();
                this.maxX = getX(getItemCount() - 1).doubleValue();
            }
            else {
                findBoundsByIteration();
            }
        }
    }

    
    private void findBoundsByIteration() {
        this.minX = Double.NaN;
        this.maxX = Double.NaN;
        this.minY = Double.NaN;
        this.maxY = Double.NaN;
        for (XYDataItem item : this.data) {
            updateBoundsForAddedItem(item);
        }
    }

    
    public boolean getAutoSort() {
        return this.autoSort;
    }

    
    public boolean getAllowDuplicateXValues() {
        return this.allowDuplicateXValues;
    }

    
    @Override
    public int getItemCount() {
        return this.data.size();
    }

    
    public List<XYDataItem> getItems() {
        return Collections.unmodifiableList(this.data);
    }

    
    public int getMaximumItemCount() {
        return this.maximumItemCount;
    }

    
    public void setMaximumItemCount(int maximum) {
        this.maximumItemCount = maximum;
        int remove = this.data.size() - maximum;
        if (remove > 0) {
            this.data.subList(0, remove).clear();
            findBoundsByIteration();
            fireSeriesChanged();
        }
    }

    
    public void add(XYDataItem item) {
        // argument checking delegated...
        add(item, true);
    }

    
    public void add(double x, double y) {
        add(Double.valueOf(x), Double.valueOf(y), true);
    }

    
    public void add(double x, double y, boolean notify) {
        add(Double.valueOf(x), Double.valueOf(y), notify);
    }

    
    public void add(double x, Number y) {
        add(Double.valueOf(x), y);
    }

    
    public void add(double x, Number y, boolean notify) {
        add(Double.valueOf(x), y, notify);
    }

    
    public void add(Number x, Number y) {
        // argument checking delegated...
        add(x, y, true);
    }

    
    public void add(Number x, Number y, boolean notify) {
        // delegate argument checking to XYDataItem...
        XYDataItem item = new XYDataItem(x, y);
        add(item, notify);
    }

    
    public void add(XYDataItem item, boolean notify) {
        Args.nullNotPermitted(item, "item");
        item = (XYDataItem) item.clone();
        if (this.autoSort) {
            int index = Collections.binarySearch(this.data, item);
            if (index < 0) {
                this.data.add(-index - 1, item);
            }
            else {
                if (this.allowDuplicateXValues) {
                    // need to make sure we are adding *after* any duplicates
                    int size = this.data.size();
                    while (index < size && item.compareTo(
                            this.data.get(index)) == 0) {
                        index++;
                    }
                    if (index < this.data.size()) {
                        this.data.add(index, item);
                    }
                    else {
                        this.data.add(item);
                    }
                }
                else {
                    throw new SeriesException("X-value already exists.");
                }
            }
        }
        else {
            if (!this.allowDuplicateXValues) {
                // can't allow duplicate values, so we need to check whether
                // there is an item with the given x-value already
                int index = indexOf(item.getX());
                if (index >= 0) {
                    throw new SeriesException("X-value already exists.");
                }
            }
            this.data.add(item);
        }
        updateBoundsForAddedItem(item);
        if (getItemCount() > this.maximumItemCount) {
            XYDataItem removed = this.data.remove(0);
            updateBoundsForRemovedItem(removed);
        }
        if (notify) {
            fireSeriesChanged();
        }
    }

    
    public void delete(int start, int end) {
        this.data.subList(start, end + 1).clear();
        findBoundsByIteration();
        fireSeriesChanged();
    }

    
    public XYDataItem remove(int index) {
        XYDataItem removed = this.data.remove(index);
        updateBoundsForRemovedItem(removed);
        fireSeriesChanged();
        return removed;
    }

    
    public XYDataItem remove(Number x) {
        return remove(indexOf(x));
    }

    
    public void clear() {
        if (this.data.size() > 0) {
            this.data.clear();
            this.minX = Double.NaN;
            this.maxX = Double.NaN;
            this.minY = Double.NaN;
            this.maxY = Double.NaN;
            fireSeriesChanged();
        }
    }

    
    public XYDataItem getDataItem(int index) {
        XYDataItem item = this.data.get(index);
        return (XYDataItem) item.clone();
    }

    
    XYDataItem getRawDataItem(int index) {
        return this.data.get(index);
    }

    
    public Number getX(int index) {
        return getRawDataItem(index).getX();
    }

    
    public Number getY(int index) {
        return getRawDataItem(index).getY();
    }

    
    private double minIgnoreNaN(double a, double b) {
        if (Double.isNaN(a)) {
            return b;
        }
        if (Double.isNaN(b)) {
            return a;
        }
        return Math.min(a, b);
    }

    
    private double maxIgnoreNaN(double a, double b) {
        if (Double.isNaN(a)) {
            return b;
        }
        if (Double.isNaN(b)) {
            return a;
        }
        return Math.max(a, b);
    }

    
    public void updateByIndex(int index, Number y) {
        XYDataItem item = getRawDataItem(index);

        // figure out if we need to iterate through all the y-values
        boolean iterate = false;
        double oldY = item.getYValue();
        if (!Double.isNaN(oldY)) {
            iterate = oldY <= this.minY || oldY >= this.maxY;
        }
        item.setY(y);

        if (iterate) {
            findBoundsByIteration();
        }
        else if (y != null) {
            double yy = y.doubleValue();
            this.minY = minIgnoreNaN(this.minY, yy);
            this.maxY = maxIgnoreNaN(this.maxY, yy);
        }
        fireSeriesChanged();
    }

    
    public void update(Number x, Number y) {
        int index = indexOf(x);
        if (index < 0) {
            throw new SeriesException("No observation for x = " + x);
        }
        updateByIndex(index, y);
    }

    
    public XYDataItem addOrUpdate(double x, double y) {
        return addOrUpdate(Double.valueOf(x), Double.valueOf(y));
    }

    
    public XYDataItem addOrUpdate(Number x, Number y) {
        // defer argument checking
        return addOrUpdate(new XYDataItem(x, y));
    }

    
    public XYDataItem addOrUpdate(XYDataItem item) {
        Args.nullNotPermitted(item, "item");
        if (this.allowDuplicateXValues) {
            add(item);
            return null;
        }

        // if we get to here, we know that duplicate X values are not permitted
        XYDataItem overwritten = null;
        int index = indexOf(item.getX());
        if (index >= 0) {
            XYDataItem existing = this.data.get(index);
            overwritten = (XYDataItem) existing.clone();
            // figure out if we need to iterate through all the y-values
            boolean iterate = false;
            double oldY = existing.getYValue();
            if (!Double.isNaN(oldY)) {
                iterate = oldY <= this.minY || oldY >= this.maxY;
            }
            existing.setY(item.getY());

            if (iterate) {
                findBoundsByIteration();
            }
            else if (item.getY() != null) {
                double yy = item.getY().doubleValue();
                this.minY = minIgnoreNaN(this.minY, yy);
                this.maxY = maxIgnoreNaN(this.maxY, yy);
            }
        }
        else {
            // if the series is sorted, the negative index is a result from
            // Collections.binarySearch() and tells us where to insert the
            // new item...otherwise it will be just -1 and we should just
            // append the value to the list...
            item = (XYDataItem) item.clone();
            if (this.autoSort) {
                this.data.add(-index - 1, item);
            }
            else {
                this.data.add(item);
            }
            updateBoundsForAddedItem(item);

            // check if this addition will exceed the maximum item count...
            if (getItemCount() > this.maximumItemCount) {
                XYDataItem removed = this.data.remove(0);
                updateBoundsForRemovedItem(removed);
            }
        }
        fireSeriesChanged();
        return overwritten;
    }

    
    public int indexOf(Number x) {
        if (this.autoSort) {
            return Collections.binarySearch(this.data, new XYDataItem(x, null));
        }
        else {
            for (int i = 0; i < this.data.size(); i++) {
                XYDataItem item = this.data.get(i);
                if (item.getX().equals(x)) {
                    return i;
                }
            }
            return -1;
        }
    }

    
    public double[][] toArray() {
        int itemCount = getItemCount();
        double[][] result = new double[2][itemCount];
        for (int i = 0; i < itemCount; i++) {
            result[0][i] = this.getX(i).doubleValue();
            Number y = getY(i);
            if (y != null) {
                result[1][i] = y.doubleValue();
            }
            else {
                result[1][i] = Double.NaN;
            }
        }
        return result;
    }

    
    @Override 
    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        XYSeries<K> clone = (XYSeries) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        return clone;
    }

    
    @SuppressWarnings("unchecked")
    public XYSeries<K> createCopy(int start, int end)
            throws CloneNotSupportedException {

        XYSeries<K> copy = (XYSeries) super.clone();
        copy.data = new ArrayList<>();
        if (!this.data.isEmpty()) {
            for (int index = start; index <= end; index++) {
                XYDataItem item = this.data.get(index);
                XYDataItem clone = CloneUtils.clone(item);
                try {
                    copy.add(clone);
                }
                catch (SeriesException e) {
                    throw new RuntimeException(
                            "Unable to add cloned data item.", e);
                }
            }
        }
        return copy;

    }

    
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof XYSeries)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        XYSeries<K> that = (XYSeries) obj;
        if (this.maximumItemCount != that.maximumItemCount) {
            return false;
        }
        if (this.autoSort != that.autoSort) {
            return false;
        }
        if (this.allowDuplicateXValues != that.allowDuplicateXValues) {
            return false;
        }
        if (!Objects.equals(this.data, that.data)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        // it is too slow to look at every data item, so let's just look at
        // the first, middle and last items...
        int count = getItemCount();
        if (count > 0) {
            XYDataItem item = getRawDataItem(0);
            result = 29 * result + item.hashCode();
        }
        if (count > 1) {
            XYDataItem item = getRawDataItem(count - 1);
            result = 29 * result + item.hashCode();
        }
        if (count > 2) {
            XYDataItem item = getRawDataItem(count / 2);
            result = 29 * result + item.hashCode();
        }
        result = 29 * result + this.maximumItemCount;
        result = 29 * result + (this.autoSort ? 1 : 0);
        result = 29 * result + (this.allowDuplicateXValues ? 1 : 0);
        return result;
    }

}

