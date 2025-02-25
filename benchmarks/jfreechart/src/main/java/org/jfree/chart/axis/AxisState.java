

package org.jfree.chart.axis;

import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.Args;


public class AxisState {

    
    private double cursor;

    
    private List<ValueTick> ticks;

    
    private double max;

    
    public AxisState() {
        this(0.0);
    }

    
    public AxisState(double cursor) {
        this.cursor = cursor;
        this.ticks = new ArrayList<>();
    }

    
    public double getCursor() {
        return this.cursor;
    }

    
    public void setCursor(double cursor) {
        this.cursor = cursor;
    }

    
    public void moveCursor(double units, RectangleEdge edge) {
        Args.nullNotPermitted(edge, "edge");
        switch (edge) {
            case TOP:
                cursorUp(units);
                break;
            case BOTTOM:
                cursorDown(units);
                break;
            case LEFT:
                cursorLeft(units);
                break;
            case RIGHT:
                cursorRight(units);
                break;
            default:
                throw new IllegalStateException("Unexpected enum value " + edge);
        }
    }

    
    public void cursorUp(double units) {
        this.cursor = this.cursor - units;
    }

    
    public void cursorDown(double units) {
        this.cursor = this.cursor + units;
    }

    
    public void cursorLeft(double units) {
        this.cursor = this.cursor - units;
    }

    
    public void cursorRight(double units) {
        this.cursor = this.cursor + units;
    }

    
    public List<ValueTick> getTicks() {
        return this.ticks;
    }

    
    public void setTicks(List<ValueTick> ticks) {
        this.ticks = ticks;
    }

    
    public double getMax() {
        return this.max;
    }

    
    public void setMax(double max) {
        this.max = max;
    }
}
