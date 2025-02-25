

package org.jfree.chart.axis;

import java.io.Serializable;
import org.jfree.chart.text.TextBlockAnchor;
import org.jfree.chart.api.RectangleAnchor;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.Args;


public class CategoryLabelPosition implements Serializable {

    
    private static final long serialVersionUID = 5168681143844183864L;

    
    private RectangleAnchor categoryAnchor;

    
    private TextBlockAnchor labelAnchor;

    
    private TextAnchor rotationAnchor;

    
    private double angle;

    
    private CategoryLabelWidthType widthType;

    
    private float widthRatio;

    
    public CategoryLabelPosition() {
        this(RectangleAnchor.CENTER, TextBlockAnchor.BOTTOM_CENTER,
                TextAnchor.CENTER, 0.0, CategoryLabelWidthType.CATEGORY, 0.95f);
    }

    
    public CategoryLabelPosition(RectangleAnchor categoryAnchor,
                                 TextBlockAnchor labelAnchor) {
        // argument checking delegated...
        this(categoryAnchor, labelAnchor, TextAnchor.CENTER, 0.0,
                CategoryLabelWidthType.CATEGORY, 0.95f);
    }

    
    public CategoryLabelPosition(RectangleAnchor categoryAnchor,
            TextBlockAnchor labelAnchor, CategoryLabelWidthType widthType,
            float widthRatio) {
        // argument checking delegated...
        this(categoryAnchor, labelAnchor, TextAnchor.CENTER, 0.0, widthType,
                widthRatio);
    }

    
    public CategoryLabelPosition(RectangleAnchor categoryAnchor,
            TextBlockAnchor labelAnchor, TextAnchor rotationAnchor, 
            double angle, CategoryLabelWidthType widthType, float widthRatio) {

        Args.nullNotPermitted(categoryAnchor, "categoryAnchor");
        Args.nullNotPermitted(labelAnchor, "labelAnchor");
        Args.nullNotPermitted(rotationAnchor, "rotationAnchor");
        Args.nullNotPermitted(widthType, "widthType");

        this.categoryAnchor = categoryAnchor;
        this.labelAnchor = labelAnchor;
        this.rotationAnchor = rotationAnchor;
        this.angle = angle;
        this.widthType = widthType;
        this.widthRatio = widthRatio;

    }

    
    public RectangleAnchor getCategoryAnchor() {
        return this.categoryAnchor;
    }

    
    public TextBlockAnchor getLabelAnchor() {
        return this.labelAnchor;
    }

    
    public TextAnchor getRotationAnchor() {
        return this.rotationAnchor;
    }

    
    public double getAngle() {
        return this.angle;
    }

    
    public CategoryLabelWidthType getWidthType() {
        return this.widthType;
    }

    
    public float getWidthRatio() {
        return this.widthRatio;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryLabelPosition)) {
            return false;
        }
        CategoryLabelPosition that = (CategoryLabelPosition) obj;
        if (!this.categoryAnchor.equals(that.categoryAnchor)) {
            return false;
        }
        if (!this.labelAnchor.equals(that.labelAnchor)) {
            return false;
        }
        if (!this.rotationAnchor.equals(that.rotationAnchor)) {
            return false;
        }
        if (this.angle != that.angle) {
            return false;
        }
        if (this.widthType != that.widthType) {
            return false;
        }
        if (this.widthRatio != that.widthRatio) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = 19;
        result = 37 * result + this.categoryAnchor.hashCode();
        result = 37 * result + this.labelAnchor.hashCode();
        result = 37 * result + this.rotationAnchor.hashCode();
        return result;
    }

}
