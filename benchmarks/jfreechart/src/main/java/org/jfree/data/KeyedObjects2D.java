

package org.jfree.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.jfree.chart.internal.Args;


public class KeyedObjects2D<R extends Comparable<R>, C extends Comparable<C>> 
        implements Cloneable, Serializable {

    
    private static final long serialVersionUID = -1015873563138522374L;

    
    private List<R> rowKeys;

    
    private List<C> columnKeys;

    
    private List<KeyedObjects> rows;

    
    public KeyedObjects2D() {
        this.rowKeys = new ArrayList<>();
        this.columnKeys = new ArrayList<>();
        this.rows = new ArrayList();
    }

    
    public int getRowCount() {
        return this.rowKeys.size();
    }

    
    public int getColumnCount() {
        return this.columnKeys.size();
    }

    
    public Object getObject(int row, int column) {
        Object result = null;
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
        if (rowData != null) {
            Comparable columnKey = (Comparable) this.columnKeys.get(column);
            if (columnKey != null) {
                int index = rowData.getIndex(columnKey);
                if (index >= 0) {
                    result = rowData.getObject(columnKey);
                }
            }
        }
        return result;
    }

    
    public R getRowKey(int row) {
        return this.rowKeys.get(row);
    }

    
    public int getRowIndex(R key) {
        Args.nullNotPermitted(key, "key");
        return this.rowKeys.indexOf(key);
    }

    
    public List<R> getRowKeys() {
        return Collections.unmodifiableList(this.rowKeys);
    }

    
    public C getColumnKey(int column) {
        return this.columnKeys.get(column);
    }

    
    public int getColumnIndex(C key) {
        Args.nullNotPermitted(key, "key");
        return this.columnKeys.indexOf(key);
    }

    
    public List<C> getColumnKeys() {
        return Collections.unmodifiableList(this.columnKeys);
    }

    
    public Object getObject(R rowKey, C columnKey) {
        Args.nullNotPermitted(rowKey, "rowKey");
        Args.nullNotPermitted(columnKey, "columnKey");
        int row = this.rowKeys.indexOf(rowKey);
        if (row < 0) {
            throw new UnknownKeyException("Row key (" + rowKey
                    + ") not recognised.");
        }
        int column = this.columnKeys.indexOf(columnKey);
        if (column < 0) {
            throw new UnknownKeyException("Column key (" + columnKey
                    + ") not recognised.");
        }
        KeyedObjects rowData = (KeyedObjects) this.rows.get(row);
        int index = rowData.getIndex(columnKey);
        if (index >= 0) {
            return rowData.getObject(index);
        }
        else {
            return null;
        }
    }

    
    public void addObject(Object object, R rowKey, C columnKey) {
        setObject(object, rowKey, columnKey);
    }

    
    public void setObject(Object object, R rowKey, C columnKey) {
        Args.nullNotPermitted(rowKey, "rowKey");
        Args.nullNotPermitted(columnKey, "columnKey");
        KeyedObjects row;
        int rowIndex = this.rowKeys.indexOf(rowKey);
        if (rowIndex >= 0) {
            row = (KeyedObjects) this.rows.get(rowIndex);
        }
        else {
            this.rowKeys.add(rowKey);
            row = new KeyedObjects();
            this.rows.add(row);
        }
        row.setObject(columnKey, object);
        int columnIndex = this.columnKeys.indexOf(columnKey);
        if (columnIndex < 0) {
            this.columnKeys.add(columnKey);
        }
    }

    
    public void removeObject(R rowKey, C columnKey) {
        int rowIndex = getRowIndex(rowKey);
        if (rowIndex < 0) {
            throw new UnknownKeyException("Row key (" + rowKey
                    + ") not recognised.");
        }
        int columnIndex = getColumnIndex(columnKey);
        if (columnIndex < 0) {
            throw new UnknownKeyException("Column key (" + columnKey
                    + ") not recognised.");
        }
        setObject(null, rowKey, columnKey);

        // 1. check whether the row is now empty.
        boolean allNull = true;
        KeyedObjects row = (KeyedObjects) this.rows.get(rowIndex);

        for (int item = 0, itemCount = row.getItemCount(); item < itemCount;
             item++) {
            if (row.getObject(item) != null) {
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

        for (int item = 0, itemCount = this.rows.size(); item < itemCount;
             item++) {
            row = (KeyedObjects) this.rows.get(item);
            int colIndex = row.getIndex(columnKey);
            if (colIndex >= 0 && row.getObject(colIndex) != null) {
                allNull = false;
                break;
            }
        }

        if (allNull) {
            for (int item = 0, itemCount = this.rows.size(); item < itemCount;
                 item++) {
                row = (KeyedObjects) this.rows.get(item);
                int colIndex = row.getIndex(columnKey);
                if (colIndex >= 0) {
                    row.removeValue(colIndex);
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
        int index = getRowIndex(rowKey);
        if (index < 0) {
            throw new UnknownKeyException("Row key (" + rowKey
                    + ") not recognised.");
        }
        removeRow(index);
    }

    
    public void removeColumn(int columnIndex) {
        C columnKey = getColumnKey(columnIndex);
        removeColumn(columnKey);
    }

    
    public void removeColumn(C columnKey) {
        int index = getColumnIndex(columnKey);
        if (index < 0) {
            throw new UnknownKeyException("Column key (" + columnKey
                    + ") not recognised.");
        }
        for (KeyedObjects rowData : this.rows) {
            int i = rowData.getIndex(columnKey);
            if (i >= 0) {
                rowData.removeValue(i);
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
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof KeyedObjects2D)) {
            return false;
        }

        KeyedObjects2D that = (KeyedObjects2D) obj;
        if (!getRowKeys().equals(that.getRowKeys())) {
            return false;
        }
        if (!getColumnKeys().equals(that.getColumnKeys())) {
            return false;
        }
        int rowCount = getRowCount();
        if (rowCount != that.getRowCount()) {
            return false;
        }
        int colCount = getColumnCount();
        if (colCount != that.getColumnCount()) {
            return false;
        }
        for (int r = 0; r < rowCount; r++) {
            for (int c = 0; c < colCount; c++) {
                Object v1 = getObject(r, c);
                Object v2 = that.getObject(r, c);
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
        KeyedObjects2D clone = (KeyedObjects2D) super.clone();
        clone.columnKeys = new ArrayList(this.columnKeys);
        clone.rowKeys = new ArrayList(this.rowKeys);
        clone.rows = new ArrayList(this.rows.size());
        for (KeyedObjects row : this.rows) {
            clone.rows.add(row.clone());
        }
        return clone;
    }

}
