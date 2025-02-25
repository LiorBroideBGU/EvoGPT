

package org.jfree.chart.renderer.xy;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

import org.jfree.chart.axis.CyclicNumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.urls.XYURLGenerator;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.xy.XYDataset;


public class CyclicXYItemRenderer extends StandardXYItemRenderer
                                  implements Serializable {

    
    private static final long serialVersionUID = 4035912243303764892L;

    
    public CyclicXYItemRenderer() {
        super();
    }

    
    public CyclicXYItemRenderer(int type) {
        super(type);
    }

    
    public CyclicXYItemRenderer(int type, XYToolTipGenerator labelGenerator) {
        super(type, labelGenerator);
    }

    
    public CyclicXYItemRenderer(int type,
                                XYToolTipGenerator labelGenerator,
                                XYURLGenerator urlGenerator) {
        super(type, labelGenerator, urlGenerator);
    }


    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state, 
            Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
            ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
            int series, int item, CrosshairState crosshairState, int pass) {

        if ((!getPlotLines()) || ((!(domainAxis instanceof CyclicNumberAxis))
                && (!(rangeAxis instanceof CyclicNumberAxis))) || (item <= 0)) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, dataset, series, item, crosshairState, pass);
            return;
        }

        // get the previous data point...
        double xn = dataset.getXValue(series, item - 1);
        double yn = dataset.getYValue(series, item - 1);
        // If null, don't draw line => then delegate to parent
        if (Double.isNaN(yn)) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, dataset, series, item, crosshairState, pass);
            return;
        }
        double[] x = new double[2];
        double[] y = new double[2];
        x[0] = xn;
        y[0] = yn;

        // get the data point...
        xn = dataset.getXValue(series, item);
        yn = dataset.getYValue(series, item);
        // If null, don't draw line at all
        if (Double.isNaN(yn)) {
            return;
        }
        x[1] = xn;
        y[1] = yn;

        // Now split the segment as needed
        double xcycleBound = Double.NaN;
        double ycycleBound = Double.NaN;
        boolean xBoundMapping = false, yBoundMapping = false;
        CyclicNumberAxis cnax = null, cnay = null;

        if (domainAxis instanceof CyclicNumberAxis) {
            cnax = (CyclicNumberAxis) domainAxis;
            xcycleBound = cnax.getCycleBound();
            xBoundMapping = cnax.isBoundMappedToLastCycle();
            // If the segment must be splitted, insert a new point
            // Strict test forces to have real segments (not 2 equal points)
            // and avoids division by 0
            if ((x[0] != x[1])
                    && ((xcycleBound >= x[0])
                    && (xcycleBound <= x[1])
                    || (xcycleBound >= x[1])
                    && (xcycleBound <= x[0]))) {
                double[] nx = new double[3];
                double[] ny = new double[3];
                nx[0] = x[0]; nx[2] = x[1]; ny[0] = y[0]; ny[2] = y[1];
                nx[1] = xcycleBound;
                ny[1] = (y[1] - y[0]) * (xcycleBound - x[0])
                        / (x[1] - x[0]) + y[0];
                x = nx; y = ny;
            }
        }

        if (rangeAxis instanceof CyclicNumberAxis) {
            cnay = (CyclicNumberAxis) rangeAxis;
            ycycleBound = cnay.getCycleBound();
            yBoundMapping = cnay.isBoundMappedToLastCycle();
            // The split may occur in either x splitted segments, if any, but
            // not in both
            if ((y[0] != y[1]) && ((ycycleBound >= y[0])
                    && (ycycleBound <= y[1])
                    || (ycycleBound >= y[1]) && (ycycleBound <= y[0]))) {
                double[] nx = new double[x.length + 1];
                double[] ny = new double[y.length + 1];
                nx[0] = x[0]; nx[2] = x[1]; ny[0] = y[0]; ny[2] = y[1];
                ny[1] = ycycleBound;
                nx[1] = (x[1] - x[0]) * (ycycleBound - y[0])
                        / (y[1] - y[0]) + x[0];
                if (x.length == 3) {
                    nx[3] = x[2]; ny[3] = y[2];
                }
                x = nx; y = ny;
            }
            else if ((x.length == 3) && (y[1] != y[2]) && ((ycycleBound >= y[1])
                    && (ycycleBound <= y[2])
                    || (ycycleBound >= y[2]) && (ycycleBound <= y[1]))) {
                double[] nx = new double[4];
                double[] ny = new double[4];
                nx[0] = x[0]; nx[1] = x[1]; nx[3] = x[2];
                ny[0] = y[0]; ny[1] = y[1]; ny[3] = y[2];
                ny[2] = ycycleBound;
                nx[2] = (x[2] - x[1]) * (ycycleBound - y[1])
                        / (y[2] - y[1]) + x[1];
                x = nx; y = ny;
            }
        }

        // If the line is not wrapping, then parent is OK
        if (x.length == 2) {
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, dataset, series, item, crosshairState, pass);
            return;
        }

        OverwriteDataSet newset = new OverwriteDataSet(x, y, dataset);

        if (cnax != null) {
            if (xcycleBound == x[0]) {
                cnax.setBoundMappedToLastCycle(x[1] <= xcycleBound);
            }
            if (xcycleBound == x[1]) {
                cnax.setBoundMappedToLastCycle(x[0] <= xcycleBound);
            }
        }
        if (cnay != null) {
            if (ycycleBound == y[0]) {
                cnay.setBoundMappedToLastCycle(y[1] <= ycycleBound);
            }
            if (ycycleBound == y[1]) {
                cnay.setBoundMappedToLastCycle(y[0] <= ycycleBound);
            }
        }
        super.drawItem(
            g2, state, dataArea, info, plot, domainAxis, rangeAxis,
            newset, series, 1, crosshairState, pass
        );

        if (cnax != null) {
            if (xcycleBound == x[1]) {
                cnax.setBoundMappedToLastCycle(x[2] <= xcycleBound);
            }
            if (xcycleBound == x[2]) {
                cnax.setBoundMappedToLastCycle(x[1] <= xcycleBound);
            }
        }
        if (cnay != null) {
            if (ycycleBound == y[1]) {
                cnay.setBoundMappedToLastCycle(y[2] <= ycycleBound);
            }
            if (ycycleBound == y[2]) {
                cnay.setBoundMappedToLastCycle(y[1] <= ycycleBound);
            }
        }
        super.drawItem(g2, state, dataArea, info, plot, domainAxis, rangeAxis,
                newset, series, 2, crosshairState, pass);

        if (x.length == 4) {
            if (cnax != null) {
                if (xcycleBound == x[2]) {
                    cnax.setBoundMappedToLastCycle(x[3] <= xcycleBound);
                }
                if (xcycleBound == x[3]) {
                    cnax.setBoundMappedToLastCycle(x[2] <= xcycleBound);
                }
            }
            if (cnay != null) {
                if (ycycleBound == y[2]) {
                    cnay.setBoundMappedToLastCycle(y[3] <= ycycleBound);
                }
                if (ycycleBound == y[3]) {
                    cnay.setBoundMappedToLastCycle(y[2] <= ycycleBound);
                }
            }
            super.drawItem(g2, state, dataArea, info, plot, domainAxis,
                    rangeAxis, newset, series, 3, crosshairState, pass);
        }

        if (cnax != null) {
            cnax.setBoundMappedToLastCycle(xBoundMapping);
        }
        if (cnay != null) {
            cnay.setBoundMappedToLastCycle(yBoundMapping);
        }
    }

    
    protected static class OverwriteDataSet implements XYDataset {

        
        protected XYDataset delegateSet;

        
        Double[] x, y;

        
        public OverwriteDataSet(double[] x, double[] y, XYDataset delegateSet) {
            this.delegateSet = delegateSet;
            this.x = new Double[x.length]; this.y = new Double[y.length];
            for (int i = 0; i < x.length; ++i) {
                this.x[i] = x[i];
                this.y[i] = y[i];
            }
        }

        
        @Override
        public DomainOrder getDomainOrder() {
            return DomainOrder.NONE;
        }

        
        @Override
        public int getItemCount(int series) {
            return this.x.length;
        }

        
        @Override
        public Number getX(int series, int item) {
            return this.x[item];
        }

        
        @Override
        public double getXValue(int series, int item) {
            double result = Double.NaN;
            Number xx = getX(series, item);
            if (xx != null) {
                result = xx.doubleValue();
            }
            return result;
        }

        
        @Override
        public Number getY(int series, int item) {
            return this.y[item];
        }

        
        @Override
        public double getYValue(int series, int item) {
            double result = Double.NaN;
            Number yy = getY(series, item);
            if (yy != null) {
                result = yy.doubleValue();
            }
            return result;
        }

        
        @Override
        public int getSeriesCount() {
            return this.delegateSet.getSeriesCount();
        }

        
        @Override
        public Comparable getSeriesKey(int series) {
            return this.delegateSet.getSeriesKey(series);
        }

        
        @Override
        public int indexOf(Comparable seriesName) {
            return this.delegateSet.indexOf(seriesName);
        }

        
        @Override
        public void addChangeListener(DatasetChangeListener listener) {
            // unused in parent
        }

        
        @Override
        public void removeChangeListener(DatasetChangeListener listener) {
            // unused in parent
        }

    }

}


