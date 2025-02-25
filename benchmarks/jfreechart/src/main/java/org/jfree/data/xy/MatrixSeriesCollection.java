

package org.jfree.data.xy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;


public class MatrixSeriesCollection<S extends Comparable<S>> 
        extends AbstractXYZDataset
        implements XYZDataset, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -3197705779242543945L;

    
    private List<MatrixSeries<S>> seriesList;

    
    public MatrixSeriesCollection() {
        this(null);
    }


    
    public MatrixSeriesCollection(MatrixSeries<S> series) {
        this.seriesList = new ArrayList<>();

        if (series != null) {
            this.seriesList.add(series);
            series.addChangeListener(this);
        }
    }

    
    @Override
    public int getItemCount(int seriesIndex) {
        return getSeries(seriesIndex).getItemCount();
    }


    
    public MatrixSeries<S> getSeries(int seriesIndex) {
        Args.requireInRange(seriesIndex, "seriesIndex", 0, this.seriesList.size() - 1);
        MatrixSeries<S> series = this.seriesList.get(seriesIndex);
        return series;
    }


    
    @Override
    public int getSeriesCount() {
        return this.seriesList.size();
    }


    
    @Override
    public S getSeriesKey(int seriesIndex) {
        return getSeries(seriesIndex).getKey();
    }


    
    @Override
    public Number getX(int seriesIndex, int itemIndex) {
        MatrixSeries series = this.seriesList.get(seriesIndex);
        return series.getItemColumn(itemIndex);
    }


    
    @Override
    public Number getY(int seriesIndex, int itemIndex) {
        MatrixSeries series = this.seriesList.get(seriesIndex);
        return series.getItemRow(itemIndex);
    }


    
    @Override
    public Number getZ(int seriesIndex, int itemIndex) {
        MatrixSeries series = this.seriesList.get(seriesIndex);
        return series.getItem(itemIndex);
    }

    
    public void addSeries(MatrixSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        // FIXME: Check that there isn't already a series with the same key

        // add the series...
        this.seriesList.add(series);
        series.addChangeListener(this);
        fireDatasetChanged();
    }


    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj instanceof MatrixSeriesCollection) {
            MatrixSeriesCollection<S> c = (MatrixSeriesCollection) obj;

            return Objects.equals(this.seriesList, c.seriesList);
        }

        return false;
    }

    
    @Override
    public int hashCode() {
        return (this.seriesList != null ? this.seriesList.hashCode() : 0);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        MatrixSeriesCollection<S> clone = (MatrixSeriesCollection) super.clone();
        clone.seriesList = CloneUtils.cloneList(this.seriesList);
        return clone;
    }

    
    public void removeAllSeries() {
        // Unregister the collection as a change listener to each series in
        // the collection.
        for (MatrixSeries series : this.seriesList) {
            series.removeChangeListener(this);
        }

        // Remove all the series from the collection and notify listeners.
        this.seriesList.clear();
        fireDatasetChanged();
    }


    
    public void removeSeries(MatrixSeries<S> series) {
        Args.nullNotPermitted(series, "series");
        if (this.seriesList.contains(series)) {
            series.removeChangeListener(this);
            this.seriesList.remove(series);
            fireDatasetChanged();
        }
    }


    
    public void removeSeries(int seriesIndex) {
        Args.requireInRange(seriesIndex, "seriesIndex", 0, this.seriesList.size() -1);

        // fetch the series, remove the change listener, then remove the series.
        MatrixSeries series = this.seriesList.get(seriesIndex);
        series.removeChangeListener(this);
        this.seriesList.remove(seriesIndex);
        fireDatasetChanged();
    }

}
