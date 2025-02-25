

package org.jfree.chart.axis;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.data.Range;


public class SymbolAxis extends NumberAxis implements Serializable {

    
    private static final long serialVersionUID = 7216330468770619716L;

    
    public static final Paint DEFAULT_GRID_BAND_PAINT
            = new Color(232, 234, 232, 128);

    
    public static final Paint DEFAULT_GRID_BAND_ALTERNATE_PAINT
            = new Color(0, 0, 0, 0);  // transparent

    
    private List symbols;

    
    private boolean gridBandsVisible;

    
    private transient Paint gridBandPaint;

    
    private transient Paint gridBandAlternatePaint;

    
    public SymbolAxis(String label, String[] sv) {
        super(label);
        this.symbols = Arrays.asList(sv);
        this.gridBandsVisible = true;
        this.gridBandPaint = DEFAULT_GRID_BAND_PAINT;
        this.gridBandAlternatePaint = DEFAULT_GRID_BAND_ALTERNATE_PAINT;
        setAutoTickUnitSelection(false, false);
        setAutoRangeStickyZero(false);
    }

    
    public String[] getSymbols() {
        String[] result = new String[this.symbols.size()];
        result = (String[]) this.symbols.toArray(result);
        return result;
    }

    
    public boolean isGridBandsVisible() {
        return this.gridBandsVisible;
    }

    
    public void setGridBandsVisible(boolean flag) {
        this.gridBandsVisible = flag;
        fireChangeEvent();
    }

    
    public Paint getGridBandPaint() {
        return this.gridBandPaint;
    }

    
    public void setGridBandPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.gridBandPaint = paint;
        fireChangeEvent();
    }

    
    public Paint getGridBandAlternatePaint() {
        return this.gridBandAlternatePaint;
    }

    
    public void setGridBandAlternatePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.gridBandAlternatePaint = paint;
        fireChangeEvent();
    }

    
    @Override
    protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {
        throw new UnsupportedOperationException();
    }

    
    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge, 
            PlotRenderingInfo plotState) {

        AxisState info = new AxisState(cursor);
        if (isVisible()) {
            info = super.draw(g2, cursor, plotArea, dataArea, edge, plotState);
        }
        if (this.gridBandsVisible) {
            drawGridBands(g2, plotArea, dataArea, edge, info.getTicks());
        }
        return info;

    }

    
    protected void drawGridBands(Graphics2D g2, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge, List ticks) {
        Shape savedClip = g2.getClip();
        g2.clip(dataArea);
        if (RectangleEdge.isTopOrBottom(edge)) {
            drawGridBandsHorizontal(g2, plotArea, dataArea, true, ticks);
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            drawGridBandsVertical(g2, plotArea, dataArea, true, ticks);
        }
        g2.setClip(savedClip);
    }

    
    protected void drawGridBandsHorizontal(Graphics2D g2,
            Rectangle2D plotArea, Rectangle2D dataArea, 
            boolean firstGridBandIsDark, List ticks) {

        boolean currentGridBandIsDark = firstGridBandIsDark;
        double yy = dataArea.getY();
        double xx1, xx2;

        //gets the outline stroke width of the plot
        double outlineStrokeWidth = 1.0;
        Stroke outlineStroke = getPlot().getOutlineStroke();
        if (outlineStroke != null && outlineStroke instanceof BasicStroke) {
            outlineStrokeWidth = ((BasicStroke) outlineStroke).getLineWidth();
        }

        Iterator iterator = ticks.iterator();
        ValueTick tick;
        Rectangle2D band;
        while (iterator.hasNext()) {
            tick = (ValueTick) iterator.next();
            xx1 = valueToJava2D(tick.getValue() - 0.5d, dataArea,
                    RectangleEdge.BOTTOM);
            xx2 = valueToJava2D(tick.getValue() + 0.5d, dataArea,
                    RectangleEdge.BOTTOM);
            if (currentGridBandIsDark) {
                g2.setPaint(this.gridBandPaint);
            } else {
                g2.setPaint(this.gridBandAlternatePaint);
            }
            band = new Rectangle2D.Double(Math.min(xx1, xx2), 
                    yy + outlineStrokeWidth, Math.abs(xx2 - xx1), 
                    dataArea.getMaxY() - yy - outlineStrokeWidth);
            g2.fill(band);
            currentGridBandIsDark = !currentGridBandIsDark;
        }
    }

    
    protected void drawGridBandsVertical(Graphics2D g2, Rectangle2D plotArea,
            Rectangle2D dataArea, boolean firstGridBandIsDark, 
            List ticks) {

        boolean currentGridBandIsDark = firstGridBandIsDark;
        double xx = dataArea.getX();
        double yy1, yy2;

        //gets the outline stroke width of the plot
        double outlineStrokeWidth = 1.0;
        Stroke outlineStroke = getPlot().getOutlineStroke();
        if (outlineStroke != null && outlineStroke instanceof BasicStroke) {
            outlineStrokeWidth = ((BasicStroke) outlineStroke).getLineWidth();
        }

        Iterator iterator = ticks.iterator();
        ValueTick tick;
        Rectangle2D band;
        while (iterator.hasNext()) {
            tick = (ValueTick) iterator.next();
            yy1 = valueToJava2D(tick.getValue() + 0.5d, dataArea,
                    RectangleEdge.LEFT);
            yy2 = valueToJava2D(tick.getValue() - 0.5d, dataArea,
                    RectangleEdge.LEFT);
            if (currentGridBandIsDark) {
                g2.setPaint(this.gridBandPaint);
            } else {
                g2.setPaint(this.gridBandAlternatePaint);
            }
            band = new Rectangle2D.Double(xx + outlineStrokeWidth, 
                    Math.min(yy1, yy2), dataArea.getMaxX() - xx 
                    - outlineStrokeWidth, Math.abs(yy2 - yy1));
            g2.fill(band);
            currentGridBandIsDark = !currentGridBandIsDark;
        }
    }

    
    @Override
    protected void autoAdjustRange() {
        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data
        }

        if (plot instanceof ValueAxisPlot) {

            // ensure that all the symbols are displayed
            double upper = this.symbols.size() - 1;
            double lower = 0;
            double range = upper - lower;

            // ensure the autorange is at least <minRange> in size...
            double minRange = getAutoRangeMinimumSize();
            if (range < minRange) {
                upper = (upper + lower + minRange) / 2;
                lower = (upper + lower - minRange) / 2;
            }

            // this ensure that the grid bands will be displayed correctly.
            double upperMargin = 0.5;
            double lowerMargin = 0.5;

            if (getAutoRangeIncludesZero()) {
                if (getAutoRangeStickyZero()) {
                    if (upper <= 0.0) {
                        upper = 0.0;
                    } else {
                        upper = upper + upperMargin;
                    }
                    if (lower >= 0.0) {
                        lower = 0.0;
                    } else {
                        lower = lower - lowerMargin;
                    }
                } else {
                    upper = Math.max(0.0, upper + upperMargin);
                    lower = Math.min(0.0, lower - lowerMargin);
                }
            } else {
                if (getAutoRangeStickyZero()) {
                    if (upper <= 0.0) {
                        upper = Math.min(0.0, upper + upperMargin);
                    } else {
                        upper = upper + upperMargin * range;
                    }
                    if (lower >= 0.0) {
                        lower = Math.max(0.0, lower - lowerMargin);
                    } else {
                        lower = lower - lowerMargin;
                    }
                } else {
                    upper = upper + upperMargin;
                    lower = lower - lowerMargin;
                }
            }
            setRange(new Range(lower, upper), false, false);
        }
    }

    
    @Override
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        List ticks = null;
        if (RectangleEdge.isTopOrBottom(edge)) {
            ticks = refreshTicksHorizontal(g2, dataArea, edge);
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            ticks = refreshTicksVertical(g2, dataArea, edge);
        }
        return ticks;
    }

    
    @Override
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        double size = getTickUnit().getSize();
        int count = calculateVisibleTickCount();
        double lowestTickValue = calculateLowestVisibleTickValue();

        double previousDrawnTickLabelPos = 0.0;
        double previousDrawnTickLabelLength = 0.0;

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                double xx = valueToJava2D(currentTickValue, dataArea, edge);
                String tickLabel;
                NumberFormat formatter = getNumberFormatOverride();
                if (formatter != null) {
                    tickLabel = formatter.format(currentTickValue);
                }
                else {
                    tickLabel = valueToString(currentTickValue);
                }

                // avoid to draw overlapping tick labels
                Rectangle2D bounds = TextUtils.getTextBounds(tickLabel, g2,
                        g2.getFontMetrics());
                double tickLabelLength = isVerticalTickLabels()
                        ? bounds.getHeight() : bounds.getWidth();
                boolean tickLabelsOverlapping = false;
                if (i > 0) {
                    double avgTickLabelLength = (previousDrawnTickLabelLength
                            + tickLabelLength) / 2.0;
                    if (Math.abs(xx - previousDrawnTickLabelPos)
                            < avgTickLabelLength) {
                        tickLabelsOverlapping = true;
                    }
                }
                if (tickLabelsOverlapping) {
                    tickLabel = ""; // don't draw this tick label
                }
                else {
                    // remember these values for next comparison
                    previousDrawnTickLabelPos = xx;
                    previousDrawnTickLabelLength = tickLabelLength;
                }

                TextAnchor anchor;
                TextAnchor rotationAnchor;
                double angle = 0.0;
                if (isVerticalTickLabels()) {
                    anchor = TextAnchor.CENTER_RIGHT;
                    rotationAnchor = TextAnchor.CENTER_RIGHT;
                    if (edge == RectangleEdge.TOP) {
                        angle = Math.PI / 2.0;
                    }
                    else {
                        angle = -Math.PI / 2.0;
                    }
                }
                else {
                    if (edge == RectangleEdge.TOP) {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    }
                    else {
                        anchor = TextAnchor.TOP_CENTER;
                        rotationAnchor = TextAnchor.TOP_CENTER;
                    }
                }
                Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, 
                        rotationAnchor, angle);
                ticks.add(tick);
            }
        }
        return ticks;

    }

    
    @Override
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        List ticks = new java.util.ArrayList();

        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        double size = getTickUnit().getSize();
        int count = calculateVisibleTickCount();
        double lowestTickValue = calculateLowestVisibleTickValue();

        double previousDrawnTickLabelPos = 0.0;
        double previousDrawnTickLabelLength = 0.0;

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                double yy = valueToJava2D(currentTickValue, dataArea, edge);
                String tickLabel;
                NumberFormat formatter = getNumberFormatOverride();
                if (formatter != null) {
                    tickLabel = formatter.format(currentTickValue);
                }
                else {
                    tickLabel = valueToString(currentTickValue);
                }

                // avoid to draw overlapping tick labels
                Rectangle2D bounds = TextUtils.getTextBounds(tickLabel, g2,
                        g2.getFontMetrics());
                double tickLabelLength = isVerticalTickLabels()
                    ? bounds.getWidth() : bounds.getHeight();
                boolean tickLabelsOverlapping = false;
                if (i > 0) {
                    double avgTickLabelLength = (previousDrawnTickLabelLength
                            + tickLabelLength) / 2.0;
                    if (Math.abs(yy - previousDrawnTickLabelPos)
                            < avgTickLabelLength) {
                        tickLabelsOverlapping = true;
                    }
                }
                if (tickLabelsOverlapping) {
                    tickLabel = ""; // don't draw this tick label
                }
                else {
                    // remember these values for next comparison
                    previousDrawnTickLabelPos = yy;
                    previousDrawnTickLabelLength = tickLabelLength;
                }

                TextAnchor anchor;
                TextAnchor rotationAnchor;
                double angle = 0.0;
                if (isVerticalTickLabels()) {
                    anchor = TextAnchor.BOTTOM_CENTER;
                    rotationAnchor = TextAnchor.BOTTOM_CENTER;
                    if (edge == RectangleEdge.LEFT) {
                        angle = -Math.PI / 2.0;
                    }
                    else {
                        angle = Math.PI / 2.0;
                    }
                }
                else {
                    if (edge == RectangleEdge.LEFT) {
                        anchor = TextAnchor.CENTER_RIGHT;
                        rotationAnchor = TextAnchor.CENTER_RIGHT;
                    }
                    else {
                        anchor = TextAnchor.CENTER_LEFT;
                        rotationAnchor = TextAnchor.CENTER_LEFT;
                    }
                }
                Tick tick = new NumberTick(currentTickValue, tickLabel, anchor, 
                        rotationAnchor, angle);
                ticks.add(tick);
            }
        }
        return ticks;

    }

    
    public String valueToString(double value) {
        String strToReturn;
        try {
            strToReturn = (String) this.symbols.get((int) value);
        }
        catch (IndexOutOfBoundsException  ex) {
            strToReturn = "";
        }
        return strToReturn;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SymbolAxis)) {
            return false;
        }
        SymbolAxis that = (SymbolAxis) obj;
        if (!this.symbols.equals(that.symbols)) {
            return false;
        }
        if (this.gridBandsVisible != that.gridBandsVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.gridBandPaint, that.gridBandPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.gridBandAlternatePaint,
                that.gridBandAlternatePaint)) {
            return false;
        }
        return super.equals(obj);
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.gridBandPaint, stream);
        SerialUtils.writePaint(this.gridBandAlternatePaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.gridBandPaint = SerialUtils.readPaint(stream);
        this.gridBandAlternatePaint = SerialUtils.readPaint(stream);
    }

}
