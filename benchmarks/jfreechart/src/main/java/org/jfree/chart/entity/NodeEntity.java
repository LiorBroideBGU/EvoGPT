

package org.jfree.chart.entity;

import java.awt.Shape;
import org.jfree.chart.internal.Args;
import org.jfree.chart.plot.flow.FlowPlot;
import org.jfree.data.flow.NodeKey;


public class NodeEntity extends ChartEntity {

    private NodeKey key;
    
    
    public NodeEntity(NodeKey key, Shape area, String toolTipText) {
        super(area, toolTipText);
        Args.nullNotPermitted(key, "key");
        this.key = key;
    }
    
    
    public NodeEntity(Shape area, String toolTipText, String urlText) {
        super(area, toolTipText, urlText);
    }

    
    public NodeKey getKey() {
        return this.key;
    }
    
    
    @Override
    public String toString() {
        return "[NodeEntity: " + this.key + "]";
    }

}
