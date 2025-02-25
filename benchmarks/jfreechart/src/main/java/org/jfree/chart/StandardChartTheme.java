

package org.jfree.chart;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Stroke;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Objects;

import org.jfree.chart.annotations.XYAnnotation;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.PeriodAxisLabelInfo;
import org.jfree.chart.axis.SubCategoryAxis;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.Block;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.LabelBlock;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.CombinedDomainCategoryPlot;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.CombinedRangeCategoryPlot;
import org.jfree.chart.plot.CombinedRangeXYPlot;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.FastScatterPlot;
import org.jfree.chart.plot.MeterPlot;
import org.jfree.chart.plot.pie.MultiplePiePlot;
import org.jfree.chart.plot.pie.PieLabelLinkStyle;
import org.jfree.chart.plot.pie.PiePlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PolarPlot;
import org.jfree.chart.plot.SpiderWebPlot;
import org.jfree.chart.plot.ThermometerPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.AbstractRenderer;
import org.jfree.chart.renderer.category.BarPainter;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.renderer.category.MinMaxCategoryRenderer;
import org.jfree.chart.renderer.category.StatisticalBarRenderer;
import org.jfree.chart.renderer.xy.GradientXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.CompositeTitle;
import org.jfree.chart.legend.LegendTitle;
import org.jfree.chart.legend.PaintScaleLegend;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.title.Title;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.util.DefaultShadowGenerator;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.util.ShadowGenerator;


