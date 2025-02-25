

package org.jfree.data.flow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.jfree.chart.internal.Args;
import org.jfree.chart.internal.CloneUtils;
import org.jfree.chart.api.PublicCloneable;
import org.jfree.data.general.AbstractDataset;


public class DefaultFlowDataset<K extends Comparable<K>> extends AbstractDataset 
        implements FlowDataset<K>, PublicCloneable, Serializable {

    
    private List<List<K>> nodes;
    
    
    private Map<NodeKey, Map<String, Object>> nodeProperties;
    
    
    private Map<FlowKey<K>, Number> flows;
    
    
    private Map<FlowKey, Map<String, Object>> flowProperties;

    
    public DefaultFlowDataset() {
        this.nodes = new ArrayList<>();
        this.nodes.add(new ArrayList<>());
        this.nodes.add(new ArrayList<>());
        this.nodeProperties = new HashMap<>();
        this.flows = new HashMap<>();
        this.flowProperties = new HashMap<>();
    }

    
    @Override
    public List<K> getSources(int stage) {
        return new ArrayList<>(this.nodes.get(stage));
    }

    
    @Override
    public List<K> getDestinations(int stage) {
        return new ArrayList<>(this.nodes.get(stage + 1));
    }

    
    @Override
    public Set<NodeKey<K>> getAllNodes() {
        Set<NodeKey<K>> result = new HashSet<>();
        for (int s = 0; s <= this.getStageCount(); s++) {
            for (K key : this.getSources(s)) {
                result.add(new NodeKey<>(s, key));
            }
        }
        return result;
    }
 
        
    @Override
    public Object getNodeProperty(NodeKey<K> nodeKey, String propertyKey) {
        Map<String, Object> props = this.nodeProperties.get(nodeKey);
        if (props != null) {
            return props.get(propertyKey);
        }
        return null;
    }
    
    
    public void setNodeProperty(NodeKey<K> nodeKey, String propertyKey, Object value) {
        Map<String, Object> props = this.nodeProperties.get(nodeKey);
        if (props == null) {
            props = new HashMap<>();
            this.nodeProperties.put(nodeKey, props);
        }
        props.put(propertyKey, value);
        fireDatasetChanged();
    }

    
    @Override
    public Number getFlow(int stage, K source, K destination) {
        return this.flows.get(new FlowKey<>(stage, source, destination));
    }

    
    public void setFlow(int stage, K source, K destination, double flow) {
        Args.requireInRange(stage, "stage", 0, getStageCount());
        Args.nullNotPermitted(source, "source");
        Args.nullNotPermitted(destination, "destination");
        if (stage > this.nodes.size() - 2) {
            this.nodes.add(new ArrayList<>());
        }
        if (!getSources(stage).contains(source)) {
            this.nodes.get(stage).add(source);
        }
        if (!getDestinations(stage).contains(destination)) {
            this.nodes.get(stage + 1).add(destination);
        }
        this.flows.put(new FlowKey<>(stage, source, destination), flow);
        fireDatasetChanged();
    }

        
    @Override
    public Object getFlowProperty(FlowKey<K> flowKey, String propertyKey) {
        Map<String, Object> props = this.flowProperties.get(flowKey);
        if (props != null) {
            return props.get(propertyKey);
        }
        return null;      
    }

    

    public void setFlowProperty(FlowKey<K> flowKey, String propertyKey, Object value) {
        Map<String, Object> props = this.flowProperties.get(flowKey);
        if (props == null) {
            props = new HashMap<>();
            this.flowProperties.put(flowKey, props);
        }
        props.put(propertyKey, value);
        fireDatasetChanged();
    }

    
    @Override
    public int getStageCount() {
        return this.nodes.size() - 1;
    }
    
    
    @Override
    public Set<FlowKey<K>> getAllFlows() {
        return new HashSet<>(this.flows.keySet());    
    }
    
    
    public List<FlowKey<K>> getInFlows(NodeKey nodeKey) {
        Args.nullNotPermitted(nodeKey, "nodeKey");
        if (nodeKey.getStage() == 0) {
            return Collections.EMPTY_LIST;
        }
        List<FlowKey<K>> result = new ArrayList<>();
        for (FlowKey<K> flowKey : this.flows.keySet()) {
            if (flowKey.getStage() == nodeKey.getStage() - 1 && flowKey.getDestination().equals(nodeKey.getNode())) {
                result.add(flowKey);
            }
        }
        return result;
    }

    
    public List<FlowKey> getOutFlows(NodeKey nodeKey) {
        Args.nullNotPermitted(nodeKey, "nodeKey");
        if (nodeKey.getStage() == this.getStageCount()) {
            return Collections.EMPTY_LIST;
        }
        List<FlowKey> result = new ArrayList<>();
        for (FlowKey flowKey : this.flows.keySet()) {
            if (flowKey.getStage() == nodeKey.getStage() && flowKey.getSource().equals(nodeKey.getNode())) {
                result.add(flowKey);
            }
        }
        return result;
    }

    
    @Override
    public Object clone() throws CloneNotSupportedException {
        DefaultFlowDataset<K> clone = (DefaultFlowDataset) super.clone();
        clone.flows = new HashMap<>(this.flows);
        clone.nodes = new ArrayList<>();
        for (List<?> list : nodes) {
            clone.nodes.add((List<K>) CloneUtils.cloneList(list));
        }
        return clone;
    }

    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof FlowDataset)) {
            return false;
        }
        final FlowDataset other = (FlowDataset) obj;
        if (other.getStageCount() != getStageCount()) {
            return false;
        }
        for (int stage = 0; stage < getStageCount(); stage++) {
            if (!Objects.equals(other.getSources(stage), getSources(stage))) {
                return false;
            }
            if (!Objects.equals(other.getDestinations(stage), getDestinations(stage))) {
                return false;
            }
            for (K source : getSources(stage)) {
                for (K destination : getDestinations(stage)) {
                    if (!Objects.equals(other.getFlow(stage, source, destination), getFlow(stage, source, destination))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(getSources(0));
        hash = 89 * hash + Objects.hashCode(getDestinations(getStageCount() - 1));
        return hash;
    }

}
