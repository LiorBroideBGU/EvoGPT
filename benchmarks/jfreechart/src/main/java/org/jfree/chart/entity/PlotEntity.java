

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import org.jfree.chart.plot.Plot;
import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;


public class PlotEntity extends ChartEntity {

    
    private static final long serialVersionUID = -4445994133561919083L;
            //same as for ChartEntity!

    
    private final Plot plot;

    
    public PlotEntity(Shape area, Plot plot) {
        // defer argument checks...
        this(area, plot, null);
    }

    
    public PlotEntity(Shape area, Plot plot, String toolTipText) {
        // defer argument checks...
        this(area, plot, toolTipText, null);
    }

    
    public PlotEntity(Shape area, Plot plot, String toolTipText,
            String urlText) {
        super(area, toolTipText, urlText);
        Args.nullNotPermitted(plot, "plot");
        this.plot = plot;
    }

    
    public Plot getPlot() {
        return this.plot;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("PlotEntity: ");
        sb.append("tooltip = ");
        sb.append(getToolTipText());
        return sb.toString();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PlotEntity)) {
            return false;
        }
        PlotEntity that = (PlotEntity) obj;
        if (!getArea().equals(that.getArea())) {
            return false;
        }
        if (!Objects.equals(getToolTipText(), that.getToolTipText())) {
            return false;
        }
        if (!Objects.equals(getURLText(), that.getURLText())) {
            return false;
        }
        if (!(this.plot.equals(that.plot))) {
            return false;
        }
        return true;
    }

    
    @Override
    public int hashCode() {
        int result = 39;
        result = HashUtils.hashCode(result, getToolTipText());
        result = HashUtils.hashCode(result, getURLText());
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeShape(getArea(), stream);
     }

    
    private void readObject(ObjectInputStream stream)
            throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        setArea(SerialUtils.readShape(stream));
    }

}
