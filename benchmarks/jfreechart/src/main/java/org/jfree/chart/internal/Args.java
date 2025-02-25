

package org.jfree.chart.internal;


public class Args {

    
    public static void nullNotPermitted(Object param, String name) {
        if (param == null) {
            throw new IllegalArgumentException("Null '" + name + "' argument.");
        }
    }
    
    
    public static void requireNonNegative(int value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Require '" + name + "' (" 
                    + value + ") to be non-negative.");
        }
    }
    
    
    public static void requireNonNegative(double value, String name) {
        if (value < 0) {
            throw new IllegalArgumentException("Require '" + name + "' (" 
                    + value + ") to be non-negative.");
        }
    }
    
    
    public static void requireInRange(int value, String name, 
            int lowerBound, int upperBound) {
        if (value < lowerBound || value > upperBound) {
            throw new IllegalArgumentException("Require '" + name + "' (" 
                    + value + ") to be in the range " + lowerBound + " to " 
                    + upperBound);
        }
    }
    
    
    public static void requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("Require '" + name + "' (" 
                    + value + ") to be finite.");
        }
    }
}
