package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WeisfeilerLehmanEdgeSetsDTGraphIterator extends WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> {
	private boolean reverse;
	private boolean trackPrevNBH;
	private double minFrac;
	private boolean noRoot;
	private boolean useSets;

	public WeisfeilerLehmanEdgeSetsDTGraphIterator(boolean reverse) {
		this(reverse, false, true, true, 0.1);
	}

	public WeisfeilerLehmanEdgeSetsDTGraphIterator(boolean reverse, boolean trackPrevNBH, boolean noRoot, boolean useSets, double minFreq) {
		super();
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
		this.noRoot = noRoot;
		this.useSets = useSets;
		this.minFrac = minFreq;
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

				if (trackPrevNBH) {
					node.label().setPrevNBH("");
				}

			}
			for (DTLink<StringLabel,StringLabel> link : graph.links()) {
				String lab = labelDict.get(link.tag().toString());
				if (lab == null) {
					lab = Integer.toString(labelDict.size());
					labelDict.put(link.tag().toString(), lab);
				}
				link.tag().clear();
				link.tag().append(lab);

				if (trackPrevNBH) {
					link.tag().setPrevNBH("");
				}
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<StringLabel, StringLabel>> graphs) {
		// 0.5 Count the labels
		int minFreq = (int) Math.round(minFrac * graphs.size());
		Map<String, Integer> labelFreq = new HashMap<String, Integer>();

		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				String lab = edge.tag().toString();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				}
				labelFreq.put(lab, labelFreq.get(lab) + 1);
			}
			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				String lab = vertex.label().toString();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				}
				labelFreq.put(lab, labelFreq.get(lab) + 1);

			}
		}



		Map<String, Bucket<DTNode<StringLabel,StringLabel>>> bucketsV = new HashMap<String, Bucket<DTNode<StringLabel,StringLabel>>>();
		Map<String, Bucket<DTLink<StringLabel,StringLabel>>> bucketsE = new HashMap<String, Bucket<DTLink<StringLabel,StringLabel>>>();

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" in the root direction	
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					if (labelFreq.get(edge.tag().toString()) >= minFreq) {
						if (!bucketsV.containsKey(edge.tag().toString())) {
							bucketsV.put(edge.tag().toString(), new Bucket<DTNode<StringLabel,StringLabel>>(edge.tag().toString()));
						}			
						bucketsV.get(edge.tag().toString()).getContents().add(edge.from());
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
					if (labelFreq.get(vertex.label().toString()) >= minFreq) {
						if (!bucketsE.containsKey(vertex.label().toString())) {
							bucketsE.put(vertex.label().toString(), new Bucket<DTLink<StringLabel,StringLabel>>(vertex.label().toString()));
						}
						bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksIn());
					}
				}	
			}
		} else { // Labels "travel" in the fringe vertices direction
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					if (labelFreq.get(edge.tag().toString()) >= minFreq) {
						if (!bucketsV.containsKey(edge.tag().toString())) {
							bucketsV.put(edge.tag().toString(), new Bucket<DTNode<StringLabel,StringLabel>>(edge.tag().toString()));
						}
						bucketsV.get(edge.tag().toString()).getContents().add(edge.to());
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
					if (labelFreq.get(vertex.label().toString()) >= minFreq) {
						if (!bucketsE.containsKey(vertex.label().toString())) {
							bucketsE.put(vertex.label().toString(), new Bucket<DTLink<StringLabel,StringLabel>>(vertex.label().toString()));
						}
						bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksOut());
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
		if (noRoot) {
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				for (DTNode<StringLabel,StringLabel> node : graph.nodes()) {
					node.label().clear();
				}
				for (DTLink<StringLabel,StringLabel> link : graph.links()) {
					link.tag().clear();
				}
			}
		}



		// 3. Relabel to the labels in the buckets
		for (String key : keysV) {	
			// Process vertices
			Bucket<DTNode<StringLabel,StringLabel>> bucketV = bucketsV.get(key);			
			for (DTNode<StringLabel,StringLabel> vertex : bucketV.getContents()) {
				if (!useSets || !vertex.label().getLastAdded().equals(bucketV.getLabel())) { // we want sets, not multisets, if useSets == true
					vertex.label().append("_");
					vertex.label().append(bucketV.getLabel());
				}
			}
		}
		for (String key : keysE) {
			// Process edges
			Bucket<DTLink<StringLabel,StringLabel>> bucketE = bucketsE.get(key);			
			for (DTLink<StringLabel,StringLabel> edge : bucketE.getContents()) {
				edge.tag().append("_");
				edge.tag().append(bucketE.getLabel());
			}
		}

		String label;
		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				if (trackPrevNBH) {
					String nb = edge.tag().toString();
					if (nb.contains("_")) {
						nb = nb.substring(nb.indexOf("_"));
					} else {
						nb = "";
					}

					if (nb.equals(edge.tag().getPrevNBH())) {
						edge.tag().setSameAsPrev(true);
					}
					edge.tag().setPrevNBH(nb);
				}

				if (!edge.tag().isSameAsPrev()) {
					label = labelDict.get(edge.tag().toString());						
					if (label == null) {					
						label = Integer.toString(labelDict.size());
						labelDict.put(edge.tag().toString(), label);				
					}
					edge.tag().clear();
					edge.tag().append(label);
				}
			}

			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				if (trackPrevNBH) {
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
				}

				if (!vertex.label().isSameAsPrev()) {
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

	public double getMinFrac() {
		return minFrac;
	}

	public void setMinFrac(double minFrac) {
		this.minFrac = minFrac;
	}
}
