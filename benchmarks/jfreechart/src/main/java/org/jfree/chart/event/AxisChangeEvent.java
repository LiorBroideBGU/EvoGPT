

package org.jfree.chart.event;

import org.jfree.chart.axis.Axis;


public class AxisChangeEvent extends ChartChangeEvent {

    
    private final Axis axis;

    
    public AxisChangeEvent(Axis axis) {
        super(axis); // null is checked in this call
        this.axis = axis;
    }

    
    public Axis getAxis() {
        return this.axis;
    }

}
