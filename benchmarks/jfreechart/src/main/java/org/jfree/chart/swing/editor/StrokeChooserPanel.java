

package org.jfree.chart.swing.editor;

import java.awt.BorderLayout;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;


public class StrokeChooserPanel extends JPanel {

    
    private JComboBox selector;

    
    public StrokeChooserPanel(StrokeSample current, StrokeSample[] available) {
        setLayout(new BorderLayout());
        // we've changed the behaviour here to populate the combo box
        // with Stroke objects directly - ideally we'd change the signature
        // of the constructor too...maybe later.
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        for (int i = 0; i < available.length; i++) {
            model.addElement(available[i].getStroke());
        }
        this.selector = new JComboBox(model);
        this.selector.setSelectedItem(current.getStroke());
        this.selector.setRenderer(new StrokeSample(null));
        add(this.selector);
        // Changes due to focus problems!! DZ
        this.selector.addActionListener((ActionEvent evt) -> {
            getSelector().transferFocus();
        });
    }


    
    protected final JComboBox getSelector() {
        return this.selector;
    }

    
    public Stroke getSelectedStroke() {
        return (Stroke) this.selector.getSelectedItem();
    }

}

