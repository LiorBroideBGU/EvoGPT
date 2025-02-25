

package org.jfree.chart.renderer.category;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.ShapeUtils;
import org.jfree.data.category.CategoryDataset;


public class LineAndShapeRenderer extends AbstractCategoryItemRenderer
        implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = -197749519869226398L;

    
    private Map<Integer, Boolean> seriesLinesVisibleMap;

    
    private boolean defaultLinesVisible;

    
    private Map<Integer, Boolean> seriesShapesVisibleMap;

    
    private boolean defaultShapesVisible;

    
    private Map<Integer, Boolean> seriesShapesFilledMap;

    
    private boolean defaultShapesFilled;

    
    private boolean useFillPaint;

    
    private boolean drawOutlines;

    
    private boolean useOutlinePaint;

    
    private boolean useSeriesOffset;

    
    private double itemMargin;

    
    public LineAndShapeRenderer() {
        this(true, true);
    }

    
    public LineAndShapeRenderer(boolean lines, boolean shapes) {
        super();
        this.seriesLinesVisibleMap = new HashMap<>();
        this.defaultLinesVisible = lines;
        this.seriesShapesVisibleMap = new HashMap<>();
        this.defaultShapesVisible = shapes;
        this.seriesShapesFilledMap = new HashMap<>();
        this.defaultShapesFilled = true;
        this.useFillPaint = false;
        this.drawOutlines = true;
        this.useOutlinePaint = false;
        this.useSeriesOffset = false;  // preserves old behaviour
        this.itemMargin = 0.0;
    }

    // LINES VISIBLE

    
    public boolean getItemLineVisible(int series, int item) {
        Boolean flag = getSeriesLinesVisible(series);
        if (flag != null) {
            return flag;
        }
        return this.defaultLinesVisible;
    }

    
    public Boolean getSeriesLinesVisible(int series) {
        return this.seriesLinesVisibleMap.get(series);
    }

    
    public void setSeriesLinesVisible(int series, Boolean flag) {
        this.seriesLinesVisibleMap.put(series, flag);
        fireChangeEvent();
    }

    
    public void setSeriesLinesVisible(int series, boolean visible) {
        setSeriesLinesVisible(series, Boolean.valueOf(visible));
    }

    
    public boolean getDefaultLinesVisible() {
        return this.defaultLinesVisible;
    }

    
    public void setDefaultLinesVisible(boolean flag) {
        this.defaultLinesVisible = flag;
        fireChangeEvent();
    }

    // SHAPES VISIBLE

    
    public boolean getItemShapeVisible(int series, int item) {
        Boolean flag = getSeriesShapesVisible(series);
        if (flag != null) {
            return flag;
        }
        return this.defaultShapesVisible;
    }

    
    public Boolean getSeriesShapesVisible(int series) {
        return this.seriesShapesVisibleMap.get(series);
    }

    
    public void setSeriesShapesVisible(int series, boolean visible) {
        setSeriesShapesVisible(series, Boolean.valueOf(visible));
    }

    
    public void setSeriesShapesVisible(int series, Boolean flag) {
        this.seriesShapesVisibleMap.put(series, flag);
        fireChangeEvent();
    }

    
    public boolean getDefaultShapesVisible() {
        return this.defaultShapesVisible;
    }

    
    public void setDefaultShapesVisible(boolean flag) {
        this.defaultShapesVisible = flag;
        fireChangeEvent();
    }

    
    public boolean getDrawOutlines() {
        return this.drawOutlines;
    }

    
    public void setDrawOutlines(boolean flag) {
        this.drawOutlines = flag;
        fireChangeEvent();
    }

    
    public boolean getUseOutlinePaint() {
        return this.useOutlinePaint;
    }

    
    public void setUseOutlinePaint(boolean use) {
        this.useOutlinePaint = use;
        fireChangeEvent();
    }

    // SHAPES FILLED

    
    public boolean getItemShapeFilled(int series, int item) {
        return getSeriesShapesFilled(series);
    }

    
    public boolean getSeriesShapesFilled(int series) {
        Boolean flag = this.seriesShapesFilledMap.get(series);
        if (flag != null) {
            return flag;
        }
        return this.defaultShapesFilled;
    }

    
    public void setSeriesShapesFilled(int series, Boolean filled) {
        this.seriesShapesFilledMap.put(series, filled);
        fireChangeEvent();
    }

    
    public void setSeriesShapesFilled(int series, boolean filled) {
        // delegate
        setSeriesShapesFilled(series, Boolean.valueOf(filled));
    }

    
    public boolean getDefaultShapesFilled() {
        return this.defaultShapesFilled;
    }

    
    public void setDefaultShapesFilled(boolean flag) {
        this.defaultShapesFilled = flag;
        fireChangeEvent();
    }

    
    public boolean getUseFillPaint() {
        return this.useFillPaint;
    }

    
    public void setUseFillPaint(boolean flag) {
        this.useFillPaint = flag;
        fireChangeEvent();
    }

    
    public boolean getUseSeriesOffset() {
        return this.useSeriesOffset;
    }

    
    public void setUseSeriesOffset(boolean offset) {
        this.useSeriesOffset = offset;
        fireChangeEvent();
    }

    
    public double getItemMargin() {
        return this.itemMargin;
    }

    
    public void setItemMargin(double margin) {
        if (margin < 0.0 || margin >= 1.0) {
            throw new IllegalArgumentException("Requires 0.0 <= margin < 1.0.");
        }
        this.itemMargin = margin;
        fireChangeEvent();
    }

    
    @Override
    public LegendItem getLegendItem(int datasetIndex, int series) {

        CategoryPlot cp = getPlot();
        if (cp == null) {
            return null;
        }

        if (isSeriesVisible(series) && isSeriesVisibleInLegend(series)) {
            CategoryDataset dataset = cp.getDataset(datasetIndex);
            String label = getLegendItemLabelGenerator().generateLabel(
                    dataset, series);
            String description = label;
            String toolTipText = null;
            if (getLegendItemToolTipGenerator() != null) {
                toolTipText = getLegendItemToolTipGenerator().generateLabel(
                        dataset, series);
            }
            String urlText = null;
            if (getLegendItemURLGenerator() != null) {
                urlText = getLegendItemURLGenerator().generateLabel(
                        dataset, series);
            }
            Shape shape = lookupLegendShape(series);
            Paint paint = lookupSeriesPaint(series);
            Paint fillPaint = (this.useFillPaint
                    ? getItemFillPaint(series, 0) : paint);
            boolean shapeOutlineVisible = this.drawOutlines;
            Paint outlinePaint = (this.useOutlinePaint
                    ? getItemOutlinePaint(series, 0) : paint);
            Stroke outlineStroke = lookupSeriesOutlineStroke(series);
            boolean lineVisible = getItemLineVisible(series, 0);
            boolean shapeVisible = getItemShapeVisible(series, 0);
            LegendItem result = new LegendItem(label, description, toolTipText,
                    urlText, shapeVisible, shape, getItemShapeFilled(series, 0),
                    fillPaint, shapeOutlineVisible, outlinePaint, outlineStroke,
                    lineVisible, new Line2D.Double(-7.0, 0.0, 7.0, 0.0),
                    getItemStroke(series, 0), getItemPaint(series, 0));
            result.setLabelFont(lookupLegendTextFont(series));
            Paint labelPaint = lookupLegendTextPaint(series);
            if (labelPaint != null) {
                result.setLabelPaint(labelPaint);
            }
            result.setDataset(dataset);
            result.setDatasetIndex(datasetIndex);
            result.setSeriesKey(dataset.getRowKey(series));
            result.setSeriesIndex(series);
            return result;
        }
        return null;

    }

    
    @Override
    public int getPassCount() {
        return 2;
    }

    
    @Override
    public void drawItem(Graphics2D g2, CategoryItemRendererState state,
            Rectangle2D dataArea, CategoryPlot plot, CategoryAxis domainAxis,
            ValueAxis rangeAxis, CategoryDataset dataset, int row, int column,
            int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(row, column)) {
            return;
        }

        // do nothing if both the line and shape are not visible
        if (!getItemLineVisible(row, column)
                && !getItemShapeVisible(row, column)) {
            return;
        }

        // nothing is drawn for null...
        Number v = dataset.getValue(row, column);
        if (v == null) {
            return;
        }

        int visibleRow = state.getVisibleSeriesIndex(row);
        if (visibleRow < 0) {
            return;
        }
        int visibleRowCount = state.getVisibleSeriesCount();

        PlotOrientation orientation = plot.getOrientation();

        // current data point...
        double x1;
        if (this.useSeriesOffset) {
            x1 = domainAxis.getCategorySeriesMiddle(column,
                    dataset.getColumnCount(), visibleRow, visibleRowCount,
                    this.itemMargin, dataArea, plot.getDomainAxisEdge());
        }
        else {
            x1 = domainAxis.getCategoryMiddle(column, getColumnCount(),
                    dataArea, plot.getDomainAxisEdge());
        }
        double value = v.doubleValue();
        double y1 = rangeAxis.valueToJava2D(value, dataArea,
                plot.getRangeAxisEdge());

        if (pass == 0 && getItemLineVisible(row, column)) {
            if (column != 0) {
                Number previousValue = dataset.getValue(row, column - 1);
                if (previousValue != null) {
                    // previous data point...
                    double previous = previousValue.doubleValue();
                    double x0;
                    if (this.useSeriesOffset) {
                        x0 = domainAxis.getCategorySeriesMiddle(
                                column - 1, dataset.getColumnCount(),
                                visibleRow, visibleRowCount,
                                this.itemMargin, dataArea,
                                plot.getDomainAxisEdge());
                    }
                    else {
                        x0 = domainAxis.getCategoryMiddle(column - 1,
                                getColumnCount(), dataArea,
                                plot.getDomainAxisEdge());
                    }
                    double y0 = rangeAxis.valueToJava2D(previous, dataArea,
                            plot.getRangeAxisEdge());

                    Line2D line = null;
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        line = new Line2D.Double(y0, x0, y1, x1);
                    }
                    else if (orientation == PlotOrientation.VERTICAL) {
                        line = new Line2D.Double(x0, y0, x1, y1);
                    }
                    g2.setPaint(getItemPaint(row, column));
                    g2.setStroke(getItemStroke(row, column));
                    g2.draw(line);
                }
            }
        }

        if (pass == 1) {
            Shape shape = getItemShape(row, column);
            if (orientation == PlotOrientation.HORIZONTAL) {
                shape = ShapeUtils.createTranslatedShape(shape, y1, x1);
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                shape = ShapeUtils.createTranslatedShape(shape, x1, y1);
            }

            if (getItemShapeVisible(row, column)) {
                if (getItemShapeFilled(row, column)) {
                    if (this.useFillPaint) {
                        g2.setPaint(getItemFillPaint(row, column));
                    }
                    else {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.fill(shape);
                }
                if (this.drawOutlines) {
                    if (this.useOutlinePaint) {
                        g2.setPaint(getItemOutlinePaint(row, column));
                    }
                    else {
                        g2.setPaint(getItemPaint(row, column));
                    }
                    g2.setStroke(getItemOutlineStroke(row, column));
                    g2.draw(shape);
                }
            }

            // draw the item label if there is one...
            if (isItemLabelVisible(row, column)) {
                if (orientation == PlotOrientation.HORIZONTAL) {
                    drawItemLabel(g2, orientation, dataset, row, column, y1,
                            x1, (value < 0.0));
                }
                else if (orientation == PlotOrientation.VERTICAL) {
                    drawItemLabel(g2, orientation, dataset, row, column, x1,
                            y1, (value < 0.0));
                }
            }

            // submit the current data point as a crosshair candidate
            int datasetIndex = plot.indexOf(dataset);
            updateCrosshairValues(state.getCrosshairState(),
                    dataset.getRowKey(row), dataset.getColumnKey(column),
                    value, datasetIndex, x1, y1, orientation);

            // add an item entity, if this information is being collected
            EntityCollection entities = state.getEntityCollection();
            if (entities != null) {
                addItemEntity(entities, dataset, row, column, shape);
            }
        }

    }

    
    @Override
    public boolean equals(Object obj) {

        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LineAndShapeRenderer)) {
            return false;
        }

        LineAndShapeRenderer that = (LineAndShapeRenderer) obj;
        if (this.defaultLinesVisible != that.defaultLinesVisible) {
            return false;
        }
        if (!Objects.equals(this.seriesLinesVisibleMap, that.seriesLinesVisibleMap)) {
            return false;
        }
        if (this.defaultShapesVisible != that.defaultShapesVisible) {
            return false;
        }
        if (!Objects.equals(this.seriesShapesVisibleMap, that.seriesShapesVisibleMap)) {
            return false;
        }
        if (!Objects.equals(this.seriesShapesFilledMap, that.seriesShapesFilledMap)) {
            return false;
        }
        if (this.defaultShapesFilled != that.defaultShapesFilled) {
            return false;
        }
        if (this.useOutlinePaint != that.useOutlinePaint) {
            return false;
        }
        if (this.useSeriesOffset != that.useSeriesOffset) {
            return false;
        }
        if (this.itemMargin != that.itemMargin) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        LineAndShapeRenderer clone = (LineAndShapeRenderer) super.clone();
        clone.seriesLinesVisibleMap = new HashMap<>(this.seriesLinesVisibleMap);
        clone.seriesShapesVisibleMap = new HashMap<>(this.seriesShapesVisibleMap);
        clone.seriesShapesFilledMap = new HashMap<>(this.seriesShapesFilledMap);
        return clone;
    }

}
