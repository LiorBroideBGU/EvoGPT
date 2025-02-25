

package org.jfree.chart;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.jfree.chart.block.BlockParams;
import org.jfree.chart.block.EntityBlockResult;
import org.jfree.chart.block.LengthConstraintType;
import org.jfree.chart.block.RectangleConstraint;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.JFreeChartEntity;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressEventType;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.event.TitleChangeEvent;
import org.jfree.chart.event.TitleChangeListener;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.legend.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.api.HorizontalAlignment;
import org.jfree.chart.api.RectangleAlignment;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.block.Size2D;
import org.jfree.chart.api.VerticalAlignment;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.swing.ChartPanel;
import org.jfree.data.Range;


public class JFreeChart implements Drawable, TitleChangeListener,
        PlotChangeListener, ChartElement, Serializable, Cloneable {

    
    private static final long serialVersionUID = -3470703747817429120L;

    
    public static final Font DEFAULT_TITLE_FONT
            = new Font("SansSerif", Font.BOLD, 18);

    
    public static final Paint DEFAULT_BACKGROUND_PAINT
            = UIManager.getColor("Panel.background");

    
    public static final Image DEFAULT_BACKGROUND_IMAGE = null;

    
    public static final RectangleAlignment DEFAULT_BACKGROUND_IMAGE_ALIGNMENT = RectangleAlignment.FILL;

    
    public static final float DEFAULT_BACKGROUND_IMAGE_ALPHA = 0.5f;

    
    public static final RenderingHints.Key KEY_SUPPRESS_SHADOW_GENERATION
            = new RenderingHints.Key(0) {
        @Override
        public boolean isCompatibleValue(Object val) {
            return val instanceof Boolean;
        }
    };
    
    
    private transient RenderingHints renderingHints;

    
    private String id;
    
    
    private boolean borderVisible;

    
    private transient Stroke borderStroke;

    
    private transient Paint borderPaint;

    
    private RectangleInsets padding;

    
    private TextTitle title;

    
    private List<Title> subtitles;

    
    private Plot plot;

    
    private transient Paint backgroundPaint;

    
    private transient Image backgroundImage;  // todo: not serialized yet

    
    private RectangleAlignment backgroundImageAlignment = RectangleAlignment.FILL;

    
    private float backgroundImageAlpha = 0.5f;

    
    private transient EventListenerList changeListeners;

    
    private transient EventListenerList progressListeners;

    
    private boolean notify;

    
    private boolean elementHinting;
    
    
    public JFreeChart(Plot plot) {
        this(null, null, plot, true);
    }

    
    public JFreeChart(String title, Plot plot) {
        this(title, JFreeChart.DEFAULT_TITLE_FONT, plot, true);
    }

    
    public JFreeChart(String title, Font titleFont, Plot plot,
                      boolean createLegend) {

        Args.nullNotPermitted(plot, "plot");
        this.id = null;
        plot.setChart(this);
        
        // create storage for listeners...
        this.progressListeners = new EventListenerList();
        this.changeListeners = new EventListenerList();
        this.notify = true;  // default is to notify listeners when the
                             // chart changes

        this.renderingHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        // added the following hint because of 
        // http://stackoverflow.com/questions/7785082/
        this.renderingHints.put(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        this.borderVisible = false;
        this.borderStroke = new BasicStroke(1.0f);
        this.borderPaint = Color.BLACK;

        this.padding = RectangleInsets.ZERO_INSETS;

        this.plot = plot;
        plot.addChangeListener(this);

        this.subtitles = new ArrayList<>();

        // create a legend, if requested...
        if (createLegend) {
            LegendTitle legend = new LegendTitle(this.plot);
            legend.setMargin(new RectangleInsets(1.0, 1.0, 1.0, 1.0));
            legend.setBackgroundPaint(Color.WHITE);
            legend.setPosition(RectangleEdge.BOTTOM);
            this.subtitles.add(legend);
            legend.addChangeListener(this);
        }

        // add the chart title, if one has been specified...
        if (title != null) {
            if (titleFont == null) {
                titleFont = DEFAULT_TITLE_FONT;
            }
            this.title = new TextTitle(title, titleFont);
            this.title.addChangeListener(this);
        }

        this.backgroundPaint = DEFAULT_BACKGROUND_PAINT;

        this.backgroundImage = DEFAULT_BACKGROUND_IMAGE;
        this.backgroundImageAlignment = DEFAULT_BACKGROUND_IMAGE_ALIGNMENT;
        this.backgroundImageAlpha = DEFAULT_BACKGROUND_IMAGE_ALPHA;
    }

    
    public String getID() {
        return this.id;
    }
    
    
    public void setID(String id) {
        this.id = id;
    }
    
    
    public boolean getElementHinting() {
        return this.elementHinting;
    }
    
    
    public void setElementHinting(boolean hinting) {
        this.elementHinting = hinting;
    }
    
    
    public RenderingHints getRenderingHints() {
        return this.renderingHints;
    }

    
    public void setRenderingHints(RenderingHints renderingHints) {
        Args.nullNotPermitted(renderingHints, "renderingHints");
        this.renderingHints = renderingHints;
        fireChartChanged();
    }

    
    public boolean isBorderVisible() {
        return this.borderVisible;
    }

    
    public void setBorderVisible(boolean visible) {
        this.borderVisible = visible;
        fireChartChanged();
    }

    
    public Stroke getBorderStroke() {
        return this.borderStroke;
    }

    
    public void setBorderStroke(Stroke stroke) {
        this.borderStroke = stroke;
        fireChartChanged();
    }

    
    public Paint getBorderPaint() {
        return this.borderPaint;
    }

    
    public void setBorderPaint(Paint paint) {
        this.borderPaint = paint;
        fireChartChanged();
    }

    
    public RectangleInsets getPadding() {
        return this.padding;
    }

    
    public void setPadding(RectangleInsets padding) {
        Args.nullNotPermitted(padding, "padding");
        this.padding = padding;
        notifyListeners(new ChartChangeEvent(this));
    }

    
    public TextTitle getTitle() {
        return this.title;
    }

    
    public void setTitle(TextTitle title) {
        if (this.title != null) {
            this.title.removeChangeListener(this);
        }
        this.title = title;
        if (title != null) {
            title.addChangeListener(this);
        }
        fireChartChanged();
    }

    
    public void setTitle(String text) {
        if (text != null) {
            if (this.title == null) {
                setTitle(new TextTitle(text, JFreeChart.DEFAULT_TITLE_FONT));
            } else {
                this.title.setText(text);
            }
        }
        else {
            setTitle((TextTitle) null);
        }
    }

    
    public void addLegend(LegendTitle legend) {
        addSubtitle(legend);
    }

    
    public LegendTitle getLegend() {
        return getLegend(0);
    }

    
    public LegendTitle getLegend(int index) {
        int seen = 0;
        for (Title subtitle : this.subtitles) {
            if (subtitle instanceof LegendTitle) {
                if (seen == index) {
                    return (LegendTitle) subtitle;
                }
                else {
                    seen++;
                }
            }
        }
        return null;
    }

    
    public void removeLegend() {
        removeSubtitle(getLegend());
    }

    
    public List<Title> getSubtitles() {
        return new ArrayList<>(this.subtitles);
    }

    
    public void setSubtitles(List<Title> subtitles) {
        Args.nullNotPermitted(subtitles, "subtitles");
        setNotify(false);
        clearSubtitles();
        for (Title t: subtitles) {
            if (t != null) {
                addSubtitle(t);
            }
        }
        setNotify(true);  // this fires a ChartChangeEvent
    }

    
    public int getSubtitleCount() {
        return this.subtitles.size();
    }

    
    public Title getSubtitle(int index) {
        if ((index < 0) || (index >= getSubtitleCount())) {
            throw new IllegalArgumentException("Index out of range.");
        }
        return this.subtitles.get(index);
    }

    
    public void addSubtitle(Title subtitle) {
        Args.nullNotPermitted(subtitle, "subtitle");
        this.subtitles.add(subtitle);
        subtitle.addChangeListener(this);
        fireChartChanged();
    }

    
    public void addSubtitle(int index, Title subtitle) {
        Args.requireInRange(index, "index", 0, getSubtitleCount());
        Args.nullNotPermitted(subtitle, "subtitle");
        this.subtitles.add(index, subtitle);
        subtitle.addChangeListener(this);
        fireChartChanged();
    }

    
    public void clearSubtitles() {
        for (Title t : this.subtitles) {
            t.removeChangeListener(this);
        }
        this.subtitles.clear();
        fireChartChanged();
    }

    
    public void removeSubtitle(Title title) {
        this.subtitles.remove(title);
        fireChartChanged();
    }

    
    public Plot getPlot() {
        return this.plot;
    }

    
    public boolean getAntiAlias() {
        Object val = this.renderingHints.get(RenderingHints.KEY_ANTIALIASING);
        return RenderingHints.VALUE_ANTIALIAS_ON.equals(val);
    }

    
    public void setAntiAlias(boolean flag) {
        Object hint = flag ? RenderingHints.VALUE_ANTIALIAS_ON 
                : RenderingHints.VALUE_ANTIALIAS_OFF;
        this.renderingHints.put(RenderingHints.KEY_ANTIALIASING, hint);
        fireChartChanged();
    }

    
    public Object getTextAntiAlias() {
        return this.renderingHints.get(RenderingHints.KEY_TEXT_ANTIALIASING);
    }

    
    public void setTextAntiAlias(boolean flag) {
        if (flag) {
            setTextAntiAlias(RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        } else {
            setTextAntiAlias(RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        }
    }

    
    public void setTextAntiAlias(Object val) {
        this.renderingHints.put(RenderingHints.KEY_TEXT_ANTIALIASING, val);
        notifyListeners(new ChartChangeEvent(this));
    }

    
    public Paint getBackgroundPaint() {
        return this.backgroundPaint;
    }

    
    public void setBackgroundPaint(Paint paint) {
        if (this.backgroundPaint != null) {
            if (!this.backgroundPaint.equals(paint)) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        } else {
            if (paint != null) {
                this.backgroundPaint = paint;
                fireChartChanged();
            }
        }
    }

    
    public Image getBackgroundImage() {
        return this.backgroundImage;
    }

    
    public void setBackgroundImage(Image image) {
        if (this.backgroundImage != null) {
            if (!this.backgroundImage.equals(image)) {
                this.backgroundImage = image;
                fireChartChanged();
            }
        } else {
            if (image != null) {
                this.backgroundImage = image;
                fireChartChanged();
            }
        }
    }

    
    public RectangleAlignment getBackgroundImageAlignment() {
        return this.backgroundImageAlignment;
    }

    
    public void setBackgroundImageAlignment(RectangleAlignment alignment) {
        Args.nullNotPermitted(alignment, "alignment");
        if (this.backgroundImageAlignment != alignment) {
            this.backgroundImageAlignment = alignment;
            fireChartChanged();
        }
    }

    
    public float getBackgroundImageAlpha() {
        return this.backgroundImageAlpha;
    }

    
    public void setBackgroundImageAlpha(float alpha) {
        if (this.backgroundImageAlpha != alpha) {
            this.backgroundImageAlpha = alpha;
            fireChartChanged();
        }
    }

    
    public boolean isNotify() {
        return this.notify;
    }

    
    public void setNotify(boolean notify) {
        this.notify = notify;
        // if the flag is being set to true, there may be queued up changes...
        if (notify) {
            notifyListeners(new ChartChangeEvent(this));
        }
    }

    @Override
    public void receive(ChartElementVisitor visitor) {
        this.title.receive(visitor);
        this.subtitles.forEach(subtitle -> {
            subtitle.receive(visitor);
        });
        this.plot.receive(visitor);
        visitor.visit(this);
    }
    
    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area) {
        draw(g2, area, null, null);
    }

    
    public void draw(Graphics2D g2, Rectangle2D area, ChartRenderingInfo info) {
        draw(g2, area, null, info);
    }

    
    public void draw(Graphics2D g2, Rectangle2D chartArea, Point2D anchor,
             ChartRenderingInfo info) {

        notifyListeners(new ChartProgressEvent(this, this,
                ChartProgressEventType.DRAWING_STARTED, 0));
        
        if (this.elementHinting) {
            Map<String, String> m = new HashMap<>();
            if (this.id != null) {
                m.put("id", this.id);
            }
            m.put("ref", "JFREECHART_TOP_LEVEL");            
            g2.setRenderingHint(ChartHints.KEY_BEGIN_ELEMENT, m);            
        }
        
        EntityCollection entities = null;
        // record the chart area, if info is requested...
        if (info != null) {
            info.clear();
            info.setChartArea(chartArea);
            entities = info.getEntityCollection();
        }
        if (entities != null) {
            entities.add(new JFreeChartEntity((Rectangle2D) chartArea.clone(),
                    this));
        }

        // ensure no drawing occurs outside chart area...
        Shape savedClip = g2.getClip();
        g2.clip(chartArea);

        g2.addRenderingHints(this.renderingHints);

        // draw the chart background...
        if (this.backgroundPaint != null) {
            g2.setPaint(this.backgroundPaint);
            g2.fill(chartArea);
        }

        if (this.backgroundImage != null) {
            Composite originalComposite = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,
                    this.backgroundImageAlpha));
            Rectangle2D dest = new Rectangle2D.Double(0.0, 0.0,
                    this.backgroundImage.getWidth(null),
                    this.backgroundImage.getHeight(null));
            this.backgroundImageAlignment.align(dest, chartArea);
            g2.drawImage(this.backgroundImage, (int) dest.getX(),
                    (int) dest.getY(), (int) dest.getWidth(),
                    (int) dest.getHeight(), null);
            g2.setComposite(originalComposite);
        }

        if (isBorderVisible()) {
            Paint paint = getBorderPaint();
            Stroke stroke = getBorderStroke();
            if (paint != null && stroke != null) {
                Rectangle2D borderArea = new Rectangle2D.Double(
                        chartArea.getX(), chartArea.getY(),
                        chartArea.getWidth() - 1.0, chartArea.getHeight()
                        - 1.0);
                g2.setPaint(paint);
                g2.setStroke(stroke);
                g2.draw(borderArea);
            }
        }

        // draw the title and subtitles...
        Rectangle2D nonTitleArea = new Rectangle2D.Double();
        nonTitleArea.setRect(chartArea);
        this.padding.trim(nonTitleArea);

        if (this.title != null && this.title.isVisible()) {
            EntityCollection e = drawTitle(this.title, g2, nonTitleArea,
                    (entities != null));
            if (e != null && entities != null) {
                entities.addAll(e);
            }
        }

        for (Title currentTitle : this.subtitles) {
            if (currentTitle.isVisible()) {
                EntityCollection e = drawTitle(currentTitle, g2, nonTitleArea,
                        (entities != null));
                if (e != null && entities != null) {
                    entities.addAll(e);
                }
            }
        }

        Rectangle2D plotArea = nonTitleArea;

        // draw the plot (axes and data visualisation)
        PlotRenderingInfo plotInfo = null;
        if (info != null) {
            plotInfo = info.getPlotInfo();
        }
        this.plot.draw(g2, plotArea, anchor, null, plotInfo);
        g2.setClip(savedClip);
        if (this.elementHinting) {         
            g2.setRenderingHint(ChartHints.KEY_END_ELEMENT, Boolean.TRUE);            
        }

        notifyListeners(new ChartProgressEvent(this, this,
                ChartProgressEventType.DRAWING_FINISHED, 100));
    }

    
    private Rectangle2D createAlignedRectangle2D(Size2D dimensions,
            Rectangle2D frame, HorizontalAlignment hAlign,
            VerticalAlignment vAlign) {
        Args.nullNotPermitted(hAlign, "hAlign");
        Args.nullNotPermitted(vAlign, "vAlign");
        double x = Double.NaN;
        double y = Double.NaN;
        switch (hAlign) {
            case LEFT:
                x = frame.getX();
                break;
            case CENTER:
                x = frame.getCenterX() - (dimensions.width / 2.0);
                break;
            case RIGHT:
                x = frame.getMaxX() - dimensions.width;
                break;
            default:
                throw new IllegalStateException("Unexpected enum value " + hAlign);
        }
        switch (vAlign) {
            case TOP:
                y = frame.getY();
                break;
            case CENTER:
                y = frame.getCenterY() - (dimensions.height / 2.0);
                break;
            case BOTTOM:
                y = frame.getMaxY() - dimensions.height;
                break;
            default:
                throw new IllegalStateException("Unexpected enum value " + hAlign);
        }

        return new Rectangle2D.Double(x, y, dimensions.width,
                dimensions.height);
    }

    
    protected EntityCollection drawTitle(Title t, Graphics2D g2,
                                         Rectangle2D area, boolean entities) {

        Args.nullNotPermitted(t, "t");
        Args.nullNotPermitted(area, "area");
        Rectangle2D titleArea;
        RectangleEdge position = t.getPosition();
        double ww = area.getWidth();
        if (ww <= 0.0) {
            return null;
        }
        double hh = area.getHeight();
        if (hh <= 0.0) {
            return null;
        }
        RectangleConstraint constraint = new RectangleConstraint(ww,
                new Range(0.0, ww), LengthConstraintType.RANGE, hh,
                new Range(0.0, hh), LengthConstraintType.RANGE);
        Object retValue = null;
        BlockParams p = new BlockParams();
        p.setGenerateEntities(entities);
        switch (position) {
            case TOP: {
                Size2D size = t.arrange(g2, constraint);
                titleArea = createAlignedRectangle2D(size, area,
                        t.getHorizontalAlignment(), VerticalAlignment.TOP);
                retValue = t.draw(g2, titleArea, p);
                area.setRect(area.getX(), Math.min(area.getY() + size.height,
                        area.getMaxY()), area.getWidth(), Math.max(area.getHeight()
                        - size.height, 0));
                break;
            }
            case BOTTOM: {
                Size2D size = t.arrange(g2, constraint);
                titleArea = createAlignedRectangle2D(size, area,
                        t.getHorizontalAlignment(), VerticalAlignment.BOTTOM);
                retValue = t.draw(g2, titleArea, p);
                area.setRect(area.getX(), area.getY(), area.getWidth(),
                        area.getHeight() - size.height);
                break;
            }
            case RIGHT: {
                Size2D size = t.arrange(g2, constraint);
                titleArea = createAlignedRectangle2D(size, area,
                        HorizontalAlignment.RIGHT, t.getVerticalAlignment());
                retValue = t.draw(g2, titleArea, p);
                area.setRect(area.getX(), area.getY(), area.getWidth()
                        - size.width, area.getHeight());
                break;
            }
            case LEFT: {
                Size2D size = t.arrange(g2, constraint);
                titleArea = createAlignedRectangle2D(size, area,
                        HorizontalAlignment.LEFT, t.getVerticalAlignment());
                retValue = t.draw(g2, titleArea, p);
                area.setRect(area.getX() + size.width, area.getY(), area.getWidth()
                        - size.width, area.getHeight());
                break;
            }
            default: {
                throw new RuntimeException("Unrecognised title position.");
            }
        }
        EntityCollection result = null;
        if (retValue instanceof EntityBlockResult) {
            EntityBlockResult ebr = (EntityBlockResult) retValue;
            result = ebr.getEntityCollection();
        }
        return result;
    }

    
    public BufferedImage createBufferedImage(int width, int height) {
        return createBufferedImage(width, height, null);
    }

    
    public BufferedImage createBufferedImage(int width, int height,
                                             ChartRenderingInfo info) {
        return createBufferedImage(width, height, BufferedImage.TYPE_INT_ARGB,
                info);
    }

    
    public BufferedImage createBufferedImage(int width, int height,
            int imageType, ChartRenderingInfo info) {
        BufferedImage image = new BufferedImage(width, height, imageType);
        Graphics2D g2 = image.createGraphics();
        draw(g2, new Rectangle2D.Double(0, 0, width, height), null, info);
        g2.dispose();
        return image;
    }

    
    public BufferedImage createBufferedImage(int imageWidth,
                                             int imageHeight,
                                             double drawWidth,
                                             double drawHeight,
                                             ChartRenderingInfo info) {

        BufferedImage image = new BufferedImage(imageWidth, imageHeight,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = image.createGraphics();
        double scaleX = imageWidth / drawWidth;
        double scaleY = imageHeight / drawHeight;
        AffineTransform st = AffineTransform.getScaleInstance(scaleX, scaleY);
        g2.transform(st);
        draw(g2, new Rectangle2D.Double(0, 0, drawWidth, drawHeight), null,
                info);
        g2.dispose();
        return image;
    }

    
    public void handleClick(int x, int y, ChartRenderingInfo info) {
        // pass the click on to the plot...
        // rely on the plot to post a plot change event and redraw the chart...
        this.plot.handleClick(x, y, info.getPlotInfo());
    }

    
    public void addChangeListener(ChartChangeListener listener) {
        Args.nullNotPermitted(listener, "listener");
        this.changeListeners.add(ChartChangeListener.class, listener);
    }

    
    public void removeChangeListener(ChartChangeListener listener) {
        Args.nullNotPermitted(listener, "listener");
        this.changeListeners.remove(ChartChangeListener.class, listener);
    }

    
    public void fireChartChanged() {
        ChartChangeEvent event = new ChartChangeEvent(this);
        notifyListeners(event);
    }

    
    protected void notifyListeners(ChartChangeEvent event) {
        if (this.notify) {
            Object[] listeners = this.changeListeners.getListenerList();
            for (int i = listeners.length - 2; i >= 0; i -= 2) {
                if (listeners[i] == ChartChangeListener.class) {
                    ((ChartChangeListener) listeners[i + 1]).chartChanged(
                            event);
                }
            }
        }
    }

    
    public void addProgressListener(ChartProgressListener listener) {
        this.progressListeners.add(ChartProgressListener.class, listener);
    }

    
    public void removeProgressListener(ChartProgressListener listener) {
        this.progressListeners.remove(ChartProgressListener.class, listener);
    }

    
    protected void notifyListeners(ChartProgressEvent event) {
        Object[] listeners = this.progressListeners.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ChartProgressListener.class) {
                ((ChartProgressListener) listeners[i + 1]).chartProgress(event);
            }
        }
    }

    
    @Override
    public void titleChanged(TitleChangeEvent event) {
        event.setChart(this);
        notifyListeners(event);
    }

    
    @Override
    public void plotChanged(PlotChangeEvent event) {
        event.setChart(this);
        notifyListeners(event);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JFreeChart)) {
            return false;
        }
        JFreeChart that = (JFreeChart) obj;
        if (!this.renderingHints.equals(that.renderingHints)) {
            return false;
        }
        if (this.borderVisible != that.borderVisible) {
            return false;
        }
        if (!Objects.equals(this.borderStroke, that.borderStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.borderPaint, that.borderPaint)) {
            return false;
        }
        if (!this.padding.equals(that.padding)) {
            return false;
        }
        if (!Objects.equals(this.title, that.title)) {
            return false;
        }
        if (!Objects.equals(this.subtitles, that.subtitles)) {
            return false;
        }
        if (!Objects.equals(this.plot, that.plot)) {
            return false;
        }
        if (!PaintUtils.equal(
            this.backgroundPaint, that.backgroundPaint
        )) {
            return false;
        }
        if (!Objects.equals(this.backgroundImage, that.backgroundImage)) {
            return false;
        }
        if (this.backgroundImageAlignment != that.backgroundImageAlignment) {
            return false;
        }
        if (this.backgroundImageAlpha != that.backgroundImageAlpha) {
            return false;
        }
        if (this.notify != that.notify) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.renderingHints);
        hash = 59 * hash + (this.borderVisible ? 1 : 0);
        hash = 59 * hash + Objects.hashCode(this.borderStroke);
        hash = 59 * hash + Objects.hashCode(this.borderPaint);
        hash = 59 * hash + Objects.hashCode(this.padding);
        hash = 59 * hash + Objects.hashCode(this.title);
        hash = 59 * hash + Objects.hashCode(this.subtitles);
        hash = 59 * hash + Objects.hashCode(this.plot);
        hash = 59 * hash + Objects.hashCode(this.backgroundPaint);
        hash = 59 * hash + Objects.hashCode(this.backgroundImage);
        hash = 59 * hash + Objects.hashCode(this.backgroundImageAlignment);
        hash = 59 * hash + Float.floatToIntBits(this.backgroundImageAlpha);
        hash = 59 * hash + (this.notify ? 1 : 0);
        return hash;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeStroke(this.borderStroke, stream);
        SerialUtils.writePaint(this.borderPaint, stream);
        SerialUtils.writePaint(this.backgroundPaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.borderStroke = SerialUtils.readStroke(stream);
        this.borderPaint = SerialUtils.readPaint(stream);
        this.backgroundPaint = SerialUtils.readPaint(stream);
        this.progressListeners = new EventListenerList();
        this.changeListeners = new EventListenerList();
        this.renderingHints = new RenderingHints(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        this.renderingHints.put(RenderingHints.KEY_STROKE_CONTROL,
                RenderingHints.VALUE_STROKE_PURE);
        
        // register as a listener with sub-components...
        if (this.title != null) {
            this.title.addChangeListener(this);
        }

        for (int i = 0; i < getSubtitleCount(); i++) {
            getSubtitle(i).addChangeListener(this);
        }
        this.plot.addChangeListener(this);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        JFreeChart chart = (JFreeChart) super.clone();

        chart.renderingHints = (RenderingHints) this.renderingHints.clone();
        // private boolean borderVisible;
        // private transient Stroke borderStroke;
        // private transient Paint borderPaint;

        if (this.title != null) {
            chart.title = (TextTitle) this.title.clone();
            chart.title.addChangeListener(chart);
        }

        chart.subtitles = new ArrayList<>();
        for (int i = 0; i < getSubtitleCount(); i++) {
            Title subtitle = (Title) getSubtitle(i).clone();
            chart.subtitles.add(subtitle);
            subtitle.addChangeListener(chart);
        }

        if (this.plot != null) {
            chart.plot = (Plot) this.plot.clone();
            chart.plot.addChangeListener(chart);
        }

        chart.progressListeners = new EventListenerList();
        chart.changeListeners = new EventListenerList();
        return chart;
    }


}
