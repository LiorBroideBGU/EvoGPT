

package org.jfree.data.general;

import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.api.PublicCloneable;


public class DefaultValueDataset extends AbstractDataset
        implements ValueDataset, Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 8137521217249294891L;

    
    private Number value;

    
    public DefaultValueDataset() {
        this(null);
    }

    
    public DefaultValueDataset(double value) {
        this(Double.valueOf(value));
    }

    
    public DefaultValueDataset(Number value) {
        super();
        this.value = value;
    }

    
    @Override
    public Number getValue() {
        return this.value;
    }

    
    public void setValue(Number value) {
        this.value = value;
        notifyListeners(new DatasetChangeEvent(this, this));
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof ValueDataset) {
            ValueDataset vd = (ValueDataset) obj;
            return Objects.equals(this.value, vd.getValue());
        }
        return false;
    }

    
    @Override
    public int hashCode() {
        return (this.value != null ? this.value.hashCode() : 0);
    }

}
