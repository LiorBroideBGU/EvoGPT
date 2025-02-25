

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.labels.ItemLabelAnchor;
import org.jfree.chart.labels.ItemLabelPosition;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.text.TextAnchor;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.TableXYDataset;
import org.jfree.data.xy.XYDataset;


public class StackedXYBarRenderer extends XYBarRenderer {

    
    private static final long serialVersionUID = -7049101055533436444L;

    
    private boolean renderAsPercentages;

    
    public StackedXYBarRenderer() {
        this(0.0);
    }

    
    public StackedXYBarRenderer(double margin) {
        super(margin);
        this.renderAsPercentages = false;

        // set the default item label positions, which will only be used if
        // the user requests visible item labels...
        ItemLabelPosition p = new ItemLabelPosition(ItemLabelAnchor.CENTER,
                TextAnchor.CENTER);
        setDefaultPositiveItemLabelPosition(p);
        setDefaultNegativeItemLabelPosition(p);
        setPositiveItemLabelPositionFallback(null);
        setNegativeItemLabelPositionFallback(null);
    }

    
    public boolean getRenderAsPercentages() {
        return this.renderAsPercentages;
    }

    
    public void setRenderAsPercentages(boolean asPercentages) {
        this.renderAsPercentages = asPercentages;
        fireChangeEvent();
    }

    
    @Override
    public int getPassCount() {
        return 3;
    }

    
    @Override
    public XYItemRendererState initialise(Graphics2D g2, Rectangle2D dataArea,
            XYPlot plot, XYDataset data, PlotRenderingInfo info) {
        return new XYBarRendererState(info);
    }

    
    @Override
    public Range findRangeBounds(XYDataset dataset) {
        if (dataset != null) {
            if (this.renderAsPercentages) {
                return new Range(0.0, 1.0);
            } else {
                return DatasetUtils.findStackedRangeBounds(
                        (TableXYDataset) dataset);
            }
        } else {
            return null;
        }
    }

    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        if (!getItemVisible(series, item)) {
            return;
        }

        if (!(dataset instanceof IntervalXYDataset
                && dataset instanceof TableXYDataset)) {
            String message = "dataset (type " + dataset.getClass().getName()
                + ") has wrong type:";
            boolean and = false;
            if (!IntervalXYDataset.class.isAssignableFrom(dataset.getClass())) {
                message += " it is no IntervalXYDataset";
                and = true;
            }
            if (!TableXYDataset.class.isAssignableFrom(dataset.getClass())) {
                if (and) {
                    message += " and";
                }
                message += " it is no TableXYDataset";
            }

            throw new IllegalArgumentException(message);
        }

        IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;
        double value = intervalDataset.getYValue(series, item);
        if (Double.isNaN(value)) {
            return;
        }

        // if we are rendering the values as percentages, we need to calculate
        // the total for the current item.  Unfortunately here we end up
        // repeating the calculation more times than is strictly necessary -
        // hopefully I'll come back to this and find a way to add the
        // total(s) to the renderer state.  The other problem is we implicitly
        // assume the dataset has no negative values...perhaps that can be
        // fixed too.
        double total = 0.0;
        if (this.renderAsPercentages) {
            total = DatasetUtils.calculateStackTotal(
                    (TableXYDataset) dataset, item);
            value = value / total;
        }

        double positiveBase = 0.0;
        double negativeBase = 0.0;

        for (int i = 0; i < series; i++) {
            double v = dataset.getYValue(i, item);
            if (!Double.isNaN(v) && isSeriesVisible(i)) {
                if (this.renderAsPercentages) {
                    v = v / total;
                }
                if (v > 0) {
                    positiveBase = positiveBase + v;
                } else {
                    negativeBase = negativeBase + v;
                }
            }
        }

        double translatedBase;
        double translatedValue;
        RectangleEdge edgeR = plot.getRangeAxisEdge();
        if (value > 0.0) {
            translatedBase = rangeAxis.valueToJava2D(positiveBase, dataArea,
                    edgeR);
            translatedValue = rangeAxis.valueToJava2D(positiveBase + value,
                    dataArea, edgeR);
        } else {
            translatedBase = rangeAxis.valueToJava2D(negativeBase, dataArea,
                    edgeR);
            translatedValue = rangeAxis.valueToJava2D(negativeBase + value,
                    dataArea, edgeR);
        }

        RectangleEdge edgeD = plot.getDomainAxisEdge();
        double startX = intervalDataset.getStartXValue(series, item);
        if (Double.isNaN(startX)) {
            return;
        }
        double translatedStartX = domainAxis.valueToJava2D(startX, dataArea,
                edgeD);

        double endX = intervalDataset.getEndXValue(series, item);
        if (Double.isNaN(endX)) {
            return;
        }
        double translatedEndX = domainAxis.valueToJava2D(endX, dataArea, edgeD);

        double translatedWidth = Math.max(1, Math.abs(translatedEndX
                - translatedStartX));
        double translatedHeight = Math.abs(translatedValue - translatedBase);
        if (getMargin() > 0.0) {
            double cut = translatedWidth * getMargin();
            translatedWidth = translatedWidth - cut;
            translatedStartX = translatedStartX + cut / 2;
        }

        Rectangle2D bar = null;
        PlotOrientation orientation = plot.getOrientation();
        if (orientation == PlotOrientation.HORIZONTAL) {
            bar = new Rectangle2D.Double(Math.min(translatedBase,
                    translatedValue), Math.min(translatedEndX,
                    translatedStartX), translatedHeight, translatedWidth);
        } else if (orientation == PlotOrientation.VERTICAL) {
            bar = new Rectangle2D.Double(Math.min(translatedStartX,
                    translatedEndX), Math.min(translatedBase, translatedValue),
                    translatedWidth, translatedHeight);
        } else {
            throw new IllegalStateException();
        }
        boolean positive = (value > 0.0);
        boolean inverted = rangeAxis.isInverted();
        RectangleEdge barBase;
        if (orientation == PlotOrientation.HORIZONTAL) {
            if (positive && inverted || !positive && !inverted) {
                barBase = RectangleEdge.RIGHT;
            } else {
                barBase = RectangleEdge.LEFT;
            }
        } else {
            if (positive && !inverted || !positive && inverted) {
                barBase = RectangleEdge.BOTTOM;
            } else {
                barBase = RectangleEdge.TOP;
            }
        }

        if (pass == 0) {
            if (getShadowsVisible()) {
                getBarPainter().paintBarShadow(g2, this, series, item, bar,
                        barBase, false);
            }
        } else if (pass == 1) {
            getBarPainter().paintBar(g2, this, series, item, bar, barBase);

            // add an entity for the item...
            if (info != null) {
                EntityCollection entities = info.getOwner()
                        .getEntityCollection();
                if (entities != null) {
                    addEntity(entities, bar, dataset, series, item,
                            bar.getCenterX(), bar.getCenterY());
                }
            }
        } else if (pass == 2) {
            // handle item label drawing, now that we know all the bars have
            // been drawn...
            if (isItemLabelVisible(series, item)) {
                XYItemLabelGenerator generator = getItemLabelGenerator(series,
                        item);
                drawItemLabel(g2, dataset, series, item, plot, generator, bar,
                        value < 0.0);
            }
        }

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StackedXYBarRenderer)) {
            return false;
        }
        StackedXYBarRenderer that = (StackedXYBarRenderer) obj;
        if (this.renderAsPercentages != that.renderAsPercentages) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = result * 37 + (this.renderAsPercentages ? 1 : 0);
        return result;
    }

}
