

package org.jfree.chart.title;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.api.HorizontalAlignment;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.api.VerticalAlignment;


public class ImageTitle extends Title {

    
    private Image image;

    
    public ImageTitle(Image image) {
        this(image, image.getHeight(null), image.getWidth(null),
                Title.DEFAULT_POSITION, Title.DEFAULT_HORIZONTAL_ALIGNMENT,
                Title.DEFAULT_VERTICAL_ALIGNMENT, Title.DEFAULT_PADDING);
    }

    
    public ImageTitle(Image image, RectangleEdge position,
                      HorizontalAlignment horizontalAlignment,
                      VerticalAlignment verticalAlignment) {

        this(image, image.getHeight(null), image.getWidth(null),
                position, horizontalAlignment, verticalAlignment,
                Title.DEFAULT_PADDING);
    }

    
    public ImageTitle(Image image, int height, int width,
                      RectangleEdge position,
                      HorizontalAlignment horizontalAlignment,
                      VerticalAlignment verticalAlignment,
                      RectangleInsets padding) {

        super(position, horizontalAlignment, verticalAlignment, padding);
        if (image == null) {
            throw new NullPointerException("Null 'image' argument.");
        }
        this.image = image;
        setHeight(height);
        setWidth(width);

    }

    
    public Image getImage() {
        return this.image;
    }

    
    public void setImage(Image image) {
        if (image == null) {
            throw new NullPointerException("Null 'image' argument.");
        }
        this.image = image;
        notifyListeners(new TitleChangeEvent(this));
    }

    
    @Override
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        Size2D s = new Size2D(this.image.getWidth(null),
                this.image.getHeight(null));
        return new Size2D(calculateTotalWidth(s.getWidth()),
                calculateTotalHeight(s.getHeight()));
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        RectangleEdge position = getPosition();
        if (position == RectangleEdge.TOP || position == RectangleEdge.BOTTOM) {
            drawHorizontal(g2, area);
        }
        else if (position == RectangleEdge.LEFT
                     || position == RectangleEdge.RIGHT) {
            drawVertical(g2, area);
        }
        else {
            throw new RuntimeException("Invalid title position.");
        }
    }

    
    protected Size2D drawHorizontal(Graphics2D g2, Rectangle2D chartArea) {
        double startY;
        double topSpace;
        double bottomSpace;
        double leftSpace;
        double rightSpace;

        double w = getWidth();
        double h = getHeight();
        RectangleInsets padding = getPadding();
        topSpace = padding.calculateTopOutset(h);
        bottomSpace = padding.calculateBottomOutset(h);
        leftSpace = padding.calculateLeftOutset(w);
        rightSpace = padding.calculateRightOutset(w);

        if (getPosition() == RectangleEdge.TOP) {
            startY = chartArea.getY() + topSpace;
        }
        else {
            startY = chartArea.getY() + chartArea.getHeight() - bottomSpace - h;
        }

        // what is our alignment?
        double startX = 0.0;
        switch (getHorizontalAlignment()) {
            case CENTER:
                startX = chartArea.getX() + leftSpace + chartArea.getWidth() / 2.0
                        - w / 2.0;
                break;
            case LEFT:
                startX = chartArea.getX() + leftSpace;
                break;
            case RIGHT:
                startX = chartArea.getX() + chartArea.getWidth() - rightSpace - w;
                break;
            default:
                throw new IllegalStateException("Unexpected horizontal alignment.");
        }
        g2.drawImage(this.image, (int) startX, (int) startY, (int) w, (int) h,
                null);

        return new Size2D(chartArea.getWidth() + leftSpace + rightSpace,
            h + topSpace + bottomSpace);

    }

    
    protected Size2D drawVertical(Graphics2D g2, Rectangle2D chartArea) {

        double startX;
        double topSpace = 0.0;
        double bottomSpace = 0.0;
        double leftSpace = 0.0;
        double rightSpace = 0.0;

        double w = getWidth();
        double h = getHeight();

        RectangleInsets padding = getPadding();
        if (padding != null) {
            topSpace = padding.calculateTopOutset(h);
            bottomSpace = padding.calculateBottomOutset(h);
            leftSpace = padding.calculateLeftOutset(w);
            rightSpace = padding.calculateRightOutset(w);
        }

        if (getPosition() == RectangleEdge.LEFT) {
            startX = chartArea.getX() + leftSpace;
        }
        else {
            startX = chartArea.getMaxX() - rightSpace - w;
        }

        // what is our alignment?
        double startY = 0.0;
        switch (getVerticalAlignment()) {
            case CENTER:
                startY = chartArea.getMinY() + topSpace
                        + chartArea.getHeight() / 2.0 - h / 2.0;
                break;
            case TOP:
                startY = chartArea.getMinY() + topSpace;
                break;
            case BOTTOM:
                startY = chartArea.getMaxY() - bottomSpace - h;
                break;
            default:
                throw new IllegalStateException("Unexpected vertical alignment.");
        }

        g2.drawImage(this.image, (int) startX, (int) startY, (int) w, (int) h,
                null);

        return new Size2D(chartArea.getWidth() + leftSpace + rightSpace,
            h + topSpace + bottomSpace);

    }

    
    @Override
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        draw(g2, area);
        return null;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ImageTitle)) {
            return false;
        }
        ImageTitle that = (ImageTitle) obj;
        if (!Objects.equals(this.image, that.image)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int hash = super.hashCode();
        hash = 83 * hash + Objects.hashCode(this.image);
        return hash;
    }

}
