

package org.jfree.chart.labels;

import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.HashUtils;
import org.jfree.data.xy.XYDataset;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MultipleXYSeriesLabelGenerator implements XYSeriesLabelGenerator,
        Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 138976236941898560L;

    
    public static final String DEFAULT_LABEL_FORMAT = "{0}";

    
    private final String formatPattern;

    
    private final String additionalFormatPattern;

    
    private Map seriesLabelLists;

    
    public MultipleXYSeriesLabelGenerator() {
        this(DEFAULT_LABEL_FORMAT);
    }

    
    public MultipleXYSeriesLabelGenerator(String format) {
        Args.nullNotPermitted(format, "format");
        this.formatPattern = format;
        this.additionalFormatPattern = "\n{0}";
        this.seriesLabelLists = new HashMap();
    }

    
    public void addSeriesLabel(int series, String label) {
        Integer key = series;
        List labelList = (List) this.seriesLabelLists.get(key);
        if (labelList == null) {
            labelList = new java.util.ArrayList();
            this.seriesLabelLists.put(key, labelList);
        }
        labelList.add(label);
    }

    
    public void clearSeriesLabels(int series) {
        Integer key = series;
        this.seriesLabelLists.put(key, null);
    }

    
    @Override
    public String generateLabel(XYDataset dataset, int series) {
        Args.nullNotPermitted(dataset, "dataset");
        StringBuilder label = new StringBuilder();
        label.append(MessageFormat.format(this.formatPattern,
                createItemArray(dataset, series)));
        Integer key = series;
        List extraLabels = (List) this.seriesLabelLists.get(key);
        if (extraLabels != null) {
            Object[] temp = new Object[1];
            for (int i = 0; i < extraLabels.size(); i++) {
                temp[0] = extraLabels.get(i);
                String labelAddition = MessageFormat.format(
                        this.additionalFormatPattern, temp);
                label.append(labelAddition);
            }
        }
        return label.toString();
    }

    
    protected Object[] createItemArray(XYDataset dataset, int series) {
        Object[] result = new Object[1];
        result[0] = dataset.getSeriesKey(series).toString();
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        MultipleXYSeriesLabelGenerator clone
                = (MultipleXYSeriesLabelGenerator) super.clone();
        clone.seriesLabelLists = new HashMap();
        Set keys = this.seriesLabelLists.keySet();
        for (Object key : keys) {
            Object entry = this.seriesLabelLists.get(key);
            Object toAdd = entry;
            if (entry instanceof PublicCloneable) {
                PublicCloneable pc = (PublicCloneable) entry;
                toAdd = pc.clone();
            }
            clone.seriesLabelLists.put(key, toAdd);
        }
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MultipleXYSeriesLabelGenerator)) {
            return false;
        }
        MultipleXYSeriesLabelGenerator that
                = (MultipleXYSeriesLabelGenerator) obj;
        if (!this.formatPattern.equals(that.formatPattern)) {
            return false;
        }
        if (!this.additionalFormatPattern.equals(
                that.additionalFormatPattern)) {
            return false;
        }
        if (!this.seriesLabelLists.equals(that.seriesLabelLists)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = 127;
        result = HashUtils.hashCode(result, this.formatPattern);
        result = HashUtils.hashCode(result, this.additionalFormatPattern);
        result = HashUtils.hashCode(result, this.seriesLabelLists);
        return result;
    }

}
