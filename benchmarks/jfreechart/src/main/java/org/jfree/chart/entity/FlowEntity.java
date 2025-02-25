

package org.jfree.chart.entity;

import java.awt.Shape;
import java.util.Objects;
import org.jfree.chart.internal.Args;
import org.jfree.chart.plot.flow.FlowPlot;
import org.jfree.data.flow.FlowKey;


public class FlowEntity extends ChartEntity {

    private FlowKey key;
    
    
    public FlowEntity(FlowKey key, Shape area, String toolTipText, String urlText) {
        super(area, toolTipText, urlText);
        Args.nullNotPermitted(key, "key");
        this.key = key;
    }
    
    
    public FlowKey getKey() {
        return this.key;
    }

    
    @Override
    public String toString() {
        return "[FlowEntity: " + this.key + "]";
    }

    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FlowEntity)) {
            return false;
        }
        FlowEntity that = (FlowEntity) obj;
        if (!this.key.equals(that.key)) {
            return false;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

}
