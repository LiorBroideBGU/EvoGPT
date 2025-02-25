

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.PieDataset;


public class AbstractPieItemLabelGenerator implements Serializable {

    
    private static final long serialVersionUID = 7347703325267846275L;

    
    private final String labelFormat;

    
    private NumberFormat numberFormat;

    
    private NumberFormat percentFormat;

    
    protected AbstractPieItemLabelGenerator(String labelFormat, 
            NumberFormat numberFormat, NumberFormat percentFormat) {
        Args.nullNotPermitted(labelFormat, "labelFormat");
        Args.nullNotPermitted(numberFormat, "numberFormat");
        Args.nullNotPermitted(percentFormat, "percentFormat");
        this.labelFormat = labelFormat;
        this.numberFormat = numberFormat;
        this.percentFormat = percentFormat;
    }

    
    public String getLabelFormat() {
        return this.labelFormat;
    }

    
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    
    public NumberFormat getPercentFormat() {
        return this.percentFormat;
    }

    
    protected Object[] createItemArray(PieDataset dataset, Comparable key) {
        Object[] result = new Object[4];
        double total = DatasetUtils.calculatePieDatasetTotal(dataset);
        result[0] = key.toString();
        Number value = dataset.getValue(key);
        if (value != null) {
            result[1] = this.numberFormat.format(value);
        }
        else {
            result[1] = "null";
        }
        double percent = 0.0;
        if (value != null) {
            double v = value.doubleValue();
            if (v > 0.0) {
                percent = v / total;
            }
        }
        result[2] = this.percentFormat.format(percent);
        result[3] = this.numberFormat.format(total);
        return result;
    }

    
    protected String generateSectionLabel(PieDataset dataset, Comparable key) {
        String result = null;
        if (dataset != null) {
            Object[] items = createItemArray(dataset, key);
            result = MessageFormat.format(this.labelFormat, items);
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof AbstractPieItemLabelGenerator)) {
            return false;
        }

        AbstractPieItemLabelGenerator that
                = (AbstractPieItemLabelGenerator) obj;
        if (!this.labelFormat.equals(that.labelFormat)) {
            return false;
        }
        if (!this.numberFormat.equals(that.numberFormat)) {
            return false;
        }
        if (!this.percentFormat.equals(that.percentFormat)) {
            return false;
        }
        return true;

    }

    
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtils.hashCode(result, this.labelFormat);
        result = HashUtils.hashCode(result, this.numberFormat);
        result = HashUtils.hashCode(result, this.percentFormat);
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        AbstractPieItemLabelGenerator clone
                = (AbstractPieItemLabelGenerator) super.clone();
        if (this.numberFormat != null) {
            clone.numberFormat = (NumberFormat) this.numberFormat.clone();
        }
        if (this.percentFormat != null) {
            clone.percentFormat = (NumberFormat) this.percentFormat.clone();
        }
        return clone;
    }

}
