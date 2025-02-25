

package org.jfree.chart.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;


public class UIUtils {

    private UIUtils() {
    }

    
    public static void centerFrameOnScreen(Window frame) {
        positionFrameOnScreen(frame, 0.5, 0.5);
    }

    
    public static void positionFrameOnScreen(Window frame, 
            double horizontalPercent, double verticalPercent) {

        Rectangle s = frame.getGraphicsConfiguration().getBounds();
        Dimension f = frame.getSize();
        int w = Math.max(s.width - f.width, 0);
        int h = Math.max(s.height - f.height, 0);
        int x = (int) (horizontalPercent * w) + s.x;
        int y = (int) (verticalPercent * h) + s.y;
        frame.setBounds(x, y, f.width, f.height);

    }

    
    public static void positionFrameRandomly(Window frame) {
        positionFrameOnScreen(frame, Math.random(), Math.random());
    }

    
    public static void centerDialogInParent(Dialog dialog) {
        positionDialogRelativeToParent(dialog, 0.5, 0.5);
    }

    
    public static void positionDialogRelativeToParent(Dialog dialog,
            double horizontalPercent, double verticalPercent) {
        Container parent = dialog.getParent();
        if (parent == null) {
            centerFrameOnScreen(dialog);
            return;
        }

        Dimension d = dialog.getSize();
        Dimension p = parent.getSize();

        int baseX = parent.getX();
        int baseY = parent.getY();

        int x = baseX + (int) (horizontalPercent * p.width);
        int y = baseY + (int) (verticalPercent * p.height);

        // make sure the dialog fits completely on the screen...
        Rectangle s = parent.getGraphicsConfiguration().getBounds();
        Rectangle r = new Rectangle(x, y, d.width, d.height);
        dialog.setBounds(r.intersection(s));
    }

    
    public static JPanel createTablePanel(TableModel model) {

        JPanel panel = new JPanel(new BorderLayout());
        JTable table = new JTable(model);
        for (int columnIndex = 0; columnIndex < model.getColumnCount(); columnIndex++) {
            TableColumn column = table.getColumnModel().getColumn(columnIndex);
            Class c = model.getColumnClass(columnIndex);
            if (c.equals(Number.class)) {
                column.setCellRenderer(new NumberCellRenderer());
            }
        }
        panel.add(new JScrollPane(table));
        return panel;

    }

    
    public static JLabel createJLabel(String text, Font font) {
        JLabel result = new JLabel(text);
        result.setFont(font);
        return result;
    }

    
    public static JLabel createJLabel(String text, Font font, Color color) {
        JLabel result = new JLabel(text);
        result.setFont(font);
        result.setForeground(color);
        return result;
    }

    
    public static JButton createJButton(String label, Font font) {
        JButton result = new JButton(label);
        result.setFont(font);
        return result;
    }

}



