

package org.jfree.chart.swing;

import javax.swing.event.EventListenerList;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.swing.ChartPanel;
import org.jfree.chart.internal.Args;


public class AbstractOverlay {

    
    private final transient EventListenerList changeListeners;

    
    public AbstractOverlay() {
        this.changeListeners = new EventListenerList();
    }

    
    public void addChangeListener(OverlayChangeListener listener) {
        Args.nullNotPermitted(listener, "listener");
        this.changeListeners.add(OverlayChangeListener.class, listener);
    }

    
    public void removeChangeListener(OverlayChangeListener listener) {
        Args.nullNotPermitted(listener, "listener");
        this.changeListeners.remove(OverlayChangeListener.class, listener);
    }

    
    public void fireOverlayChanged() {
        OverlayChangeEvent event = new OverlayChangeEvent(this);
        notifyListeners(event);
    }

    
    protected void notifyListeners(OverlayChangeEvent event) {
       Object[] listeners = this.changeListeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == OverlayChangeListener.class) {
                ((OverlayChangeListener) listeners[i + 1]).overlayChanged(
                        event);
            }
        }
    }

}

