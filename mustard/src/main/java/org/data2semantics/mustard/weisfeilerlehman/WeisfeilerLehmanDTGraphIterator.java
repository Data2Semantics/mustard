package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;


public class WeisfeilerLehmanDTGraphIterator extends WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> {
	private boolean reverse;

	public WeisfeilerLehmanDTGraphIterator(boolean reverse) {
		super();
		this.reverse = reverse;
	}


	@Override
	public void wlInitialize(List<DTGraph<StringLabel, StringLabel>> graphs) {
		for (DTGraph<StringLabel, StringLabel> graph : graphs) {
			for (DTNode<StringLabel,StringLabel> node : graph.nodes()) {
				String lab = labelDict.get(node.label().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(node.label().toString(), lab);
				}
				node.label().clear();
				node.label().append(lab);
			}
			for (DTLink<StringLabel,StringLabel> link : graph.links()) {
				String lab = labelDict.get(link.tag().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(link.tag().toString(), lab);
				}
				link.tag().clear();
				link.tag().append(lab);
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<StringLabel, StringLabel>> graphs) {
		Map<String, Bucket<DTNode<StringLabel,StringLabel>>> bucketsV = new HashMap<String, Bucket<DTNode<StringLabel,StringLabel>>>();
		Map<String, Bucket<DTLink<StringLabel,StringLabel>>> bucketsE = new HashMap<String, Bucket<DTLink<StringLabel,StringLabel>>>();

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" in the root direction	
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					if (!bucketsV.containsKey(edge.tag().toString())) {
						bucketsV.put(edge.tag().toString(), new Bucket<DTNode<StringLabel,StringLabel>>(edge.tag().toString()));
					}			
					bucketsV.get(edge.tag().toString()).getContents().add(edge.from());
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
					if (!bucketsE.containsKey(vertex.label().toString())) {
						bucketsE.put(vertex.label().toString(), new Bucket<DTLink<StringLabel,StringLabel>>(vertex.label().toString()));
					}
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksIn());
				}	
			}
		} else { // Labels "travel" in the fringe vertices direction
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					if (!bucketsV.containsKey(edge.tag().toString())) {
						bucketsV.put(edge.tag().toString(), new Bucket<DTNode<StringLabel,StringLabel>>(edge.tag().toString()));
					}
					bucketsV.get(edge.tag().toString()).getContents().add(edge.to());
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
					if (!bucketsE.containsKey(vertex.label().toString())) {
						bucketsE.put(vertex.label().toString(), new Bucket<DTLink<StringLabel,StringLabel>>(vertex.label().toString()));
					}
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksOut());
				}	
			}
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				edge.tag().append("_");
			}
			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				vertex.label().append("_");
			}
		}

		// Since the labels are not necessarily neatly from i to n+i, we sort them
		List<String> keysE = new ArrayList<String>(bucketsE.keySet());
		Collections.sort(keysE);
		List<String> keysV = new ArrayList<String>(bucketsV.keySet());
		Collections.sort(keysV);


		// 3. Relabel to the labels in the buckets
		for (String key : keysV) {	
			// Process vertices
			Bucket<DTNode<StringLabel,StringLabel>> bucketV = bucketsV.get(key);			
			for (DTNode<StringLabel,StringLabel> vertex : bucketV.getContents()) {
				vertex.label().append(bucketV.getLabel());
				vertex.label().append("_");
			}
		}
		for (String key : keysE) {
			// Process edges
			Bucket<DTLink<StringLabel,StringLabel>> bucketE = bucketsE.get(key);			
			for (DTLink<StringLabel,StringLabel> edge : bucketE.getContents()) {
				edge.tag().append(bucketE.getLabel());
				edge.tag().append("_");
			}
		}

		String label;
		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				label = labelDict.get(edge.tag().toString());						
				if (label == null) {					
					label = Integer.toString(labelDict.size());
					labelDict.put(edge.tag().toString(), label);				
				}
				edge.tag().clear();
				edge.tag().append(label);			
			}

			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
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
