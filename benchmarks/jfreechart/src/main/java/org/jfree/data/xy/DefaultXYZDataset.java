

package org.jfree.data.xy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;


public class DefaultXYZDataset<S extends Comparable<S>> extends AbstractXYZDataset<S>
        implements XYZDataset<S>, PublicCloneable {

    
    private List<S> seriesKeys;

    
    private List<double[][]> seriesList;

    
    public DefaultXYZDataset() {
        this.seriesKeys = new ArrayList<>();
        this.seriesList = new ArrayList<>();
    }

    
    @Override
    public int getSeriesCount() {
        return this.seriesList.size();
    }

    
    @Override
    public S getSeriesKey(int series) {
        Args.requireInRange(series, "series", 0, this.seriesList.size() - 1);
        return this.seriesKeys.get(series);
    }

    
    @Override
    public int indexOf(S seriesKey) {
        return this.seriesKeys.indexOf(seriesKey);
    }

    
    @Override
    public DomainOrder getDomainOrder() {
        return DomainOrder.NONE;
    }

    
    @Override
    public int getItemCount(int series) {
        Args.requireInRange(series, "series", 0, this.seriesList.size() - 1);
        double[][] seriesArray = (double[][]) this.seriesList.get(series);
        return seriesArray[0].length;
    }

    
    @Override
    public double getXValue(int series, int item) {
        double[][] seriesData = this.seriesList.get(series);
        return seriesData[0][item];
    }

    
    @Override
    public Number getX(int series, int item) {
        return getXValue(series, item);
    }

    
    @Override
    public double getYValue(int series, int item) {
        double[][] seriesData = (double[][]) this.seriesList.get(series);
        return seriesData[1][item];
    }

    
    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    
    @Override
    public double getZValue(int series, int item) {
        double[][] seriesData = this.seriesList.get(series);
        return seriesData[2][item];
    }

    
    @Override
    public Number getZ(int series, int item) {
        return getZValue(series, item);
    }

    
    public void addSeries(S seriesKey, double[][] data) {
        Args.nullNotPermitted(seriesKey, "seriesKey");
        Args.nullNotPermitted(data, "data");
        if (data.length != 3) {
            throw new IllegalArgumentException(
                    "The 'data' array must have length == 3.");
        }
        if (data[0].length != data[1].length
                || data[0].length != data[2].length) {
            throw new IllegalArgumentException("The 'data' array must contain "
                    + "three arrays all having the same length.");
        }
        int seriesIndex = indexOf(seriesKey);
        if (seriesIndex == -1) {  // add a new series
            this.seriesKeys.add(seriesKey);
            this.seriesList.add(data);
        }
        else {  // replace an existing series
            this.seriesList.remove(seriesIndex);
            this.seriesList.add(seriesIndex, data);
        }
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    public void removeSeries(S seriesKey) {
        int seriesIndex = indexOf(seriesKey);
        if (seriesIndex >= 0) {
            this.seriesKeys.remove(seriesIndex);
            this.seriesList.remove(seriesIndex);
            notifyListeners(new DatasetChangeEvent(this, this));
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultXYZDataset)) {
            return false;
        }
        DefaultXYZDataset that = (DefaultXYZDataset) obj;
        if (!this.seriesKeys.equals(that.seriesKeys)) {
            return false;
        }
        for (int i = 0; i < this.seriesList.size(); i++) {
            double[][] d1 = (double[][]) this.seriesList.get(i);
            double[][] d2 = (double[][]) that.seriesList.get(i);
            double[] d1x = d1[0];
            double[] d2x = d2[0];
            if (!Arrays.equals(d1x, d2x)) {
                return false;
            }
            double[] d1y = d1[1];
            double[] d2y = d2[1];
            if (!Arrays.equals(d1y, d2y)) {
                return false;
            }
            double[] d1z = d1[2];
            double[] d2z = d2[2];
            if (!Arrays.equals(d1z, d2z)) {
                return false;
            }
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result;
        result = this.seriesKeys.hashCode();
        result = 29 * result + this.seriesList.hashCode();
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultXYZDataset clone = (DefaultXYZDataset) super.clone();
        clone.seriesKeys = new ArrayList(this.seriesKeys);
        clone.seriesList = new ArrayList(this.seriesList.size());
        for (int i = 0; i < this.seriesList.size(); i++) {
            double[][] data = (double[][]) this.seriesList.get(i);
            double[] x = data[0];
            double[] y = data[1];
            double[] z = data[2];
            double[] xx = new double[x.length];
            double[] yy = new double[y.length];
            double[] zz = new double[z.length];
            System.arraycopy(x, 0, xx, 0, x.length);
            System.arraycopy(y, 0, yy, 0, y.length);
            System.arraycopy(z, 0, zz, 0, z.length);
            clone.seriesList.add(i, new double[][] {xx, yy, zz});
        }
        return clone;
    }

}
