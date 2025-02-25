

package org.jfree.chart.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JFrame;


public class ApplicationFrame extends JFrame implements WindowListener {

    
    public ApplicationFrame(String title) {
        super(title);
        addWindowListener(this);
    }

    
    @Override
    public void windowClosing(WindowEvent event) {
        if (event.getWindow() == this) {
            dispose();
            System.exit(0);
        }
    }

    
    @Override
    public void windowClosed(WindowEvent event) {
        // ignore
    }

    
    @Override
    public void windowActivated(WindowEvent event) {
        // ignore
    }

    
    @Override
    public void windowDeactivated(WindowEvent event) {
        // ignore
    }

    
    @Override
    public void windowDeiconified(WindowEvent event) {
        // ignore
    }

    
    @Override
    public void windowIconified(WindowEvent event) {
        // ignore
    }

    
    @Override
    public void windowOpened(WindowEvent event) {
        // ignore
    }

}
