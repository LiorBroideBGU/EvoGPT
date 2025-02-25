

package org.jfree.chart.plot;

import java.awt.geom.Point2D;

import org.jfree.chart.renderer.category.CategoryItemRenderer;


public class CategoryCrosshairState<R extends Comparable<R>, C extends Comparable<C>> 
        extends CrosshairState {

    
    private R rowKey;

    
    private C columnKey;

    
    public CategoryCrosshairState() {
        this.rowKey = null;
        this.columnKey = null;
    }

    
    public R getRowKey() {
        return this.rowKey;
    }

    
    public void setRowKey(R key) {
        this.rowKey = key;
    }

    
    public C getColumnKey() {
        return this.columnKey;
    }

    
    public void setColumnKey(C key) {
        this.columnKey = key;
    }

    
    public void updateCrosshairPoint(R rowKey, C columnKey,
            double value, int datasetIndex, double transX, double transY,
            PlotOrientation orientation) {

        Point2D anchor = getAnchor();
        if (anchor != null) {
            double xx = anchor.getX();
            double yy = anchor.getY();
            if (orientation == PlotOrientation.HORIZONTAL) {
                double temp = yy;
                yy = xx;
                xx = temp;
            }
            double d = (transX - xx) * (transX - xx)
                    + (transY - yy) * (transY - yy);

            if (d < getCrosshairDistance()) {
                this.rowKey = rowKey;
                this.columnKey = columnKey;
                setCrosshairY(value);
                setDatasetIndex(datasetIndex);
                setCrosshairDistance(d);
            }
        }

    }

    
    public void updateCrosshairX(R rowKey, C columnKey,
            int datasetIndex, double transX, PlotOrientation orientation) {

        Point2D anchor = getAnchor();
        if (anchor != null) {
            double anchorX = anchor.getX();
            if (orientation == PlotOrientation.HORIZONTAL) {
                anchorX = anchor.getY();
            }
            double d = Math.abs(transX - anchorX);
            if (d < getCrosshairDistance()) {
                this.rowKey = rowKey;
                this.columnKey = columnKey;
                setDatasetIndex(datasetIndex);
                setCrosshairDistance(d);
            }
        }

    }

}
