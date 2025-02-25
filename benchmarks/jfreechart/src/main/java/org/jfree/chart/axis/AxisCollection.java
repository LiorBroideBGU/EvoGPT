

package org.jfree.chart.axis;

import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.Args;


public class AxisCollection {

    
    private final List<Axis> axesAtTop;

    
    private final List<Axis> axesAtBottom;

    
    private final List<Axis> axesAtLeft;

    
    private final List<Axis> axesAtRight;

    
    public AxisCollection() {
        this.axesAtTop = new ArrayList<>();
        this.axesAtBottom = new ArrayList<>();
        this.axesAtLeft = new ArrayList<>();
        this.axesAtRight = new ArrayList<>();
    }

    
    public List<Axis> getAxesAtTop() {
        return this.axesAtTop;
    }

   
   public List<Axis> getAxesAtBottom() {
        return this.axesAtBottom;
    }

    
    public List<Axis> getAxesAtLeft() {
        return this.axesAtLeft;
    }

    
    public List<Axis> getAxesAtRight() {
        return this.axesAtRight;
    }

    
    public void add(Axis axis, RectangleEdge edge) {
        Args.nullNotPermitted(axis, "axis");
        Args.nullNotPermitted(edge, "edge");
        switch (edge) {
            case TOP:
                this.axesAtTop.add(axis);
                break;
            case BOTTOM:
                this.axesAtBottom.add(axis);
                break;
            case LEFT:
                this.axesAtLeft.add(axis);
                break;
            case RIGHT:
                this.axesAtRight.add(axis);
                break;
            default:
                break;
        }
    }

}
