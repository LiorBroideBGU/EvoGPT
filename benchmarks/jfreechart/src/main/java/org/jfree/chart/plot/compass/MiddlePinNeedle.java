

package org.jfree.chart.plot.compass;

import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;


public class MiddlePinNeedle extends MeterNeedle implements Cloneable, 
        Serializable {

    
    private static final long serialVersionUID = 6237073996403125310L;

    
    @Override
    protected void drawNeedle(Graphics2D g2, Rectangle2D plotArea,
            Point2D rotate, double angle) {

        Area shape;
        GeneralPath pointer = new GeneralPath();

        int minY = (int) (plotArea.getMinY());
        //int maxX = (int) (plotArea.getMaxX());
        int maxY = (int) (plotArea.getMaxY());
        int midY = ((maxY - minY) / 2) + minY;

        int midX = (int) (plotArea.getMinX() + (plotArea.getWidth() / 2));
        //int midY = (int) (plotArea.getMinY() + (plotArea.getHeight() / 2));
        int lenX = (int) (plotArea.getWidth() / 10);
        if (lenX < 2) {
            lenX = 2;
        }

        pointer.moveTo(midX - lenX, midY - lenX);
        pointer.lineTo(midX + lenX, midY - lenX);
        pointer.lineTo(midX, minY);
        pointer.closePath();

        lenX = 4 * lenX;
        Ellipse2D circle = new Ellipse2D.Double(midX - lenX / 2.0,
                                                midY - lenX, lenX, lenX);

        shape = new Area(circle);
        shape.add(new Area(pointer));
        if ((rotate != null) && (angle != 0)) {
            /// we have rotation
            getTransform().setToRotation(angle, rotate.getX(), rotate.getY());
            shape.transform(getTransform());
        }

        defaultDisplay(g2, shape);

    }

    
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (super.equals(object) && object instanceof MiddlePinNeedle) {
            return true;
        }
        return false;
    }

    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
