

package org.jfree.chart.legend;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Objects;
import org.jfree.chart.text.AttributedStringUtils;
import org.jfree.chart.util.GradientPaintTransformer;
import org.jfree.chart.util.StandardGradientPaintTransformer;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.PaintUtils;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.SerialUtils;
import org.jfree.chart.internal.ShapeUtils;

import org.jfree.data.general.Dataset;


public class LegendItem implements Cloneable, Serializable {

    
    private static final long serialVersionUID = -797214582948827144L;

    
    private Dataset dataset;

    
    private Comparable seriesKey;

    
    private int datasetIndex;

    
    private int series;

    
    private final String label;

    
    private Font labelFont;

    
    private transient Paint labelPaint;

    
    private transient AttributedString attributedLabel;

    
    private String description;

    
    private String toolTipText;

    
    private String urlText;

    
    private boolean shapeVisible;

    
    private transient Shape shape;

    
    private final boolean shapeFilled;

    
    private transient Paint fillPaint;

    
    private GradientPaintTransformer fillPaintTransformer;

    
    private final boolean shapeOutlineVisible;

    
    private transient Paint outlinePaint;

    
    private transient Stroke outlineStroke;

    
    private boolean lineVisible;

    
    private transient Shape line;

    
    private transient Stroke lineStroke;

    
    private transient Paint linePaint;

    
    private static final Shape UNUSED_SHAPE = new Line2D.Float();

    
    private static final Stroke UNUSED_STROKE = new BasicStroke(0.0f);

    
    public LegendItem(String label) {
        this(label, Color.BLACK);
    }

    
    public LegendItem(String label, Paint paint) {
        this(label, null, null, null, new Rectangle2D.Double(-4.0, -4.0, 8.0,
                8.0), paint);
    }

    
    public LegendItem(String label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint) {

        this(label, description, toolTipText, urlText,
                 true, shape,
                 true, fillPaint,
                 false, Color.BLACK, UNUSED_STROKE,
                 false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.BLACK);

    }

    
    public LegendItem(String label, String description, String toolTipText, 
            String urlText, Shape shape, Paint fillPaint, Stroke outlineStroke, 
            Paint outlinePaint) {

        this(label, description, toolTipText, urlText,
                 true, shape,
                 true, fillPaint,
                 true, outlinePaint, outlineStroke,
                 false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.BLACK);

    }

    
    public LegendItem(String label, String description, String toolTipText, 
            String urlText, Shape line, Stroke lineStroke, Paint linePaint) {

        this(label, description, toolTipText, urlText,
                 false, UNUSED_SHAPE,
                 false, Color.BLACK,
                 false, Color.BLACK, UNUSED_STROKE,
                 true, line, lineStroke, linePaint);
    }

    
    public LegendItem(String label, String description,
                      String toolTipText, String urlText,
                      boolean shapeVisible, Shape shape,
                      boolean shapeFilled, Paint fillPaint,
                      boolean shapeOutlineVisible, Paint outlinePaint,
                      Stroke outlineStroke,
                      boolean lineVisible, Shape line,
                      Stroke lineStroke, Paint linePaint) {

        Args.nullNotPermitted(label, "label");
        Args.nullNotPermitted(fillPaint, "fillPaint");
        Args.nullNotPermitted(lineStroke, "lineStroke");
        Args.nullNotPermitted(outlinePaint, "outlinePaint");
        Args.nullNotPermitted(outlineStroke, "outlineStroke");
        this.label = label;
        this.labelPaint = null;
        this.attributedLabel = null;
        this.description = description;
        this.shapeVisible = shapeVisible;
        this.shape = shape;
        this.shapeFilled = shapeFilled;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = new StandardGradientPaintTransformer();
        this.shapeOutlineVisible = shapeOutlineVisible;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.lineVisible = lineVisible;
        this.line = line;
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }

    
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint) {

        this(label, description, toolTipText, urlText,
                 true, shape,
                 true, fillPaint,
                 false, Color.BLACK, UNUSED_STROKE,
                 false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.BLACK);

    }

    
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      Shape shape, Paint fillPaint,
                      Stroke outlineStroke, Paint outlinePaint) {

        this(label, description, toolTipText, urlText,
                 true, shape,
                 true, fillPaint,
                 true, outlinePaint, outlineStroke,
                 false, UNUSED_SHAPE, UNUSED_STROKE,
                Color.BLACK);
    }

    
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      Shape line, Stroke lineStroke, Paint linePaint) {

        this(label, description, toolTipText, urlText,
                 false, UNUSED_SHAPE,
                 false, Color.BLACK,
                 false, Color.BLACK, UNUSED_STROKE,
                 true, line, lineStroke, linePaint);
    }

    
    public LegendItem(AttributedString label, String description,
                      String toolTipText, String urlText,
                      boolean shapeVisible, Shape shape,
                      boolean shapeFilled, Paint fillPaint,
                      boolean shapeOutlineVisible, Paint outlinePaint,
                      Stroke outlineStroke,
                      boolean lineVisible, Shape line, Stroke lineStroke,
                      Paint linePaint) {

        Args.nullNotPermitted(label, "label");
        Args.nullNotPermitted(fillPaint, "fillPaint");
        Args.nullNotPermitted(lineStroke, "lineStroke");
        Args.nullNotPermitted(line, "line");
        Args.nullNotPermitted(linePaint, "linePaint");
        Args.nullNotPermitted(outlinePaint, "outlinePaint");
        Args.nullNotPermitted(outlineStroke, "outlineStroke");
        this.label = characterIteratorToString(label.getIterator());
        this.attributedLabel = label;
        this.description = description;
        this.shapeVisible = shapeVisible;
        this.shape = shape;
        this.shapeFilled = shapeFilled;
        this.fillPaint = fillPaint;
        this.fillPaintTransformer = new StandardGradientPaintTransformer();
        this.shapeOutlineVisible = shapeOutlineVisible;
        this.outlinePaint = outlinePaint;
        this.outlineStroke = outlineStroke;
        this.lineVisible = lineVisible;
        this.line = line;
        this.lineStroke = lineStroke;
        this.linePaint = linePaint;
        this.toolTipText = toolTipText;
        this.urlText = urlText;
    }

    
    private String characterIteratorToString(CharacterIterator iterator) {
        int endIndex = iterator.getEndIndex();
        int beginIndex = iterator.getBeginIndex();
        int count = endIndex - beginIndex;
        if (count <= 0) {
            return "";
        }
        char[] chars = new char[count];
        int i = 0;
        char c = iterator.first();
        while (c != CharacterIterator.DONE) {
            chars[i] = c;
            i++;
            c = iterator.next();
        }
        return new String(chars);
    }

    
    public Dataset getDataset() {
        return this.dataset;
    }

    
    public void setDataset(Dataset dataset) {
        this.dataset = dataset;
    }

    
    public int getDatasetIndex() {
        return this.datasetIndex;
    }

    
    public void setDatasetIndex(int index) {
        this.datasetIndex = index;
    }

    
    public Comparable getSeriesKey() {
        return this.seriesKey;
    }

    
    public void setSeriesKey(Comparable key) {
        this.seriesKey = key;
    }

    
    public int getSeriesIndex() {
        return this.series;
    }

    
    public void setSeriesIndex(int index) {
        this.series = index;
    }

    
    public String getLabel() {
        return this.label;
    }

    
    public Font getLabelFont() {
        return this.labelFont;
    }

    
    public void setLabelFont(Font font) {
        this.labelFont = font;
    }

    
    public Paint getLabelPaint() {
        return this.labelPaint;
    }

    
    public void setLabelPaint(Paint paint) {
        this.labelPaint = paint;
    }

    
    public AttributedString getAttributedLabel() {
        return this.attributedLabel;
    }

    
    public String getDescription() {
        return this.description;
    }

    
    public void setDescription(String text) {
        this.description = text;
    }

    
    public String getToolTipText() {
        return this.toolTipText;
    }

    
    public void setToolTipText(String text) {
        this.toolTipText = text;
    }

    
    public String getURLText() {
        return this.urlText;
    }

    
    public void setURLText(String text) {
        this.urlText = text;
    }

    
    public boolean isShapeVisible() {
        return this.shapeVisible;
    }

    
    public void setShapeVisible(boolean visible) {
        this.shapeVisible = visible;
    }

    
    public Shape getShape() {
        return this.shape;
    }

    
    public void setShape(Shape shape) {
        Args.nullNotPermitted(shape, "shape");
        this.shape = shape;
    }

    
    public boolean isShapeFilled() {
        return this.shapeFilled;
    }

    
    public Paint getFillPaint() {
        return this.fillPaint;
    }

    
    public void setFillPaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.fillPaint = paint;
    }

    
    public boolean isShapeOutlineVisible() {
        return this.shapeOutlineVisible;
    }

    
    public Stroke getLineStroke() {
        return this.lineStroke;
    }
    
    
    public void setLineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.lineStroke = stroke;
    }

    
    public Paint getLinePaint() {
        return this.linePaint;
    }

    
    public void setLinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.linePaint = paint;
    }

    
    public Paint getOutlinePaint() {
        return this.outlinePaint;
    }

    
    public void setOutlinePaint(Paint paint) {
        Args.nullNotPermitted(paint, "paint");
        this.outlinePaint = paint;
    }

    
    public Stroke getOutlineStroke() {
        return this.outlineStroke;
    }

    
    public void setOutlineStroke(Stroke stroke) {
        Args.nullNotPermitted(stroke, "stroke");
        this.outlineStroke = stroke;
    }

    
    public boolean isLineVisible() {
        return this.lineVisible;
    }

    
    public void setLineVisible(boolean visible) {
        this.lineVisible = visible;
    }

    
    public Shape getLine() {
        return this.line;
    }

    
    public void setLine(Shape line) {
        Args.nullNotPermitted(line, "line");
        this.line = line;
    }

    
    public GradientPaintTransformer getFillPaintTransformer() {
        return this.fillPaintTransformer;
    }

    
    public void setFillPaintTransformer(GradientPaintTransformer transformer) {
        Args.nullNotPermitted(transformer, "transformer");
        this.fillPaintTransformer = transformer;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof LegendItem)) {
            return false;
        }
        LegendItem that = (LegendItem) obj;
        if (this.datasetIndex != that.datasetIndex) {
            return false;
        }
        if (this.series != that.series) {
            return false;
        }
        if (!this.label.equals(that.label)) {
            return false;
        }
        if (!AttributedStringUtils.equal(this.attributedLabel,
                that.attributedLabel)) {
            return false;
        }
        if (!Objects.equals(this.description, that.description)) {
            return false;
        }
        if (this.shapeVisible != that.shapeVisible) {
            return false;
        }
        if (!ShapeUtils.equal(this.shape, that.shape)) {
            return false;
        }
        if (this.shapeFilled != that.shapeFilled) {
            return false;
        }
        if (!PaintUtils.equal(this.fillPaint, that.fillPaint)) {
            return false;
        }
        if (!Objects.equals(this.fillPaintTransformer, that.fillPaintTransformer)) {
            return false;
        }
        if (this.shapeOutlineVisible != that.shapeOutlineVisible) {
            return false;
        }
        if (!this.outlineStroke.equals(that.outlineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.outlinePaint, that.outlinePaint)) {
            return false;
        }
        if (!this.lineVisible == that.lineVisible) {
            return false;
        }
        if (!ShapeUtils.equal(this.line, that.line)) {
            return false;
        }
        if (!this.lineStroke.equals(that.lineStroke)) {
            return false;
        }
        if (!PaintUtils.equal(this.linePaint, that.linePaint)) {
            return false;
        }
        if (!Objects.equals(this.labelFont, that.labelFont)) {
            return false;
        }
        if (!PaintUtils.equal(this.labelPaint, that.labelPaint)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + this.datasetIndex;
        hash = 79 * hash + this.series;
        hash = 79 * hash + Objects.hashCode(this.label);
        hash = 79 * hash + Objects.hashCode(this.labelFont);
        hash = 79 * hash + Objects.hashCode(this.labelPaint);
        hash = 79 * hash + Objects.hashCode(this.attributedLabel);
        hash = 79 * hash + Objects.hashCode(this.description);
        hash = 79 * hash + (this.shapeVisible ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.shape);
        hash = 79 * hash + (this.shapeFilled ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.fillPaint);
        hash = 79 * hash + Objects.hashCode(this.fillPaintTransformer);
        hash = 79 * hash + (this.shapeOutlineVisible ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.outlinePaint);
        hash = 79 * hash + Objects.hashCode(this.outlineStroke);
        hash = 79 * hash + (this.lineVisible ? 1 : 0);
        hash = 79 * hash + Objects.hashCode(this.line);
        hash = 79 * hash + Objects.hashCode(this.lineStroke);
        hash = 79 * hash + Objects.hashCode(this.linePaint);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        LegendItem clone = (LegendItem) super.clone();
        if (this.seriesKey instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.seriesKey;
            clone.seriesKey = (Comparable) pc.clone();
        }
        // FIXME: Clone the attributed string if it is not null
        clone.shape = CloneUtils.clone(this.shape);
        if (this.fillPaintTransformer instanceof PublicCloneable) {
            PublicCloneable pc = (PublicCloneable) this.fillPaintTransformer;
            clone.fillPaintTransformer = (GradientPaintTransformer) pc.clone();

        }
        clone.line = CloneUtils.clone(this.line);
        return clone;
    }

    
    private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.defaultWriteObject();
        SerialUtils.writeAttributedString(this.attributedLabel, stream);
        SerialUtils.writeShape(this.shape, stream);
        SerialUtils.writePaint(this.fillPaint, stream);
        SerialUtils.writeStroke(this.outlineStroke, stream);
        SerialUtils.writePaint(this.outlinePaint, stream);
        SerialUtils.writeShape(this.line, stream);
        SerialUtils.writeStroke(this.lineStroke, stream);
        SerialUtils.writePaint(this.linePaint, stream);
        SerialUtils.writePaint(this.labelPaint, stream);
    }

    
    private void readObject(ObjectInputStream stream)
        throws IOException, ClassNotFoundException {
        stream.defaultReadObject();
        this.attributedLabel = SerialUtils.readAttributedString(stream);
        this.shape = SerialUtils.readShape(stream);
        this.fillPaint = SerialUtils.readPaint(stream);
        this.outlineStroke = SerialUtils.readStroke(stream);
        this.outlinePaint = SerialUtils.readPaint(stream);
        this.line = SerialUtils.readShape(stream);
        this.lineStroke = SerialUtils.readStroke(stream);
        this.linePaint = SerialUtils.readPaint(stream);
        this.labelPaint = SerialUtils.readPaint(stream);
    }

}
