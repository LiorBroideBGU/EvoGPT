

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.data.general.Dataset;


public class LegendItemEntity<S extends Comparable<S>> extends ChartEntity 
        implements Cloneable, Serializable {

    
    private static final long serialVersionUID = -7435683933545666702L;

    
    private Dataset dataset;

    
    private S seriesKey;

    
    private int seriesIndex;

    
    public LegendItemEntity(Shape area) {
        super(area);
    }

    
    public Dataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    
    public S getSeriesKey() {
        return this.seriesKey;
    }

    
    public void setSeriesKey(S key) {
        this.seriesKey = key;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendItemEntity)) {
            return false;
        }
        LegendItemEntity<?> that = (LegendItemEntity) obj;
        if (!Objects.equals(this.seriesKey, that.seriesKey)) {
            return false;
        }
        if (this.seriesIndex != that.seriesIndex) {
            return false;
        }
        if (!Objects.equals(this.dataset, that.dataset)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    @Override
    public String toString() {
        return "LegendItemEntity: seriesKey=" + this.seriesKey
                + ", dataset=" + this.dataset;
    }

}
