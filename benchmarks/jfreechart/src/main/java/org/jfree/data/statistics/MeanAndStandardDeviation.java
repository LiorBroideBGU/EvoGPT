

package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.Objects;


public class MeanAndStandardDeviation implements Serializable {

    
    private static final long serialVersionUID = 7413468697315721515L;

    
    private Number mean;

    
    private Number standardDeviation;

    
    public MeanAndStandardDeviation(double mean, double standardDeviation) {
        this(Double.valueOf(mean), Double.valueOf(standardDeviation));
    }

    
    public MeanAndStandardDeviation(Number mean, Number standardDeviation) {
        this.mean = mean;
        this.standardDeviation = standardDeviation;
    }

    
    public Number getMean() {
        return this.mean;
    }

    
    public double getMeanValue() {
        double result = Double.NaN;
        if (this.mean != null) {
            result = this.mean.doubleValue();
        }
        return result;
    }

    
    public Number getStandardDeviation() {
        return this.standardDeviation;
    }

    
    public double getStandardDeviationValue() {
        double result = Double.NaN;
        if (this.standardDeviation != null) {
            result = this.standardDeviation.doubleValue();
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MeanAndStandardDeviation)) {
            return false;
        }
        MeanAndStandardDeviation that = (MeanAndStandardDeviation) obj;
        if (!Objects.equals(this.mean, that.mean)) {
            return false;
        }
        if (!Objects.equals(this.standardDeviation, that.standardDeviation)
        ) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 79 * hash + Objects.hashCode( this.mean );
        hash = 79 * hash + Objects.hashCode( this.standardDeviation );
        return hash;
    }

    
    @Override
    public String toString() {
        return "[" + this.mean + ", " + this.standardDeviation + "]";
    }

}