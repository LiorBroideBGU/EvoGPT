

package org.jfree.data.xy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;


public class DefaultXYDataset<S extends Comparable<S>> 
        extends AbstractXYDataset<S>
        implements XYDataset<S>, PublicCloneable {

    
    private List<S> seriesKeys;

    
    private List<double[][]> seriesList;

    
    public DefaultXYDataset() {
        this.seriesKeys = new ArrayList<>();
        this.seriesList = new ArrayList<>();
    }

    
    @Override
    public int getSeriesCount() {
        return this.seriesList.size();
    }

    
    @Override
    public S getSeriesKey(int series) {
        Args.requireInRange(series, "series", 0, this.seriesKeys.size() - 1);
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
        if ((series < 0) || (series >= getSeriesCount())) {
            throw new IllegalArgumentException("Series index out of bounds");
        }
        double[][] seriesArray = this.seriesList.get(series);
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
        double[][] seriesData = this.seriesList.get(series);
        return seriesData[1][item];
    }

    
    @Override
    public Number getY(int series, int item) {
        return getYValue(series, item);
    }

    
    public void addSeries(S seriesKey, double[][] data) {
        if (seriesKey == null) {
            throw new IllegalArgumentException(
                    "The 'seriesKey' cannot be null.");
        }
        if (data == null) {
            throw new IllegalArgumentException("The 'data' is null.");
        }
        if (data.length != 2) {
            throw new IllegalArgumentException(
                    "The 'data' array must have length == 2.");
        }
        if (data[0].length != data[1].length) {
            throw new IllegalArgumentException(
                "The 'data' array must contain two arrays with equal length.");
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
        if (!(obj instanceof DefaultXYDataset)) {
            return false;
        }
        DefaultXYDataset that = (DefaultXYDataset) obj;
        if (!this.seriesKeys.equals(that.seriesKeys)) {
            return false;
        }
        for (int i = 0; i < this.seriesList.size(); i++) {
            double[][] d1 = this.seriesList.get(i);
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
        DefaultXYDataset clone = (DefaultXYDataset) super.clone();
        clone.seriesKeys = new ArrayList(this.seriesKeys);
        clone.seriesList = new ArrayList(this.seriesList.size());
        for (int i = 0; i < this.seriesList.size(); i++) {
            double[][] data = this.seriesList.get(i);
            double[] x = data[0];
            double[] y = data[1];
            double[] xx = new double[x.length];
            double[] yy = new double[y.length];
            System.arraycopy(x, 0, xx, 0, x.length);
            System.arraycopy(y, 0, yy, 0, y.length);
            clone.seriesList.add(i, new double[][] {xx, yy});
        }
        return clone;
    }

}
