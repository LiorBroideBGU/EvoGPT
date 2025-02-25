

package org.jfree.chart.swing;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Crosshair;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleAnchor;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;


public class CrosshairOverlay extends AbstractOverlay implements Overlay,
        PropertyChangeListener, PublicCloneable, Cloneable, Serializable {

    
    protected List<Crosshair> xCrosshairs;

    
    protected List<Crosshair> yCrosshairs;

    
    public CrosshairOverlay() {
        super();
        this.xCrosshairs = new ArrayList<>();
        this.yCrosshairs = new ArrayList<>();
    }

    
    public void addDomainCrosshair(Crosshair crosshair) {
        Args.nullNotPermitted(crosshair, "crosshair");
        this.xCrosshairs.add(crosshair);
        crosshair.addPropertyChangeListener(this);
        fireOverlayChanged();
    }

    
    public void removeDomainCrosshair(Crosshair crosshair) {
        Args.nullNotPermitted(crosshair, "crosshair");
        if (this.xCrosshairs.remove(crosshair)) {
            crosshair.removePropertyChangeListener(this);
            fireOverlayChanged();
        }
    }

    
    public void clearDomainCrosshairs() {
        if (this.xCrosshairs.isEmpty()) {
            return;  // nothing to do - avoids firing change event
        }
        for (Crosshair c : getDomainCrosshairs()) {
            this.xCrosshairs.remove(c);
            c.removePropertyChangeListener(this);
        }
        fireOverlayChanged();
    }

    
    public List<Crosshair> getDomainCrosshairs() {
        return new ArrayList<>(this.xCrosshairs);
    }

    
    public void addRangeCrosshair(Crosshair crosshair) {
        Args.nullNotPermitted(crosshair, "crosshair");
        this.yCrosshairs.add(crosshair);
        crosshair.addPropertyChangeListener(this);
        fireOverlayChanged();
    }

    
    public void removeRangeCrosshair(Crosshair crosshair) {
        Args.nullNotPermitted(crosshair, "crosshair");
        if (this.yCrosshairs.remove(crosshair)) {
            crosshair.removePropertyChangeListener(this);
            fireOverlayChanged();
        }
    }

    
    public void clearRangeCrosshairs() {
        if (this.yCrosshairs.isEmpty()) {
            return;  // nothing to do - avoids change notification
        }
        for (Crosshair c : getRangeCrosshairs()) {
            this.yCrosshairs.remove(c);
            c.removePropertyChangeListener(this);
        }
        fireOverlayChanged();
    }

    
    public List<Crosshair> getRangeCrosshairs() {
        return new ArrayList<>(this.yCrosshairs);
    }

    
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        fireOverlayChanged();
    }

    
    @Override
    public void paintOverlay(Graphics2D g2, ChartPanel chartPanel) {
        Shape savedClip = g2.getClip();
        Rectangle2D dataArea = chartPanel.getScreenDataArea();
        g2.clip(dataArea);
        JFreeChart chart = chartPanel.getChart();
        XYPlot plot = (XYPlot) chart.getPlot();
        ValueAxis xAxis = plot.getDomainAxis();
        RectangleEdge xAxisEdge = plot.getDomainAxisEdge();
        for (Crosshair ch : getDomainCrosshairs()) {
            if (ch.isVisible()) {
                double x = ch.getValue();
                double xx = xAxis.valueToJava2D(x, dataArea, xAxisEdge);
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    drawVerticalCrosshair(g2, dataArea, xx, ch);
                } else {
                    drawHorizontalCrosshair(g2, dataArea, xx, ch);
                }
            }
        }
        ValueAxis yAxis = plot.getRangeAxis();
        RectangleEdge yAxisEdge = plot.getRangeAxisEdge();
        for (Crosshair ch : getRangeCrosshairs()) {
            if (ch.isVisible()) {
                double y = ch.getValue();
                double yy = yAxis.valueToJava2D(y, dataArea, yAxisEdge);
                if (plot.getOrientation() == PlotOrientation.VERTICAL) {
                    drawHorizontalCrosshair(g2, dataArea, yy, ch);
                } else {
                    drawVerticalCrosshair(g2, dataArea, yy, ch);
                }
            }
        }
        g2.setClip(savedClip);
    }

    
    protected void drawHorizontalCrosshair(Graphics2D g2, Rectangle2D dataArea,
            double y, Crosshair crosshair) {

        if (y >= dataArea.getMinY() && y <= dataArea.getMaxY()) {
            Line2D line = new Line2D.Double(dataArea.getMinX(), y,
                    dataArea.getMaxX(), y);
            Paint savedPaint = g2.getPaint();
            Stroke savedStroke = g2.getStroke();
            g2.setPaint(crosshair.getPaint());
            g2.setStroke(crosshair.getStroke());
            g2.draw(line);
            if (crosshair.isLabelVisible()) {
                String label = crosshair.getLabelGenerator().generateLabel(
                        crosshair);
                if (label != null && !label.isEmpty()) {
                    Font savedFont = g2.getFont();
                    g2.setFont(crosshair.getLabelFont());
                    RectangleAnchor anchor = crosshair.getLabelAnchor();
                    Point2D pt = calculateLabelPoint(line, anchor, crosshair.getLabelXOffset(), crosshair.getLabelYOffset());
                    float xx = (float) pt.getX();
                    float yy = (float) pt.getY();
                    TextAnchor alignPt = textAlignPtForLabelAnchorH(anchor);
                    Shape hotspot = TextUtils.calculateRotatedStringBounds(
                            label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);
                    if (!dataArea.contains(hotspot.getBounds2D())) {
                        anchor = flipAnchorV(anchor);
                        pt = calculateLabelPoint(line, anchor, crosshair.getLabelXOffset(), crosshair.getLabelYOffset());
                        xx = (float) pt.getX();
                        yy = (float) pt.getY();
                        alignPt = textAlignPtForLabelAnchorH(anchor);
                        hotspot = TextUtils.calculateRotatedStringBounds(
                               label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);
                    }

                    g2.setPaint(crosshair.getLabelBackgroundPaint());
                    g2.fill(hotspot);
                    if (crosshair.isLabelOutlineVisible()) {
                        g2.setPaint(crosshair.getLabelOutlinePaint());
                        g2.setStroke(crosshair.getLabelOutlineStroke());
                        g2.draw(hotspot);
                    }
                    g2.setPaint(crosshair.getLabelPaint());
                    TextUtils.drawAlignedString(label, g2, xx, yy, alignPt);
                    g2.setFont(savedFont);
                }
            }
            g2.setPaint(savedPaint);
            g2.setStroke(savedStroke);
        }
    }

    
    protected void drawVerticalCrosshair(Graphics2D g2, Rectangle2D dataArea,
            double x, Crosshair crosshair) {

        if (x >= dataArea.getMinX() && x <= dataArea.getMaxX()) {
            Line2D line = new Line2D.Double(x, dataArea.getMinY(), x,
                    dataArea.getMaxY());
            Paint savedPaint = g2.getPaint();
            Stroke savedStroke = g2.getStroke();
            g2.setPaint(crosshair.getPaint());
            g2.setStroke(crosshair.getStroke());
            g2.draw(line);
            if (crosshair.isLabelVisible()) {
                String label = crosshair.getLabelGenerator().generateLabel(
                        crosshair);
                if (label != null && !label.isEmpty()) {
                    Font savedFont = g2.getFont();
                    g2.setFont(crosshair.getLabelFont());
                    RectangleAnchor anchor = crosshair.getLabelAnchor();
                    Point2D pt = calculateLabelPoint(line, anchor, crosshair.getLabelXOffset(), crosshair.getLabelYOffset());
                    float xx = (float) pt.getX();
                    float yy = (float) pt.getY();
                    TextAnchor alignPt = textAlignPtForLabelAnchorV(anchor);
                    Shape hotspot = TextUtils.calculateRotatedStringBounds(
                            label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);
                    if (!dataArea.contains(hotspot.getBounds2D())) {
                        anchor = flipAnchorH(anchor);
                        pt = calculateLabelPoint(line, anchor, crosshair.getLabelXOffset(), crosshair.getLabelYOffset());
                        xx = (float) pt.getX();
                        yy = (float) pt.getY();
                        alignPt = textAlignPtForLabelAnchorV(anchor);
                        hotspot = TextUtils.calculateRotatedStringBounds(
                               label, g2, xx, yy, alignPt, 0.0, TextAnchor.CENTER);
                    }
                    g2.setPaint(crosshair.getLabelBackgroundPaint());
                    g2.fill(hotspot);
                    if (crosshair.isLabelOutlineVisible()) {
                        g2.setPaint(crosshair.getLabelOutlinePaint());
                        g2.setStroke(crosshair.getLabelOutlineStroke());
                        g2.draw(hotspot);
                    }
                    g2.setPaint(crosshair.getLabelPaint());
                    TextUtils.drawAlignedString(label, g2, xx, yy, alignPt);
                    g2.setFont(savedFont);
                }
            }
            g2.setPaint(savedPaint);
            g2.setStroke(savedStroke);
        }
    }

    
    private Point2D calculateLabelPoint(Line2D line, RectangleAnchor anchor,
            double deltaX, double deltaY) {
        double x, y;
        boolean left = (anchor == RectangleAnchor.BOTTOM_LEFT 
                || anchor == RectangleAnchor.LEFT 
                || anchor == RectangleAnchor.TOP_LEFT);
        boolean right = (anchor == RectangleAnchor.BOTTOM_RIGHT 
                || anchor == RectangleAnchor.RIGHT 
                || anchor == RectangleAnchor.TOP_RIGHT);
        boolean top = (anchor == RectangleAnchor.TOP_LEFT 
                || anchor == RectangleAnchor.TOP 
                || anchor == RectangleAnchor.TOP_RIGHT);
        boolean bottom = (anchor == RectangleAnchor.BOTTOM_LEFT
                || anchor == RectangleAnchor.BOTTOM
                || anchor == RectangleAnchor.BOTTOM_RIGHT);
        Rectangle rect = line.getBounds();
        
        // we expect the line to be vertical or horizontal
        if (line.getX1() == line.getX2()) {  // vertical
            x = line.getX1();
            y = (line.getY1() + line.getY2()) / 2.0;
            if (left) {
                x = x - deltaX;
            }
            if (right) {
                x = x + deltaX;
            }
            if (top) {
                y = Math.min(line.getY1(), line.getY2()) + deltaY;
            }
            if (bottom) {
                y = Math.max(line.getY1(), line.getY2()) - deltaY;
            }
        }
        else {  // horizontal
            x = (line.getX1() + line.getX2()) / 2.0;
            y = line.getY1();
            if (left) {
                x = Math.min(line.getX1(), line.getX2()) + deltaX;
            }
            if (right) {
                x = Math.max(line.getX1(), line.getX2()) - deltaX;
            }
            if (top) {
                y = y - deltaY;
            }
            if (bottom) {
                y = y + deltaY;
            }
        }
        return new Point2D.Double(x, y);
    }

    
    private TextAnchor textAlignPtForLabelAnchorV(RectangleAnchor anchor) {
        TextAnchor result = TextAnchor.CENTER;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = TextAnchor.TOP_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.TOP)) {
            result = TextAnchor.TOP_CENTER;
        }
        else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = TextAnchor.TOP_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.LEFT)) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.RIGHT)) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = TextAnchor.BOTTOM_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM)) {
            result = TextAnchor.BOTTOM_CENTER;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = TextAnchor.BOTTOM_LEFT;
        }
        return result;
    }

    
    private TextAnchor textAlignPtForLabelAnchorH(RectangleAnchor anchor) {
        TextAnchor result = TextAnchor.CENTER;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = TextAnchor.BOTTOM_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.TOP)) {
            result = TextAnchor.BOTTOM_CENTER;
        }
        else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = TextAnchor.BOTTOM_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.LEFT)) {
            result = TextAnchor.HALF_ASCENT_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.RIGHT)) {
            result = TextAnchor.HALF_ASCENT_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = TextAnchor.TOP_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM)) {
            result = TextAnchor.TOP_CENTER;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = TextAnchor.TOP_RIGHT;
        }
        return result;
    }

    private RectangleAnchor flipAnchorH(RectangleAnchor anchor) {
        RectangleAnchor result = anchor;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = RectangleAnchor.TOP_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = RectangleAnchor.TOP_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.LEFT)) {
            result = RectangleAnchor.RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.RIGHT)) {
            result = RectangleAnchor.LEFT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = RectangleAnchor.BOTTOM_LEFT;
        }
        return result;
    }

    private RectangleAnchor flipAnchorV(RectangleAnchor anchor) {
        RectangleAnchor result = anchor;
        if (anchor.equals(RectangleAnchor.TOP_LEFT)) {
            result = RectangleAnchor.BOTTOM_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.TOP_RIGHT)) {
            result = RectangleAnchor.BOTTOM_RIGHT;
        }
        else if (anchor.equals(RectangleAnchor.TOP)) {
            result = RectangleAnchor.BOTTOM;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM)) {
            result = RectangleAnchor.TOP;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_LEFT)) {
            result = RectangleAnchor.TOP_LEFT;
        }
        else if (anchor.equals(RectangleAnchor.BOTTOM_RIGHT)) {
            result = RectangleAnchor.TOP_RIGHT;
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CrosshairOverlay)) {
            return false;
        }
        CrosshairOverlay that = (CrosshairOverlay) obj;
        if (!this.xCrosshairs.equals(that.xCrosshairs)) {
            return false;
        }
        if (!this.yCrosshairs.equals(that.yCrosshairs)) {
            return false;
        }
        return true;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        CrosshairOverlay clone = (CrosshairOverlay) super.clone();
        clone.xCrosshairs = (List) CloneUtils.cloneList(this.xCrosshairs);
        clone.yCrosshairs = (List) CloneUtils.cloneList(this.yCrosshairs);
        return clone;
    }

}
