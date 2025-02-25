

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.category.CategoryDataset;


public class StandardCategorySeriesLabelGenerator implements
    CategorySeriesLabelGenerator, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 4630760091523940820L;

    
    public static final String DEFAULT_LABEL_FORMAT = "{0}";

    
    private final String formatPattern;

    
    public StandardCategorySeriesLabelGenerator() {
        this(DEFAULT_LABEL_FORMAT);
    }

    
    public StandardCategorySeriesLabelGenerator(String format) {
        Args.nullNotPermitted(format, "format");
        this.formatPattern = format;
    }

    
    @Override
    public String generateLabel(CategoryDataset dataset, int series) {
        Args.nullNotPermitted(dataset, "dataset");
        String label = MessageFormat.format(this.formatPattern,
                createItemArray(dataset, series));
        return label;
    }

    
    protected Object[] createItemArray(CategoryDataset dataset, int series) {
        Object[] result = new Object[1];
        result[0] = dataset.getRowKey(series).toString();
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
        if (!(obj instanceof StandardCategorySeriesLabelGenerator)) {
            return false;
        }
        StandardCategorySeriesLabelGenerator that
                = (StandardCategorySeriesLabelGenerator) obj;
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
