

package org.jfree.chart.swing.editor;

import org.jfree.chart.JFreeChart;


public class DefaultChartEditorFactory implements ChartEditorFactory {

    
    public DefaultChartEditorFactory() {
    }

    
    @Override
    public ChartEditor createEditor(JFreeChart chart) {
        return new DefaultChartEditor(chart);
    }

}
