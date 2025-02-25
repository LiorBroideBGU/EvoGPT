

package org.jfree.chart.renderer.xy;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CrosshairState;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.api.RectangleEdge;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;


public class DeviationStepRenderer extends DeviationRenderer {

    
    public DeviationStepRenderer() {
        super();
    }

    
    public DeviationStepRenderer(boolean lines, boolean shapes) {
        super(lines, shapes);
    }

    
    @Override
    public void drawItem(Graphics2D g2, XYItemRendererState state,
                         Rectangle2D dataArea, PlotRenderingInfo info, XYPlot plot,
                         ValueAxis domainAxis, ValueAxis rangeAxis, XYDataset dataset,
                         int series, int item, CrosshairState crosshairState, int pass) {

        // do nothing if item is not visible
        if (!getItemVisible(series, item)) {
            return;
        }

        // first pass draws the shading
        if (pass == 0) {
            IntervalXYDataset intervalDataset = (IntervalXYDataset) dataset;
            State drState = (State) state;

            double x = intervalDataset.getXValue(series, item);
            double yLow = intervalDataset.getStartYValue(series, item);
            double yHigh  = intervalDataset.getEndYValue(series, item);

            RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
            RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

            double xx = domainAxis.valueToJava2D(x, dataArea, xAxisLocation);
            double yyLow = rangeAxis.valueToJava2D(yLow, dataArea,
                    yAxisLocation);
            double yyHigh = rangeAxis.valueToJava2D(yHigh, dataArea,
                    yAxisLocation);


            PlotOrientation orientation = plot.getOrientation();
            if (item > 0 && !Double.isNaN(xx)) {
                double yLowPrev = intervalDataset.getStartYValue(series, item-1);
                double yHighPrev  = intervalDataset.getEndYValue(series, item-1);
                double yyLowPrev = rangeAxis.valueToJava2D(yLowPrev, dataArea,
                        yAxisLocation);
                double yyHighPrev = rangeAxis.valueToJava2D(yHighPrev, dataArea,
                        yAxisLocation);

                if(!Double.isNaN(yyLow) && !Double.isNaN(yyHigh)) {
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        drState.lowerCoordinates.add(new double[]{yyLowPrev, xx});
                        drState.upperCoordinates.add(new double[]{yyHighPrev, xx});
                    } else if (orientation == PlotOrientation.VERTICAL) {
                        drState.lowerCoordinates.add(new double[]{xx, yyLowPrev});
                        drState.upperCoordinates.add(new double[]{xx, yyHighPrev});
                    }
                }
            }

            boolean intervalGood = !Double.isNaN(xx) && !Double.isNaN(yLow) && !Double.isNaN(yHigh);
            if (intervalGood) {
                if (orientation == PlotOrientation.HORIZONTAL) {
                    drState.lowerCoordinates.add(new double[]{yyLow, xx});
                    drState.upperCoordinates.add(new double[]{yyHigh, xx});
                } else if (orientation == PlotOrientation.VERTICAL) {
                    drState.lowerCoordinates.add(new double[]{xx, yyLow});
                    drState.upperCoordinates.add(new double[]{xx, yyHigh});
                }
            }

            if (item == (dataset.getItemCount(series) - 1) ||
                (!intervalGood && drState.lowerCoordinates.size() > 1)) {
                // draw items so far, either we reached the end of the series or the next interval is invalid
                // last item in series, draw the lot...
                // set up the alpha-transparency...
                Composite originalComposite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, this.alpha));
                g2.setPaint(getItemFillPaint(series, item));
                GeneralPath area = new GeneralPath(GeneralPath.WIND_NON_ZERO,
                        drState.lowerCoordinates.size()
                                + drState.upperCoordinates.size());
                double[] coords = (double[]) drState.lowerCoordinates.get(0);
                area.moveTo((float) coords[0], (float) coords[1]);
                for (int i = 1; i < drState.lowerCoordinates.size(); i++) {
                    coords = (double[]) drState.lowerCoordinates.get(i);
                    area.lineTo((float) coords[0], (float) coords[1]);
                }
                int count = drState.upperCoordinates.size();
                coords = (double[]) drState.upperCoordinates.get(count - 1);
                area.lineTo((float) coords[0], (float) coords[1]);
                for (int i = count - 2; i >= 0; i--) {
                    coords = (double[]) drState.upperCoordinates.get(i);
                    area.lineTo((float) coords[0], (float) coords[1]);
                }
                area.closePath();
                g2.fill(area);
                g2.setComposite(originalComposite);

                drState.lowerCoordinates.clear();
                drState.upperCoordinates.clear();
            }
        }
        if (isLinePass(pass)) {

            // the following code handles the line for the y-values...it's
            // all done by code in the super class
            if (item == 0) {
                State s = (State) state;
                s.seriesPath.reset();
                s.setLastPointGood(false);
            }

            if (getItemLineVisible(series, item)) {
                drawPrimaryLineAsPath(state, g2, plot, dataset, pass,
                        series, item, domainAxis, rangeAxis, dataArea);
            }
        }

        // second pass adds shapes where the items are ..
        else if (isItemPass(pass)) {

            // setup for collecting optional entity info...
            EntityCollection entities = null;
            if (info != null) {
                entities = info.getOwner().getEntityCollection();
            }

            drawSecondaryPass(g2, plot, dataset, pass, series, item,
                    domainAxis, dataArea, rangeAxis, crosshairState, entities);
        }
    }

    
    protected void drawPrimaryLineAsPath(XYItemRendererState state,
                                         Graphics2D g2, XYPlot plot, XYDataset dataset, int pass,
                                         int series, int item, ValueAxis domainAxis, ValueAxis rangeAxis,
                                         Rectangle2D dataArea) {

        RectangleEdge xAxisLocation = plot.getDomainAxisEdge();
        RectangleEdge yAxisLocation = plot.getRangeAxisEdge();

        // get the data point...
        double x1 = dataset.getXValue(series, item);
        double y1 = dataset.getYValue(series, item);
        double transX1 = domainAxis.valueToJava2D(x1, dataArea, xAxisLocation);
        double transY1 = rangeAxis.valueToJava2D(y1, dataArea, yAxisLocation);

        XYLineAndShapeRenderer.State s = (XYLineAndShapeRenderer.State) state;
        // update path to reflect latest point
        if (!Double.isNaN(transX1) && !Double.isNaN(transY1)) {
            float x = (float) transX1;
            float y = (float) transY1;
            PlotOrientation orientation = plot.getOrientation();
            if (orientation == PlotOrientation.HORIZONTAL) {
                x = (float) transY1;
                y = (float) transX1;
            }
            if (s.isLastPointGood()) {
                if (item > 0) {
                    if (orientation == PlotOrientation.HORIZONTAL) {
                        s.seriesPath.lineTo(s.seriesPath.getCurrentPoint().getX(), y);
                    } else {
                        s.seriesPath.lineTo(x, s.seriesPath.getCurrentPoint().getY());
                    }
                }
                s.seriesPath.lineTo(x, y);
            }
            else {
                s.seriesPath.moveTo(x, y);
            }
            s.setLastPointGood(true);
        } else {
            s.setLastPointGood(false);
        }
        // if this is the last item, draw the path ...
        if (item == s.getLastItemIndex()) {
            // draw path
            drawFirstPassShape(g2, pass, series, item, s.seriesPath);
        }
    }


    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DeviationStepRenderer)) {
            return false;
        }
        DeviationStepRenderer that = (DeviationStepRenderer) obj;
        if (this.alpha != that.alpha) {
            return false;
        }
        return super.equals(obj);
    }

}
