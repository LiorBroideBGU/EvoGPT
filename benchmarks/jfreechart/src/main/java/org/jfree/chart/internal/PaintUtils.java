

package org.jfree.chart.internal;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.LinearGradientPaint;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;


public class PaintUtils {

    
    private PaintUtils() {
    }

    
    public static boolean equal(Paint p1, Paint p2) {
        if (p1 == p2) {
            return true;
        }
            
        // handle cases where either or both arguments are null
        if (p1 == null) {
            return (p2 == null);   
        }
        if (p2 == null) {
            return false;   
        }

        // handle GradientPaint as a special case...
        if (p1 instanceof GradientPaint && p2 instanceof GradientPaint) {
            GradientPaint gp1 = (GradientPaint) p1;
            GradientPaint gp2 = (GradientPaint) p2;
            return gp1.getColor1().equals(gp2.getColor1()) 
                    && gp1.getColor2().equals(gp2.getColor2())
                    && gp1.getPoint1().equals(gp2.getPoint1())    
                    && gp1.getPoint2().equals(gp2.getPoint2())
                    && gp1.isCyclic() == gp2.isCyclic()
                    && gp1.getTransparency() == gp1.getTransparency(); 
        } else if (p1 instanceof LinearGradientPaint 
                && p2 instanceof LinearGradientPaint) {
            LinearGradientPaint lgp1 = (LinearGradientPaint) p1;
            LinearGradientPaint lgp2 = (LinearGradientPaint) p2;
            return lgp1.getStartPoint().equals(lgp2.getStartPoint())
                    && lgp1.getEndPoint().equals(lgp2.getEndPoint()) 
                    && Arrays.equals(lgp1.getFractions(), lgp2.getFractions())
                    && Arrays.equals(lgp1.getColors(), lgp2.getColors())
                    && lgp1.getCycleMethod() == lgp2.getCycleMethod()
                    && lgp1.getColorSpace() == lgp2.getColorSpace()
                    && lgp1.getTransform().equals(lgp2.getTransform());
        } else if (p1 instanceof RadialGradientPaint 
                && p2 instanceof RadialGradientPaint) {
            RadialGradientPaint rgp1 = (RadialGradientPaint) p1;
            RadialGradientPaint rgp2 = (RadialGradientPaint) p2;
            return rgp1.getCenterPoint().equals(rgp2.getCenterPoint())
                    && rgp1.getRadius() == rgp2.getRadius() 
                    && rgp1.getFocusPoint().equals(rgp2.getFocusPoint())
                    && Arrays.equals(rgp1.getFractions(), rgp2.getFractions())
                    && Arrays.equals(rgp1.getColors(), rgp2.getColors())
                    && rgp1.getCycleMethod() == rgp2.getCycleMethod()
                    && rgp1.getColorSpace() == rgp2.getColorSpace()
                    && rgp1.getTransform().equals(rgp2.getTransform());
        } else {
            return p1.equals(p2);
        }
    }

    
    public static <K extends Comparable<K>> boolean equal(Map<K, Paint> map1, Map<K, Paint> map2) {
        if (!map1.keySet().equals(map2.keySet())) {
            return false;
        }
        for (K key : map1.keySet()) {
            Paint p1 = map1.get(key);
            Paint p2 = map2.get(key);
            if (!PaintUtils.equal(p1, p2)) {
                return false;
            }
        }
        return true;
    }
    
    
    public static String colorToString(Color c) {
        try {
            Field[] fields = Color.class.getFields();
            for (int i = 0; i < fields.length; i++) {
                Field f = fields[i];
                if (Modifier.isPublic(f.getModifiers())
                        && Modifier.isFinal(f.getModifiers())
                        && Modifier.isStatic(f.getModifiers())) {
                    final String name = f.getName();
                    final Object oColor = f.get(null);
                    if (oColor instanceof Color) {
                        if (c.equals(oColor)) {
                            return name;
                        }
                    }
                }
            }
        } catch (Exception e) {
            //
        }

        // no defined constant color, so this must be a user defined color
        final String color = Integer.toHexString(c.getRGB() & 0x00ffffff);
        final StringBuilder retval = new StringBuilder(7);
        retval.append("#");

        final int fillUp = 6 - color.length();
        for (int i = 0; i < fillUp; i++) {
            retval.append("0");
        }

        retval.append(color);
        return retval.toString();
    }

    
    public static Color stringToColor(String value) {
        if (value == null) {
            return Color.BLACK;
        }
        try {
            // get color by hex or octal value
            return Color.decode(value);
        } catch (NumberFormatException nfe) {
            // if we can't decode lets try to get it by name
            try {
                // try to get a color by name using reflection
                final Field f = Color.class.getField(value);
                return (Color) f.get(null);
            } catch (Exception ce) {
                // if we can't get any color return black
                return Color.BLACK;
            }
        }
    }
}

