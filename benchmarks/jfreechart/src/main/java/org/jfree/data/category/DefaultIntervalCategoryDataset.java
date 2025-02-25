

package org.jfree.data.category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import org.jfree.chart.internal.Args;

import org.jfree.data.DataUtils;
import org.jfree.data.UnknownKeyException;
import org.jfree.data.general.AbstractSeriesDataset;


public class DefaultIntervalCategoryDataset extends AbstractSeriesDataset
        implements IntervalCategoryDataset {

    
    private Comparable[] seriesKeys;

    
    private Comparable[] categoryKeys;

    
    private Number[][] startData;

    
    private Number[][] endData;

    
    public DefaultIntervalCategoryDataset(double[][] starts, double[][] ends) {
        this(DataUtils.createNumberArray2D(starts),
                DataUtils.createNumberArray2D(ends));
    }

    
    public DefaultIntervalCategoryDataset(Number[][] starts, Number[][] ends) {
        this(null, null, starts, ends);
    }

    
    public DefaultIntervalCategoryDataset(String[] seriesNames,
            Number[][] starts, Number[][] ends) {
        this(seriesNames, null, starts, ends);
    }

    
    public DefaultIntervalCategoryDataset(Comparable[] seriesKeys,
            Comparable[] categoryKeys, Number[][] starts, Number[][] ends) {

        this.startData = starts;
        this.endData = ends;

        if (starts != null && ends != null) {
            ResourceBundle resources = ResourceBundle.getBundle("org.jfree.data.resources.DataPackageResources");

            int seriesCount = starts.length;
            if (seriesCount != ends.length) {
                String errMsg = "DefaultIntervalCategoryDataset: the number "
                    + "of series in the start value dataset does "
                    + "not match the number of series in the end "
                    + "value dataset.";
                throw new IllegalArgumentException(errMsg);
            }
            if (seriesCount > 0) {

                // set up the series names...
                if (seriesKeys != null) {

                    if (seriesKeys.length != seriesCount) {
                        throw new IllegalArgumentException(
                                "The number of series keys does not "
                                + "match the number of series in the data.");
                    }

                    this.seriesKeys = seriesKeys;
                }
                else {
                    String prefix = resources.getString(
                            "series.default-prefix") + " ";
                    this.seriesKeys = generateKeys(seriesCount, prefix);
                }

                // set up the category names...
                int categoryCount = starts[0].length;
                if (categoryCount != ends[0].length) {
                    String errMsg = "DefaultIntervalCategoryDataset: the "
                                + "number of categories in the start value "
                                + "dataset does not match the number of "
                                + "categories in the end value dataset.";
                    throw new IllegalArgumentException(errMsg);
                }
                if (categoryKeys != null) {
                    if (categoryKeys.length != categoryCount) {
                        throw new IllegalArgumentException(
                                "The number of category keys does not match "
                                + "the number of categories in the data.");
                    }
                    this.categoryKeys = categoryKeys;
                }
                else {
                    String prefix = resources.getString(
                            "categories.default-prefix") + " ";
                    this.categoryKeys = generateKeys(categoryCount, prefix);
                }

            }
            else {
                this.seriesKeys = new Comparable[0];
                this.categoryKeys = new Comparable[0];
            }
        }

    }

    
    @Override
    public int getSeriesCount() {
        int result = 0;
        if (this.startData != null) {
            result = this.startData.length;
        }
        return result;
    }

    
    public int getSeriesIndex(Comparable seriesKey) {
        int result = -1;
        for (int i = 0; i < this.seriesKeys.length; i++) {
            if (seriesKey.equals(this.seriesKeys[i])) {
                result = i;
                break;
            }
        }
        return result;
    }

    
    @Override
    public Comparable getSeriesKey(int series) {
        if ((series >= getSeriesCount()) || (series < 0)) {
            throw new IllegalArgumentException("No such series : " + series);
        }
        return this.seriesKeys[series];
    }

    
    public void setSeriesKeys(Comparable[] seriesKeys) {
        Args.nullNotPermitted(seriesKeys, "seriesKeys");
        if (seriesKeys.length != getSeriesCount()) {
            throw new IllegalArgumentException(
                    "The number of series keys does not match the data.");
        }
        this.seriesKeys = seriesKeys;
        fireDatasetChanged();
    }

    
    public int getCategoryCount() {
        int result = 0;
        if (this.startData != null) {
            if (getSeriesCount() > 0) {
                result = this.startData[0].length;
            }
        }
        return result;
    }

    
    @Override
    public List getColumnKeys() {
        // the CategoryDataset interface expects a list of categories, but
        // we've stored them in an array...
        if (this.categoryKeys == null) {
            return new ArrayList();
        }
        else {
            return Collections.unmodifiableList(Arrays.asList(
                    this.categoryKeys));
        }
    }

    
    public void setCategoryKeys(Comparable[] categoryKeys) {
        Args.nullNotPermitted(categoryKeys, "categoryKeys");
        if (categoryKeys.length != getCategoryCount()) {
            throw new IllegalArgumentException(
                    "The number of categories does not match the data.");
        }
        for (int i = 0; i < categoryKeys.length; i++) {
            if (categoryKeys[i] == null) {
                throw new IllegalArgumentException(
                    "DefaultIntervalCategoryDataset.setCategoryKeys(): "
                    + "null category not permitted.");
            }
        }
        this.categoryKeys = categoryKeys;
        fireDatasetChanged();
    }

    
    @Override
    public Number getValue(Comparable series, Comparable category) {
        int seriesIndex = getSeriesIndex(series);
        if (seriesIndex < 0) {
            throw new UnknownKeyException("Unknown 'series' key.");
        }
        int itemIndex = getColumnIndex(category);
        if (itemIndex < 0) {
            throw new UnknownKeyException("Unknown 'category' key.");
        }
        return getValue(seriesIndex, itemIndex);
    }

    
    @Override
    public Number getValue(int series, int category) {
        return getEndValue(series, category);
    }

    
    @Override
    public Number getStartValue(Comparable series, Comparable category) {
        int seriesIndex = getSeriesIndex(series);
        if (seriesIndex < 0) {
            throw new UnknownKeyException("Unknown 'series' key.");
        }
        int itemIndex = getColumnIndex(category);
        if (itemIndex < 0) {
            throw new UnknownKeyException("Unknown 'category' key.");
        }
        return getStartValue(seriesIndex, itemIndex);
    }

    
    @Override
    public Number getStartValue(int series, int category) {

        // check arguments...
        if ((series < 0) || (series >= getSeriesCount())) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.getValue(): "
                + "series index out of range.");
        }

        if ((category < 0) || (category >= getCategoryCount())) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.getValue(): "
                + "category index out of range.");
        }

        // fetch the value...
        return this.startData[series][category];

    }

    
    @Override
    public Number getEndValue(Comparable series, Comparable category) {
        int seriesIndex = getSeriesIndex(series);
        if (seriesIndex < 0) {
            throw new UnknownKeyException("Unknown 'series' key.");
        }
        int itemIndex = getColumnIndex(category);
        if (itemIndex < 0) {
            throw new UnknownKeyException("Unknown 'category' key.");
        }
        return getEndValue(seriesIndex, itemIndex);
    }

    
    @Override
    public Number getEndValue(int series, int category) {
        if ((series < 0) || (series >= getSeriesCount())) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.getValue(): "
                + "series index out of range.");
        }

        if ((category < 0) || (category >= getCategoryCount())) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.getValue(): "
                + "category index out of range.");
        }

        return this.endData[series][category];
    }

    
    public void setStartValue(int series, Comparable category, Number value) {

        // does the series exist?
        if ((series < 0) || (series > getSeriesCount() - 1)) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.setValue: "
                + "series outside valid range.");
        }

        // is the category valid?
        int categoryIndex = getCategoryIndex(category);
        if (categoryIndex < 0) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.setValue: "
                + "unrecognised category.");
        }

        // update the data...
        this.startData[series][categoryIndex] = value;
        fireDatasetChanged();

    }

    
    public void setEndValue(int series, Comparable category, Number value) {

        // does the series exist?
        if ((series < 0) || (series > getSeriesCount() - 1)) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.setValue: "
                + "series outside valid range.");
        }

        // is the category valid?
        int categoryIndex = getCategoryIndex(category);
        if (categoryIndex < 0) {
            throw new IllegalArgumentException(
                "DefaultIntervalCategoryDataset.setValue: "
                + "unrecognised category.");
        }

        // update the data...
        this.endData[series][categoryIndex] = value;
        fireDatasetChanged();

    }

    
    public int getCategoryIndex(Comparable category) {
        int result = -1;
        for (int i = 0; i < this.categoryKeys.length; i++) {
            if (category.equals(this.categoryKeys[i])) {
                result = i;
                break;
            }
        }
        return result;
    }

    
    private Comparable[] generateKeys(int count, String prefix) {
        Comparable[] result = new Comparable[count];
        String name;
        for (int i = 0; i < count; i++) {
            name = prefix + (i + 1);
            result[i] = name;
        }
        return result;
    }

    
    @Override
    public Comparable getColumnKey(int column) {
        return this.categoryKeys[column];
    }

    
    @Override
    public int getColumnIndex(Comparable columnKey) {
        Args.nullNotPermitted(columnKey, "columnKey");
        return getCategoryIndex(columnKey);
    }

    
    @Override
    public int getRowIndex(Comparable rowKey) {
        return getSeriesIndex(rowKey);
    }

    
    @Override
    public List getRowKeys() {
        // the CategoryDataset interface expects a list of series, but
        // we've stored them in an array...
        if (this.seriesKeys == null) {
            return new ArrayList();
        }
        else {
            return Collections.unmodifiableList(Arrays.asList(this.seriesKeys));
        }
    }

    
    @Override
    public Comparable getRowKey(int row) {
        if ((row >= getRowCount()) || (row < 0)) {
            throw new IllegalArgumentException(
                    "The 'row' argument is out of bounds.");
        }
        return this.seriesKeys[row];
    }

    
    @Override
    public int getColumnCount() {
        return this.categoryKeys.length;
    }

    
    @Override
    public int getRowCount() {
        return this.seriesKeys.length;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof DefaultIntervalCategoryDataset)) {
            return false;
        }
        DefaultIntervalCategoryDataset that
                = (DefaultIntervalCategoryDataset) obj;
        if (!Arrays.equals(this.seriesKeys, that.seriesKeys)) {
            return false;
        }
        if (!Arrays.equals(this.categoryKeys, that.categoryKeys)) {
            return false;
        }
        if (!equal(this.startData, that.startData)) {
            return false;
        }
        if (!equal(this.endData, that.endData)) {
            return false;
        }
        // seem to be the same...
        return true;
    }

    @Override
    public int hashCode()
    {
        int hash = 3;
        hash = 61 * hash + Arrays.deepHashCode( this.seriesKeys );
        hash = 61 * hash + Arrays.deepHashCode( this.categoryKeys );
        hash = 61 * hash + Arrays.deepHashCode( this.startData );
        hash = 61 * hash + Arrays.deepHashCode( this.endData );
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultIntervalCategoryDataset clone
                = (DefaultIntervalCategoryDataset) super.clone();
        clone.categoryKeys = (Comparable[]) this.categoryKeys.clone();
        clone.seriesKeys = (Comparable[]) this.seriesKeys.clone();
        clone.startData = clone(this.startData);
        clone.endData = clone(this.endData);
        return clone;
    }

    
    private static boolean equal(Number[][] array1, Number[][] array2) {
        if (array1 == null) {
            return (array2 == null);
        }
        if (array2 == null) {
            return false;
        }
        if (array1.length != array2.length) {
            return false;
        }
        for (int i = 0; i < array1.length; i++) {
            if (!Arrays.equals(array1[i], array2[i])) {
                return false;
            }
        }
        return true;
    }

    
    private static Number[][] clone(Number[][] array) {
        Args.nullNotPermitted(array, "array");
        Number[][] result = new Number[array.length][];
        for (int i = 0; i < array.length; i++) {
            Number[] child = array[i];
            Number[] copychild = new Number[child.length];
            System.arraycopy(child, 0, copychild, 0, child.length);
            result[i] = copychild;
        }
        return result;
    }

}
