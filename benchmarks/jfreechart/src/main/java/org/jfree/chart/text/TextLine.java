

package org.jfree.chart.text;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.internal.Args;


public class TextLine implements Serializable {

    
    private static final long serialVersionUID = 7100085690160465444L;
    
    
    private List<TextFragment> fragments;

    
    public TextLine() {
        this.fragments = new ArrayList<>();
    }
    
    
    public TextLine(String text) {
        this(text, TextFragment.DEFAULT_FONT);   
    }
    
    
    public TextLine(String text, Font font) {
        this.fragments = new java.util.ArrayList<>();
        final TextFragment fragment = new TextFragment(text, font);
        this.fragments.add(fragment);
    }
    
    
    public TextLine(String text, Font font, Paint paint) {
        Args.nullNotPermitted(text, "text");
        Args.nullNotPermitted(font, "font");
        Args.nullNotPermitted(paint, "paint");
        this.fragments = new ArrayList<>();
        final TextFragment fragment = new TextFragment(text, font, paint);
        this.fragments.add(fragment);
    }
    
    
    public void addFragment(TextFragment fragment) {
        Args.nullNotPermitted(fragment, "fragment");
        this.fragments.add(fragment);        
    }
    
    
    public void removeFragment(TextFragment fragment) {
        this.fragments.remove(fragment);
    }
    
    
    public void draw(Graphics2D g2, float anchorX, float anchorY, 
            TextAnchor anchor, float rotateX, float rotateY, double angle) {
    
        Size2D dim = calculateDimensions(g2);
        float xAdj = 0.0f;
        if (anchor.isHorizontalCenter()) {
            xAdj = (float) -dim.getWidth() / 2.0f;
        }
        else if (anchor.isRight()) {
            xAdj = (float) -dim.getWidth();
        }
        float x = anchorX + xAdj;
        final float yOffset = calculateBaselineOffset(g2, anchor);
        for (TextFragment fragment : this.fragments) {
            final Size2D d = fragment.calculateDimensions(g2);
            fragment.draw(g2, x, anchorY + yOffset, TextAnchor.BASELINE_LEFT,
                    rotateX, rotateY, angle);
            x = x + (float) d.getWidth();
        }
    
    }
    
    
    public Size2D calculateDimensions(Graphics2D g2) {
        double width = 0.0;
        double height = 0.0;
        for (TextFragment fragment : this.fragments) {
            final Size2D dimension = fragment.calculateDimensions(g2);
            width = width + dimension.getWidth();
            height = Math.max(height, dimension.getHeight());
        }
        return new Size2D(width, height);
    }
    
    
    public TextFragment getFirstTextFragment() {
        TextFragment result = null;
        if (this.fragments.size() > 0) {
            result = this.fragments.get(0);
        }    
        return result;
    }
    
    
    public TextFragment getLastTextFragment() {
        TextFragment result = null;
        if (this.fragments.size() > 0) {
            result = this.fragments.get(this.fragments.size() - 1);
        }    
        return result;
    }
    
    
    private float calculateBaselineOffset(Graphics2D g2, TextAnchor anchor) {
        float result = 0.0f;
        for (TextFragment fragment : this.fragments) {
            result = Math.max(result,
                    fragment.calculateBaselineOffset(g2, anchor));
        }
        return result;
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;   
        }
        if (obj instanceof TextLine) {
            final TextLine line = (TextLine) obj;
            return this.fragments.equals(line.fragments);
        }
        return false;
    }

    
    @Override
    public int hashCode() {
        return (this.fragments != null ? this.fragments.hashCode() : 0);
    }

}
