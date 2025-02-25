

package org.jfree.chart.event;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.internal.Args;


public class ChartProgressEvent extends java.util.EventObject {

    
    private ChartProgressEventType type;

    
    private int percent;

    
    private JFreeChart chart;

    
    public ChartProgressEvent(Object source, JFreeChart chart, 
            ChartProgressEventType type, int percent) {
        super(source);
        Args.nullNotPermitted(type, "type");
        this.chart = chart;
        this.type = type;
        this.percent = percent;
    }

    
    public JFreeChart getChart() {
        return this.chart;
    }

    
    public void setChart(JFreeChart chart) {
        this.chart = chart;
    }

    
    public ChartProgressEventType getType() {
        return this.type;
    }

    
    public void setType(ChartProgressEventType type) {
        Args.nullNotPermitted(type, "type");
        this.type = type;
    }

    
    public int getPercent() {
        return this.percent;
    }

    
    public void setPercent(int percent) {
        this.percent = percent;
    }

}
