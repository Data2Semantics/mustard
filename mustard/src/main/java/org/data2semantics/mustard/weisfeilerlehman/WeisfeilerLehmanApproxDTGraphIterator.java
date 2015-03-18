package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WeisfeilerLehmanApproxDTGraphIterator extends WeisfeilerLehmanApproxIterator<DTGraph<ApproxStringLabel,ApproxStringLabel>, String> {
	private boolean reverse;

	public WeisfeilerLehmanApproxDTGraphIterator(boolean reverse) {
		this(reverse, 1, 1, 1);
	}

	public WeisfeilerLehmanApproxDTGraphIterator(boolean reverse, int maxPrevNBH, int maxLabelCard, int minFreq) {
		super(maxPrevNBH, maxLabelCard, minFreq);
		this.reverse = reverse;
	}

	@Override
	public void wlInitialize(List<DTGraph<ApproxStringLabel, ApproxStringLabel>> graphs) {
		for (DTGraph<ApproxStringLabel, ApproxStringLabel> graph : graphs) {
			for (DTNode<ApproxStringLabel,ApproxStringLabel> node : graph.nodes()) {
				String lab = labelDict.get(node.label().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(node.label().toString(), lab);
				}
				node.label().clear();
				node.label().append(lab);

				node.label().setPrevNBH("");
				node.label().setSameAsPrev(0);

			}
			for (DTLink<ApproxStringLabel,ApproxStringLabel> link : graph.links()) {
				String lab = labelDict.get(link.tag().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(link.tag().toString(), lab);
				}
				link.tag().clear();
				link.tag().append(lab);

				link.tag().setPrevNBH("");
				link.tag().setSameAsPrev(0);
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<ApproxStringLabel, ApproxStringLabel>> graphs, Map<String, Integer> labelFreq) {

		Map<String, Bucket<DTNode<ApproxStringLabel,ApproxStringLabel>>> bucketsV = new HashMap<String, Bucket<DTNode<ApproxStringLabel,ApproxStringLabel>>>();
		Map<String, Bucket<DTLink<ApproxStringLabel,ApproxStringLabel>>> bucketsE = new HashMap<String, Bucket<DTLink<ApproxStringLabel,ApproxStringLabel>>>();

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" in the root direction	
			for (DTGraph<ApproxStringLabel,ApproxStringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : graph.links()) {
					if (edge.tag().getSameAsPrev() < maxPrevNBH) {
						if (labelFreq.get(edge.tag().toString()) > minFreq) {
							if (!bucketsV.containsKey(edge.tag().toString())) {
								bucketsV.put(edge.tag().toString(), new Bucket<DTNode<ApproxStringLabel,ApproxStringLabel>>(edge.tag().toString()));
							}			
							bucketsV.get(edge.tag().toString()).getContents().add(edge.from());
						}
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : graph.nodes()) {
					if (vertex.label().getSameAsPrev() < maxPrevNBH) {
						if (labelFreq.get(vertex.label().toString()) > minFreq) {
							if (!bucketsE.containsKey(vertex.label().toString())) {
								bucketsE.put(vertex.label().toString(), new Bucket<DTLink<ApproxStringLabel,ApproxStringLabel>>(vertex.label().toString()));
							}
							bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksIn());
						}
					}
				}	
			}
		} else { // Labels "travel" in the fringe vertices direction
			for (DTGraph<ApproxStringLabel,ApproxStringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : graph.links()) {
					if (edge.tag().getSameAsPrev() < maxPrevNBH) {
						if (labelFreq.get(edge.tag().toString()) > minFreq) {
							if (!bucketsV.containsKey(edge.tag().toString())) {
								bucketsV.put(edge.tag().toString(), new Bucket<DTNode<ApproxStringLabel,ApproxStringLabel>>(edge.tag().toString()));
							}
							bucketsV.get(edge.tag().toString()).getContents().add(edge.to());
						}
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : graph.nodes()) {
					if (vertex.label().getSameAsPrev() < maxPrevNBH) {
						if (labelFreq.get(vertex.label().toString()) > minFreq) {
							if (!bucketsE.containsKey(vertex.label().toString())) {
								bucketsE.put(vertex.label().toString(), new Bucket<DTLink<ApproxStringLabel,ApproxStringLabel>>(vertex.label().toString()));
							}
							bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksOut());
						}
					}
				}	
			}
		}


		// Since the labels are not necessarily neatly from i to n+i, we sort them
		List<String> keysE = new ArrayList<String>(bucketsE.keySet());
		Collections.sort(keysE);
		List<String> keysV = new ArrayList<String>(bucketsV.keySet());
		Collections.sort(keysV);



		// We want the edge (predicate) sets as the new label, without the original node label, if noRoot == true
		for (DTGraph<ApproxStringLabel,ApproxStringLabel> graph : graphs) {
			for (DTNode<ApproxStringLabel,ApproxStringLabel> node : graph.nodes()) {
				if (labelFreq.get(node.label().toString()) <= minFreq) {
					node.label().clear();
				}
			}
			for (DTLink<ApproxStringLabel,ApproxStringLabel> link : graph.links()) {
				if (labelFreq.get(link.tag().toString()) <= minFreq) {
					link.tag().clear();
				}
			}
		}

		// 3. Relabel to the labels in the buckets
		String lab;
		for (String key : keysV) {	
			// Process vertices
			Bucket<DTNode<ApproxStringLabel,ApproxStringLabel>> bucketV = bucketsV.get(key);			
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : bucketV.getContents()) {
				if (!bucketV.getLabel().equals("")) { // should not concat with an empty label, else the purpose of the empty label is lost, because it implicitly gets meaning which it should not get
					lab = "_" + bucketV.getLabel();
					if (!vertex.label().getLastAdded().equals(lab) || vertex.label().getLastAddedCount() < maxLabelCard) { // we want sets, not multisets, if useSets == true
						vertex.label().append(lab);
					}
				}
			}
		}
		for (String key : keysE) {
			// Process edges
			Bucket<DTLink<ApproxStringLabel,ApproxStringLabel>> bucketE = bucketsE.get(key);			
			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : bucketE.getContents()) {
				if (!bucketE.getLabel().equals("")) { // should not concat with an empty label, else the purpose of the empty label is lost, because it implicitly gets meaning which it should not get
					edge.tag().append("_");
					edge.tag().append(bucketE.getLabel());
				}
			}
		}

		String label;
		for (DTGraph<ApproxStringLabel,ApproxStringLabel> graph : graphs) {
			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : graph.links()) {
				String nb = edge.tag().toString();
				if (nb.contains("_")) {
					nb = nb.substring(nb.indexOf("_"));
				} else {
					nb = "";
				}

				if (nb.equals(edge.tag().getPrevNBH())) {
					edge.tag().setSameAsPrev(edge.tag().getSameAsPrev() + 1);
				} else {
					edge.tag().setSameAsPrev(0);
				}

				edge.tag().setPrevNBH(nb);

				if (edge.tag().getSameAsPrev() == 0) {
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

			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : graph.nodes()) {
				String nb = vertex.label().toString();
				if (nb.contains("_")) {
					nb = nb.substring(nb.indexOf("_"));
				} else {
					nb = "";
				}

				if (nb.equals(vertex.label().getPrevNBH())) {
					vertex.label().setSameAsPrev(vertex.label().getSameAsPrev() + 1);
				} else {
					vertex.label().setSameAsPrev(0);
				}
	
				vertex.label().setPrevNBH(nb);

				if (vertex.label().getSameAsPrev() == 0) {
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
