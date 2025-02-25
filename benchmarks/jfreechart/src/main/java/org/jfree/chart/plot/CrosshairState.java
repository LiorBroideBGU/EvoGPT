

package org.jfree.chart.plot;

import java.awt.geom.Point2D;


public class CrosshairState {

    
    private boolean calculateDistanceInDataSpace = false;

    
    private double anchorX;

    
    private double anchorY;

    
    private Point2D anchor;

    
    private double crosshairX;

    
    private double crosshairY;

    
    private int datasetIndex;

    
    private double distance;

    
    public CrosshairState() {
        this(false);
    }

    
    public CrosshairState(boolean calculateDistanceInDataSpace) {
        this.calculateDistanceInDataSpace = calculateDistanceInDataSpace;
    }

    
    public double getCrosshairDistance() {
        return this.distance;
    }

    
    public void setCrosshairDistance(double distance) {
        this.distance = distance;
    }
    
    
    public void updateCrosshairPoint(double x, double y, int datasetIndex,
            double transX, double transY, PlotOrientation orientation) {

        if (this.anchor != null) {
            double d = 0.0;
            if (this.calculateDistanceInDataSpace) { 
                d = (x - this.anchorX) * (x - this.anchorX)
                  + (y - this.anchorY) * (y - this.anchorY);
            }
            else {
                // anchor point is in Java2D coordinates
                double xx = this.anchor.getX();
                double yy = this.anchor.getY();
                if (orientation == PlotOrientation.HORIZONTAL) {
                    double temp = yy;
                    yy = xx;
                    xx = temp;
                }
                d = (transX - xx) * (transX - xx)
                    + (transY - yy) * (transY - yy);
            }

            if (d < this.distance) {
                this.crosshairX = x;
                this.crosshairY = y;
                this.datasetIndex = datasetIndex;
                this.distance = d;
            }
        }

    }
    
    
    public void updateCrosshairX(double x, double transX, int datasetIndex) {
        if (this.anchor == null) {
            return;
        }
        double d = Math.abs(transX - this.anchor.getX());
        if (d < this.distance) {
            this.crosshairX = x;
            this.datasetIndex = datasetIndex;
            this.distance = d;
        }        
    }

    
    public void updateCrosshairY(double candidateY, double transY, int datasetIndex) {
        if (this.anchor == null) {
            return;
        }
        double d = Math.abs(transY - this.anchor.getY());
        if (d < this.distance) {
            this.crosshairY = candidateY;
            this.datasetIndex = datasetIndex;
            this.distance = d;
        }

    }

    
    public Point2D getAnchor() {
        return this.anchor;
    }

    
    public void setAnchor(Point2D anchor) {
        this.anchor = anchor;
    }

    
    public double getAnchorX() {
        return this.anchorX;
    }

    
    public void setAnchorX(double x) {
        this.anchorX = x;
    }

    
    public double getAnchorY() {
        return this.anchorY;
    }

    
    public void setAnchorY(double y) {
        this.anchorY = y;
    }

    
    public double getCrosshairX() {
        return this.crosshairX;
    }

    
    public void setCrosshairX(double x) {
        this.crosshairX = x;
    }

    
    public double getCrosshairY() {
        return this.crosshairY;
    }

    
    public void setCrosshairY(double y) {
        this.crosshairY = y;
    }

    
    public int getDatasetIndex() {
        return this.datasetIndex;
    }

    
    public void setDatasetIndex(int index) {
        this.datasetIndex = index;
    }
}
