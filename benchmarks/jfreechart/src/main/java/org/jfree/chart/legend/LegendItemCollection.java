

package org.jfree.chart.legend;

import org.jfree.chart.legend.LegendItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.CloneUtils;


public class LegendItemCollection implements Cloneable, Serializable {

    
    private static final long serialVersionUID = 1365215565589815953L;

    
    private List<LegendItem> items;

    
    public LegendItemCollection() {
        this.items = new ArrayList<>();
    }

    
    public void add(LegendItem item) {
        this.items.add(item);
    }

    
    public void addAll(LegendItemCollection collection) {
        this.items.addAll(collection.items);
    }

    
    public LegendItem get(int index) {
        return this.items.get(index);
    }

    
    public int getItemCount() {
        return this.items.size();
    }

    
    public Iterator<LegendItem> iterator() {
        return this.items.iterator();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendItemCollection)) {
            return false;
        }
        LegendItemCollection that = (LegendItemCollection) obj;
        if (!this.items.equals(that.items)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 17 * hash + Objects.hashCode( this.items );
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        LegendItemCollection clone = (LegendItemCollection) super.clone();
        clone.items = CloneUtils.cloneList(this.items);
        return clone;
    }

}
