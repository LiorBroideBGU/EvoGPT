

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.xy.XYDataset;


public class StandardXYSeriesLabelGenerator implements XYSeriesLabelGenerator,
        Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 1916017081848400024L;

    
    public static final String DEFAULT_LABEL_FORMAT = "{0}";

    
    private final String formatPattern;

    
    public StandardXYSeriesLabelGenerator() {
        this(DEFAULT_LABEL_FORMAT);
    }

    
    public StandardXYSeriesLabelGenerator(String format) {
        Args.nullNotPermitted(format, "format");
        this.formatPattern = format;
    }

    
    @Override
    public String generateLabel(XYDataset dataset, int series) {
        Args.nullNotPermitted(dataset, "dataset");
        String label = MessageFormat.format(
            this.formatPattern, createItemArray(dataset, series)
        );
        return label;
    }

    
    protected Object[] createItemArray(XYDataset dataset, int series) {
        Object[] result = new Object[1];
        result[0] = dataset.getSeriesKey(series).toString();
        return result;
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
        if (!(obj instanceof StandardXYSeriesLabelGenerator)) {
            return false;
        }
        StandardXYSeriesLabelGenerator that
                = (StandardXYSeriesLabelGenerator) obj;
        if (!this.formatPattern.equals(that.formatPattern)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtils.hashCode(result, this.formatPattern);
        return result;
    }

}
