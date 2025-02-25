

package org.jfree.data.flow;

import java.util.List;
import org.jfree.chart.internal.Args;


public class FlowDatasetUtils {
    
    private FlowDatasetUtils() {
        // no need for instantiation
    }

    
    public static <K extends Comparable<K>> double calculateInflow(FlowDataset<K> dataset, K node, int stage) {
        Args.nullNotPermitted(dataset, "dataset");
        Args.nullNotPermitted(node, "node");
        Args.requireInRange(stage, "stage", 0, dataset.getStageCount());
        if (stage == 0) {
            return 0.0;  // there are no inflows for stage 0
        }
        double inflow = 0.0;
        List<K> sourceKeys = dataset.getSources(stage - 1);
        for (K key : sourceKeys) {
            Number n = dataset.getFlow(stage - 1, key, node);
            if (n != null) {
                inflow = inflow + n.doubleValue();
            }
        }
        return inflow;
    }

    
    public static <K extends Comparable<K>> double calculateOutflow(FlowDataset<K> dataset, K source, int stage) {
        Args.nullNotPermitted(dataset, "dataset");
        Args.nullNotPermitted(source, "source");
        Args.requireInRange(stage, "stage", 0, dataset.getStageCount());
        if (stage == dataset.getStageCount()) {
            return 0.0;  // there are no outflows for the last stage
        }
        double outflow = 0.0;
        List<K> destinationKeys = dataset.getDestinations(stage);
        for (K key : destinationKeys) {
            Number n = dataset.getFlow(stage, source, key);
            if (n != null) {
                outflow = outflow + n.doubleValue();
            }
        }
        return outflow;
    }

    
    public static <K extends Comparable<K>> double calculateTotalFlow(FlowDataset<K> dataset, int stage) {
        Args.nullNotPermitted(dataset, "dataset");
        double total = 0.0;
        for (K source : dataset.getSources(stage)) {
            for (K destination : dataset.getDestinations(stage)) {
                Number flow = dataset.getFlow(stage, source, destination);
                if (flow != null) {
                    total = total + flow.doubleValue();
                }
            }
        }
        return total;
    }
    
    
    public static <K extends Comparable<K>> boolean hasNodeSelections(FlowDataset<K> dataset) {
        Args.nullNotPermitted(dataset, "dataset");
        for (int stage = 0; stage < dataset.getStageCount() + 1; stage++) { // '+1' to include final destination nodes 
            for (K source : dataset.getSources(stage)) {
                NodeKey<K> nodeKey = new NodeKey<>(stage, source);
                if (Boolean.TRUE.equals(dataset.getNodeProperty(nodeKey, NodeKey.SELECTED_PROPERTY_KEY))) {
                    return true;
                }
            }
        }
        return false;
    }
    
    
    public static <K extends Comparable<K>> int selectedNodeCount(FlowDataset<K> dataset) {
        Args.nullNotPermitted(dataset, "dataset");
        int result = 0;
        for (int stage = 0; stage < dataset.getStageCount() + 1; stage++) { // '+1' to include final destination nodes 
            for (K source : dataset.getSources(stage)) {
                NodeKey<K> nodeKey = new NodeKey<>(stage, source);
                if (Boolean.TRUE.equals(dataset.getNodeProperty(nodeKey, NodeKey.SELECTED_PROPERTY_KEY))) {
                    result++;
                }
            }
        }
        return result;
    }

    
    public static <K extends Comparable<K>> boolean hasFlowSelections(FlowDataset<K> dataset) {
        Args.nullNotPermitted(dataset, "dataset");
        for (int s = 0; s < dataset.getStageCount(); s++) { 
            for (K source : dataset.getSources(s)) {
                for (K destination : dataset.getDestinations(s)) {
                    FlowKey<K> flowKey = new FlowKey<>(s, source, destination);
                    if (Boolean.TRUE.equals(dataset.getFlowProperty(flowKey, FlowKey.SELECTED_PROPERTY_KEY))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}

