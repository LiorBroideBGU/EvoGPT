

package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.ValueAxisPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.Args;
import org.jfree.data.Range;
import org.jfree.data.RangeType;


public class NumberAxis extends ValueAxis implements Cloneable, Serializable {

    
    private static final long serialVersionUID = 2805933088476185789L;

    
    public static final boolean DEFAULT_AUTO_RANGE_INCLUDES_ZERO = true;

    
    public static final boolean DEFAULT_AUTO_RANGE_STICKY_ZERO = true;

    
    public static final NumberTickUnit DEFAULT_TICK_UNIT = new NumberTickUnit(
            1.0, new DecimalFormat("0"));

    
    public static final boolean DEFAULT_VERTICAL_TICK_LABELS = false;

    
    private RangeType rangeType;

    
    private boolean autoRangeIncludesZero;

    
    private boolean autoRangeStickyZero;

    
    private NumberTickUnit tickUnit;

    
    private NumberFormat numberFormatOverride;

    
    private MarkerAxisBand markerBand;

    
    public NumberAxis() {
        this(null);
    }

    
    public NumberAxis(String label) {
        super(label, NumberAxis.createStandardTickUnits());
        this.rangeType = RangeType.FULL;
        this.autoRangeIncludesZero = DEFAULT_AUTO_RANGE_INCLUDES_ZERO;
        this.autoRangeStickyZero = DEFAULT_AUTO_RANGE_STICKY_ZERO;
        this.tickUnit = DEFAULT_TICK_UNIT;
        this.numberFormatOverride = null;
        this.markerBand = null;
    }

    
    public RangeType getRangeType() {
        return this.rangeType;
    }

    
    public void setRangeType(RangeType rangeType) {
        Args.nullNotPermitted(rangeType, "rangeType");
        this.rangeType = rangeType;
        notifyListeners(new AxisChangeEvent(this));
    }

    
    public boolean getAutoRangeIncludesZero() {
        return this.autoRangeIncludesZero;
    }

    
    public void setAutoRangeIncludesZero(boolean flag) {
        if (this.autoRangeIncludesZero != flag) {
            this.autoRangeIncludesZero = flag;
            if (isAutoRange()) {
                autoAdjustRange();
            }
            notifyListeners(new AxisChangeEvent(this));
        }
    }

    
    public boolean getAutoRangeStickyZero() {
        return this.autoRangeStickyZero;
    }

    
    public void setAutoRangeStickyZero(boolean flag) {
        if (this.autoRangeStickyZero != flag) {
            this.autoRangeStickyZero = flag;
            if (isAutoRange()) {
                autoAdjustRange();
            }
            notifyListeners(new AxisChangeEvent(this));
        }
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
            notifyListeners(new AxisChangeEvent(this));
        }

    }

    
    public NumberFormat getNumberFormatOverride() {
        return this.numberFormatOverride;
    }

    
    public void setNumberFormatOverride(NumberFormat formatter) {
        this.numberFormatOverride = formatter;
        notifyListeners(new AxisChangeEvent(this));
    }

    
    public MarkerAxisBand getMarkerBand() {
        return this.markerBand;
    }

    
    public void setMarkerBand(MarkerAxisBand band) {
        this.markerBand = band;
        notifyListeners(new AxisChangeEvent(this));
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
            double lower = r.getLowerBound();
            if (this.rangeType == RangeType.POSITIVE) {
                lower = Math.max(0.0, lower);
                upper = Math.max(0.0, upper);
            }
            else if (this.rangeType == RangeType.NEGATIVE) {
                lower = Math.min(0.0, lower);
                upper = Math.min(0.0, upper);
            }

            if (getAutoRangeIncludesZero()) {
                lower = Math.min(lower, 0.0);
                upper = Math.max(upper, 0.0);
            }
            double range = upper - lower;

            // if fixed auto range, then derive lower bound...
            double fixedAutoRange = getFixedAutoRange();
            if (fixedAutoRange > 0.0) {
                lower = upper - fixedAutoRange;
            }
            else {
                // ensure the autorange is at least <minRange> in size...
                double minRange = getAutoRangeMinimumSize();
                if (range < minRange) {
                    double expand = (minRange - range) / 2;
                    upper = upper + expand;
                    lower = lower - expand;
                    if (lower == upper) { // see bug report 1549218
                        double adjust = Math.abs(lower) / 10.0;
                        lower = lower - adjust;
                        upper = upper + adjust;
                    }
                    if (this.rangeType == RangeType.POSITIVE) {
                        if (lower < 0.0) {
                            upper = upper - lower;
                            lower = 0.0;
                        }
                    }
                    else if (this.rangeType == RangeType.NEGATIVE) {
                        if (upper > 0.0) {
                            lower = lower - upper;
                            upper = 0.0;
                        }
                    }
                }

                if (getAutoRangeStickyZero()) {
                    if (upper <= 0.0) {
                        upper = Math.min(0.0, upper + getUpperMargin() * range);
                    }
                    else {
                        upper = upper + getUpperMargin() * range;
                    }
                    if (lower >= 0.0) {
                        lower = Math.max(0.0, lower - getLowerMargin() * range);
                    }
                    else {
                        lower = lower - getLowerMargin() * range;
                    }
                }
                else {
                    upper = upper + getUpperMargin() * range;
                    lower = lower - getLowerMargin() * range;
                }
            }

            setRange(new Range(lower, upper), false, false);
        }

    }

    
    @Override
    public double valueToJava2D(double value, Rectangle2D area,
            RectangleEdge edge) {

        Range range = getRange();
        double axisMin = range.getLowerBound();
        double axisMax = range.getUpperBound();

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            max = area.getMinY();
            min = area.getMaxY();
        }
        if (isInverted()) {
            return max
                   - ((value - axisMin) / (axisMax - axisMin)) * (max - min);
        }
        else {
            return min
                   + ((value - axisMin) / (axisMax - axisMin)) * (max - min);
        }

    }

    
    @Override
    public double java2DToValue(double java2DValue, Rectangle2D area,
            RectangleEdge edge) {

        Range range = getRange();
        double axisMin = range.getLowerBound();
        double axisMax = range.getUpperBound();

        double min = 0.0;
        double max = 0.0;
        if (RectangleEdge.isTopOrBottom(edge)) {
            min = area.getX();
            max = area.getMaxX();
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            min = area.getMaxY();
            max = area.getY();
        }
        if (isInverted()) {
            return axisMax
                   - (java2DValue - min) / (max - min) * (axisMax - axisMin);
        }
        else {
            return axisMin
                   + (java2DValue - min) / (max - min) * (axisMax - axisMin);
        }

    }

    
    protected double calculateLowestVisibleTickValue() {
        double unit = getTickUnit().getSize();
        double index = Math.ceil(getRange().getLowerBound() / unit);
        return index * unit;
    }

    
    protected double calculateHighestVisibleTickValue() {
        double unit = getTickUnit().getSize();
        double index = Math.floor(getRange().getUpperBound() / unit);
        return index * unit;
    }

    
    protected int calculateVisibleTickCount() {
        double unit = getTickUnit().getSize();
        Range range = getRange();
        return (int) (Math.floor(range.getUpperBound() / unit)
                      - Math.ceil(range.getLowerBound() / unit) + 1);
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

        // draw the tick marks and labels...
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

    
    public static TickUnitSource createStandardTickUnits() {
        return new NumberTickUnitSource();
    }

    
    public static TickUnitSource createIntegerTickUnits() {
        return new NumberTickUnitSource(true);
    }

    
    public static TickUnitSource createStandardTickUnits(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        return new NumberTickUnitSource(false, numberFormat);
    }

    
    public static TickUnitSource createIntegerTickUnits(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);
        return new NumberTickUnitSource(true, numberFormat);
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
            FontMetrics fm = g2.getFontMetrics(getTickLabelFont());
            Range range = getRange();
            double lower = range.getLowerBound();
            double upper = range.getUpperBound();
            String lowerStr, upperStr;
            NumberFormat formatter = getNumberFormatOverride();
            if (formatter != null) {
                lowerStr = formatter.format(lower);
                upperStr = formatter.format(upper);
            }
            else {
                lowerStr = unit.valueToString(lower);
                upperStr = unit.valueToString(upper);
            }
            double w1 = fm.stringWidth(lowerStr);
            double w2 = fm.stringWidth(upperStr);
            result += Math.max(w1, w2);
        }

        return result;

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

        TickUnit unit = getTickUnit();
        TickUnitSource tickUnitSource = getStandardTickUnits();
 
        // we should start with the current tick unit if it gives a count in 
        // the range 3 to 40 otherwise estimate one that will give a count <= 10
        double length = getRange().getLength();
        int count = (int) (length / unit.getSize());
        if (count < 3 || count > 40) {
            unit = tickUnitSource.getCeilingTickUnit(length / 10);
        }

        // now consider the label size relative to the width of the tick unit
        // and make a guess at the ideal size
        TickUnit unit1 = tickUnitSource.getCeilingTickUnit(unit);
        double tickLabelWidth = estimateMaximumTickLabelWidth(g2, unit1);
        double unit1Width = lengthToJava2D(unit1.getSize(), dataArea, edge);
        NumberTickUnit unit2 = (NumberTickUnit) unit1;
        double guess = (tickLabelWidth / unit1Width) * unit1.getSize();

        // due to limitations of double precision, when you zoom very far into
        // a chart, eventually the visible axis range will get reported as 
        // having length 0, and then 'guess' above will be infinite ... in that 
        // case we'll just stick with the tick unit we have, it's better than 
        // throwing an exception 
        // https://github.com/jfree/jfreechart/issues/64
        if (Double.isFinite(guess)) {
            unit2 = (NumberTickUnit) tickUnitSource.getCeilingTickUnit(guess);
            double unit2Width = lengthToJava2D(unit2.getSize(), dataArea, edge);
            tickLabelWidth = estimateMaximumTickLabelWidth(g2, unit2);
            if (tickLabelWidth > unit2Width) {
                unit2 = (NumberTickUnit) tickUnitSource.getLargerTickUnit(unit2);
            }
        }
        setTickUnit(unit2, false, false);
    }

    
    protected void selectVerticalAutoTickUnit(Graphics2D g2, 
            Rectangle2D dataArea, RectangleEdge edge) {

        double tickLabelHeight = estimateMaximumTickLabelHeight(g2);

        // start with the current tick unit...
        TickUnitSource tickUnits = getStandardTickUnits();
        TickUnit unit1 = tickUnits.getCeilingTickUnit(getTickUnit());
        double unitHeight = lengthToJava2D(unit1.getSize(), dataArea, edge);
        double guess;
        if (unitHeight > 0) { // then extrapolate...
            guess = (tickLabelHeight / unitHeight) * unit1.getSize();
        } else { 
            guess = getRange().getLength() / 20.0;
        }
        NumberTickUnit unit2 = (NumberTickUnit) tickUnits.getCeilingTickUnit(
                guess);
        double unit2Height = lengthToJava2D(unit2.getSize(), dataArea, edge);

        tickLabelHeight = estimateMaximumTickLabelHeight(g2);
        if (tickLabelHeight > unit2Height) {
            unit2 = (NumberTickUnit) tickUnits.getLargerTickUnit(unit2);
        }
        setTickUnit(unit2, false, false);

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

    
    protected List refreshTicksHorizontal(Graphics2D g2,
            Rectangle2D dataArea, RectangleEdge edge) {

        List result = new java.util.ArrayList();

        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);

        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }

        TickUnit tu = getTickUnit();
        double size = tu.getSize();
        int count = calculateVisibleTickCount();
        double lowestTickValue = calculateLowestVisibleTickValue();

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            int minorTickSpaces = getMinorTickCount();
            if (minorTickSpaces <= 0) {
                minorTickSpaces = tu.getMinorTickCount();
            }
            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
                double minorTickValue = lowestTickValue 
                        - size * minorTick / minorTickSpaces;
                if (getRange().contains(minorTickValue)) {
                    result.add(new NumberTick(TickType.MINOR, minorTickValue,
                            "", TextAnchor.TOP_CENTER, TextAnchor.CENTER,
                            0.0));
                }
            }
            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                String tickLabel;
                NumberFormat formatter = getNumberFormatOverride();
                if (formatter != null) {
                    tickLabel = formatter.format(currentTickValue);
                }
                else {
                    tickLabel = getTickUnit().valueToString(currentTickValue);
                }
                TextAnchor anchor, rotationAnchor;
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

                Tick tick = new NumberTick(currentTickValue,
                        tickLabel, anchor, rotationAnchor, angle);
                result.add(tick);
                double nextTickValue = lowestTickValue + ((i + 1) * size);
                for (int minorTick = 1; minorTick < minorTickSpaces;
                        minorTick++) {
                    double minorTickValue = currentTickValue
                            + (nextTickValue - currentTickValue)
                            * minorTick / minorTickSpaces;
                    if (getRange().contains(minorTickValue)) {
                        result.add(new NumberTick(TickType.MINOR,
                                minorTickValue, "", TextAnchor.TOP_CENTER,
                                TextAnchor.CENTER, 0.0));
                    }
                }
            }
        }
        return result;

    }

    
    protected List refreshTicksVertical(Graphics2D g2,
            Rectangle2D dataArea, RectangleEdge edge) {

        List result = new java.util.ArrayList();
        result.clear();

        Font tickLabelFont = getTickLabelFont();
        g2.setFont(tickLabelFont);
        if (isAutoTickUnitSelection()) {
            selectAutoTickUnit(g2, dataArea, edge);
        }

        TickUnit tu = getTickUnit();
        double size = tu.getSize();
        int count = calculateVisibleTickCount();
        double lowestTickValue = calculateLowestVisibleTickValue();

        if (count <= ValueAxis.MAXIMUM_TICK_COUNT) {
            int minorTickSpaces = getMinorTickCount();
            if (minorTickSpaces <= 0) {
                minorTickSpaces = tu.getMinorTickCount();
            }
            for (int minorTick = 1; minorTick < minorTickSpaces; minorTick++) {
                double minorTickValue = lowestTickValue
                        - size * minorTick / minorTickSpaces;
                if (getRange().contains(minorTickValue)) {
                    result.add(new NumberTick(TickType.MINOR, minorTickValue,
                            "", TextAnchor.TOP_CENTER, TextAnchor.CENTER,
                            0.0));
                }
            }

            for (int i = 0; i < count; i++) {
                double currentTickValue = lowestTickValue + (i * size);
                String tickLabel;
                NumberFormat formatter = getNumberFormatOverride();
                if (formatter != null) {
                    tickLabel = formatter.format(currentTickValue);
                }
                else {
                    tickLabel = getTickUnit().valueToString(currentTickValue);
                }

                TextAnchor anchor;
                TextAnchor rotationAnchor;
                double angle = 0.0;
                if (isVerticalTickLabels()) {
                    if (edge == RectangleEdge.LEFT) {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
                        angle = -Math.PI / 2.0;
                    }
                    else {
                        anchor = TextAnchor.BOTTOM_CENTER;
                        rotationAnchor = TextAnchor.BOTTOM_CENTER;
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
                result.add(tick);

                double nextTickValue = lowestTickValue + ((i + 1) * size);
                for (int minorTick = 1; minorTick < minorTickSpaces;
                        minorTick++) {
                    double minorTickValue = currentTickValue
                            + (nextTickValue - currentTickValue)
                            * minorTick / minorTickSpaces;
                    if (getRange().contains(minorTickValue)) {
                        result.add(new NumberTick(TickType.MINOR,
                                minorTickValue, "", TextAnchor.TOP_CENTER,
                                TextAnchor.CENTER, 0.0));
                    }
                }
            }
        }
        return result;

    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        NumberAxis clone = (NumberAxis) super.clone();
        if (this.numberFormatOverride != null) {
            clone.numberFormatOverride
                = (NumberFormat) this.numberFormatOverride.clone();
        }
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof NumberAxis)) {
            return false;
        }
        NumberAxis that = (NumberAxis) obj;
        if (this.autoRangeIncludesZero != that.autoRangeIncludesZero) {
            return false;
        }
        if (this.autoRangeStickyZero != that.autoRangeStickyZero) {
            return false;
        }
        if (!Objects.equals(this.tickUnit, that.tickUnit)) {
            return false;
        }
        if (!Objects.equals(this.numberFormatOverride, that.numberFormatOverride)) {
            return false;
        }
        if (!this.rangeType.equals(that.rangeType)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

}
