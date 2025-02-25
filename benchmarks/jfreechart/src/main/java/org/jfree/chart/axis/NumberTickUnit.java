

package org.jfree.chart.axis;

import java.io.Serializable;
import java.text.NumberFormat;
import org.jfree.chart.internal.Args;


public class NumberTickUnit extends TickUnit implements Serializable {

    
    private static final long serialVersionUID = 3849459506627654442L;

    
    private NumberFormat formatter;

    
    public NumberTickUnit(double size) {
        this(size, NumberFormat.getNumberInstance());
    }

    
    public NumberTickUnit(double size, NumberFormat formatter) {
        super(size);
        Args.nullNotPermitted(formatter, "formatter");
        this.formatter = formatter;
    }

    
    public NumberTickUnit(double size, NumberFormat formatter,
            int minorTickCount) {
        super(size, minorTickCount);
        Args.nullNotPermitted(formatter, "formatter");
        this.formatter = formatter;
    }

    
    @Override
    public String valueToString(double value) {
        return this.formatter.format(value);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NumberTickUnit)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        NumberTickUnit that = (NumberTickUnit) obj;
        if (!this.formatter.equals(that.formatter)) {
            return false;
        }
        return true;
    }

    
    @Override
    public String toString() {
        return "[NumberTickUnit: size=" + this.valueToString(this.getSize()) 
                + ", formatter=" + this.formatter + "]";
    }

    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 29 * result + (this.formatter != null
                ? this.formatter.hashCode() : 0);
        return result;
    }

}
