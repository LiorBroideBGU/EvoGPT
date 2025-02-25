

package org.jfree.chart.swing.editor;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.internal.Args;


public class ChartEditorManager {

    
    static ChartEditorFactory factory = new DefaultChartEditorFactory();

    
    private ChartEditorManager() {
        // nothing to do
    }

    
    public static ChartEditorFactory getChartEditorFactory() {
        return factory;
    }

    
    public static void setChartEditorFactory(ChartEditorFactory f) {
        Args.nullNotPermitted(f, "f");
        factory = f;
    }

    
    public static ChartEditor getChartEditor(JFreeChart chart) {
        return factory.createEditor(chart);
    }
}
