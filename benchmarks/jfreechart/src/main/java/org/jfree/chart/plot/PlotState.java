

package org.jfree.chart.plot;

import java.util.HashMap;
import java.util.Map;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisState;


public class PlotState {

    
    private final Map<Axis, AxisState> sharedAxisStates;

    
    public PlotState() {
        this.sharedAxisStates = new HashMap<>();
    }

    
    public Map<Axis, AxisState> getSharedAxisStates() {
        return this.sharedAxisStates;
    }

}
