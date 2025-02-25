

package org.jfree.chart.entity;

import java.awt.Shape;
import java.util.Objects;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.axis.CategoryAxis;


public class CategoryLabelEntity<C extends Comparable<C>> extends TickLabelEntity {

    
    private final C key;

    
    public CategoryLabelEntity(C key, Shape area, String toolTipText, 
            String urlText) {
        super(area, toolTipText, urlText);
        this.key = key;
    }

    
    public C getKey() {
        return this.key;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryLabelEntity)) {
            return false;
        }
        CategoryLabelEntity<C> that = (CategoryLabelEntity) obj;
        if (!Objects.equals(this.key, that.key)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = HashUtils.hashCode(result, this.key);
        return result;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("CategoryLabelEntity: ");
        sb.append("category=");
        sb.append(this.key);
        sb.append(", tooltip=").append(getToolTipText());
        sb.append(", url=").append(getURLText());
        return sb.toString();
    }
}
