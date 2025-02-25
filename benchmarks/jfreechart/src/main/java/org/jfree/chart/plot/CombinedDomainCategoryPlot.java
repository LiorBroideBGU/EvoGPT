

package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.ChartElementVisitor;

import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;


public class CombinedDomainCategoryPlot extends CategoryPlot
        implements PlotChangeListener {

    
    private static final long serialVersionUID = 8207194522653701572L;

    
    private List<CategoryPlot> subplots;

    
    private double gap;

    
    private transient Rectangle2D[] subplotAreas;
    // FIXME:  move the above to the plot state

    
    public CombinedDomainCategoryPlot() {
        this(new CategoryAxis());
    }

    
    public CombinedDomainCategoryPlot(CategoryAxis domainAxis) {
        super(null, domainAxis, null, null);
        this.subplots = new ArrayList<>();
        this.gap = 5.0;
    }

    
    public double getGap() {
        return this.gap;
    }

    
    public void setGap(double gap) {
        this.gap = gap;
        fireChangeEvent();
    }

    
    public void add(CategoryPlot subplot) {
        add(subplot, 1);
    }

    
    public void add(CategoryPlot subplot, int weight) {
        Args.nullNotPermitted(subplot, "subplot");
        if (weight < 1) {
            throw new IllegalArgumentException("Require weight >= 1.");
        }
        subplot.setParent(this);
        subplot.setWeight(weight);
        subplot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        subplot.setDomainAxis(null);
        subplot.setOrientation(getOrientation());
        subplot.addChangeListener(this);
        this.subplots.add(subplot);
        CategoryAxis axis = getDomainAxis();
        if (axis != null) {
            axis.configure();
        }
        fireChangeEvent();
    }

    
    public void remove(CategoryPlot subplot) {
        Args.nullNotPermitted(subplot, "subplot");
        int position = -1;
        int size = this.subplots.size();
        int i = 0;
        while (position == -1 && i < size) {
            if (this.subplots.get(i) == subplot) {
                position = i;
            }
            i++;
        }
        if (position != -1) {
            this.subplots.remove(position);
            subplot.setParent(null);
            subplot.removeChangeListener(this);
            CategoryAxis domain = getDomainAxis();
            if (domain != null) {
                domain.configure();
            }
            fireChangeEvent();
        }
    }

    
    public List<CategoryPlot> getSubplots() {
        if (this.subplots != null) {
            return Collections.unmodifiableList(this.subplots);
        }
        else {
            return Collections.EMPTY_LIST;
        }
    }

    
    public CategoryPlot findSubplot(PlotRenderingInfo info, Point2D source) {
        Args.nullNotPermitted(info, "info");
        Args.nullNotPermitted(source, "source");
        CategoryPlot result = null;
        int subplotIndex = info.getSubplotIndex(source);
        if (subplotIndex >= 0) {
            result =  (CategoryPlot) this.subplots.get(subplotIndex);
        }
        return result;
    }

    
    @Override
    public void zoomRangeAxes(double factor, PlotRenderingInfo info,
                              Point2D source) {
        zoomRangeAxes(factor, info, source, false);
    }

    
    @Override
    public void zoomRangeAxes(double factor, PlotRenderingInfo info,
                              Point2D source, boolean useAnchor) {
        // delegate 'info' and 'source' argument checks...
        CategoryPlot subplot = findSubplot(info, source);
        if (subplot != null) {
            subplot.zoomRangeAxes(factor, info, source, useAnchor);
        }
        else {
            // if the source point doesn't fall within a subplot, we do the
            // zoom on all subplots...
            for (CategoryPlot categoryPlot : getSubplots()) {
                subplot = categoryPlot;
                subplot.zoomRangeAxes(factor, info, source, useAnchor);
            }
        }
    }

    
    @Override
    public void zoomRangeAxes(double lowerPercent, double upperPercent,
                              PlotRenderingInfo info, Point2D source) {
        // delegate 'info' and 'source' argument checks...
        CategoryPlot subplot = findSubplot(info, source);
        if (subplot != null) {
            subplot.zoomRangeAxes(lowerPercent, upperPercent, info, source);
        }
        else {
            // if the source point doesn't fall within a subplot, we do the
            // zoom on all subplots...
            for (CategoryPlot categoryPlot : getSubplots()) {
                subplot = categoryPlot;
                subplot.zoomRangeAxes(lowerPercent, upperPercent, info, source);
            }
        }
    }

    
    @Override
    protected AxisSpace calculateAxisSpace(Graphics2D g2,
                                           Rectangle2D plotArea) {

        AxisSpace space = new AxisSpace();
        PlotOrientation orientation = getOrientation();

        // work out the space required by the domain axis...
        AxisSpace fixed = getFixedDomainAxisSpace();
        if (fixed != null) {
            if (orientation == PlotOrientation.HORIZONTAL) {
                space.setLeft(fixed.getLeft());
                space.setRight(fixed.getRight());
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                space.setTop(fixed.getTop());
                space.setBottom(fixed.getBottom());
            }
        }
        else {
            CategoryAxis categoryAxis = getDomainAxis();
            RectangleEdge categoryEdge = Plot.resolveDomainAxisLocation(
                    getDomainAxisLocation(), orientation);
            if (categoryAxis != null) {
                space = categoryAxis.reserveSpace(g2, this, plotArea,
                        categoryEdge, space);
            }
            else {
                if (getDrawSharedDomainAxis()) {
                    space = getDomainAxis().reserveSpace(g2, this, plotArea,
                            categoryEdge, space);
                }
            }
        }

        Rectangle2D adjustedPlotArea = space.shrink(plotArea, null);

        // work out the maximum height or width of the non-shared axes...
        int n = this.subplots.size();
        int totalWeight = 0;
        for (int i = 0; i < n; i++) {
            CategoryPlot sub = (CategoryPlot) this.subplots.get(i);
            totalWeight += sub.getWeight();
        }
        this.subplotAreas = new Rectangle2D[n];
        double x = adjustedPlotArea.getX();
        double y = adjustedPlotArea.getY();
        double usableSize = 0.0;
        if (orientation == PlotOrientation.HORIZONTAL) {
            usableSize = adjustedPlotArea.getWidth() - this.gap * (n - 1);
        }
        else if (orientation == PlotOrientation.VERTICAL) {
            usableSize = adjustedPlotArea.getHeight() - this.gap * (n - 1);
        }

        for (int i = 0; i < n; i++) {
            CategoryPlot plot = (CategoryPlot) this.subplots.get(i);

            // calculate sub-plot area
            if (orientation == PlotOrientation.HORIZONTAL) {
                double w = usableSize * plot.getWeight() / totalWeight;
                this.subplotAreas[i] = new Rectangle2D.Double(x, y, w,
                        adjustedPlotArea.getHeight());
                x = x + w + this.gap;
            }
            else if (orientation == PlotOrientation.VERTICAL) {
                double h = usableSize * plot.getWeight() / totalWeight;
                this.subplotAreas[i] = new Rectangle2D.Double(x, y,
                        adjustedPlotArea.getWidth(), h);
                y = y + h + this.gap;
            }

            AxisSpace subSpace = plot.calculateRangeAxisSpace(g2,
                    this.subplotAreas[i], null);
            space.ensureAtLeast(subSpace);

        }

        return space;
    }

    
    @Override
    public void receive(ChartElementVisitor visitor) {
        subplots.forEach(subplot -> {
            subplot.receive(visitor);
        });
        super.receive(visitor);
    }

    
    @Override
     public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor,
            PlotState parentState, PlotRenderingInfo info) {

        // set up info collection...
        if (info != null) {
            info.setPlotArea(area);
        }

        // adjust the drawing area for plot insets (if any)...
        RectangleInsets insets = getInsets();
        area.setRect(area.getX() + insets.getLeft(),
                area.getY() + insets.getTop(),
                area.getWidth() - insets.getLeft() - insets.getRight(),
                area.getHeight() - insets.getTop() - insets.getBottom());


        // calculate the data area...
        setFixedRangeAxisSpaceForSubplots(null);
        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);

        // set the width and height of non-shared axis of all sub-plots
        setFixedRangeAxisSpaceForSubplots(space);

        // draw the shared axis
        CategoryAxis axis = getDomainAxis();
        RectangleEdge domainEdge = getDomainAxisEdge();
        double cursor = RectangleEdge.coordinate(dataArea, domainEdge);
        AxisState axisState = axis.draw(g2, cursor, area, dataArea,
                domainEdge, info);
        if (parentState == null) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put(axis, axisState);

        // draw all the subplots
        for (int i = 0; i < this.subplots.size(); i++) {
            CategoryPlot plot = (CategoryPlot) this.subplots.get(i);
            PlotRenderingInfo subplotInfo = null;
            if (info != null) {
                subplotInfo = new PlotRenderingInfo(info.getOwner());
                info.addSubplotInfo(subplotInfo);
            }
            Point2D subAnchor = null;
            if (anchor != null && this.subplotAreas[i].contains(anchor)) {
                subAnchor = anchor;
            }
            plot.draw(g2, this.subplotAreas[i], subAnchor, parentState,
                    subplotInfo);
        }

        if (info != null) {
            info.setDataArea(dataArea);
        }

    }

    
    protected void setFixedRangeAxisSpaceForSubplots(AxisSpace space) {
        for (CategoryPlot plot : this.subplots) {
            plot.setFixedRangeAxisSpace(space, false);
        }
    }

    
    @Override
    public void setOrientation(PlotOrientation orientation) {
        super.setOrientation(orientation);
        for (CategoryPlot plot : this.subplots) {
            plot.setOrientation(orientation);
        }

    }

    
    @Override
    public void setShadowGenerator(ShadowGenerator generator) {
        setNotify(false);
        super.setShadowGenerator(generator);
        for (CategoryPlot plot : this.subplots) {
            plot.setShadowGenerator(generator);
        }
        setNotify(true);
    }

    
    @Override
     public Range getDataRange(ValueAxis axis) {
         // override is only for documentation purposes
         return super.getDataRange(axis);
     }

     
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = getFixedLegendItems();
        if (result == null) {
            result = new LegendItemCollection();
            if (this.subplots != null) {
                for (CategoryPlot plot : this.subplots) {
                    LegendItemCollection more = plot.getLegendItems();
                    result.addAll(more);
                }
            }
        }
        return result;
    }

    
    @Override
    public List getCategories() {
        List result = new java.util.ArrayList();
        if (this.subplots != null) {
            for (CategoryPlot plot : this.subplots) {
                List more = plot.getCategories();
                for (Object o : more) {
                    Comparable category = (Comparable) o;
                    if (!result.contains(category)) {
                        result.add(category);
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    
    @Override
    public List getCategoriesForAxis(CategoryAxis axis) {
        // FIXME:  this code means that it is not possible to use more than
        // one domain axis for the combined plots...
        return getCategories();
    }

    
    @Override
    public void handleClick(int x, int y, PlotRenderingInfo info) {

        Rectangle2D dataArea = info.getDataArea();
        if (dataArea.contains(x, y)) {
            for (int i = 0; i < this.subplots.size(); i++) {
                CategoryPlot subplot = (CategoryPlot) this.subplots.get(i);
                PlotRenderingInfo subplotInfo = info.getSubplotInfo(i);
                subplot.handleClick(x, y, subplotInfo);
            }
        }

    }

    
    @Override
    public void plotChanged(PlotChangeEvent event) {
        notifyListeners(event);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CombinedDomainCategoryPlot)) {
            return false;
        }
        CombinedDomainCategoryPlot that = (CombinedDomainCategoryPlot) obj;
        if (this.gap != that.gap) {
            return false;
        }
        if (!Objects.equals(this.subplots, that.subplots)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        CombinedDomainCategoryPlot result
            = (CombinedDomainCategoryPlot) super.clone();
        result.subplots = (List<CategoryPlot>) CloneUtils.cloneList(this.subplots);
        for (Iterator it = result.subplots.iterator(); it.hasNext();) {
            Plot child = (Plot) it.next();
            child.setParent(result);
        }
        return result;

    }

}
