

package org.jfree.data.xy;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;


public class XYDatasetTableModel extends AbstractTableModel
        implements TableModel, DatasetChangeListener  {

    
    TableXYDataset model = null;

    
    public XYDatasetTableModel() {
        super();
    }

    
    public XYDatasetTableModel(TableXYDataset dataset) {
        this();
        this.model = dataset;
        this.model.addChangeListener(this);
    }

    
    public void setModel(TableXYDataset dataset) {
        this.model = dataset;
        this.model.addChangeListener(this);
        fireTableDataChanged();
    }

    
    @Override
    public int getRowCount() {
        if (this.model == null) {
            return 0;
        }
        return this.model.getItemCount();
    }

    
    @Override
    public int getColumnCount() {
        if (this.model == null) {
            return 0;
        }
        return this.model.getSeriesCount() + 1;
    }

    
    @Override
    public String getColumnName(int column) {
        if (this.model == null) {
            return super.getColumnName(column);
        }
        if (column < 1) {
            return "X Value";
        }
        else {
            return this.model.getSeriesKey(column - 1).toString();
        }
    }

    
    @Override
    public Object getValueAt(int row, int column) {
        if (this.model == null) {
            return null;
        }
        if (column < 1) {
            return this.model.getX(0, row);
        }
        else {
            return this.model.getY(column - 1, row);
        }
    }

    
    @Override
    public void datasetChanged(DatasetChangeEvent event) {
        fireTableDataChanged();
    }

    
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
   }

    
    @Override
    public void setValueAt(Object value, int row, int column) {
        if (isCellEditable(row, column)) {
            // XYDataset only provides methods for reading a dataset...
        }
    }

//    
//    public static void main(String args[]) throws Exception {
//        JFrame frame = new JFrame();
//        JPanel panel = new JPanel();
//        panel.setLayout(new BorderLayout());
//
//        XYSeries s1 = new XYSeries("Series 1", true, false);
//        for (int i = 0; i < 10; i++) {
//            s1.add(i, Math.random());
//        }
//        XYSeries s2 = new XYSeries("Series 2", true, false);
//        for (int i = 0; i < 15; i++) {
//            s2.add(i, Math.random());
//        }
//        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
//        dataset.addSeries(s1);
//        dataset.addSeries(s2);
//        XYDatasetTableModel tablemodel = new XYDatasetTableModel();
//
//        tablemodel.setModel(dataset);
//
//        JTable dataTable = new JTable(tablemodel);
//        JScrollPane scroll = new JScrollPane(dataTable);
//        scroll.setPreferredSize(new Dimension(600, 150));
//
//        JFreeChart chart = ChartFactory.createXYLineChart(
//            "XY Series Demo",
//            "X", "Y", dataset, PlotOrientation.VERTICAL,
//            true,
//            true,
//            false
//        );
//
//        ChartPanel chartPanel = new ChartPanel(chart);
//
//        panel.add(chartPanel, BorderLayout.CENTER);
//        panel.add(scroll, BorderLayout.SOUTH);
//
//        frame.setContentPane(panel);
//        frame.setSize(600, 500);
//        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        frame.show();
//        RefineryUtilities.centerFrameOnScreen(frame);
//    }

}
