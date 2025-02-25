

package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.geom.Rectangle2D;
import java.text.AttributedString;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.util.AttrStringUtils;
import org.jfree.chart.internal.Args;
import org.jfree.data.Range;


public class LogAxis extends ValueAxis {

    
    private double base = 10.0;

    
    private double baseLog = Math.log(10.0);

    
    private String baseSymbol = null;
    
    
    private Format baseFormatter = new DecimalFormat("0");
    
    
    private double smallestValue = 1E-100;

    
    private NumberTickUnit tickUnit;

    
    private NumberFormat numberFormatOverride;

    
    public LogAxis() {
        this(null);
    }

    
    public LogAxis(String label) {
        super(label, new NumberTickUnitSource());
        setDefaultAutoRange(new Range(0.01, 1.0));
        this.tickUnit = new NumberTickUnit(1.0, new DecimalFormat("0.#"), 10);
    }

    
    public double getBase() {
        return this.base;
    }

    
    public void setBase(double base) {
        if (base <= 1.0) {
            throw new IllegalArgumentException("Requires 'base' > 1.0.");
        }
        this.base = base;
        this.baseLog = Math.log(base);
        fireChangeEvent();
    }

    
    public String getBaseSymbol() {
        return this.baseSymbol;
    }
    
    
    public void setBaseSymbol(String symbol) {
        this.baseSymbol = symbol;
        fireChangeEvent();
    }
    
    
    public Format getBaseFormatter() {
        return this.baseFormatter;
    }
    
    
    public void setBaseFormatter(Format formatter) {
        Args.nullNotPermitted(formatter, "formatter");
        this.baseFormatter = formatter;
        fireChangeEvent();
    }
    
    
    public double getSmallestValue() {
        return this.smallestValue;
    }

    
    public void setSmallestValue(double value) {
        if (value <= 0.0) {
            throw new IllegalArgumentException("Requires 'value' > 0.0.");
        }
        this.smallestValue = value;
        fireChangeEvent();
    }

    
    public NumberTickUnit getTickUnit() {
        return this.tickUnit;
    }

    
    public void setTickUnit(NumberTickUnit unit) {
        // defer argument checking...
        setTickUnit(unit, true, true);
    }

    
    public void setTickUnit(NumberTickUnit unit, boolean notify,
            boolean turnOffAutoSelect) {
        Args.nullNotPermitted(unit, "unit");
        this.tickUnit = unit;
        if (turnOffAutoSelect) {
            setAutoTickUnitSelection(false, false);
        }
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public NumberFormat getNumberFormatOverride() {
        return this.numberFormatOverride;
    }

    
    public void setNumberFormatOverride(NumberFormat formatter) {
        this.numberFormatOverride = formatter;
        fireChangeEvent();
    }

    
    public double calculateLog(double value) {
        return Math.log(value) / this.baseLog;
    }

    
    public double calculateValue(double log) {
        return Math.pow(this.base, log);
    }
    
    private double calculateValueNoINF(double log) {
        double result = calculateValue(log);
        if (Double.isInfinite(result)) {
            result = Double.MAX_VALUE;
        }
        if (result <= 0.0) {
            result = Double.MIN_VALUE;
        }
        return result;
    }

    
    @Override
    public double java2DToValue(double java2DValue, Rectangle2D area,
            RectangleEdge edge) {

        Range range = getRange();
        double axisMin = calculateLog(Math.max(this.smallestValue, 
                range.getLowerBound()));
        double axisMax = calculateLog(range.getUpperBound());

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getMaxX();
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            min = area.getMaxY();
            max = area.getY();
        }
        double log;
        if (isInverted()) {
            log = axisMax - (java2DValue - min) / (max - min)
                    * (axisMax - axisMin);
        } else {
            log = axisMin + (java2DValue - min) / (max - min)
                    * (axisMax - axisMin);
        }
        return calculateValue(log);
    }

    
    @Override
    public double valueToJava2D(double value, Rectangle2D area,
            RectangleEdge edge) {

        Range range = getRange();
        double axisMin = calculateLog(range.getLowerBound());
        double axisMax = calculateLog(range.getUpperBound());
        value = calculateLog(value);

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getMaxX();
        } else if (RectangleEdge.isLeftOrRight(edge)) {
            max = area.getMinY();
            min = area.getMaxY();
        }
        if (isInverted()) {
            return max
                   - ((value - axisMin) / (axisMax - axisMin)) * (max - min);
        } else {
            return min
                   + ((value - axisMin) / (axisMax - axisMin)) * (max - min);
        }
    }

    
    @Override
    public void configure() {
        if (isAutoRange()) {
            autoAdjustRange();
        }
    }

    
    @Override
    protected void autoAdjustRange() {
        Plot plot = getPlot();
        if (plot == null) {
            return;  // no plot, no data
        }

        if (plot instanceof ValueAxisPlot) {
            ValueAxisPlot vap = (ValueAxisPlot) plot;

            Range r = vap.getDataRange(this);
            if (r == null) {
                r = getDefaultAutoRange();
            }

            double upper = r.getUpperBound();
            double lower = Math.max(r.getLowerBound(), this.smallestValue);
            double range = upper - lower;

            // if fixed auto range, then derive lower bound...
            double fixedAutoRange = getFixedAutoRange();
            if (fixedAutoRange > 0.0) {
                lower = Math.max(upper - fixedAutoRange, this.smallestValue);
            }
            else {
                // ensure the autorange is at least <minRange> in size...
                double minRange = getAutoRangeMinimumSize();
                if (range < minRange) {
                    double expand = (minRange - range) / 2;
                    upper = upper + expand;
                    lower = lower - expand;
                }

                // apply the margins - these should apply to the exponent range
                double logUpper = calculateLog(upper);
                double logLower = calculateLog(lower);
                double logRange = logUpper - logLower;
                logUpper = logUpper + getUpperMargin() * logRange;
                logLower = logLower - getLowerMargin() * logRange;
                upper = calculateValueNoINF(logUpper);
                lower = calculateValueNoINF(logLower);
            }
            setRange(new Range(lower, upper), false, false);
        }

    }

    
    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge,
            PlotRenderingInfo plotState) {

        AxisState state;
        // if the axis is not visible, don't draw it...
        if (!isVisible()) {
            state = new AxisState(cursor);
            // even though the axis is not visible, we need ticks for the
            // gridlines...
            List ticks = refreshTicks(g2, state, dataArea, edge);
            state.setTicks(ticks);
            return state;
        }
        state = drawTickMarksAndLabels(g2, cursor, plotArea, dataArea, edge);
        if (getAttributedLabel() != null) {
            state = drawAttributedLabel(getAttributedLabel(), g2, plotArea, 
                    dataArea, edge, state);
            
        } else {
            state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);
        }
        createAndAddEntity(cursor, state, dataArea, edge, plotState);
        return state;
    }

    
    @Override
    public List refreshTicks(Graphics2D g2, AxisState state,
            Rectangle2D dataArea, RectangleEdge edge) {
        List result = new java.util.ArrayList();
        if (RectangleEdge.isTopOrBottom(edge)) {
            result = refreshTicksHorizontal(g2, dataArea, edge);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            result = refreshTicksVertical(g2, dataArea, edge);
        }
        return result;
    }
    
    
    protected List refreshTicksHorizontal(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        Range range = getRange();
        List ticks = new ArrayList();
        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);
        TextAnchor textAnchor;
        if (edge == RectangleEdge.TOP) {
            textAnchor = TextAnchor.BOTTOM_CENTER;
        }
        else {
            textAnchor = TextAnchor.TOP_CENTER;
        }

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }
        int minorTickCount = this.tickUnit.getMinorTickCount();
        double unit = getTickUnit().getSize();
        double index = Math.ceil(calculateLog(getRange().getLowerBound()) 
                / unit);
        double start = index * unit;
        double end = calculateLog(getUpperBound());
        double current = start;
        boolean hasTicks = (this.tickUnit.getSize() > 0.0)
                           && !Double.isInfinite(start);
        while (hasTicks && current <= end) {
            double v = calculateValueNoINF(current);
            if (range.contains(v)) {
                ticks.add(new LogTick(TickType.MAJOR, v, createTickLabel(v),
                        textAnchor));
            }
            // add minor ticks (for gridlines)
            double next = Math.pow(this.base, current
                    + this.tickUnit.getSize());
            for (int i = 1; i < minorTickCount; i++) {
                double minorV = v + i * ((next - v) / minorTickCount);
                if (range.contains(minorV)) {
                    ticks.add(new LogTick(TickType.MINOR, minorV, null,
                            textAnchor));
                }
            }
            current = current + this.tickUnit.getSize();
        }
        return ticks;
    }

    
    protected List refreshTicksVertical(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {

        Range range = getRange();
        List ticks = new ArrayList();
        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);
        TextAnchor textAnchor;
        if (edge == RectangleEdge.RIGHT) {
            textAnchor = TextAnchor.CENTER_LEFT;
        }
        else {
            textAnchor = TextAnchor.CENTER_RIGHT;
        }

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }
        int minorTickCount = this.tickUnit.getMinorTickCount();
        double unit = getTickUnit().getSize();
        double index = Math.ceil(calculateLog(getRange().getLowerBound()) 
                / unit);
        double start = index * unit;
        double end = calculateLog(getUpperBound());
        double current = start;
        boolean hasTicks = (this.tickUnit.getSize() > 0.0)
                           && !Double.isInfinite(start);
        while (hasTicks && current <= end) {
            double v = calculateValueNoINF(current);
            if (range.contains(v)) {
                ticks.add(new LogTick(TickType.MAJOR, v, createTickLabel(v),
                        textAnchor));
            }
            // add minor ticks (for gridlines)
            double next = Math.pow(this.base, current
                    + this.tickUnit.getSize());
            for (int i = 1; i < minorTickCount; i++) {
                double minorV = v + i * ((next - v) / minorTickCount);
                if (range.contains(minorV)) {
                    ticks.add(new LogTick(TickType.MINOR, minorV, null,
                            textAnchor));
                }
            }
            current = current + this.tickUnit.getSize();
        }
        return ticks;
    }

    
    protected void selectAutoTickUnit(Graphics2D g2, Rectangle2D dataArea,
            RectangleEdge edge) {
        if (RectangleEdge.isTopOrBottom(edge)) {
            selectHorizontalAutoTickUnit(g2, dataArea, edge);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            selectVerticalAutoTickUnit(g2, dataArea, edge);
        }
    }

    
    protected void selectHorizontalAutoTickUnit(Graphics2D g2,
            Rectangle2D dataArea, RectangleEdge edge) {

        // select a tick unit that is the next one bigger than the current
        // (log) range divided by 50
        Range range = getRange();
        double logAxisMin = calculateLog(Math.max(this.smallestValue, 
                range.getLowerBound()));
        double logAxisMax = calculateLog(range.getUpperBound());
        double size = (logAxisMax - logAxisMin) / 50;
        TickUnitSource tickUnits = getStandardTickUnits();
        TickUnit candidate = tickUnits.getCeilingTickUnit(size);
        TickUnit prevCandidate = candidate;
        boolean found = false;
        while (!found) {
        // while the tick labels overlap and there are more tick sizes available,
            // choose the next bigger label
            this.tickUnit = (NumberTickUnit) candidate;
            double tickLabelWidth = estimateMaximumTickLabelWidth(g2, 
                    candidate);
            // what is the available space for one unit?
            double candidateWidth = exponentLengthToJava2D(candidate.getSize(), 
                    dataArea, edge);
            if (tickLabelWidth < candidateWidth) {
                found = true;
            } else if (Double.isNaN(candidateWidth)) {
                candidate = prevCandidate;
                found = true;
            } else {
                prevCandidate = candidate;
                candidate = tickUnits.getLargerTickUnit(prevCandidate);
                if (candidate.equals(prevCandidate)) {
                    found = true;  // there are no more candidates
                }
            }
        } 
        setTickUnit((NumberTickUnit) candidate, false, false);
    }

    
    public double exponentLengthToJava2D(double length, Rectangle2D area,
                                RectangleEdge edge) {
        double one = valueToJava2D(calculateValueNoINF(1.0), area, edge);
        double l = valueToJava2D(calculateValueNoINF(length + 1.0), area, edge);
        return Math.abs(l - one);
    }

    
    protected void selectVerticalAutoTickUnit(Graphics2D g2, 
            Rectangle2D dataArea, RectangleEdge edge) {
        // select a tick unit that is the next one bigger than the current
        // (log) range divided by 50
        Range range = getRange();
        double logAxisMin = calculateLog(Math.max(this.smallestValue, 
                range.getLowerBound()));
        double logAxisMax = calculateLog(range.getUpperBound());
        double size = (logAxisMax - logAxisMin) / 50;
        TickUnitSource tickUnits = getStandardTickUnits();
        TickUnit candidate = tickUnits.getCeilingTickUnit(size);
        TickUnit prevCandidate = candidate;
        boolean found = false;
        while (!found) {
        // while the tick labels overlap and there are more tick sizes available,
            // choose the next bigger label
            this.tickUnit = (NumberTickUnit) candidate;
            double tickLabelHeight = estimateMaximumTickLabelHeight(g2);
            // what is the available space for one unit?
            double candidateHeight = exponentLengthToJava2D(candidate.getSize(), 
                    dataArea, edge);
            if (tickLabelHeight < candidateHeight) {
                found = true;
            } else if (Double.isNaN(candidateHeight)) {
                candidate = prevCandidate;
                found = true;
            } else {
                prevCandidate = candidate;
                candidate = tickUnits.getLargerTickUnit(prevCandidate);
                if (candidate.equals(prevCandidate)) {
                    found = true;  // there are no more candidates
                }
            }
        } 
        setTickUnit((NumberTickUnit) candidate, false, false);
    }

    
    protected AttributedString createTickLabel(double value) {
        if (this.numberFormatOverride != null) {
            String text = this.numberFormatOverride.format(value);
            AttributedString as = new AttributedString(text);
            as.addAttribute(TextAttribute.FONT, getTickLabelFont());
            return as;
        } else {
            String baseStr = this.baseSymbol;
            if (baseStr == null) {
                baseStr = this.baseFormatter.format(this.base);
            }
            double logy = calculateLog(value);
            String exponentStr = getTickUnit().valueToString(logy);
            AttributedString as = new AttributedString(baseStr + exponentStr);
            as.addAttributes(getTickLabelFont().getAttributes(), 0, (baseStr 
                    + exponentStr).length());
            as.addAttribute(TextAttribute.SUPERSCRIPT, 
                    TextAttribute.SUPERSCRIPT_SUPER, baseStr.length(), 
                    baseStr.length() + exponentStr.length());
            return as;
        }
    }

    
    protected double estimateMaximumTickLabelHeight(Graphics2D g2) {
        RectangleInsets tickLabelInsets = getTickLabelInsets();
        double result = tickLabelInsets.getTop() + tickLabelInsets.getBottom();

        Font tickLabelFont = getTickLabelFont();
        FontRenderContext frc = g2.getFontRenderContext();
        result += tickLabelFont.getLineMetrics("123", frc).getHeight();
        return result;
    }

    
    protected double estimateMaximumTickLabelWidth(Graphics2D g2, 
            TickUnit unit) {

        RectangleInsets tickLabelInsets = getTickLabelInsets();
        double result = tickLabelInsets.getLeft() + tickLabelInsets.getRight();

        if (isVerticalTickLabels()) {
            // all tick labels have the same width (equal to the height of the
            // font)...
            FontRenderContext frc = g2.getFontRenderContext();
            LineMetrics lm = getTickLabelFont().getLineMetrics("0", frc);
            result += lm.getHeight();
        }
        else {
            // look at lower and upper bounds...
            Range range = getRange();
            double lower = range.getLowerBound();
            double upper = range.getUpperBound();
            AttributedString lowerStr = createTickLabel(lower);
            AttributedString upperStr = createTickLabel(upper);
            double w1 = AttrStringUtils.getTextBounds(lowerStr, g2).getWidth();
            double w2 = AttrStringUtils.getTextBounds(upperStr, g2).getWidth();
            result += Math.max(w1, w2);
        }
        return result;
    }

    
    @Override
    public void zoomRange(double lowerPercent, double upperPercent) {
        Range range = getRange();
        double start = range.getLowerBound();
        double end = range.getUpperBound();
        double log1 = calculateLog(start);
        double log2 = calculateLog(end);
        double length = log2 - log1;
        Range adjusted;
        if (isInverted()) {
            double logA = log1 + length * (1 - upperPercent);
            double logB = log1 + length * (1 - lowerPercent);
            adjusted = new Range(calculateValueNoINF(logA), 
                    calculateValueNoINF(logB));
        }
        else {
            double logA = log1 + length * lowerPercent;
            double logB = log1 + length * upperPercent;
            adjusted = new Range(calculateValueNoINF(logA), 
                    calculateValueNoINF(logB));
        }
        setRange(adjusted);
    }

    
    @Override
    public void pan(double percent) {
        Range range = getRange();
        double lower = range.getLowerBound();
        double upper = range.getUpperBound();
        double log1 = calculateLog(lower);
        double log2 = calculateLog(upper);
        double length = log2 - log1;
        double adj = length * percent;
        log1 = log1 + adj;
        log2 = log2 + adj;
        setRange(calculateValueNoINF(log1), calculateValueNoINF(log2));
    }
    
    
    @Override
    public void resizeRange(double percent) {
        Range range = getRange();
        double logMin = calculateLog(range.getLowerBound());
        double logMax = calculateLog(range.getUpperBound());
        double centralValue = calculateValueNoINF((logMin + logMax) / 2.0);
        resizeRange(percent, centralValue);
    }

    @Override
    public void resizeRange(double percent, double anchorValue) {
        resizeRange2(percent, anchorValue);
    }

    
    @Override
    public void resizeRange2(double percent, double anchorValue) {
        if (percent > 0.0) {
            double logAnchorValue = calculateLog(anchorValue);
            Range range = getRange();
            double logAxisMin = calculateLog(range.getLowerBound());
            double logAxisMax = calculateLog(range.getUpperBound());

            double left = percent * (logAnchorValue - logAxisMin);
            double right = percent * (logAxisMax - logAnchorValue);
            
            double upperBound = calculateValueNoINF(logAnchorValue + right);
            Range adjusted = new Range(calculateValueNoINF(
                    logAnchorValue - left), upperBound);
            setRange(adjusted);
        }
        else {
            setAutoRange(true);
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LogAxis)) {
            return false;
        }
        LogAxis that = (LogAxis) obj;
        if (this.base != that.base) {
            return false;
        }
        if (!Objects.equals(this.baseSymbol, that.baseSymbol)) {
            return false;
        }
        if (!this.baseFormatter.equals(that.baseFormatter)) {
            return false;
        }
        if (this.smallestValue != that.smallestValue) {
            return false;
        }
        if (!Objects.equals(this.numberFormatOverride, that.numberFormatOverride)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int result = 193;
        long temp = Double.doubleToLongBits(this.base);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.smallestValue);
        result = 37 * result + (int) (temp ^ (temp >>> 32));
        if (this.numberFormatOverride != null) {
            result = 37 * result + this.numberFormatOverride.hashCode();
        }
        result = 37 * result + this.tickUnit.hashCode();
        return result;
    }

}
