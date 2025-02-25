

package org.jfree.chart.text.format;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;


public class HMSNumberFormat extends NumberFormat {

    private final NumberFormat formatter = new DecimalFormat("00");
    
    
    public HMSNumberFormat() {
        // nothing to do
    }

    
    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return format((long) number, toAppendTo, pos);
    }

    
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        StringBuffer sb = new StringBuffer();
        long hours = number / 3600;
        sb.append(this.formatter.format(hours)).append(":");
        long remaining = number - (hours * 3600);
        long minutes = remaining / 60;
        sb.append(this.formatter.format(minutes)).append(":");
        long seconds = remaining - (minutes * 60);
        sb.append(this.formatter.format(seconds));
        return sb;
    }

    
    @Override
    public Number parse (String source, ParsePosition parsePosition) {
        return null; // don't bother with parsing
    }

}
