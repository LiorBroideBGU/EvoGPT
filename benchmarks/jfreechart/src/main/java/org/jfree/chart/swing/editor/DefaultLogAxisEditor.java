

package org.jfree.chart.swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberTickUnit;


public class DefaultLogAxisEditor extends DefaultValueAxisEditor {

    private double manualTickUnitValue;

    private JTextField manualTickUnit;

    
    public DefaultLogAxisEditor(LogAxis axis) {
        super(axis);
        this.manualTickUnitValue = axis.getTickUnit().getSize();
        manualTickUnit.setText(Double.toString(this.manualTickUnitValue));
    }
    
    
    @Override
    protected JPanel createTickUnitPanel() {
        JPanel tickUnitPanel = super.createTickUnitPanel();

        tickUnitPanel.add(new JLabel(localizationResources.getString(
                "Manual_TickUnit_value")));
        this.manualTickUnit = new JTextField(Double.toString(
                this.manualTickUnitValue));
        this.manualTickUnit.setEnabled(!isAutoTickUnitSelection());
        this.manualTickUnit.setActionCommand("TickUnitValue");
        this.manualTickUnit.addActionListener(this);
        this.manualTickUnit.addFocusListener(this);
        tickUnitPanel.add(this.manualTickUnit);
        tickUnitPanel.add(new JPanel());

        return tickUnitPanel;
    }

    
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();
        if (command.equals("TickUnitValue")) {
            validateTickUnit();
        }
        else {
            // pass to the super-class for handling
            super.actionPerformed(event);
        }
    }

    @Override
    public void focusLost(FocusEvent event) {
        super.focusLost(event);
        if (event.getSource() == this.manualTickUnit) {
            validateTickUnit();
        }
    }

    
    @Override
    public void toggleAutoTick() {
        super.toggleAutoTick();
        if (isAutoTickUnitSelection()) {
            this.manualTickUnit.setText(Double.toString(this.manualTickUnitValue));
            this.manualTickUnit.setEnabled(false);
        }
        else {
            this.manualTickUnit.setEnabled(true);
        }
    }

    
    public void validateTickUnit() {
        double newTickUnit;
        try {
            newTickUnit = Double.parseDouble(this.manualTickUnit.getText());
        }
        catch (NumberFormatException e) {
            newTickUnit = this.manualTickUnitValue;
        }

        if (newTickUnit > 0.0) {
            this.manualTickUnitValue = newTickUnit;
        }
        this.manualTickUnit.setText(Double.toString(this.manualTickUnitValue));
    }

    
    @Override
    public void setAxisProperties(Axis axis) {
        super.setAxisProperties(axis);
        LogAxis logAxis = (LogAxis) axis;
        if (!isAutoTickUnitSelection()) {
            logAxis.setTickUnit(new NumberTickUnit(manualTickUnitValue));
        }
    }
}
