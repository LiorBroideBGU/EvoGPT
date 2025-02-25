

package org.jfree.chart;

import java.util.Map;


public final class ChartHints {

    private ChartHints() {
        // no need to instantiate this    
    }
    
    
    public static final Key KEY_BEGIN_ELEMENT = new ChartHints.Key(0);
    
    
    public static final Key KEY_END_ELEMENT = new ChartHints.Key(1);
    
    
    public static class Key extends java.awt.RenderingHints.Key {

        
        public Key(int privateKey) {
            super(privateKey);    
        }
    
        
        @Override
        public boolean isCompatibleValue(Object val) {
            switch (intKey()) {
                case 0:
                    return val == null || val instanceof String 
                            || val instanceof Map;
                case 1:
                    return val == null || val instanceof Object;
                default:
                    throw new RuntimeException("Not possible!");
            }
        }
    }
    
}
