

package org.jfree.chart.labels;

import java.io.Serializable;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XisSymbolic;
import org.jfree.data.xy.YisSymbolic;


public class SymbolicXYItemLabelGenerator implements XYItemLabelGenerator,
        XYToolTipGenerator, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 3963400354475494395L;

    
    @Override
    public String generateToolTip(XYDataset dataset, int series, int item) {
        Args.nullNotPermitted(dataset, "dataset");
        String xStr, yStr;
        if (dataset instanceof YisSymbolic) {
            yStr = ((YisSymbolic) dataset).getYSymbolicValue(series, item);
        }
        else {
            double y = dataset.getYValue(series, item);
            yStr = Double.toString(round(y, 2));
        }
        if (dataset instanceof XisSymbolic) {
            xStr = ((XisSymbolic) dataset).getXSymbolicValue(series, item);
        }
        else if (dataset instanceof TimeSeriesCollection) {
            RegularTimePeriod p
                = ((TimeSeriesCollection) dataset).getSeries(series)
                    .getTimePeriod(item);
            xStr = p.toString();
        }
        else {
            double x = dataset.getXValue(series, item);
            xStr = Double.toString(round(x, 2));
        }
        return "X: " + xStr + ", Y: " + yStr;
    }

    
    @Override
    public String generateLabel(XYDataset dataset, int series, int category) {
        return null;  //TODO: implement this method properly
    }

    
    private static double round(double value, int nb) {
        if (nb <= 0) {
            return Math.floor(value + 0.5d);
        }
        double p = Math.pow(10, nb);
        double tempval = Math.floor(value * p + 0.5d);
        return tempval / p;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof SymbolicXYItemLabelGenerator) {
            return true;
        }
        return false;
    }

    
    @Override
    public int hashCode() {
        int result = 127;
        return result;
    }

}
