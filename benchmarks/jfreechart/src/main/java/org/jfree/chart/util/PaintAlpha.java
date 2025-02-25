

package org.jfree.chart.util;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.TexturePaint;
import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;


public class PaintAlpha {
    // TODO Revert to SVN revision 2469 in JFreeChart 1.0.16
    //      (MultipleGradientPaint's / JDK issues)
    // TODO THEN: change visibility of ALL darker(...) Methods EXCEPT
    //      darker(Paint) to private!

    
    private static final double FACTOR = 0.7;

    private static boolean legacyAlpha = false;

    
    public static boolean setLegacyAlpha(boolean legacyAlpha) {
        boolean old = PaintAlpha.legacyAlpha;
        PaintAlpha.legacyAlpha = legacyAlpha;
        return old;
    }

    
    public static Paint darker(Paint paint) {

        if (paint instanceof Color) {
            return darker((Color) paint);
        }
        if (legacyAlpha == true) {
            
            return paint;
        }
        if (paint instanceof GradientPaint) {
            return darker((GradientPaint) paint);
        }
        if (paint instanceof LinearGradientPaint) {
            return darkerLinearGradientPaint((LinearGradientPaint) paint);
        }
        if (paint instanceof RadialGradientPaint) {
            return darkerRadialGradientPaint((RadialGradientPaint) paint);
        }
        if (paint instanceof TexturePaint) {
            try {
                return darkerTexturePaint((TexturePaint) paint);
            }
            catch (Exception e) {
                
                return paint;
            }
        }
        return paint;
    }

    
    private static Color darker(Color paint) {
        return new Color(
                (int)(paint.getRed  () * FACTOR),
                (int)(paint.getGreen() * FACTOR),
                (int)(paint.getBlue () * FACTOR), paint.getAlpha());
    }

    
    private static GradientPaint darker(GradientPaint paint) {
        return new GradientPaint(
                paint.getPoint1(), darker(paint.getColor1()),
                paint.getPoint2(), darker(paint.getColor2()),
                paint.isCyclic());
    }

    
    private static Paint darkerLinearGradientPaint(LinearGradientPaint paint) {
        final Color[] paintColors = paint.getColors();
        for (int i = 0; i < paintColors.length; i++) {
            paintColors[i] = darker(paintColors[i]);
        }
        return new LinearGradientPaint(paint.getStartPoint(),
                paint.getEndPoint(), paint.getFractions(), paintColors,
                paint.getCycleMethod(), paint.getColorSpace(), 
                paint.getTransform());
    }

    
    private static Paint darkerRadialGradientPaint(RadialGradientPaint paint) {
        final Color[] paintColors = paint.getColors();
        for (int i = 0; i < paintColors.length; i++) {
            paintColors[i] = darker(paintColors[i]);
        }
        return new RadialGradientPaint(paint.getCenterPoint(), 
                paint.getRadius(), paint.getFocusPoint(), 
                paint.getFractions(), paintColors, paint.getCycleMethod(),
                paint.getColorSpace(), paint.getTransform());
    }

    
    private static TexturePaint darkerTexturePaint(TexturePaint paint) {
        
        if (paint.getImage().getColorModel().isAlphaPremultiplied()) {
            
        }

        BufferedImage img = cloneImage(paint.getImage());

        WritableRaster ras = img.copyData(null);

        final int miX = ras.getMinX();
        final int miY = ras.getMinY();
        final int maY = ras.getMinY() + ras.getHeight();

        final int   wid = ras.getWidth();

        /**/  int[] pix = new int[wid * img.getSampleModel().getNumBands()];
        

        
        if (img.getColorModel() instanceof IndexColorModel) {

            int[] nco = new int[4]; // RGB (+ optional Alpha which we leave
                                    // unchanged)

            for (int y = miY; y < maY; y++)  {

                pix = ras.getPixels(miX, y, wid, 1, pix);

                for (int p = 0; p < pix.length; p++) {
                    nco    =  img.getColorModel().getComponents(pix[p], nco, 0);
                    nco[0] *= FACTOR; // Red
                    nco[1] *= FACTOR; // Green
                    nco[2] *= FACTOR; // Blue. Now map computed colour to
                                      // nearest in Palette...
                    pix[p] = img.getColorModel().getDataElement(nco, 0);
                }
                /**/ ras.setPixels(miX, y, wid, 1, pix);
            }
            img.setData(ras);

            return new TexturePaint(img, paint.getAnchorRect());
        }

        
        if (img.getSampleModel().getNumBands() == 4) {
            
            for (int y = miY; y < maY; y++)  {

                pix = ras.getPixels(miX, y, wid, 1, pix);

                for (int p = 0; p < pix.length;) {
                    pix[p] = (int)(pix[p++] * FACTOR); // Red
                    pix[p] = (int)(pix[p++] * FACTOR); // Green
                    pix[p] = (int)(pix[p++] * FACTOR); // Blue
                    p++;
                }
                /**/  ras.setPixels(miX, y, wid, 1, pix);
            }
            img.setData(ras);
            return new TexturePaint(img, paint.getAnchorRect());
        } else {
            for (int y = miY; y < maY; y++)  {

                pix = ras.getPixels(miX, y, wid, 1, pix);

                for (int p = 0; p < pix.length; p++) {
                    pix[p] = (int)(pix[p] * FACTOR);
                }
                /**/  ras.setPixels(miX, y, wid, 1, pix);
            }
            img.setData(ras);
            return new TexturePaint(img, paint.getAnchorRect());
            
        }
    }

    
    public static BufferedImage cloneImage(BufferedImage image) {

        WritableRaster rin = image.getRaster();
        WritableRaster ras = rin.createCompatibleWritableRaster();
        /**/ ras.setRect(rin); // <- this is the code that actually COPIES the pixels

        
        Hashtable props = null;
        String[] propNames = image.getPropertyNames();
        if (propNames != null) { // ALWAYS null
            props = new Hashtable();
            for (int i = 0; i < propNames.length; i++) {
                props.put(propNames[i], image.getProperty(propNames[i]));
            }
        }
        return new BufferedImage(image.getColorModel(), ras,
                image.isAlphaPremultiplied(), props);
    }
}
