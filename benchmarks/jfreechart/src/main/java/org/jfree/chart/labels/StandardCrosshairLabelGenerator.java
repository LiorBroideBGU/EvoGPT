

package org.jfree.chart.labels;

import java.io.Serializable;
import java.text.MessageFormat;
import java.text.NumberFormat;
import org.jfree.chart.internal.Args;
import org.jfree.chart.plot.Crosshair;


public class StandardCrosshairLabelGenerator implements CrosshairLabelGenerator,
        Serializable {

    
    private final String labelTemplate;

    
    private final NumberFormat numberFormat;

    
    public StandardCrosshairLabelGenerator() {
        this("{0}", NumberFormat.getNumberInstance());
    }

    
    public StandardCrosshairLabelGenerator(String labelTemplate,
            NumberFormat numberFormat) {
        super();
        Args.nullNotPermitted(labelTemplate, "labelTemplate");
        Args.nullNotPermitted(numberFormat, "numberFormat");
        this.labelTemplate = labelTemplate;
        this.numberFormat = numberFormat;
    }

    
    public String getLabelTemplate() {
        return this.labelTemplate;
    }

    
    public NumberFormat getNumberFormat() {
        return this.numberFormat;
    }

    
    @Override
    public String generateLabel(Crosshair crosshair) {
        Object[] v = new Object[] {this.numberFormat.format(
                crosshair.getValue())};
        String result = MessageFormat.format(this.labelTemplate, v);
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardCrosshairLabelGenerator)) {
            return false;
        }
        StandardCrosshairLabelGenerator that
                = (StandardCrosshairLabelGenerator) obj;
        if (!this.labelTemplate.equals(that.labelTemplate)) {
            return false;
        }
        if (!this.numberFormat.equals(that.numberFormat)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        return this.labelTemplate.hashCode();
    }

}
