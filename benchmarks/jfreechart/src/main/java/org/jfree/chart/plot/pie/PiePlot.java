

package org.jfree.chart.plot.pie;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.api.RectangleAnchor;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.api.Rotation;
import org.jfree.chart.api.UnitType;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.PieSectionEntity;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.internal.*;
import org.jfree.chart.labels.PieSectionLabelGenerator;
import org.jfree.chart.labels.PieToolTipGenerator;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.plot.*;
import org.jfree.chart.text.*;
import org.jfree.chart.urls.PieURLGenerator;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetUtils;
import org.jfree.data.general.PieDataset;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.*;


public class PiePlot<K extends Comparable<K>> extends Plot implements Cloneable, Serializable {

    
    private static final long serialVersionUID = -795612466005590431L;

    
    public static final double DEFAULT_INTERIOR_GAP = 0.08;

    
    public static final double MAX_INTERIOR_GAP = 0.40;

    
    public static final double DEFAULT_START_ANGLE = 90.0;

    
    public static final Font DEFAULT_LABEL_FONT = new Font("SansSerif",
            Font.PLAIN, 10);

    
    public static final Paint DEFAULT_LABEL_PAINT = Color.BLACK;

    
    public static final Paint DEFAULT_LABEL_BACKGROUND_PAINT = new Color(255,
            255, 192);

    
    public static final Paint DEFAULT_LABEL_OUTLINE_PAINT = Color.BLACK;

    
    public static final Stroke DEFAULT_LABEL_OUTLINE_STROKE = new BasicStroke(
            0.5f);

    
    public static final Paint DEFAULT_LABEL_SHADOW_PAINT = new Color(151, 151,
            151, 128);

    
    public static final double DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW = 0.00001;

    
    private PieDataset<K> dataset;

    
    private int pieIndex;

    
    private double interiorGap;

    
    private boolean circular;

    
    private double startAngle;

    
    private Rotation direction;

    
    private Map<K, Paint> sectionPaintMap;

    
    private transient Paint defaultSectionPaint;

    
    private boolean autoPopulateSectionPaint;

    
    private boolean sectionOutlinesVisible;

    
    private Map<K, Paint> sectionOutlinePaintMap;

    
    private transient Paint defaultSectionOutlinePaint;

    
    private boolean autoPopulateSectionOutlinePaint;

    
    private Map<K, Stroke> sectionOutlineStrokeMap;

    
    private transient Stroke defaultSectionOutlineStroke;

    
    private boolean autoPopulateSectionOutlineStroke;

    
    private transient Paint shadowPaint = Color.GRAY;

    
    private double shadowXOffset = 4.0f;

    
    private double shadowYOffset = 4.0f;

    
    private Map<K, Double> explodePercentages;

    
    private PieSectionLabelGenerator labelGenerator;

    
    private Font labelFont;

    
    private transient Paint labelPaint;

    
    private transient Paint labelBackgroundPaint;

    
    private transient Paint labelOutlinePaint;

    
    private transient Stroke labelOutlineStroke;

    
    private transient Paint labelShadowPaint;

    
    private boolean simpleLabels = true;

    
    private RectangleInsets labelPadding;

    
    private RectangleInsets simpleLabelOffset;

    
    private double maximumLabelWidth = 0.14;

    
    private double labelGap = 0.025;

    
    private boolean labelLinksVisible;

    
    private PieLabelLinkStyle labelLinkStyle = PieLabelLinkStyle.STANDARD;

    
    private double labelLinkMargin = 0.025;

    
    private transient Paint labelLinkPaint = Color.BLACK;

    
    private transient Stroke labelLinkStroke = new BasicStroke(0.5f);

    
    private AbstractPieLabelDistributor labelDistributor;

    
    private PieToolTipGenerator toolTipGenerator;

    
    private PieURLGenerator urlGenerator;

    
    private PieSectionLabelGenerator legendLabelGenerator;

    
    private PieSectionLabelGenerator legendLabelToolTipGenerator;

    
    private PieURLGenerator legendLabelURLGenerator;

    
    private boolean ignoreNullValues;

    
    private boolean ignoreZeroValues;

    
    private transient Shape legendItemShape;

    
    private double minimumArcAngleToDraw;

    
    private ShadowGenerator shadowGenerator;

    
    protected static ResourceBundle localizationResources
            = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    
    static final boolean DEBUG_DRAW_INTERIOR = false;

    
    static final boolean DEBUG_DRAW_LINK_AREA = false;

    
    static final boolean DEBUG_DRAW_PIE_AREA = false;

    
    public PiePlot() {
        this(null);
    }

    
    public PiePlot(PieDataset<K> dataset) {
        super();
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        this.pieIndex = 0;

        this.interiorGap = DEFAULT_INTERIOR_GAP;
        this.circular = true;
        this.startAngle = DEFAULT_START_ANGLE;
        this.direction = Rotation.CLOCKWISE;
        this.minimumArcAngleToDraw = DEFAULT_MINIMUM_ARC_ANGLE_TO_DRAW;

        this.sectionPaintMap = new HashMap<>();
        this.defaultSectionPaint = Color.GRAY;
        this.autoPopulateSectionPaint = true;

        this.sectionOutlinesVisible = true;
        this.sectionOutlinePaintMap = new HashMap<>();
        this.defaultSectionOutlinePaint = DEFAULT_OUTLINE_PAINT;
        this.autoPopulateSectionOutlinePaint = false;

        this.sectionOutlineStrokeMap = new HashMap<>();
        this.defaultSectionOutlineStroke = DEFAULT_OUTLINE_STROKE;
        this.autoPopulateSectionOutlineStroke = false;

        this.explodePercentages = new TreeMap<>();

        this.labelGenerator = new StandardPieSectionLabelGenerator();
        this.labelFont = DEFAULT_LABEL_FONT;
        this.labelPaint = DEFAULT_LABEL_PAINT;
        this.labelBackgroundPaint = DEFAULT_LABEL_BACKGROUND_PAINT;
        this.labelOutlinePaint = DEFAULT_LABEL_OUTLINE_PAINT;
        this.labelOutlineStroke = DEFAULT_LABEL_OUTLINE_STROKE;
        this.labelShadowPaint = DEFAULT_LABEL_SHADOW_PAINT;
        this.labelLinksVisible = true;
        this.labelDistributor = new PieLabelDistributor(0);

        this.simpleLabels = false;
        this.simpleLabelOffset = new RectangleInsets(UnitType.RELATIVE, 0.18,
                0.18, 0.18, 0.18);
        this.labelPadding = new RectangleInsets(2, 2, 2, 2);

        this.toolTipGenerator = null;
        this.urlGenerator = null;
        this.legendLabelGenerator = new StandardPieSectionLabelGenerator();
        this.legendLabelToolTipGenerator = null;
        this.legendLabelURLGenerator = null;
        this.legendItemShape = Plot.DEFAULT_LEGEND_ITEM_CIRCLE;

        this.ignoreNullValues = false;
        this.ignoreZeroValues = false;

        this.shadowGenerator = null;
    }

    
    public PieDataset<K> getDataset() {
        return this.dataset;
    }

    
    public void setDataset(PieDataset<K> dataset) {
        // if there is an existing dataset, remove the plot from the list of
        // change listeners...
        PieDataset<K> existing = this.dataset;
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

    
    public int getPieIndex() {
        return this.pieIndex;
    }

    
    public void setPieIndex(int index) {
        this.pieIndex = index;
    }

    
    public double getStartAngle() {
        return this.startAngle;
    }

    
    public void setStartAngle(double angle) {
        this.startAngle = angle;
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
                "Invalid 'percent' (" + percent + ") argument.");
        }

