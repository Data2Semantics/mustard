package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.UGraph;
import org.nodes.UNode;

public class WeisfeilerLehmanUGraphIterator extends WeisfeilerLehmanIterator<UGraph<StringLabel>> {

	@Override
	public void wlInitialize(List<UGraph<StringLabel>> graphs) {	
		for (UGraph<StringLabel> graph : graphs) {
			for (UNode<StringLabel> node : graph.nodes()) {
				String lab = labelDict.get(node.label().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(node.label().toString(), lab);
				}
				node.label().clear();
				node.label().append(lab);
			}
		}
	}

	@Override
	public void wlIterate(List<UGraph<StringLabel>> graphs) {
		Map<String, Bucket<UNode<StringLabel>>> buckets = new HashMap<String, Bucket<UNode<StringLabel>>>();

		// 1. Fill buckets 
		for (UGraph<StringLabel> graph : graphs) {
			for (UNode<StringLabel> vertex : graph.nodes()) {
				if (!buckets.containsKey(vertex.label().toString())) {
					buckets.put(vertex.label().toString(), new Bucket<UNode<StringLabel>>(vertex.label().toString()));
				}
				buckets.get(vertex.label().toString()).getContents().addAll(vertex.neighbors());
			}
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (UGraph<StringLabel> graph : graphs) {
			for (UNode<StringLabel> vertex : graph.nodes()) {
				vertex.label().append("_");
			}
		}
		
		// Since the labels are not necessarily neatly from i to n+i, we sort them
		List<String> keys = new ArrayList<String>(buckets.keySet());
		Collections.sort(keys);
		
		// 3. Relabel to the labels in the buckets
		for (String key : keys) {
			// Process vertices
			Bucket<UNode<StringLabel>> bucket = buckets.get(key);			
			for (UNode<StringLabel> vertex : bucket.getContents()) {
				vertex.label().append(bucket.getLabel());
				vertex.label().append("_");
			}
		}

		// Compress labels
		String label;
		for (UGraph<StringLabel> graph : graphs) {
			for (UNode<StringLabel> vertex : graph.nodes()) {
				label = labelDict.get(vertex.label().toString());
				if (label == null) {
					label = Integer.toString(labelDict.size());
					labelDict.put(vertex.label().toString(), label);
				}
				vertex.label().clear();
				vertex.label().append(label);
			}
		}
	}

}
