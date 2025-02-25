

package org.jfree.data.gantt;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.general.AbstractSeriesDataset;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.data.time.TimePeriod;


public class TaskSeriesCollection<R extends Comparable<R>, C extends Comparable<C>>  
        extends AbstractSeriesDataset<R>
        implements GanttCategoryDataset<R, C>, Cloneable, PublicCloneable,
                   Serializable {

    
    private static final long serialVersionUID = -2065799050738449903L;

    
    private List<C> keys;

    
    private List<TaskSeries<R>> data;

    
    public TaskSeriesCollection() {
        this.keys = new ArrayList<>();
        this.data = new ArrayList<>();
    }

    
    public TaskSeries<R> getSeries(R key) {
        Args.nullNotPermitted(key, "key");
        TaskSeries<R> result = null;
        int index = getRowIndex(key);
        if (index >= 0) {
            result = getSeries(index);
        }
        return result;
    }

    
    public TaskSeries<R> getSeries(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);
        return this.data.get(series);
    }

    
    @Override
    public int getSeriesCount() {
        return getRowCount();
    }

    
    @Override
    public R getSeriesKey(int series) {
        TaskSeries<R> ts = this.data.get(series);
        return ts.getKey();
    }

    
    @Override
    public int getRowCount() {
        return this.data.size();
    }

    
    @Override
    public List<R> getRowKeys() {
        List<R> result = new ArrayList<>();
        for (TaskSeries<R> series : this.data) {
            result.add(series.getKey());
        }
        return result;
    }

    
    @Override
    public int getColumnCount() {
        return this.keys.size();
    }

    
    @Override
    public List<C> getColumnKeys() {
        return this.keys;
    }

    
    @Override
    public C getColumnKey(int index) {
        return this.keys.get(index);
    }

    
    @Override
    public int getColumnIndex(C columnKey) {
        Args.nullNotPermitted(columnKey, "columnKey");
        return this.keys.indexOf(columnKey);
    }

    
    @Override
    public int getRowIndex(R rowKey) {
        int result = -1;
        int count = this.data.size();
        for (int i = 0; i < count; i++) {
            TaskSeries<R> s = this.data.get(i);
            if (s.getKey().equals(rowKey)) {
                result = i;
                break;
            }
        }
        return result;
    }

    
    @Override
    public R getRowKey(int index) {
        TaskSeries<R> series = this.data.get(index);
        return series.getKey();
    }

    
    public void add(TaskSeries<R> series) {
        Args.nullNotPermitted(series, "series");
        this.data.add(series);
        series.addChangeListener(this);

        // look for any keys that we don't already know about...
        for (Task task : series.getTasks()) {
            C key = (C) task.getDescription(); // FIXME
            int index = this.keys.indexOf(key);
            if (index < 0) {
                this.keys.add(key);
            }
        }
        fireDatasetChanged();
    }

    
    public void remove(TaskSeries<R> series) {
        Args.nullNotPermitted(series, "series");
        if (this.data.contains(series)) {
            series.removeChangeListener(this);
            this.data.remove(series);
            fireDatasetChanged();
        }
    }

    
    public void remove(int series) {
        Args.requireInRange(series, "series", 0, this.data.size() - 1);

        // fetch the series, remove the change listener, then remove the series.
        TaskSeries<R> ts = this.data.get(series);
        ts.removeChangeListener(this);
        this.data.remove(series);
        fireDatasetChanged();

    }

    
    public void removeAll() {
        // deregister the collection as a change listener to each series in
        // the collection.
        for (TaskSeries<R> series : this.data) {
            series.removeChangeListener(this);
        }

        // remove all the series from the collection and notify listeners.
        this.data.clear();
        fireDatasetChanged();
    }

    
    @Override
    public Number getValue(R rowKey, C columnKey) {
        return getStartValue(rowKey, columnKey);
    }

    
    @Override
    public Number getValue(int row, int column) {
        return getStartValue(row, column);
    }

    
    @Override
    public Number getStartValue(R rowKey, C columnKey) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            TimePeriod duration = task.getDuration();
            if (duration != null) {
                result = duration.getStart().getTime();
            }
        }
        return result;
    }

    
    @Override
    public Number getStartValue(int row, int column) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getStartValue(rowKey, columnKey);
    }

    
    @Override
    public Number getEndValue(R rowKey, C columnKey) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            TimePeriod duration = task.getDuration();
            if (duration != null) {
                result = duration.getEnd().getTime();
            }
        }
        return result;
    }

    
    @Override
    public Number getEndValue(int row, int column) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getEndValue(rowKey, columnKey);
    }

    
    @Override
    public Number getPercentComplete(int row, int column) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getPercentComplete(rowKey, columnKey);
    }

    
    @Override
    public Number getPercentComplete(R rowKey, C columnKey) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            result = task.getPercentComplete();
        }
        return result;
    }

    
    @Override
    public int getSubIntervalCount(int row, int column) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getSubIntervalCount(rowKey, columnKey);
    }

    
    @Override
    public int getSubIntervalCount(R rowKey, C columnKey) {
        int result = 0;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            result = task.getSubtaskCount();
        }
        return result;
    }

    
    @Override
    public Number getStartValue(int row, int column, int subinterval) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getStartValue(rowKey, columnKey, subinterval);
    }

    
    @Override
    public Number getStartValue(R rowKey, C columnKey, int subinterval) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            Task sub = task.getSubtask(subinterval);
            if (sub != null) {
                TimePeriod duration = sub.getDuration();
                if (duration != null) {
                    result = duration.getStart().getTime();
                }
            }
        }
        return result;
    }

    
    @Override
    public Number getEndValue(int row, int column, int subinterval) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getEndValue(rowKey, columnKey, subinterval);
    }

    
    @Override
    public Number getEndValue(R rowKey, C columnKey, int subinterval) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            Task sub = task.getSubtask(subinterval);
            if (sub != null) {
                TimePeriod duration = sub.getDuration();
                if (duration != null) {
                    result = duration.getEnd().getTime();
                }
            }
        }
        return result;
    }

    
    @Override
    public Number getPercentComplete(int row, int column, int subinterval) {
        R rowKey = getRowKey(row);
        C columnKey = getColumnKey(column);
        return getPercentComplete(rowKey, columnKey, subinterval);
    }

    
    @Override
    public Number getPercentComplete(R rowKey, C columnKey, int subinterval) {
        Number result = null;
        int row = getRowIndex(rowKey);
        TaskSeries<R> series = this.data.get(row);
        Task task = series.get(columnKey.toString());
        if (task != null) {
            Task sub = task.getSubtask(subinterval);
            if (sub != null) {
                result = sub.getPercentComplete();
            }
        }
        return result;
    }

    
    @Override
    public void seriesChanged(SeriesChangeEvent event) {
        refreshKeys();
        fireDatasetChanged();
    }

    
    private void refreshKeys() {

        this.keys.clear();
        for (int i = 0; i < getSeriesCount(); i++) {
            TaskSeries<R> series = this.data.get(i);
            // look for any keys that we don't already know about...
            for (Task task : series.getTasks()) {
                C key = (C) task.getDescription(); // FIXME
                int index = this.keys.indexOf(key);
                if (index < 0) {
                    this.keys.add(key);
                }
            }
        }

    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TaskSeriesCollection)) {
            return false;
        }
        TaskSeriesCollection that = (TaskSeriesCollection) obj;
        if (!Objects.equals(this.data, that.data)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 89 * hash + Objects.hashCode(this.data);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        TaskSeriesCollection clone = (TaskSeriesCollection) super.clone();
        clone.data = CloneUtils.cloneList(this.data);
        clone.keys = new ArrayList(this.keys);
        return clone;
    }

}