public class StandardChartTheme implements ChartTheme, Cloneable,
        PublicCloneable, Serializable {

    
    private final String name;

    
    private Font extraLargeFont;

    
    private Font largeFont;

    
    private Font regularFont;

    
    private Font smallFont;

    
    private transient Paint titlePaint;

    
    private transient Paint subtitlePaint;

    
    private transient Paint chartBackgroundPaint;

    
    private transient Paint legendBackgroundPaint;

    
    private transient Paint legendItemPaint;

    
    private DrawingSupplier drawingSupplier;

    
    private transient Paint plotBackgroundPaint;

    
    private transient Paint plotOutlinePaint;

    
    private PieLabelLinkStyle labelLinkStyle;

    
    private transient Paint labelLinkPaint;

    
    private transient Paint domainGridlinePaint;

    
    private transient Paint rangeGridlinePaint;

    
    private transient Paint baselinePaint;

    
    private transient Paint crosshairPaint;

    
    private RectangleInsets axisOffset;

    
    private transient Paint axisLabelPaint;

    
    private transient Paint tickLabelPaint;

    
    private transient Paint itemLabelPaint;

    
    private boolean shadowVisible;

    
    private transient Paint shadowPaint;

    
    private BarPainter barPainter;

    
    private XYBarPainter xyBarPainter;

    
    private transient Paint thermometerPaint;

    
    private transient Paint errorIndicatorPaint;

    
    private transient Paint gridBandPaint = SymbolAxis.DEFAULT_GRID_BAND_PAINT;

    
    private transient Paint gridBandAlternatePaint
            = SymbolAxis.DEFAULT_GRID_BAND_ALTERNATE_PAINT;

    
    private ShadowGenerator shadowGenerator;

    
    public static ChartTheme createJFreeTheme() {
        return new StandardChartTheme("JFree");
    }

    
    public static ChartTheme createDarknessTheme() {
        StandardChartTheme theme = new StandardChartTheme("Darkness");
        theme.titlePaint = Color.WHITE;
        theme.subtitlePaint = Color.WHITE;
        theme.legendBackgroundPaint = Color.BLACK;
        theme.legendItemPaint = Color.WHITE;
        theme.chartBackgroundPaint = Color.BLACK;
        theme.plotBackgroundPaint = Color.BLACK;
        theme.plotOutlinePaint = Color.YELLOW;
        theme.baselinePaint = Color.WHITE;
        theme.crosshairPaint = Color.RED;
        theme.labelLinkPaint = Color.LIGHT_GRAY;
        theme.tickLabelPaint = Color.WHITE;
        theme.axisLabelPaint = Color.WHITE;
        theme.shadowPaint = Color.DARK_GRAY;
        theme.itemLabelPaint = Color.WHITE;
        theme.drawingSupplier = new DefaultDrawingSupplier(
                new Paint[] {Color.decode("0xFFFF00"),
                        Color.decode("0x0036CC"), Color.decode("0xFF0000"),
                        Color.decode("0xFFFF7F"), Color.decode("0x6681CC"),
                        Color.decode("0xFF7F7F"), Color.decode("0xFFFFBF"),
                        Color.decode("0x99A6CC"), Color.decode("0xFFBFBF"),
                        Color.decode("0xA9A938"), Color.decode("0x2D4587")},
                new Paint[] {Color.decode("0xFFFF00"),
                        Color.decode("0x0036CC")},
                new Stroke[] {new BasicStroke(2.0f)},
                new Stroke[] {new BasicStroke(0.5f)},
                DefaultDrawingSupplier.DEFAULT_SHAPE_SEQUENCE);
        theme.errorIndicatorPaint = Color.LIGHT_GRAY;
        theme.gridBandPaint = new Color(255, 255, 255, 20);
        theme.gridBandAlternatePaint = new Color(255, 255, 255, 40);
        theme.shadowGenerator = null;
        return theme;
    }

    
    public static ChartTheme createLegacyTheme() {
        StandardChartTheme theme = new StandardChartTheme("Legacy") {
            @Override
            public void apply(JFreeChart chart) {
                // do nothing at all
            }
        };
        return theme;
    }

    
    public StandardChartTheme(String name) {
        this(name, false);
    }

    
    public StandardChartTheme(String name, boolean shadow) {
        Args.nullNotPermitted(name, "name");
        this.name = name;
        this.extraLargeFont = new Font("Tahoma", Font.BOLD, 20);
        this.largeFont = new Font("Tahoma", Font.BOLD, 14);
        this.regularFont = new Font("Tahoma", Font.PLAIN, 12);
        this.smallFont = new Font("Tahoma", Font.PLAIN, 10);
        this.titlePaint = Color.BLACK;
        this.subtitlePaint = Color.BLACK;
        this.legendBackgroundPaint = Color.WHITE;
        this.legendItemPaint = Color.DARK_GRAY;
        this.chartBackgroundPaint = Color.WHITE;
        this.drawingSupplier = new DefaultDrawingSupplier();
        this.plotBackgroundPaint = Color.LIGHT_GRAY;
        this.plotOutlinePaint = Color.BLACK;
        this.labelLinkPaint = Color.BLACK;
        this.labelLinkStyle = PieLabelLinkStyle.CUBIC_CURVE;
        this.axisOffset = new RectangleInsets(4, 4, 4, 4);
        this.domainGridlinePaint = Color.WHITE;
        this.rangeGridlinePaint = Color.WHITE;
        this.baselinePaint = Color.BLACK;
        this.crosshairPaint = Color.BLUE;
        this.axisLabelPaint = Color.DARK_GRAY;
        this.tickLabelPaint = Color.DARK_GRAY;
        this.barPainter = new GradientBarPainter();
        this.xyBarPainter = new GradientXYBarPainter();
        this.shadowVisible = false;
        this.shadowPaint = Color.GRAY;
        this.itemLabelPaint = Color.BLACK;
        this.thermometerPaint = Color.WHITE;
        this.errorIndicatorPaint = Color.BLACK;
        this.shadowGenerator = shadow ? new DefaultShadowGenerator() : null;
    }

    
    public Font getExtraLargeFont() {
        return this.extraLargeFont;
    }

    
    public void setExtraLargeFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.extraLargeFont = font;
    }

    
    public Font getLargeFont() {
        return this.largeFont;
    }

    
    public void setLargeFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.largeFont = font;
    }

    
    public Font getRegularFont() {
        return this.regularFont;
    }

    
    public void setRegularFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.regularFont = font;
    }

    
    public Font getSmallFont() {
        return this.smallFont;
    }

    
    public void setSmallFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.smallFont = font;
    }

    
    public Paint getTitlePaint() {
        return this.titlePaint;
    }

    
    public void setTitlePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.titlePaint = paint;
    }

    
    public Paint getSubtitlePaint() {
        return this.subtitlePaint;
    }

    
    public void setSubtitlePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.subtitlePaint = paint;
    }

    
    public Paint getChartBackgroundPaint() {
        return this.chartBackgroundPaint;
    }

    
    public void setChartBackgroundPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.chartBackgroundPaint = paint;
    }

    
    public Paint getLegendBackgroundPaint() {
        return this.legendBackgroundPaint;
    }

    
    public void setLegendBackgroundPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.legendBackgroundPaint = paint;
    }

    
    public Paint getLegendItemPaint() {
        return this.legendItemPaint;
    }

    
    public void setLegendItemPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.legendItemPaint = paint;
    }

    
    public Paint getPlotBackgroundPaint() {
        return this.plotBackgroundPaint;
    }

    
    public void setPlotBackgroundPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.plotBackgroundPaint = paint;
    }

    
    public Paint getPlotOutlinePaint() {
        return this.plotOutlinePaint;
    }

    
    public void setPlotOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.plotOutlinePaint = paint;
    }

    
    public PieLabelLinkStyle getLabelLinkStyle() {
        return this.labelLinkStyle;
    }

    
    public void setLabelLinkStyle(PieLabelLinkStyle style) {
        Args.nullNotPermitted(style, "style");
        this.labelLinkStyle = style;
    }

    
    public Paint getLabelLinkPaint() {
        return this.labelLinkPaint;
    }

    
    public void setLabelLinkPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.labelLinkPaint = paint;
    }

    
    public Paint getDomainGridlinePaint() {
        return this.domainGridlinePaint;
    }

    
    public void setDomainGridlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.domainGridlinePaint = paint;
    }

    
    public Paint getRangeGridlinePaint() {
        return this.rangeGridlinePaint;
    }

    
    public void setRangeGridlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.rangeGridlinePaint = paint;
    }

    
    public Paint getBaselinePaint() {
        return this.baselinePaint;
    }

    
    public void setBaselinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.baselinePaint = paint;
    }

    
    public Paint getCrosshairPaint() {
        return this.crosshairPaint;
    }

    
    public void setCrosshairPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.crosshairPaint = paint;
    }

    
    public RectangleInsets getAxisOffset() {
        return this.axisOffset;
    }

    
    public void setAxisOffset(RectangleInsets offset) {
        Args.nullNotPermitted(offset, "offset");
        this.axisOffset = offset;
    }

    
    public Paint getAxisLabelPaint() {
        return this.axisLabelPaint;
    }

    
    public void setAxisLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.axisLabelPaint = paint;
    }

    
    public Paint getTickLabelPaint() {
        return this.tickLabelPaint;
    }

    
    public void setTickLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.tickLabelPaint = paint;
    }

    
    public Paint getItemLabelPaint() {
        return this.itemLabelPaint;
    }

    
    public void setItemLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.itemLabelPaint = paint;
    }

    
    public boolean isShadowVisible() {
        return this.shadowVisible;
    }

    
    public void setShadowVisible(boolean visible) {
        this.shadowVisible = visible;
    }

    
    public Paint getShadowPaint() {
        return this.shadowPaint;
    }

    
    public void setShadowPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.shadowPaint = paint;
    }

    
    public BarPainter getBarPainter() {
        return this.barPainter;
    }

    
    public void setBarPainter(BarPainter painter) {
        Args.nullNotPermitted(painter, "painter");
        this.barPainter = painter;
    }

    
    public XYBarPainter getXYBarPainter() {
        return this.xyBarPainter;
    }

    
    public void setXYBarPainter(XYBarPainter painter) {
        Args.nullNotPermitted(painter, "painter");
        this.xyBarPainter = painter;
    }

    
    public Paint getThermometerPaint() {
        return this.thermometerPaint;
    }

    
    public void setThermometerPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.thermometerPaint = paint;
    }

    
    public Paint getErrorIndicatorPaint() {
        return this.errorIndicatorPaint;
    }

    
    public void setErrorIndicatorPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.errorIndicatorPaint = paint;
    }

    
    public Paint getGridBandPaint() {
        return this.gridBandPaint;
    }

    
    public void setGridBandPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.gridBandPaint = paint;
    }

    
    public Paint getGridBandAlternatePaint() {
        return this.gridBandAlternatePaint;
    }

    
    public void setGridBandAlternatePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.gridBandAlternatePaint = paint;
    }

    
    public String getName() {
        return this.name;
    }

    
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        if (this.drawingSupplier instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.drawingSupplier;
              try {
                result = (DrawingSupplier) pc.clone();
            }
            catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    
    public void setDrawingSupplier(DrawingSupplier supplier) {
        Args.nullNotPermitted(supplier, "supplier");
        this.drawingSupplier = supplier;
    }

    
    @Override
    public void apply(JFreeChart chart) {
        Args.nullNotPermitted(chart, "chart");
        TextTitle title = chart.getTitle();
        if (title != null) {
            title.setFont(this.extraLargeFont);
            title.setPaint(this.titlePaint);
        }

        int subtitleCount = chart.getSubtitleCount();
        for (int i = 0; i < subtitleCount; i++) {
            applyToTitle(chart.getSubtitle(i));
        }

        chart.setBackgroundPaint(this.chartBackgroundPaint);

        // now process the plot if there is one
        Plot plot = chart.getPlot();
        if (plot != null) {
            applyToPlot(plot);
        }
    }

    
    protected void applyToTitle(Title title) {
        if (title instanceof TextTitle) {
            TextTitle tt = (TextTitle) title;
            tt.setFont(this.largeFont);
            tt.setPaint(this.subtitlePaint);
        }
        else if (title instanceof LegendTitle) {
            LegendTitle lt = (LegendTitle) title;
            if (lt.getBackgroundPaint() != null) {
                lt.setBackgroundPaint(this.legendBackgroundPaint);
            }
            lt.setItemFont(this.regularFont);
            lt.setItemPaint(this.legendItemPaint);
            if (lt.getWrapper() != null) {
                applyToBlockContainer(lt.getWrapper());
            }
        }
        else if (title instanceof PaintScaleLegend) {
            PaintScaleLegend psl = (PaintScaleLegend) title;
            psl.setBackgroundPaint(this.legendBackgroundPaint);
            ValueAxis axis = psl.getAxis();
            if (axis != null) {
                applyToValueAxis(axis);
            }
        }
        else if (title instanceof CompositeTitle) {
            CompositeTitle ct = (CompositeTitle) title;
            BlockContainer bc = ct.getContainer();
            for (Block b: bc.getBlocks()) {
                if (b instanceof Title) {
                    applyToTitle((Title) b);
                }
            }
        }
    }

    
    protected void applyToBlockContainer(BlockContainer bc) {
        for (Block b : bc.getBlocks()) {
            applyToBlock(b);
        }
    }

    
    protected void applyToBlock(Block b) {
        if (b instanceof Title) {
            applyToTitle((Title) b);
        }
        else if (b instanceof LabelBlock) {
            LabelBlock lb = (LabelBlock) b;
            lb.setFont(this.regularFont);
            lb.setPaint(this.legendItemPaint);
        }
    }

    
    protected void applyToPlot(Plot plot) {
        Args.nullNotPermitted(plot, "plot");
        if (plot.getDrawingSupplier() != null) {
            plot.setDrawingSupplier(getDrawingSupplier());
        }
        if (plot.getBackgroundPaint() != null) {
            plot.setBackgroundPaint(this.plotBackgroundPaint);
        }
        plot.setOutlinePaint(this.plotOutlinePaint);

        // now handle specific plot types (and yes, I know this is some
        // really ugly code that has to be manually updated any time a new
        // plot type is added - I should have written something much cooler,
        // but I didn't and neither did anyone else).
        if (plot instanceof PiePlot) {
            applyToPiePlot((PiePlot) plot);
        }
        else if (plot instanceof MultiplePiePlot) {
            applyToMultiplePiePlot((MultiplePiePlot) plot);
        }
        else if (plot instanceof CategoryPlot) {
            applyToCategoryPlot((CategoryPlot) plot);
        }
        else if (plot instanceof XYPlot) {
            applyToXYPlot((XYPlot) plot);
        }
        else if (plot instanceof FastScatterPlot) {
            applyToFastScatterPlot((FastScatterPlot) plot);
        }
        else if (plot instanceof MeterPlot) {
            applyToMeterPlot((MeterPlot) plot);
        }
        else if (plot instanceof ThermometerPlot) {
            applyToThermometerPlot((ThermometerPlot) plot);
        }
        else if (plot instanceof SpiderWebPlot) {
            applyToSpiderWebPlot((SpiderWebPlot) plot);
        }
        else if (plot instanceof PolarPlot) {
            applyToPolarPlot((PolarPlot) plot);
        }
    }

    
    protected void applyToPiePlot(PiePlot plot) {
        plot.setLabelLinkPaint(this.labelLinkPaint);
        plot.setLabelLinkStyle(this.labelLinkStyle);
        plot.setLabelFont(this.regularFont);
        plot.setShadowGenerator(this.shadowGenerator);

        // clear the section attributes so that the theme's DrawingSupplier
        // will be used
        if (plot.getAutoPopulateSectionPaint()) {
            plot.clearSectionPaints(false);
        }
        if (plot.getAutoPopulateSectionOutlinePaint()) {
            plot.clearSectionOutlinePaints(false);
        }
        if (plot.getAutoPopulateSectionOutlineStroke()) {
            plot.clearSectionOutlineStrokes(false);
        }
    }

    
    protected void applyToMultiplePiePlot(MultiplePiePlot plot) {
        apply(plot.getPieChart());
    }

    
    protected void applyToCategoryPlot(CategoryPlot plot) {
        plot.setAxisOffset(this.axisOffset);
        plot.setDomainGridlinePaint(this.domainGridlinePaint);
        plot.setRangeGridlinePaint(this.rangeGridlinePaint);
        plot.setRangeZeroBaselinePaint(this.baselinePaint);
        plot.setShadowGenerator(this.shadowGenerator);

        // process all domain axes
        int domainAxisCount = plot.getDomainAxisCount();
        for (int i = 0; i < domainAxisCount; i++) {
            CategoryAxis axis = plot.getDomainAxis(i);
            if (axis != null) {
                applyToCategoryAxis(axis);
            }
        }

        // process all range axes
        int rangeAxisCount = plot.getRangeAxisCount();
        for (int i = 0; i < rangeAxisCount; i++) {
            ValueAxis axis = plot.getRangeAxis(i);
            if (axis != null) {
                applyToValueAxis(axis);
            }
        }

        // process all renderers
        int rendererCount = plot.getRendererCount();
        for (int i = 0; i < rendererCount; i++) {
            CategoryItemRenderer r = plot.getRenderer(i);
            if (r != null) {
                applyToCategoryItemRenderer(r);
            }
        }

        if (plot instanceof CombinedDomainCategoryPlot) {
            CombinedDomainCategoryPlot cp = (CombinedDomainCategoryPlot) plot;
            for (CategoryPlot subplot : cp.getSubplots()) {
                if (subplot != null) {
                    applyToPlot(subplot);
                }
            }
        }
        if (plot instanceof CombinedRangeCategoryPlot) {
            CombinedRangeCategoryPlot cp = (CombinedRangeCategoryPlot) plot;
            for (CategoryPlot subplot : cp.getSubplots()) {
                if (subplot != null) {
                    applyToPlot(subplot);
                }
            }
        }
    }

    
    protected <S extends Comparable<S>> void applyToXYPlot(XYPlot<S> plot) {
        plot.setAxisOffset(this.axisOffset);
        plot.setDomainZeroBaselinePaint(this.baselinePaint);
        plot.setRangeZeroBaselinePaint(this.baselinePaint);
        plot.setDomainGridlinePaint(this.domainGridlinePaint);
        plot.setRangeGridlinePaint(this.rangeGridlinePaint);
        plot.setDomainCrosshairPaint(this.crosshairPaint);
        plot.setRangeCrosshairPaint(this.crosshairPaint);
        plot.setShadowGenerator(this.shadowGenerator);

        // process all domain axes
        int domainAxisCount = plot.getDomainAxisCount();
        for (int i = 0; i < domainAxisCount; i++) {
            ValueAxis axis = plot.getDomainAxis(i);
            if (axis != null) {
                applyToValueAxis(axis);
            }
        }

        // process all range axes
        int rangeAxisCount = plot.getRangeAxisCount();
        for (int i = 0; i < rangeAxisCount; i++) {
            ValueAxis axis = plot.getRangeAxis(i);
            if (axis != null) {
                applyToValueAxis(axis);
            }
        }

        // process all renderers
        int rendererCount = plot.getRendererCount();
        for (int i = 0; i < rendererCount; i++) {
            XYItemRenderer r = plot.getRenderer(i);
            if (r != null) {
                applyToXYItemRenderer(r);
            }
        }
        // process all annotations

        for (XYAnnotation a : plot.getAnnotations()) {
            applyToXYAnnotation(a);
        }

        if (plot instanceof CombinedDomainXYPlot) {
            CombinedDomainXYPlot<S> cp = (CombinedDomainXYPlot) plot;
            for (XYPlot<S> subplot : cp.getSubplots()) {
                if (subplot != null) {
                    applyToPlot(subplot);
                }
            }
        }
        if (plot instanceof CombinedRangeXYPlot) {
            CombinedRangeXYPlot<S> cp = (CombinedRangeXYPlot) plot;
            for (XYPlot subplot : cp.getSubplots()) {
                if (subplot != null) {
                    applyToPlot(subplot);
                }
            }
        }
    }

    
    protected void applyToFastScatterPlot(FastScatterPlot plot) {
        plot.setDomainGridlinePaint(this.domainGridlinePaint);
        plot.setRangeGridlinePaint(this.rangeGridlinePaint);
        ValueAxis xAxis = plot.getDomainAxis();
        if (xAxis != null) {
            applyToValueAxis(xAxis);
        }
        ValueAxis yAxis = plot.getRangeAxis();
        if (yAxis != null) {
            applyToValueAxis(yAxis);
        }

    }

    
    protected void applyToPolarPlot(PolarPlot plot) {
        plot.setAngleLabelFont(this.regularFont);
        plot.setAngleLabelPaint(this.tickLabelPaint);
        plot.setAngleGridlinePaint(this.domainGridlinePaint);
        plot.setRadiusGridlinePaint(this.rangeGridlinePaint);
        ValueAxis axis = plot.getAxis();
        if (axis != null) {
            applyToValueAxis(axis);
        }
    }

    
    protected void applyToSpiderWebPlot(SpiderWebPlot plot) {
        plot.setLabelFont(this.regularFont);
        plot.setLabelPaint(this.axisLabelPaint);
        plot.setAxisLinePaint(this.axisLabelPaint);
    }

    
    protected void applyToMeterPlot(MeterPlot plot) {
        plot.setDialBackgroundPaint(this.plotBackgroundPaint);
        plot.setValueFont(this.largeFont);
        plot.setValuePaint(this.axisLabelPaint);
        plot.setDialOutlinePaint(this.plotOutlinePaint);
        plot.setNeedlePaint(this.thermometerPaint);
        plot.setTickLabelFont(this.regularFont);
        plot.setTickLabelPaint(this.tickLabelPaint);
    }

    
    protected void applyToThermometerPlot(ThermometerPlot plot) {
        plot.setValueFont(this.largeFont);
        plot.setThermometerPaint(this.thermometerPaint);
        ValueAxis axis = plot.getRangeAxis();
        if (axis != null) {
            applyToValueAxis(axis);
        }
    }

    
    protected void applyToCategoryAxis(CategoryAxis axis) {
        axis.setLabelFont(this.largeFont);
        axis.setLabelPaint(this.axisLabelPaint);
        axis.setTickLabelFont(this.regularFont);
        axis.setTickLabelPaint(this.tickLabelPaint);
        if (axis instanceof SubCategoryAxis) {
            SubCategoryAxis sca = (SubCategoryAxis) axis;
            sca.setSubLabelFont(this.regularFont);
            sca.setSubLabelPaint(this.tickLabelPaint);
        }
    }

    
    protected void applyToValueAxis(ValueAxis axis) {
        axis.setLabelFont(this.largeFont);
        axis.setLabelPaint(this.axisLabelPaint);
        axis.setTickLabelFont(this.regularFont);
        axis.setTickLabelPaint(this.tickLabelPaint);
        if (axis instanceof SymbolAxis) {
            applyToSymbolAxis((SymbolAxis) axis);
        }
        if (axis instanceof PeriodAxis) {
            applyToPeriodAxis((PeriodAxis) axis);
        }
    }

    
    protected void applyToSymbolAxis(SymbolAxis axis) {
        axis.setGridBandPaint(this.gridBandPaint);
        axis.setGridBandAlternatePaint(this.gridBandAlternatePaint);
    }

    
    protected void applyToPeriodAxis(PeriodAxis axis) {
        PeriodAxisLabelInfo[] info = axis.getLabelInfo();
        for (int i = 0; i < info.length; i++) {
            PeriodAxisLabelInfo e = info[i];
            PeriodAxisLabelInfo n = new PeriodAxisLabelInfo(e.getPeriodClass(),
                    e.getDateFormat(), e.getPadding(), this.regularFont,
                    this.tickLabelPaint, e.getDrawDividers(),
                    e.getDividerStroke(), e.getDividerPaint());
            info[i] = n;
        }
        axis.setLabelInfo(info);
    }

    
    protected void applyToAbstractRenderer(AbstractRenderer renderer) {
        if (renderer.getAutoPopulateSeriesPaint()) {
            renderer.clearSeriesPaints(false);
        }
        if (renderer.getAutoPopulateSeriesStroke()) {
            renderer.clearSeriesStrokes(false);
        }
    }

    
    protected void applyToCategoryItemRenderer(CategoryItemRenderer renderer) {
        Args.nullNotPermitted(renderer, "renderer");

        if (renderer instanceof AbstractRenderer) {
            applyToAbstractRenderer((AbstractRenderer) renderer);
        }

        renderer.setDefaultItemLabelFont(this.regularFont);
        renderer.setDefaultItemLabelPaint(this.itemLabelPaint);

        // now we handle some special cases - yes, UGLY code alert!

        // BarRenderer
        if (renderer instanceof BarRenderer) {
            BarRenderer br = (BarRenderer) renderer;
            br.setBarPainter(this.barPainter);
            br.setShadowVisible(this.shadowVisible);
            br.setShadowPaint(this.shadowPaint);
        }


        //  StatisticalBarRenderer
        if (renderer instanceof StatisticalBarRenderer) {
            StatisticalBarRenderer sbr = (StatisticalBarRenderer) renderer;
            sbr.setErrorIndicatorPaint(this.errorIndicatorPaint);
        }

        // MinMaxCategoryRenderer
        if (renderer instanceof MinMaxCategoryRenderer) {
            MinMaxCategoryRenderer mmcr = (MinMaxCategoryRenderer) renderer;
            mmcr.setGroupPaint(this.errorIndicatorPaint);
        }
    }

    
    protected void applyToXYItemRenderer(XYItemRenderer renderer) {
        Args.nullNotPermitted(renderer, "renderer");
        if (renderer instanceof AbstractRenderer) {
            applyToAbstractRenderer((AbstractRenderer) renderer);
        }
        renderer.setDefaultItemLabelFont(this.regularFont);
        renderer.setDefaultItemLabelPaint(this.itemLabelPaint);
        if (renderer instanceof XYBarRenderer) {
            XYBarRenderer br = (XYBarRenderer) renderer;
            br.setBarPainter(this.xyBarPainter);
            br.setShadowVisible(this.shadowVisible);
        }
    }

    
    protected void applyToXYAnnotation(XYAnnotation annotation) {
        Args.nullNotPermitted(annotation, "annotation");
        if (annotation instanceof XYTextAnnotation) {
            XYTextAnnotation xyta = (XYTextAnnotation) annotation;
            xyta.setFont(this.smallFont);
            xyta.setPaint(this.itemLabelPaint);
        }
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof StandardChartTheme)) {
            return false;
        }
        StandardChartTheme that = (StandardChartTheme) obj;
        if (!this.name.equals(that.name)) {
            return false;
        }
        if (!this.extraLargeFont.equals(that.extraLargeFont)) {
            return false;
        }
        if (!this.largeFont.equals(that.largeFont)) {
            return false;
        }
        if (!this.regularFont.equals(that.regularFont)) {
            return false;
        }
        if (!this.smallFont.equals(that.smallFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.titlePaint, that.titlePaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.subtitlePaint, that.subtitlePaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.chartBackgroundPaint,
                that.chartBackgroundPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.legendBackgroundPaint,
                that.legendBackgroundPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.legendItemPaint, that.legendItemPaint)) {
            return false;
        }
        if (!this.drawingSupplier.equals(that.drawingSupplier)) {
            return false;
        }
        if (!PaintUtils.equal(this.plotBackgroundPaint,
                that.plotBackgroundPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.plotOutlinePaint,
                that.plotOutlinePaint)) {
            return false;
        }
        if (!this.labelLinkStyle.equals(that.labelLinkStyle)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelLinkPaint, that.labelLinkPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.domainGridlinePaint,
                that.domainGridlinePaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.rangeGridlinePaint,
                that.rangeGridlinePaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.crosshairPaint, that.crosshairPaint)) {
            return false;
        }
        if (!this.axisOffset.equals(that.axisOffset)) {
            return false;
        }
        if (!PaintUtils.equal(this.axisLabelPaint, that.axisLabelPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.tickLabelPaint, that.tickLabelPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.itemLabelPaint, that.itemLabelPaint)) {
            return false;
        }
        if (this.shadowVisible != that.shadowVisible) {
            return false;
        }
        if (!PaintUtils.equal(this.shadowPaint, that.shadowPaint)) {
            return false;
        }
        if (!this.barPainter.equals(that.barPainter)) {
            return false;
        }
        if (!this.xyBarPainter.equals(that.xyBarPainter)) {
            return false;
        }
        if (!PaintUtils.equal(this.thermometerPaint,
                that.thermometerPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.errorIndicatorPaint,
                that.errorIndicatorPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.gridBandPaint, that.gridBandPaint)) {
            return false;
        }
        if (!PaintUtils.equal(this.gridBandAlternatePaint,
                that.gridBandAlternatePaint)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.name);
        hash = 71 * hash + Objects.hashCode(this.extraLargeFont);
        hash = 71 * hash + Objects.hashCode(this.largeFont);
        hash = 71 * hash + Objects.hashCode(this.regularFont);
        hash = 71 * hash + Objects.hashCode(this.smallFont);
        hash = 71 * hash + Objects.hashCode(this.titlePaint);
        hash = 71 * hash + Objects.hashCode(this.subtitlePaint);
        hash = 71 * hash + Objects.hashCode(this.chartBackgroundPaint);
        hash = 71 * hash + Objects.hashCode(this.legendBackgroundPaint);
        hash = 71 * hash + Objects.hashCode(this.legendItemPaint);
        hash = 71 * hash + Objects.hashCode(this.plotBackgroundPaint);
        hash = 71 * hash + Objects.hashCode(this.plotOutlinePaint);
        hash = 71 * hash + Objects.hashCode(this.labelLinkStyle);
        hash = 71 * hash + Objects.hashCode(this.labelLinkPaint);
        hash = 71 * hash + Objects.hashCode(this.domainGridlinePaint);
        hash = 71 * hash + Objects.hashCode(this.rangeGridlinePaint);
        hash = 71 * hash + Objects.hashCode(this.crosshairPaint);
        hash = 71 * hash + Objects.hashCode(this.axisOffset);
        hash = 71 * hash + Objects.hashCode(this.axisLabelPaint);
        hash = 71 * hash + Objects.hashCode(this.tickLabelPaint);
        hash = 71 * hash + Objects.hashCode(this.itemLabelPaint);
        hash = 71 * hash + (this.shadowVisible ? 1 : 0);
        hash = 71 * hash + Objects.hashCode(this.shadowPaint);
        hash = 71 * hash + Objects.hashCode(this.barPainter);
        hash = 71 * hash + Objects.hashCode(this.xyBarPainter);
        hash = 71 * hash + Objects.hashCode(this.thermometerPaint);
        hash = 71 * hash + Objects.hashCode(this.errorIndicatorPaint);
        hash = 71 * hash + Objects.hashCode(this.gridBandPaint);
        hash = 71 * hash + Objects.hashCode(this.gridBandAlternatePaint);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writePaint(this.titlePaint, stream);
        SerialUtils.writePaint(this.subtitlePaint, stream);
        SerialUtils.writePaint(this.chartBackgroundPaint, stream);
        SerialUtils.writePaint(this.legendBackgroundPaint, stream);
        SerialUtils.writePaint(this.legendItemPaint, stream);
        SerialUtils.writePaint(this.plotBackgroundPaint, stream);
        SerialUtils.writePaint(this.plotOutlinePaint, stream);
        SerialUtils.writePaint(this.labelLinkPaint, stream);
        SerialUtils.writePaint(this.baselinePaint, stream);
        SerialUtils.writePaint(this.domainGridlinePaint, stream);
        SerialUtils.writePaint(this.rangeGridlinePaint, stream);
        SerialUtils.writePaint(this.crosshairPaint, stream);
        SerialUtils.writePaint(this.axisLabelPaint, stream);
        SerialUtils.writePaint(this.tickLabelPaint, stream);
        SerialUtils.writePaint(this.itemLabelPaint, stream);
        SerialUtils.writePaint(this.shadowPaint, stream);
        SerialUtils.writePaint(this.thermometerPaint, stream);
        SerialUtils.writePaint(this.errorIndicatorPaint, stream);
        SerialUtils.writePaint(this.gridBandPaint, stream);
        SerialUtils.writePaint(this.gridBandAlternatePaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.titlePaint = SerialUtils.readPaint(stream);
        this.subtitlePaint = SerialUtils.readPaint(stream);
        this.chartBackgroundPaint = SerialUtils.readPaint(stream);
        this.legendBackgroundPaint = SerialUtils.readPaint(stream);
        this.legendItemPaint = SerialUtils.readPaint(stream);
        this.plotBackgroundPaint = SerialUtils.readPaint(stream);
        this.plotOutlinePaint = SerialUtils.readPaint(stream);
        this.labelLinkPaint = SerialUtils.readPaint(stream);
        this.baselinePaint = SerialUtils.readPaint(stream);
        this.domainGridlinePaint = SerialUtils.readPaint(stream);
        this.rangeGridlinePaint = SerialUtils.readPaint(stream);
        this.crosshairPaint = SerialUtils.readPaint(stream);
        this.axisLabelPaint = SerialUtils.readPaint(stream);
        this.tickLabelPaint = SerialUtils.readPaint(stream);
        this.itemLabelPaint = SerialUtils.readPaint(stream);
        this.shadowPaint = SerialUtils.readPaint(stream);
        this.thermometerPaint = SerialUtils.readPaint(stream);
        this.errorIndicatorPaint = SerialUtils.readPaint(stream);
        this.gridBandPaint = SerialUtils.readPaint(stream);
        this.gridBandAlternatePaint = SerialUtils.readPaint(stream);
    }

}