        if (this.interiorGap != percent) {
            this.interiorGap = percent;
            fireChangeEvent();
        }

    }

    
    public boolean isCircular() {
        return this.circular;
    }

    
    public void setCircular(boolean flag) {
        setCircular(flag, true);
    }

    
    public void setCircular(boolean circular, boolean notify) {
        this.circular = circular;
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public boolean getIgnoreNullValues() {
        return this.ignoreNullValues;
    }

    
    public void setIgnoreNullValues(boolean flag) {
        this.ignoreNullValues = flag;
        fireChangeEvent();
    }

    
    public boolean getIgnoreZeroValues() {
        return this.ignoreZeroValues;
    }

    
    public void setIgnoreZeroValues(boolean flag) {
        this.ignoreZeroValues = flag;
        fireChangeEvent();
    }

    //// SECTION PAINT ////////////////////////////////////////////////////////

    
    protected Paint lookupSectionPaint(K key) {
        return lookupSectionPaint(key, getAutoPopulateSectionPaint());
    }

    
    protected Paint lookupSectionPaint(K key, boolean autoPopulate) { 

        // if not, check if there is a paint defined for the specified key
        Paint result = this.sectionPaintMap.get(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextPaint();
                this.sectionPaintMap.put(key, result);
            } else {
                result = this.defaultSectionPaint;
            }
        } else {
            result = this.defaultSectionPaint;
        }
        return result;
    }

    
    protected K getSectionKey(int section) {
        K key = null;
        if (this.dataset != null) {
            if (section >= 0 && section < this.dataset.getItemCount()) {
                key = this.dataset.getKey(section);
            }
        }
        return key;
    }

    
    public Paint getSectionPaint(K key) {
        // null argument check delegated...
        return this.sectionPaintMap.get(key);
    }

    
    public void setSectionPaint(K key, Paint paint) {
        // null argument check delegated...
        this.sectionPaintMap.put(key, paint);
        fireChangeEvent();
    }

    
    public void clearSectionPaints(boolean notify) {
        this.sectionPaintMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public Paint getDefaultSectionPaint() {
        return this.defaultSectionPaint;
    }

    
    public void setDefaultSectionPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.defaultSectionPaint = paint;
        fireChangeEvent();
    }

    
    public boolean getAutoPopulateSectionPaint() {
        return this.autoPopulateSectionPaint;
    }

    
    public void setAutoPopulateSectionPaint(boolean auto) {
        this.autoPopulateSectionPaint = auto;
        fireChangeEvent();
    }

    //// SECTION OUTLINE PAINT ////////////////////////////////////////////////

    
    public boolean getSectionOutlinesVisible() {
        return this.sectionOutlinesVisible;
    }

    
    public void setSectionOutlinesVisible(boolean visible) {
        this.sectionOutlinesVisible = visible;
        fireChangeEvent();
    }

    
    protected Paint lookupSectionOutlinePaint(K key) {
        return lookupSectionOutlinePaint(key, getAutoPopulateSectionOutlinePaint());
    }

    
    protected Paint lookupSectionOutlinePaint(K key, boolean autoPopulate) {

        // if not, check if there is a paint defined for the specified key
        Paint result = this.sectionOutlinePaintMap.get(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextOutlinePaint();
                this.sectionOutlinePaintMap.put(key, result);
            } else {
                result = this.defaultSectionOutlinePaint;
            }
        } else {
            result = this.defaultSectionOutlinePaint;
        }
        return result;
    }

    
    public Paint getSectionOutlinePaint(K key) {
        // null argument check delegated...
        return this.sectionOutlinePaintMap.get(key);
    }

    
    public void setSectionOutlinePaint(K key, Paint paint) {
        // null argument check delegated...
        this.sectionOutlinePaintMap.put(key, paint);
        fireChangeEvent();
    }

    
    public void clearSectionOutlinePaints(boolean notify) {
        this.sectionOutlinePaintMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public Paint getDefaultSectionOutlinePaint() {
        return this.defaultSectionOutlinePaint;
    }

    
    public void setDefaultSectionOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.defaultSectionOutlinePaint = paint;
        fireChangeEvent();
    }

    
    public boolean getAutoPopulateSectionOutlinePaint() {
        return this.autoPopulateSectionOutlinePaint;
    }

    
    public void setAutoPopulateSectionOutlinePaint(boolean auto) {
        this.autoPopulateSectionOutlinePaint = auto;
        fireChangeEvent();
    }

    //// SECTION OUTLINE STROKE ///////////////////////////////////////////////

    
    protected Stroke lookupSectionOutlineStroke(K key) {
        return lookupSectionOutlineStroke(key, getAutoPopulateSectionOutlineStroke());
    }

    
    protected Stroke lookupSectionOutlineStroke(K key, boolean autoPopulate) {

        // if not, check if there is a stroke defined for the specified key
        Stroke result = this.sectionOutlineStrokeMap.get(key);
        if (result != null) {
            return result;
        }

        // nothing defined - do we autoPopulate?
        if (autoPopulate) {
            DrawingSupplier ds = getDrawingSupplier();
            if (ds != null) {
                result = ds.getNextOutlineStroke();
                this.sectionOutlineStrokeMap.put(key, result);
            } else {
                result = this.defaultSectionOutlineStroke;
            }
        } else {
            result = this.defaultSectionOutlineStroke;
        }
        return result;
    }

    
    public Stroke getSectionOutlineStroke(K key) {
        // null argument check delegated...
        return this.sectionOutlineStrokeMap.get(key);
    }

    
    public void setSectionOutlineStroke(K key, Stroke stroke) {
        // null argument check delegated...
        this.sectionOutlineStrokeMap.put(key, stroke);
        fireChangeEvent();
    }

    
    public void clearSectionOutlineStrokes(boolean notify) {
        this.sectionOutlineStrokeMap.clear();
        if (notify) {
            fireChangeEvent();
        }
    }

    
    public Stroke getDefaultSectionOutlineStroke() {
        return this.defaultSectionOutlineStroke;
    }

    
    public void setDefaultSectionOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.defaultSectionOutlineStroke = stroke;
        fireChangeEvent();
    }

    
    public boolean getAutoPopulateSectionOutlineStroke() {
        return this.autoPopulateSectionOutlineStroke;
    }

    
    public void setAutoPopulateSectionOutlineStroke(boolean auto) {
        this.autoPopulateSectionOutlineStroke = auto;
        fireChangeEvent();
    }

    
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }

    
    public void setShadowPaint(Paint paint) {
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

    
    public double getExplodePercent(K key) {
        double result = 0.0;
        if (this.explodePercentages != null) {
            Number percent = (Number) this.explodePercentages.get(key);
            if (percent != null) {
                result = percent.doubleValue();
            }
        }
        return result;
    }

    
    public void setExplodePercent(K key, double percent) {
        Args.nullNotPermitted(key, "key");
        if (this.explodePercentages == null) {
            this.explodePercentages = new TreeMap<>();
        }
        this.explodePercentages.put(key, percent);
        fireChangeEvent();
    }

    
    public double getMaximumExplodePercent() {
        if (this.dataset == null) {
            return 0.0;
        }
        double result = 0.0;
        for (K key : this.dataset.getKeys()) {
            Double explode = this.explodePercentages.get(key);
            if (explode != null) {
                result = Math.max(result, explode);
            }
        }
        return result;
    }

    
    public PieSectionLabelGenerator getLabelGenerator() {
        return this.labelGenerator;
    }

    
    public void setLabelGenerator(PieSectionLabelGenerator generator) {
        this.labelGenerator = generator;
        fireChangeEvent();
    }

    
    public double getLabelGap() {
        return this.labelGap;
    }

    
    public void setLabelGap(double gap) {
        this.labelGap = gap;
        fireChangeEvent();
    }

    
    public double getMaximumLabelWidth() {
        return this.maximumLabelWidth;
    }

    
    public void setMaximumLabelWidth(double width) {
        this.maximumLabelWidth = width;
        fireChangeEvent();
    }

    
    public boolean getLabelLinksVisible() {
        return this.labelLinksVisible;
    }

    
    public void setLabelLinksVisible(boolean visible) {
        this.labelLinksVisible = visible;
        fireChangeEvent();
    }

    
    public PieLabelLinkStyle getLabelLinkStyle() {
        return this.labelLinkStyle;
    }

    
    public void setLabelLinkStyle(PieLabelLinkStyle style) {
        Args.nullNotPermitted(style, "style");
        this.labelLinkStyle = style;
        fireChangeEvent();
    }

    
    public double getLabelLinkMargin() {
        return this.labelLinkMargin;
    }

    
    public void setLabelLinkMargin(double margin) {
        this.labelLinkMargin = margin;
        fireChangeEvent();
    }

    
    public Paint getLabelLinkPaint() {
        return this.labelLinkPaint;
    }

    
    public void setLabelLinkPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.labelLinkPaint = paint;
        fireChangeEvent();
    }

    
    public Stroke getLabelLinkStroke() {
        return this.labelLinkStroke;
    }

    
    public void setLabelLinkStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.labelLinkStroke = stroke;
        fireChangeEvent();
    }

    
    protected double getLabelLinkDepth() {
        return 0.1;
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

    
    public Paint getLabelBackgroundPaint() {
        return this.labelBackgroundPaint;
    }

    
    public void setLabelBackgroundPaint(Paint paint) {
        this.labelBackgroundPaint = paint;
        fireChangeEvent();
    }

    
    public Paint getLabelOutlinePaint() {
        return this.labelOutlinePaint;
    }

    
    public void setLabelOutlinePaint(Paint paint) {
        this.labelOutlinePaint = paint;
        fireChangeEvent();
    }

    
    public Stroke getLabelOutlineStroke() {
        return this.labelOutlineStroke;
    }

    
    public void setLabelOutlineStroke(Stroke stroke) {
        this.labelOutlineStroke = stroke;
        fireChangeEvent();
    }

    
    public Paint getLabelShadowPaint() {
        return this.labelShadowPaint;
    }

    
    public void setLabelShadowPaint(Paint paint) {
        this.labelShadowPaint = paint;
        fireChangeEvent();
    }

    
    public RectangleInsets getLabelPadding() {
        return this.labelPadding;
    }

    
    public void setLabelPadding(RectangleInsets padding) {
        Args.nullNotPermitted(padding, "padding");
        this.labelPadding = padding;
        fireChangeEvent();
    }

    
    public boolean getSimpleLabels() {
        return this.simpleLabels;
    }

    
    public void setSimpleLabels(boolean simple) {
        this.simpleLabels = simple;
        fireChangeEvent();
    }

    
    public RectangleInsets getSimpleLabelOffset() {
        return this.simpleLabelOffset;
    }

    
    public void setSimpleLabelOffset(RectangleInsets offset) {
        Args.nullNotPermitted(offset, "offset");
        this.simpleLabelOffset = offset;
        fireChangeEvent();
    }

    
    public AbstractPieLabelDistributor getLabelDistributor() {
        return this.labelDistributor;
    }

    
    public void setLabelDistributor(AbstractPieLabelDistributor distributor) {
        Args.nullNotPermitted(distributor, "distributor");
        this.labelDistributor = distributor;
        fireChangeEvent();
    }

    
    public PieToolTipGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }

    
    public void setToolTipGenerator(PieToolTipGenerator generator) {
        this.toolTipGenerator = generator;
        fireChangeEvent();
    }

    
    public PieURLGenerator getURLGenerator() {
        return this.urlGenerator;
    }

    
    public void setURLGenerator(PieURLGenerator generator) {
        this.urlGenerator = generator;
        fireChangeEvent();
    }

    
    public double getMinimumArcAngleToDraw() {
        return this.minimumArcAngleToDraw;
    }

    
    public void setMinimumArcAngleToDraw(double angle) {
        this.minimumArcAngleToDraw = angle;
    }

    
    public Shape getLegendItemShape() {
        return this.legendItemShape;
    }

    
    public void setLegendItemShape(Shape shape) {
        Args.nullNotPermitted(shape, "shape");
        this.legendItemShape = shape;
        fireChangeEvent();
    }

    
    public PieSectionLabelGenerator getLegendLabelGenerator() {
        return this.legendLabelGenerator;
    }

    
    public void setLegendLabelGenerator(PieSectionLabelGenerator generator) {
        Args.nullNotPermitted(generator, "generator");
        this.legendLabelGenerator = generator;
        fireChangeEvent();
    }

    
    public PieSectionLabelGenerator getLegendLabelToolTipGenerator() {
        return this.legendLabelToolTipGenerator;
    }

    
    public void setLegendLabelToolTipGenerator(
            PieSectionLabelGenerator generator) {
        this.legendLabelToolTipGenerator = generator;
        fireChangeEvent();
    }

    
    public PieURLGenerator getLegendLabelURLGenerator() {
        return this.legendLabelURLGenerator;
    }

    
    public void setLegendLabelURLGenerator(PieURLGenerator generator) {
        this.legendLabelURLGenerator = generator;
        fireChangeEvent();
    }

    
    public ShadowGenerator getShadowGenerator() {
        return this.shadowGenerator;
    }

    
    public void setShadowGenerator(ShadowGenerator generator) {
        this.shadowGenerator = generator;
        fireChangeEvent();
    }

    
    public void handleMouseWheelRotation(int rotateClicks) {
        setStartAngle(this.startAngle + rotateClicks * 4.0);
    }

    
    public PiePlotState initialise(Graphics2D g2, Rectangle2D plotArea,
            PiePlot<?> plot, Integer index, PlotRenderingInfo info) {

        PiePlotState state = new PiePlotState(info);
        state.setPassesRequired(2);
        if (this.dataset != null) {
            state.setTotal(DatasetUtils.calculatePieDatasetTotal(
                    plot.getDataset()));
        }
        state.setLatestAngle(plot.getStartAngle());
        return state;

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
            Graphics2D savedG2 = g2;
            boolean suppressShadow = Boolean.TRUE.equals(g2.getRenderingHint(
                    JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION));
            BufferedImage dataImage = null;
            if (this.shadowGenerator != null && !suppressShadow) {
                dataImage = new BufferedImage((int) area.getWidth(),
                    (int) area.getHeight(), BufferedImage.TYPE_INT_ARGB);
                g2 = dataImage.createGraphics();
                g2.translate(-area.getX(), -area.getY());
                g2.setRenderingHints(savedG2.getRenderingHints());
            }
            drawPie(g2, area, info);
            if (this.shadowGenerator != null && !suppressShadow) {
                BufferedImage shadowImage 
                        = this.shadowGenerator.createDropShadow(dataImage);
                g2 = savedG2;
                g2.drawImage(shadowImage, (int) area.getX() 
                        + this.shadowGenerator.calculateOffsetX(), 
                        (int) area.getY()
                        + this.shadowGenerator.calculateOffsetY(), null);
                g2.drawImage(dataImage, (int) area.getX(), (int) area.getY(), 
                        null);
            }
        }
        else {
            drawNoDataMessage(g2, area);
        }

        g2.setClip(savedClip);
        g2.setComposite(originalComposite);

        drawOutline(g2, area);

    }

    
    protected void drawPie(Graphics2D g2, Rectangle2D plotArea,
                           PlotRenderingInfo info) {

        PiePlotState state = initialise(g2, plotArea, this, null, info);

        // adjust the plot area for interior spacing and labels...
        double labelReserve = 0.0;
        if (this.labelGenerator != null && !this.simpleLabels) {
            labelReserve = this.labelGap + this.maximumLabelWidth;
        }
        double gapHorizontal = plotArea.getWidth() * labelReserve * 2.0;
        double gapVertical = plotArea.getHeight() * this.interiorGap * 2.0;


        if (DEBUG_DRAW_INTERIOR) {
            double hGap = plotArea.getWidth() * this.interiorGap;
            double vGap = plotArea.getHeight() * this.interiorGap;

            double igx1 = plotArea.getX() + hGap;
            double igx2 = plotArea.getMaxX() - hGap;
            double igy1 = plotArea.getY() + vGap;
            double igy2 = plotArea.getMaxY() - vGap;
            g2.setPaint(Color.GRAY);
            g2.draw(new Rectangle2D.Double(igx1, igy1, igx2 - igx1,
                    igy2 - igy1));
        }

        double linkX = plotArea.getX() + gapHorizontal / 2;
        double linkY = plotArea.getY() + gapVertical / 2;
        double linkW = plotArea.getWidth() - gapHorizontal;
        double linkH = plotArea.getHeight() - gapVertical;

        // make the link area a square if the pie chart is to be circular...
        if (this.circular) {
            double min = Math.min(linkW, linkH) / 2;
            linkX = (linkX + linkX + linkW) / 2 - min;
            linkY = (linkY + linkY + linkH) / 2 - min;
            linkW = 2 * min;
            linkH = 2 * min;
        }

        // the link area defines the dog leg points for the linking lines to
        // the labels
        Rectangle2D linkArea = new Rectangle2D.Double(linkX, linkY, linkW,
                linkH);
        state.setLinkArea(linkArea);

        if (DEBUG_DRAW_LINK_AREA) {
            g2.setPaint(Color.BLUE);
            g2.draw(linkArea);
            g2.setPaint(Color.YELLOW);
            g2.draw(new Ellipse2D.Double(linkArea.getX(), linkArea.getY(),
                    linkArea.getWidth(), linkArea.getHeight()));
        }

        // the explode area defines the max circle/ellipse for the exploded
        // pie sections.  it is defined by shrinking the linkArea by the
        // linkMargin factor.
        double lm = 0.0;
        if (!this.simpleLabels) {
            lm = this.labelLinkMargin;
        }
        double hh = linkArea.getWidth() * lm * 2.0;
        double vv = linkArea.getHeight() * lm * 2.0;
        Rectangle2D explodeArea = new Rectangle2D.Double(linkX + hh / 2.0,
                linkY + vv / 2.0, linkW - hh, linkH - vv);

        state.setExplodedPieArea(explodeArea);

        // the pie area defines the circle/ellipse for regular pie sections.
        // it is defined by shrinking the explodeArea by the explodeMargin
        // factor.
        double maximumExplodePercent = getMaximumExplodePercent();
        double percent = maximumExplodePercent / (1.0 + maximumExplodePercent);

        double h1 = explodeArea.getWidth() * percent;
        double v1 = explodeArea.getHeight() * percent;
        Rectangle2D pieArea = new Rectangle2D.Double(explodeArea.getX()
                + h1 / 2.0, explodeArea.getY() + v1 / 2.0,
                explodeArea.getWidth() - h1, explodeArea.getHeight() - v1);

        if (DEBUG_DRAW_PIE_AREA) {
            g2.setPaint(Color.GREEN);
            g2.draw(pieArea);
        }
        state.setPieArea(pieArea);
        state.setPieCenterX(pieArea.getCenterX());
        state.setPieCenterY(pieArea.getCenterY());
        state.setPieWRadius(pieArea.getWidth() / 2.0);
        state.setPieHRadius(pieArea.getHeight() / 2.0);

        // plot the data (unless the dataset is null)...
        if ((this.dataset != null) && (this.dataset.getKeys().size() > 0)) {

            List<K> keys = this.dataset.getKeys();
            double totalValue = DatasetUtils.calculatePieDatasetTotal(
                    this.dataset);

            int passesRequired = state.getPassesRequired();
            for (int pass = 0; pass < passesRequired; pass++) {
                double runningTotal = 0.0;
                for (int section = 0; section < keys.size(); section++) {
                    Number n = this.dataset.getValue(section);
                    if (n != null) {
                        double value = n.doubleValue();
                        if (value > 0.0) {
                            runningTotal += value;
                            drawItem(g2, section, explodeArea, state, pass);
                        }
                    }
                }
            }
            if (this.simpleLabels) {
                drawSimpleLabels(g2, keys, totalValue, plotArea, linkArea,
                        state);
            }
            else {
                drawLabels(g2, keys, totalValue, plotArea, linkArea, state);
            }

        }
        else {
            drawNoDataMessage(g2, plotArea);
        }
    }

    
    protected void drawItem(Graphics2D g2, int section, Rectangle2D dataArea,
                            PiePlotState state, int currentPass) {

        Number n = this.dataset.getValue(section);
        if (n == null) {
            return;
        }
        double value = n.doubleValue();
        double angle1 = 0.0;
        double angle2 = 0.0;

        if (this.direction == Rotation.CLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 - value / state.getTotal() * 360.0;
        }
        else if (this.direction == Rotation.ANTICLOCKWISE) {
            angle1 = state.getLatestAngle();
            angle2 = angle1 + value / state.getTotal() * 360.0;
        }
        else {
            throw new IllegalStateException("Rotation type not recognised.");
        }

        double angle = (angle2 - angle1);
        if (Math.abs(angle) > getMinimumArcAngleToDraw()) {
            double ep = 0.0;
            double mep = getMaximumExplodePercent();
            if (mep > 0.0) {
                ep = getExplodePercent(dataset.getKey(section)) / mep;
            }
            Rectangle2D arcBounds = getArcBounds(state.getPieArea(),
                    state.getExplodedPieArea(), angle1, angle, ep);
            Arc2D.Double arc = new Arc2D.Double(arcBounds, angle1, angle,
                    Arc2D.PIE);

            if (currentPass == 0) {
                if (this.shadowPaint != null && this.shadowGenerator == null) {
                    Shape shadowArc = ShapeUtils.createTranslatedShape(
                            arc, (float) this.shadowXOffset,
                            (float) this.shadowYOffset);
                    g2.setPaint(this.shadowPaint);
                    g2.fill(shadowArc);
                }
            }
            else if (currentPass == 1) {
                K key = getSectionKey(section);
                Paint paint = lookupSectionPaint(key, state);
                g2.setPaint(paint);
                g2.fill(arc);

                Paint outlinePaint = lookupSectionOutlinePaint(key);
                Stroke outlineStroke = lookupSectionOutlineStroke(key);
                if (this.sectionOutlinesVisible) {
                    g2.setPaint(outlinePaint);
                    g2.setStroke(outlineStroke);
                    g2.draw(arc);
                }

                // update the linking line target for later
                // add an entity for the pie section
                if (state.getInfo() != null) {
                    EntityCollection entities = state.getEntityCollection();
                    if (entities != null) {
                        String tip = null;
                        if (this.toolTipGenerator != null) {
                            tip = this.toolTipGenerator.generateToolTip(
                                    this.dataset, key);
                        }
                        String url = null;
                        if (this.urlGenerator != null) {
                            url = this.urlGenerator.generateURL(this.dataset,
                                    key, this.pieIndex);
                        }
                        PieSectionEntity entity = new PieSectionEntity(
                                arc, this.dataset, this.pieIndex, section, key,
                                tip, url);
                        entities.add(entity);
                    }
                }
            }
        }
        state.setLatestAngle(angle2);
    }

    
    protected void drawSimpleLabels(Graphics2D g2, List<K> keys,
            double totalValue, Rectangle2D plotArea, Rectangle2D pieArea,
            PiePlotState state) {

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                1.0f));

        Rectangle2D labelsArea = this.simpleLabelOffset.createInsetRectangle(
                pieArea);
        double runningTotal = 0.0;
        for (K key : keys) {
            boolean include;
            double v = 0.0;
            Number n = getDataset().getValue(key);
            if (n == null) {
                include = !getIgnoreNullValues();
            }
            else {
                v = n.doubleValue();
                include = getIgnoreZeroValues() ? v > 0.0 : v >= 0.0;
            }

            if (include) {
                runningTotal = runningTotal + v;
                // work out the mid angle (0 - 90 and 270 - 360) = right,
                // otherwise left
                double mid = getStartAngle() + (getDirection().getFactor()
                        * ((runningTotal - v / 2.0) * 360) / totalValue);

                Arc2D arc = new Arc2D.Double(labelsArea, getStartAngle(),
                        mid - getStartAngle(), Arc2D.OPEN);
                int x = (int) arc.getEndPoint().getX();
                int y = (int) arc.getEndPoint().getY();

                PieSectionLabelGenerator myLabelGenerator = getLabelGenerator();
                if (myLabelGenerator == null) {
                    continue;
                }
                String label = myLabelGenerator.generateSectionLabel(
                        this.dataset, key);
                if (label == null) {
                    continue;
                }
                g2.setFont(this.labelFont);
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D bounds = TextUtils.getTextBounds(label, g2, fm);
                Rectangle2D out = this.labelPadding.createOutsetRectangle(
                        bounds);
                Shape bg = ShapeUtils.createTranslatedShape(out,
                        x - bounds.getCenterX(), y - bounds.getCenterY());
                if (this.labelShadowPaint != null
                        && this.shadowGenerator == null) {
                    Shape shadow = ShapeUtils.createTranslatedShape(bg,
                            this.shadowXOffset, this.shadowYOffset);
                    g2.setPaint(this.labelShadowPaint);
                    g2.fill(shadow);
                }
                if (this.labelBackgroundPaint != null) {
                    g2.setPaint(this.labelBackgroundPaint);
                    g2.fill(bg);
                }
                if (this.labelOutlinePaint != null
                        && this.labelOutlineStroke != null) {
                    g2.setPaint(this.labelOutlinePaint);
                    g2.setStroke(this.labelOutlineStroke);
                    g2.draw(bg);
                }

                g2.setPaint(this.labelPaint);
                g2.setFont(this.labelFont);
                TextUtils.drawAlignedString(label, g2, x, y,
                        TextAnchor.CENTER);

            }
        }

        g2.setComposite(originalComposite);

    }

    
    protected void drawLabels(Graphics2D g2, List<K> keys, double totalValue,
                              Rectangle2D plotArea, Rectangle2D linkArea,
                              PiePlotState state) {

        Composite originalComposite = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                1.0f));

        // classify the keys according to which side the label will appear...
        DefaultKeyedValues leftKeys = new DefaultKeyedValues();
        DefaultKeyedValues rightKeys = new DefaultKeyedValues();

        double runningTotal = 0.0;
        for (K key : keys) {
            boolean include;
            double v = 0.0;
            Number n = this.dataset.getValue(key);
            if (n == null) {
                include = !this.ignoreNullValues;
            }
            else {
                v = n.doubleValue();
                include = this.ignoreZeroValues ? v > 0.0 : v >= 0.0;
            }

            if (include) {
                runningTotal = runningTotal + v;
                // work out the mid angle (0 - 90 and 270 - 360) = right,
                // otherwise left
                double mid = this.startAngle + (this.direction.getFactor()
                        * ((runningTotal - v / 2.0) * 360) / totalValue);
                if (Math.cos(Math.toRadians(mid)) < 0.0) {
                    leftKeys.addValue(key, mid);
                }
                else {
                    rightKeys.addValue(key, mid);
                }
            }
        }

        g2.setFont(getLabelFont());

        // calculate the max label width from the plot dimensions, because
        // a circular pie can leave a lot more room for labels...
        double marginX = plotArea.getX();
        double gap = plotArea.getWidth() * this.labelGap;
        double ww = linkArea.getX() - gap - marginX;
        float labelWidth = (float) this.labelPadding.trimWidth(ww);

        // draw the labels...
        if (this.labelGenerator != null) {
            drawLeftLabels(leftKeys, g2, plotArea, linkArea, labelWidth,
                    state);
            drawRightLabels(rightKeys, g2, plotArea, linkArea, labelWidth,
                    state);
        }
        g2.setComposite(originalComposite);

    }

    
    protected void drawLeftLabels(KeyedValues<K> leftKeys, Graphics2D g2,
                                  Rectangle2D plotArea, Rectangle2D linkArea,
                                  float maxLabelWidth, PiePlotState state) {

        this.labelDistributor.clear();
        double lGap = plotArea.getWidth() * this.labelGap;
        double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;
        for (int i = 0; i < leftKeys.getItemCount(); i++) {
            String label = this.labelGenerator.generateSectionLabel(
                    this.dataset, leftKeys.getKey(i));
            if (label != null) {
                TextBlock block = TextUtils.createTextBlock(label,
                        this.labelFont, this.labelPaint, maxLabelWidth,
                        new G2TextMeasurer(g2));
                TextBox labelBox = new TextBox(block);
                labelBox.setBackgroundPaint(this.labelBackgroundPaint);
                labelBox.setOutlinePaint(this.labelOutlinePaint);
                labelBox.setOutlineStroke(this.labelOutlineStroke);
                if (this.shadowGenerator == null) {
                    labelBox.setShadowPaint(this.labelShadowPaint);
                }
                else {
                    labelBox.setShadowPaint(null);
                }
                labelBox.setInteriorGap(this.labelPadding);
                double theta = Math.toRadians(
                        leftKeys.getValue(i).doubleValue());
                double baseY = state.getPieCenterY() - Math.sin(theta)
                               * verticalLinkRadius;
                double hh = labelBox.getHeight(g2);

                this.labelDistributor.addPieLabelRecord(new PieLabelRecord(
                        leftKeys.getKey(i), theta, baseY, labelBox, hh,
                        lGap / 2.0 + lGap / 2.0 * -Math.cos(theta), 1.0
                        - getLabelLinkDepth()
                        + getExplodePercent(leftKeys.getKey(i))));
            }
        }
        double hh = plotArea.getHeight();
        double gap = hh * getInteriorGap();
        this.labelDistributor.distributeLabels(plotArea.getMinY() + gap,
                hh - 2 * gap);
        for (int i = 0; i < this.labelDistributor.getItemCount(); i++) {
            drawLeftLabel(g2, state,
                    this.labelDistributor.getPieLabelRecord(i));
        }
    }

    
    protected void drawRightLabels(KeyedValues<K> keys, Graphics2D g2,
                                   Rectangle2D plotArea, Rectangle2D linkArea,
                                   float maxLabelWidth, PiePlotState state) {

        // draw the right labels...
        this.labelDistributor.clear();
        double lGap = plotArea.getWidth() * this.labelGap;
        double verticalLinkRadius = state.getLinkArea().getHeight() / 2.0;

        for (int i = 0; i < keys.getItemCount(); i++) {
            String label = this.labelGenerator.generateSectionLabel(
                    this.dataset, keys.getKey(i));

            if (label != null) {
                TextBlock block = TextUtils.createTextBlock(label,
                        this.labelFont, this.labelPaint, maxLabelWidth,
                        new G2TextMeasurer(g2));
                TextBox labelBox = new TextBox(block);
                labelBox.setBackgroundPaint(this.labelBackgroundPaint);
                labelBox.setOutlinePaint(this.labelOutlinePaint);
                labelBox.setOutlineStroke(this.labelOutlineStroke);
                if (this.shadowGenerator == null) {
                    labelBox.setShadowPaint(this.labelShadowPaint);
                }
                else {
                    labelBox.setShadowPaint(null);
                }
                labelBox.setInteriorGap(this.labelPadding);
                double theta = Math.toRadians(keys.getValue(i).doubleValue());
                double baseY = state.getPieCenterY()
                              - Math.sin(theta) * verticalLinkRadius;
                double hh = labelBox.getHeight(g2);
                this.labelDistributor.addPieLabelRecord(new PieLabelRecord(
                        keys.getKey(i), theta, baseY, labelBox, hh,
                        lGap / 2.0 + lGap / 2.0 * Math.cos(theta),
                        1.0 - getLabelLinkDepth()
                        + getExplodePercent(keys.getKey(i))));
            }
        }
        double hh = plotArea.getHeight();
        double gap = 0.00; //hh * getInteriorGap();
        this.labelDistributor.distributeLabels(plotArea.getMinY() + gap,
                hh - 2 * gap);
        for (int i = 0; i < this.labelDistributor.getItemCount(); i++) {
            drawRightLabel(g2, state,
                    this.labelDistributor.getPieLabelRecord(i));
        }

    }

    
    @Override
    public LegendItemCollection getLegendItems() {

        LegendItemCollection result = new LegendItemCollection();
        if (this.dataset == null) {
            return result;
        }
        List<K> keys = this.dataset.getKeys();
        int section = 0;
        Shape shape = getLegendItemShape();
        for (K key : keys) {
            Number n = this.dataset.getValue(key);
            boolean include;
            if (n == null) {
                include = !this.ignoreNullValues;
            }
            else {
                double v = n.doubleValue();
                if (v == 0.0) {
                    include = !this.ignoreZeroValues;
                }
                else {
                    include = v > 0.0;
                }
            }
            if (include) {
                String label = this.legendLabelGenerator.generateSectionLabel(
                        this.dataset, key);
                if (label != null) {
                    String description = label;
                    String toolTipText = null;
                    if (this.legendLabelToolTipGenerator != null) {
                        toolTipText = this.legendLabelToolTipGenerator
                                .generateSectionLabel(this.dataset, key);
                    }
                    String urlText = null;
                    if (this.legendLabelURLGenerator != null) {
                        urlText = this.legendLabelURLGenerator.generateURL(
                                this.dataset, key, this.pieIndex);
                    }
                    Paint paint = lookupSectionPaint(key);
                    Paint outlinePaint = lookupSectionOutlinePaint(key);
                    Stroke outlineStroke = lookupSectionOutlineStroke(key);
                    LegendItem item = new LegendItem(label, description,
                            toolTipText, urlText, true, shape, true, paint,
                            true, outlinePaint, outlineStroke,
                            false,          // line not visible
                            new Line2D.Float(), new BasicStroke(), Color.BLACK);
                    item.setDataset(getDataset());
                    item.setSeriesIndex(this.dataset.getIndex(key));
                    item.setSeriesKey(key);
                    result.add(item);
                }
                section++;
            }
            else {
                section++;
            }
        }
        return result;
    }

    
    @Override
    public String getPlotType() {
        return localizationResources.getString("Pie_Plot");
    }

    
    protected Rectangle2D getArcBounds(Rectangle2D unexploded,
                                       Rectangle2D exploded,
                                       double angle, double extent,
                                       double explodePercent) {

        if (explodePercent == 0.0) {
            return unexploded;
        }
        Arc2D arc1 = new Arc2D.Double(unexploded, angle, extent / 2,
                Arc2D.OPEN);
        Point2D point1 = arc1.getEndPoint();
        Arc2D.Double arc2 = new Arc2D.Double(exploded, angle, extent / 2,
                Arc2D.OPEN);
        Point2D point2 = arc2.getEndPoint();
        double deltaX = (point1.getX() - point2.getX()) * explodePercent;
        double deltaY = (point1.getY() - point2.getY()) * explodePercent;
        return new Rectangle2D.Double(unexploded.getX() - deltaX,
                unexploded.getY() - deltaY, unexploded.getWidth(),
                unexploded.getHeight());
    }

    
    protected void drawLeftLabel(Graphics2D g2, PiePlotState state,
                                 PieLabelRecord record) {

        double anchorX = state.getLinkArea().getMinX();
        double targetX = anchorX - record.getGap();
        double targetY = record.getAllocatedY();

        if (this.labelLinksVisible) {
            double theta = record.getAngle();
            double linkX = state.getPieCenterX() + Math.cos(theta)
                    * state.getPieWRadius() * record.getLinkPercent();
            double linkY = state.getPieCenterY() - Math.sin(theta)
                    * state.getPieHRadius() * record.getLinkPercent();
            double elbowX = state.getPieCenterX() + Math.cos(theta)
                    * state.getLinkArea().getWidth() / 2.0;
            double elbowY = state.getPieCenterY() - Math.sin(theta)
                    * state.getLinkArea().getHeight() / 2.0;
            double anchorY = elbowY;
            g2.setPaint(this.labelLinkPaint);
            g2.setStroke(this.labelLinkStroke);
            PieLabelLinkStyle style = getLabelLinkStyle();
            if (style.equals(PieLabelLinkStyle.STANDARD)) {
                g2.draw(new Line2D.Double(linkX, linkY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, targetX, targetY));
            }
            else if (style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
                QuadCurve2D q = new QuadCurve2D.Float();
                q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
                g2.draw(q);
                g2.draw(new Line2D.Double(elbowX, elbowY, linkX, linkY));
            }
            else if (style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
                CubicCurve2D c = new CubicCurve2D .Float();
                c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY,
                        linkX, linkY);
                g2.draw(c);
            }
        }
        TextBox tb = record.getLabel();
        tb.draw(g2, (float) targetX, (float) targetY, RectangleAnchor.RIGHT);

    }

    
    protected void drawRightLabel(Graphics2D g2, PiePlotState state,
                                  PieLabelRecord record) {

        double anchorX = state.getLinkArea().getMaxX();
        double targetX = anchorX + record.getGap();
        double targetY = record.getAllocatedY();

        if (this.labelLinksVisible) {
            double theta = record.getAngle();
            double linkX = state.getPieCenterX() + Math.cos(theta)
                    * state.getPieWRadius() * record.getLinkPercent();
            double linkY = state.getPieCenterY() - Math.sin(theta)
                    * state.getPieHRadius() * record.getLinkPercent();
            double elbowX = state.getPieCenterX() + Math.cos(theta)
                    * state.getLinkArea().getWidth() / 2.0;
            double elbowY = state.getPieCenterY() - Math.sin(theta)
                    * state.getLinkArea().getHeight() / 2.0;
            double anchorY = elbowY;
            g2.setPaint(this.labelLinkPaint);
            g2.setStroke(this.labelLinkStroke);
            PieLabelLinkStyle style = getLabelLinkStyle();
            if (style.equals(PieLabelLinkStyle.STANDARD)) {
                g2.draw(new Line2D.Double(linkX, linkY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, elbowX, elbowY));
                g2.draw(new Line2D.Double(anchorX, anchorY, targetX, targetY));
            }
            else if (style.equals(PieLabelLinkStyle.QUAD_CURVE)) {
                QuadCurve2D q = new QuadCurve2D.Float();
                q.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY);
                g2.draw(q);
                g2.draw(new Line2D.Double(elbowX, elbowY, linkX, linkY));
            }
            else if (style.equals(PieLabelLinkStyle.CUBIC_CURVE)) {
                CubicCurve2D c = new CubicCurve2D .Float();
                c.setCurve(targetX, targetY, anchorX, anchorY, elbowX, elbowY,
                        linkX, linkY);
                g2.draw(c);
            }
        }

        TextBox tb = record.getLabel();
        tb.draw(g2, (float) targetX, (float) targetY, RectangleAnchor.LEFT);

    }

    
    protected Point2D getArcCenter(PiePlotState state, K key) {
        Point2D center = new Point2D.Double(state.getPieCenterX(), state
            .getPieCenterY());

        double ep = getExplodePercent(key);
        double mep = getMaximumExplodePercent();
        if (mep > 0.0) {
            ep = ep / mep;
        }
        if (ep != 0) {
            Rectangle2D pieArea = state.getPieArea();
            Rectangle2D expPieArea = state.getExplodedPieArea();
            double angle1, angle2;
            Number n = this.dataset.getValue(key);
            double value = n.doubleValue();

            if (this.direction == Rotation.CLOCKWISE) {
                angle1 = state.getLatestAngle();
                angle2 = angle1 - value / state.getTotal() * 360.0;
            } else if (this.direction == Rotation.ANTICLOCKWISE) {
                angle1 = state.getLatestAngle();
                angle2 = angle1 + value / state.getTotal() * 360.0;
            } else {
                throw new IllegalStateException("Rotation type not recognised.");
            }
            double angle = (angle2 - angle1);

            Arc2D arc1 = new Arc2D.Double(pieArea, angle1, angle / 2,
                    Arc2D.OPEN);
            Point2D point1 = arc1.getEndPoint();
            Arc2D.Double arc2 = new Arc2D.Double(expPieArea, angle1, angle / 2,
                    Arc2D.OPEN);
            Point2D point2 = arc2.getEndPoint();
            double deltaX = (point1.getX() - point2.getX()) * ep;
            double deltaY = (point1.getY() - point2.getY()) * ep;

            center = new Point2D.Double(state.getPieCenterX() - deltaX,
                     state.getPieCenterY() - deltaY);

        }
        return center;
    }

    
    protected Paint lookupSectionPaint(K key, PiePlotState state) {
        Paint paint = lookupSectionPaint(key, getAutoPopulateSectionPaint());
        // for a RadialGradientPaint we adjust the center and radius to match
        // the current pie segment...
        if (paint instanceof RadialGradientPaint) {
            RadialGradientPaint rgp = (RadialGradientPaint) paint;
            Point2D center = getArcCenter(state, key);
            float radius = (float) Math.max(state.getPieHRadius(), 
                    state.getPieWRadius());
            float[] fractions = rgp.getFractions();
            Color[] colors = rgp.getColors();
            paint = new RadialGradientPaint(center, radius, fractions, colors);
        }
        return paint;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PiePlot)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        PiePlot that = (PiePlot) obj;
        if (this.pieIndex != that.pieIndex) {
            return false;
        }
        if (this.interiorGap != that.interiorGap) {
            return false;
        }
        if (this.circular != that.circular) {
            return false;
        }
        if (this.startAngle != that.startAngle) {
            return false;
        }
        if (this.direction != that.direction) {
            return false;
        }
        if (this.ignoreZeroValues != that.ignoreZeroValues) {
            return false;
        }
        if (this.ignoreNullValues != that.ignoreNullValues) {
            return false;
        }
        if (!PaintUtils.equal(this.sectionPaintMap, that.sectionPaintMap)) {
            return false;
        }
        if (!PaintUtils.equal(this.defaultSectionPaint,
                that.defaultSectionPaint)) {
            return false;
        }
        if (this.sectionOutlinesVisible != that.sectionOutlinesVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.sectionOutlinePaintMap, that.sectionOutlinePaintMap)) {
            return false;
        }
        if (!PaintUtils.equal(this.defaultSectionOutlinePaint,
                that.defaultSectionOutlinePaint)) {
            return false;
        }
        if (!Objects.equals(this.sectionOutlineStrokeMap, that.sectionOutlineStrokeMap)) {
            return false;
        }
        if (!Objects.equals(this.defaultSectionOutlineStroke, that.defaultSectionOutlineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.shadowPaint, that.shadowPaint)) {
            return false;
        }
        if (!(this.shadowXOffset == that.shadowXOffset)) {
            return false;
        }
        if (!(this.shadowYOffset == that.shadowYOffset)) {
            return false;
        }
        if (!Objects.equals(this.explodePercentages, that.explodePercentages)) {
            return false;
        }
        if (!Objects.equals(this.labelGenerator, that.labelGenerator)) {
            return false;
        }
        if (!Objects.equals(this.labelFont, that.labelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelBackgroundPaint,
                that.labelBackgroundPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelOutlinePaint,
                that.labelOutlinePaint)) {
            return false;
        }
        if (!Objects.equals(this.labelOutlineStroke, that.labelOutlineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelShadowPaint,
                that.labelShadowPaint)) {
            return false;
        }
        if (this.simpleLabels != that.simpleLabels) {
            return false;
        }
        if (!this.simpleLabelOffset.equals(that.simpleLabelOffset)) {
            return false;
        }
        if (!this.labelPadding.equals(that.labelPadding)) {
            return false;
        }
        if (!(this.maximumLabelWidth == that.maximumLabelWidth)) {
            return false;
        }
        if (!(this.labelGap == that.labelGap)) {
            return false;
        }
        if (!(this.labelLinkMargin == that.labelLinkMargin)) {
            return false;
        }
        if (this.labelLinksVisible != that.labelLinksVisible) {
            return false;
        }
        if (!this.labelLinkStyle.equals(that.labelLinkStyle)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelLinkPaint, that.labelLinkPaint)) {
            return false;
        }
        if (!Objects.equals(this.labelLinkStroke, that.labelLinkStroke)) {
            return false;
        }
        if (!Objects.equals(this.toolTipGenerator, that.toolTipGenerator)) {
            return false;
        }
        if (!Objects.equals(this.urlGenerator, that.urlGenerator)) {
            return false;
        }
        if (!(this.minimumArcAngleToDraw == that.minimumArcAngleToDraw)) {
            return false;
        }
        if (!ShapeUtils.equal(this.legendItemShape, that.legendItemShape)) {
            return false;
        }
        if (!Objects.equals(this.legendLabelGenerator, that.legendLabelGenerator)) {
            return false;
        }
        if (!Objects.equals(this.legendLabelToolTipGenerator, that.legendLabelToolTipGenerator)) {
            return false;
        }
        if (!Objects.equals(this.legendLabelURLGenerator, that.legendLabelURLGenerator)) {
            return false;
        }
        if (this.autoPopulateSectionPaint != that.autoPopulateSectionPaint) {
            return false;
        }
        if (this.autoPopulateSectionOutlinePaint
                != that.autoPopulateSectionOutlinePaint) {
            return false;
        }
        if (this.autoPopulateSectionOutlineStroke
                != that.autoPopulateSectionOutlineStroke) {
            return false;
        }
        if (!Objects.equals(this.shadowGenerator, that.shadowGenerator)) {
            return false;
        }
        // can't find any difference...
        return true;
    }

    
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 73 * hash + this.pieIndex;
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.interiorGap) ^ (Double.doubleToLongBits(this.interiorGap) >>> 32));
        hash = 73 * hash + (this.circular ? 1 : 0);
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.startAngle) ^ (Double.doubleToLongBits(this.startAngle) >>> 32));
        hash = 73 * hash + Objects.hashCode(this.direction);
        hash = 73 * hash + Objects.hashCode(this.sectionPaintMap);
        hash = 73 * hash + Objects.hashCode(this.defaultSectionPaint);
        hash = 73 * hash + (this.autoPopulateSectionPaint ? 1 : 0);
        hash = 73 * hash + (this.sectionOutlinesVisible ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.sectionOutlinePaintMap);
        hash = 73 * hash + Objects.hashCode(this.defaultSectionOutlinePaint);
        hash = 73 * hash + (this.autoPopulateSectionOutlinePaint ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.sectionOutlineStrokeMap);
        hash = 73 * hash + Objects.hashCode(this.defaultSectionOutlineStroke);
        hash = 73 * hash + (this.autoPopulateSectionOutlineStroke ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.shadowPaint);
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.shadowXOffset) ^ (Double.doubleToLongBits(this.shadowXOffset) >>> 32));
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.shadowYOffset) ^ (Double.doubleToLongBits(this.shadowYOffset) >>> 32));
        hash = 73 * hash + Objects.hashCode(this.explodePercentages);
        hash = 73 * hash + Objects.hashCode(this.labelGenerator);
        hash = 73 * hash + Objects.hashCode(this.labelFont);
        hash = 73 * hash + Objects.hashCode(this.labelPaint);
        hash = 73 * hash + Objects.hashCode(this.labelBackgroundPaint);
        hash = 73 * hash + Objects.hashCode(this.labelOutlinePaint);
        hash = 73 * hash + Objects.hashCode(this.labelOutlineStroke);
        hash = 73 * hash + Objects.hashCode(this.labelShadowPaint);
        hash = 73 * hash + (this.simpleLabels ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.labelPadding);
        hash = 73 * hash + Objects.hashCode(this.simpleLabelOffset);
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.maximumLabelWidth) ^ (Double.doubleToLongBits(this.maximumLabelWidth) >>> 32));
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.labelGap) ^ (Double.doubleToLongBits(this.labelGap) >>> 32));
        hash = 73 * hash + (this.labelLinksVisible ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.labelLinkStyle);
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.labelLinkMargin) ^ (Double.doubleToLongBits(this.labelLinkMargin) >>> 32));
        hash = 73 * hash + Objects.hashCode(this.labelLinkPaint);
        hash = 73 * hash + Objects.hashCode(this.labelLinkStroke);
        hash = 73 * hash + Objects.hashCode(this.toolTipGenerator);
        hash = 73 * hash + Objects.hashCode(this.urlGenerator);
        hash = 73 * hash + Objects.hashCode(this.legendLabelGenerator);
        hash = 73 * hash + Objects.hashCode(this.legendLabelToolTipGenerator);
        hash = 73 * hash + Objects.hashCode(this.legendLabelURLGenerator);
        hash = 73 * hash + (this.ignoreNullValues ? 1 : 0);
        hash = 73 * hash + (this.ignoreZeroValues ? 1 : 0);
        hash = 73 * hash + Objects.hashCode(this.legendItemShape);
        hash = 73 * hash + (int) (Double.doubleToLongBits(this.minimumArcAngleToDraw) ^ (Double.doubleToLongBits(this.minimumArcAngleToDraw) >>> 32));
        hash = 73 * hash + Objects.hashCode(this.shadowGenerator);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        PiePlot clone = (PiePlot) super.clone();
        clone.sectionPaintMap = new HashMap<>(this.sectionPaintMap);
        clone.sectionOutlinePaintMap = new HashMap<>(this.sectionOutlinePaintMap);
        clone.sectionOutlineStrokeMap = new HashMap<>(this.sectionOutlineStrokeMap);
        clone.explodePercentages = new TreeMap<>(this.explodePercentages);
        if (this.labelGenerator != null) {
            clone.labelGenerator = CloneUtils.clone(this.labelGenerator);
        }
        if (clone.dataset != null) {
            clone.dataset.addChangeListener(clone);
        }
        clone.urlGenerator = CloneUtils.copy(this.urlGenerator);
        clone.legendItemShape = CloneUtils.clone(this.legendItemShape);
        clone.legendLabelGenerator = CloneUtils.copy(this.legendLabelGenerator);
        clone.legendLabelToolTipGenerator = CloneUtils.clone(this.legendLabelToolTipGenerator);
        clone.legendLabelURLGenerator = CloneUtils.copy(this.legendLabelURLGenerator);
        return clone;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.defaultSectionPaint, stream);
        SerialUtils.writePaint(this.defaultSectionOutlinePaint, stream);
        SerialUtils.writeStroke(this.defaultSectionOutlineStroke, stream);
        SerialUtils.writePaint(this.shadowPaint, stream);
        SerialUtils.writePaint(this.labelPaint, stream);
        SerialUtils.writePaint(this.labelBackgroundPaint, stream);
        SerialUtils.writePaint(this.labelOutlinePaint, stream);
        SerialUtils.writeStroke(this.labelOutlineStroke, stream);
        SerialUtils.writePaint(this.labelShadowPaint, stream);
        SerialUtils.writePaint(this.labelLinkPaint, stream);
        SerialUtils.writeStroke(this.labelLinkStroke, stream);
        SerialUtils.writeShape(this.legendItemShape, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.defaultSectionPaint = SerialUtils.readPaint(stream);
        this.defaultSectionOutlinePaint = SerialUtils.readPaint(stream);
        this.defaultSectionOutlineStroke = SerialUtils.readStroke(stream);
        this.shadowPaint = SerialUtils.readPaint(stream);
        this.labelPaint = SerialUtils.readPaint(stream);
        this.labelBackgroundPaint = SerialUtils.readPaint(stream);
        this.labelOutlinePaint = SerialUtils.readPaint(stream);
        this.labelOutlineStroke = SerialUtils.readStroke(stream);
        this.labelShadowPaint = SerialUtils.readPaint(stream);
        this.labelLinkPaint = SerialUtils.readPaint(stream);
        this.labelLinkStroke = SerialUtils.readStroke(stream);
        this.legendItemShape = SerialUtils.readShape(stream);
    }

}
