

package org.jfree.chart.axis;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jfree.chart.entity.CategoryLabelEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.text.G2TextMeasurer;
import org.jfree.chart.text.TextBlock;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.internal.ShapeUtils;
import org.jfree.data.category.CategoryDataset;


public class CategoryAxis extends Axis implements Cloneable, Serializable {

    
    private static final long serialVersionUID = 5886554608114265863L;

    
    public static final double DEFAULT_AXIS_MARGIN = 0.05;

    
    public static final double DEFAULT_CATEGORY_MARGIN = 0.20;

    
    private double lowerMargin;

    
    private double upperMargin;

    
    private double categoryMargin;

    
    private int maximumCategoryLabelLines;

    
    private float maximumCategoryLabelWidthRatio;

    
    private int categoryLabelPositionOffset;

    
    private CategoryLabelPositions categoryLabelPositions;

    
    private Map<Comparable, Font> tickLabelFontMap; 

    
    private transient Map<Comparable, Paint> tickLabelPaintMap;

    
    private Map<Comparable, String> categoryLabelToolTips;

    
    private Map<Comparable, String> categoryLabelURLs;
    
    
    public CategoryAxis() {
        this(null);
    }

    
    public CategoryAxis(String label) {
        super(label);

        this.lowerMargin = DEFAULT_AXIS_MARGIN;
        this.upperMargin = DEFAULT_AXIS_MARGIN;
        this.categoryMargin = DEFAULT_CATEGORY_MARGIN;
        this.maximumCategoryLabelLines = 1;
        this.maximumCategoryLabelWidthRatio = 0.0f;

        this.categoryLabelPositionOffset = 4;
        this.categoryLabelPositions = CategoryLabelPositions.STANDARD;
        this.tickLabelFontMap = new HashMap<>();
        this.tickLabelPaintMap = new HashMap<>();
        this.categoryLabelToolTips = new HashMap<>();
        this.categoryLabelURLs = new HashMap<>();
    }

    
    public double getLowerMargin() {
        return this.lowerMargin;
    }

    
    public void setLowerMargin(double margin) {
        this.lowerMargin = margin;
        fireChangeEvent();
    }

    
    public double getUpperMargin() {
        return this.upperMargin;
    }

    
    public void setUpperMargin(double margin) {
        this.upperMargin = margin;
        fireChangeEvent();
    }

    
    public double getCategoryMargin() {
        return this.categoryMargin;
    }

    
    public void setCategoryMargin(double margin) {
        this.categoryMargin = margin;
        fireChangeEvent();
    }

    
    public int getMaximumCategoryLabelLines() {
        return this.maximumCategoryLabelLines;
    }

    
    public void setMaximumCategoryLabelLines(int lines) {
        this.maximumCategoryLabelLines = lines;
        fireChangeEvent();
    }

    
    public float getMaximumCategoryLabelWidthRatio() {
        return this.maximumCategoryLabelWidthRatio;
    }

    
    public void setMaximumCategoryLabelWidthRatio(float ratio) {
        this.maximumCategoryLabelWidthRatio = ratio;
        fireChangeEvent();
    }

    
    public int getCategoryLabelPositionOffset() {
        return this.categoryLabelPositionOffset;
    }

    
    public void setCategoryLabelPositionOffset(int offset) {
        this.categoryLabelPositionOffset = offset;
        fireChangeEvent();
    }

    
    public CategoryLabelPositions getCategoryLabelPositions() {
        return this.categoryLabelPositions;
    }

    
    public void setCategoryLabelPositions(CategoryLabelPositions positions) {
        Args.nullNotPermitted(positions, "positions");
        this.categoryLabelPositions = positions;
        fireChangeEvent();
    }

    
    public Font getTickLabelFont(Comparable category) {
        Args.nullNotPermitted(category, "category");
        Font result = this.tickLabelFontMap.get(category);
        // if there is no specific font, use the general one...
        if (result == null) {
            result = getTickLabelFont();
        }
        return result;
    }

    
    public void setTickLabelFont(Comparable category, Font font) {
        Args.nullNotPermitted(category, "category");
        if (font == null) {
            this.tickLabelFontMap.remove(category);
        } else {
            this.tickLabelFontMap.put(category, font);
        }
        fireChangeEvent();
    }

    
    public Paint getTickLabelPaint(Comparable category) {
        Args.nullNotPermitted(category, "category");
        Paint result = this.tickLabelPaintMap.get(category);
        // if there is no specific paint, use the general one...
        if (result == null) {
            result = getTickLabelPaint();
        }
        return result;
    }

    
    public void setTickLabelPaint(Comparable category, Paint paint) {
        Args.nullNotPermitted(category, "category");
        if (paint == null) {
            this.tickLabelPaintMap.remove(category);
        } else {
            this.tickLabelPaintMap.put(category, paint);
        }
        fireChangeEvent();
    }

    
    public void addCategoryLabelToolTip(Comparable category, String tooltip) {
        Args.nullNotPermitted(category, "category");
        this.categoryLabelToolTips.put(category, tooltip);
        fireChangeEvent();
    }

    
    public String getCategoryLabelToolTip(Comparable category) {
        Args.nullNotPermitted(category, "category");
        return this.categoryLabelToolTips.get(category);
    }

    
    public void removeCategoryLabelToolTip(Comparable category) {
        Args.nullNotPermitted(category, "category");
        if (this.categoryLabelToolTips.remove(category) != null) {
            fireChangeEvent();
        }
    }

    
    public void clearCategoryLabelToolTips() {
        this.categoryLabelToolTips.clear();
        fireChangeEvent();
    }

    
    public void addCategoryLabelURL(Comparable category, String url) {
        Args.nullNotPermitted(category, "category");
        this.categoryLabelURLs.put(category, url);
        fireChangeEvent();
    }

    
    public String getCategoryLabelURL(Comparable category) {
        Args.nullNotPermitted(category, "category");
        return this.categoryLabelURLs.get(category);
    }

    
    public void removeCategoryLabelURL(Comparable category) {
        Args.nullNotPermitted(category, "category");
        if (this.categoryLabelURLs.remove(category) != null) {
            fireChangeEvent();
        }
    }

    
    public void clearCategoryLabelURLs() {
        this.categoryLabelURLs.clear();
        fireChangeEvent();
    }
    
    
    public double getCategoryJava2DCoordinate(CategoryAnchor anchor, 
            int category, int categoryCount, Rectangle2D area, 
            RectangleEdge edge) {
        Args.nullNotPermitted(anchor, "anchor");
        double result = 0.0;
        switch (anchor) {
            case START:
                result = getCategoryStart(category, categoryCount, area, edge);
                break;
            case MIDDLE:
                result = getCategoryMiddle(category, categoryCount, area, edge);
                break;
            case END:
                result = getCategoryEnd(category, categoryCount, area, edge);
                break;
            default:
                throw new IllegalStateException("Unexpected anchor value.");
        }
        return result;

    }

    
    public double getCategoryStart(int category, int categoryCount, 
            Rectangle2D area, RectangleEdge edge) {

        double result = 0.0;
        if ((edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM)) {
            result = area.getX() + area.getWidth() * getLowerMargin();
        }
        else if ((edge == RectangleEdge.LEFT)
                || (edge == RectangleEdge.RIGHT)) {
            result = area.getMinY() + area.getHeight() * getLowerMargin();
        }

        double categorySize = calculateCategorySize(categoryCount, area, edge);
        double categoryGapWidth = calculateCategoryGapSize(categoryCount, area,
                edge);

        result = result + category * (categorySize + categoryGapWidth);
        return result;
    }

    
    public double getCategoryMiddle(int category, int categoryCount,
            Rectangle2D area, RectangleEdge edge) {

        if (category < 0 || category >= categoryCount) {
            throw new IllegalArgumentException("Invalid category index: "
                    + category);
        }
        return getCategoryStart(category, categoryCount, area, edge)
               + calculateCategorySize(categoryCount, area, edge) / 2;

    }

    
    public double getCategoryEnd(int category, int categoryCount,
            Rectangle2D area, RectangleEdge edge) {
        return getCategoryStart(category, categoryCount, area, edge)
               + calculateCategorySize(categoryCount, area, edge);
    }

    
    public double getCategoryMiddle(Comparable category,
            List categories, Rectangle2D area, RectangleEdge edge) {
        Args.nullNotPermitted(categories, "categories");
        int categoryIndex = categories.indexOf(category);
        int categoryCount = categories.size();
        return getCategoryMiddle(categoryIndex, categoryCount, area, edge);
    }

    
    public double getCategorySeriesMiddle(Comparable category,
            Comparable seriesKey, CategoryDataset dataset, double itemMargin,
            Rectangle2D area, RectangleEdge edge) {

        int categoryIndex = dataset.getColumnIndex(category);
        int categoryCount = dataset.getColumnCount();
        int seriesIndex = dataset.getRowIndex(seriesKey);
        int seriesCount = dataset.getRowCount();
        double start = getCategoryStart(categoryIndex, categoryCount, area,
                edge);
        double end = getCategoryEnd(categoryIndex, categoryCount, area, edge);
        double width = end - start;
        if (seriesCount == 1) {
            return start + width / 2.0;
        }
        else {
            double gap = (width * itemMargin) / (seriesCount - 1);
            double ww = (width * (1 - itemMargin)) / seriesCount;
            return start + (seriesIndex * (ww + gap)) + ww / 2.0;
        }
    }

    
    public double getCategorySeriesMiddle(int categoryIndex, int categoryCount,
            int seriesIndex, int seriesCount, double itemMargin,
            Rectangle2D area, RectangleEdge edge) {

        double start = getCategoryStart(categoryIndex, categoryCount, area,
                edge);
        double end = getCategoryEnd(categoryIndex, categoryCount, area, edge);
        double width = end - start;
        if (seriesCount == 1) {
            return start + width / 2.0;
        }
        else {
            double gap = (width * itemMargin) / (seriesCount - 1);
            double ww = (width * (1 - itemMargin)) / seriesCount;
            return start + (seriesIndex * (ww + gap)) + ww / 2.0;
        }
    }

    
    protected double calculateCategorySize(int categoryCount, Rectangle2D area,
            RectangleEdge edge) {
        double result;
        double available = 0.0;

        if ((edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM)) {
            available = area.getWidth();
        }
        else if ((edge == RectangleEdge.LEFT)
                || (edge == RectangleEdge.RIGHT)) {
            available = area.getHeight();
        }
        if (categoryCount > 1) {
            result = available * (1 - getLowerMargin() - getUpperMargin()
                     - getCategoryMargin());
            result = result / categoryCount;
        }
        else {
            result = available * (1 - getLowerMargin() - getUpperMargin());
        }
        return result;
    }

    
    protected double calculateCategoryGapSize(int categoryCount, 
            Rectangle2D area, RectangleEdge edge) {

        double result = 0.0;
        double available = 0.0;

        if ((edge == RectangleEdge.TOP) || (edge == RectangleEdge.BOTTOM)) {
            available = area.getWidth();
        }
        else if ((edge == RectangleEdge.LEFT)
                || (edge == RectangleEdge.RIGHT)) {
            available = area.getHeight();
        }

        if (categoryCount > 1) {
            result = available * getCategoryMargin() / (categoryCount - 1);
        }
        return result;
    }

    
    @Override
    public AxisSpace reserveSpace(Graphics2D g2, Plot plot, 
            Rectangle2D plotArea, RectangleEdge edge, AxisSpace space) {

        // create a new space object if one wasn't supplied...
        if (space == null) {
            space = new AxisSpace();
        }

        // if the axis is not visible, no additional space is required...
        if (!isVisible()) {
            return space;
        }

        // calculate the max size of the tick labels (if visible)...
        double tickLabelHeight = 0.0;
        double tickLabelWidth = 0.0;
        if (isTickLabelsVisible()) {
            g2.setFont(getTickLabelFont());
            AxisState state = new AxisState();
            // we call refresh ticks just to get the maximum width or height
            refreshTicks(g2, state, plotArea, edge);
            switch (edge) {
                case TOP:
                    tickLabelHeight = state.getMax();
                    break;
                case BOTTOM:
                    tickLabelHeight = state.getMax();
                    break;
                case LEFT:
                    tickLabelWidth = state.getMax();
                    break;
                case RIGHT:
                    tickLabelWidth = state.getMax();
                    break;
                default:
                    throw new IllegalStateException("Unexpected RectangleEdge value.");
            }
        }

        // get the axis label size and update the space object...
        Rectangle2D labelEnclosure = getLabelEnclosure(g2, edge);
        double labelHeight, labelWidth;
        if (RectangleEdge.isTopOrBottom(edge)) {
            labelHeight = labelEnclosure.getHeight();
            space.add(labelHeight + tickLabelHeight
                    + this.categoryLabelPositionOffset, edge);
        }
        else if (RectangleEdge.isLeftOrRight(edge)) {
            labelWidth = labelEnclosure.getWidth();
            space.add(labelWidth + tickLabelWidth
                    + this.categoryLabelPositionOffset, edge);
        }
        return space;
    }

    
    @Override
    public void configure() {
        // nothing required
    }

    
    @Override
    public AxisState draw(Graphics2D g2, double cursor, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge,
            PlotRenderingInfo plotState) {

        // if the axis is not visible, don't draw it...
        if (!isVisible()) {
            return new AxisState(cursor);
        }

        if (isAxisLineVisible()) {
            drawAxisLine(g2, cursor, dataArea, edge);
        }
        AxisState state = new AxisState(cursor);
        if (isTickMarksVisible()) {
            drawTickMarks(g2, cursor, dataArea, edge, state);
        }

        createAndAddEntity(cursor, state, dataArea, edge, plotState);

        // draw the category labels and axis label
        state = drawCategoryLabels(g2, plotArea, dataArea, edge, state,
                plotState);
        if (getAttributedLabel() != null) {
            state = drawAttributedLabel(getAttributedLabel(), g2, plotArea, 
                    dataArea, edge, state);
            
        } else {
            state = drawLabel(getLabel(), g2, plotArea, dataArea, edge, state);
        }
        return state;

    }

    
    protected AxisState drawCategoryLabels(Graphics2D g2, Rectangle2D plotArea,
            Rectangle2D dataArea, RectangleEdge edge, AxisState state,
            PlotRenderingInfo plotState) {

        Args.nullNotPermitted(state, "state");
        if (!isTickLabelsVisible()) {
            return state;
        }
 
        List ticks = refreshTicks(g2, state, plotArea, edge);
        state.setTicks(ticks);
        int categoryIndex = 0;
        for (Object o : ticks) {
            CategoryTick tick = (CategoryTick) o;
            g2.setFont(getTickLabelFont(tick.getCategory()));
            g2.setPaint(getTickLabelPaint(tick.getCategory()));

            CategoryLabelPosition position
                    = this.categoryLabelPositions.getLabelPosition(edge);
            double x0 = 0.0;
            double x1 = 0.0;
            double y0 = 0.0;
            double y1 = 0.0;
            if (edge == RectangleEdge.TOP) {
                x0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, 
                        edge);
                x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, 
                        edge);
                y1 = state.getCursor() - this.categoryLabelPositionOffset;
                y0 = y1 - state.getMax();
            }
            else if (edge == RectangleEdge.BOTTOM) {
                x0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, 
                        edge);
                x1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea, 
                        edge);
                y0 = state.getCursor() + this.categoryLabelPositionOffset;
                y1 = y0 + state.getMax();
            }
            else if (edge == RectangleEdge.LEFT) {
                y0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, 
                        edge);
                y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea,
                        edge);
                x1 = state.getCursor() - this.categoryLabelPositionOffset;
                x0 = x1 - state.getMax();
            }
            else if (edge == RectangleEdge.RIGHT) {
                y0 = getCategoryStart(categoryIndex, ticks.size(), dataArea, 
                        edge);
                y1 = getCategoryEnd(categoryIndex, ticks.size(), dataArea,
                        edge);
                x0 = state.getCursor() + this.categoryLabelPositionOffset;
                x1 = x0 - state.getMax();
            }
            Rectangle2D area = new Rectangle2D.Double(x0, y0, (x1 - x0),
                    (y1 - y0));
            Point2D anchorPoint = position.getCategoryAnchor().getAnchorPoint(area);
            TextBlock block = tick.getLabel();
            block.draw(g2, (float) anchorPoint.getX(),
                    (float) anchorPoint.getY(), position.getLabelAnchor(),
                    (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                    position.getAngle());
            Shape bounds = block.calculateBounds(g2,
                    (float) anchorPoint.getX(), (float) anchorPoint.getY(),
                    position.getLabelAnchor(), (float) anchorPoint.getX(),
                    (float) anchorPoint.getY(), position.getAngle());
            if (plotState != null && plotState.getOwner() != null) {
                EntityCollection entities = plotState.getOwner()
                        .getEntityCollection();
                if (entities != null) {
                    String tooltip = getCategoryLabelToolTip(
                            tick.getCategory());
                    String url = getCategoryLabelURL(tick.getCategory());
                    entities.add(new CategoryLabelEntity(tick.getCategory(),
                            bounds, tooltip, url));
                }
            }
            categoryIndex++;
        }

        if (edge.equals(RectangleEdge.TOP)) {
            double h = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorUp(h);
        }
        else if (edge.equals(RectangleEdge.BOTTOM)) {
            double h = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorDown(h);
        }
        else if (edge == RectangleEdge.LEFT) {
            double w = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorLeft(w);
        }
        else if (edge == RectangleEdge.RIGHT) {
            double w = state.getMax() + this.categoryLabelPositionOffset;
            state.cursorRight(w);
        }
        return state;
    }

    
    @Override
    public List refreshTicks(Graphics2D g2, AxisState state, 
            Rectangle2D dataArea, RectangleEdge edge) {

        List ticks = new java.util.ArrayList(); // FIXME generics

        // sanity check for data area...
        if (dataArea.getHeight() <= 0.0 || dataArea.getWidth() < 0.0) {
            return ticks;
        }

        CategoryPlot plot = (CategoryPlot) getPlot();
        List categories = plot.getCategoriesForAxis(this);
        double max = 0.0;

        if (categories != null) {
            CategoryLabelPosition position
                    = this.categoryLabelPositions.getLabelPosition(edge);
            float r = this.maximumCategoryLabelWidthRatio;
            if (r <= 0.0) {
                r = position.getWidthRatio();
            }

            float l;
            if (position.getWidthType() == CategoryLabelWidthType.CATEGORY) {
                l = (float) calculateCategorySize(categories.size(), dataArea,
                        edge);
            }
            else {
                if (RectangleEdge.isLeftOrRight(edge)) {
                    l = (float) dataArea.getWidth();
                }
                else {
                    l = (float) dataArea.getHeight();
                }
            }
            int categoryIndex = 0;
            for (Object o : categories) {
                Comparable category = (Comparable) o;
                g2.setFont(getTickLabelFont(category));
                TextBlock label = createLabel(category, l * r, edge, g2);
                if (edge == RectangleEdge.TOP || edge == RectangleEdge.BOTTOM) {
                    max = Math.max(max, calculateCategoryLabelHeight(label,
                            position, getTickLabelInsets(), g2));
                } else if (edge == RectangleEdge.LEFT
                        || edge == RectangleEdge.RIGHT) {
                    max = Math.max(max, calculateCategoryLabelWidth(label,
                            position, getTickLabelInsets(), g2));
                }
                Tick tick = new CategoryTick(category, label,
                        position.getLabelAnchor(),
                        position.getRotationAnchor(), position.getAngle());
                ticks.add(tick);
                categoryIndex = categoryIndex + 1;
            }
        }
        state.setMax(max);
        return ticks;

    }

    
    public void drawTickMarks(Graphics2D g2, double cursor,
            Rectangle2D dataArea, RectangleEdge edge, AxisState state) {

        Plot p = getPlot();
        if (p == null) {
            return;
        }
        CategoryPlot plot = (CategoryPlot) p;
        double il = getTickMarkInsideLength();
        double ol = getTickMarkOutsideLength();
        Line2D line = new Line2D.Double();
        List<Comparable> categories = plot.getCategoriesForAxis(this);
        g2.setPaint(getTickMarkPaint());
        g2.setStroke(getTickMarkStroke());
        Object saved = g2.getRenderingHint(RenderingHints.KEY_STROKE_CONTROL);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_NORMALIZE);
        if (edge.equals(RectangleEdge.TOP)) {
            for (Comparable category : categories) {
                double x = getCategoryMiddle(category, categories, dataArea, edge);
                line.setLine(x, cursor, x, cursor + il);
                g2.draw(line);
                line.setLine(x, cursor, x, cursor - ol);
                g2.draw(line);
            }
            state.cursorUp(ol);
        } else if (edge.equals(RectangleEdge.BOTTOM)) {
            for (Comparable category : categories) {
                double x = getCategoryMiddle(category, categories, dataArea, edge);
                line.setLine(x, cursor, x, cursor - il);
                g2.draw(line);
                line.setLine(x, cursor, x, cursor + ol);
                g2.draw(line);
            }
            state.cursorDown(ol);
        } else if (edge.equals(RectangleEdge.LEFT)) {
            for (Comparable category : categories) {
                double y = getCategoryMiddle(category, categories, dataArea, edge);
                line.setLine(cursor, y, cursor + il, y);
                g2.draw(line);
                line.setLine(cursor, y, cursor - ol, y);
                g2.draw(line);
            }
            state.cursorLeft(ol);
        } else if (edge.equals(RectangleEdge.RIGHT)) {
            for (Comparable category : categories) {
                double y = getCategoryMiddle(category, categories, dataArea, edge);
                line.setLine(cursor, y, cursor - il, y);
                g2.draw(line);
                line.setLine(cursor, y, cursor + ol, y);
                g2.draw(line);
            }
            state.cursorRight(ol);
        }
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, saved);
    }

    
    protected TextBlock createLabel(Comparable category, float width,
            RectangleEdge edge, Graphics2D g2) {
        TextBlock label = TextUtils.createTextBlock(category.toString(),
                getTickLabelFont(category), getTickLabelPaint(category), width,
                this.maximumCategoryLabelLines, new G2TextMeasurer(g2));
        return label;
    }

    
    protected double calculateCategoryLabelWidth(TextBlock label, 
            CategoryLabelPosition position, RectangleInsets insets, Graphics2D g2) {
        Size2D size = label.calculateDimensions(g2);
        Rectangle2D box = new Rectangle2D.Double(0.0, 0.0, size.getWidth(),
                size.getHeight());
        Shape rotatedBox = ShapeUtils.rotateShape(box, position.getAngle(),
                0.0f, 0.0f);
        double w = rotatedBox.getBounds2D().getWidth() + insets.getLeft()
                + insets.getRight();
        return w;
    }

    
    protected double calculateCategoryLabelHeight(TextBlock block,
            CategoryLabelPosition position, RectangleInsets insets, Graphics2D g2) {
        Size2D size = block.calculateDimensions(g2);
        Rectangle2D box = new Rectangle2D.Double(0.0, 0.0, size.getWidth(),
                size.getHeight());
        Shape rotatedBox = ShapeUtils.rotateShape(box, position.getAngle(),
                0.0f, 0.0f);
        double h = rotatedBox.getBounds2D().getHeight()
                   + insets.getTop() + insets.getBottom();
        return h;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        CategoryAxis clone = (CategoryAxis) super.clone();
        clone.tickLabelFontMap = new HashMap<>(this.tickLabelFontMap);
        clone.tickLabelPaintMap = new HashMap<>(this.tickLabelPaintMap);
        clone.categoryLabelToolTips = new HashMap<>(this.categoryLabelToolTips);
        clone.categoryLabelURLs = new HashMap<>(this.categoryLabelToolTips);
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CategoryAxis)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        CategoryAxis that = (CategoryAxis) obj;
        if (that.lowerMargin != this.lowerMargin) {
            return false;
        }
        if (that.upperMargin != this.upperMargin) {
            return false;
        }
        if (that.categoryMargin != this.categoryMargin) {
            return false;
        }
        if (that.maximumCategoryLabelWidthRatio
                != this.maximumCategoryLabelWidthRatio) {
            return false;
        }
        if (that.categoryLabelPositionOffset
                != this.categoryLabelPositionOffset) {
            return false;
        }
        if (!Objects.equals(that.categoryLabelPositions, this.categoryLabelPositions)) {
            return false;
        }
        if (!Objects.equals(that.categoryLabelToolTips, this.categoryLabelToolTips)) {
            return false;
        }
        if (!Objects.equals(this.categoryLabelURLs, that.categoryLabelURLs)) {
            return false;
        }
        if (!Objects.equals(this.tickLabelFontMap, that.tickLabelFontMap)) {
            return false;
        }
        if (!PaintUtils.equal(this.tickLabelPaintMap, that.tickLabelPaintMap)) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        writePaintMap(this.tickLabelPaintMap, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.tickLabelPaintMap = readPaintMap(stream);
    }

    
    private Map readPaintMap(ObjectInputStream in)
            throws IOException, ClassNotFoundException {
        boolean isNull = in.readBoolean();
        if (isNull) {
            return null;
        }
        Map result = new HashMap();
        int count = in.readInt();
        for (int i = 0; i < count; i++) {
            Comparable category = (Comparable) in.readObject();
            Paint paint = SerialUtils.readPaint(in);
            result.put(category, paint);
        }
        return result;
    }

    
    private void writePaintMap(Map map, ObjectOutputStream out)
            throws IOException {
        if (map == null) {
            out.writeBoolean(true);
        }
        else {
            out.writeBoolean(false);
            Set keys = map.keySet();
            int count = keys.size();
            out.writeInt(count);
            for (Object o : keys) {
                Comparable key = (Comparable) o;
                out.writeObject(key);
                SerialUtils.writePaint((Paint) map.get(key), out);
            }
        }
    }

}
