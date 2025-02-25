

package org.jfree.chart.plot.flow;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.api.RectangleInsets;
import org.jfree.chart.api.VerticalAlignment;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.entity.FlowEntity;
import org.jfree.chart.entity.NodeEntity;
import org.jfree.chart.internal.Args;
import org.jfree.chart.labels.FlowLabelGenerator;
import org.jfree.chart.labels.StandardFlowLabelGenerator;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.PlotState;
import org.jfree.chart.text.TextAnchor;
import org.jfree.chart.text.TextUtils;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.data.flow.FlowDataset;
import org.jfree.data.flow.FlowDatasetUtils;
import org.jfree.data.flow.FlowKey;
import org.jfree.data.flow.NodeKey;


public class FlowPlot extends Plot implements Cloneable, PublicCloneable, 
        Serializable {

    
    private FlowDataset dataset;
    
    
    private double nodeWidth = 20.0;
    
    
    private double nodeMargin = 0.01;

    
    private double flowMargin = 0.005;
    
    
    private Map<NodeKey, Color> nodeColorMap;
    
    private List<Color> nodeColorSwatch;
    
    
    private int nodeColorSwatchPointer = 0;

    
    private Color defaultNodeColor;

    private Font defaultNodeLabelFont;
    
    private Paint defaultNodeLabelPaint;
    
    private VerticalAlignment nodeLabelAlignment;
    
    
    private double nodeLabelOffsetX;
    
    
    private double nodeLabelOffsetY;
    
    
    private FlowLabelGenerator toolTipGenerator; 
    
    
    public FlowPlot(FlowDataset dataset) {
        this.dataset = dataset;
        if (dataset != null) {
            dataset.addChangeListener(this);
        }
        this.nodeColorMap = new HashMap<>();
        this.nodeColorSwatch = new ArrayList<>();
        this.defaultNodeColor = Color.GRAY;
        this.defaultNodeLabelFont = new Font(Font.DIALOG, Font.BOLD, 12);
        this.defaultNodeLabelPaint = Color.BLACK;
        this.nodeLabelAlignment = VerticalAlignment.CENTER;
        this.nodeLabelOffsetX = 2.0;
        this.nodeLabelOffsetY = 2.0;
        this.toolTipGenerator = new StandardFlowLabelGenerator();
    }

    
    @Override
    public String getPlotType() {
        return "FlowPlot";
    }

    
    public FlowDataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(FlowDataset dataset) {
        this.dataset = dataset;
        fireChangeEvent();
    }

    
    public double getNodeMargin() {
        return this.nodeMargin;
    }

    
    public void setNodeMargin(double margin) {
        Args.requireNonNegative(margin, "margin");
        this.nodeMargin = margin;
        fireChangeEvent();
    }
    

    
    public double getFlowMargin() {
        return this.flowMargin;
    }
    
    
    public void setFlowMargin(double margin) {
        Args.requireNonNegative(margin, "margin");
        this.flowMargin = margin;
        fireChangeEvent();
    }

    
    public double getNodeWidth() {
        return this.nodeWidth;
    }

    
    public void setNodeWidth(double width) {
        this.nodeWidth = width;
        fireChangeEvent();
    }

    
    public List<Color> getNodeColorSwatch() {
        return new ArrayList<>(this.nodeColorSwatch);
    }

    
    public void setNodeColorSwatch(List<Color> colors) {
        Args.nullNotPermitted(colors, "colors");
        this.nodeColorSwatch = colors;
        
    }
    
    
    public Color getNodeFillColor(NodeKey nodeKey) {
        return this.nodeColorMap.get(nodeKey);
    }
    
    
    public void setNodeFillColor(NodeKey nodeKey, Color color) {
        this.nodeColorMap.put(nodeKey, color);
        fireChangeEvent();
    }
    
    
    public Color getDefaultNodeColor() {
        return this.defaultNodeColor;
    }
    
    
    public void setDefaultNodeColor(Color color) {
        Args.nullNotPermitted(color, "color");
        this.defaultNodeColor = color;
        fireChangeEvent();
    }

    
    public Font getDefaultNodeLabelFont() {
        return this.defaultNodeLabelFont;
    }

    
    public void setDefaultNodeLabelFont(Font font) {
        Args.nullNotPermitted(font, "font");
        this.defaultNodeLabelFont = font;
        fireChangeEvent();
    }

    
    public Paint getDefaultNodeLabelPaint() {
        return this.defaultNodeLabelPaint;
    }

    
    public void setDefaultNodeLabelPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.defaultNodeLabelPaint = paint;
        fireChangeEvent();
    }

    
    public VerticalAlignment getNodeLabelAlignment() {
        return this.nodeLabelAlignment;
    }
    
    
    public void setNodeLabelAlignment(VerticalAlignment alignment) {
        Args.nullNotPermitted(alignment, "alignment");
        this.nodeLabelAlignment = alignment;
        fireChangeEvent();
    }
    
    
    public double getNodeLabelOffsetX() {
        return this.nodeLabelOffsetX;
    }

    
    public void setNodeLabelOffsetX(double offsetX) {
        this.nodeLabelOffsetX = offsetX;
        fireChangeEvent();
    }

    
    public double getNodeLabelOffsetY() {
        return nodeLabelOffsetY;
    }

    
    public void setNodeLabelOffsetY(double offsetY) {
        this.nodeLabelOffsetY = offsetY;
        fireChangeEvent();
    }

    
    public FlowLabelGenerator getToolTipGenerator() {
        return this.toolTipGenerator;
    }
    
    
    public void setToolTipGenerator(FlowLabelGenerator generator) {
        this.toolTipGenerator = generator;
        fireChangeEvent();
    }

    
    @Override
    public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info) {
        Args.nullNotPermitted(g2, "g2");
        Args.nullNotPermitted(area, "area");
 
        EntityCollection entities = null;
        if (info != null) {
            info.setPlotArea(area);
            entities = info.getOwner().getEntityCollection();
        }
        RectangleInsets insets = getInsets();
        insets.trim(area);
        if (info != null) {
            info.setDataArea(area);
        }
        
        // use default JFreeChart background handling
        drawBackground(g2, area);

        // we need to ensure there is space to show all the inflows and all 
        // the outflows at each node group, so first we calculate the max
        // flow space required - for each node in the group, consider the 
        // maximum of the inflow and the outflow
        double flow2d = Double.POSITIVE_INFINITY;
        double nodeMargin2d = this.nodeMargin * area.getHeight();
        int stageCount = this.dataset.getStageCount();
        for (int stage = 0; stage < this.dataset.getStageCount(); stage++) {
            List<Comparable> sources = this.dataset.getSources(stage);
            int nodeCount = sources.size();
            double flowTotal = 0.0;
            for (Comparable source : sources) {
                double inflow = FlowDatasetUtils.calculateInflow(this.dataset, source, stage);
                double outflow = FlowDatasetUtils.calculateOutflow(this.dataset, source, stage);
                flowTotal = flowTotal + Math.max(inflow, outflow);
            }
            if (flowTotal > 0.0) {
                double availableH = area.getHeight() - (nodeCount - 1) * nodeMargin2d;
                flow2d = Math.min(availableH / flowTotal, flow2d);
            }
            
            if (stage == this.dataset.getStageCount() - 1) {
                // check inflows to the final destination nodes...
                List<Comparable> destinations = this.dataset.getDestinations(stage);
                int destinationCount = destinations.size();
                flowTotal = 0.0;
                for (Comparable destination : destinations) {
                    double inflow = FlowDatasetUtils.calculateInflow(this.dataset, destination, stage + 1);
                    flowTotal = flowTotal + inflow;
                }
                if (flowTotal > 0.0) {
                    double availableH = area.getHeight() - (destinationCount - 1) * nodeMargin2d;
                    flow2d = Math.min(availableH / flowTotal, flow2d);
                }
            }
        }

        double stageWidth = (area.getWidth() - ((stageCount + 1) * this.nodeWidth)) / stageCount;
        double flowOffset = area.getWidth() * this.flowMargin;
        
        Map<NodeKey, Rectangle2D> nodeRects = new HashMap<>();
        boolean hasNodeSelections = FlowDatasetUtils.hasNodeSelections(this.dataset);
        boolean hasFlowSelections = FlowDatasetUtils.hasFlowSelections(this.dataset);
        
        // iterate over all the stages, we can render the source node rects and
        // the flows ... we should add the destination node rects last, then
        // in a final pass add the labels
        for (int stage = 0; stage < this.dataset.getStageCount(); stage++) {
            
            double stageLeft = area.getX() + (stage + 1) * this.nodeWidth + (stage * stageWidth);
            double stageRight = stageLeft + stageWidth;
            
            // calculate the source node and flow rectangles
            Map<FlowKey, Rectangle2D> sourceFlowRects = new HashMap<>();
            double nodeY = area.getY();
            for (Object s : this.dataset.getSources(stage)) {
                Comparable source = (Comparable) s;
                double inflow = FlowDatasetUtils.calculateInflow(dataset, source, stage);
                double outflow = FlowDatasetUtils.calculateOutflow(dataset, source, stage);
                double nodeHeight = (Math.max(inflow, outflow) * flow2d);
                Rectangle2D nodeRect = new Rectangle2D.Double(stageLeft - nodeWidth, nodeY, nodeWidth, nodeHeight);
                if (entities != null) {
                    entities.add(new NodeEntity(new NodeKey<>(stage, source), nodeRect, source.toString()));                
                }
                nodeRects.put(new NodeKey<>(stage, source), nodeRect);
                double y = nodeY;
                for (Object d : this.dataset.getDestinations(stage)) {
                    Comparable destination = (Comparable) d;
                    Number flow = this.dataset.getFlow(stage, source, destination);
                    if (flow != null) {
                        double height = flow.doubleValue() * flow2d;
                        Rectangle2D rect = new Rectangle2D.Double(stageLeft - nodeWidth, y, nodeWidth, height);
                        sourceFlowRects.put(new FlowKey<>(stage, source, destination), rect);
                        y = y + height;
                    }
                }
                nodeY = nodeY + nodeHeight + nodeMargin2d;
            }
            
            // calculate the destination rectangles
            Map<FlowKey, Rectangle2D> destFlowRects = new HashMap<>();
            nodeY = area.getY();
            for (Object d : this.dataset.getDestinations(stage)) {
                Comparable destination = (Comparable) d;
                double inflow = FlowDatasetUtils.calculateInflow(dataset, destination, stage + 1);
                double outflow = FlowDatasetUtils.calculateOutflow(dataset, destination, stage + 1);
                double nodeHeight = Math.max(inflow, outflow) * flow2d;
                nodeRects.put(new NodeKey<>(stage + 1, destination), new Rectangle2D.Double(stageRight, nodeY, nodeWidth, nodeHeight));
                double y = nodeY;
                for (Object s : this.dataset.getSources(stage)) {
                    Comparable source = (Comparable) s;
                    Number flow = this.dataset.getFlow(stage, source, destination);
                    if (flow != null) {
                        double height = flow.doubleValue() * flow2d;
                        Rectangle2D rect = new Rectangle2D.Double(stageRight, y, nodeWidth, height);
                        y = y + height;
                        destFlowRects.put(new FlowKey<>(stage, source, destination), rect);
                    }
                }
                nodeY = nodeY + nodeHeight + nodeMargin2d;
            }
        
            for (Object s : this.dataset.getSources(stage)) {
                Comparable source = (Comparable) s;
                NodeKey nodeKey = new NodeKey<>(stage, source);
                Rectangle2D nodeRect = nodeRects.get(nodeKey);
                Color ncol = lookupNodeColor(nodeKey);
                if (hasNodeSelections) {
                    if (!Boolean.TRUE.equals(dataset.getNodeProperty(nodeKey, NodeKey.SELECTED_PROPERTY_KEY))) {
                        int g = (ncol.getRed() + ncol.getGreen() + ncol.getBlue()) / 3;
                        ncol = new Color(g, g, g, ncol.getAlpha());
                    }
                }
                g2.setPaint(ncol);
                g2.fill(nodeRect);
                                
                for (Object d : this.dataset.getDestinations(stage)) {
                    Comparable destination = (Comparable) d;
                    FlowKey flowKey = new FlowKey<>(stage, source, destination);
                    Rectangle2D sourceRect = sourceFlowRects.get(flowKey);
                    if (sourceRect == null) { 
                        continue; 
                    }
                    Rectangle2D destRect = destFlowRects.get(flowKey);
                
                    Path2D connect = new Path2D.Double();
                    connect.moveTo(sourceRect.getMaxX() + flowOffset, sourceRect.getMinY());
                    connect.curveTo(stageLeft + stageWidth / 2.0, sourceRect.getMinY(), stageLeft + stageWidth / 2.0, destRect.getMinY(), destRect.getX() - flowOffset, destRect.getMinY());
                    connect.lineTo(destRect.getX() - flowOffset, destRect.getMaxY());
                    connect.curveTo(stageLeft + stageWidth / 2.0, destRect.getMaxY(), stageLeft + stageWidth / 2.0, sourceRect.getMaxY(), sourceRect.getMaxX() + flowOffset, sourceRect.getMaxY());
                    connect.closePath();
                    Color nc = lookupNodeColor(nodeKey);
                    if (hasFlowSelections) {
                        if (!Boolean.TRUE.equals(dataset.getFlowProperty(flowKey, FlowKey.SELECTED_PROPERTY_KEY))) {
                            int g = (ncol.getRed() + ncol.getGreen() + ncol.getBlue()) / 3;
                            nc = new Color(g, g, g, ncol.getAlpha());
                        }
                    }
                    
                    GradientPaint gp = new GradientPaint((float) sourceRect.getMaxX(), 0, nc, (float) destRect.getMinX(), 0, new Color(nc.getRed(), nc.getGreen(), nc.getBlue(), 128));
                    Composite saved = g2.getComposite();
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.75f));
                    g2.setPaint(gp);
                    g2.fill(connect);
                    if (entities != null) {
                        String toolTip = null;
                        if (this.toolTipGenerator != null) {
                            toolTip = this.toolTipGenerator.generateLabel(this.dataset, flowKey);
                        }
                        entities.add(new FlowEntity(flowKey, connect, toolTip, ""));                
                    }
                    g2.setComposite(saved);
                }
                
            }
        }
        
        // now draw the destination nodes
        int lastStage = this.dataset.getStageCount() - 1;
        for (Object d : this.dataset.getDestinations(lastStage)) {
            Comparable destination = (Comparable) d;
            NodeKey nodeKey = new NodeKey<>(lastStage + 1, destination);
            Rectangle2D nodeRect = nodeRects.get(nodeKey);
            if (nodeRect != null) {
                Color ncol = lookupNodeColor(nodeKey);
                if (hasNodeSelections) {
                    if (!Boolean.TRUE.equals(dataset.getNodeProperty(nodeKey, NodeKey.SELECTED_PROPERTY_KEY))) {
                        int g = (ncol.getRed() + ncol.getGreen() + ncol.getBlue()) / 3;
                        ncol = new Color(g, g, g, ncol.getAlpha());
                    }
                }
                g2.setPaint(ncol);
                g2.fill(nodeRect);
                if (entities != null) {
                    entities.add(new NodeEntity(new NodeKey<>(lastStage + 1, destination), nodeRect, destination.toString()));                
                }
            }
        }
        
        // now draw all the labels over top of everything else
        g2.setFont(this.defaultNodeLabelFont);
        g2.setPaint(this.defaultNodeLabelPaint);
        for (NodeKey key : nodeRects.keySet()) {
            Rectangle2D r = nodeRects.get(key);
            if (key.getStage() < this.dataset.getStageCount()) {
                TextUtils.drawAlignedString(key.getNode().toString(), g2, 
                        (float) (r.getMaxX() + flowOffset + this.nodeLabelOffsetX), 
                        (float) labelY(r), TextAnchor.CENTER_LEFT);                
            } else {
                TextUtils.drawAlignedString(key.getNode().toString(), g2, 
                        (float) (r.getX() - flowOffset - this.nodeLabelOffsetX), 
                        (float) labelY(r), TextAnchor.CENTER_RIGHT);                
            }
        }
    }
    
    
    protected Color lookupNodeColor(NodeKey nodeKey) {
        Color result = this.nodeColorMap.get(nodeKey);
        if (result == null) {
            // if the color swatch is non-empty, we use it to autopopulate 
            // the node colors...
            if (!this.nodeColorSwatch.isEmpty()) {
                // look through previous stages to see if this source key is already seen
                for (int s = 0; s < nodeKey.getStage(); s++) {
                    for (Object key : dataset.getSources(s)) {
                        if (nodeKey.getNode().equals(key)) {
                            Color color = this.nodeColorMap.get(new NodeKey<>(s, (Comparable) key));
                            setNodeFillColor(nodeKey, color);
                            return color;
                        }
                    }
                }

                result = this.nodeColorSwatch.get(Math.min(this.nodeColorSwatchPointer, this.nodeColorSwatch.size() - 1));
                this.nodeColorSwatchPointer++;
                if (this.nodeColorSwatchPointer > this.nodeColorSwatch.size() - 1) { 
                    this.nodeColorSwatchPointer = 0;
                }
                setNodeFillColor(nodeKey, result);
                return result;
            } else {
                result = this.defaultNodeColor;
            }
        }
        return result;
    }

    
    private double labelY(Rectangle2D r) {
        if (this.nodeLabelAlignment == VerticalAlignment.TOP) {
            return r.getY() + this.nodeLabelOffsetY;
        } else if (this.nodeLabelAlignment == VerticalAlignment.BOTTOM) {
            return r.getMaxY() - this.nodeLabelOffsetY;
        } else {
            return r.getCenterY();
        }
    }
    
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FlowPlot)) {
            return false;
        }
        FlowPlot that = (FlowPlot) obj;
        if (!this.defaultNodeColor.equals(that.defaultNodeColor)) {
            return false;
        }
        if (!this.nodeColorMap.equals(that.nodeColorMap)) {
            return false;
        }
        if (!this.nodeColorSwatch.equals(that.nodeColorSwatch)) {
            return false;
        }
        if (!this.defaultNodeLabelFont.equals(that.defaultNodeLabelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.defaultNodeLabelPaint, that.defaultNodeLabelPaint)) {
            return false;
        }
        if (this.flowMargin != that.flowMargin) {
            return false;
        }
        if (this.nodeMargin != that.nodeMargin) {
            return false;
        }
        if (this.nodeWidth != that.nodeWidth) {
            return false;
        }
        if (this.nodeLabelOffsetX != that.nodeLabelOffsetX) {
            return false;
        }
        if (this.nodeLabelOffsetY != that.nodeLabelOffsetY) {
            return false;
        }
        if (this.nodeLabelAlignment != that.nodeLabelAlignment) {
            return false;
        }
        if (!Objects.equals(this.toolTipGenerator, that.toolTipGenerator)) {
            return false;
        }
        return super.equals(obj);
    }

    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.nodeWidth) ^ (Double.doubleToLongBits(this.nodeWidth) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.nodeMargin) ^ (Double.doubleToLongBits(this.nodeMargin) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.flowMargin) ^ (Double.doubleToLongBits(this.flowMargin) >>> 32));
        hash = 83 * hash + Objects.hashCode(this.nodeColorMap);
        hash = 83 * hash + Objects.hashCode(this.nodeColorSwatch);
        hash = 83 * hash + Objects.hashCode(this.defaultNodeColor);
        hash = 83 * hash + Objects.hashCode(this.defaultNodeLabelFont);
        hash = 83 * hash + Objects.hashCode(this.defaultNodeLabelPaint);
        hash = 83 * hash + Objects.hashCode(this.nodeLabelAlignment);
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.nodeLabelOffsetX) ^ (Double.doubleToLongBits(this.nodeLabelOffsetX) >>> 32));
        hash = 83 * hash + (int) (Double.doubleToLongBits(this.nodeLabelOffsetY) ^ (Double.doubleToLongBits(this.nodeLabelOffsetY) >>> 32));
        hash = 83 * hash + Objects.hashCode(this.toolTipGenerator);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        FlowPlot clone = (FlowPlot) super.clone();
        clone.nodeColorMap = new HashMap<>(this.nodeColorMap);
        return clone;
    }

}

