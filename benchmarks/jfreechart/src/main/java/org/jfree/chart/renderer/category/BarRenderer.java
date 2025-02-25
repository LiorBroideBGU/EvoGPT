

package org.jfree.chart.renderer.category;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.data.KeyedValues2DItemKey;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;


public class BarRenderer extends AbstractCategoryItemRenderer
        implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 6000649414965887481L;

    
    public static final double DEFAULT_ITEM_MARGIN = 0.20;

    
    public static final double BAR_OUTLINE_WIDTH_THRESHOLD = 3.0;

    
    private static BarPainter defaultBarPainter = new GradientBarPainter();

    
    public static BarPainter getDefaultBarPainter() {
        return BarRenderer.defaultBarPainter;
    }

    
    public static void setDefaultBarPainter(BarPainter painter) {
        Args.nullNotPermitted(painter, "painter");
        BarRenderer.defaultBarPainter = painter;
    }

    
    private static boolean defaultShadowsVisible = true;

    
    public static boolean getDefaultShadowsVisible() {
        return BarRenderer.defaultShadowsVisible;
    }

    
    public static void setDefaultShadowsVisible(boolean visible) {
        BarRenderer.defaultShadowsVisible = visible;
    }

    
    private double itemMargin;

    
    private boolean drawBarOutline;

    
    private double maximumBarWidth;

    
    private double minimumBarLength;

    
    private GradientPaintTransformer gradientPaintTransformer;

    
    private ItemLabelPosition positiveItemLabelPositionFallback;

    
    private ItemLabelPosition negativeItemLabelPositionFallback;

    
    private double upperClip;
    // TODO:  this needs to move into the renderer state

    
    private double lowerClip;
    // TODO:  this needs to move into the renderer state

    
    private double base;

    
    private boolean includeBaseInRange;

    
    private BarPainter barPainter;

    
    private boolean shadowsVisible;

    
    private transient Paint shadowPaint;

    
    private double shadowXOffset;

    
    private double shadowYOffset;

    
    public BarRenderer() {
        super();
        this.base = 0.0;
        this.includeBaseInRange = true;
        this.itemMargin = DEFAULT_ITEM_MARGIN;
        this.drawBarOutline = false;
        this.maximumBarWidth = 1.0;
            // 100 percent, so it will not apply unless changed
        this.positiveItemLabelPositionFallback = null;
        this.negativeItemLabelPositionFallback = null;
        this.gradientPaintTransformer = new StandardGradientPaintTransformer();
        this.minimumBarLength = 0.0;
        setDefaultLegendShape(new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0));
        this.barPainter = getDefaultBarPainter();
        this.shadowsVisible = getDefaultShadowsVisible();
        this.shadowPaint = Color.GRAY;
        this.shadowXOffset = 4.0;
        this.shadowYOffset = 4.0;
    }

    
    public double getBase() {
        return this.base;
    }

    
    public void setBase(double base) {
        this.base = base;
        fireChangeEvent();
    }

    
    public double getItemMargin() {
        return this.itemMargin;
    }

    
    public void setItemMargin(double percent) {
        this.itemMargin = percent;
        fireChangeEvent();
    }

    
    public boolean isDrawBarOutline() {
        return this.drawBarOutline;
    }

    
    public void setDrawBarOutline(boolean draw) {
        this.drawBarOutline = draw;
        fireChangeEvent();
    }

    
    public double getMaximumBarWidth() {
        return this.maximumBarWidth;
    }

    
    public void setMaximumBarWidth(double percent) {
        this.maximumBarWidth = percent;
        fireChangeEvent();
    }

    
    public double getMinimumBarLength() {
        return this.minimumBarLength;
    }

    
    public void setMinimumBarLength(double min) {
        if (min < 0.0) {
            throw new IllegalArgumentException("Requires 'min' >= 0.0");
        }
        this.minimumBarLength = min;
        fireChangeEvent();
    }

    
    public GradientPaintTransformer getGradientPaintTransformer() {
        return this.gradientPaintTransformer;
    }

    
    public void setGradientPaintTransformer(
            GradientPaintTransformer transformer) {
        this.gradientPaintTransformer = transformer;
        fireChangeEvent();
    }

    
    public ItemLabelPosition getPositiveItemLabelPositionFallback() {
        return this.positiveItemLabelPositionFallback;
    }

    
    public void setPositiveItemLabelPositionFallback(
            ItemLabelPosition position) {
        this.positiveItemLabelPositionFallback = position;
        fireChangeEvent();
    }

    
    public ItemLabelPosition getNegativeItemLabelPositionFallback() {
        return this.negativeItemLabelPositionFallback;
    }

    
    public void setNegativeItemLabelPositionFallback(
            ItemLabelPosition position) {
        this.negativeItemLabelPositionFallback = position;
        fireChangeEvent();
    }

    
    public boolean getIncludeBaseInRange() {
        return this.includeBaseInRange;
    }

    
    public void setIncludeBaseInRange(boolean include) {
        if (this.includeBaseInRange != include) {
            this.includeBaseInRange = include;
            fireChangeEvent();
        }
    }

    
    public BarPainter getBarPainter() {
        return this.barPainter;
    }

    
    public void setBarPainter(BarPainter painter) {
        Args.nullNotPermitted(painter, "painter");
        this.barPainter = painter;
        fireChangeEvent();
    }

    
    public boolean getShadowsVisible() {
        return this.shadowsVisible;
    }

    
    public void setShadowVisible(boolean visible) {
        this.shadowsVisible = visible;
        fireChangeEvent();
    }

    
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }

    
    public void setShadowPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.shadowPaint = paint;
        fireChangeEvent();
    }

    
    public double getShadowXOffset() {
        return this.shadowXOffset;
    }

    
    public void setShadowXOffset(double offset) {
        this.shadowXOffset = offset;
        fireChangeEvent();
    }

    
    public double getShadowYOffset() {
        return this.shadowYOffset;
    }

    
    public void setShadowYOffset(double offset) {
        this.shadowYOffset = offset;
        fireChangeEvent();
    }

    
    public double getLowerClip() {
        // TODO:  this attribute should be transferred to the renderer state.
        return this.lowerClip;
    }

    
    public double getUpperClip() {
        // TODO:  this attribute should be transferred to the renderer state.
        return this.upperClip;
    }

    
    @Override
    public CategoryItemRendererState initialise(Graphics2D g2, 
            Rectangle2D dataArea, CategoryPlot plot, int rendererIndex,
            PlotRenderingInfo info) {

        CategoryItemRendererState state = super.initialise(g2, dataArea, plot,
                rendererIndex, info);

        // get the clipping values...
        ValueAxis rangeAxis = plot.getRangeAxisForDataset(rendererIndex);
        this.lowerClip = rangeAxis.getRange().getLowerBound();
        this.upperClip = rangeAxis.getRange().getUpperBound();

        // calculate the bar width
        calculateBarWidth(plot, dataArea, rendererIndex, state);

        return state;

    }

    
    protected void calculateBarWidth(CategoryPlot plot,
                                     Rectangle2D dataArea,
                                     int rendererIndex,
                                     CategoryItemRendererState state) {

        CategoryAxis domainAxis = getDomainAxis(plot, rendererIndex);
        CategoryDataset dataset = plot.getDataset(rendererIndex);
        if (dataset != null) {
            int columns = dataset.getColumnCount();
            int rows = state.getVisibleSeriesCount() >= 0
                    ? state.getVisibleSeriesCount() : dataset.getRowCount();
            double space = 0.0;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                space = dataArea.getHeight();
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                space = dataArea.getWidth();
            }
            double maxWidth = space * getMaximumBarWidth();
            double categoryMargin = 0.0;
            double currentItemMargin = 0.0;
            if (columns > 1) {
                categoryMargin = domainAxis.getCategoryMargin();
            }
            if (rows > 1) {
                currentItemMargin = getItemMargin();
            }
            double used = space * (1 - domainAxis.getLowerMargin()
                                     - domainAxis.getUpperMargin()
                                     - categoryMargin - currentItemMargin);
            if ((rows * columns) > 0) {
                state.setBarWidth(Math.min(used / (rows * columns), maxWidth));
            }
            else {
                state.setBarWidth(Math.min(used, maxWidth));
            }
        }
    }

    
    protected double calculateBarW0(CategoryPlot plot, 
            PlotOrientation orientation, Rectangle2D dataArea, 
            CategoryAxis domainAxis, CategoryItemRendererState state,
            int row, int column) {
        // calculate bar width...
        double space;
        if (orientation == PlotOrientation.HORIZONTAL) {
            space = dataArea.getHeight();
        }
        else {
            space = dataArea.getWidth();
        }
        double barW0 = domainAxis.getCategoryStart(column, getColumnCount(),
                dataArea, plot.getDomainAxisEdge());
        int seriesCount = state.getVisibleSeriesCount() >= 0
                ? state.getVisibleSeriesCount() : getRowCount();
        int categoryCount = getColumnCount();
        if (seriesCount > 1) {
            double seriesGap = space * getItemMargin()
                               / (categoryCount * (seriesCount - 1));
            double seriesW = calculateSeriesWidth(space, domainAxis,
                    categoryCount, seriesCount);
            barW0 = barW0 + row * (seriesW + seriesGap)
                          + (seriesW / 2.0) - (state.getBarWidth() / 2.0);
        }
        else {
            barW0 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                    dataArea, plot.getDomainAxisEdge()) - state.getBarWidth()
                    / 2.0;
        }
        return barW0;
    }

    
    protected double[] calculateBarL0L1(double value) {
        double lclip = getLowerClip();
        double uclip = getUpperClip();
        double barLow = Math.min(this.base, value);
        double barHigh = Math.max(this.base, value);
        if (barHigh < lclip) {  // bar is not visible
            return null;
        }
        if (barLow > uclip) {   // bar is not visible
            return null;
        }
        barLow = Math.max(barLow, lclip);
        barHigh = Math.min(barHigh, uclip);
        return new double[] {barLow, barHigh};
    }

    
    @Override
    public Range findRangeBounds(CategoryDataset dataset,
            boolean includeInterval) {
        if (dataset == null) {
            return null;
        }
        Range result = super.findRangeBounds(dataset, includeInterval);
        if (result != null) {
            if (this.includeBaseInRange) {
                result = Range.expandToInclude(result, this.base);
            }
        }
        return result;
    }

    
    @Override
    public LegendItem getLegendItem(int datasetIndex, int series) {

        CategoryPlot cp = getPlot();
        if (cp == null) {
            return null;
        }

        // check that a legend item needs to be displayed...
        if (!isSeriesVisible(series) || !isSeriesVisibleInLegend(series)) {
            return null;
        }

        CategoryDataset dataset = cp.getDataset(datasetIndex);
        String label = getLegendItemLabelGenerator().generateLabel(dataset,
                series);
        String description = label;
        String toolTipText = null;
        if (getLegendItemToolTipGenerator() != null) {
            toolTipText = getLegendItemToolTipGenerator().generateLabel(
                    dataset, series);
        }
        String urlText = null;
        if (getLegendItemURLGenerator() != null) {
            urlText = getLegendItemURLGenerator().generateLabel(dataset,
                    series);
        }
        Shape shape = lookupLegendShape(series);
        Paint paint = lookupSeriesPaint(series);
        Paint outlinePaint = lookupSeriesOutlinePaint(series);
        Stroke outlineStroke = lookupSeriesOutlineStroke(series);

        LegendItem result = new LegendItem(label, description, toolTipText,
                urlText, true, shape, true, paint, isDrawBarOutline(),
                outlinePaint, outlineStroke, false, new Line2D.Float(),
                new BasicStroke(1.0f), Color.BLACK);
        result.setLabelFont(lookupLegendTextFont(series));
        Paint labelPaint = lookupLegendTextPaint(series);
        if (labelPaint != null) {
            result.setLabelPaint(labelPaint);
        }
        result.setDataset(dataset);
        result.setDatasetIndex(datasetIndex);
        result.setSeriesKey(dataset.getRowKey(series));
        result.setSeriesIndex(series);
        if (this.gradientPaintTransformer != null) {
            result.setFillPaintTransformer(this.gradientPaintTransformer);
        }
        return result;
    }

    
    @Override
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, CategoryDataset dataset, int row,
            int column, int pass) {

        // nothing is drawn if the row index is not included in the list with
        // the indices of the visible rows...
        int visibleRow = state.getVisibleSeriesIndex(row);
        if (visibleRow < 0) {
            return;
        }
        // nothing is drawn for null values...
        Number dataValue = dataset.getValue(row, column);
        if (dataValue == null) {
            return;
        }

        final double value = dataValue.doubleValue();
        PlotOrientation orientation = plot.getOrientation();
        double barW0 = calculateBarW0(plot, orientation, dataArea, domainAxis,
                state, visibleRow, column);
        double[] barL0L1 = calculateBarL0L1(value);
        if (barL0L1 == null) {
            return;  // the bar is not visible
        }

        RectangleEdge edge = plot.getRangeAxisEdge();
        double transL0 = rangeAxis.valueToJava2D(barL0L1[0], dataArea, edge);
        double transL1 = rangeAxis.valueToJava2D(barL0L1[1], dataArea, edge);

        // in the following code, barL0 is (in Java2D coordinates) the LEFT
        // end of the bar for a horizontal bar chart, and the TOP end of the
        // bar for a vertical bar chart.  Whether this is the BASE of the bar
        // or not depends also on (a) whether the data value is 'negative'
        // relative to the base value and (b) whether or not the range axis is
        // inverted.  This only matters if/when we apply the minimumBarLength
        // attribute, because we should extend the non-base end of the bar
        boolean positive = (value >= this.base);
        boolean inverted = rangeAxis.isInverted();
        double barL0 = Math.min(transL0, transL1);
        double barLength = Math.abs(transL1 - transL0);
        double barLengthAdj = 0.0;
        if (barLength > 0.0 && barLength < getMinimumBarLength()) {
            barLengthAdj = getMinimumBarLength() - barLength;
        }
        double barL0Adj = 0.0;
        RectangleEdge barBase;
        if (orientation == PlotOrientation.HORIZONTAL) {
            if (positive && inverted || !positive && !inverted) {
                barL0Adj = barLengthAdj;
                barBase = RectangleEdge.RIGHT;
            }
            else {
                barBase = RectangleEdge.LEFT;
            }
        }
        else {
            if (positive && !inverted || !positive && inverted) {
                barL0Adj = barLengthAdj;
                barBase = RectangleEdge.BOTTOM;
            }
            else {
                barBase = RectangleEdge.TOP;
            }
        }

        // draw the bar...
        Rectangle2D bar;
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(barL0 - barL0Adj, barW0,
                    barLength + barLengthAdj, state.getBarWidth());
        }
        else {
            bar = new Rectangle2D.Double(barW0, barL0 - barL0Adj,
                    state.getBarWidth(), barLength + barLengthAdj);
        }
        if (state.getElementHinting()) {
            KeyedValues2DItemKey key = new KeyedValues2DItemKey(
                    dataset.getRowKey(row), dataset.getColumnKey(column));
            beginElementGroup(g2, key);
        }
        if (getShadowsVisible()) {
            this.barPainter.paintBarShadow(g2, this, row, column, bar, barBase,
                true);
        }
        this.barPainter.paintBar(g2, this, row, column, bar, barBase);
        if (state.getElementHinting()) {
            endElementGroup(g2);
        }
        
        CategoryItemLabelGenerator generator = getItemLabelGenerator(row,
                column);
        if (generator != null && isItemLabelVisible(row, column)) {
            drawItemLabel(g2, dataset, row, column, plot, generator, bar,
                    (value < 0.0));
        }

        // submit the current data point as a crosshair candidate
        int datasetIndex = plot.indexOf(dataset);
        updateCrosshairValues(state.getCrosshairState(),
                dataset.getRowKey(row), dataset.getColumnKey(column), value,
                datasetIndex, barW0, barL0, orientation);

        // add an item entity, if this information is being collected
        EntityCollection entities = state.getEntityCollection();
        if (entities != null) {
            addItemEntity(entities, dataset, row, column, bar);
        }

    }
    
    
    protected double calculateSeriesWidth(double space, CategoryAxis axis,
                                          int categories, int series) {
        double factor = 1.0 - getItemMargin() - axis.getLowerMargin()
                            - axis.getUpperMargin();
        if (categories > 1) {
            factor = factor - axis.getCategoryMargin();
        }
        return (space * factor) / (categories * series);
    }

    
    protected void drawItemLabel(Graphics2D g2,
                                 CategoryDataset data,
                                 int row,
                                 int column,
                                 CategoryPlot plot,
                                 CategoryItemLabelGenerator generator,
                                 Rectangle2D bar,
                                 boolean negative) {

        String label = generator.generateLabel(data, row, column);
        if (label == null) {
            return;  // nothing to do
        }

        Font labelFont = getItemLabelFont(row, column);
        g2.setFont(labelFont);
        Paint paint = getItemLabelPaint(row, column);
        g2.setPaint(paint);

        // find out where to place the label...
        ItemLabelPosition position;
        if (!negative) {
            position = getPositiveItemLabelPosition(row, column);
        }
        else {
            position = getNegativeItemLabelPosition(row, column);
        }

        // work out the label anchor point...
        Point2D anchorPoint = calculateLabelAnchorPoint(
                position.getItemLabelAnchor(), bar, plot.getOrientation());

        if (isInternalAnchor(position.getItemLabelAnchor())) {
            Shape bounds = TextUtils.calculateRotatedStringBounds(label,
                    g2, (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                    position.getTextAnchor(), position.getAngle(),
                    position.getRotationAnchor());

            if (bounds != null) {
                if (!bar.contains(bounds.getBounds2D())) {
                    if (!negative) {
                        position = getPositiveItemLabelPositionFallback();
                    }
                    else {
                        position = getNegativeItemLabelPositionFallback();
                    }
                    if (position != null) {
                        anchorPoint = calculateLabelAnchorPoint(
                                position.getItemLabelAnchor(), bar,
                                plot.getOrientation());
                    }
                }
            }

        }

        if (position != null) {
            TextUtils.drawRotatedString(label, g2,
                    (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                    position.getTextAnchor(), position.getAngle(),
                    position.getRotationAnchor());
        }
    }

    
    private Point2D calculateLabelAnchorPoint(ItemLabelAnchor anchor,
                                              Rectangle2D bar,
                                              PlotOrientation orientation) {

        Point2D result = null;
        double offset = getItemLabelAnchorOffset();
        double x0 = bar.getX() - offset;
        double x1 = bar.getX();
        double x2 = bar.getX() + offset;
        double x3 = bar.getCenterX();
        double x4 = bar.getMaxX() - offset;
        double x5 = bar.getMaxX();
        double x6 = bar.getMaxX() + offset;

        double y0 = bar.getMaxY() + offset;
        double y1 = bar.getMaxY();
        double y2 = bar.getMaxY() - offset;
        double y3 = bar.getCenterY();
        double y4 = bar.getMinY() + offset;
        double y5 = bar.getMinY();
        double y6 = bar.getMinY() - offset;

        if (anchor == ItemLabelAnchor.CENTER) {
            result = new Point2D.Double(x3, y3);
        }
        else if (anchor == ItemLabelAnchor.INSIDE1) {
            result = new Point2D.Double(x4, y4);
        }
        else if (anchor == ItemLabelAnchor.INSIDE2) {
            result = new Point2D.Double(x4, y4);
        }
        else if (anchor == ItemLabelAnchor.INSIDE3) {
            result = new Point2D.Double(x4, y3);
        }
        else if (anchor == ItemLabelAnchor.INSIDE4) {
            result = new Point2D.Double(x4, y2);
        }
        else if (anchor == ItemLabelAnchor.INSIDE5) {
            result = new Point2D.Double(x4, y2);
        }
        else if (anchor == ItemLabelAnchor.INSIDE6) {
            result = new Point2D.Double(x3, y2);
        }
        else if (anchor == ItemLabelAnchor.INSIDE7) {
            result = new Point2D.Double(x2, y2);
        }
        else if (anchor == ItemLabelAnchor.INSIDE8) {
            result = new Point2D.Double(x2, y2);
        }
        else if (anchor == ItemLabelAnchor.INSIDE9) {
            result = new Point2D.Double(x2, y3);
        }
        else if (anchor == ItemLabelAnchor.INSIDE10) {
            result = new Point2D.Double(x2, y4);
        }
        else if (anchor == ItemLabelAnchor.INSIDE11) {
            result = new Point2D.Double(x2, y4);
        }
        else if (anchor == ItemLabelAnchor.INSIDE12) {
            result = new Point2D.Double(x3, y4);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE1) {
            result = new Point2D.Double(x5, y6);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE2) {
            result = new Point2D.Double(x6, y5);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE3) {
            result = new Point2D.Double(x6, y3);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE4) {
            result = new Point2D.Double(x6, y1);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE5) {
            result = new Point2D.Double(x5, y0);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE6) {
            result = new Point2D.Double(x3, y0);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE7) {
            result = new Point2D.Double(x1, y0);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE8) {
            result = new Point2D.Double(x0, y1);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE9) {
            result = new Point2D.Double(x0, y3);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE10) {
            result = new Point2D.Double(x0, y5);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE11) {
            result = new Point2D.Double(x1, y6);
        }
        else if (anchor == ItemLabelAnchor.OUTSIDE12) {
            result = new Point2D.Double(x3, y6);
        }

        return result;

    }

    
    private boolean isInternalAnchor(ItemLabelAnchor anchor) {
        return anchor == ItemLabelAnchor.CENTER
               || anchor == ItemLabelAnchor.INSIDE1
               || anchor == ItemLabelAnchor.INSIDE2
               || anchor == ItemLabelAnchor.INSIDE3
               || anchor == ItemLabelAnchor.INSIDE4
               || anchor == ItemLabelAnchor.INSIDE5
               || anchor == ItemLabelAnchor.INSIDE6
               || anchor == ItemLabelAnchor.INSIDE7
               || anchor == ItemLabelAnchor.INSIDE8
               || anchor == ItemLabelAnchor.INSIDE9
               || anchor == ItemLabelAnchor.INSIDE10
               || anchor == ItemLabelAnchor.INSIDE11
               || anchor == ItemLabelAnchor.INSIDE12;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BarRenderer)) {
            return false;
        }
        BarRenderer that = (BarRenderer) obj;
        if (this.base != that.base) {
            return false;
        }
        if (this.itemMargin != that.itemMargin) {
            return false;
        }
        if (this.drawBarOutline != that.drawBarOutline) {
            return false;
        }
        if (this.maximumBarWidth != that.maximumBarWidth) {
            return false;
        }
        if (this.minimumBarLength != that.minimumBarLength) {
            return false;
        }
        if (!Objects.equals(this.gradientPaintTransformer, that.gradientPaintTransformer)) {
            return false;
        }
        if (!Objects.equals(this.positiveItemLabelPositionFallback, that.positiveItemLabelPositionFallback)) {
            return false;
        }
        if (!Objects.equals(this.negativeItemLabelPositionFallback, that.negativeItemLabelPositionFallback)) {
            return false;
        }
        if (!this.barPainter.equals(that.barPainter)) {
            return false;
        }
        if (this.shadowsVisible != that.shadowsVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.shadowPaint, that.shadowPaint)) {
            return false;
        }
        if (this.shadowXOffset != that.shadowXOffset) {
            return false;
        }
        if (this.shadowYOffset != that.shadowYOffset) {
            return false;
        }
        return super.equals(obj);
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.shadowPaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.shadowPaint = SerialUtils.readPaint(stream);
    }

}
