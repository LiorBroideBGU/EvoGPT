

package org.jfree.chart.swing;

import java.awt.AWTEvent;
import java.awt.AlphaComposite;
import java.awt.Composite;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.datatransfer.Clipboard;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.EventListenerList;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.ChartTransferable;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;

import org.jfree.chart.swing.editor.ChartEditor;
import org.jfree.chart.swing.editor.ChartEditorManager;
import org.jfree.chart.entity.ChartEntity;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.event.ChartChangeEvent;
import org.jfree.chart.event.ChartChangeListener;
import org.jfree.chart.event.ChartProgressEvent;
import org.jfree.chart.event.ChartProgressListener;
import org.jfree.chart.plot.Pannable;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.Zoomable;
import org.jfree.chart.internal.Args;


@SuppressWarnings("unused")
public class ChartPanel extends JPanel implements ChartChangeListener,
        ChartProgressListener, ActionListener, MouseListener,
        MouseMotionListener, OverlayChangeListener, Printable, Serializable {

    
    protected static final long serialVersionUID = 6046366297214274674L;

    
    public static final boolean DEFAULT_BUFFER_USED = true;

    
    public static final int DEFAULT_WIDTH = 1024;

    
    public static final int DEFAULT_HEIGHT = 768;

    
    public static final int DEFAULT_MINIMUM_DRAW_WIDTH = 300;

    
    public static final int DEFAULT_MINIMUM_DRAW_HEIGHT = 200;

    
    public static final int DEFAULT_MAXIMUM_DRAW_WIDTH = 1024;

    
    public static final int DEFAULT_MAXIMUM_DRAW_HEIGHT = 768;

    
    public static final String PROPERTIES_COMMAND = "PROPERTIES";

    
    public static final String COPY_COMMAND = "COPY";

    
    public static final String SAVE_COMMAND = "SAVE";

    
    protected static final String SAVE_AS_PNG_COMMAND = "SAVE_AS_PNG";
    
    
    protected static final String SAVE_AS_PNG_SIZE_COMMAND = "SAVE_AS_PNG_SIZE";

    
    protected static final String SAVE_AS_SVG_COMMAND = "SAVE_AS_SVG";
    
    
    protected static final String SAVE_AS_PDF_COMMAND = "SAVE_AS_PDF";
    
    
    public static final String PRINT_COMMAND = "PRINT";

    
    public static final String ZOOM_IN_BOTH_COMMAND = "ZOOM_IN_BOTH";

    
    public static final String ZOOM_IN_DOMAIN_COMMAND = "ZOOM_IN_DOMAIN";

    
    public static final String ZOOM_IN_RANGE_COMMAND = "ZOOM_IN_RANGE";

    
    public static final String ZOOM_OUT_BOTH_COMMAND = "ZOOM_OUT_BOTH";

    
    public static final String ZOOM_OUT_DOMAIN_COMMAND = "ZOOM_DOMAIN_BOTH";

    
    public static final String ZOOM_OUT_RANGE_COMMAND = "ZOOM_RANGE_BOTH";

    
    public static final String ZOOM_RESET_BOTH_COMMAND = "ZOOM_RESET_BOTH";

    
    public static final String ZOOM_RESET_DOMAIN_COMMAND = "ZOOM_RESET_DOMAIN";

    
    public static final String ZOOM_RESET_RANGE_COMMAND = "ZOOM_RESET_RANGE";

    // default modifiers for zooming, private to avoid constant inlining,
    // publicly available through getDefaultDragModifiersEx()
    private static final int DEFAULT_DRAG_MODIFIERS_EX;

    // mask for all modifier keys to check for
    private static final int MODIFIERS_EX_MASK =
            InputEvent.SHIFT_DOWN_MASK |
            InputEvent.CTRL_DOWN_MASK |
            InputEvent.META_DOWN_MASK |
            InputEvent.ALT_DOWN_MASK;

    static {
        int dragModifiers = InputEvent.CTRL_DOWN_MASK;
        // for MacOSX we can't use the CTRL key for mouse drags, see:
        // http://developer.apple.com/qa/qa2004/qa1362.html
        String osName = System.getProperty("os.name").toLowerCase();
        if (osName.startsWith("mac os x")) {
            dragModifiers = InputEvent.ALT_DOWN_MASK;
        }
        DEFAULT_DRAG_MODIFIERS_EX = dragModifiers;
    }

    
    public static int getDefaultDragModifiersEx() {
        return DEFAULT_DRAG_MODIFIERS_EX;
    }

    
    protected JFreeChart chart;

    
    protected transient EventListenerList chartMouseListeners;

    
    protected boolean useBuffer;

    
    protected boolean refreshBuffer;

    
    protected transient Image chartBuffer;

    
    protected int chartBufferHeight;

    
    protected int chartBufferWidth;

    
    protected int minimumDrawWidth;

    
    protected int minimumDrawHeight;

    
    protected int maximumDrawWidth;

    
    protected int maximumDrawHeight;

    
    protected JPopupMenu popup;

    
    protected ChartRenderingInfo info;

    
    protected Point2D anchor;

    
    protected double scaleX;

    
    protected double scaleY;

    
    protected PlotOrientation orientation = PlotOrientation.VERTICAL;

    
    protected boolean domainZoomable = false;

    
    protected boolean rangeZoomable = false;

    
    private SelectionZoomStrategy selectionZoomStrategy = new DefaultSelectionZoomStrategy();

    
    protected JMenuItem zoomInBothMenuItem;

    
    protected JMenuItem zoomInDomainMenuItem;

    
    protected JMenuItem zoomInRangeMenuItem;

    
    protected JMenuItem zoomOutBothMenuItem;

    
    protected JMenuItem zoomOutDomainMenuItem;

    
    protected JMenuItem zoomOutRangeMenuItem;

    
    protected JMenuItem zoomResetBothMenuItem;

    
    protected JMenuItem zoomResetDomainMenuItem;

    
    protected JMenuItem zoomResetRangeMenuItem;

    
    protected File defaultDirectoryForSaveAs;

    
    protected boolean enforceFileExtensions;

    
    protected boolean ownToolTipDelaysActive;

    
    protected int originalToolTipInitialDelay;

    
    protected int originalToolTipReshowDelay;

    
    protected int originalToolTipDismissDelay;

    
    protected int ownToolTipInitialDelay;

    
    protected int ownToolTipReshowDelay;

    
    protected int ownToolTipDismissDelay;

    
    protected double zoomInFactor = 0.5;

    
    protected double zoomOutFactor = 2.0;

    
    protected boolean zoomAroundAnchor;

    
    protected static ResourceBundle localizationResources
            = ResourceBundle.getBundle("org.jfree.chart.LocalizationBundle");

    
    protected double panW, panH;

    
    protected Point panLast;

    
    protected int panMask = getDefaultDragModifiersEx();

    
    protected int zoomMask = 0;

    
    protected final Map<Integer, Integer> panButtonMasks = new HashMap<>(3);

    
    protected final Map<Integer, Integer> zoomButtonMasks = new HashMap<>(3);

    
    protected List<Overlay> overlays;
    
    
    public ChartPanel(JFreeChart chart) {
        this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT,
            DEFAULT_MINIMUM_DRAW_WIDTH, DEFAULT_MINIMUM_DRAW_HEIGHT,
            DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT,
            DEFAULT_BUFFER_USED,
            true,  // properties
            true,  // save
            true,  // print
            true,  // zoom
            true   // tooltips
        );

    }

    
    public ChartPanel(JFreeChart chart, boolean useBuffer) {

        this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_MINIMUM_DRAW_WIDTH,
                DEFAULT_MINIMUM_DRAW_HEIGHT, DEFAULT_MAXIMUM_DRAW_WIDTH,
                DEFAULT_MAXIMUM_DRAW_HEIGHT, useBuffer,
                true,  // properties
                true,  // save
                true,  // print
                true,  // zoom
                true   // tooltips
                );

    }

    
    public ChartPanel(JFreeChart chart, boolean properties, boolean save,
            boolean print, boolean zoom, boolean tooltips) {

        this(chart, DEFAULT_WIDTH, DEFAULT_HEIGHT,
             DEFAULT_MINIMUM_DRAW_WIDTH, DEFAULT_MINIMUM_DRAW_HEIGHT,
             DEFAULT_MAXIMUM_DRAW_WIDTH, DEFAULT_MAXIMUM_DRAW_HEIGHT,
             DEFAULT_BUFFER_USED, properties, save, print, zoom, tooltips);

    }

    
    public ChartPanel(JFreeChart chart, int width, int height,
            int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
            int maximumDrawHeight, boolean useBuffer, boolean properties,
            boolean save, boolean print, boolean zoom, boolean tooltips) {

        this(chart, width, height, minimumDrawWidth, minimumDrawHeight,
                maximumDrawWidth, maximumDrawHeight, useBuffer, properties,
                true, save, print, zoom, tooltips);
    }

    
    public ChartPanel(JFreeChart chart, int width, int height,
           int minimumDrawWidth, int minimumDrawHeight, int maximumDrawWidth,
           int maximumDrawHeight, boolean useBuffer, boolean properties,
           boolean copy, boolean save, boolean print, boolean zoom,
           boolean tooltips) {

        setChart(chart);
        this.chartMouseListeners = new EventListenerList();
        this.info = new ChartRenderingInfo();
        setPreferredSize(new Dimension(width, height));
        this.useBuffer = useBuffer;
        this.refreshBuffer = false;
        this.minimumDrawWidth = minimumDrawWidth;
        this.minimumDrawHeight = minimumDrawHeight;
        this.maximumDrawWidth = maximumDrawWidth;
        this.maximumDrawHeight = maximumDrawHeight;

        // set up popup menu...
        this.popup = null;
        if (properties || copy || save || print || zoom) {
            this.popup = createPopupMenu(properties, copy, save, print, zoom);
        }

        enableEvents(AWTEvent.MOUSE_EVENT_MASK);
        enableEvents(AWTEvent.MOUSE_MOTION_EVENT_MASK);
        setDisplayToolTips(tooltips);
        addMouseListener(this);
        addMouseMotionListener(this);

        this.defaultDirectoryForSaveAs = null;
        this.enforceFileExtensions = true;

        // initialize ChartPanel-specific tool tip delays with
        // values the from ToolTipManager.sharedInstance()
        ToolTipManager ttm = ToolTipManager.sharedInstance();
        this.ownToolTipInitialDelay = ttm.getInitialDelay();
        this.ownToolTipDismissDelay = ttm.getDismissDelay();
        this.ownToolTipReshowDelay = ttm.getReshowDelay();

        this.zoomAroundAnchor = false;

        this.overlays = new ArrayList<>();
    }

    
    public JFreeChart getChart() {
        return this.chart;
    }

    
    public void setChart(JFreeChart chart) {

        // stop listening for changes to the existing chart
        if (this.chart != null) {
            this.chart.removeChangeListener(this);
            this.chart.removeProgressListener(this);
        }

        // add the new chart
        this.chart = chart;
        if (chart != null) {
            this.chart.addChangeListener(this);
            this.chart.addProgressListener(this);
            Plot plot = chart.getPlot();
            this.domainZoomable = false;
            this.rangeZoomable = false;
            if (plot instanceof Zoomable) {
                Zoomable z = (Zoomable) plot;
                this.domainZoomable = z.isDomainZoomable();
                this.rangeZoomable = z.isRangeZoomable();
                this.orientation = z.getOrientation();
            }
        }
        else {
            this.domainZoomable = false;
            this.rangeZoomable = false;
        }
        if (this.useBuffer) {
            this.refreshBuffer = true;
        }
        repaint();

    }

    
    public int getMinimumDrawWidth() {
        return this.minimumDrawWidth;
    }

    
    public void setMinimumDrawWidth(int width) {
        this.minimumDrawWidth = width;
    }

    
    public int getMaximumDrawWidth() {
        return this.maximumDrawWidth;
    }

    
    public void setMaximumDrawWidth(int width) {
        this.maximumDrawWidth = width;
    }

    
    public int getMinimumDrawHeight() {
        return this.minimumDrawHeight;
    }

    
    public void setMinimumDrawHeight(int height) {
        this.minimumDrawHeight = height;
    }

    
    public int getMaximumDrawHeight() {
        return this.maximumDrawHeight;
    }

    
    public void setMaximumDrawHeight(int height) {
        this.maximumDrawHeight = height;
    }

    
    public double getScaleX() {
        return this.scaleX;
    }

    
    public double getScaleY() {
        return this.scaleY;
    }

    
    public Point2D getAnchor() {
        return this.anchor;
    }

    
    protected void setAnchor(Point2D anchor) {
        this.anchor = anchor;
    }

    
    public JPopupMenu getPopupMenu() {
        return this.popup;
    }

    
    public void setPopupMenu(JPopupMenu popup) {
        this.popup = popup;
    }

    
    public ChartRenderingInfo getChartRenderingInfo() {
        return this.info;
    }

    
    public void setMouseZoomable(boolean flag) {
        setMouseZoomable(flag, true);
    }

    
    public void setMouseZoomable(boolean flag, boolean fillRectangle) {
        setDomainZoomable(flag);
        setRangeZoomable(flag);
        setFillZoomRectangle(fillRectangle);
    }

    
    public void setDefaultPanModifiersEx(int modifiersEx) {
        this.panMask = modifiersEx;
    }

    
    public void setDefaultZoomModifiersEx(int modifiersEx) {
        this.zoomMask = modifiersEx;
    }

    
    public void setPanModifiersEx(int mouseButton, int modifiersEx) {
        panButtonMasks.put(mouseButton, modifiersEx);
    }

    
    public void setZoomModifiersEx(int mouseButton, int modifiersEx) {
        zoomButtonMasks.put(mouseButton, modifiersEx);
    }

    
    public boolean isDomainZoomable() {
        return this.domainZoomable;
    }

    
    public void setDomainZoomable(boolean flag) {
        if (flag) {
            Plot plot = this.chart.getPlot();
            if (plot instanceof Zoomable) {
                Zoomable z = (Zoomable) plot;
                this.domainZoomable = z.isDomainZoomable();
            }
        }
        else {
            this.domainZoomable = false;
        }
    }

    
    public boolean isRangeZoomable() {
        return this.rangeZoomable;
    }

    
    public void setRangeZoomable(boolean flag) {
        if (flag) {
            Plot plot = this.chart.getPlot();
            if (plot instanceof Zoomable) {
                Zoomable z = (Zoomable) plot;
                this.rangeZoomable = z.isRangeZoomable();
            }
        }
        else {
            this.rangeZoomable = false;
        }
    }

    
    public SelectionZoomStrategy getSelectionZoomStrategy() {
        return selectionZoomStrategy;
    }

    
    public void setSelectionZoomStrategy(SelectionZoomStrategy selectionZoomStrategy) {
        this.selectionZoomStrategy = selectionZoomStrategy;
    }

    
    public boolean getFillZoomRectangle() {
        return this.selectionZoomStrategy.getFillZoomRectangle();
    }

    
    public void setFillZoomRectangle(boolean flag) {
        this.selectionZoomStrategy.setFillZoomRectangle(flag);
    }

    
    public int getZoomTriggerDistance() {
        return this.selectionZoomStrategy.getZoomTriggerDistance();
    }

    
    public void setZoomTriggerDistance(int distance) {
        this.selectionZoomStrategy.setZoomTriggerDistance(distance);
    }

    
    public File getDefaultDirectoryForSaveAs() {
        return this.defaultDirectoryForSaveAs;
    }

    
    public void setDefaultDirectoryForSaveAs(File directory) {
        if (directory != null) {
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException(
                        "The 'directory' argument is not a directory.");
            }
        }
        this.defaultDirectoryForSaveAs = directory;
    }

    
    public boolean isEnforceFileExtensions() {
        return this.enforceFileExtensions;
    }

    
    public void setEnforceFileExtensions(boolean enforce) {
        this.enforceFileExtensions = enforce;
    }

    
    public boolean getZoomAroundAnchor() {
        return this.zoomAroundAnchor;
    }

    
    public void setZoomAroundAnchor(boolean zoomAroundAnchor) {
        this.zoomAroundAnchor = zoomAroundAnchor;
    }

    
    public Paint getZoomFillPaint() {
        return selectionZoomStrategy.getZoomFillPaint();
    }

    
    public void setZoomFillPaint(Paint paint) {
        selectionZoomStrategy.setZoomFillPaint(paint);
    }

    
    public Paint getZoomOutlinePaint() {
        return selectionZoomStrategy.getZoomOutlinePaint();
    }

    
    public void setZoomOutlinePaint(Paint paint) {
        this.selectionZoomStrategy.setZoomOutlinePaint(paint);
    }

    
    protected MouseWheelHandler mouseWheelHandler;

    
    public boolean isMouseWheelEnabled() {
        return this.mouseWheelHandler != null;
    }

    
    public void setMouseWheelEnabled(boolean flag) {
        if (flag && this.mouseWheelHandler == null) {
            this.mouseWheelHandler = new MouseWheelHandler(this);
        }
        else if (!flag && this.mouseWheelHandler != null) {
            this.removeMouseWheelListener(this.mouseWheelHandler);
            this.mouseWheelHandler = null;
        } 
    }

    
    public void addOverlay(Overlay overlay) {
        Args.nullNotPermitted(overlay, "overlay");
        this.overlays.add(overlay);
        overlay.addChangeListener(this);
        repaint();
    }

    
    public void removeOverlay(Overlay overlay) {
        Args.nullNotPermitted(overlay, "overlay");
        boolean removed = this.overlays.remove(overlay);
        if (removed) {
            overlay.removeChangeListener(this);
            repaint();
        }
    }

    
    @Override
    public void overlayChanged(OverlayChangeEvent event) {
        repaint();
    }

    
    public void setDisplayToolTips(boolean flag) {
        if (flag) {
            ToolTipManager.sharedInstance().registerComponent(this);
        }
        else {
            ToolTipManager.sharedInstance().unregisterComponent(this);
        }
    }

    
    @Override
    public String getToolTipText(MouseEvent e) {
        String result = null;
        if (this.info != null) {
            EntityCollection entities = this.info.getEntityCollection();
            if (entities != null) {
                Insets insets = getInsets();
                ChartEntity entity = entities.getEntity(
                        (int) ((e.getX() - insets.left) / this.scaleX),
                        (int) ((e.getY() - insets.top) / this.scaleY));
                if (entity != null) {
                    result = entity.getToolTipText();
                }
            }
        }
        return result;
    }

    
    public Point translateJava2DToScreen(Point2D java2DPoint) {
        Insets insets = getInsets();
        int x = (int) (java2DPoint.getX() * this.scaleX + insets.left);
        int y = (int) (java2DPoint.getY() * this.scaleY + insets.top);
        return new Point(x, y);
    }

    
    public Point2D translateScreenToJava2D(Point screenPoint) {
        Insets insets = getInsets();
        double x = (screenPoint.getX() - insets.left) / this.scaleX;
        double y = (screenPoint.getY() - insets.top) / this.scaleY;
        return new Point2D.Double(x, y);
    }

    
    public Rectangle2D scale(Rectangle2D rect) {
        Insets insets = getInsets();
        double x = rect.getX() * getScaleX() + insets.left;
        double y = rect.getY() * getScaleY() + insets.top;
        double w = rect.getWidth() * getScaleX();
        double h = rect.getHeight() * getScaleY();
        return new Rectangle2D.Double(x, y, w, h);
    }

    
    public ChartEntity getEntityForPoint(int viewX, int viewY) {

        ChartEntity result = null;
        if (this.info != null) {
            Insets insets = getInsets();
            double x = (viewX - insets.left) / this.scaleX;
            double y = (viewY - insets.top) / this.scaleY;
            EntityCollection entities = this.info.getEntityCollection();
            result = entities != null ? entities.getEntity(x, y) : null;
        }
        return result;

    }

    
    public boolean getRefreshBuffer() {
        return this.refreshBuffer;
    }

    
    public void setRefreshBuffer(boolean flag) {
        this.refreshBuffer = flag;
    }

    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (this.chart == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        
        // first determine the size of the chart rendering area...
        Dimension size = getSize();
        Insets insets = getInsets();
        Rectangle2D available = new Rectangle2D.Double(insets.left, insets.top,
                size.getWidth() - insets.left - insets.right,
                size.getHeight() - insets.top - insets.bottom);

        // work out if scaling is required...
        boolean scale = false;
        double drawWidth = available.getWidth();
        double drawHeight = available.getHeight();
        this.scaleX = 1.0;
        this.scaleY = 1.0;

        if (drawWidth < this.minimumDrawWidth) {
            this.scaleX = drawWidth / this.minimumDrawWidth;
            drawWidth = this.minimumDrawWidth;
            scale = true;
        }
        else if (drawWidth > this.maximumDrawWidth) {
            this.scaleX = drawWidth / this.maximumDrawWidth;
            drawWidth = this.maximumDrawWidth;
            scale = true;
        }

        if (drawHeight < this.minimumDrawHeight) {
            this.scaleY = drawHeight / this.minimumDrawHeight;
            drawHeight = this.minimumDrawHeight;
            scale = true;
        }
        else if (drawHeight > this.maximumDrawHeight) {
            this.scaleY = drawHeight / this.maximumDrawHeight;
            drawHeight = this.maximumDrawHeight;
            scale = true;
        }

        Rectangle2D chartArea = new Rectangle2D.Double(0.0, 0.0, drawWidth,
                drawHeight);

        // are we using the chart buffer?
        if (this.useBuffer) {

            // for better rendering on the HiDPI monitors upscaling the buffer to the "native" resoution
            // instead of using logical one provided by Swing
            final AffineTransform globalTransform = ((Graphics2D) g).getTransform();
            final double globalScaleX = globalTransform.getScaleX();
            final double globalScaleY = globalTransform.getScaleY();

            final int scaledWidth = (int) (available.getWidth() * globalScaleX);
            final int scaledHeight = (int) (available.getHeight() * globalScaleY);

            // do we need to resize the buffer?
            if ((this.chartBuffer == null)
                    || (this.chartBufferWidth != scaledWidth)
                    || (this.chartBufferHeight != scaledHeight)) {
                this.chartBufferWidth = scaledWidth;
                this.chartBufferHeight = scaledHeight;
                GraphicsConfiguration gc = g2.getDeviceConfiguration();
                this.chartBuffer = gc.createCompatibleImage(
                        this.chartBufferWidth, this.chartBufferHeight,
                        Transparency.TRANSLUCENT);
                this.refreshBuffer = true;
            }

            // do we need to redraw the buffer?
            if (this.refreshBuffer) {

                this.refreshBuffer = false; // clear the flag

                // scale graphics of the buffer to the same value as global
                // Swing graphics - this allow to paint all elements as usual
                // but applies all necessary smoothing
                Graphics2D bufferG2 = (Graphics2D) this.chartBuffer.getGraphics();
                bufferG2.scale(globalScaleX, globalScaleY);

                Rectangle2D bufferArea = new Rectangle2D.Double(
                        0, 0, available.getWidth(), available.getHeight());

                // make the background of the buffer clear and transparent
                Composite savedComposite = bufferG2.getComposite();
                bufferG2.setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR, 0.0f));
                Rectangle r = new Rectangle(0, 0, (int) available.getWidth(), (int) available.getHeight());
                bufferG2.fill(r);
                bufferG2.setComposite(savedComposite);
                
                if (scale) {
                    AffineTransform saved = bufferG2.getTransform();
                    AffineTransform st = AffineTransform.getScaleInstance(
                            this.scaleX, this.scaleY);
                    bufferG2.transform(st);
                    this.chart.draw(bufferG2, chartArea, this.anchor,
                            this.info);
                    bufferG2.setTransform(saved);
                } else {
                    this.chart.draw(bufferG2, bufferArea, this.anchor,
                            this.info);
                }
                bufferG2.dispose();
            }

            // zap the buffer onto the panel...
            g2.drawImage(this.chartBuffer, insets.left, insets.top, (int) available.getWidth(), (int) available.getHeight(), this);
            g2.addRenderingHints(this.chart.getRenderingHints()); // bug#187

        } else { // redrawing the chart every time...
            AffineTransform saved = g2.getTransform();
            g2.translate(insets.left, insets.top);
            if (scale) {
                AffineTransform st = AffineTransform.getScaleInstance(
                        this.scaleX, this.scaleY);
                g2.transform(st);
            }
            this.chart.draw(g2, chartArea, this.anchor, this.info);
            g2.setTransform(saved);

        }

        for (Overlay overlay : this.overlays) {
            overlay.paintOverlay(g2, this);
        }

        // redraw the zoom rectangle (if present) - if useBuffer is false,
        // we use XOR so we can XOR the rectangle away again without redrawing
        // the chart
        selectionZoomStrategy.drawZoomRectangle(g2, !this.useBuffer);

        g2.dispose();

        this.anchor = null;
    }

    
    @Override
    public void chartChanged(ChartChangeEvent event) {
        this.refreshBuffer = true;
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable z = (Zoomable) plot;
            this.orientation = z.getOrientation();
        }
        repaint();
    }

    
    @Override
    public void chartProgress(ChartProgressEvent event) {
        // does nothing - override if necessary
    }

    
    @Override
    public void actionPerformed(ActionEvent event) {

        String command = event.getActionCommand();

        // many of the zoom methods need a screen location - all we have is
        // the zoomPoint, but it might be null.  Here we grab the x and y
        // coordinates, or use defaults...
        double screenX = -1.0;
        double screenY = -1.0;
        Point2D zoomPoint = this.selectionZoomStrategy.getZoomPoint();
        if (zoomPoint != null) {
            screenX = zoomPoint.getX();
            screenY = zoomPoint.getY();
        }

        switch (command) {
        case PROPERTIES_COMMAND:
            doEditChartProperties();
            break;
        case COPY_COMMAND:
            doCopy();
            break;
        case SAVE_AS_PNG_COMMAND:
            try {
                doSaveAs();
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "I/O error occurred.",
                        localizationResources.getString("Save_as_PNG"), JOptionPane.WARNING_MESSAGE);
            }
            break;
        case SAVE_AS_PNG_SIZE_COMMAND:
            try {
                final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
                doSaveAs(ss.width, ss.height);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(ChartPanel.this, "I/O error occurred.",
                        localizationResources.getString("Save_as_PNG"), JOptionPane.WARNING_MESSAGE);
            }
            break;
        case SAVE_AS_SVG_COMMAND:
            try {
                saveAsSVG(null);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, "I/O error occurred.",
                        localizationResources.getString("Save_as_SVG"), JOptionPane.WARNING_MESSAGE);
            }
            break;
        case SAVE_AS_PDF_COMMAND:
            saveAsPDF(null);
            break;
        case PRINT_COMMAND:
            createChartPrintJob();
            break;
        case ZOOM_IN_BOTH_COMMAND:
            zoomInBoth(screenX, screenY);
            break;
        case ZOOM_IN_DOMAIN_COMMAND:
            zoomInDomain(screenX, screenY);
            break;
        case ZOOM_IN_RANGE_COMMAND:
            zoomInRange(screenX, screenY);
            break;
        case ZOOM_OUT_BOTH_COMMAND:
            zoomOutBoth(screenX, screenY);
            break;
        case ZOOM_OUT_DOMAIN_COMMAND:
            zoomOutDomain(screenX, screenY);
            break;
        case ZOOM_OUT_RANGE_COMMAND:
            zoomOutRange(screenX, screenY);
            break;
        case ZOOM_RESET_BOTH_COMMAND:
            restoreAutoBounds();
            break;
        case ZOOM_RESET_DOMAIN_COMMAND:
            restoreAutoDomainBounds();
            break;
        case ZOOM_RESET_RANGE_COMMAND:
            restoreAutoRangeBounds();
            break;
        }
    }

    
    @Override
    public void mouseEntered(MouseEvent e) {
        if (!this.ownToolTipDelaysActive) {
            ToolTipManager ttm = ToolTipManager.sharedInstance();

            this.originalToolTipInitialDelay = ttm.getInitialDelay();
            ttm.setInitialDelay(this.ownToolTipInitialDelay);

            this.originalToolTipReshowDelay = ttm.getReshowDelay();
            ttm.setReshowDelay(this.ownToolTipReshowDelay);

            this.originalToolTipDismissDelay = ttm.getDismissDelay();
            ttm.setDismissDelay(this.ownToolTipDismissDelay);

            this.ownToolTipDelaysActive = true;
        }
    }

    
    @Override
    public void mouseExited(MouseEvent e) {
        if (this.ownToolTipDelaysActive) {
            // restore original tooltip dealys
            ToolTipManager ttm = ToolTipManager.sharedInstance();
            ttm.setInitialDelay(this.originalToolTipInitialDelay);
            ttm.setReshowDelay(this.originalToolTipReshowDelay);
            ttm.setDismissDelay(this.originalToolTipDismissDelay);
            this.ownToolTipDelaysActive = false;
        }
    }

    
    @Override
    public void mousePressed(MouseEvent e) {
        if (this.chart == null) {
            return;
        }
        Plot plot = this.chart.getPlot();
        int button = e.getButton();
        int mods = e.getModifiersEx();
        if ((mods & MODIFIERS_EX_MASK) == panButtonMasks.getOrDefault(button, panMask)) {
            // can we pan this plot?
            if (plot instanceof Pannable) {
                Pannable pannable = (Pannable) plot;
                if (pannable.isDomainPannable() || pannable.isRangePannable()) {
                    Rectangle2D screenDataArea = getScreenDataArea(e.getX(),
                            e.getY());
                    if (screenDataArea != null && screenDataArea.contains(
                            e.getPoint())) {
                        this.panW = screenDataArea.getWidth();
                        this.panH = screenDataArea.getHeight();
                        this.panLast = e.getPoint();
                        setCursor(Cursor.getPredefinedCursor(
                                Cursor.MOVE_CURSOR));
                    }
                }
                // the actual panning occurs later in the mouseDragged() 
                // method
            }
        }
        else if (!this.selectionZoomStrategy.isActivated()) {
            if ((mods & MODIFIERS_EX_MASK) == zoomButtonMasks.getOrDefault(button, zoomMask)) {
                Rectangle2D screenDataArea = getScreenDataArea(e.getX(), e.getY());
                if (screenDataArea != null) {
                Point2D zoomPoint = getPointInRectangle(e.getX(), e.getY(),
                            screenDataArea);
                selectionZoomStrategy.setZoomPoint(zoomPoint);
                }
                else {
                selectionZoomStrategy.setZoomPoint(null);
                }
            }
            if (e.isPopupTrigger()) {
                if (this.popup != null) {
                    displayPopupMenu(e.getX(), e.getY());
                }
            }
        }
    }

    
    protected Point2D getPointInRectangle(int x, int y, Rectangle2D area) {
        double xx = Math.max(area.getMinX(), Math.min(x, area.getMaxX()));
        double yy = Math.max(area.getMinY(), Math.min(y, area.getMaxY()));
        return new Point2D.Double(xx, yy);
    }

    
    @Override
    public void mouseDragged(MouseEvent e) {

        // if the popup menu has already been triggered, then ignore dragging...
        if (this.popup != null && this.popup.isShowing()) {
            return;
        }

        // handle panning if we have a start point
        if (this.panLast != null) {
            double dx = e.getX() - this.panLast.getX();
            double dy = e.getY() - this.panLast.getY();
            if (dx == 0.0 && dy == 0.0) {
                return;
            }
            double wPercent = -dx / this.panW;
            double hPercent = dy / this.panH;
            boolean old = this.chart.getPlot().isNotify();
            this.chart.getPlot().setNotify(false);
            Pannable p = (Pannable) this.chart.getPlot();
            if (p.getOrientation() == PlotOrientation.VERTICAL) {
                p.panDomainAxes(wPercent, this.info.getPlotInfo(),
                        this.panLast);
                p.panRangeAxes(hPercent, this.info.getPlotInfo(),
                        this.panLast);
            }
            else {
                p.panDomainAxes(hPercent, this.info.getPlotInfo(),
                        this.panLast);
                p.panRangeAxes(wPercent, this.info.getPlotInfo(),
                        this.panLast);
            }
            this.panLast = e.getPoint();
            this.chart.getPlot().setNotify(old);
            return;
        }

        // if no initial zoom point was set, ignore dragging...
        if (this.selectionZoomStrategy.getZoomPoint() == null) {
            return;
        }
        Graphics2D g2 = (Graphics2D) getGraphics();

        // erase the previous zoom rectangle (if any).  We only need to do
        // this is we are using XOR mode, which we do when we're not using
        // the buffer (if there is a buffer, then at the end of this method we
        // just trigger a repaint)
        if (!this.useBuffer) {
            selectionZoomStrategy.drawZoomRectangle(g2, true);
        }

        boolean hZoom, vZoom;
        if (this.orientation == PlotOrientation.HORIZONTAL) {
            hZoom = this.rangeZoomable;
            vZoom = this.domainZoomable;
        }
        else {
            hZoom = this.domainZoomable;
            vZoom = this.rangeZoomable;
        }
        Point2D zoomPoint = this.selectionZoomStrategy.getZoomPoint();
        Rectangle2D scaledDataArea = getScreenDataArea(
                (int) zoomPoint.getX(), (int) zoomPoint.getY());

        selectionZoomStrategy.updateZoomRectangleSelection(e, hZoom, vZoom, scaledDataArea);

        // Draw the new zoom rectangle...
        if (this.useBuffer) {
            repaint();
        }
        else {
            // with no buffer, we use XOR to draw the rectangle "over" the
            // chart...
            selectionZoomStrategy.drawZoomRectangle(g2, true);
        }
        g2.dispose();

    }

    
    @Override
    public void mouseReleased(MouseEvent e) {

        // if we've been panning, we need to reset now that the mouse is 
        // released...
        if (this.panLast != null) {
            this.panLast = null;
            setCursor(Cursor.getDefaultCursor());
        }

        else if (this.selectionZoomStrategy.isActivated()) {
            boolean hZoom, vZoom;
            if (this.orientation == PlotOrientation.HORIZONTAL) {
                hZoom = this.rangeZoomable;
                vZoom = this.domainZoomable;
            }
            else {
                hZoom = this.domainZoomable;
                vZoom = this.rangeZoomable;
            }

            Point2D zoomPoint = this.selectionZoomStrategy.getZoomPoint();
            boolean zoomTrigger1 = hZoom && Math.abs(e.getX()
                - zoomPoint.getX()) >= this.selectionZoomStrategy.getZoomTriggerDistance();
            boolean zoomTrigger2 = vZoom && Math.abs(e.getY()
                - zoomPoint.getY()) >= this.selectionZoomStrategy.getZoomTriggerDistance();
            if (zoomTrigger1 || zoomTrigger2) {
                if ((hZoom && (e.getX() < zoomPoint.getX()))
                    || (vZoom && (e.getY() < zoomPoint.getY()))) {
                    restoreAutoBounds();
                }
                else {
                    Rectangle2D screenDataArea = getScreenDataArea(
                            (int) zoomPoint.getX(),
                            (int) zoomPoint.getY());

                    Rectangle2D zoomArea = selectionZoomStrategy.getZoomRectangle(hZoom, vZoom, screenDataArea);
                    zoom(zoomArea);
                }
                this.selectionZoomStrategy.reset();
            }
            else {
                // erase the zoom rectangle
                Graphics2D g2 = (Graphics2D) getGraphics();
                if (this.useBuffer) {
                    repaint();
                }
                else {
                    selectionZoomStrategy.drawZoomRectangle(g2, true);
                }
                g2.dispose();
                this.selectionZoomStrategy.reset();
            }

        }

        else if (e.isPopupTrigger()) {
            if (this.popup != null) {
                displayPopupMenu(e.getX(), e.getY());
            }
        }

    }

    
    @Override
    public void mouseClicked(MouseEvent event) {

        Insets insets = getInsets();
        int x = (int) ((event.getX() - insets.left) / this.scaleX);
        int y = (int) ((event.getY() - insets.top) / this.scaleY);

        this.anchor = new Point2D.Double(x, y);
        if (this.chart == null) {
            return;
        }
        this.chart.setNotify(true);  // force a redraw
        // new entity code...
        Object[] listeners = this.chartMouseListeners.getListeners(
                ChartMouseListener.class);
        if (listeners.length == 0) {
            return;
        }

        ChartEntity entity = null;
        if (this.info != null) {
            EntityCollection entities = this.info.getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }
        ChartMouseEvent chartEvent = new ChartMouseEvent(getChart(), event,
                entity);
        for (int i = listeners.length - 1; i >= 0; i -= 1) {
            ((ChartMouseListener) listeners[i]).chartMouseClicked(chartEvent);
        }

    }

    
    @Override
    public void mouseMoved(MouseEvent e) {
        Graphics2D g2 = (Graphics2D) getGraphics();
        g2.dispose();

        Object[] listeners = this.chartMouseListeners.getListeners(
                ChartMouseListener.class);
        if (listeners.length == 0) {
            return;
        }
        Insets insets = getInsets();
        int x = (int) ((e.getX() - insets.left) / this.scaleX);
        int y = (int) ((e.getY() - insets.top) / this.scaleY);

        ChartEntity entity = null;
        if (this.info != null) {
            EntityCollection entities = this.info.getEntityCollection();
            if (entities != null) {
                entity = entities.getEntity(x, y);
            }
        }

        // we can only generate events if the panel's chart is not null
        // (see bug report 1556951)
        if (this.chart != null) {
            ChartMouseEvent event = new ChartMouseEvent(getChart(), e, entity);
            for (int i = listeners.length - 1; i >= 0; i -= 1) {
                ((ChartMouseListener) listeners[i]).chartMouseMoved(event);
            }
        }

    }

    
    public void zoomInBoth(double x, double y) {
        Plot plot = this.chart.getPlot();
        if (plot == null) {
            return;
        }
        // here we tweak the notify flag on the plot so that only
        // one notification happens even though we update multiple
        // axes...
        boolean savedNotify = plot.isNotify();
        plot.setNotify(false);
        zoomInDomain(x, y);
        zoomInRange(x, y);
        plot.setNotify(savedNotify);
    }

    
    public void zoomInDomain(double x, double y) {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomDomainAxes(this.zoomInFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    
    public void zoomInRange(double x, double y) {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomRangeAxes(this.zoomInFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    
    public void zoomOutBoth(double x, double y) {
        Plot plot = this.chart.getPlot();
        if (plot == null) {
            return;
        }
        // here we tweak the notify flag on the plot so that only
        // one notification happens even though we update multiple
        // axes...
        boolean savedNotify = plot.isNotify();
        plot.setNotify(false);
        zoomOutDomain(x, y);
        zoomOutRange(x, y);
        plot.setNotify(savedNotify);
    }

    
    public void zoomOutDomain(double x, double y) {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomDomainAxes(this.zoomOutFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    
    public void zoomOutRange(double x, double y) {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            Zoomable z = (Zoomable) plot;
            z.zoomRangeAxes(this.zoomOutFactor, this.info.getPlotInfo(),
                    translateScreenToJava2D(new Point((int) x, (int) y)),
                    this.zoomAroundAnchor);
            plot.setNotify(savedNotify);
        }
    }

    
    public void zoom(Rectangle2D selection) {

        // get the origin of the zoom selection in the Java2D space used for
        // drawing the chart (that is, before any scaling to fit the panel)
        Point2D selectOrigin = translateScreenToJava2D(new Point(
                (int) Math.ceil(selection.getX()),
                (int) Math.ceil(selection.getY())));
        PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D scaledDataArea = getScreenDataArea(
                (int) selection.getCenterX(), (int) selection.getCenterY());
        if ((selection.getHeight() > 0) && (selection.getWidth() > 0)) {

            double hLower = (selection.getMinX() - scaledDataArea.getMinX())
                / scaledDataArea.getWidth();
            double hUpper = (selection.getMaxX() - scaledDataArea.getMinX())
                / scaledDataArea.getWidth();
            double vLower = (scaledDataArea.getMaxY() - selection.getMaxY())
                / scaledDataArea.getHeight();
            double vUpper = (scaledDataArea.getMaxY() - selection.getMinY())
                / scaledDataArea.getHeight();

            Plot p = this.chart.getPlot();
            if (p instanceof Zoomable) {
                // here we tweak the notify flag on the plot so that only
                // one notification happens even though we update multiple
                // axes...
                boolean savedNotify = p.isNotify();
                p.setNotify(false);
                Zoomable z = (Zoomable) p;
                if (z.getOrientation() == PlotOrientation.HORIZONTAL) {
                    z.zoomDomainAxes(vLower, vUpper, plotInfo, selectOrigin);
                    z.zoomRangeAxes(hLower, hUpper, plotInfo, selectOrigin);
                }
                else {
                    z.zoomDomainAxes(hLower, hUpper, plotInfo, selectOrigin);
                    z.zoomRangeAxes(vLower, vUpper, plotInfo, selectOrigin);
                }
                p.setNotify(savedNotify);
            }

        }

    }

    
    public void restoreAutoBounds() {
        Plot plot = this.chart.getPlot();
        if (plot == null) {
            return;
        }
        // here we tweak the notify flag on the plot so that only
        // one notification happens even though we update multiple
        // axes...
        boolean savedNotify = plot.isNotify();
        plot.setNotify(false);
        restoreAutoDomainBounds();
        restoreAutoRangeBounds();
        plot.setNotify(savedNotify);
    }

    
    public void restoreAutoDomainBounds() {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable z = (Zoomable) plot;
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            // we need to guard against this.zoomPoint being null
            Point2D zoomPoint = this.selectionZoomStrategy.getZoomPoint();
            Point2D zp = zoomPoint != null ? zoomPoint : new Point();
            z.zoomDomainAxes(0.0, this.info.getPlotInfo(), zp);
            plot.setNotify(savedNotify);
        }
    }

    
    public void restoreAutoRangeBounds() {
        Plot plot = this.chart.getPlot();
        if (plot instanceof Zoomable) {
            Zoomable z = (Zoomable) plot;
            // here we tweak the notify flag on the plot so that only
            // one notification happens even though we update multiple
            // axes...
            boolean savedNotify = plot.isNotify();
            plot.setNotify(false);
            // we need to guard against this.zoomPoint being null
            Point2D zoomPoint = this.selectionZoomStrategy.getZoomPoint();
            Point2D zp = zoomPoint != null ? zoomPoint : new Point();
            z.zoomRangeAxes(0.0, this.info.getPlotInfo(), zp);
            plot.setNotify(savedNotify);
        }
    }

    
    public Rectangle2D getScreenDataArea() {
        Rectangle2D dataArea = this.info.getPlotInfo().getDataArea();
        Insets insets = getInsets();
        double x = dataArea.getX() * this.scaleX + insets.left;
        double y = dataArea.getY() * this.scaleY + insets.top;
        double w = dataArea.getWidth() * this.scaleX;
        double h = dataArea.getHeight() * this.scaleY;
        return new Rectangle2D.Double(x, y, w, h);
    }

    
    public Rectangle2D getScreenDataArea(int x, int y) {
        PlotRenderingInfo plotInfo = this.info.getPlotInfo();
        Rectangle2D result;
        if (plotInfo.getSubplotCount() == 0) {
            result = getScreenDataArea();
        }
        else {
            // get the origin of the zoom selection in the Java2D space used for
            // drawing the chart (that is, before any scaling to fit the panel)
            Point2D selectOrigin = translateScreenToJava2D(new Point(x, y));
            int subplotIndex = plotInfo.getSubplotIndex(selectOrigin);
            if (subplotIndex == -1) {
                return null;
            }
            result = scale(plotInfo.getSubplotInfo(subplotIndex).getDataArea());
        }
        return result;
    }

    
    public int getInitialDelay() {
        return this.ownToolTipInitialDelay;
    }

    
    public int getReshowDelay() {
        return this.ownToolTipReshowDelay;
    }

    
    public int getDismissDelay() {
        return this.ownToolTipDismissDelay;
    }

    
    public void setInitialDelay(int delay) {
        this.ownToolTipInitialDelay = delay;
    }

    
    public void setReshowDelay(int delay) {
        this.ownToolTipReshowDelay = delay;
    }

    
    public void setDismissDelay(int delay) {
        this.ownToolTipDismissDelay = delay;
    }

    
    public double getZoomInFactor() {
        return this.zoomInFactor;
    }

    
    public void setZoomInFactor(double factor) {
        this.zoomInFactor = factor;
    }

    
    public double getZoomOutFactor() {
        return this.zoomOutFactor;
    }

    
    public void setZoomOutFactor(double factor) {
        this.zoomOutFactor = factor;
    }


    
    public void doEditChartProperties() {

        ChartEditor editor = ChartEditorManager.getChartEditor(this.chart);
        int result = JOptionPane.showConfirmDialog(this, editor,
                localizationResources.getString("Chart_Properties"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            editor.updateChart(this.chart);
        }

    }

    
    public void doCopy() {
        Clipboard systemClipboard
                = Toolkit.getDefaultToolkit().getSystemClipboard();
        Insets insets = getInsets();
        int w = getWidth() - insets.left - insets.right;
        int h = getHeight() - insets.top - insets.bottom;
        ChartTransferable selection = new ChartTransferable(this.chart, w, h,
                getMinimumDrawWidth(), getMinimumDrawHeight(),
                getMaximumDrawWidth(), getMaximumDrawHeight(), true);
        systemClipboard.setContents(selection, null);
    }

    
    public void doSaveAs() throws IOException {
        doSaveAs(-1, -1);
    }

    
    public void doSaveAs(int w, int h) throws IOException {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(this.defaultDirectoryForSaveAs);
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    localizationResources.getString("PNG_Image_Files"), "png");
        fileChooser.addChoosableFileFilter(filter);
        fileChooser.setFileFilter(filter);

        int option = fileChooser.showSaveDialog(this);
        if (option == JFileChooser.APPROVE_OPTION) {
            String filename = fileChooser.getSelectedFile().getPath();
            if (isEnforceFileExtensions()) {
                if (!filename.endsWith(".png")) {
                    filename = filename + ".png";
                }
            }
            if (w <= 0) {
            	w = getWidth();
            }
            if (h <= 0) {
            	h = getHeight();
            }
            ChartUtils.saveChartAsPNG(new File(filename), this.chart, w, h);
        }
    }
    
    
    protected void saveAsSVG(File f) throws IOException {
        File file = f;
        if (file == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(this.defaultDirectoryForSaveAs);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    localizationResources.getString("SVG_Files"), "svg");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);

            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getPath();
                if (isEnforceFileExtensions()) {
                    if (!filename.endsWith(".svg")) {
                        filename = filename + ".svg";
                    }
                }
                file = new File(filename);
                if (file.exists()) {
                    String fileExists = localizationResources.getString(
                            "FILE_EXISTS_CONFIRM_OVERWRITE");
                    int response = JOptionPane.showConfirmDialog(this, 
                            fileExists,
                            localizationResources.getString("Save_as_SVG"),
                            JOptionPane.OK_CANCEL_OPTION);
                    if (response == JOptionPane.CANCEL_OPTION) {
                        file = null;
                    }
                }
            }
        }
        
        if (file != null) {
            // use reflection to get the SVG string
            String svg = generateSVG(getWidth(), getHeight());
            BufferedWriter writer = null;
            Exception originalException = null;
            try {
                writer = new BufferedWriter(new FileWriter(file));
                writer.write("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
                writer.write(svg + "\n");
                writer.flush();
            } catch (Exception e) {
                originalException = e;
            }
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException ex) {
                RuntimeException th = new RuntimeException(ex);
                if (originalException != null)
                    th.addSuppressed(originalException);
                throw th;
            }
        }
    }
    
    
    protected String generateSVG(int width, int height) {
        Graphics2D g2 = createSVGGraphics2D(width, height);
        if (g2 == null) {
            throw new IllegalStateException("JFreeSVG library is not present.");
        }
        // we suppress shadow generation, because SVG is a vector format and
        // the shadow effect is applied via bitmap effects...
        g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
        String svg = null;
        Rectangle2D drawArea = new Rectangle2D.Double(0, 0, width, height);
        this.chart.draw(g2, drawArea);
        try {
            Method m = g2.getClass().getMethod("getSVGElement");
            svg = (String) m.invoke(g2);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException |
                IllegalArgumentException | InvocationTargetException e) {
            // null will be returned
        }
        return svg;
    }

    
    protected Graphics2D createSVGGraphics2D(int w, int h) {
        try {
            Class<?> svgGraphics2d = Class.forName("org.jfree.graphics2d.svg.SVGGraphics2D");
            Constructor<?> ctor = svgGraphics2d.getConstructor(int.class, int.class);
            return (Graphics2D) ctor.newInstance(w, h);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException |
                IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            return null;
        }
    }

    
    protected void saveAsPDF(File f) {
        File file = f;
        if (file == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(this.defaultDirectoryForSaveAs);
            FileNameExtensionFilter filter = new FileNameExtensionFilter(
                    localizationResources.getString("PDF_Files"), "pdf");
            fileChooser.addChoosableFileFilter(filter);
            fileChooser.setFileFilter(filter);

            int option = fileChooser.showSaveDialog(this);
            if (option == JFileChooser.APPROVE_OPTION) {
                String filename = fileChooser.getSelectedFile().getPath();
                if (isEnforceFileExtensions()) {
                    if (!filename.endsWith(".pdf")) {
                        filename = filename + ".pdf";
                    }
                }
                file = new File(filename);
                if (file.exists()) {
                    String fileExists = localizationResources.getString(
                            "FILE_EXISTS_CONFIRM_OVERWRITE");
                    int response = JOptionPane.showConfirmDialog(this, 
                            fileExists,
                            localizationResources.getString("Save_as_PDF"),
                            JOptionPane.OK_CANCEL_OPTION);
                    if (response == JOptionPane.CANCEL_OPTION) {
                        file = null;
                    }
                }
            }
        }
        
        if (file != null) {
            writeAsPDF(file, getWidth(), getHeight());
        }
    }

    
    private void writeAsPDF(File file, int w, int h) {
        if (!ChartUtils.isOrsonPDFAvailable()) {
            throw new IllegalStateException(
                    "OrsonPDF is not present on the classpath.");
        }
        Args.nullNotPermitted(file, "file");
        try {
            Class<?> pdfDocClass = Class.forName("com.orsonpdf.PDFDocument");
            Object pdfDoc = pdfDocClass.getDeclaredConstructor().newInstance();
            Method m = pdfDocClass.getMethod("createPage", Rectangle2D.class);
            Rectangle2D rect = new Rectangle(w, h);
            Object page = m.invoke(pdfDoc, rect);
            Method m2 = page.getClass().getMethod("getGraphics2D");
            Graphics2D g2 = (Graphics2D) m2.invoke(page);
            // we suppress shadow generation, because PDF is a vector format and
            // the shadow effect is applied via bitmap effects...
            g2.setRenderingHint(JFreeChart.KEY_SUPPRESS_SHADOW_GENERATION, true);
            Rectangle2D drawArea = new Rectangle2D.Double(0, 0, w, h);
            this.chart.draw(g2, drawArea);
            Method m3 = pdfDocClass.getMethod("writeToFile", File.class);
            m3.invoke(pdfDoc, file);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException |
                NoSuchMethodException | SecurityException | IllegalArgumentException |
                InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }

    
    public void createChartPrintJob() {
        PrinterJob job = PrinterJob.getPrinterJob();
        PageFormat pf = job.defaultPage();
        PageFormat pf2 = job.pageDialog(pf);
        if (pf2 != pf) {
            job.setPrintable(this, pf2);
            if (job.printDialog()) {
                try {
                    job.print();
                }
                catch (PrinterException e) {
                    JOptionPane.showMessageDialog(this, e);
                }
            }
        }
    }

    
    @Override
    public int print(Graphics g, PageFormat pf, int pageIndex) {

        if (pageIndex != 0) {
            return NO_SUCH_PAGE;
        }
        Graphics2D g2 = (Graphics2D) g;
        double x = pf.getImageableX();
        double y = pf.getImageableY();
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        this.chart.draw(g2, new Rectangle2D.Double(x, y, w, h), this.anchor,
                null);
        return PAGE_EXISTS;

    }

    
    public void addChartMouseListener(ChartMouseListener listener) {
        Args.nullNotPermitted(listener, "listener");
        this.chartMouseListeners.add(ChartMouseListener.class, listener);
    }

    
    public void removeChartMouseListener(ChartMouseListener listener) {
        this.chartMouseListeners.remove(ChartMouseListener.class, listener);
    }

    
    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType) {
        if (listenerType == ChartMouseListener.class) {
            // fetch listeners from local storage
            return this.chartMouseListeners.getListeners(listenerType);
        }
        else {
            return super.getListeners(listenerType);
        }
    }

    
    protected JPopupMenu createPopupMenu(boolean properties,
            boolean copy, boolean save, boolean print, boolean zoom) {

        JPopupMenu result = new JPopupMenu(localizationResources.getString("Chart") + ":");
        boolean separator = false;

        if (properties) {
            JMenuItem propertiesItem = new JMenuItem(
                    localizationResources.getString("Properties..."));
            propertiesItem.setActionCommand(PROPERTIES_COMMAND);
            propertiesItem.addActionListener(this);
            result.add(propertiesItem);
            separator = true;
        }

        if (copy) {
            if (separator) {
                result.addSeparator();
            }
            JMenuItem copyItem = new JMenuItem(
                    localizationResources.getString("Copy"));
            copyItem.setActionCommand(COPY_COMMAND);
            copyItem.addActionListener(this);
            result.add(copyItem);
            separator = !save;
        }

        if (save) {
            if (separator) {
                result.addSeparator();
            }

            JMenu saveSubMenu = new JMenu(localizationResources.getString("Save_as"));

            // PNG - current res
            {
                JMenuItem pngItem = new JMenuItem(localizationResources.getString(
                        "PNG..."));
                pngItem.setActionCommand(SAVE_AS_PNG_COMMAND);
                pngItem.addActionListener(this);
                saveSubMenu.add(pngItem);

            }

            // PNG - screen res
            {
            	final Dimension ss = Toolkit.getDefaultToolkit().getScreenSize();
                final String pngName = "PNG ("+ss.width+"x"+ss.height+") ...";
                JMenuItem pngItem = new JMenuItem(pngName);
                pngItem.setActionCommand(SAVE_AS_PNG_SIZE_COMMAND);
                pngItem.addActionListener(this);
                saveSubMenu.add(pngItem);
            }
            
            if (ChartUtils.isJFreeSVGAvailable()) {
                JMenuItem svgItem = new JMenuItem(localizationResources.getString(
                        "SVG..."));
                svgItem.setActionCommand(SAVE_AS_SVG_COMMAND);
                svgItem.addActionListener(this);
                saveSubMenu.add(svgItem);                
            }
            
            if (ChartUtils.isOrsonPDFAvailable()) {
                JMenuItem pdfItem = new JMenuItem(
                        localizationResources.getString("PDF..."));
                pdfItem.setActionCommand(SAVE_AS_PDF_COMMAND);
                pdfItem.addActionListener(this);
                saveSubMenu.add(pdfItem);
            }
            result.add(saveSubMenu);
            separator = true;
        }

        if (print) {
            if (separator) {
                result.addSeparator();
            }
            JMenuItem printItem = new JMenuItem(
                    localizationResources.getString("Print..."));
            printItem.setActionCommand(PRINT_COMMAND);
            printItem.addActionListener(this);
            result.add(printItem);
            separator = true;
        }

        if (zoom) {
            if (separator) {
                result.addSeparator();
            }

            JMenu zoomInMenu = new JMenu(
                    localizationResources.getString("Zoom_In"));

            this.zoomInBothMenuItem = new JMenuItem(
                    localizationResources.getString("All_Axes"));
            this.zoomInBothMenuItem.setActionCommand(ZOOM_IN_BOTH_COMMAND);
            this.zoomInBothMenuItem.addActionListener(this);
            zoomInMenu.add(this.zoomInBothMenuItem);

            zoomInMenu.addSeparator();

            this.zoomInDomainMenuItem = new JMenuItem(
                    localizationResources.getString("Domain_Axis"));
            this.zoomInDomainMenuItem.setActionCommand(ZOOM_IN_DOMAIN_COMMAND);
            this.zoomInDomainMenuItem.addActionListener(this);
            zoomInMenu.add(this.zoomInDomainMenuItem);

            this.zoomInRangeMenuItem = new JMenuItem(
                    localizationResources.getString("Range_Axis"));
            this.zoomInRangeMenuItem.setActionCommand(ZOOM_IN_RANGE_COMMAND);
            this.zoomInRangeMenuItem.addActionListener(this);
            zoomInMenu.add(this.zoomInRangeMenuItem);

            result.add(zoomInMenu);

            JMenu zoomOutMenu = new JMenu(
                    localizationResources.getString("Zoom_Out"));

            this.zoomOutBothMenuItem = new JMenuItem(
                    localizationResources.getString("All_Axes"));
            this.zoomOutBothMenuItem.setActionCommand(ZOOM_OUT_BOTH_COMMAND);
            this.zoomOutBothMenuItem.addActionListener(this);
            zoomOutMenu.add(this.zoomOutBothMenuItem);

            zoomOutMenu.addSeparator();

            this.zoomOutDomainMenuItem = new JMenuItem(
                    localizationResources.getString("Domain_Axis"));
            this.zoomOutDomainMenuItem.setActionCommand(
                    ZOOM_OUT_DOMAIN_COMMAND);
            this.zoomOutDomainMenuItem.addActionListener(this);
            zoomOutMenu.add(this.zoomOutDomainMenuItem);

            this.zoomOutRangeMenuItem = new JMenuItem(
                    localizationResources.getString("Range_Axis"));
            this.zoomOutRangeMenuItem.setActionCommand(ZOOM_OUT_RANGE_COMMAND);
            this.zoomOutRangeMenuItem.addActionListener(this);
            zoomOutMenu.add(this.zoomOutRangeMenuItem);

            result.add(zoomOutMenu);

            JMenu autoRangeMenu = new JMenu(
                    localizationResources.getString("Auto_Range"));

            this.zoomResetBothMenuItem = new JMenuItem(
                    localizationResources.getString("All_Axes"));
            this.zoomResetBothMenuItem.setActionCommand(
                    ZOOM_RESET_BOTH_COMMAND);
            this.zoomResetBothMenuItem.addActionListener(this);
            autoRangeMenu.add(this.zoomResetBothMenuItem);

            autoRangeMenu.addSeparator();
            this.zoomResetDomainMenuItem = new JMenuItem(
                    localizationResources.getString("Domain_Axis"));
            this.zoomResetDomainMenuItem.setActionCommand(
                    ZOOM_RESET_DOMAIN_COMMAND);
            this.zoomResetDomainMenuItem.addActionListener(this);
            autoRangeMenu.add(this.zoomResetDomainMenuItem);

            this.zoomResetRangeMenuItem = new JMenuItem(
                    localizationResources.getString("Range_Axis"));
            this.zoomResetRangeMenuItem.setActionCommand(
                    ZOOM_RESET_RANGE_COMMAND);
            this.zoomResetRangeMenuItem.addActionListener(this);
            autoRangeMenu.add(this.zoomResetRangeMenuItem);

            result.addSeparator();
            result.add(autoRangeMenu);

        }

        return result;

    }

    
    protected void displayPopupMenu(int x, int y) {

        if (this.popup == null) {
            return;
        }

        // go through each zoom menu item and decide whether or not to
        // enable it...
        boolean isDomainZoomable = false;
        boolean isRangeZoomable = false;
        Plot plot = (this.chart != null ? this.chart.getPlot() : null);
        if (plot instanceof Zoomable) {
            Zoomable z = (Zoomable) plot;
            isDomainZoomable = z.isDomainZoomable();
            isRangeZoomable = z.isRangeZoomable();
        }

        if (this.zoomInDomainMenuItem != null) {
            this.zoomInDomainMenuItem.setEnabled(isDomainZoomable);
        }
        if (this.zoomOutDomainMenuItem != null) {
            this.zoomOutDomainMenuItem.setEnabled(isDomainZoomable);
        }
        if (this.zoomResetDomainMenuItem != null) {
            this.zoomResetDomainMenuItem.setEnabled(isDomainZoomable);
        }

        if (this.zoomInRangeMenuItem != null) {
            this.zoomInRangeMenuItem.setEnabled(isRangeZoomable);
        }
        if (this.zoomOutRangeMenuItem != null) {
            this.zoomOutRangeMenuItem.setEnabled(isRangeZoomable);
        }

        if (this.zoomResetRangeMenuItem != null) {
            this.zoomResetRangeMenuItem.setEnabled(isRangeZoomable);
        }

        if (this.zoomInBothMenuItem != null) {
            this.zoomInBothMenuItem.setEnabled(isDomainZoomable
                    && isRangeZoomable);
        }
        if (this.zoomOutBothMenuItem != null) {
            this.zoomOutBothMenuItem.setEnabled(isDomainZoomable
                    && isRangeZoomable);
        }
        if (this.zoomResetBothMenuItem != null) {
            this.zoomResetBothMenuItem.setEnabled(isDomainZoomable
                    && isRangeZoomable);
        }

        this.popup.show(this, x, y);

    }

    
    @Override
    public void updateUI() {
        // here we need to update the UI for the popup menu, if the panel
        // has one...
        if (this.popup != null) {
            SwingUtilities.updateComponentTreeUI(this.popup);
        }
        super.updateUI();
    }

    
    protected void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
    }

    
    protected void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();

        // we create a new but empty chartMouseListeners list
        this.chartMouseListeners = new EventListenerList();

        // register as a listener with sub-components...
        if (this.chart != null) {
            this.chart.addChangeListener(this);
        }

    }

}

