

package org.jfree.data.category;

import java.io.Serializable;
import java.util.List;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.DefaultKeyedValues2D;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.general.DatasetChangeEvent;


public class DefaultCategoryDataset<R extends Comparable<R>, C extends Comparable<C>> 
        extends AbstractDataset implements CategoryDataset<R, C>, 
        PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -8168173757291644622L;

    
    private DefaultKeyedValues2D<R, C> data;

    
    public DefaultCategoryDataset() {
        this.data = new DefaultKeyedValues2D<>();
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
    public Number getValue(int row, int column) {
        return this.data.getValue(row, column);
    }

    
    @Override
    public R getRowKey(int row) {
        return this.data.getRowKey(row);
    }

    
    @Override
    public int getRowIndex(R key) {
        // defer null argument check
        return this.data.getRowIndex(key);
    }

    
    @Override
    public List<R> getRowKeys() {
        return this.data.getRowKeys();
    }

    
    @Override
    public C getColumnKey(int column) {
        return this.data.getColumnKey(column);
    }

    
    @Override
    public int getColumnIndex(C key) {
        // defer null argument check
        return this.data.getColumnIndex(key);
    }

    
    @Override
    public List<C> getColumnKeys() {
        return this.data.getColumnKeys();
    }

    
    @Override
    public Number getValue(R rowKey, C columnKey) {
        return this.data.getValue(rowKey, columnKey);
    }

    
    public void addValue(Number value, R rowKey, C columnKey) {
        this.data.addValue(value, rowKey, columnKey);
        fireDatasetChanged();
    }

    
    public void addValue(double value, R rowKey, C columnKey) {
        addValue(Double.valueOf(value), rowKey, columnKey);
    }

    
    public void setValue(Number value, R rowKey, C columnKey) {
        this.data.setValue(value, rowKey, columnKey);
        fireDatasetChanged();
    }

    
    public void setValue(double value, R rowKey, C columnKey) {
        setValue(Double.valueOf(value), rowKey, columnKey);
    }

    
    public void incrementValue(double value, R rowKey, C columnKey) {
        double existing = 0.0;
        Number n = getValue(rowKey, columnKey);
        if (n != null) {
            existing = n.doubleValue();
        }
        setValue(existing + value, rowKey, columnKey);
    }

    
    public void removeValue(R rowKey, C columnKey) {
        this.data.removeValue(rowKey, columnKey);
        fireDatasetChanged();
    }

    
    public void removeRow(int rowIndex) {
        this.data.removeRow(rowIndex);
        fireDatasetChanged();
    }

    
    public void removeRow(R rowKey) {
        this.data.removeRow(rowKey);
        fireDatasetChanged();
    }

    
    public void removeColumn(int columnIndex) {
        this.data.removeColumn(columnIndex);
        fireDatasetChanged();
    }

    
    public void removeColumn(C columnKey) {
        this.data.removeColumn(columnKey);
        fireDatasetChanged();
    }

    
    public void clear() {
        this.data.clear();
        fireDatasetChanged();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryDataset)) {
            return false;
        }
        CategoryDataset<R, C> that = (CategoryDataset) obj;
        if (!getRowKeys().equals(that.getRowKeys())) {
            return false;
        }
        if (!getColumnKeys().equals(that.getColumnKeys())) {
            return false;
        }
        int rowCount = getRowCount();
        int colCount = getColumnCount();
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Number v1 = getValue(r, c);
                Number v2 = that.getValue(r, c);
                if (v1 == null) {
                    if (v2 != null) {
                        return false;
                    }
                }
                else if (!v1.equals(v2)) {
                    return false;
                }
            }
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        return this.data.hashCode();
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultCategoryDataset<R, C> clone = (DefaultCategoryDataset) super.clone();
        clone.data = (DefaultKeyedValues2D) this.data.clone();
        return clone;
    }

}
