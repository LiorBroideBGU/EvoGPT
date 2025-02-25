

package org.jfree.chart.entity;

import java.awt.Shape;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Objects;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.internal.HashUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.SerialUtils;


public class JFreeChartEntity extends ChartEntity {

    
    private static final long serialVersionUID = -4445994133561919083L;
            //same as for ChartEntity!

    
    private final JFreeChart chart;

    
    public JFreeChartEntity(Shape area, JFreeChart chart) {
        // defer argument checks...
        this(area, chart, null);
    }

    
    public JFreeChartEntity(Shape area, JFreeChart chart, String toolTipText) {
        // defer argument checks...
        this(area, chart, toolTipText, null);
    }

    
    public JFreeChartEntity(Shape area, JFreeChart chart, String toolTipText,
            String urlText) {
        super(area, toolTipText, urlText);
        Args.nullNotPermitted(chart, "chart");
        this.chart = chart;
    }

    
    public JFreeChart getChart() {
        return this.chart;
    }

    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("JFreeChartEntity: ");
        sb.append("tooltip = ");
        sb.append(getToolTipText());
        return sb.toString();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof JFreeChartEntity)) {
            return false;
        }
        JFreeChartEntity that = (JFreeChartEntity) obj;
        if (!getArea().equals(that.getArea())) {
            return false;
        }
        if (!Objects.equals(getToolTipText(), that.getToolTipText())) {
            return false;
        }
        if (!Objects.equals(getURLText(), that.getURLText())) {
            return false;
        }
        if (!(this.chart.equals(that.chart))) {
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
