

package org.jfree.chart.annotations;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CategoryAnchor;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AnnotationChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.category.CategoryDataset;


public class CategoryTextAnnotation extends TextAnnotation
        implements CategoryAnnotation, Cloneable, PublicCloneable,
                   Serializable {

    
    private static final long serialVersionUID = 3333360090781320147L;

    
    private Comparable category;

    
    private CategoryAnchor categoryAnchor;

    
    private double value;

    
    public CategoryTextAnnotation(String text, Comparable category,
                                  double value) {
        super(text);
        Args.nullNotPermitted(category, "category");
        this.category = category;
        this.value = value;
        this.categoryAnchor = CategoryAnchor.MIDDLE;
    }

    
    public Comparable getCategory() {
        return this.category;
    }

    
    public void setCategory(Comparable category) {
        Args.nullNotPermitted(category, "category");
        this.category = category;
        fireAnnotationChanged();
    }

    
    public CategoryAnchor getCategoryAnchor() {
        return this.categoryAnchor;
    }

    
    public void setCategoryAnchor(CategoryAnchor anchor) {
        Args.nullNotPermitted(anchor, "anchor");
        this.categoryAnchor = anchor;
        fireAnnotationChanged();
    }

    
    public double getValue() {
        return this.value;
    }

    
    public void setValue(double value) {
        this.value = value;
        fireAnnotationChanged();
    }

    
    @Override
    public void draw(Graphics2D g2, CategoryPlot plot, Rectangle2D dataArea,
            CategoryAxis domainAxis, ValueAxis rangeAxis) {

        CategoryDataset dataset = plot.getDataset();
        int catIndex = dataset.getColumnIndex(this.category);
        int catCount = dataset.getColumnCount();

        float anchorX = 0.0f;
        float anchorY = 0.0f;
        PlotOrientation orientation = plot.getOrientation();
        RectangleEdge domainEdge = Plot.resolveDomainAxisLocation(
                plot.getDomainAxisLocation(), orientation);
        RectangleEdge rangeEdge = Plot.resolveRangeAxisLocation(
                plot.getRangeAxisLocation(), orientation);

        if (orientation == PlotOrientation.HORIZONTAL) {
            anchorY = (float) domainAxis.getCategoryJava2DCoordinate(
                    this.categoryAnchor, catIndex, catCount, dataArea,
                    domainEdge);
            anchorX = (float) rangeAxis.valueToJava2D(this.value, dataArea,
                    rangeEdge);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            anchorX = (float) domainAxis.getCategoryJava2DCoordinate(
                    this.categoryAnchor, catIndex, catCount, dataArea,
                    domainEdge);
            anchorY = (float) rangeAxis.valueToJava2D(this.value, dataArea,
                    rangeEdge);
        }
        g2.setFont(getFont());
        g2.setPaint(getPaint());
        TextUtils.drawRotatedString(getText(), g2, anchorX, anchorY,
                getTextAnchor(), getRotationAngle(), getRotationAnchor());

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryTextAnnotation)) {
            return false;
        }
        CategoryTextAnnotation that = (CategoryTextAnnotation) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!this.category.equals(that.getCategory())) {
            return false;
        }
        if (!this.categoryAnchor.equals(that.getCategoryAnchor())) {
            return false;
        }
        if (this.value != that.getValue()) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 37 * result + this.category.hashCode();
        result = 37 * result + this.categoryAnchor.hashCode();
        long temp = Double.doubleToLongBits(this.value);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}
