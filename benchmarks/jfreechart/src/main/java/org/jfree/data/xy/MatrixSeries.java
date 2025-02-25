

package org.jfree.data.xy;

import java.io.Serializable;

import org.jfree.data.general.Series;


public class MatrixSeries<K extends Comparable<K>> extends Series<K> 
        implements Serializable {

    
    private static final long serialVersionUID = 7934188527308315704L;

    
    protected double[][] data;

    
    public MatrixSeries(K name, int rows, int columns) {
        super(name);
        this.data = new double[rows][columns];
        zeroAll();
    }

    
    public int getColumnsCount() {
        return this.data[0].length;
    }


    
    public Number getItem(int itemIndex) {
        int i = getItemRow(itemIndex);
        int j = getItemColumn(itemIndex);
        return get(i, j);
    }


    
    public int getItemColumn(int itemIndex) {
        //assert itemIndex >= 0 && itemIndex < getItemCount();
        return itemIndex % getColumnsCount();
    }


    
    @Override
    public int getItemCount() {
        return getRowCount() * getColumnsCount();
    }


    
    public int getItemRow(int itemIndex) {
        //assert itemIndex >= 0 && itemIndex < getItemCount();
        return itemIndex / getColumnsCount();
    }


    
    public int getRowCount() {
        return this.data.length;
    }


    
    public double get(int i, int j) {
        return this.data[i][j];
    }


    
    public void update(int i, int j, double mij) {
        this.data[i][j] = mij;
        fireSeriesChanged();
    }


    
    public void zeroAll() {
        int rows = getRowCount();
        int columns = getColumnsCount();

        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                this.data[row][column] = 0.0;
            }
        }
        fireSeriesChanged();
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MatrixSeries)) {
            return false;
        }
        MatrixSeries that = (MatrixSeries) obj;
        if (!(getRowCount() == that.getRowCount())) {
            return false;
        }
        if (!(getColumnsCount() == that.getColumnsCount())) {
            return false;
        }
        for (int r = 0; r < getRowCount(); r++) {
            for (int c = 0; c < getColumnsCount(); c++) {
                if (get(r, c) != that.get(r, c)) {
                    return false;
                }
            }
        }
        return super.equals(obj);
    }

}
