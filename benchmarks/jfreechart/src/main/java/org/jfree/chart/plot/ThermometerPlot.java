

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Objects;
import java.util.ResourceBundle;
import org.jfree.chart.ChartElementVisitor;

import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.api.UnitType;
import org.jfree.data.Range;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.general.ValueDataset;


public class ThermometerPlot extends Plot implements ValueAxisPlot,
        Zoomable, Cloneable, Serializable {

    
    private static final long serialVersionUID = 4087093313147984390L;

    
    public static final int UNITS_NONE = 0;

    
    public static final int UNITS_FAHRENHEIT = 1;

    
    public static final int UNITS_CELCIUS = 2;

    
    public static final int UNITS_KELVIN = 3;

    
    public static final int NONE = 0;

    
    public static final int RIGHT = 1;

    
    public static final int LEFT = 2;

    
    public static final int BULB = 3;

    
    public static final int NORMAL = 0;

    
    public static final int WARNING = 1;

    
    public static final int CRITICAL = 2;

    
    protected static final int AXIS_GAP = 10;

    
    protected static final String[] UNITS = {"", "\u00B0F", "\u00B0C",
            "\u00B0K"};

    
    protected static final int RANGE_LOW = 0;

    
    protected static final int RANGE_HIGH = 1;

    
    protected static final int DISPLAY_LOW = 2;

    
    protected static final int DISPLAY_HIGH = 3;

    
    protected static final double DEFAULT_LOWER_BOUND = 0.0;

    
    protected static final double DEFAULT_UPPER_BOUND = 100.0;

    
    protected static final int DEFAULT_BULB_RADIUS = 40;

    
    protected static final int DEFAULT_COLUMN_RADIUS = 20;

    
    protected static final int DEFAULT_GAP = 5;

    
    private ValueDataset dataset;

    
    private ValueAxis rangeAxis;

    
    private double lowerBound = DEFAULT_LOWER_BOUND;

    
    private double upperBound = DEFAULT_UPPER_BOUND;

    
    private int bulbRadius = DEFAULT_BULB_RADIUS;

    
    private int columnRadius = DEFAULT_COLUMN_RADIUS;

    
    private int gap = DEFAULT_GAP;

    
    private RectangleInsets padding;

    
    private transient Stroke thermometerStroke = new BasicStroke(1.0f);

    
    private transient Paint thermometerPaint = Color.BLACK;

    
    private int units = UNITS_CELCIUS;

    
    private int valueLocation = BULB;

    
    private int axisLocation = LEFT;

    
    private Font valueFont = new Font("SansSerif", Font.BOLD, 16);

    
    private transient Paint valuePaint = Color.WHITE;

    
    private NumberFormat valueFormat = new DecimalFormat();

    
    private transient Paint mercuryPaint = Color.LIGHT_GRAY;

    
    private boolean showValueLines = false;

    
    private int subrange = -1;

    
    private double[][] subrangeInfo = {
        {0.0, 50.0, 0.0, 50.0},
        {50.0, 75.0, 50.0, 75.0},
        {75.0, 100.0, 75.0, 100.0}
    };

    
    private boolean followDataInSubranges = false;

    
    private boolean useSubrangePaint = true;

    
    private transient Paint[] subrangePaint = {Color.GREEN, Color.ORANGE,
            Color.RED};

    
    private boolean subrangeIndicatorsVisible = true;

    
    private transient Stroke subrangeIndicatorStroke = new BasicStroke(2.0f);

    
    private transient Stroke rangeIndicatorStroke = new BasicStroke(3.0f);

    
    protected static ResourceBundle localizationResources
            = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    
    public ThermometerPlot() {
        this(new DefaultValueDataset());
    }

    
    public ThermometerPlot(ValueDataset dataset) {

        super();

        this.padding = new RectangleInsets(UnitType.RELATIVE, 0.05, 0.05, 0.05,
                0.05);
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        NumberAxis axis = new NumberAxis(null);
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        axis.setAxisLineVisible(false);
        axis.setPlot(this);
        axis.addChangeListener(this);
        this.rangeAxis = axis;
        setAxisRange();
    }

    
    public ValueDataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(ValueDataset dataset) {

        // if there is an existing dataset, remove the plot from the list
        // of change listeners...
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

    
    public ValueAxis getRangeAxis() {
        return this.rangeAxis;
    }

    
    public void setRangeAxis(ValueAxis axis) {
        Args.nullNotPermitted(axis, "axis");
        // plot is registered as a listener with the existing axis...
        this.rangeAxis.removeChangeListener(this);

        axis.setPlot(this);
        axis.addChangeListener(this);
        this.rangeAxis = axis;
        fireChangeEvent();
    }

    
    public double getLowerBound() {
        return this.lowerBound;
    }

    
    public void setLowerBound(double lower) {
        this.lowerBound = lower;
        setAxisRange();
    }

    
    public double getUpperBound() {
        return this.upperBound;
    }

    
    public void setUpperBound(double upper) {
        this.upperBound = upper;
        setAxisRange();
    }

    
    public void setRange(double lower, double upper) {
        this.lowerBound = lower;
        this.upperBound = upper;
        setAxisRange();
    }

    
    public RectangleInsets getPadding() {
        return this.padding;
    }

    
    public void setPadding(RectangleInsets padding) {
        Args.nullNotPermitted(padding, "padding");
        this.padding = padding;
        fireChangeEvent();
    }

    
    public Stroke getThermometerStroke() {
        return this.thermometerStroke;
    }

    
    public void setThermometerStroke(Stroke s) {
        if (s != null) {
            this.thermometerStroke = s;
            fireChangeEvent();
        }
    }

    
    public Paint getThermometerPaint() {
        return this.thermometerPaint;
    }

    
    public void setThermometerPaint(Paint paint) {
        if (paint != null) {
            this.thermometerPaint = paint;
            fireChangeEvent();
        }
    }

    
    public int getUnits() {
        return this.units;
    }

    
    public void setUnits(int u) {
        if ((u >= 0) && (u < UNITS.length)) {
            if (this.units != u) {
                this.units = u;
                fireChangeEvent();
            }
        }
    }

    
    public int getValueLocation() {
        return this.valueLocation;
    }

    
    public void setValueLocation(int location) {
        if ((location >= 0) && (location < 4)) {
            this.valueLocation = location;
            fireChangeEvent();
        }
        else {
            throw new IllegalArgumentException("Location not recognised.");
        }
    }

    
    public int getAxisLocation() {
        return this.axisLocation;
    }

    
    public void setAxisLocation(int location) {
        if ((location >= 0) && (location < 3)) {
            this.axisLocation = location;
            fireChangeEvent();
        }
        else {
            throw new IllegalArgumentException("Location not recognised.");
        }
    }

    
    public Font getValueFont() {
        return this.valueFont;
    }

    
    public void setValueFont(Font f) {
        Args.nullNotPermitted(f, "f");
        if (!this.valueFont.equals(f)) {
            this.valueFont = f;
            fireChangeEvent();
        }
    }

    
    public Paint getValuePaint() {
        return this.valuePaint;
    }

    
    public void setValuePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        if (!this.valuePaint.equals(paint)) {
            this.valuePaint = paint;
            fireChangeEvent();
        }
    }

    // FIXME: No getValueFormat() method?

    
    public void setValueFormat(NumberFormat formatter) {
        Args.nullNotPermitted(formatter, "formatter");
        this.valueFormat = formatter;
        fireChangeEvent();
    }

    
    public Paint getMercuryPaint() {
        return this.mercuryPaint;
    }

    
    public void setMercuryPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.mercuryPaint = paint;
        fireChangeEvent();
    }

    
    public void setSubrangeInfo(int range, double low, double hi) {
        setSubrangeInfo(range, low, hi, low, hi);
    }

    
    public void setSubrangeInfo(int range,
                                double rangeLow, double rangeHigh,
                                double displayLow, double displayHigh) {

        if ((range >= 0) && (range < 3)) {
            setSubrange(range, rangeLow, rangeHigh);
            setDisplayRange(range, displayLow, displayHigh);
            setAxisRange();
            fireChangeEvent();
        }

    }

    
    public void setSubrange(int range, double low, double high) {
        if ((range >= 0) && (range < 3)) {
            this.subrangeInfo[range][RANGE_HIGH] = high;
            this.subrangeInfo[range][RANGE_LOW] = low;
        }
    }

    
    public void setDisplayRange(int range, double low, double high) {

        if ((range >= 0) && (range < this.subrangeInfo.length)
            && isValidNumber(high) && isValidNumber(low)) {

            if (high > low) {
                this.subrangeInfo[range][DISPLAY_HIGH] = high;
                this.subrangeInfo[range][DISPLAY_LOW] = low;
            }
            else {
                this.subrangeInfo[range][DISPLAY_HIGH] = low;
                this.subrangeInfo[range][DISPLAY_LOW] = high;
            }

        }

    }

    
    public Paint getSubrangePaint(int range) {
        if ((range >= 0) && (range < this.subrangePaint.length)) {
            return this.subrangePaint[range];
        }
        else {
            return this.mercuryPaint;
        }
    }

    
    public void setSubrangePaint(int range, Paint paint) {
        if ((range >= 0)
                && (range < this.subrangePaint.length) && (paint != null)) {
            this.subrangePaint[range] = paint;
            fireChangeEvent();
        }
    }

    
    public boolean getFollowDataInSubranges() {
        return this.followDataInSubranges;
    }

    
    public void setFollowDataInSubranges(boolean flag) {
        this.followDataInSubranges = flag;
        fireChangeEvent();
    }

    
    public boolean getUseSubrangePaint() {
        return this.useSubrangePaint;
    }

    
    public void setUseSubrangePaint(boolean flag) {
        this.useSubrangePaint = flag;
        fireChangeEvent();
    }

    
    public int getBulbRadius() {
        return this.bulbRadius;
    }

    
    public void setBulbRadius(int r) {
        this.bulbRadius = r;
        fireChangeEvent();
    }

    
    public int getBulbDiameter() {
        return getBulbRadius() * 2;
    }

    
    public int getColumnRadius() {
        return this.columnRadius;
    }

    
    public void setColumnRadius(int r) {
        this.columnRadius = r;
        fireChangeEvent();
    }

    
    public int getColumnDiameter() {
        return getColumnRadius() * 2;
    }

    
    public int getGap() {
        return this.gap;
    }

    
    public void setGap(int gap) {
        this.gap = gap;
        fireChangeEvent();
    }

    
    @Override
    public void receive(ChartElementVisitor visitor) {
        this.rangeAxis.receive(visitor);
        super.receive(visitor);
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
            PlotState parentState, PlotRenderingInfo info) {

        RoundRectangle2D outerStem = new RoundRectangle2D.Double();
        RoundRectangle2D innerStem = new RoundRectangle2D.Double();
        RoundRectangle2D mercuryStem = new RoundRectangle2D.Double();
        Ellipse2D outerBulb = new Ellipse2D.Double();
        Ellipse2D innerBulb = new Ellipse2D.Double();
        String temp;
        FontMetrics metrics;
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust for insets...
        RectangleInsets insets = getInsets();
        insets.trim(area);
        drawBackground(g2, area);

        // adjust for padding...
        Rectangle2D interior = (Rectangle2D) area.clone();
        this.padding.trim(interior);
        int midX = (int) (interior.getX() + (interior.getWidth() / 2));
        int midY = (int) (interior.getY() + (interior.getHeight() / 2));
        int stemTop = (int) (interior.getMinY() + getBulbRadius());
        int stemBottom = (int) (interior.getMaxY() - getBulbDiameter());
        Rectangle2D dataArea = new Rectangle2D.Double(midX - getColumnRadius(),
                stemTop, getColumnRadius(), stemBottom - stemTop);

        outerBulb.setFrame(midX - getBulbRadius(), stemBottom,
                getBulbDiameter(), getBulbDiameter());

        outerStem.setRoundRect(midX - getColumnRadius(), interior.getMinY(),
                getColumnDiameter(), stemBottom + getBulbDiameter() - stemTop,
                getColumnDiameter(), getColumnDiameter());

        Area outerThermometer = new Area(outerBulb);
        Area tempArea = new Area(outerStem);
        outerThermometer.add(tempArea);

        innerBulb.setFrame(midX - getBulbRadius() + getGap(), stemBottom
                + getGap(), getBulbDiameter() - getGap() * 2, getBulbDiameter()
                - getGap() * 2);

        innerStem.setRoundRect(midX - getColumnRadius() + getGap(),
                interior.getMinY() + getGap(), getColumnDiameter()
                - getGap() * 2, stemBottom + getBulbDiameter() - getGap() * 2
                - stemTop, getColumnDiameter() - getGap() * 2,
                getColumnDiameter() - getGap() * 2);

        Area innerThermometer = new Area(innerBulb);
        tempArea = new Area(innerStem);
        innerThermometer.add(tempArea);

        if ((this.dataset != null) && (this.dataset.getValue() != null)) {
            double current = this.dataset.getValue().doubleValue();
            double ds = this.rangeAxis.valueToJava2D(current, dataArea,
                    RectangleEdge.LEFT);

            int i = getColumnDiameter() - getGap() * 2; // already calculated
            int j = getColumnRadius() - getGap(); // already calculated
            int l = (i / 2);
            int k = (int) Math.round(ds);
            if (k < (getGap() + interior.getMinY())) {
                k = (int) (getGap() + interior.getMinY());
                l = getBulbRadius();
            }

            Area mercury = new Area(innerBulb);

            if (k < (stemBottom + getBulbRadius())) {
                mercuryStem.setRoundRect(midX - j, k, i,
                        (stemBottom + getBulbRadius()) - k, l, l);
                tempArea = new Area(mercuryStem);
                mercury.add(tempArea);
            }

            g2.setPaint(getCurrentPaint());
            g2.fill(mercury);

            // draw range indicators...
            if (this.subrangeIndicatorsVisible) {
                g2.setStroke(this.subrangeIndicatorStroke);
                Range range = this.rangeAxis.getRange();

                // draw start of normal range
                double value = this.subrangeInfo[NORMAL][RANGE_LOW];
                if (range.contains(value)) {
                    double x = midX + getColumnRadius() + 2;
                    double y = this.rangeAxis.valueToJava2D(value, dataArea,
                            RectangleEdge.LEFT);
                    Line2D line = new Line2D.Double(x, y, x + 10, y);
                    g2.setPaint(this.subrangePaint[NORMAL]);
                    g2.draw(line);
                }

                // draw start of warning range
                value = this.subrangeInfo[WARNING][RANGE_LOW];
                if (range.contains(value)) {
                    double x = midX + getColumnRadius() + 2;
                    double y = this.rangeAxis.valueToJava2D(value, dataArea,
                            RectangleEdge.LEFT);
                    Line2D line = new Line2D.Double(x, y, x + 10, y);
                    g2.setPaint(this.subrangePaint[WARNING]);
                    g2.draw(line);
                }

                // draw start of critical range
                value = this.subrangeInfo[CRITICAL][RANGE_LOW];
                if (range.contains(value)) {
                    double x = midX + getColumnRadius() + 2;
                    double y = this.rangeAxis.valueToJava2D(value, dataArea,
                            RectangleEdge.LEFT);
                    Line2D line = new Line2D.Double(x, y, x + 10, y);
                    g2.setPaint(this.subrangePaint[CRITICAL]);
                    g2.draw(line);
                }
            }

            // draw the axis...
            if ((this.rangeAxis != null) && (this.axisLocation != NONE)) {
                int drawWidth = AXIS_GAP;
                if (this.showValueLines) {
                    drawWidth += getColumnDiameter();
                }
                Rectangle2D drawArea;
                double cursor;

                switch (this.axisLocation) {
                    case RIGHT:
                        cursor = midX + getColumnRadius();
                        drawArea = new Rectangle2D.Double(cursor,
                                stemTop, drawWidth, (stemBottom - stemTop + 1));
                        this.rangeAxis.draw(g2, cursor, area, drawArea,
                                RectangleEdge.RIGHT, null);
                        break;

                    case LEFT:
                    default:
                        //cursor = midX - COLUMN_RADIUS - AXIS_GAP;
                        cursor = midX - getColumnRadius();
                        drawArea = new Rectangle2D.Double(cursor, stemTop,
                                drawWidth, (stemBottom - stemTop + 1));
                        this.rangeAxis.draw(g2, cursor, area, drawArea,
                                RectangleEdge.LEFT, null);
                        break;
                }

            }

            // draw text value on screen
            g2.setFont(this.valueFont);
            g2.setPaint(this.valuePaint);
            metrics = g2.getFontMetrics();
            switch (this.valueLocation) {
                case RIGHT:
                    g2.drawString(this.valueFormat.format(current),
                            midX + getColumnRadius() + getGap(), midY);
                    break;
                case LEFT:
                    String valueString = this.valueFormat.format(current);
                    int stringWidth = metrics.stringWidth(valueString);
                    g2.drawString(valueString, midX - getColumnRadius()
                            - getGap() - stringWidth, midY);
                    break;
                case BULB:
                    temp = this.valueFormat.format(current);
                    i = metrics.stringWidth(temp) / 2;
                    g2.drawString(temp, midX - i,
                            stemBottom + getBulbRadius() + getGap());
                    break;
                default:
            }
            /***/
        }

        g2.setPaint(this.thermometerPaint);
        g2.setFont(this.valueFont);

        //  draw units indicator
        metrics = g2.getFontMetrics();
        int tickX1 = midX - getColumnRadius() - getGap() * 2
                     - metrics.stringWidth(UNITS[this.units]);
        if (tickX1 > area.getMinX()) {
            g2.drawString(UNITS[this.units], tickX1,
                    (int) (area.getMinY() + 20));
        }

        // draw thermometer outline
        g2.setStroke(this.thermometerStroke);
        g2.draw(outerThermometer);
        g2.draw(innerThermometer);

        drawOutline(g2, area);
    }

    
    @Override
    public void zoom(double percent) {
        // intentionally blank
   }

    
    @Override
    public String getPlotType() {
        return localizationResources.getString("Thermometer_Plot");
    }

    
    @Override
    public void datasetChanged(DatasetChangeEvent event) {
        if (this.dataset != null) {
            Number vn = this.dataset.getValue();
            if (vn != null) {
                double value = vn.doubleValue();
                if (inSubrange(NORMAL, value)) {
                    this.subrange = NORMAL;
                }
                else if (inSubrange(WARNING, value)) {
                   this.subrange = WARNING;
                }
                else if (inSubrange(CRITICAL, value)) {
                    this.subrange = CRITICAL;
                }
                else {
                    this.subrange = -1;
                }
                setAxisRange();
            }
        }
        super.datasetChanged(event);
    }

    
    @Override
    public Range getDataRange(ValueAxis axis) {
       return new Range(this.lowerBound, this.upperBound);
    }

    
    protected void setAxisRange() {
        if ((this.subrange >= 0) && (this.followDataInSubranges)) {
            this.rangeAxis.setRange(
                    new Range(this.subrangeInfo[this.subrange][DISPLAY_LOW],
                    this.subrangeInfo[this.subrange][DISPLAY_HIGH]));
        }
        else {
            this.rangeAxis.setRange(this.lowerBound, this.upperBound);
        }
    }

    
    @Override
    public LegendItemCollection getLegendItems() {
        return null;
    }

    
    @Override
    public PlotOrientation getOrientation() {
        return PlotOrientation.VERTICAL;
    }

    
    protected static boolean isValidNumber(double d) {
        return (!(Double.isNaN(d) || Double.isInfinite(d)));
    }

    
    private boolean inSubrange(int subrange, double value) {
        return (value > this.subrangeInfo[subrange][RANGE_LOW]
            && value <= this.subrangeInfo[subrange][RANGE_HIGH]);
    }

    
    private Paint getCurrentPaint() {
        Paint result = this.mercuryPaint;
        if (this.useSubrangePaint) {
            double value = this.dataset.getValue().doubleValue();
            if (inSubrange(NORMAL, value)) {
                result = this.subrangePaint[NORMAL];
            }
            else if (inSubrange(WARNING, value)) {
                result = this.subrangePaint[WARNING];
            }
            else if (inSubrange(CRITICAL, value)) {
                result = this.subrangePaint[CRITICAL];
            }
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ThermometerPlot)) {
            return false;
        }
        ThermometerPlot that = (ThermometerPlot) obj;
        if (!super.equals(obj)) {
            return false;
        }
        if (!Objects.equals(this.rangeAxis, that.rangeAxis)) {
            return false;
        }
        if (this.axisLocation != that.axisLocation) {
            return false;
        }
        if (this.lowerBound != that.lowerBound) {
            return false;
        }
        if (this.upperBound != that.upperBound) {
            return false;
        }
        if (!Objects.equals(this.padding, that.padding)) {
            return false;
        }
        if (!Objects.equals(this.thermometerStroke, that.thermometerStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.thermometerPaint,
                that.thermometerPaint)) {
            return false;
        }
        if (this.units != that.units) {
            return false;
        }
        if (this.valueLocation != that.valueLocation) {
            return false;
        }
        if (!Objects.equals(this.valueFont, that.valueFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.valuePaint, that.valuePaint)) {
            return false;
        }
        if (!Objects.equals(this.valueFormat, that.valueFormat)) {
            return false;
        }
        if (!PaintUtils.equal(this.mercuryPaint, that.mercuryPaint)) {
            return false;
        }
        if (this.showValueLines != that.showValueLines) {
            return false;
        }
        if (this.subrange != that.subrange) {
            return false;
        }
        if (this.followDataInSubranges != that.followDataInSubranges) {
            return false;
        }
        if (!equal(this.subrangeInfo, that.subrangeInfo)) {
            return false;
        }
        if (this.useSubrangePaint != that.useSubrangePaint) {
            return false;
        }
        if (this.bulbRadius != that.bulbRadius) {
            return false;
        }
        if (this.columnRadius != that.columnRadius) {
            return false;
        }
        if (this.gap != that.gap) {
            return false;
        }
        for (int i = 0; i < this.subrangePaint.length; i++) {
            if (!PaintUtils.equal(this.subrangePaint[i],
                    that.subrangePaint[i])) {
                return false;
            }
        }
        return true;
    }

    
    private static boolean equal(double[][] array1, double[][] array2) {
        if (array1 == null) {
            return (array2 == null);
        }
        if (array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!Arrays.equals(array1[i], array2[i])) {
                return false;
            }
        }
        return true;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {

        ThermometerPlot clone = (ThermometerPlot) super.clone();

        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }
        clone.rangeAxis = CloneUtils.clone(this.rangeAxis);
        if (clone.rangeAxis != null) {
            clone.rangeAxis.setPlot(clone);
            clone.rangeAxis.addChangeListener(clone);
        }
        clone.valueFormat = (NumberFormat) this.valueFormat.clone();
        clone.subrangePaint = (Paint[]) this.subrangePaint.clone();

        return clone;

    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeStroke(this.thermometerStroke, stream);
        SerialUtils.writePaint(this.thermometerPaint, stream);
        SerialUtils.writePaint(this.valuePaint, stream);
        SerialUtils.writePaint(this.mercuryPaint, stream);
        SerialUtils.writeStroke(this.subrangeIndicatorStroke, stream);
        SerialUtils.writeStroke(this.rangeIndicatorStroke, stream);
        for (int i = 0; i < 3; i++) {
            SerialUtils.writePaint(this.subrangePaint[i], stream);
        }
    }

    
    private void readObject(ObjectInputStream stream) throws IOException,
            ClassNotFoundException {
        stream.defaultReadObject();
        this.thermometerStroke = SerialUtils.readStroke(stream);
        this.thermometerPaint = SerialUtils.readPaint(stream);
        this.valuePaint = SerialUtils.readPaint(stream);
        this.mercuryPaint = SerialUtils.readPaint(stream);
        this.subrangeIndicatorStroke = SerialUtils.readStroke(stream);
        this.rangeIndicatorStroke = SerialUtils.readStroke(stream);
        this.subrangePaint = new Paint[3];
        for (int i = 0; i < 3; i++) {
            this.subrangePaint[i] = SerialUtils.readPaint(stream);
        }
        if (this.rangeAxis != null) {
            this.rangeAxis.addChangeListener(this);
        }
    }

    
    @Override
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source) {
        // no domain axis to zoom
    }

    
    @Override
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source, boolean useAnchor) {
        // no domain axis to zoom
    }

    
    @Override
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source) {
        this.rangeAxis.resizeRange(factor);
    }

    
    @Override
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source, boolean useAnchor) {
        double anchorY = this.getRangeAxis().java2DToValue(source.getY(),
                state.getDataArea(), RectangleEdge.LEFT);
        this.rangeAxis.resizeRange(factor, anchorY);
    }

    
    @Override
    public void zoomDomainAxes(double lowerPercent, double upperPercent,
                               PlotRenderingInfo state, Point2D source) {
        // no domain axis to zoom
    }

    
    @Override
    public void zoomRangeAxes(double lowerPercent, double upperPercent,
                              PlotRenderingInfo state, Point2D source) {
        this.rangeAxis.zoomRange(lowerPercent, upperPercent);
    }

    
    @Override
    public boolean isDomainZoomable() {
        return false;
    }

    
    @Override
    public boolean isRangeZoomable() {
        return true;
    }

}
