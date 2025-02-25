

package org.jfree.chart.renderer;

import java.awt.Color;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jfree.chart.legend.LegendItem;
import org.jfree.chart.legend.LegendItemCollection;
import org.jfree.chart.plot.DrawingSupplier;
import org.jfree.chart.plot.WaferMapPlot;
import org.jfree.data.general.WaferMapDataset;


public class WaferMapRenderer extends AbstractRenderer {

    
    private Map paintIndex;

    
    private WaferMapPlot plot;

    
    private int paintLimit;

    
    private static final int DEFAULT_PAINT_LIMIT = 35;

    
    public static final int POSITION_INDEX = 0;

    
    public static final int VALUE_INDEX = 1;

    
    private int paintIndexMethod;

    
    public WaferMapRenderer() {
        this(null, null);
    }

    
    public WaferMapRenderer(int paintLimit, int paintIndexMethod) {
        this(Integer.valueOf(paintLimit), Integer.valueOf(paintIndexMethod));
    }

    
    public WaferMapRenderer(Integer paintLimit, Integer paintIndexMethod) {

        super();
        this.paintIndex = new HashMap();

        if (paintLimit == null) {
            this.paintLimit = DEFAULT_PAINT_LIMIT;
        }
        else {
            this.paintLimit = paintLimit.intValue();
        }

        this.paintIndexMethod = VALUE_INDEX;
        if (paintIndexMethod != null) {
            if (isMethodValid(paintIndexMethod.intValue())) {
                this.paintIndexMethod = paintIndexMethod.intValue();
            }
        }
    }

    
    private boolean isMethodValid(int method) {
        switch (method) {
            case POSITION_INDEX: return true;
            case VALUE_INDEX:    return true;
            default: return false;
        }
    }

    
    @Override
    public DrawingSupplier getDrawingSupplier() {
        DrawingSupplier result = null;
        WaferMapPlot p = getPlot();
        if (p != null) {
            result = p.getDrawingSupplier();
        }
        return result;
    }

    
    public WaferMapPlot getPlot() {
        return this.plot;
    }

    
    public void setPlot(WaferMapPlot plot) {
        this.plot = plot;
        makePaintIndex();
    }

    
    public Paint getChipColor(Number value) {
        return getSeriesPaint(getPaintIndex(value));
    }

    
    private int getPaintIndex(Number value) {
        return ((Integer) this.paintIndex.get(value)).intValue();
    }

    
    private void makePaintIndex() {
        if (this.plot == null) {
            return;
        }
        WaferMapDataset data = this.plot.getDataset();
        Number dataMin = data.getMinValue();
        Number dataMax = data.getMaxValue();
        Set uniqueValues = data.getUniqueValues();
        if (uniqueValues.size() <= this.paintLimit) {
            int count = 0; // assign a color for each unique value
            for (Iterator i = uniqueValues.iterator(); i.hasNext();) {
                this.paintIndex.put(i.next(), count++);
            }
        }
        else {
            // more values than paints so map
            // multiple values to the same color
            switch (this.paintIndexMethod) {
                case POSITION_INDEX:
                    makePositionIndex(uniqueValues);
                    break;
                case VALUE_INDEX:
                    makeValueIndex(dataMax, dataMin, uniqueValues);
                    break;
                default:
                    break;
            }
        }
    }

    
    private void makePositionIndex(Set uniqueValues) {
        int valuesPerColor = (int) Math.ceil(
            (double) uniqueValues.size() / this.paintLimit
        );
        int count = 0; // assign a color for each unique value
        int paint = 0;
        for (Iterator i = uniqueValues.iterator(); i.hasNext();) {
            this.paintIndex.put(i.next(), paint);
            if (++count % valuesPerColor == 0) {
                paint++;
            }
            if (paint > this.paintLimit) {
                paint = this.paintLimit;
            }
        }
    }

    
    private void makeValueIndex(Number max, Number min, Set uniqueValues) {
        double valueRange = max.doubleValue() - min.doubleValue();
        double valueStep = valueRange / this.paintLimit;
        int paint = 0;
        double cutPoint = min.doubleValue() + valueStep;
        for (Iterator i = uniqueValues.iterator(); i.hasNext();) {
            Number value = (Number) i.next();
            while (value.doubleValue() > cutPoint) {
                cutPoint += valueStep;
                paint++;
                if (paint > this.paintLimit) {
                    paint = this.paintLimit;
                }
            }
            this.paintIndex.put(value, paint);
        }
    }

    
    public LegendItemCollection getLegendCollection() {
        LegendItemCollection result = new LegendItemCollection();
        if (this.paintIndex != null && this.paintIndex.size() > 0) {
            if (this.paintIndex.size() <= this.paintLimit) {
                for (Iterator i = this.paintIndex.entrySet().iterator();
                     i.hasNext();) {
                    // in this case, every color has a unique value
                    Map.Entry entry =  (Map.Entry) i.next();
                    String label = entry.getKey().toString();
                    String description = label;
                    Shape shape = new Rectangle2D.Double(1d, 1d, 1d, 1d);
                    Paint paint = lookupSeriesPaint(
                            ((Integer) entry.getValue()).intValue());
                    Paint outlinePaint = Color.BLACK;
                    Stroke outlineStroke = DEFAULT_STROKE;

                    result.add(new LegendItem(label, description, null,
                            null, shape, paint, outlineStroke, outlinePaint));

                }
            }
            else {
                // in this case, every color has a range of values
                Set unique = new HashSet();
                for (Iterator i = this.paintIndex.entrySet().iterator();
                     i.hasNext();) {
                    Map.Entry entry = (Map.Entry) i.next();
                    if (unique.add(entry.getValue())) {
                        String label = getMinPaintValue(
                            (Integer) entry.getValue()).toString()
                            + " - " + getMaxPaintValue(
                                (Integer) entry.getValue()).toString();
                        String description = label;
                        Shape shape = new Rectangle2D.Double(1d, 1d, 1d, 1d);
                        Paint paint = getSeriesPaint(
                            ((Integer) entry.getValue()).intValue()
                        );
                        Paint outlinePaint = Color.BLACK;
                        Stroke outlineStroke = DEFAULT_STROKE;

                        result.add(new LegendItem(label, description,
                                null, null, shape, paint, outlineStroke,
                                outlinePaint));
                    }
                } // end foreach map entry
            } // end else
        }
        return result;
    }

    
    private Number getMinPaintValue(Integer index) {
        double minValue = Double.POSITIVE_INFINITY;
        for (Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (((Integer) entry.getValue()).equals(index)) {
                if (((Number) entry.getKey()).doubleValue() < minValue) {
                    minValue = ((Number) entry.getKey()).doubleValue();
                }
            }
        }
        return minValue;
    }

    
    private Number getMaxPaintValue(Integer index) {
        double maxValue = Double.NEGATIVE_INFINITY;
        for (Iterator i = this.paintIndex.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (((Integer) entry.getValue()).equals(index)) {
                if (((Number) entry.getKey()).doubleValue() > maxValue) {
                    maxValue = ((Number) entry.getKey()).doubleValue();
                }
            }
        }
        return maxValue;
    }


} // end class wafermaprenderer
