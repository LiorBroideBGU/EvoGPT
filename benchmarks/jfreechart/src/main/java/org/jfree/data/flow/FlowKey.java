

package org.jfree.data.flow;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;


public class FlowKey<K extends Comparable<K>> implements PublicCloneable, Serializable  {

    
    public static final String SELECTED_PROPERTY_KEY = "selected";

    
    private final int stage;
    
    
    private final K source;
    
    
    private final K destination;
    
    
    public FlowKey(int stage, K source, K destination) {
        Args.nullNotPermitted(source, "source");
        Args.nullNotPermitted(destination, "destination");
        this.stage = stage;
        this.source = source;
        this.destination = destination;
    }

    
    public int getStage() {
        return this.stage;
    }
    
    
    public K getSource() {
        return source;
    }

    
    public K getDestination() {
        return destination;
    }

    
    @Override
    public String toString() {
        return "[FlowKey: " + this.stage + ", " + this.source + " -> " + this.destination + "]";
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FlowKey<?> other = (FlowKey<?>) obj;
        if (this.stage != other.stage) {
            return false;
        }
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.destination, other.destination)) {
            return false;
        }
        return true;
    }
    
    
    @Override
    public int hashCode() {
        int hash = 5;
        hash = 67 * hash + this.stage;
        hash = 67 * hash + Objects.hashCode(this.source);
        hash = 67 * hash + Objects.hashCode(this.destination);
        return hash;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }    

}

