package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.simplegraph.SimpleGraph;

public class WeisfeilerLehmanSimpleGraphIterator extends WeisfeilerLehmanIterator<SimpleGraph<StringLabel,StringLabel>> {
	private boolean reverse;
	
	public WeisfeilerLehmanSimpleGraphIterator(boolean reverse) {
		super();
		this.reverse = reverse;
	}

	@Override
	public void wlInitialize(List<SimpleGraph<StringLabel, StringLabel>> graphs) {
		for (SimpleGraph<StringLabel, StringLabel> graph : graphs) {
			for (SimpleGraph<StringLabel,StringLabel>.Node node : graph.nodes()) {
				String lab = labelDict.get(node.label().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(node.label().toString(), lab);
				}
				node.label().clear();
				node.label().append(lab);
				
				node.label().setPrevNBH("");
				node.label().setSameAsPrev(false);			
			}
			for (SimpleGraph<StringLabel,StringLabel>.Link link : graph.links()) {
				String lab = labelDict.get(link.tag().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(link.tag().toString(), lab);
				}
				link.tag().clear();
				link.tag().append(lab);
				
				link.tag().setPrevNBH("");
				link.tag().setSameAsPrev(false);
			}
		}
	}

	@Override
	public void wlIterate(List<SimpleGraph<StringLabel, StringLabel>> graphs) {
		Map<String, Bucket<SimpleGraph<StringLabel,StringLabel>.Node>> bucketsV = new HashMap<String, Bucket<SimpleGraph<StringLabel,StringLabel>.Node>>();
		Map<String, Bucket<SimpleGraph<StringLabel,StringLabel>.Link>> bucketsE = new HashMap<String, Bucket<SimpleGraph<StringLabel,StringLabel>.Link>>();

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" in the root direction	
			for (SimpleGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (SimpleGraph<StringLabel,StringLabel>.Link edge : graph.links()) {
					if (!bucketsV.containsKey(edge.tag().toString())) {
						bucketsV.put(edge.tag().toString(), new Bucket<SimpleGraph<StringLabel,StringLabel>.Node>(edge.tag().toString()));
					}			
					//bucketsV.get(edge.tag().toString()).getContents().add(edge.from());
				}

				// Add each incident edge to the bucket of the node label
				for (SimpleGraph<StringLabel,StringLabel>.Node vertex : graph.nodes()) {
					if (!bucketsE.containsKey(vertex.label().toString())) {
						bucketsE.put(vertex.label().toString(), new Bucket<SimpleGraph<StringLabel,StringLabel>.Link>(vertex.label().toString()));
					}
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.inLinks());
				}	
			}
		} else { // Labels "travel" in the fringe vertices direction
			for (SimpleGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (SimpleGraph<StringLabel,StringLabel>.Link edge : graph.links()) {
					if (!bucketsV.containsKey(edge.tag().toString())) {
						bucketsV.put(edge.tag().toString(), new Bucket<SimpleGraph<StringLabel,StringLabel>.Node>(edge.tag().toString()));
					}
					//bucketsV.get(edge.tag().toString()).getContents().add(edge.to());
				}

				// Add each incident edge to the bucket of the node label
				for (SimpleGraph<StringLabel,StringLabel>.Node vertex : graph.nodes()) {
					if (!bucketsE.containsKey(vertex.label().toString())) {
						bucketsE.put(vertex.label().toString(), new Bucket<SimpleGraph<StringLabel,StringLabel>.Link>(vertex.label().toString()));
					}
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.outLinks());
				}	
			}
		}


		// Since the labels are not necessarily neatly from i to n+i, we sort them
		List<String> keysE = new ArrayList<String>(bucketsE.keySet());
		Collections.sort(keysE);
		List<String> keysV = new ArrayList<String>(bucketsV.keySet());
		Collections.sort(keysV);

		/*
		for (SimpleGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTNode<StringLabel,StringLabel> node : graph.nodes()) {
				node.label().clear();
			}
		}
		*/
		

		// 3. Relabel to the labels in the buckets
		for (String key : keysV) {	
			// Process vertices
			Bucket<SimpleGraph<StringLabel,StringLabel>.Node> bucketV = bucketsV.get(key);			
			for (SimpleGraph<StringLabel,StringLabel>.Node vertex : bucketV.getContents()) {
				vertex.label().append("_");
				vertex.label().append(bucketV.getLabel());				
			}
		}
		for (String key : keysE) {
			// Process edges
			Bucket<SimpleGraph<StringLabel,StringLabel>.Link> bucketE = bucketsE.get(key);			
			for (SimpleGraph<StringLabel,StringLabel>.Link edge : bucketE.getContents()) {
				edge.tag().append("_");
				edge.tag().append(bucketE.getLabel());
			}
		}

		String label;
		for (SimpleGraph<StringLabel,StringLabel> graph : graphs) {
			for (SimpleGraph<StringLabel,StringLabel>.Link edge : graph.links()) {
				//if (trackPrevNBH) {
					String nb = edge.tag().toString();
					nb = nb.substring(nb.indexOf("_"));

					if (nb.equals(edge.tag().getPrevNBH())) {
						edge.tag().setSameAsPrev(true);
					}
					edge.tag().setPrevNBH(nb);
				//}

				if (!edge.tag().isSameAsPrev()) {
					label = labelDict.get(edge.tag().toString());						
					if (label == null) {					
						label = Integer.toString(labelDict.size());
						labelDict.put(edge.tag().toString(), label);				
					}
					edge.tag().clear();
					edge.tag().append(label);
				} else { // retain old label
					String old = edge.tag().toString();
					if (old.contains("_")) {
						old = old.substring(0, old.indexOf("_"));
						edge.tag().clear();
						edge.tag().append(old);
					} 
				}
			}

			for (SimpleGraph<StringLabel,StringLabel>.Node vertex : graph.nodes()) {
				//if (trackPrevNBH) {
					String nb = vertex.label().toString();
					if (nb.contains("_")) {
						nb = nb.substring(nb.indexOf("_"));
					} else {
						nb = "";
					}

					if (nb.equals(vertex.label().getPrevNBH())) {
						vertex.label().setSameAsPrev(true);
					}				
					vertex.label().setPrevNBH(nb);
				//}

				if (!vertex.label().isSameAsPrev()) {
					label = labelDict.get(vertex.label().toString());
					if (label == null) {
						label = Integer.toString(labelDict.size());
						labelDict.put(vertex.label().toString(), label);
					}
					vertex.label().clear();
					vertex.label().append(label);
				} else { // retain old label
					String old = vertex.label().toString();
					if (old.contains("_")) {
						old = old.substring(0, old.indexOf("_"));
						vertex.label().clear();
						vertex.label().append(old);
					} 
				}
			}
		}
	}
}
