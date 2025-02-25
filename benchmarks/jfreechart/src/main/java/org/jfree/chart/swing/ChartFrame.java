

package org.jfree.chart.swing;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;
import org.jfree.chart.JFreeChart;


public class ChartFrame extends JFrame {

    
    private final ChartPanel chartPanel;

    
    public ChartFrame(String title, JFreeChart chart) {
        this(title, chart, false);
    }

    
    public ChartFrame(String title, JFreeChart chart, boolean scrollPane) {
        super(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        this.chartPanel = new ChartPanel(chart, false);
        if (scrollPane) {
            setContentPane(new JScrollPane(this.chartPanel));
        }
        else {
            setContentPane(this.chartPanel);
        }
    }

    
    public ChartPanel getChartPanel() {
        return this.chartPanel;
    }

}
