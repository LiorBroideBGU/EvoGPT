

package org.jfree.chart.plot;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ResourceBundle;
import org.jfree.chart.ChartElementVisitor;

import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.RendererChangeEvent;
import org.jfree.chart.event.RendererChangeListener;
import org.jfree.chart.renderer.WaferMapRenderer;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.WaferMapDataset;


public class WaferMapPlot extends Plot implements RendererChangeListener,
        Cloneable, Serializable {

    
    private static final long serialVersionUID = 4668320403707308155L;

    
    public static final Stroke DEFAULT_GRIDLINE_STROKE = new BasicStroke(0.5f,
        BasicStroke.CAP_BUTT,
        BasicStroke.JOIN_BEVEL,
        0.0f,
        new float[] {2.0f, 2.0f},
        0.0f);

    
    public static final Paint DEFAULT_GRIDLINE_PAINT = Color.LIGHT_GRAY;

    
    public static final boolean DEFAULT_CROSSHAIR_VISIBLE = false;

    
    public static final Stroke DEFAULT_CROSSHAIR_STROKE
            = DEFAULT_GRIDLINE_STROKE;

    
    public static final Paint DEFAULT_CROSSHAIR_PAINT = Color.BLUE;

    
    protected static ResourceBundle localizationResources
            = ResourceBundle.getBundle("org.jfree.chart.plot.LocalizationBundle");

    
    private final PlotOrientation orientation;

    
    private WaferMapDataset dataset;

    
    private WaferMapRenderer renderer;

    
    public WaferMapPlot() {
        this(null);
    }

    
    public WaferMapPlot(WaferMapDataset dataset) {
        this(dataset, null);
    }

    
    public WaferMapPlot(WaferMapDataset dataset, WaferMapRenderer renderer) {

        super();

        this.orientation = PlotOrientation.VERTICAL;

        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }

        this.renderer = renderer;
        if (renderer != null) {
            renderer.setPlot(this);
            renderer.addChangeListener(this);
        }

    }

    
    @Override
    public String getPlotType() {
        return ("WMAP_Plot");
    }

    
    public WaferMapDataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(WaferMapDataset dataset) {
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

    
    public void setRenderer(WaferMapRenderer renderer) {
        if (this.renderer != null) {
            this.renderer.removeChangeListener(this);
        }
        this.renderer = renderer;
        if (renderer != null) {
            renderer.setPlot(this);
        }
        fireChangeEvent();
    }

    
    @Override
    public void receive(ChartElementVisitor visitor) {
        // FIXME : handle the renderer
        super.receive(visitor);
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
            PlotState state, PlotRenderingInfo info) {

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

        drawChipGrid(g2, area);
        drawWaferEdge(g2, area);

    }

    
    protected void drawChipGrid(Graphics2D g2, Rectangle2D plotArea) {

        Shape savedClip = g2.getClip();
        g2.setClip(getWaferEdge(plotArea));
        Rectangle2D chip = new Rectangle2D.Double();
        int xchips = 35;
        int ychips = 20;
        double space = 1d;
        if (this.dataset != null) {
            xchips = this.dataset.getMaxChipX() + 2;
            ychips = this.dataset.getMaxChipY() + 2;
            space = this.dataset.getChipSpace();
        }
        double startX = plotArea.getX();
        double startY = plotArea.getY();
        double chipWidth = 1d;
        double chipHeight = 1d;
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major, minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            //set upperLeft point
            if (plotArea.getWidth() == minor) { // x is minor
                startY += (major - minor) / 2;
                chipWidth = (plotArea.getWidth() - (space * xchips - 1))
                    / xchips;
                chipHeight = (plotArea.getWidth() - (space * ychips - 1))
                    / ychips;
            }
            else { // y is minor
                startX += (major - minor) / 2;
                chipWidth = (plotArea.getHeight() - (space * xchips - 1))
                    / xchips;
                chipHeight = (plotArea.getHeight() - (space * ychips - 1))
                    / ychips;
            }
        }

        for (int x = 1; x <= xchips; x++) {
            double upperLeftX = (startX - chipWidth) + (chipWidth * x)
                + (space * (x - 1));
            for (int y = 1; y <= ychips; y++) {
                double upperLeftY = (startY - chipHeight) + (chipHeight * y)
                    + (space * (y - 1));
                chip.setFrame(upperLeftX, upperLeftY, chipWidth, chipHeight);
                g2.setColor(Color.WHITE);
                if (this.dataset.getChipValue(x - 1, ychips - y - 1) != null) {
                    g2.setPaint(
                        this.renderer.getChipColor(
                            this.dataset.getChipValue(x - 1, ychips - y - 1)
                        )
                    );
                }
                g2.fill(chip);
                g2.setColor(Color.LIGHT_GRAY);
                g2.draw(chip);
            }
        }
        g2.setClip(savedClip);
    }

    
    protected Ellipse2D getWaferEdge(Rectangle2D plotArea) {
        Ellipse2D edge = new Ellipse2D.Double();
        double diameter = plotArea.getWidth();
        double upperLeftX = plotArea.getX();
        double upperLeftY = plotArea.getY();
        //get major dimension
        if (plotArea.getWidth() != plotArea.getHeight()) {
            double major, minor;
            if (plotArea.getWidth() > plotArea.getHeight()) {
                major = plotArea.getWidth();
                minor = plotArea.getHeight();
            }
            else {
                major = plotArea.getHeight();
                minor = plotArea.getWidth();
            }
            //ellipse diameter is the minor dimension
            diameter = minor;
            //set upperLeft point
            if (plotArea.getWidth() == minor) { // x is minor
                upperLeftY = plotArea.getY() + (major - minor) / 2;
            }
            else { // y is minor
                upperLeftX = plotArea.getX() + (major - minor) / 2;
            }
        }
        edge.setFrame(upperLeftX, upperLeftY, diameter, diameter);
        return edge;
    }

    
    protected void drawWaferEdge(Graphics2D g2, Rectangle2D plotArea) {
        // draw the wafer
        Ellipse2D waferEdge = getWaferEdge(plotArea);
        g2.setColor(Color.BLACK);
        g2.draw(waferEdge);
        // calculate and draw the notch
        // horizontal orientation is considered notch right
        // vertical orientation is considered notch down
        Arc2D notch;
        Rectangle2D waferFrame = waferEdge.getFrame();
        double notchDiameter = waferFrame.getWidth() * 0.04;
        if (this.orientation == PlotOrientation.HORIZONTAL) {
            Rectangle2D notchFrame =
                new Rectangle2D.Double(
                    waferFrame.getX() + waferFrame.getWidth()
                    - (notchDiameter / 2), waferFrame.getY()
                    + (waferFrame.getHeight() / 2) - (notchDiameter / 2),
                    notchDiameter, notchDiameter
                );
            notch = new Arc2D.Double(notchFrame, 90d, 180d, Arc2D.OPEN);
        }
        else {
            Rectangle2D notchFrame =
                new Rectangle2D.Double(
                    waferFrame.getX() + (waferFrame.getWidth() / 2)
                    - (notchDiameter / 2), waferFrame.getY()
                    + waferFrame.getHeight() - (notchDiameter / 2),
                    notchDiameter, notchDiameter
                );
            notch = new Arc2D.Double(notchFrame, 0d, 180d, Arc2D.OPEN);
        }
        g2.setColor(Color.WHITE);
        g2.fill(notch);
        g2.setColor(Color.BLACK);
        g2.draw(notch);

    }

    
    @Override
    public LegendItemCollection getLegendItems() {
        return this.renderer.getLegendCollection();
    }

    
    @Override
    public void rendererChanged(RendererChangeEvent event) {
        fireChangeEvent();
    }

}
