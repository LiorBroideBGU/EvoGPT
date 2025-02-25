

package org.jfree.chart.plot;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeMap;
import org.jfree.chart.ChartElementVisitor;

import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberTick;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.TickType;
import org.jfree.chart.axis.TickUnit;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.ValueTick;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.PolarItemRenderer;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.data.Range;
import org.jfree.data.general.Dataset;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.xy.XYDataset;


public class PolarPlot extends Plot implements ValueAxisPlot, Zoomable,
        RendererChangeListener, Cloneable, Serializable {

    
    private static final long serialVersionUID = 3794383185924179525L;

    
    private static final int DEFAULT_MARGIN = 20;

    
    private static final double ANNOTATION_MARGIN = 7.0;

    
    public static final double DEFAULT_ANGLE_TICK_UNIT_SIZE = 45.0;

    
    public static final double DEFAULT_ANGLE_OFFSET = -90.0;

    
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(
            0.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL,
            0.0f, new float[]{2.0f, 2.0f}, 0.0f);

    
    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.GRAY;

    
    protected static ResourceBundle localizationResources
            = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    
    private List<ValueTick> angleTicks;

    
    private Map<Integer, ValueAxis> axes;

    
    private final Map<Integer, PolarAxisLocation> axisLocations;

    
    private Map<Integer, XYDataset> datasets;

    
    private Map<Integer, PolarItemRenderer> renderers;

    
    private TickUnit angleTickUnit;

    
    private double angleOffset;

    
    private boolean counterClockwise;

    
    private boolean angleLabelsVisible = true;

    
    private Font angleLabelFont = new Font("SansSerif", Font.PLAIN, 12);

    
    private transient Paint angleLabelPaint = Color.BLACK;

    
    private boolean angleGridlinesVisible;

    
    private transient Stroke angleGridlineStroke;

    
    private transient Paint angleGridlinePaint;

    
    private boolean radiusGridlinesVisible;

    
    private transient Stroke radiusGridlineStroke;

    
    private transient Paint radiusGridlinePaint;

    
    private boolean radiusMinorGridlinesVisible;

    
    private List<String> cornerTextItems = new ArrayList<>();

    
    private int margin;

    
    private LegendItemCollection fixedLegendItems;

    
    private final Map<Integer, List<Integer>> datasetToAxesMap;

    
    public PolarPlot() {
        this(null, null, null);
    }

   
    public PolarPlot(XYDataset dataset, ValueAxis radiusAxis, PolarItemRenderer renderer) {
        super();
        this.datasets = new HashMap<>();
        this.datasets.put(0, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        this.angleTickUnit = new NumberTickUnit(DEFAULT_ANGLE_TICK_UNIT_SIZE);

        this.axes = new HashMap<>();
        this.datasetToAxesMap = new TreeMap<>();
        this.axes.put(0, radiusAxis);
        if (radiusAxis != null) {
            radiusAxis.setPlot(this);
            radiusAxis.addChangeListener(this);
        }

        // define the default locations for up to 8 axes...
        this.axisLocations = new HashMap<>();
        this.axisLocations.put(0, PolarAxisLocation.EAST_ABOVE);
        this.axisLocations.put(1, PolarAxisLocation.NORTH_LEFT);
        this.axisLocations.put(2, PolarAxisLocation.WEST_BELOW);
        this.axisLocations.put(3, PolarAxisLocation.SOUTH_RIGHT);
        this.axisLocations.put(4, PolarAxisLocation.EAST_BELOW);
        this.axisLocations.put(5, PolarAxisLocation.NORTH_RIGHT);
        this.axisLocations.put(6, PolarAxisLocation.WEST_ABOVE);
        this.axisLocations.put(7, PolarAxisLocation.SOUTH_LEFT);

        this.renderers = new HashMap<>();
        this.renderers.put(0, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

        this.angleOffset = DEFAULT_ANGLE_OFFSET;
        this.counterClockwise = false;
        this.angleGridlinesVisible = true;
        this.angleGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.angleGridlinePaint = DEFAULT_GRIDLINE_PAINT;

        this.radiusGridlinesVisible = true;
        this.radiusMinorGridlinesVisible = true;
        this.radiusGridlineStroke = DEFAULT_GRIDLINE_STROKE;
        this.radiusGridlinePaint = DEFAULT_GRIDLINE_PAINT;
        this.margin = DEFAULT_MARGIN;
    }

    
    @Override
    public String getPlotType() {
       return PolarPlot.localizationResources.getString("Polar_Plot");
    }

    
    public ValueAxis getAxis() {
        return getAxis(0);
    }

    
    public ValueAxis getAxis(int index) {
        return this.axes.get(index);
    }

    
    public void setAxis(ValueAxis axis) {
        setAxis(0, axis);
    }

    
    public void setAxis(int index, ValueAxis axis) {
        setAxis(index, axis, true);
    }

    
    public void setAxis(int index, ValueAxis axis, boolean notify) {
        ValueAxis existing = getAxis(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        if (axis != null) {
            axis.setPlot(this);
        }
        this.axes.put(index, axis);
        if (axis != null) {
            axis.configure();
            axis.addChangeListener(this);
        }
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public PolarAxisLocation getAxisLocation() {
        return getAxisLocation(0);
    }

    
    public PolarAxisLocation getAxisLocation(int index) {
        return this.axisLocations.get(index);
    }

    
    public void setAxisLocation(PolarAxisLocation location) {
        // delegate argument checks...
        setAxisLocation(0, location, true);
    }

    
    public void setAxisLocation(PolarAxisLocation location, boolean notify) {
        // delegate...
        setAxisLocation(0, location, notify);
    }

    
    public void setAxisLocation(int index, PolarAxisLocation location) {
        // delegate...
        setAxisLocation(index, location, true);
    }

    
    public void setAxisLocation(int index, PolarAxisLocation location,
            boolean notify) {
        Args.nullNotPermitted(location, "location");
        this.axisLocations.put(index, location);
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public int getAxisCount() {
        return this.axes.size();
    }

    
    public XYDataset getDataset() {
        return getDataset(0);
    }

    
    public XYDataset getDataset(int index) {
        return this.datasets.get(index);
    }

    
    public void setDataset(XYDataset dataset) {
        setDataset(0, dataset);
    }

    
    public void setDataset(int index, XYDataset dataset) {
        XYDataset existing = getDataset(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.datasets.put(index, dataset);
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        // send a dataset change event to self...
        DatasetChangeEvent event = new DatasetChangeEvent(this, dataset);
        datasetChanged(event);
    }

    
    public int getDatasetCount() {
        return this.datasets.size();
    }

    
    public int indexOf(XYDataset dataset) {
        for (Entry<Integer, XYDataset> entry : this.datasets.entrySet()) {
            if (entry.getValue() == dataset) {
                return entry.getKey();
            }
        }
        return -1;
    }

    
    public PolarItemRenderer getRenderer() {
        return getRenderer(0);
    }

    
    public PolarItemRenderer getRenderer(int index) {
        return this.renderers.get(index);
    }

    
    public void setRenderer(PolarItemRenderer renderer) {
        setRenderer(0, renderer);
    }

    
    public void setRenderer(int index, PolarItemRenderer renderer) {
        setRenderer(index, renderer, true);
    }

    
    public void setRenderer(int index, PolarItemRenderer renderer,
                            boolean notify) {
        PolarItemRenderer existing = getRenderer(index);
        if (existing != null) {
            existing.removeChangeListener(this);
        }
        this.renderers.put(index, renderer);
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public TickUnit getAngleTickUnit() {
        return this.angleTickUnit;
    }

    
    public void setAngleTickUnit(TickUnit unit) {
        Args.nullNotPermitted(unit, "unit");
        this.angleTickUnit = unit;
        fireChangeEvent();
    }

    
    public double getAngleOffset() {
        return this.angleOffset;
    }

    
    public void setAngleOffset(double offset) {
        this.angleOffset = offset;
        fireChangeEvent();
    }

    
    public boolean isCounterClockwise() {
        return this.counterClockwise;
    }

    
    public void setCounterClockwise(boolean counterClockwise)
    {
        this.counterClockwise = counterClockwise;
    }

    
    public boolean isAngleLabelsVisible() {
        return this.angleLabelsVisible;
    }

    
    public void setAngleLabelsVisible(boolean visible) {
        if (this.angleLabelsVisible != visible) {
            this.angleLabelsVisible = visible;
            fireChangeEvent();
        }
    }

    
    public Font getAngleLabelFont() {
        return this.angleLabelFont;
    }

    
    public void setAngleLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.angleLabelFont = font;
        fireChangeEvent();
    }

    
    public Paint getAngleLabelPaint() {
        return this.angleLabelPaint;
    }

    
    public void setAngleLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.angleLabelPaint = paint;
        fireChangeEvent();
    }

    
    public boolean isAngleGridlinesVisible() {
        return this.angleGridlinesVisible;
    }

    
    public void setAngleGridlinesVisible(boolean visible) {
        if (this.angleGridlinesVisible != visible) {
            this.angleGridlinesVisible = visible;
            fireChangeEvent();
        }
    }

    
    public Stroke getAngleGridlineStroke() {
        return this.angleGridlineStroke;
    }

    
    public void setAngleGridlineStroke(Stroke stroke) {
        this.angleGridlineStroke = stroke;
        fireChangeEvent();
    }

    
    public Paint getAngleGridlinePaint() {
        return this.angleGridlinePaint;
    }

    
    public void setAngleGridlinePaint(Paint paint) {
        this.angleGridlinePaint = paint;
        fireChangeEvent();
    }

    
    public boolean isRadiusGridlinesVisible() {
        return this.radiusGridlinesVisible;
    }

    
    public void setRadiusGridlinesVisible(boolean visible) {
        if (this.radiusGridlinesVisible != visible) {
            this.radiusGridlinesVisible = visible;
            fireChangeEvent();
        }
    }

    
    public Stroke getRadiusGridlineStroke() {
        return this.radiusGridlineStroke;
    }

    
    public void setRadiusGridlineStroke(Stroke stroke) {
        this.radiusGridlineStroke = stroke;
        fireChangeEvent();
    }

    
    public Paint getRadiusGridlinePaint() {
        return this.radiusGridlinePaint;
    }

    
    public void setRadiusGridlinePaint(Paint paint) {
        this.radiusGridlinePaint = paint;
        fireChangeEvent();
    }

    
    public boolean isRadiusMinorGridlinesVisible() {
        return this.radiusMinorGridlinesVisible;
    }

    
    public void setRadiusMinorGridlinesVisible(boolean flag) {
        this.radiusMinorGridlinesVisible = flag;
        fireChangeEvent();
    }

    
    public int getMargin() {
        return this.margin;
    }

    
    public void setMargin(int margin) {
        this.margin = margin;
        fireChangeEvent();
    }

    
    public LegendItemCollection getFixedLegendItems() {
        return this.fixedLegendItems;
    }

    
    public void setFixedLegendItems(LegendItemCollection items) {
        this.fixedLegendItems = items;
        fireChangeEvent();
    }

    
    public void addCornerTextItem(String text) {
        Args.nullNotPermitted(text, "text");
        this.cornerTextItems.add(text);
        fireChangeEvent();
    }

    
    public void removeCornerTextItem(String text) {
        boolean removed = this.cornerTextItems.remove(text);
        if (removed) {
            fireChangeEvent();
        }
    }

    
    public void clearCornerTextItems() {
        if (!this.cornerTextItems.isEmpty()) {
            this.cornerTextItems.clear();
            fireChangeEvent();
        }
    }

    
    protected List<ValueTick> refreshAngleTicks() {
        List<ValueTick> ticks = new ArrayList<>();
        for (double currentTickVal = 0.0; currentTickVal < 360.0;
                currentTickVal += this.angleTickUnit.getSize()) {
            TextAnchor ta = calculateTextAnchor(currentTickVal);
            NumberTick tick = new NumberTick(currentTickVal,
                this.angleTickUnit.valueToString(currentTickVal),
                ta, TextAnchor.CENTER, 0.0);
            ticks.add(tick);
        }
        return ticks;
    }

    
    protected TextAnchor calculateTextAnchor(double angleDegrees) {
        TextAnchor ta = TextAnchor.CENTER;

        // normalize angle
        double offset = this.angleOffset;
        while (offset < 0.0) {
            offset += 360.0;
        }
        double normalizedAngle = (((this.counterClockwise ? -1 : 1)
                * angleDegrees) + offset) % 360;
        while (this.counterClockwise && (normalizedAngle < 0.0)) {
            normalizedAngle += 360.0;
        }

        if (normalizedAngle == 0.0) {
            ta = TextAnchor.CENTER_LEFT;
        } else if (normalizedAngle > 0.0 && normalizedAngle < 90.0) {
            ta = TextAnchor.TOP_LEFT;
        } else if (normalizedAngle == 90.0) {
            ta = TextAnchor.TOP_CENTER;
        } else if (normalizedAngle > 90.0 && normalizedAngle < 180.0) {
            ta = TextAnchor.TOP_RIGHT;
        } else if (normalizedAngle == 180) {
            ta = TextAnchor.CENTER_RIGHT;
        } else if (normalizedAngle > 180.0 && normalizedAngle < 270.0) {
            ta = TextAnchor.BOTTOM_RIGHT;
        } else if (normalizedAngle == 270) {
            ta = TextAnchor.BOTTOM_CENTER;
        } else if (normalizedAngle > 270.0 && normalizedAngle < 360.0) {
            ta = TextAnchor.BOTTOM_LEFT;
        }
        return ta;
    }

    
    public void mapDatasetToAxis(int index, int axisIndex) {
        List<Integer> axisIndices = new ArrayList<>(1);
        axisIndices.add(axisIndex);
        mapDatasetToAxes(index, axisIndices);
    }

    
    public void mapDatasetToAxes(int index, List<Integer> axisIndices) {
        if (index < 0) {
            throw new IllegalArgumentException("Requires 'index' >= 0.");
        }
        checkAxisIndices(axisIndices);
        Integer key = index;
        this.datasetToAxesMap.put(key, new ArrayList<>(axisIndices));
        // fake a dataset change event to update axes...
        datasetChanged(new DatasetChangeEvent(this, getDataset(index)));
    }

    
    private void checkAxisIndices(List<Integer> indices) {
        // axisIndices can be:
        // 1.  null;
        // 2.  non-empty, containing only Integer objects that are unique.
        if (indices == null) {
            return;  // OK
        }
        if (indices.isEmpty()) {
            throw new IllegalArgumentException("Empty list not permitted.");
        }
        Set<Integer> set = new HashSet<>();
        for (Integer i : indices) {
            if (set.contains(i)) {
                throw new IllegalArgumentException("Indices must be unique.");
            }
            set.add(i);
        }
    }

    
    public ValueAxis getAxisForDataset(int index) {
        ValueAxis valueAxis;
        List<Integer> axisIndices = this.datasetToAxesMap.get(index);
        if (axisIndices != null) {
            // the first axis in the list is used for data <--> Java2D
            Integer axisIndex = axisIndices.get(0);
            valueAxis = getAxis(axisIndex);
        }
        else {
            valueAxis = getAxis(0);
        }
        return valueAxis;
    }

    
    public int getAxisIndex(ValueAxis axis) {
        for (Entry<Integer, ValueAxis> entry : this.axes.entrySet()) {
            if (axis.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        // try the parent plot
        Plot parent = getParent();
        if (parent instanceof PolarPlot) {
            PolarPlot p = (PolarPlot) parent;
            return p.getAxisIndex(axis);
        }
        return -1;
    }

    
    public int getIndexOf(PolarItemRenderer renderer) {
        Args.nullNotPermitted(renderer, "renderer");
        for (Entry<Integer, PolarItemRenderer> entry : this.renderers.entrySet()) {
            if (renderer.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return -1;
    }

    
    @Override
    public void receive(ChartElementVisitor visitor) {
        // FIXME: handle axes and renderers
        visitor.visit(this);
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
            PlotState parentState, PlotRenderingInfo info) {

        // if the plot area is too small, just return...
        boolean b1 = (area.getWidth() <= MINIMUM_WIDTH_TO_DRAW);
        boolean b2 = (area.getHeight() <= MINIMUM_HEIGHT_TO_DRAW);
        if (b1 || b2) {
            return;
        }

        // record the plot area...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for the plot insets (if any)...
        RectangleInsets insets = getInsets();
        insets.trim(area);

        Rectangle2D dataArea = area;
        if (info != null) {
            info.setDataArea(dataArea);
        }

        // draw the plot background and axes...
        drawBackground(g2, dataArea);
        int axisCount = this.axes.size();
        AxisState state = null;
        for (int i = 0; i < axisCount; i++) {
            ValueAxis axis = getAxis(i);
            if (axis != null) {
                PolarAxisLocation location = this.axisLocations.get(i);
                AxisState s = drawAxis(axis, location, g2, dataArea);
                if (i == 0) {
                    state = s;
                }
            }
        }

        // now for each dataset, get the renderer and the appropriate axis
        // and render the dataset...
        Shape originalClip = g2.getClip();
        Composite originalComposite = g2.getComposite();

        g2.clip(dataArea);
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                getForegroundAlpha()));
        this.angleTicks = refreshAngleTicks();
        drawGridlines(g2, dataArea, this.angleTicks, state.getTicks());
        render(g2, dataArea, info);
        g2.setClip(originalClip);
        g2.setComposite(originalComposite);
        drawOutline(g2, dataArea);
        drawCornerTextItems(g2, dataArea);
    }

    
    protected void drawCornerTextItems(Graphics2D g2, Rectangle2D area) {
        if (this.cornerTextItems.isEmpty()) {
            return;
        }

        g2.setColor(Color.BLACK);
        double width = 0.0;
        double height = 0.0;
        for (String msg : this.cornerTextItems) {
            FontMetrics fm = g2.getFontMetrics();
            Rectangle2D bounds = TextUtils.getTextBounds(msg, g2, fm);
            width = Math.max(width, bounds.getWidth());
            height += bounds.getHeight();
        }

        double xadj = ANNOTATION_MARGIN * 2.0;
        double yadj = ANNOTATION_MARGIN;
        width += xadj;
        height += yadj;

        double x = area.getMaxX() - width;
        double y = area.getMaxY() - height;
        g2.drawRect((int) x, (int) y, (int) width, (int) height);
        x += ANNOTATION_MARGIN;
        for (String msg : this.cornerTextItems) {
            Rectangle2D bounds = TextUtils.getTextBounds(msg, g2,
                    g2.getFontMetrics());
            y += bounds.getHeight();
            g2.drawString(msg, (int) x, (int) y);
        }
    }

    
    protected AxisState drawAxis(ValueAxis axis, PolarAxisLocation location,
            Graphics2D g2, Rectangle2D plotArea) {

        double centerX = plotArea.getCenterX();
        double centerY = plotArea.getCenterY();
        double r = Math.min(plotArea.getWidth() / 2.0,
                plotArea.getHeight() / 2.0) - this.margin;
        double x = centerX - r;
        double y = centerY - r;

        Rectangle2D dataArea = null;
        AxisState result = null;
        if (location == PolarAxisLocation.NORTH_RIGHT) {
            dataArea = new Rectangle2D.Double(x, y, r, r);
            result = axis.draw(g2, centerX, plotArea, dataArea,
                    RectangleEdge.RIGHT, null);
        }
        else if (location == PolarAxisLocation.NORTH_LEFT) {
            dataArea = new Rectangle2D.Double(centerX, y, r, r);
            result = axis.draw(g2, centerX, plotArea, dataArea,
                    RectangleEdge.LEFT, null);
        }
        else if (location == PolarAxisLocation.SOUTH_LEFT) {
            dataArea = new Rectangle2D.Double(centerX, centerY, r, r);
            result = axis.draw(g2, centerX, plotArea, dataArea,
                    RectangleEdge.LEFT, null);
        }
        else if (location == PolarAxisLocation.SOUTH_RIGHT) {
            dataArea = new Rectangle2D.Double(x, centerY, r, r);
            result = axis.draw(g2, centerX, plotArea, dataArea,
                    RectangleEdge.RIGHT, null);
        }
        else if (location == PolarAxisLocation.EAST_ABOVE) {
            dataArea = new Rectangle2D.Double(centerX, centerY, r, r);
            result = axis.draw(g2, centerY, plotArea, dataArea,
                    RectangleEdge.TOP, null);
        }
        else if (location == PolarAxisLocation.EAST_BELOW) {
            dataArea = new Rectangle2D.Double(centerX, y, r, r);
            result = axis.draw(g2, centerY, plotArea, dataArea,
                    RectangleEdge.BOTTOM, null);
        }
        else if (location == PolarAxisLocation.WEST_ABOVE) {
            dataArea = new Rectangle2D.Double(x, centerY, r, r);
            result = axis.draw(g2, centerY, plotArea, dataArea,
                    RectangleEdge.TOP, null);
        }
        else if (location == PolarAxisLocation.WEST_BELOW) {
            dataArea = new Rectangle2D.Double(x, y, r, r);
            result = axis.draw(g2, centerY, plotArea, dataArea,
                    RectangleEdge.BOTTOM, null);
        }

        return result;
    }

    
    protected void render(Graphics2D g2, Rectangle2D dataArea,
            PlotRenderingInfo info) {

        // now get the data and plot it (the visual representation will depend
        // on the m_Renderer that has been set)...
        boolean hasData = false;
        int datasetCount = this.datasets.size();
        for (int i = datasetCount - 1; i >= 0; i--) {
            XYDataset dataset = getDataset(i);
            if (dataset == null) {
                continue;
            }
            PolarItemRenderer renderer = getRenderer(i);
            if (renderer == null) {
                continue;
            }
            if (!DatasetUtils.isEmptyOrNull(dataset)) {
                hasData = true;
                int seriesCount = dataset.getSeriesCount();
                for (int series = 0; series < seriesCount; series++) {
                    renderer.drawSeries(g2, dataArea, info, this, dataset,
                            series);
                }
            }
        }
        if (!hasData) {
            drawNoDataMessage(g2, dataArea);
        }
    }

    
    protected void drawGridlines(Graphics2D g2, Rectangle2D dataArea,
            List<ValueTick> angularTicks, List<ValueTick> radialTicks) {

        PolarItemRenderer renderer = getRenderer();
        // no renderer, no gridlines...
        if (renderer == null) {
            return;
        }

        // draw the domain grid lines, if any...
        if (isAngleGridlinesVisible()) {
            Stroke gridStroke = getAngleGridlineStroke();
            Paint gridPaint = getAngleGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                renderer.drawAngularGridLines(g2, this, angularTicks,
                        dataArea);
            }
        }

        // draw the radius grid lines, if any...
        if (isRadiusGridlinesVisible()) {
            Stroke gridStroke = getRadiusGridlineStroke();
            Paint gridPaint = getRadiusGridlinePaint();
            if ((gridStroke != null) && (gridPaint != null)) {
                List<ValueTick> ticks = buildRadialTicks(radialTicks);
                renderer.drawRadialGridLines(g2, this, getAxis(), ticks, dataArea);
            }
        }
    }

    
    protected List<ValueTick> buildRadialTicks(List<ValueTick> allTicks) {
        List<ValueTick> ticks = new ArrayList<>();
        for (ValueTick tick : allTicks) {
            if (isRadiusMinorGridlinesVisible() || TickType.MAJOR.equals(tick.getTickType())) {
                ticks.add(tick);
            }
        }
        return ticks;
    }

    
    @Override
    public void zoom(double percent) {
        for (int axisIdx = 0; axisIdx < getAxisCount(); axisIdx++) {
            final ValueAxis axis = getAxis(axisIdx);
            if (axis != null) {
                if (percent > 0.0) {
                    double radius = axis.getUpperBound();
                    double scaledRadius = radius * percent;
                    axis.setUpperBound(scaledRadius);
                    axis.setAutoRange(false);
                } else {
                    axis.setAutoRange(true);
                }
            }
        }
    }

    
    private List<XYDataset> getDatasetsMappedToAxis(Integer axisIndex) { 
        Args.nullNotPermitted(axisIndex, "axisIndex");
        List<XYDataset> result = new ArrayList<>();
       for (Entry<Integer, XYDataset> entry : this.datasets.entrySet()) {
            List<Integer> mappedAxes = this.datasetToAxesMap.get(entry.getKey());
            if (mappedAxes == null) {
                if (axisIndex.equals(ZERO)) {
                    result.add(getDataset(entry.getKey()));
                }
            } else {
                if (mappedAxes.contains(axisIndex)) {
                    result.add(getDataset(entry.getKey()));
                }
            }
        }
        return result;
    }

    
    @Override
    public Range getDataRange(ValueAxis axis) {
        Range result = null;
        List<XYDataset> mappedDatasets = new ArrayList<>();
        int axisIndex = getAxisIndex(axis);
        if (axisIndex >= 0) {
            mappedDatasets = getDatasetsMappedToAxis(axisIndex);
        }

        // iterate through the datasets that map to the axis and get the union
        // of the ranges.
        for (XYDataset dataset : mappedDatasets) {
            if (dataset != null) {
                // FIXME better ask the renderer instead of DatasetUtilities
                result = Range.combine(result, DatasetUtils.findRangeBounds(dataset));
            }
        }

        return result;
    }

    
    @Override
    public void datasetChanged(DatasetChangeEvent event) {
        for (int i = 0; i < this.axes.size(); i++) {
            final ValueAxis axis = (ValueAxis) this.axes.get(i);
            if (axis != null) {
                axis.configure();
            }
        }
        if (getParent() != null) {
            getParent().datasetChanged(event);
        }
        else {
            super.datasetChanged(event);
        }
    }

    
    @Override
    public void rendererChanged(RendererChangeEvent event) {
        fireChangeEvent();
    }

    
    @Override
    public LegendItemCollection getLegendItems() {
        if (this.fixedLegendItems != null) {
            return this.fixedLegendItems;
        }
        LegendItemCollection result = new LegendItemCollection();
        int count = this.datasets.size();
        for (int datasetIndex = 0; datasetIndex < count; datasetIndex++) {
            XYDataset dataset = getDataset(datasetIndex);
            PolarItemRenderer renderer = getRenderer(datasetIndex);
            if (dataset != null && renderer != null) {
                int seriesCount = dataset.getSeriesCount();
                for (int i = 0; i < seriesCount; i++) {
                    LegendItem item = renderer.getLegendItem(i);
                    result.add(item);
                }
            }
        }
        return result;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PolarPlot)) {
            return false;
        }
        PolarPlot that = (PolarPlot) obj;
        if (!this.axes.equals(that.axes)) {
            return false;
        }
        if (!this.axisLocations.equals(that.axisLocations)) {
            return false;
        }
        if (!this.renderers.equals(that.renderers)) {
            return false;
        }
        if (!this.angleTickUnit.equals(that.angleTickUnit)) {
            return false;
        }
        if (this.angleGridlinesVisible != that.angleGridlinesVisible) {
            return false;
        }
        if (this.angleOffset != that.angleOffset)
        {
            return false;
        }
        if (this.counterClockwise != that.counterClockwise)
        {
            return false;
        }
        if (this.angleLabelsVisible != that.angleLabelsVisible) {
            return false;
        }
        if (!this.angleLabelFont.equals(that.angleLabelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.angleLabelPaint, that.angleLabelPaint)) {
            return false;
        }
        if (!Objects.equals(this.angleGridlineStroke, that.angleGridlineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(
            this.angleGridlinePaint, that.angleGridlinePaint
        )) {
            return false;
        }
        if (this.radiusGridlinesVisible != that.radiusGridlinesVisible) {
            return false;
        }
        if (!Objects.equals(this.radiusGridlineStroke, that.radiusGridlineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.radiusGridlinePaint,
                that.radiusGridlinePaint)) {
            return false;
        }
        if (this.radiusMinorGridlinesVisible !=
            that.radiusMinorGridlinesVisible) {
            return false;
        }
        if (!this.cornerTextItems.equals(that.cornerTextItems)) {
            return false;
        }
        if (this.margin != that.margin) {
            return false;
        }
        if (!Objects.equals(this.fixedLegendItems, that.fixedLegendItems)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        PolarPlot clone = (PolarPlot) super.clone();
        clone.axes = CloneUtils.clone(this.axes);
        for (int i = 0; i < this.axes.size(); i++) {
            ValueAxis axis = (ValueAxis) this.axes.get(i);
            if (axis != null) {
                ValueAxis clonedAxis = (ValueAxis) axis.clone();
                clone.axes.put(i, clonedAxis);
                clonedAxis.setPlot(clone);
                clonedAxis.addChangeListener(clone);
            }
        }

        // the datasets are not cloned, but listeners need to be added...
        clone.datasets = CloneUtils.clone(this.datasets);
        for (int i = 0; i < clone.datasets.size(); ++i) {
            XYDataset d = getDataset(i);
            if (d != null) {
                d.addChangeListener(clone);
            }
        }

        clone.renderers = CloneUtils.clone(this.renderers);
        for (int i = 0; i < this.renderers.size(); i++) {
            PolarItemRenderer renderer2 = (PolarItemRenderer) this.renderers.get(i);
            if (renderer2 instanceof PublicCloneable) {
                PublicCloneable pc = (PublicCloneable) renderer2;
                PolarItemRenderer rc = (PolarItemRenderer) pc.clone();
                clone.renderers.put(i, rc);
                rc.setPlot(clone);
                rc.addChangeListener(clone);
            }
        }

        clone.cornerTextItems = new ArrayList<>(this.cornerTextItems);

        return clone;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeStroke(this.angleGridlineStroke, stream);
        SerialUtils.writePaint(this.angleGridlinePaint, stream);
        SerialUtils.writeStroke(this.radiusGridlineStroke, stream);
        SerialUtils.writePaint(this.radiusGridlinePaint, stream);
        SerialUtils.writePaint(this.angleLabelPaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {

        stream.defaultReadObject();
        this.angleGridlineStroke = SerialUtils.readStroke(stream);
        this.angleGridlinePaint = SerialUtils.readPaint(stream);
        this.radiusGridlineStroke = SerialUtils.readStroke(stream);
        this.radiusGridlinePaint = SerialUtils.readPaint(stream);
        this.angleLabelPaint = SerialUtils.readPaint(stream);

        int rangeAxisCount = this.axes.size();
        for (int i = 0; i < rangeAxisCount; i++) {
            Axis axis = (Axis) this.axes.get(i);
            if (axis != null) {
                axis.setPlot(this);
                axis.addChangeListener(this);
            }
        }
        int datasetCount = this.datasets.size();
        for (int i = 0; i < datasetCount; i++) {
            Dataset dataset = (Dataset) this.datasets.get(i);
            if (dataset != null) {
                dataset.addChangeListener(this);
            }
        }
        int rendererCount = this.renderers.size();
        for (int i = 0; i < rendererCount; i++) {
            PolarItemRenderer renderer = (PolarItemRenderer) this.renderers.get(i);
            if (renderer != null) {
                renderer.addChangeListener(this);
            }
        }
    }

    
    @Override
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source) {
        // do nothing
    }

    
    @Override
    public void zoomDomainAxes(double factor, PlotRenderingInfo state,
                               Point2D source, boolean useAnchor) {
        // do nothing
    }

    
    @Override
    public void zoomDomainAxes(double lowerPercent, double upperPercent,
                               PlotRenderingInfo state, Point2D source) {
        // do nothing
    }

    
    @Override
    public void zoomRangeAxes(double factor, PlotRenderingInfo state,
                              Point2D source) {
        zoom(factor);
    }

    
    @Override
    public void zoomRangeAxes(double factor, PlotRenderingInfo info,
                              Point2D source, boolean useAnchor) {
        // get the source coordinate - this plot has always a VERTICAL
        // orientation
        final double sourceX = source.getX();

        for (int axisIdx = 0; axisIdx < getAxisCount(); axisIdx++) {
            final ValueAxis axis = getAxis(axisIdx);
            if (axis != null) {
                if (useAnchor) {
                    double anchorX = axis.java2DToValue(sourceX,
                            info.getDataArea(), RectangleEdge.BOTTOM);
                    axis.resizeRange(factor, anchorX);
                }
                else {
                    axis.resizeRange(factor);
                }
            }
        }
    }

    
    @Override
    public void zoomRangeAxes(double lowerPercent, double upperPercent,
                              PlotRenderingInfo state, Point2D source) {
        zoom((upperPercent + lowerPercent) / 2.0);
    }

    
    @Override
    public boolean isDomainZoomable() {
        return false;
    }

    
    @Override
    public boolean isRangeZoomable() {
        return true;
    }

    
    @Override
    public PlotOrientation getOrientation() {
        return PlotOrientation.HORIZONTAL;
    }

    
    public Point translateToJava2D(double angleDegrees, double radius,
            ValueAxis axis, Rectangle2D dataArea) {

        if (counterClockwise) {
            angleDegrees = -angleDegrees;
        }
        double radians = Math.toRadians(angleDegrees + this.angleOffset);

        double minx = dataArea.getMinX() + this.margin;
        double maxx = dataArea.getMaxX() - this.margin;
        double miny = dataArea.getMinY() + this.margin;
        double maxy = dataArea.getMaxY() - this.margin;

        double halfWidth = (maxx - minx) / 2.0;
        double halfHeight = (maxy - miny) / 2.0;

        double midX = minx + halfWidth;
        double midY = miny + halfHeight;

        double l = Math.min(halfWidth, halfHeight);
        Rectangle2D quadrant = new Rectangle2D.Double(midX, midY, l, l);

        double axisMin = axis.getLowerBound();
        double adjustedRadius = Math.max(radius, axisMin);

        double length = axis.valueToJava2D(adjustedRadius, quadrant, RectangleEdge.BOTTOM) - midX;
        float x = (float) (midX + Math.cos(radians) * length);
        float y = (float) (midY + Math.sin(radians) * length);

        int ix = Math.round(x);
        int iy = Math.round(y);

        Point p = new Point(ix, iy);
        return p;

    }

}
