

package org.jfree.data.statistics;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


public class BoxAndWhiskerItem implements Serializable {

    
    private static final long serialVersionUID = 7329649623148167423L;

    
    private Number mean;

    
    private Number median;

    
    private Number q1;

    
    private Number q3;

    
    private Number minRegularValue;

    
    private Number maxRegularValue;

    
    private Number minOutlier;

    
    private Number maxOutlier;

    
    private List<? extends Number> outliers;

    
    public BoxAndWhiskerItem(Number mean, Number median, Number q1, Number q3,
            Number minRegularValue, Number maxRegularValue, Number minOutlier,
            Number maxOutlier, List<? extends Number> outliers) {

        this.mean = mean;
        this.median = median;
        this.q1 = q1;
        this.q3 = q3;
        this.minRegularValue = minRegularValue;
        this.maxRegularValue = maxRegularValue;
        this.minOutlier = minOutlier;
        this.maxOutlier = maxOutlier;
        this.outliers = outliers;

    }

    
    public BoxAndWhiskerItem(double mean, double median, double q1, double q3,
            double minRegularValue, double maxRegularValue, double minOutlier,
            double maxOutlier, List<? extends Number> outliers) {

        // pass values to other constructor
        this(Double.valueOf(mean), Double.valueOf(median), Double.valueOf(q1),
                Double.valueOf(q3), Double.valueOf(minRegularValue),
                Double.valueOf(maxRegularValue), Double.valueOf(minOutlier),
                Double.valueOf(maxOutlier), outliers);

    }

    
    public Number getMean() {
        return this.mean;
    }

    
    public Number getMedian() {
        return this.median;
    }

    
    public Number getQ1() {
        return this.q1;
    }

    
    public Number getQ3() {
        return this.q3;
    }

    
    public Number getMinRegularValue() {
        return this.minRegularValue;
    }

    
    public Number getMaxRegularValue() {
        return this.maxRegularValue;
    }

    
    public Number getMinOutlier() {
        return this.minOutlier;
    }

    
    public Number getMaxOutlier() {
        return this.maxOutlier;
    }

    
    public List<? extends Number> getOutliers() {
        if (this.outliers == null) {
            return null;
        }
        return Collections.unmodifiableList(this.outliers);
    }

    
    @Override
    public String toString() {
        return super.toString() + "[mean=" + this.mean + ",median="
                + this.median + ",q1=" + this.q1 + ",q3=" + this.q3 + "]";
    }

    
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BoxAndWhiskerItem)) {
            return false;
        }
        BoxAndWhiskerItem that = (BoxAndWhiskerItem) obj;
        if (!Objects.equals(this.mean, that.mean)) {
            return false;
        }
        if (!Objects.equals(this.median, that.median)) {
            return false;
        }
        if (!Objects.equals(this.q1, that.q1)) {
            return false;
        }
        if (!Objects.equals(this.q3, that.q3)) {
            return false;
        }
        if (!Objects.equals(this.minRegularValue, that.minRegularValue)) {
            return false;
        }
        if (!Objects.equals(this.maxRegularValue, that.maxRegularValue)) {
            return false;
        }
        if (!Objects.equals(this.minOutlier, that.minOutlier)) {
            return false;
        }
        if (!Objects.equals(this.maxOutlier, that.maxOutlier)) {
            return false;
        }
        if (!Objects.equals(this.outliers, that.outliers)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 3;
        hash = 67 * hash + Objects.hashCode(this.mean);
        hash = 67 * hash + Objects.hashCode(this.median);
        hash = 67 * hash + Objects.hashCode(this.q1);
        hash = 67 * hash + Objects.hashCode(this.q3);
        hash = 67 * hash + Objects.hashCode(this.minRegularValue);
        hash = 67 * hash + Objects.hashCode(this.maxRegularValue);
        hash = 67 * hash + Objects.hashCode(this.minOutlier);
        hash = 67 * hash + Objects.hashCode(this.maxOutlier);
        hash = 67 * hash + Objects.hashCode(this.outliers);
        return hash;
    }

}
