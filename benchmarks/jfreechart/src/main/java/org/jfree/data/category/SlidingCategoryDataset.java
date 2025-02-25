

package org.jfree.data.category;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;


public class SlidingCategoryDataset<R extends Comparable<R>, C extends Comparable<C>> 
        extends AbstractDataset implements CategoryDataset<R, C> {

    
    private CategoryDataset<R, C> underlying;

    
    private int firstCategoryIndex;

    
    private int maximumCategoryCount;

    
    public SlidingCategoryDataset(CategoryDataset<R, C> underlying, 
            int firstColumn, int maxColumns) {
        this.underlying = underlying;
        this.firstCategoryIndex = firstColumn;
        this.maximumCategoryCount = maxColumns;
    }

    
    public CategoryDataset<R, C> getUnderlyingDataset() {
        return this.underlying;
    }

    
    public int getFirstCategoryIndex() {
        return this.firstCategoryIndex;
    }

    
    public void setFirstCategoryIndex(int first) {
        if (first < 0 || first >= this.underlying.getColumnCount()) {
            throw new IllegalArgumentException("Invalid index.");
        }
        this.firstCategoryIndex = first;
        fireDatasetChanged();
    }

    
    public int getMaximumCategoryCount() {
        return this.maximumCategoryCount;
    }

    
    public void setMaximumCategoryCount(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("Requires 'max' >= 0.");
        }
        this.maximumCategoryCount = max;
        fireDatasetChanged();
    }

    
    private int lastCategoryIndex() {
        if (this.maximumCategoryCount == 0) {
            return -1;
        }
        return Math.min(this.firstCategoryIndex + this.maximumCategoryCount,
                this.underlying.getColumnCount()) - 1;
    }

    
    @Override
    public int getColumnIndex(C key) {
        int index = this.underlying.getColumnIndex(key);
        if (index >= this.firstCategoryIndex && index <= lastCategoryIndex()) {
            return index - this.firstCategoryIndex;
        }
        return -1;  // we didn't find the key
    }

    
    @Override
    public C getColumnKey(int column) {
        return this.underlying.getColumnKey(column + this.firstCategoryIndex);
    }

    
    @Override
    public List<C> getColumnKeys() {
        List result = new java.util.ArrayList();
        int last = lastCategoryIndex();
        for (int i = this.firstCategoryIndex; i <= last; i++) {
            result.add(this.underlying.getColumnKey(i));
        }
        return Collections.unmodifiableList(result);
    }

    
    @Override
    public int getRowIndex(R key) {
        return this.underlying.getRowIndex(key);
    }

    
    @Override
    public R getRowKey(int row) {
        return this.underlying.getRowKey(row);
    }

    
    @Override
    public List<R> getRowKeys() {
        return this.underlying.getRowKeys();
    }

    
    @Override
    public Number getValue(R rowKey, C columnKey) {
        int r = getRowIndex(rowKey);
        int c = getColumnIndex(columnKey);
        if (c == -1) {
            throw new UnknownKeyException("Unknown columnKey: " + columnKey);
        }
        else if (r == -1) {
            throw new UnknownKeyException("Unknown rowKey: " + rowKey);
        }
        else {
            return this.underlying.getValue(r, c + this.firstCategoryIndex);
        }
    }

    
    @Override
    public int getColumnCount() {
        int last = lastCategoryIndex();
        if (last == -1) {
            return 0;
        }
        else {
            return Math.max(last - this.firstCategoryIndex + 1, 0);
        }
    }

    
    @Override
    public int getRowCount() {
        return this.underlying.getRowCount();
    }

    
    @Override
    public Number getValue(int row, int column) {
        return this.underlying.getValue(row, column + this.firstCategoryIndex);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SlidingCategoryDataset)) {
            return false;
        }
        SlidingCategoryDataset that = (SlidingCategoryDataset) obj;
        if (this.firstCategoryIndex != that.firstCategoryIndex) {
            return false;
        }
        if (this.maximumCategoryCount != that.maximumCategoryCount) {
            return false;
        }
        if (!this.underlying.equals(that.underlying)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 43 * hash + Objects.hashCode(this.underlying);
        hash = 43 * hash + this.firstCategoryIndex;
        hash = 43 * hash + this.maximumCategoryCount;
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        SlidingCategoryDataset<R, C> clone = (SlidingCategoryDataset) super.clone();
        if (this.underlying instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.underlying;
            clone.underlying = (CategoryDataset) pc.clone();
        }
        return clone;
    }

}
