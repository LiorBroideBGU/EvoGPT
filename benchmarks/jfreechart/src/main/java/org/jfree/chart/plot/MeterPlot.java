

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.ValueDataset;


public class MeterPlot extends Plot implements Serializable, Cloneable {

    
    private static final long serialVersionUID = 2987472457734470962L;

    
    static final Paint DEFAULT_DIAL_BACKGROUND_PAINT = Color.BLACK;

    
    static final Paint DEFAULT_NEEDLE_PAINT = Color.GREEN;

    
    static final Font DEFAULT_VALUE_FONT = new Font("SansSerif", Font.BOLD, 12);

    
    static final Paint DEFAULT_VALUE_PAINT = Color.YELLOW;

    
    public static final int DEFAULT_METER_ANGLE = 270;

    
    public static final float DEFAULT_BORDER_SIZE = 3f;

    
    public static final float DEFAULT_CIRCLE_SIZE = 10f;

    
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.BOLD, 10);

    
    private ValueDataset dataset;

    
    private DialShape shape;

    
    private int meterAngle;

    
    private Range range;

    
    private double tickSize;

    
    private transient Paint tickPaint;

    
    private String units;

    
    private Font valueFont;

    
    private transient Paint valuePaint;

    
    private boolean valueVisible = true;

    
    private boolean drawBorder;

    
    private transient Paint dialOutlinePaint;

    
    private transient Paint dialBackgroundPaint;

    
    private transient Paint needlePaint;

    
    private boolean tickLabelsVisible;

    
    private Font tickLabelFont;

    
    private transient Paint tickLabelPaint;

    
    private NumberFormat tickLabelFormat;

    
    protected static ResourceBundle localizationResources
            = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    
    private List<MeterInterval> intervals;

    
    public MeterPlot() {
        this(null);
    }

    
    public MeterPlot(ValueDataset dataset) {
        super();
        this.shape = DialShape.CIRCLE;
        this.meterAngle = DEFAULT_METER_ANGLE;
        this.range = new Range(0.0, 100.0);
        this.tickSize = 10.0;
        this.tickPaint = Color.WHITE;
        this.units = "Units";
        this.needlePaint = MeterPlot.DEFAULT_NEEDLE_PAINT;
        this.tickLabelsVisible = true;
        this.tickLabelFont = MeterPlot.DEFAULT_LABEL_FONT;
        this.tickLabelPaint = Color.BLACK;
        this.tickLabelFormat = NumberFormat.getInstance();
        this.valueFont = MeterPlot.DEFAULT_VALUE_FONT;
        this.valuePaint = MeterPlot.DEFAULT_VALUE_PAINT;
        this.dialBackgroundPaint = MeterPlot.DEFAULT_DIAL_BACKGROUND_PAINT;
        this.intervals = new ArrayList<>();
        setDataset(dataset);
    }

    
    public DialShape getDialShape() {
        return this.shape;
    }

    
    public void setDialShape(DialShape shape) {
        Args.nullNotPermitted(shape, "shape");
        this.shape = shape;
        fireChangeEvent();
    }

    
    public int getMeterAngle() {
        return this.meterAngle;
    }

    
    public void setMeterAngle(int angle) {
        if (angle < 1 || angle > 360) {
            throw new IllegalArgumentException("Invalid 'angle' (" + angle
                    + ")");
        }
        this.meterAngle = angle;
        fireChangeEvent();
    }

    
    public Range getRange() {
        return this.range;
    }

    
    public void setRange(Range range) {
        Args.nullNotPermitted(range, "range");
        if (!(range.getLength() > 0.0)) {
            throw new IllegalArgumentException(
                    "Range length must be positive.");
        }
        this.range = range;
        fireChangeEvent();
    }

    
    public double getTickSize() {
        return this.tickSize;
    }

    
    public void setTickSize(double size) {
        if (size <= 0) {
            throw new IllegalArgumentException("Requires 'size' > 0.");
        }
        this.tickSize = size;
        fireChangeEvent();
    }

    
    public Paint getTickPaint() {
        return this.tickPaint;
    }

    
    public void setTickPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.tickPaint = paint;
        fireChangeEvent();
    }

    
    public String getUnits() {
        return this.units;
    }

    
    public void setUnits(String units) {
        this.units = units;
        fireChangeEvent();
    }

    
    public Paint getNeedlePaint() {
        return this.needlePaint;
    }

    
    public void setNeedlePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.needlePaint = paint;
        fireChangeEvent();
    }

    
    public boolean getTickLabelsVisible() {
        return this.tickLabelsVisible;
    }

    
    public void setTickLabelsVisible(boolean visible) {
        if (this.tickLabelsVisible != visible) {
            this.tickLabelsVisible = visible;
            fireChangeEvent();
        }
    }

    
    public Font getTickLabelFont() {
        return this.tickLabelFont;
    }

    
    public void setTickLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        if (!this.tickLabelFont.equals(font)) {
            this.tickLabelFont = font;
            fireChangeEvent();
        }
    }

    
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }

    
    public void setTickLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        if (!this.tickLabelPaint.equals(paint)) {
            this.tickLabelPaint = paint;
            fireChangeEvent();
        }
    }

    
    public boolean isValueVisible() {
        return valueVisible;
    }

    
    public void setValueVisible(boolean valueVisible) {
        this.valueVisible = valueVisible;
        fireChangeEvent();
    }

    
    public NumberFormat getTickLabelFormat() {
        return this.tickLabelFormat;
    }

    
    public void setTickLabelFormat(NumberFormat format) {
        Args.nullNotPermitted(format, "format");
        this.tickLabelFormat = format;
        fireChangeEvent();
    }

    
    public Font getValueFont() {
        return this.valueFont;
    }

    
    public void setValueFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.valueFont = font;
        fireChangeEvent();
    }

    
    public Paint getValuePaint() {
        return this.valuePaint;
    }

    
    public void setValuePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.valuePaint = paint;
        fireChangeEvent();
    }

    
    public Paint getDialBackgroundPaint() {
        return this.dialBackgroundPaint;
    }

    
    public void setDialBackgroundPaint(Paint paint) {
        this.dialBackgroundPaint = paint;
        fireChangeEvent();
    }

    
    public boolean getDrawBorder() {
        return this.drawBorder;
    }

    
    public void setDrawBorder(boolean draw) {
        // TODO: fix output when this flag is set to true
        this.drawBorder = draw;
        fireChangeEvent();
    }

    
    public Paint getDialOutlinePaint() {
        return this.dialOutlinePaint;
    }

    
    public void setDialOutlinePaint(Paint paint) {
        this.dialOutlinePaint = paint;
        fireChangeEvent();
    }

    
    public ValueDataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(ValueDataset dataset) {

        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        ValueDataset existing = this.dataset;
        if (existing != null) {
            existing.removeChangeListener(this);
        }

        // set the new dataset, and register the chart as a change listener...
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);

    }

    
    public List<MeterInterval> getIntervals() {
        return Collections.unmodifiableList(intervals);
    }

    
    public void addInterval(MeterInterval interval) {
        Args.nullNotPermitted(interval, "interval");
        intervals.add(interval);
        fireChangeEvent();
    }

    
    public void clearIntervals() {
        intervals.clear();
        fireChangeEvent();
    }

    
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = new LegendItemCollection();
        for (MeterInterval mi : intervals) {
            Paint color = mi.getBackgroundPaint();
            if (color == null) {
                color = mi.getOutlinePaint();
            }
            LegendItem item = new LegendItem(mi.getLabel(), mi.getLabel(),
                    null, null, new Rectangle2D.Double(-4.0, -4.0, 8.0, 8.0),
                    color);
            item.setDataset(getDataset());
            result.add(item);
        }
        return result;
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
                     PlotState parentState, PlotRenderingInfo info) {

        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        area.setRect(area.getX() + 4, area.getY() + 4, area.getWidth() - 8,
                area.getHeight() - 8);

        // draw the background
        if (this.drawBorder) {
            drawBackground(g2, area);
        }

        // adjust the plot area by the interior spacing value
        double gapHorizontal = (2 * DEFAULT_BORDER_SIZE);
        double gapVertical = (2 * DEFAULT_BORDER_SIZE);
        double meterX = area.getX() + gapHorizontal / 2;
        double meterY = area.getY() + gapVertical / 2;
        double meterW = area.getWidth() - gapHorizontal;
        double meterH = area.getHeight() - gapVertical
                + ((this.meterAngle <= 180) && (this.shape != DialShape.CIRCLE)
                ? area.getHeight() / 1.25 : 0);

        double min = Math.min(meterW, meterH) / 2;
        meterX = (meterX + meterX + meterW) / 2 - min;
        meterY = (meterY + meterY + meterH) / 2 - min;
        meterW = 2 * min;
        meterH = 2 * min;

        Rectangle2D meterArea = new Rectangle2D.Double(meterX, meterY, meterW,
                meterH);

        Rectangle2D.Double originalArea = new Rectangle2D.Double(
                meterArea.getX() - 4, meterArea.getY() - 4,
                meterArea.getWidth() + 8, meterArea.getHeight() + 8);

        double meterMiddleX = meterArea.getCenterX();
        double meterMiddleY = meterArea.getCenterY();

        // plot the data (unless the dataset is null)...
        ValueDataset data = getDataset();
        if (data != null) {
            double dataMin = this.range.getLowerBound();
            double dataMax = this.range.getUpperBound();

            Shape savedClip = g2.getClip();
            g2.clip(originalArea);
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    getForegroundAlpha()));

            if (this.dialBackgroundPaint != null) {
                fillArc(g2, originalArea, dataMin, dataMax,
                        this.dialBackgroundPaint, true);
            }
            drawTicks(g2, meterArea, dataMin, dataMax);
            drawArcForInterval(g2, meterArea, new MeterInterval("", this.range,
                    this.dialOutlinePaint, new BasicStroke(1.0f), null));

            for (MeterInterval interval : this.intervals) {
                drawArcForInterval(g2, meterArea, interval);
            }

            Number n = data.getValue();
            if (n != null) {
                double value = n.doubleValue();
                drawValueLabel(g2, meterArea);

                if (this.range.contains(value)) {
                    g2.setPaint(this.needlePaint);
                    g2.setStroke(new BasicStroke(2.0f));

                    double radius = (meterArea.getWidth() / 2)
                                    + DEFAULT_BORDER_SIZE + 15;
                    double valueAngle = valueToAngle(value);
                    double valueP1 = meterMiddleX
                            + (radius * Math.cos(Math.PI * (valueAngle / 180)));
                    double valueP2 = meterMiddleY
                            - (radius * Math.sin(Math.PI * (valueAngle / 180)));

                    Polygon arrow = new Polygon();
                    if ((valueAngle > 135 && valueAngle < 225)
                        || (valueAngle < 45 && valueAngle > -45)) {

                        double valueP3 = (meterMiddleY
                                - DEFAULT_CIRCLE_SIZE / 4);
                        double valueP4 = (meterMiddleY
                                + DEFAULT_CIRCLE_SIZE / 4);
                        arrow.addPoint((int) meterMiddleX, (int) valueP3);
                        arrow.addPoint((int) meterMiddleX, (int) valueP4);

                    }
                    else {
                        arrow.addPoint((int) (meterMiddleX
                                - DEFAULT_CIRCLE_SIZE / 4), (int) meterMiddleY);
                        arrow.addPoint((int) (meterMiddleX
                                + DEFAULT_CIRCLE_SIZE / 4), (int) meterMiddleY);
                    }
                    arrow.addPoint((int) valueP1, (int) valueP2);
                    g2.fill(arrow);

                    Ellipse2D circle = new Ellipse2D.Double(meterMiddleX
                            - DEFAULT_CIRCLE_SIZE / 2, meterMiddleY
                            - DEFAULT_CIRCLE_SIZE / 2, DEFAULT_CIRCLE_SIZE,
                            DEFAULT_CIRCLE_SIZE);
                    g2.fill(circle);
                }
            }

            g2.setClip(savedClip);
            g2.setComposite(originalComposite);

        }
        if (this.drawBorder) {
            drawOutline(g2, area);
        }

    }

    
    protected void drawArcForInterval(Graphics2D g2, Rectangle2D meterArea,
                                      MeterInterval interval) {

        double minValue = interval.getRange().getLowerBound();
        double maxValue = interval.getRange().getUpperBound();
        Paint outlinePaint = interval.getOutlinePaint();
        Stroke outlineStroke = interval.getOutlineStroke();
        Paint backgroundPaint = interval.getBackgroundPaint();

        if (backgroundPaint != null) {
            fillArc(g2, meterArea, minValue, maxValue, backgroundPaint, false);
        }
        if (outlinePaint != null) {
            if (outlineStroke != null) {
                drawArc(g2, meterArea, minValue, maxValue, outlinePaint,
                        outlineStroke);
            }
            drawTick(g2, meterArea, minValue, true);
            drawTick(g2, meterArea, maxValue, true);
        }
    }

    
    protected void drawArc(Graphics2D g2, Rectangle2D area, double minValue,
                           double maxValue, Paint paint, Stroke stroke) {

        double startAngle = valueToAngle(maxValue);
        double endAngle = valueToAngle(minValue);
        double extent = endAngle - startAngle;

        double x = area.getX();
        double y = area.getY();
        double w = area.getWidth();
        double h = area.getHeight();
        g2.setPaint(paint);
        g2.setStroke(stroke);

        if (paint != null && stroke != null) {
            Arc2D.Double arc = new Arc2D.Double(x, y, w, h, startAngle,
                    extent, Arc2D.OPEN);
            g2.setPaint(paint);
            g2.setStroke(stroke);
            g2.draw(arc);
        }

    }

    
    protected void fillArc(Graphics2D g2, Rectangle2D area,
            double minValue, double maxValue, Paint paint, boolean dial) {

        Args.nullNotPermitted(paint, "paint");
        double startAngle = valueToAngle(maxValue);
        double endAngle = valueToAngle(minValue);
        double extent = endAngle - startAngle;

        double x = area.getX();
        double y = area.getY();
        double w = area.getWidth();
        double h = area.getHeight();
        int joinType = Arc2D.OPEN;
        if (this.shape == DialShape.PIE) {
            joinType = Arc2D.PIE;
        }
        else if (this.shape == DialShape.CHORD) {
            if (dial && this.meterAngle > 180) {
                joinType = Arc2D.CHORD;
            }
            else {
                joinType = Arc2D.PIE;
            }
        }
        else if (this.shape == DialShape.CIRCLE) {
            joinType = Arc2D.PIE;
            if (dial) {
                extent = 360;
            }
        }
        else {
            throw new IllegalStateException("DialShape not recognised.");
        }

        g2.setPaint(paint);
        Arc2D.Double arc = new Arc2D.Double(x, y, w, h, startAngle, extent,
                joinType);
        g2.fill(arc);
    }

    
    public double valueToAngle(double value) {
        value = value - this.range.getLowerBound();
        double baseAngle = 180 + ((this.meterAngle - 180) / 2.0);
        return baseAngle - ((value / this.range.getLength()) * this.meterAngle);
    }

    
    protected void drawTicks(Graphics2D g2, Rectangle2D meterArea,
                             double minValue, double maxValue) {
        for (double v = minValue; v <= maxValue; v += this.tickSize) {
            drawTick(g2, meterArea, v);
        }
    }

    
    protected void drawTick(Graphics2D g2, Rectangle2D meterArea,
            double value) {
        drawTick(g2, meterArea, value, false);
    }

    
    protected void drawTick(Graphics2D g2, Rectangle2D meterArea,
                            double value, boolean label) {

        double valueAngle = valueToAngle(value);

        double meterMiddleX = meterArea.getCenterX();
        double meterMiddleY = meterArea.getCenterY();

        g2.setPaint(this.tickPaint);
        g2.setStroke(new BasicStroke(2.0f));

        double valueP2X;
        double valueP2Y;

        double radius = (meterArea.getWidth() / 2) + DEFAULT_BORDER_SIZE;
        double radius1 = radius - 15;

        double valueP1X = meterMiddleX
                + (radius * Math.cos(Math.PI * (valueAngle / 180)));
        double valueP1Y = meterMiddleY
                - (radius * Math.sin(Math.PI * (valueAngle / 180)));

        valueP2X = meterMiddleX
                + (radius1 * Math.cos(Math.PI * (valueAngle / 180)));
        valueP2Y = meterMiddleY
                - (radius1 * Math.sin(Math.PI * (valueAngle / 180)));

        Line2D.Double line = new Line2D.Double(valueP1X, valueP1Y, valueP2X,
                valueP2Y);
        g2.draw(line);

        if (this.tickLabelsVisible && label) {

            String tickLabel =  this.tickLabelFormat.format(value);
            g2.setFont(this.tickLabelFont);
            g2.setPaint(this.tickLabelPaint);

            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D tickLabelBounds
                = TextUtils.getTextBounds(tickLabel, g2, fm);

            double x = valueP2X;
            double y = valueP2Y;
            if (valueAngle == 90 || valueAngle == 270) {
                x = x - tickLabelBounds.getWidth() / 2;
            }
            else if (valueAngle < 90 || valueAngle > 270) {
                x = x - tickLabelBounds.getWidth();
            }
            if ((valueAngle > 135 && valueAngle < 225)
                    || valueAngle > 315 || valueAngle < 45) {
                y = y - tickLabelBounds.getHeight() / 2;
            }
            else {
                y = y + tickLabelBounds.getHeight() / 2;
            }
            g2.drawString(tickLabel, (float) x, (float) y);
        }
    }

    
    protected void drawValueLabel(Graphics2D g2, Rectangle2D area) {
        if (valueVisible) {
            g2.setFont(this.valueFont);
            g2.setPaint(this.valuePaint);
            String valueStr = "No value";
            if (this.dataset != null) {
                Number n = this.dataset.getValue();
                if (n != null) {
                    valueStr = this.tickLabelFormat.format(n.doubleValue()) + " "
                        + this.units;
                }
            }
            float x = (float) area.getCenterX();
            float y = (float) area.getCenterY() + DEFAULT_CIRCLE_SIZE;
            TextUtils.drawAlignedString(valueStr, g2, x, y,
                TextAnchor.TOP_CENTER);
        }
    }

    
    @Override
    public String getPlotType() {
        return localizationResources.getString("Meter_Plot");
    }

    
    @Override
    public void zoom(double percent) {
        // intentionally blank
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MeterPlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        MeterPlot that = (MeterPlot) obj;
        if (!Objects.equals(this.units, that.units)) {
            return false;
        }
        if (!Objects.equals(this.range, that.range)) {
            return false;
        }
        if (!Objects.equals(this.intervals, that.intervals)) {
            return false;
        }
        if (!PaintUtils.equal(this.dialOutlinePaint,
                that.dialOutlinePaint)) {
            return false;
        }
        if (this.shape != that.shape) {
            return false;
        }
        if (!PaintUtils.equal(this.dialBackgroundPaint,
                that.dialBackgroundPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.needlePaint, that.needlePaint)) {
            return false;
        }
        if (this.valueVisible != that.valueVisible) {
            return false;
        }
        if (!Objects.equals(this.valueFont, that.valueFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.valuePaint, that.valuePaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.tickPaint, that.tickPaint)) {
            return false;
        }
        if (this.tickSize != that.tickSize) {
            return false;
        }
        if (this.tickLabelsVisible != that.tickLabelsVisible) {
            return false;
        }
        if (!Objects.equals(this.tickLabelFont, that.tickLabelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
        }
        if (!Objects.equals(this.tickLabelFormat, that.tickLabelFormat)) {
            return false;
        }
        if (this.drawBorder != that.drawBorder) {
            return false;
        }
        if (this.meterAngle != that.meterAngle) {
            return false;
        }
        return true;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.dialBackgroundPaint, stream);
        SerialUtils.writePaint(this.dialOutlinePaint, stream);
        SerialUtils.writePaint(this.needlePaint, stream);
        SerialUtils.writePaint(this.valuePaint, stream);
        SerialUtils.writePaint(this.tickPaint, stream);
        SerialUtils.writePaint(this.tickLabelPaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.dialBackgroundPaint = SerialUtils.readPaint(stream);
        this.dialOutlinePaint = SerialUtils.readPaint(stream);
        this.needlePaint = SerialUtils.readPaint(stream);
        this.valuePaint = SerialUtils.readPaint(stream);
        this.tickPaint = SerialUtils.readPaint(stream);
        this.tickLabelPaint = SerialUtils.readPaint(stream);
        if (this.dataset != null) {
            this.dataset.addChangeListener(this);
        }
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        MeterPlot clone = (MeterPlot) super.clone();
        clone.tickLabelFormat = (NumberFormat) this.tickLabelFormat.clone();
        // the following relies on the fact that the intervals are immutable
        clone.intervals = new ArrayList<>(this.intervals);
        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }
        return clone;
    }

}
