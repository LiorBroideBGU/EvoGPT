

package org.jfree.chart.title;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.text.TextAnchor;
import org.jfree.data.Range;


public class ShortTextTitle extends TextTitle {

    
    public ShortTextTitle(String text) {
        setText(text);
    }

    
    @Override
    public Size2D arrange(Graphics2D g2, RectangleConstraint constraint) {
        RectangleConstraint cc = toContentConstraint(constraint);
        LengthConstraintType w = cc.getWidthConstraintType();
        LengthConstraintType h = cc.getHeightConstraintType();
        Size2D contentSize = null;
        if (w == LengthConstraintType.NONE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeNN(g2);
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.RANGE) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeRN(g2, cc.getWidthRange());
            }
            else if (h == LengthConstraintType.RANGE) {
                contentSize = arrangeRR(g2, cc.getWidthRange(),
                        cc.getHeightRange());
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        else if (w == LengthConstraintType.FIXED) {
            if (h == LengthConstraintType.NONE) {
                contentSize = arrangeFN(g2, cc.getWidth());
            }
            else if (h == LengthConstraintType.RANGE) {
                throw new RuntimeException("Not yet implemented.");
            }
            else if (h == LengthConstraintType.FIXED) {
                throw new RuntimeException("Not yet implemented.");
            }
        }
        assert contentSize != null;
        if (contentSize.width <= 0.0 || contentSize.height <= 0.0) {
            return new Size2D(0.0, 0.0);
        }
        else {
            return new Size2D(calculateTotalWidth(contentSize.getWidth()),
                    calculateTotalHeight(contentSize.getHeight()));
        }
    }

    
    @Override
    protected Size2D arrangeNN(Graphics2D g2) {
        Range max = new Range(0.0, Float.MAX_VALUE);
        return arrangeRR(g2, max, max);
    }

    
    @Override
    protected Size2D arrangeRN(Graphics2D g2, Range widthRange) {
        Size2D s = arrangeNN(g2);
        if (widthRange.contains(s.getWidth())) {
            return s;
        }
        double ww = widthRange.constrain(s.getWidth());
        return arrangeFN(g2, ww);
    }

    
    @Override
    protected Size2D arrangeFN(Graphics2D g2, double w) {
        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics(getFont());
        Rectangle2D bounds = TextUtils.getTextBounds(getText(), g2, fm);
        if (bounds.getWidth() <= w) {
            return new Size2D(w, bounds.getHeight());
        }
        else {
            return new Size2D(0.0, 0.0);
        }
    }

    
    @Override
    protected Size2D arrangeRR(Graphics2D g2, Range widthRange,
            Range heightRange) {

        g2.setFont(getFont());
        FontMetrics fm = g2.getFontMetrics(getFont());
        Rectangle2D bounds = TextUtils.getTextBounds(getText(), g2, fm);
        if (bounds.getWidth() <= widthRange.getUpperBound()
                && bounds.getHeight() <= heightRange.getUpperBound()) {
            return new Size2D(bounds.getWidth(), bounds.getHeight());
        }
        else {
            return new Size2D(0.0, 0.0);
        }
    }

    
    @Override
    public Object draw(Graphics2D g2, Rectangle2D area, Object params) {
        if (area.isEmpty()) {
            return null;
        }
        area = trimMargin(area);
        drawBorder(g2, area);
        area = trimBorder(area);
        area = trimPadding(area);
        g2.setFont(getFont());
        g2.setPaint(getPaint());
        TextUtils.drawAlignedString(getText(), g2, (float) area.getMinX(),
                (float) area.getMinY(), TextAnchor.TOP_LEFT);
        return null;
    }

}
