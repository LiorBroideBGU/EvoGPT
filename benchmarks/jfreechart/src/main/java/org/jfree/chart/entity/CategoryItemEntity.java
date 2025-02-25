

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.internal.Args;

import org.jfree.data.category.CategoryDataset;


public class CategoryItemEntity<R extends Comparable<R>, C extends Comparable<C>> 
        extends ChartEntity implements Cloneable, Serializable {

    
    private static final long serialVersionUID = -8657249457902337349L;

    
    private CategoryDataset<R, C> dataset;

    
    private R rowKey;

    
    private C columnKey;

    
    public CategoryItemEntity(Shape area, String toolTipText, String urlText,
            CategoryDataset dataset, R rowKey, C columnKey) {
        super(area, toolTipText, urlText);
        Args.nullNotPermitted(dataset, "dataset");
        this.dataset = dataset;
        this.rowKey = rowKey;
        this.columnKey = columnKey;
    }

    
    public CategoryDataset<R, C> getDataset() {
        return this.dataset;
    }

    
    public void setDataset(CategoryDataset<R, C> dataset) {
        Args.nullNotPermitted(dataset, "dataset");
        this.dataset = dataset;
    }

    
    public R getRowKey() {
        return this.rowKey;
    }

    
    public void setRowKey(R rowKey) {
        this.rowKey = rowKey;
    }

    
    public C getColumnKey() {
        return this.columnKey;
    }

    
    public void setColumnKey(C columnKey) {
        this.columnKey = columnKey;
    }

    
    @Override
    public String toString() {
        return "CategoryItemEntity: rowKey=" + this.rowKey
               + ", columnKey=" + this.columnKey + ", dataset=" + this.dataset;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryItemEntity)) {
            return false;
        }
        CategoryItemEntity that = (CategoryItemEntity) obj;
        if (!this.rowKey.equals(that.rowKey)) {
            return false;
        }
        if (!this.columnKey.equals(that.columnKey)) {
            return false;
        }
        if (!Objects.equals(this.dataset, that.dataset)) {
            return false;
        }

        return super.equals(obj);
    }

}
