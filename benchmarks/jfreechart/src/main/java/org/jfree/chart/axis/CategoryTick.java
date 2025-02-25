

package org.jfree.chart.axis;

import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.text.TextAnchor;

import java.util.Objects;


public class CategoryTick extends Tick {

    
    private Comparable<?> category;

    
    private TextBlock label;

    
    private TextBlockAnchor labelAnchor;

    
    public CategoryTick(Comparable<?> category, TextBlock label,
            TextBlockAnchor labelAnchor, TextAnchor rotationAnchor, 
            double angle) {

        super("", TextAnchor.CENTER, rotationAnchor, angle);
        this.category = category;
        this.label = label;
        this.labelAnchor = labelAnchor;

    }

    
    public Comparable<?> getCategory() {
        return this.category;
    }

    
    public TextBlock getLabel() {
        return this.label;
    }

    
    public TextBlockAnchor getLabelAnchor() {
        return this.labelAnchor;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CategoryTick && super.equals(obj)) {
            CategoryTick that = (CategoryTick) obj;
            if (!Objects.equals(this.category, that.category)) {
                return false;
            }
            if (!Objects.equals(this.label, that.label)) {
                return false;
            }
            if (!Objects.equals(this.labelAnchor, that.labelAnchor)) {
                return false;
           }
            return true;
        }
        return false;
    }

    
    @Override
    public int hashCode() {
        int result = 41;
        result = 37 * result + this.category.hashCode();
        result = 37 * result + this.label.hashCode();
        result = 37 * result + this.labelAnchor.hashCode();
        return result;
    }
}
