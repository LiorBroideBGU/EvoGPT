

package org.jfree.chart.plot;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.ChartRenderingInfo;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.SerialUtils;


public class PlotRenderingInfo implements Cloneable, Serializable {

    
    private static final long serialVersionUID = 8446720134379617220L;

    
    private final ChartRenderingInfo owner;

    
    private transient Rectangle2D plotArea;

    
    private transient Rectangle2D dataArea;

    
    private List<PlotRenderingInfo> subplotInfo;

    
    public PlotRenderingInfo(ChartRenderingInfo owner) {
        this.owner = owner;
        this.dataArea = new Rectangle2D.Double();
        this.subplotInfo = new ArrayList<>();
    }

    
    public ChartRenderingInfo getOwner() {
        return this.owner;
    }

    
    public Rectangle2D getPlotArea() {
        return this.plotArea;
    }

    
    public void setPlotArea(Rectangle2D area) {
        this.plotArea = area;
    }

    
    public Rectangle2D getDataArea() {
        return this.dataArea;
    }

    
    public void setDataArea(Rectangle2D area) {
        this.dataArea = area;
    }

    
    public int getSubplotCount() {
        return this.subplotInfo.size();
    }

    
    public void addSubplotInfo(PlotRenderingInfo info) {
        this.subplotInfo.add(info);
    }

    
    public PlotRenderingInfo getSubplotInfo(int index) {
        return this.subplotInfo.get(index);
    }

    
    public int getSubplotIndex(Point2D source) {
        Args.nullNotPermitted(source, "source");
        int subplotCount = getSubplotCount();
        for (int i = 0; i < subplotCount; i++) {
            PlotRenderingInfo info = getSubplotInfo(i);
            Rectangle2D area = info.getDataArea();
            if (area.contains(source)) {
                return i;
            }
        }
        return -1;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof PlotRenderingInfo)) {
            return false;
        }
        PlotRenderingInfo that = (PlotRenderingInfo) obj;
        if (!Objects.equals(this.dataArea, that.dataArea)) {
            return false;
        }
        if (!Objects.equals(this.plotArea, that.plotArea)) {
            return false;
        }
        if (!Objects.equals(this.subplotInfo, that.subplotInfo)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 53 * hash + Objects.hashCode(this.plotArea);
        hash = 53 * hash + Objects.hashCode(this.dataArea);
        hash = 53 * hash + Objects.hashCode(this.subplotInfo);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        PlotRenderingInfo clone = (PlotRenderingInfo) super.clone();
        if (this.plotArea != null) {
            clone.plotArea = (Rectangle2D) this.plotArea.clone();
        }
        if (this.dataArea != null) {
            clone.dataArea = (Rectangle2D) this.dataArea.clone();
        }
        clone.subplotInfo = new ArrayList<>(this.subplotInfo.size());
        for (int i = 0; i < this.subplotInfo.size(); i++) {
            clone.subplotInfo.add(CloneUtils.clone(this.subplotInfo.get(i)));
        }
        return clone;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeShape(this.dataArea, stream);
        SerialUtils.writeShape(this.plotArea, stream);
    }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.dataArea = (Rectangle2D) SerialUtils.readShape(stream);
        this.plotArea = (Rectangle2D) SerialUtils.readShape(stream);
    }

}
