

package org.jfree.chart.plot;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.ChartElementVisitor;

import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.axis.AxisSpace;
import org.jfree.chart.axis.AxisState;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.event.PlotChangeListener;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.util.ShadowGenerator;
import org.jfree.data.Range;


public class CombinedRangeCategoryPlot extends CategoryPlot
        implements PlotChangeListener {

    
    private static final long serialVersionUID = 7260210007554504515L;

    
    private List<CategoryPlot> subplots;

    
    private double gap;

    
    private transient Rectangle2D[] subplotArea;  // TODO: move to plot state

    
    public CombinedRangeCategoryPlot() {
        this(new NumberAxis());
    }

    
    public CombinedRangeCategoryPlot(ValueAxis rangeAxis) {
        super(null, null, rangeAxis, null);
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
        // defer argument checking
        add(subplot, 1);
    }

    
    public void add(CategoryPlot subplot, int weight) {
        Args.nullNotPermitted(subplot, "subplot");
        if (weight <= 0) {
            throw new IllegalArgumentException("Require weight >= 1.");
        }
        // store the plot and its weight
        subplot.setParent(this);
        subplot.setWeight(weight);
        subplot.setInsets(new RectangleInsets(0.0, 0.0, 0.0, 0.0));
        subplot.setRangeAxis(null);
        subplot.setOrientation(getOrientation());
        subplot.addChangeListener(this);
        this.subplots.add(subplot);
        // configure the range axis...
        ValueAxis axis = getRangeAxis();
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

            ValueAxis range = getRangeAxis();
            if (range != null) {
                range.configure();
            }

            ValueAxis range2 = getRangeAxis(1);
            if (range2 != null) {
                range2.configure();
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

    
    @Override
    protected AxisSpace calculateAxisSpace(Graphics2D g2, 
            Rectangle2D plotArea) {

        AxisSpace space = new AxisSpace();
        PlotOrientation orientation = getOrientation();

        // work out the space required by the domain axis...
        AxisSpace fixed = getFixedRangeAxisSpace();
        if (fixed != null) {
            if (orientation == PlotOrientation.VERTICAL) {
                space.setLeft(fixed.getLeft());
                space.setRight(fixed.getRight());
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                space.setTop(fixed.getTop());
                space.setBottom(fixed.getBottom());
            }
        }
        else {
            ValueAxis valueAxis = getRangeAxis();
            RectangleEdge valueEdge = Plot.resolveRangeAxisLocation(
                    getRangeAxisLocation(), orientation);
            if (valueAxis != null) {
                space = valueAxis.reserveSpace(g2, this, plotArea, valueEdge,
                        space);
            }
        }

        Rectangle2D adjustedPlotArea = space.shrink(plotArea, null);
        // work out the maximum height or width of the non-shared axes...
        int n = this.subplots.size();
        int totalWeight = 0;
        for (int i = 0; i < n; i++) {
            CategoryPlot sub = this.subplots.get(i);
            totalWeight += sub.getWeight();
        }
        // calculate plotAreas of all sub-plots, maximum vertical/horizontal
        // axis width/height
        this.subplotArea = new Rectangle2D[n];
        double x = adjustedPlotArea.getX();
        double y = adjustedPlotArea.getY();
        double usableSize = 0.0;
        if (orientation == PlotOrientation.VERTICAL) {
            usableSize = adjustedPlotArea.getWidth() - this.gap * (n - 1);
        }
        else if (orientation == PlotOrientation.HORIZONTAL) {
            usableSize = adjustedPlotArea.getHeight() - this.gap * (n - 1);
        }

        for (int i = 0; i < n; i++) {
            CategoryPlot plot = this.subplots.get(i);

            // calculate sub-plot area
            if (orientation == PlotOrientation.VERTICAL) {
                double w = usableSize * plot.getWeight() / totalWeight;
                this.subplotArea[i] = new Rectangle2D.Double(x, y, w,
                        adjustedPlotArea.getHeight());
                x = x + w + this.gap;
            }
            else if (orientation == PlotOrientation.HORIZONTAL) {
                double h = usableSize * plot.getWeight() / totalWeight;
                this.subplotArea[i] = new Rectangle2D.Double(x, y,
                        adjustedPlotArea.getWidth(), h);
                y = y + h + this.gap;
            }

            AxisSpace subSpace = plot.calculateDomainAxisSpace(g2,
                    this.subplotArea[i], null);
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
        insets.trim(area);

        // calculate the data area...
        AxisSpace space = calculateAxisSpace(g2, area);
        Rectangle2D dataArea = space.shrink(area, null);

        // set the width and height of non-shared axis of all sub-plots
        setFixedDomainAxisSpaceForSubplots(space);

        // draw the shared axis
        ValueAxis axis = getRangeAxis();
        RectangleEdge rangeEdge = getRangeAxisEdge();
        double cursor = RectangleEdge.coordinate(dataArea, rangeEdge);
        AxisState state = axis.draw(g2, cursor, area, dataArea, rangeEdge,
                info);
        if (parentState == null) {
            parentState = new PlotState();
        }
        parentState.getSharedAxisStates().put(axis, state);

        // draw all the charts
        for (int i = 0; i < this.subplots.size(); i++) {
            CategoryPlot plot = this.subplots.get(i);
            PlotRenderingInfo subplotInfo = null;
            if (info != null) {
                subplotInfo = new PlotRenderingInfo(info.getOwner());
                info.addSubplotInfo(subplotInfo);
            }
            Point2D subAnchor = null;
            if (anchor != null && this.subplotArea[i].contains(anchor)) {
                subAnchor = anchor;
            }
            plot.draw(g2, this.subplotArea[i], subAnchor, parentState,
                    subplotInfo);
        }

        if (info != null) {
            info.setDataArea(dataArea);
        }

    }

    
    @Override
    public void setOrientation(PlotOrientation orientation) {
        super.setOrientation(orientation);
        for (CategoryPlot subplot : this.subplots) {
            subplot.setOrientation(orientation);
        }
    }

    
    @Override
    public void setShadowGenerator(ShadowGenerator generator) {
        setNotify(false);
        super.setShadowGenerator(generator);
        for (CategoryPlot subplot : this.subplots) {
            subplot.setShadowGenerator(generator);
        }
        setNotify(true);
    }

    
    @Override
     public Range getDataRange(ValueAxis axis) {
         Range result = null;
         if (this.subplots != null) {
             for (CategoryPlot subplot : this.subplots) {
                 result = Range.combine(result, subplot.getDataRange(axis));
             }
         }
         return result;
     }

    
    @Override
    public LegendItemCollection getLegendItems() {
        LegendItemCollection result = getFixedLegendItems();
        if (result == null) {
            result = new LegendItemCollection();
            if (this.subplots != null) {
                for (CategoryPlot subplot : this.subplots) {
                    LegendItemCollection more = subplot.getLegendItems();
                    result.addAll(more);
                }
            }
        }
        return result;
    }

    
    protected void setFixedDomainAxisSpaceForSubplots(AxisSpace space) {
        for (CategoryPlot subplot : this.subplots) {
            subplot.setFixedDomainAxisSpace(space, false);
        }
    }

    
    @Override
    public void handleClick(int x, int y, PlotRenderingInfo info) {
        Rectangle2D dataArea = info.getDataArea();
        if (dataArea.contains(x, y)) {
            for (int i = 0; i < this.subplots.size(); i++) {
                CategoryPlot subplot = this.subplots.get(i);
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
        if (!(obj instanceof CombinedRangeCategoryPlot)) {
            return false;
        }
        CombinedRangeCategoryPlot that = (CombinedRangeCategoryPlot) obj;
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
        CombinedRangeCategoryPlot result
            = (CombinedRangeCategoryPlot) super.clone();
        result.subplots = CloneUtils.cloneList(this.subplots);
        for (Plot child : result.subplots) {
            child.setParent(result);
        }

        // after setting up all the subplots, the shared range axis may need
        // reconfiguring
        ValueAxis rangeAxis = result.getRangeAxis();
        if (rangeAxis != null) {
            rangeAxis.configure();
        }

        return result;
    }

    
    private void readObject(ObjectInputStream stream) throws IOException, 
            ClassNotFoundException {

        stream.defaultReadObject();

        // the range axis is deserialized before the subplots, so its value
        // range is likely to be incorrect...
        ValueAxis rangeAxis = getRangeAxis();
        if (rangeAxis != null) {
            rangeAxis.configure();
        }

    }

}
