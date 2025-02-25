

package org.jfree.chart.labels;

import java.io.Serializable;
import java.util.Objects;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.Args;


public class ItemLabelPosition implements Serializable {

    
    private static final long serialVersionUID = 5845390630157034499L;

    
    private final ItemLabelAnchor itemLabelAnchor;

    
    private final TextAnchor textAnchor;

    
    private final TextAnchor rotationAnchor;

    
    private final double angle;

    
    public ItemLabelPosition() {
        this(ItemLabelAnchor.OUTSIDE12, TextAnchor.BOTTOM_CENTER,
                TextAnchor.CENTER, 0.0);
    }

    
    public ItemLabelPosition(ItemLabelAnchor itemLabelAnchor,
                             TextAnchor textAnchor) {
        this(itemLabelAnchor, textAnchor, TextAnchor.CENTER, 0.0);
    }

    
    public ItemLabelPosition(ItemLabelAnchor itemLabelAnchor, 
            TextAnchor textAnchor, TextAnchor rotationAnchor, double angle) {

        Args.nullNotPermitted(itemLabelAnchor, "itemLabelAnchor");
        Args.nullNotPermitted(textAnchor, "textAnchor");
        Args.nullNotPermitted(rotationAnchor, "rotationAnchor");
        this.itemLabelAnchor = itemLabelAnchor;
        this.textAnchor = textAnchor;
        this.rotationAnchor = rotationAnchor;
        this.angle = angle;
    }

    
    public ItemLabelAnchor getItemLabelAnchor() {
        return this.itemLabelAnchor;
    }

    
    public TextAnchor getTextAnchor() {
        return this.textAnchor;
    }

    
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }

    
    public double getAngle() {
        return this.angle;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ItemLabelPosition)) {
            return false;
        }
        ItemLabelPosition that = (ItemLabelPosition) obj;
        if (!this.itemLabelAnchor.equals(that.itemLabelAnchor)) {
            return false;
        }
        if (!this.textAnchor.equals(that.textAnchor)) {
            return false;
        }
        if (!this.rotationAnchor.equals(that.rotationAnchor)) {
            return false;
        }
        if (this.angle != that.angle) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 5;
        hash = 83 * hash + Objects.hashCode(this.itemLabelAnchor);
        hash = 83 * hash + Objects.hashCode(this.textAnchor);
        hash = 83 * hash + Objects.hashCode(this.rotationAnchor);
        hash = 83 * hash +
                (int) (Double.doubleToLongBits(this.angle) ^
                (Double.doubleToLongBits(this.angle) >>> 32));
        return hash;
    }

}
