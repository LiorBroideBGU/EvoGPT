

package org.jfree.data.gantt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;

import org.jfree.data.general.Series;


public class TaskSeries<K extends Comparable<K>> extends Series<K> {

    
    private List<Task> tasks;

    
    public TaskSeries(K name) {
        super(name);
        this.tasks = new ArrayList<>();
    }

    
    public void add(Task task) {
        Args.nullNotPermitted(task, "task");
        this.tasks.add(task);
        fireSeriesChanged();
    }

    
    public void remove(Task task) {
        this.tasks.remove(task);
        fireSeriesChanged();
    }

    
    public void removeAll() {
        this.tasks.clear();
        fireSeriesChanged();
    }

    
    @Override
    public int getItemCount() {
        return this.tasks.size();
    }

    
    public Task get(int index) {
        return this.tasks.get(index);
    }

    
    public Task get(String description) {
        Task result = null;
        int count = this.tasks.size();
        for (int i = 0; i < count; i++) {
            Task t = this.tasks.get(i);
            if (t.getDescription().equals(description)) {
                result = t;
                break;
            }
        }
        return result;
    }

    
    public List<Task> getTasks() {
        return Collections.unmodifiableList(this.tasks);
    }

    
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof TaskSeries)) {
            return false;
        }
        if (!super.equals(obj)) {
            return false;
        }
        TaskSeries that = (TaskSeries) obj;
        if (!this.tasks.equals(that.tasks)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 5;
        hash = 67 * hash + Objects.hashCode(this.tasks);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        TaskSeries clone = (TaskSeries) super.clone();
        clone.tasks = CloneUtils.cloneList(this.tasks);
        return clone;
    }

}
