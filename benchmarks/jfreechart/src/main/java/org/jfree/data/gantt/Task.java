

package org.jfree.data.gantt;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.jfree.chart.internal.Args;
import org.jfree.chart.api.PublicCloneable;

import org.jfree.data.time.SimpleTimePeriod;
import org.jfree.data.time.TimePeriod;


public class Task implements Cloneable, PublicCloneable, Serializable {

    
    private static final long serialVersionUID = 1094303785346988894L;

    
    private String description;

    
    private TimePeriod duration;

    
    private Double percentComplete;

    
    private List subtasks;

    
    public Task(String description, TimePeriod duration) {
        Args.nullNotPermitted(description, "description");
        this.description = description;
        this.duration = duration;
        this.percentComplete = null;
        this.subtasks = new java.util.ArrayList();
    }

    
    public Task(String description, Date start, Date end) {
        this(description, new SimpleTimePeriod(start, end));
    }

    
    public String getDescription() {
        return this.description;
    }

    
    public void setDescription(String description) {
        Args.nullNotPermitted(description, "description");
        this.description = description;
    }

    
    public TimePeriod getDuration() {
        return this.duration;
    }

    
    public void setDuration(TimePeriod duration) {
        this.duration = duration;
    }

    
    public Double getPercentComplete() {
        return this.percentComplete;
    }

    
    public void setPercentComplete(Double percent) {
        this.percentComplete = percent;
    }

    
    public void setPercentComplete(double percent) {
        setPercentComplete(Double.valueOf(percent));
    }

    
    public void addSubtask(Task subtask) {
        Args.nullNotPermitted(subtask, "subtask");
        this.subtasks.add(subtask);
    }

    
    public void removeSubtask(Task subtask) {
        this.subtasks.remove(subtask);
    }

    
    public int getSubtaskCount() {
        return this.subtasks.size();
    }

    
    public Task getSubtask(int index) {
        return (Task) this.subtasks.get(index);
    }

    
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Task)) {
            return false;
        }
        Task that = (Task) object;
        if (!Objects.equals(this.description, that.description)) {
            return false;
        }
        if (!Objects.equals(this.duration, that.duration)) {
            return false;
        }
        if (!Objects.equals(this.percentComplete, that.percentComplete)) {
            return false;
        }
        if (!Objects.equals(this.subtasks, that.subtasks)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode(){
        int hash = 7;
        hash = 71 * hash + Objects.hashCode(this.description);
        hash = 71 * hash + Objects.hashCode(this.duration);
        hash = 71 * hash + Objects.hashCode(this.percentComplete);
        hash = 71 * hash + Objects.hashCode(this.subtasks);
        return hash;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        Task clone = (Task) super.clone();
        return clone;
    }

}
