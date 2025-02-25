

package org.jfree.chart.event;


public class RendererChangeEvent extends ChartChangeEvent {

    
    private final Object renderer;

    
    private final boolean seriesVisibilityChanged;

    
    public RendererChangeEvent(Object renderer) {
        this(renderer, false);
    }

    
    public RendererChangeEvent(Object renderer, boolean seriesVisibilityChanged) {
        super(renderer);
        this.renderer = renderer;
        this.seriesVisibilityChanged = seriesVisibilityChanged;
    }

    
    public Object getRenderer() {
        return this.renderer;
    }

    
    public boolean getSeriesVisibilityChanged() {
        return this.seriesVisibilityChanged;
    }

}
