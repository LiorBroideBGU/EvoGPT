

package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;


public class DefaultKeyedValues2D<R extends Comparable<R>, C extends Comparable<C>> 
        implements KeyedValues2D<R, C>, PublicCloneable, Cloneable, Serializable {

    
    private static final long serialVersionUID = -5514169970951994748L;

    
    private List<R> rowKeys;

    
    private List<C> columnKeys;

    
    private List<DefaultKeyedValues<C>> rows;

    
    private final boolean sortRowKeys;

    
    public DefaultKeyedValues2D() {
        this(false);
    }

    
    public DefaultKeyedValues2D(boolean sortRowKeys) {
        this.rowKeys = new ArrayList<>();
        this.columnKeys = new ArrayList<>();
        this.rows = new ArrayList<>();
        this.sortRowKeys = sortRowKeys;
    }

    
    @Override
    public int getRowCount() {
        return this.rowKeys.size();
    }

    
    @Override
    public int getColumnCount() {
        return this.columnKeys.size();
    }

    
    @Override
    public Number getValue(int row, int column) {
        Number result = null;
        DefaultKeyedValues<C> rowData = this.rows.get(row);
        if (rowData != null) {
            C columnKey = this.columnKeys.get(column);
            // the row may not have an entry for this key, in which case the
            // return value is null
            int index = rowData.getIndex(columnKey);
            if (index >= 0) {
                result = rowData.getValue(index);
            }
        }
        return result;
    }

    
    @Override
    public R getRowKey(int row) {
        return this.rowKeys.get(row);
    }

    
    @Override
    public int getRowIndex(R key) {
        Args.nullNotPermitted(key, "key");
        if (this.sortRowKeys) {
            return Collections.<R>binarySearch(this.rowKeys, key);
        } else {
            return this.rowKeys.indexOf(key);
        }
    }

    
    @Override
    public List<R> getRowKeys() {
        return Collections.unmodifiableList(this.rowKeys);
    }

    
    @Override
    public C getColumnKey(int column) {
        return this.columnKeys.get(column);
    }

    
    @Override
    public int getColumnIndex(C key) {
        Args.nullNotPermitted(key, "key");
        return this.columnKeys.indexOf(key);
    }

    
    @Override
    public List<C> getColumnKeys() {
        return Collections.unmodifiableList(this.columnKeys);
    }

    
    @Override
    public Number getValue(R rowKey, C columnKey) {
        Args.nullNotPermitted(rowKey, "rowKey");
        Args.nullNotPermitted(columnKey, "columnKey");

        // check that the column key is defined in the 2D structure
        if (!(this.columnKeys.contains(columnKey))) {
            throw new UnknownKeyException("Unrecognised columnKey: "
                    + columnKey);
        }

        // now fetch the row data - need to bear in mind that the row
        // structure may not have an entry for the column key, but that we
        // have already checked that the key is valid for the 2D structure
        int row = getRowIndex(rowKey);
        if (row >= 0) {
            DefaultKeyedValues rowData = this.rows.get(row);
            int col = rowData.getIndex(columnKey);
            return (col >= 0 ? rowData.getValue(col) : null);
        }
        else {
            throw new UnknownKeyException("Unrecognised rowKey: " + rowKey);
        }
    }

    
    public void addValue(Number value, R rowKey, C columnKey) {
        // defer argument checking
        setValue(value, rowKey, columnKey);
    }

    
    public void setValue(Number value, R rowKey, C columnKey) {

        DefaultKeyedValues row;
        int rowIndex = getRowIndex(rowKey);

        if (rowIndex >= 0) {
            row = this.rows.get(rowIndex);
        }
        else {
            row = new DefaultKeyedValues();
            if (this.sortRowKeys) {
                rowIndex = -rowIndex - 1;
                this.rowKeys.add(rowIndex, rowKey);
                this.rows.add(rowIndex, row);
            }
            else {
                this.rowKeys.add(rowKey);
                this.rows.add(row);
            }
        }
        row.setValue(columnKey, value);

        int columnIndex = this.columnKeys.indexOf(columnKey);
        if (columnIndex < 0) {
            this.columnKeys.add(columnKey);
        }
    }

    
    public void removeValue(R rowKey, C columnKey) {
        setValue(null, rowKey, columnKey);

        // 1. check whether the row is now empty.
        boolean allNull = true;
        int rowIndex = getRowIndex(rowKey);
        DefaultKeyedValues row = this.rows.get(rowIndex);

        for (int item = 0, itemCount = row.getItemCount(); item < itemCount;
             item++) {
            if (row.getValue(item) != null) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            this.rowKeys.remove(rowIndex);
            this.rows.remove(rowIndex);
        }

        // 2. check whether the column is now empty.
        allNull = true;
        //int columnIndex = getColumnIndex(columnKey);

        for (int item = 0, itemCount = this.rows.size(); item < itemCount;
             item++) {
            row = this.rows.get(item);
            int columnIndex = row.getIndex(columnKey);
            if (columnIndex >= 0 && row.getValue(columnIndex) != null) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            for (int item = 0, itemCount = this.rows.size(); item < itemCount;
                 item++) {
                row = this.rows.get(item);
                int columnIndex = row.getIndex(columnKey);
                if (columnIndex >= 0) {
                    row.removeValue(columnIndex);
                }
            }
            this.columnKeys.remove(columnKey);
        }
    }

    
    public void removeRow(int rowIndex) {
        this.rowKeys.remove(rowIndex);
        this.rows.remove(rowIndex);
    }

    
    public void removeRow(R rowKey) {
        Args.nullNotPermitted(rowKey, "rowKey");
        int index = getRowIndex(rowKey);
        if (index >= 0) {
            removeRow(index);
        }
        else {
            throw new UnknownKeyException("Unknown key: " + rowKey);
        }
    }

    
    public void removeColumn(int columnIndex) {
        C columnKey = getColumnKey(columnIndex);
        removeColumn(columnKey);
    }

    
    public void removeColumn(C columnKey) {
        Args.nullNotPermitted(columnKey, "columnKey");
        if (!this.columnKeys.contains(columnKey)) {
            throw new UnknownKeyException("Unknown key: " + columnKey);
        }
        for (DefaultKeyedValues rowData : this.rows) {
            int index = rowData.getIndex(columnKey);
            if (index >= 0) {
                rowData.removeValue(columnKey);
            }
        }
        this.columnKeys.remove(columnKey);
    }

    
    public void clear() {
        this.rowKeys.clear();
        this.columnKeys.clear();
        this.rows.clear();
    }

    
    @Override
    public boolean equals(Object o) {

        if (o == null) {
            return false;
        }
        if (o == this) {
            return true;
        }

        if (!(o instanceof KeyedValues2D)) {
            return false;
        }
        KeyedValues2D kv2D = (KeyedValues2D) o;
        if (!getRowKeys().equals(kv2D.getRowKeys())) {
            return false;
        }
        if (!getColumnKeys().equals(kv2D.getColumnKeys())) {
            return false;
        }
        int rowCount = getRowCount();
        if (rowCount != kv2D.getRowCount()) {
            return false;
        }

        int colCount = getColumnCount();
        if (colCount != kv2D.getColumnCount()) {
            return false;
        }

        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Number v1 = getValue(r, c);
                Number v2 = kv2D.getValue(r, c);
                if (v1 == null) {
                    if (v2 != null) {
                        return false;
                    }
                }
                else {
                    if (!v1.equals(v2)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = this.rowKeys.hashCode();
        result = 29 * result + this.columnKeys.hashCode();
        result = 29 * result + this.rows.hashCode();
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultKeyedValues2D clone = (DefaultKeyedValues2D) super.clone();
        // for the keys, a shallow copy should be fine because keys
        // should be immutable...
        clone.columnKeys = new ArrayList(this.columnKeys);
        clone.rowKeys = new ArrayList(this.rowKeys);

        // but the row data requires a deep copy
        clone.rows = CloneUtils.cloneList(this.rows);
        return clone;
    }

}
