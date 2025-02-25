

package org.jfree.chart.plot;

import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.api.Rotation;
import org.jfree.chart.api.TableOrder;
import org.jfree.chart.entity.CategoryItemEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.internal.*;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.urls.CategoryURLGenerator;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.*;


public class SpiderWebPlot extends Plot implements Cloneable, Serializable {

    
    private static final long serialVersionUID = -5376340422031599463L;

    
    public static final double DEFAULT_HEAD = 0.01;

    
    public static final double DEFAULT_AXIS_LABEL_GAP = 0.10;

    
    public static final double DEFAULT_INTERIOR_GAP = 0.25;

    
    public static final double MAX_INTERIOR_GAP = 0.40;

    
    public static final double DEFAULT_START_ANGLE = 90.0;

    
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.PLAIN, 10);

    
    public static final Paint  DEFAULT_LABEL_PAINT = Color.BLACK;

    
    public static final Paint  DEFAULT_LABEL_BACKGROUND_PAINT
            = new Color(255, 255, 192);

    
    public static final Paint  DEFAULT_LABEL_OUTLINE_PAINT = Color.BLACK;

    
    public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE
            = new BasicStroke(0.5f);

    
    public static final Paint  DEFAULT_LABEL_SHADOW_PAINT = Color.LIGHT_GRAY;

    
    public static final double DEFAULT_MAX_VALUE = -1.0;

    
    protected double headPercent;

    
    private double interiorGap;

    
    private double axisLabelGap;

    
    private transient Paint axisLinePaint;

    
    private transient Stroke axisLineStroke;

    
    private CategoryDataset dataset;

    
    private double maxValue;

    
    private TableOrder dataExtractOrder;

    
    private double startAngle;

    
    private Rotation direction;

    
    private transient Shape legendItemShape;

    
    private transient Map<Integer, Paint> seriesPaints;

    
    private transient Paint defaultSeriesPaint;

    
    private transient Map<Integer, Paint> seriesOutlinePaints;

    
    private transient Paint defaultSeriesOutlinePaint;

    
    private transient Map<Integer, Stroke> seriesOutlineStrokes;

    
    private transient Stroke defaultSeriesOutlineStroke;

    
    private Font labelFont;

    
    private transient Paint labelPaint;

    
    private CategoryItemLabelGenerator labelGenerator;

    
    private boolean webFilled = true;

    
    private CategoryToolTipGenerator toolTipGenerator;

    
    private CategoryURLGenerator urlGenerator;

    
    public SpiderWebPlot() {
        this(null);
    }

    
    public SpiderWebPlot(CategoryDataset dataset) {
        this(dataset, TableOrder.BY_ROW);
    }

    
    public SpiderWebPlot(CategoryDataset dataset, TableOrder extract) {
        super();
        Args.nullNotPermitted(extract, "extract");
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        this.dataExtractOrder = extract;
        this.headPercent = DEFAULT_HEAD;
        this.axisLabelGap = DEFAULT_AXIS_LABEL_GAP;
        this.axisLinePaint = Color.BLACK;
        this.axisLineStroke = new BasicStroke(1.0f);

        this.interiorGap = DEFAULT_INTERIOR_GAP;
        this.startAngle = DEFAULT_START_ANGLE;
        this.direction = Rotation.CLOCKWISE;
        this.maxValue = DEFAULT_MAX_VALUE;

        this.seriesPaints = new HashMap<>();
        this.defaultSeriesPaint = null;

        this.seriesOutlinePaints = new HashMap<>();
        this.defaultSeriesOutlinePaint = DEFAULT_OUTLINE_PAINT;

        this.seriesOutlineStrokes = new HashMap<>();
        this.defaultSeriesOutlineStroke = DEFAULT_OUTLINE_STROKE;

        this.labelFont = DEFAULT_LABEL_FONT;
        this.labelPaint = DEFAULT_LABEL_PAINT;
        this.labelGenerator = new StandardCategoryItemLabelGenerator();

        this.legendItemShape = DEFAULT_LEGEND_ITEM_CIRCLE;
    }

    
    @Override
    public String getPlotType() {
        // return localizationResources.getString("Radar_Plot");
        return ("Spider Web Plot");
    }

    
    public CategoryDataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(CategoryDataset dataset) {
        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        if (this.dataset != null) {
            this.dataset.removeChangeListener(this);
        }

        // set the new dataset, and register the chart as a change listener...
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self to trigger plot change event
        datasetChanged(new DatasetChangeEvent(this, dataset));
    }

    
    public boolean isWebFilled() {
        return this.webFilled;
    }

    
    public void setWebFilled(boolean flag) {
        this.webFilled = flag;
        fireChangeEvent();
    }

    
    public TableOrder getDataExtractOrder() {
        return this.dataExtractOrder;
    }

    
    public void setDataExtractOrder(TableOrder order) {
        Args.nullNotPermitted(order, "order");
        this.dataExtractOrder = order;
        fireChangeEvent();
    }

    
    public double getHeadPercent() {
        return this.headPercent;
    }

    
    public void setHeadPercent(double percent) {
        Args.requireNonNegative(percent, "percent");
        this.headPercent = percent;
        fireChangeEvent();
    }

    
    public double getStartAngle() {
        return this.startAngle;
    }

    
    public void setStartAngle(double angle) {
        this.startAngle = angle;
        fireChangeEvent();
    }

    
    public double getMaxValue() {
        return this.maxValue;
    }

    
    public void setMaxValue(double value) {
        this.maxValue = value;
        fireChangeEvent();
    }

    
    public Rotation getDirection() {
        return this.direction;
    }

    
    public void setDirection(Rotation direction) {
        Args.nullNotPermitted(direction, "direction");
        this.direction = direction;
        fireChangeEvent();
    }

    
    public double getInteriorGap() {
        return this.interiorGap;
    }

    
    public void setInteriorGap(double percent) {
        if ((percent < 0.0) || (percent > MAX_INTERIOR_GAP)) {
            throw new IllegalArgumentException(
                    "Percentage outside valid range.");
        }
        if (this.interiorGap != percent) {
            this.interiorGap = percent;
            fireChangeEvent();
        }
    }

    
    public double getAxisLabelGap() {
        return this.axisLabelGap;
    }

    
    public void setAxisLabelGap(double gap) {
        this.axisLabelGap = gap;
        fireChangeEvent();
    }

    
    public Paint getAxisLinePaint() {
        return this.axisLinePaint;
    }

    
    public void setAxisLinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.axisLinePaint = paint;
        fireChangeEvent();
    }

    
    public Stroke getAxisLineStroke() {
        return this.axisLineStroke;
    }

    
    public void setAxisLineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.axisLineStroke = stroke;
        fireChangeEvent();
    }

    //// SERIES PAINT /////////////////////////

    
    public Paint getSeriesPaint(int series) {
        // look up the paint list
        Paint result = this.seriesPaints.get(series);
        if (result == null) {
            DrawingSupplier supplier = getDrawingSupplier();
            if (supplier != null) {
                Paint p = supplier.getNextPaint();
                this.seriesPaints.put(series, p);
                result = p;
            } else {
                result = this.defaultSeriesPaint;
            }
        }
        return result;
    }

    
    public void setSeriesPaint(int series, Paint paint) {
        this.seriesPaints.put(series, paint);
        fireChangeEvent();
    }

    
    public Paint getDefaultSeriesPaint() {
      return this.defaultSeriesPaint;
    }

    
    public void setDefaultSeriesPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.defaultSeriesPaint = paint;
        fireChangeEvent();
    }

    //// SERIES OUTLINE PAINT ////////////////////////////

    
    public Paint getSeriesOutlinePaint(int series) {
        // otherwise look up the paint list
        Paint result = this.seriesOutlinePaints.get(series);
        if (result == null) {
            result = this.defaultSeriesOutlinePaint;
        }
        return result;
    }

    
    public void setSeriesOutlinePaint(int series, Paint paint) {
        this.seriesOutlinePaints.put(series, paint);
        fireChangeEvent();
    }

    
    public Paint getDefaultSeriesOutlinePaint() {
        return this.defaultSeriesOutlinePaint;
    }

    
    public void setDefaultSeriesOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.defaultSeriesOutlinePaint = paint;
        fireChangeEvent();
    }

    //// SERIES OUTLINE STROKE /////////////////////

    
    public Stroke getSeriesOutlineStroke(int series) {
        Stroke result = this.seriesOutlineStrokes.get(series);
        if (result == null) {
            result = this.defaultSeriesOutlineStroke;
        }
        return result;

    }

    
    public void setSeriesOutlineStroke(int series, Stroke stroke) {
        this.seriesOutlineStrokes.put(series, stroke);
        fireChangeEvent();
    }

    
    public Stroke getDefaultSeriesOutlineStroke() {
        return this.defaultSeriesOutlineStroke;
    }

    
    public void setDefaultSeriesOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.defaultSeriesOutlineStroke = stroke;
        fireChangeEvent();
    }

    
    public Shape getLegendItemShape() {
        return this.legendItemShape;
    }

    
    public void setLegendItemShape(Shape shape) {
        Args.nullNotPermitted(shape, "shape");
        this.legendItemShape = shape;
        fireChangeEvent();
    }

    
    public Font getLabelFont() {
        return this.labelFont;
    }

    
    public void setLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.labelFont = font;
        fireChangeEvent();
    }

    
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    
    public void setLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.labelPaint = paint;
        fireChangeEvent();
    }

    
    public CategoryItemLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }

    
    public void setLabelGenerator(CategoryItemLabelGenerator generator) {
        Args.nullNotPermitted(generator, "generator");
        this.labelGenerator = generator;
    }

    
    public CategoryToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }

    
    public void setToolTipGenerator(CategoryToolTipGenerator generator) {
        this.toolTipGenerator = generator;
        fireChangeEvent();
    }

    
    public CategoryURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    
    public void setURLGenerator(CategoryURLGenerator generator) {
        this.urlGenerator = generator;
        fireChangeEvent();
    }

    
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = new LegendItemCollection();
        if (getDataset() == null) {
            return result;
        }
        List keys = null;
        if (this.dataExtractOrder == TableOrder.BY_ROW) {
            keys = this.dataset.getRowKeys();
        }
        else if (this.dataExtractOrder == TableOrder.BY_COLUMN) {
            keys = this.dataset.getColumnKeys();
        }
        if (keys == null) {
            return result;
        }

        int series = 0;
        Iterator iterator = keys.iterator();
        Shape shape = getLegendItemShape();
        while (iterator.hasNext()) {
            Comparable key = (Comparable) iterator.next();
            String label = key.toString();
            String description = label;
            Paint paint = getSeriesPaint(series);
            Paint outlinePaint = getSeriesOutlinePaint(series);
            Stroke stroke = getSeriesOutlineStroke(series);
            LegendItem item = new LegendItem(label, description,
                    null, null, shape, paint, stroke, outlinePaint);
            item.setDataset(getDataset());
            item.setSeriesKey(key);
            item.setSeriesIndex(series);
            result.add(item);
            series++;
        }
        return result;
    }

    
    protected Point2D getWebPoint(Rectangle2D bounds,
                                  double angle, double length) {

        double angrad = Math.toRadians(angle);
        double x = Math.cos(angrad) * length * bounds.getWidth() / 2;
        double y = -Math.sin(angrad) * length * bounds.getHeight() / 2;

        return new Point2D.Double(bounds.getX() + x + bounds.getWidth() / 2,
                bounds.getY() + y + bounds.getHeight() / 2);
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
            PlotState parentState, PlotRenderingInfo info) {

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        if (info != null) {
            info.setPlotArea(area);
            info.setDataArea(area);
        }

        drawBackground(g2, area);
        drawOutline(g2, area);

        Shape savedClip = g2.getClip();

        g2.clip(area);
        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));

        if (!DatasetUtils.isEmptyOrNull(this.dataset)) {
            int seriesCount, catCount;

            if (this.dataExtractOrder == TableOrder.BY_ROW) {
                seriesCount = this.dataset.getRowCount();
                catCount = this.dataset.getColumnCount();
            }
            else {
                seriesCount = this.dataset.getColumnCount();
                catCount = this.dataset.getRowCount();
            }

            // ensure we have a maximum value to use on the axes
            if (this.maxValue == DEFAULT_MAX_VALUE) {
                calculateMaxValue(seriesCount, catCount);
            }

            // Next, setup the plot area

            // adjust the plot area by the interior spacing value

            double gapHorizontal = area.getWidth() * getInteriorGap();
            double gapVertical = area.getHeight() * getInteriorGap();

            double X = area.getX() + gapHorizontal / 2;
            double Y = area.getY() + gapVertical / 2;
            double W = area.getWidth() - gapHorizontal;
            double H = area.getHeight() - gapVertical;

            double headW = area.getWidth() * this.headPercent;
            double headH = area.getHeight() * this.headPercent;

            // make the chart area a square
            double min = Math.min(W, H) / 2;
            X = (X + X + W) / 2 - min;
            Y = (Y + Y + H) / 2 - min;
            W = 2 * min;
            H = 2 * min;

            Point2D  centre = new Point2D.Double(X + W / 2, Y + H / 2);
            Rectangle2D radarArea = new Rectangle2D.Double(X, Y, W, H);

            // draw the axis and category label
            for (int cat = 0; cat < catCount; cat++) {
                double angle = getStartAngle()
                        + (getDirection().getFactor() * cat * 360 / catCount);

                Point2D endPoint = getWebPoint(radarArea, angle, 1);
                                                     // 1 = end of axis
                Line2D  line = new Line2D.Double(centre, endPoint);
                g2.setPaint(this.axisLinePaint);
                g2.setStroke(this.axisLineStroke);
                g2.draw(line);
                drawLabel(g2, radarArea, 0.0, cat, angle, 360.0 / catCount);
            }

            // Now actually plot each of the series polygons..
            for (int series = 0; series < seriesCount; series++) {
                drawRadarPoly(g2, radarArea, centre, info, series, catCount,
                        headH, headW);
            }
        } else {
            drawNoDataMessage(g2, area);
        }
        g2.setClip(savedClip);
        g2.setComposite(originalComposite);
        drawOutline(g2, area);
    }

    
    private void calculateMaxValue(int seriesCount, int catCount) {
        double v;
        Number nV;

        for (int seriesIndex = 0; seriesIndex < seriesCount; seriesIndex++) {
            for (int catIndex = 0; catIndex < catCount; catIndex++) {
                nV = getPlotValue(seriesIndex, catIndex);
                if (nV != null) {
                    v = nV.doubleValue();
                    if (v > this.maxValue) {
                        this.maxValue = v;
                    }
                }
            }
        }
    }

    
    protected void drawRadarPoly(Graphics2D g2, Rectangle2D plotArea,
            Point2D centre, PlotRenderingInfo info, int series, int catCount,
            double headH, double headW) {

        Polygon polygon = new Polygon();

        EntityCollection entities = null;
        if (info != null) {
            entities = info.getOwner().getEntityCollection();
        }

        // plot the data...
        for (int cat = 0; cat < catCount; cat++) {

            Number dataValue = getPlotValue(series, cat);

            if (dataValue != null) {
                double value = dataValue.doubleValue();

                if (value >= 0) { // draw the polygon series...

                    // Finds our starting angle from the centre for this axis

                    double angle = getStartAngle()
                        + (getDirection().getFactor() * cat * 360 / catCount);

                    // The following angle calc will ensure there isn't a top
                    // vertical axis - this may be useful if you don't want any
                    // given criteria to 'appear' move important than the
                    // others..
                    //  + (getDirection().getFactor()
                    //        * (cat + 0.5) * 360 / catCount);

                    // find the point at the appropriate distance end point
                    // along the axis/angle identified above and add it to the
                    // polygon

                    Point2D point = getWebPoint(plotArea, angle,
                            value / this.maxValue);
                    polygon.addPoint((int) point.getX(), (int) point.getY());

                    // put an elipse at the point being plotted..

                    Paint paint = getSeriesPaint(series);
                    Paint outlinePaint = getSeriesOutlinePaint(series);
                    Stroke outlineStroke = getSeriesOutlineStroke(series);

                    Ellipse2D head = new Ellipse2D.Double(point.getX()
                            - headW / 2, point.getY() - headH / 2, headW,
                            headH);
                    g2.setPaint(paint);
                    g2.fill(head);
                    g2.setStroke(outlineStroke);
                    g2.setPaint(outlinePaint);
                    g2.draw(head);

                    if (entities != null) {
                        int row, col;
                        if (this.dataExtractOrder == TableOrder.BY_ROW) {
                            row = series;
                            col = cat;
                        } else {
                            row = cat;
                            col = series;
                        }
                        String tip = null;
                        if (this.toolTipGenerator != null) {
                            tip = this.toolTipGenerator.generateToolTip(
                                    this.dataset, row, col);
                        }

                        String url = null;
                        if (this.urlGenerator != null) {
                            url = this.urlGenerator.generateURL(this.dataset,
                                   row, col);
                        }

                        Shape area = new Rectangle(
                                (int) (point.getX() - headW),
                                (int) (point.getY() - headH),
                                (int) (headW * 2), (int) (headH * 2));
                        CategoryItemEntity entity = new CategoryItemEntity(
                                area, tip, url, this.dataset,
                                this.dataset.getRowKey(row),
                                this.dataset.getColumnKey(col));
                        entities.add(entity);
                    }

                }
            }
        }
        // Plot the polygon

        Paint paint = getSeriesPaint(series);
        g2.setPaint(paint);
        g2.setStroke(getSeriesOutlineStroke(series));
        g2.draw(polygon);

        // Lastly, fill the web polygon if this is required

        if (this.webFilled) {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    0.1f));
            g2.fill(polygon);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getForegroundAlpha()));
        }
    }

    
    protected Number getPlotValue(int series, int cat) {
        Number value = null;
        if (this.dataExtractOrder == TableOrder.BY_ROW) {
            value = this.dataset.getValue(series, cat);
        } else if (this.dataExtractOrder == TableOrder.BY_COLUMN) {
            value = this.dataset.getValue(cat, series);
        }
        return value;
    }

    
    protected void drawLabel(Graphics2D g2, Rectangle2D plotArea, double value,
                             int cat, double startAngle, double extent) {
        FontRenderContext frc = g2.getFontRenderContext();

        String label;
        if (this.dataExtractOrder == TableOrder.BY_ROW) {
            // if series are in rows, then the categories are the column keys
            label = this.labelGenerator.generateColumnLabel(this.dataset, cat);
        } else {
            // if series are in columns, then the categories are the row keys
            label = this.labelGenerator.generateRowLabel(this.dataset, cat);
        }

        Rectangle2D labelBounds = getLabelFont().getStringBounds(label, frc);
        LineMetrics lm = getLabelFont().getLineMetrics(label, frc);
        double ascent = lm.getAscent();

        Point2D labelLocation = calculateLabelLocation(labelBounds, ascent,
                plotArea, startAngle);

        Composite saveComposite = g2.getComposite();

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2.setPaint(getLabelPaint());
        g2.setFont(getLabelFont());
        g2.drawString(label, (float) labelLocation.getX(),
                (float) labelLocation.getY());
        g2.setComposite(saveComposite);
    }

    
    protected Point2D calculateLabelLocation(Rectangle2D labelBounds,
            double ascent, Rectangle2D plotArea, double startAngle) {
        Arc2D arc1 = new Arc2D.Double(plotArea, startAngle, 0, Arc2D.OPEN);
        Point2D point1 = arc1.getEndPoint();

        double deltaX = -(point1.getX() - plotArea.getCenterX())
                        * this.axisLabelGap;
        double deltaY = -(point1.getY() - plotArea.getCenterY())
                        * this.axisLabelGap;

        double labelX = point1.getX() - deltaX;
        double labelY = point1.getY() - deltaY;

        if (labelX < plotArea.getCenterX()) {
            labelX -= labelBounds.getWidth();
        }

        if (labelX == plotArea.getCenterX()) {
            labelX -= labelBounds.getWidth() / 2;
        }

        if (labelY > plotArea.getCenterY()) {
            labelY += ascent;
        }

        return new Point2D.Double(labelX, labelY);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof SpiderWebPlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        SpiderWebPlot that = (SpiderWebPlot) obj;
        if (!this.dataExtractOrder.equals(that.dataExtractOrder)) {
            return false;
        }
        if (this.headPercent != that.headPercent) {
            return false;
        }
        if (this.interiorGap != that.interiorGap) {
            return false;
        }
        if (this.startAngle != that.startAngle) {
            return false;
        }
        if (!this.direction.equals(that.direction)) {
            return false;
        }
        if (this.maxValue != that.maxValue) {
            return false;
        }
        if (this.webFilled != that.webFilled) {
            return false;
        }
        if (this.axisLabelGap != that.axisLabelGap) {
            return false;
        }
        if (!PaintUtils.equal(this.axisLinePaint, that.axisLinePaint)) {
            return false;
        }
        if (!this.axisLineStroke.equals(that.axisLineStroke)) {
            return false;
        }
        if (!ShapeUtils.equal(this.legendItemShape, that.legendItemShape)) {
            return false;
        }
        if (!PaintUtils.equal(this.seriesPaints, that.seriesPaints)) {
            return false;
        }
        if (!PaintUtils.equal(this.defaultSeriesPaint, that.defaultSeriesPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.seriesOutlinePaints, that.seriesOutlinePaints)) {
            return false;
        }
        if (!PaintUtils.equal(this.defaultSeriesOutlinePaint,
                that.defaultSeriesOutlinePaint)) {
            return false;
        }

        if (!this.seriesOutlineStrokes.equals(that.seriesOutlineStrokes)) {
            return false;
        }
        if (!this.defaultSeriesOutlineStroke.equals(that.defaultSeriesOutlineStroke)) {
            return false;
        }
        if (!this.labelFont.equals(that.labelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        if (!this.labelGenerator.equals(that.labelGenerator)) {
            return false;
        }
        if (!Objects.equals(this.toolTipGenerator, that.toolTipGenerator)) {
            return false;
        }
        if (!Objects.equals(this.urlGenerator, that.urlGenerator)) {
            return false;
        }
        return true;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        SpiderWebPlot clone = (SpiderWebPlot) super.clone();
        clone.legendItemShape = CloneUtils.clone(this.legendItemShape);
        clone.seriesPaints = CloneUtils.cloneMapValues(this.seriesPaints);
        clone.seriesOutlinePaints = CloneUtils.cloneMapValues(this.seriesOutlinePaints);
        clone.seriesOutlineStrokes = CloneUtils.cloneMapValues(this.seriesOutlineStrokes);
        return clone;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();

        SerialUtils.writeShape(this.legendItemShape, stream);
        SerialUtils.writeMapOfPaint(this.seriesPaints, stream);
        SerialUtils.writePaint(this.defaultSeriesPaint, stream);
        SerialUtils.writeMapOfPaint(this.seriesOutlinePaints, stream);
        SerialUtils.writePaint(this.defaultSeriesOutlinePaint, stream);
        SerialUtils.writeMapOfStroke(this.seriesOutlineStrokes, stream);
        SerialUtils.writeStroke(this.defaultSeriesOutlineStroke, stream);
        SerialUtils.writePaint(this.labelPaint, stream);
        SerialUtils.writePaint(this.axisLinePaint, stream);
        SerialUtils.writeStroke(this.axisLineStroke, stream);
    }

    
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();

        this.legendItemShape = SerialUtils.readShape(stream);
        this.seriesPaints = SerialUtils.readMapOfPaint(stream);
        this.defaultSeriesPaint = SerialUtils.readPaint(stream);
        this.seriesOutlinePaints = SerialUtils.readMapOfPaint(stream);
        this.defaultSeriesOutlinePaint = SerialUtils.readPaint(stream);
        this.seriesOutlineStrokes = SerialUtils.readMapOfStroke(stream);
        this.defaultSeriesOutlineStroke = SerialUtils.readStroke(stream);
        this.labelPaint = SerialUtils.readPaint(stream);
        this.axisLinePaint = SerialUtils.readPaint(stream);
        this.axisLineStroke = SerialUtils.readStroke(stream);
        if (this.dataset != null) {
            this.dataset.addChangeListener(this);
        }
    }

}
