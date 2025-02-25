

package org.jfree.chart.axis;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import org.jfree.chart.internal.Args;


public class CompassFormat extends NumberFormat {

    
    public final String[] directions;

    
    public CompassFormat() {
        this("N", "E", "S", "W");
    }

    
    public CompassFormat(String n, String e, String s, String w) {
        this(new String[] {
            n, n + n + e, n + e, e + n + e, e, e + s + e, s + e, s + s + e, s,
            s + s + w, s + w, w + s + w, w, w + n + w, n + w, n + n + w
        });
    }

    
    public CompassFormat(String[] directions) {
        super();
        Args.nullNotPermitted(directions, "directions");
        if (directions.length != 16) {
            throw new IllegalArgumentException("The 'directions' array must "
                    + "contain exactly 16 elements");
        }
        this.directions = directions;
    }

    
    public String getDirectionCode(double direction) {
        direction = direction % 360;
        if (direction < 0.0) {
            direction = direction + 360.0;
        }
        int index = ((int) Math.floor(direction / 11.25) + 1) / 2;
        return directions[index];
    }

    
    @Override
    public StringBuffer format(double number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return toAppendTo.append(getDirectionCode(number));
    }

    
    @Override
    public StringBuffer format(long number, StringBuffer toAppendTo,
            FieldPosition pos) {
        return toAppendTo.append(getDirectionCode(number));
    }

    
    @Override
    public Number parse(String source, ParsePosition parsePosition) {
        return null;
    }

}
