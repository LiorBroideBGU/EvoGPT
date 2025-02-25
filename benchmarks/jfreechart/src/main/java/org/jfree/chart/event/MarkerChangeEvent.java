

package org.jfree.chart.event;

import org.jfree.chart.plot.Marker;


public class MarkerChangeEvent extends ChartChangeEvent {

    
    private final Marker marker;

    
    public MarkerChangeEvent(Marker marker) {
        super(marker); // null check is in here
        this.marker = marker;
    }

    
    public Marker getMarker() {
        return this.marker;
    }

}
