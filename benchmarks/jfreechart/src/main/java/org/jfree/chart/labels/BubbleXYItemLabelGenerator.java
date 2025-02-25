

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Objects;

import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.renderer.xy.XYBubbleRenderer;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;


public class BubbleXYItemLabelGenerator extends AbstractXYItemLabelGenerator
        implements XYItemLabelGenerator, PublicCloneable, Serializable {

    
    static final long serialVersionUID = -8458568928021240922L;

    
    public static final String DEFAULT_FORMAT_STRING = "{3}";

    
    private NumberFormat zFormat;

    
    private DateFormat zDateFormat;

    
    public BubbleXYItemLabelGenerator() {
        this(DEFAULT_FORMAT_STRING, NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance(),
                NumberFormat.getNumberInstance());
    }

    
    public BubbleXYItemLabelGenerator(String formatString,
            NumberFormat xFormat, NumberFormat yFormat, NumberFormat zFormat) {
        super(formatString, xFormat, yFormat);
        Args.nullNotPermitted(zFormat, "zFormat");
        this.zFormat = zFormat;
    }

    
    public BubbleXYItemLabelGenerator(String formatString,
            DateFormat xFormat, DateFormat yFormat, DateFormat zFormat) {
        super(formatString, xFormat, yFormat);
        Args.nullNotPermitted(zFormat, "zFormat");
        this.zDateFormat = zFormat;
    }

    
    public NumberFormat getZFormat() {
        return this.zFormat;
    }

    
    public DateFormat getZDateFormat() {
        return this.zDateFormat;
    }

    
    @Override
    public String generateLabel(XYDataset dataset, int series, int item) {
        return generateLabelString(dataset, series, item);
    }

    
    @Override
    public String generateLabelString(XYDataset dataset, int series, int item) {
        String result;
        Object[] items;
        if (dataset instanceof XYZDataset) {
            items = createItemArray((XYZDataset) dataset, series, item);
        }
        else {
            items = createItemArray(dataset, series, item);
        }
        result = MessageFormat.format(getFormatString(), items);
        return result;
    }

    
    protected Object[] createItemArray(XYZDataset dataset,
                                       int series, int item) {

        Object[] result = new Object[4];
        result[0] = dataset.getSeriesKey(series).toString();

        Number x = dataset.getX(series, item);
        DateFormat xf = getXDateFormat();
        if (xf != null) {
            result[1] = xf.format(x);
        }
        else {
            result[1] = getXFormat().format(x);
        }

        Number y = dataset.getY(series, item);
        DateFormat yf = getYDateFormat();
        if (yf != null) {
            result[2] = yf.format(y);
        }
        else {
            result[2] = getYFormat().format(y);
        }

        Number z = dataset.getZ(series, item);
        if (this.zDateFormat != null) {
            result[3] = this.zDateFormat.format(z);
        }
        else {
            result[3] = this.zFormat.format(z);
        }

        return result;

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BubbleXYItemLabelGenerator)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        BubbleXYItemLabelGenerator that = (BubbleXYItemLabelGenerator) obj;
        if (!Objects.equals(this.zFormat, that.zFormat)) {
            return false;
        }
        if (!Objects.equals(this.zDateFormat, that.zDateFormat)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int h = super.hashCode();
        h = HashUtils.hashCode(h, this.zFormat);
        h = HashUtils.hashCode(h, this.zDateFormat);
        return h;
    }

}
