

package org.jfree.chart.swing.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.geom.Rectangle2D;
import javax.swing.JComponent;


public class PaintSample extends JComponent {

    
    private Paint paint;

    
    private Dimension preferredSize;

    
    public PaintSample(Paint paint) {
        this.paint = paint;
        this.preferredSize = new Dimension(80, 12);
    }

    
    public Paint getPaint() {
        return this.paint;
    }

    
    public void setPaint(Paint paint) {
        this.paint = paint;
        repaint();
    }

    
    @Override
    public Dimension getPreferredSize() {
        return this.preferredSize;
    }

    
    @Override
    public void paintComponent(Graphics g) {

        final Graphics2D g2 = (Graphics2D) g;
        final Dimension size = getSize();
        final Insets insets = getInsets();
        final double xx = insets.left;
        final double yy = insets.top;
        final double ww = size.getWidth() - insets.left - insets.right - 1;
        final double hh = size.getHeight() - insets.top - insets.bottom - 1;
        final Rectangle2D area = new Rectangle2D.Double(xx, yy, ww, hh);
        g2.setPaint(this.paint);
        g2.fill(area);
        g2.setPaint(Color.BLACK);
        g2.draw(area);

    }

}

