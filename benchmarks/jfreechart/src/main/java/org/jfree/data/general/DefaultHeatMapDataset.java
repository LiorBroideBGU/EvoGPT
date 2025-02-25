

package org.jfree.data.general;

import java.io.Serializable;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.DataUtils;


public class DefaultHeatMapDataset extends AbstractDataset
        implements HeatMapDataset, Cloneable, PublicCloneable, Serializable {

    
    private final int xSamples;

    
    private final int ySamples;

    
    private final double minX;

    
    private final double maxX;

    
    private final double minY;

    
    private final double maxY;

    
    private double[][] zValues;

    
    public DefaultHeatMapDataset(int xSamples, int ySamples, double minX,
            double maxX, double minY, double maxY) {

        if (xSamples < 1) {
            throw new IllegalArgumentException("Requires 'xSamples' > 0");
        }
        if (ySamples < 1) {
            throw new IllegalArgumentException("Requires 'ySamples' > 0");
        }
        if (Double.isInfinite(minX) || Double.isNaN(minX)) {
            throw new IllegalArgumentException("'minX' cannot be INF or NaN.");
        }
        if (Double.isInfinite(maxX) || Double.isNaN(maxX)) {
            throw new IllegalArgumentException("'maxX' cannot be INF or NaN.");
        }
        if (Double.isInfinite(minY) || Double.isNaN(minY)) {
            throw new IllegalArgumentException("'minY' cannot be INF or NaN.");
        }
        if (Double.isInfinite(maxY) || Double.isNaN(maxY)) {
            throw new IllegalArgumentException("'maxY' cannot be INF or NaN.");
        }

        this.xSamples = xSamples;
        this.ySamples = ySamples;
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.zValues = new double[xSamples][];
        for (int x = 0; x < xSamples; x++) {
            this.zValues[x] = new double[ySamples];
        }
    }

    
    @Override
    public int getXSampleCount() {
        return this.xSamples;
    }

    
    @Override
    public int getYSampleCount() {
        return this.ySamples;
    }

    
    @Override
    public double getMinimumXValue() {
        return this.minX;
    }

    
    @Override
    public double getMaximumXValue() {
        return this.maxX;
    }

    
    @Override
    public double getMinimumYValue() {
        return this.minY;
    }

    
    @Override
    public double getMaximumYValue() {
        return this.maxY;
    }

    
    @Override
    public double getXValue(int xIndex) {
        double x = this.minX
                + (this.maxX - this.minX) * (xIndex / (double) this.xSamples);
        return x;
    }

    
    @Override
    public double getYValue(int yIndex) {
        double y = this.minY
                + (this.maxY - this.minY) * (yIndex / (double) this.ySamples);
        return y;
    }

    
    @Override
    public double getZValue(int xIndex, int yIndex) {
        return this.zValues[xIndex][yIndex];
    }

    
    @Override
    public Number getZ(int xIndex, int yIndex) {
        return getZValue(xIndex, yIndex);
    }

    
    public void setZValue(int xIndex, int yIndex, double z) {
        setZValue(xIndex, yIndex, z, true);
    }

    
    public void setZValue(int xIndex, int yIndex, double z, boolean notify) {
        this.zValues[xIndex][yIndex] = z;
        if (notify) {
            fireDatasetChanged();
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultHeatMapDataset)) {
            return false;
        }
        DefaultHeatMapDataset that = (DefaultHeatMapDataset) obj;
        if (this.xSamples != that.xSamples) {
            return false;
        }
        if (this.ySamples != that.ySamples) {
            return false;
        }
        if (this.minX != that.minX) {
            return false;
        }
        if (this.maxX != that.maxX) {
            return false;
        }
        if (this.minY != that.minY) {
            return false;
        }
        if (this.maxY != that.maxY) {
            return false;
        }
        if (!DataUtils.equal(this.zValues, that.zValues)) {
            return false;
        }
        // can't find any differences
        return true;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultHeatMapDataset clone = (DefaultHeatMapDataset) super.clone();
        clone.zValues = DataUtils.clone(this.zValues);
        return clone;
    }

}
