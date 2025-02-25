

package org.jfree.chart.text.format;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.jfree.chart.internal.Args;


public class LogFormat extends NumberFormat {

    
    private double base;

    
    private double baseLog;

    
    private String baseLabel;

    
    private String powerLabel;

    
    private boolean showBase;

    
    private NumberFormat formatter = new DecimalFormat("0.0#");

    
    public LogFormat() {
        this(10.0, "10", true);
    }

    
    public LogFormat(double base, String baseLabel, boolean showBase) {
        this(base, baseLabel, "^", showBase);
    }

    
    public LogFormat(double base, String baseLabel, String powerLabel,
            boolean showBase) {
        Args.nullNotPermitted(baseLabel, "baseLabel");
        Args.nullNotPermitted(powerLabel, "powerLabel");
        this.base = base;
        this.baseLog = Math.log(this.base);
        this.baseLabel = baseLabel;
        this.showBase = showBase;
        this.powerLabel = powerLabel;
    }

    
    public NumberFormat getExponentFormat() {
        return (NumberFormat) this.formatter.clone();
    }

    
    public void setExponentFormat(NumberFormat format) {
        Args.nullNotPermitted(format, "format");
        this.formatter = format;
    }

    
    private double calculateLog(double value) {
        return Math.log(value) / this.baseLog;
    }

    
    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        StringBuffer result = new StringBuffer();
        if (this.showBase) {
            result.append(this.baseLabel);
            result.append(this.powerLabel);
        }
        result.append(this.formatter.format(calculateLog(number)));
        return result;
    }

    
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        StringBuffer result = new StringBuffer();
        if (this.showBase) {
            result.append(this.baseLabel);
            result.append(this.powerLabel);
        }
        result.append(this.formatter.format(calculateLog(number)));
        return result;
    }

    
    @Override
    public Number parse (String source, ParsePosition parsePosition) {
        return null; // don't bother with parsing
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LogFormat)) {
            return false;
        }
        LogFormat that = (LogFormat) obj;
        if (this.base != that.base) {
            return false;
        }
        if (!this.baseLabel.equals(that.baseLabel)) {
            return false;
        }
        if (this.baseLog != that.baseLog) {
            return false;
        }
        if (this.showBase != that.showBase) {
            return false;
        }
        if (!this.formatter.equals(that.formatter)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() {
        LogFormat clone = (LogFormat) super.clone();
        clone.formatter = (NumberFormat) this.formatter.clone();
        return clone;
    }

}
