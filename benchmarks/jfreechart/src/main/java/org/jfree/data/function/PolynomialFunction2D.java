

package org.jfree.data.function;

import java.io.Serializable;
import java.util.Arrays;
import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;


public class PolynomialFunction2D implements Function2D, Serializable {

    
    private double[] coefficients;

    
    public PolynomialFunction2D(double[] coefficients) {
        Args.nullNotPermitted(coefficients, "coefficients");
        this.coefficients = (double[]) coefficients.clone();
    }

    
    public double[] getCoefficients() {
        return (double[]) this.coefficients.clone();
    }

    
    public int getOrder() {
        return this.coefficients.length - 1;
    }

    
    @Override
    public double getValue(double x) {
        double y = 0;
        for(int i = 0; i < coefficients.length; i++){
            y += coefficients[i] * Math.pow(x, i);
        }
        return y;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PolynomialFunction2D)) {
            return false;
        }
        PolynomialFunction2D that = (PolynomialFunction2D) obj;
        return Arrays.equals(this.coefficients, that.coefficients);
    }

    
    @Override
    public int hashCode() {
        return HashUtils.hashCodeForDoubleArray(this.coefficients);
    }

}
