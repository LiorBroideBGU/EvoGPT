

package org.jfree.data.statistics;

import java.util.List;
import java.util.Objects;

import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.KeyedObjects2D;
import org.jfree.data.Range;
import org.jfree.data.RangeInfo;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;


public class DefaultBoxAndWhiskerCategoryDataset<R extends Comparable<R>, 
        C extends Comparable<C>> extends AbstractDataset
        implements BoxAndWhiskerCategoryDataset<R, C>, RangeInfo, PublicCloneable {

    
    protected KeyedObjects2D<R, C> data;

    
    private double minimumRangeValue;

    
    private int minimumRangeValueRow;

    
    private int minimumRangeValueColumn;

    
    private double maximumRangeValue;

    
    private int maximumRangeValueRow;

    
    private int maximumRangeValueColumn;

    
    public DefaultBoxAndWhiskerCategoryDataset() {
        this.data = new KeyedObjects2D<>();
        this.minimumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValue = Double.NaN;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
    }

    
    public void add(List<? extends Number> list, R rowKey, C columnKey) {
        BoxAndWhiskerItem item = BoxAndWhiskerCalculator
                .calculateBoxAndWhiskerStatistics(list);
        add(item, rowKey, columnKey);
    }

    
    public void add(BoxAndWhiskerItem item, R rowKey, C columnKey) {

        this.data.addObject(item, rowKey, columnKey);

        // update cached min and max values
        int r = this.data.getRowIndex(rowKey);
        int c = this.data.getColumnIndex(columnKey);
        if ((this.maximumRangeValueRow == r && this.maximumRangeValueColumn
                == c) || (this.minimumRangeValueRow == r
                && this.minimumRangeValueColumn == c))  {
            updateBounds();
        }
        else {

            double minval = Double.NaN;
            if (item.getMinOutlier() != null) {
                minval = item.getMinOutlier().doubleValue();
            }
            double maxval = Double.NaN;
            if (item.getMaxOutlier() != null) {
                maxval = item.getMaxOutlier().doubleValue();
            }

            if (Double.isNaN(this.maximumRangeValue)) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }
            else if (maxval > this.maximumRangeValue) {
                this.maximumRangeValue = maxval;
                this.maximumRangeValueRow = r;
                this.maximumRangeValueColumn = c;
            }

            if (Double.isNaN(this.minimumRangeValue)) {
                this.minimumRangeValue = minval;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            }
            else if (minval < this.minimumRangeValue) {
                this.minimumRangeValue = minval;
                this.minimumRangeValueRow = r;
                this.minimumRangeValueColumn = c;
            }
        }

        fireDatasetChanged();

    }

    
    public void remove(R rowKey, C columnKey) {
        // defer null argument checks
        int r = getRowIndex(rowKey);
        int c = getColumnIndex(columnKey);
        this.data.removeObject(rowKey, columnKey);

        // if this cell held a maximum and/or minimum value, we'll need to
        // update the cached bounds...
        if ((this.maximumRangeValueRow == r && this.maximumRangeValueColumn
                == c) || (this.minimumRangeValueRow == r
                && this.minimumRangeValueColumn == c))  {
            updateBounds();
        }

        fireDatasetChanged();
    }

    
    public void removeRow(int rowIndex) {
        this.data.removeRow(rowIndex);
        updateBounds();
        fireDatasetChanged();
    }

    
    public void removeRow(R rowKey) {
        this.data.removeRow(rowKey);
        updateBounds();
        fireDatasetChanged();
    }

    
    public void removeColumn(int columnIndex) {
        this.data.removeColumn(columnIndex);
        updateBounds();
        fireDatasetChanged();
    }

    
    public void removeColumn(C columnKey) {
        this.data.removeColumn(columnKey);
        updateBounds();
        fireDatasetChanged();
    }

    
    public void clear() {
        this.data.clear();
        updateBounds();
        fireDatasetChanged();
    }

    
    public BoxAndWhiskerItem getItem(int row, int column) {
        return (BoxAndWhiskerItem) this.data.getObject(row, column);
    }

    
    @Override
    public Number getValue(int row, int column) {
        return getMedianValue(row, column);
    }

    
    @Override
    public Number getValue(R rowKey, C columnKey) {
        return getMedianValue(rowKey, columnKey);
    }

    
    @Override
    public Number getMeanValue(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row,
                column);
        if (item != null) {
            result = item.getMean();
        }
        return result;
    }

    
    @Override
    public Number getMeanValue(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getMean();
        }
        return result;
    }

    
    @Override
    public Number getMedianValue(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(row,
                column);
        if (item != null) {
            result = item.getMedian();
        }
        return result;
    }

    
    @Override
    public Number getMedianValue(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getMedian();
        }
        return result;
    }

    
    @Override
    public Number getQ1Value(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getQ1();
        }
        return result;
    }

    
    @Override
    public Number getQ1Value(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getQ1();
        }
        return result;
    }

    
    @Override
    public Number getQ3Value(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getQ3();
        }
        return result;
    }

    
    @Override
    public Number getQ3Value(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getQ3();
        }
        return result;
    }

    
    @Override
    public int getColumnIndex(C key) {
        return this.data.getColumnIndex(key);
    }

    
    @Override
    public C getColumnKey(int column) {
        return this.data.getColumnKey(column);
    }

    
    @Override
    public List<C> getColumnKeys() {
        return this.data.getColumnKeys();
    }

    
    @Override
    public int getRowIndex(R key) {
        // defer null argument check
        return this.data.getRowIndex(key);
    }

    
    @Override
    public R getRowKey(int row) {
        return this.data.getRowKey(row);
    }

    
    @Override
    public List<R> getRowKeys() {
        return this.data.getRowKeys();
    }

    
    @Override
    public int getRowCount() {
        return this.data.getRowCount();
    }

    
    @Override
    public int getColumnCount() {
        return this.data.getColumnCount();
    }

    
    @Override
    public double getRangeLowerBound(boolean includeInterval) {
        return this.minimumRangeValue;
    }

    
    @Override
    public double getRangeUpperBound(boolean includeInterval) {
        return this.maximumRangeValue;
    }

    
    @Override
    public Range getRangeBounds(boolean includeInterval) {
        return new Range(this.minimumRangeValue, this.maximumRangeValue);
    }

    
    @Override
    public Number getMinRegularValue(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getMinRegularValue();
        }
        return result;
    }

    
    @Override
    public Number getMinRegularValue(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getMinRegularValue();
        }
        return result;
    }

    
    @Override
    public Number getMaxRegularValue(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getMaxRegularValue();
        }
        return result;
    }

    
    @Override
    public Number getMaxRegularValue(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getMaxRegularValue();
        }
        return result;
    }

    
    @Override
    public Number getMinOutlier(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getMinOutlier();
        }
        return result;
    }

    
    @Override
    public Number getMinOutlier(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getMinOutlier();
        }
        return result;
    }

    
    @Override
    public Number getMaxOutlier(int row, int column) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getMaxOutlier();
        }
        return result;
    }

    
    @Override
    public Number getMaxOutlier(R rowKey, C columnKey) {
        Number result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getMaxOutlier();
        }
        return result;
    }

    
    @Override
    public List<? extends Number> getOutliers(int row, int column) {
        List result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                row, column);
        if (item != null) {
            result = item.getOutliers();
        }
        return result;
    }

    
    @Override
    public List<? extends Number> getOutliers(R rowKey, C columnKey) {
        List result = null;
        BoxAndWhiskerItem item = (BoxAndWhiskerItem) this.data.getObject(
                rowKey, columnKey);
        if (item != null) {
            result = item.getOutliers();
        }
        return result;
    }

    
    private void updateBounds() {
        this.minimumRangeValue = Double.NaN;
        this.minimumRangeValueRow = -1;
        this.minimumRangeValueColumn = -1;
        this.maximumRangeValue = Double.NaN;
        this.maximumRangeValueRow = -1;
        this.maximumRangeValueColumn = -1;
        int rowCount = getRowCount();
        int columnCount = getColumnCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < columnCount; c++) {
                BoxAndWhiskerItem item = getItem(r, c);
                if (item != null) {
                    Number min = item.getMinOutlier();
                    if (min != null) {
                        double minv = min.doubleValue();
                        if (!Double.isNaN(minv)) {
                            if (minv < this.minimumRangeValue || Double.isNaN(
                                    this.minimumRangeValue)) {
                                this.minimumRangeValue = minv;
                                this.minimumRangeValueRow = r;
                                this.minimumRangeValueColumn = c;
                            }
                        }
                    }
                    Number max = item.getMaxOutlier();
                    if (max != null) {
                        double maxv = max.doubleValue();
                        if (!Double.isNaN(maxv)) {
                            if (maxv > this.maximumRangeValue || Double.isNaN(
                                    this.maximumRangeValue)) {
                                this.maximumRangeValue = maxv;
                                this.maximumRangeValueRow = r;
                                this.maximumRangeValueColumn = c;
                            }
                        }
                    }
                }
            }
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof DefaultBoxAndWhiskerCategoryDataset) {
            DefaultBoxAndWhiskerCategoryDataset dataset
                    = (DefaultBoxAndWhiskerCategoryDataset) obj;
            return Objects.equals(this.data, dataset.data);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 23 * hash + Objects.hashCode( this.data );
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultBoxAndWhiskerCategoryDataset<R, C> clone
                = (DefaultBoxAndWhiskerCategoryDataset) super.clone();
        clone.data = (KeyedObjects2D<R, C>) this.data.clone();
        return clone;
    }

}
