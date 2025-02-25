

package org.jfree.data.function;

import java.io.Serializable;

import org.jfree.chart.internal.HashUtils;


public class NormalDistributionFunction2D implements Function2D, Serializable {

    
    private double mean;

    
    private double std;

    
    private double factor;

    
    private double denominator;

    
    public NormalDistributionFunction2D(double mean, double std) {
        if (std <= 0) {
            throw new IllegalArgumentException("Requires 'std' > 0.");
        }
        this.mean = mean;
        this.std = std;
        // calculate constant values
        this.factor = 1 / (std * Math.sqrt(2.0 * Math.PI));
        this.denominator = 2 * std * std;
    }

    
    public double getMean() {
        return this.mean;
    }
    
    
    public double getStandardDeviation() {
        return this.std;
    }

    
    @Override
    public double getValue(double x) {
        double z = x - this.mean;
        return this.factor * Math.exp(-z * z / this.denominator);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof NormalDistributionFunction2D)) {
            return false;
        }
        NormalDistributionFunction2D that = (NormalDistributionFunction2D) obj;
        if (this.mean != that.mean) {
            return false;
        }
        if (this.std != that.std) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = 29;
        result = HashUtils.hashCode(result, this.mean);
        result = HashUtils.hashCode(result, this.std);
        return result;
    }

}
