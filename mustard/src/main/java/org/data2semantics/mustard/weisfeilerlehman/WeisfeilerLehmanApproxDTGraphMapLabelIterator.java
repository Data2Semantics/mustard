package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WeisfeilerLehmanApproxDTGraphMapLabelIterator extends WeisfeilerLehmanApproxIterator<DTGraph<MapLabel,MapLabel>, String> {
	private boolean reverse;
	private boolean trackPrevNBH;
	private double minFreq;
	private int maxLabelCard;

	public WeisfeilerLehmanApproxDTGraphMapLabelIterator(boolean reverse) {
		this(reverse, false, 1, 0.1);
	}

	public WeisfeilerLehmanApproxDTGraphMapLabelIterator(boolean reverse, boolean trackPrevNBH, int maxLabelCard, double minFreq) {
		super();
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
		this.minFreq = minFreq;
		this.maxLabelCard = maxLabelCard;
	}

	@Override
	public void wlInitialize(List<DTGraph<MapLabel, MapLabel>> graphs) {
		for (DTGraph<MapLabel, MapLabel> graph : graphs) {
			for (DTNode<MapLabel,MapLabel> node : graph.nodes()) {
				for (Integer k : node.label().keySet()) {
					String oldLab = node.label().get(k);
					String lab = labelDict.get(oldLab);

					if (lab == null) {
						lab = Integer.toString(labelDict.size());
						labelDict.put(oldLab, lab);
					}
					node.label().clear(k);
					node.label().append(k, lab);

					if (trackPrevNBH) {
						node.label().putPrevNBH(k, "");
					}

					//node.label().put(k, new StringBuilder(lab));
				}
			}
			for (DTLink<MapLabel,MapLabel> link : graph.links()) {
				for (Integer k : link.tag().keySet()) {
					String oldLab = link.tag().get(k);
					String lab = labelDict.get(oldLab);

					if (lab == null) {
						lab = Integer.toString(labelDict.size());
						labelDict.put(oldLab, lab);
					}
					link.tag().clear(k);
					link.tag().append(k, lab);

					if (trackPrevNBH) {
						link.tag().putPrevNBH(k, "");
					}		

					//link.tag().put(k, new StringBuilder(lab));
				}
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<MapLabel, MapLabel>> graphs, Map<String, Double> labelFreq) {
		Map<String, Bucket<VertexIndexPair>> bucketsV = new HashMap<String, Bucket<VertexIndexPair>>();
		Map<String, Bucket<EdgeIndexPair>> bucketsE   = new HashMap<String, Bucket<EdgeIndexPair>>();

		for (DTGraph<MapLabel,MapLabel> graph :graphs) {

			// 1. Fill buckets 
			if (reverse) { // Labels "travel" to the root node

				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
					// for each label we add a vertex-index-pair to the bucket
					for (int index : edge.tag().keySet()) {
						if (labelFreq.get(edge.tag().get(index)) >= minFreq) {
							if (!bucketsV.containsKey(edge.tag().get(index))) {
								bucketsV.put(edge.tag().get(index), new Bucket<VertexIndexPair>(edge.tag().get(index)));
							}					
							bucketsV.get(edge.tag().get(index)).getContents().add(new VertexIndexPair(edge.from(), index + 1));
						}
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {			
					for (int index : vertex.label().keySet()) {
						if (labelFreq.get(vertex.label().get(index)) >= minFreq) {
							for (DTLink<MapLabel,MapLabel> e2 : vertex.linksIn()) {
								if (e2.tag().containsKey(index)) {
									if (!bucketsE.containsKey(vertex.label().get(index))) {
										bucketsE.put(vertex.label().get(index), new Bucket<EdgeIndexPair>(vertex.label().get(index)));
									}
									bucketsE.get(vertex.label().get(index)).getContents().add(new EdgeIndexPair(e2, index));
								}
							}
						}
					}
				}

			} else { // Labels "travel" to the fringe nodes

				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
					// for each label we add a vertex-index-pair to the bucket
					for (int index : edge.tag().keySet()) {
						if (labelFreq.get(edge.tag().get(index)) >= minFreq) {
							if (!bucketsV.containsKey(edge.tag().get(index))) {
								bucketsV.put(edge.tag().get(index), new Bucket<VertexIndexPair>(edge.tag().get(index)));
							}
							bucketsV.get(edge.tag().get(index)).getContents().add(new VertexIndexPair(edge.to(), index));
						}
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {			
					for (int index : vertex.label().keySet()) {
						if (labelFreq.get(vertex.label().get(index)) >= minFreq) {
							if (index > 0) { // If index is 0 then we treat it as a fringe node, thus the label will not be propagated to the edges
								for (DTLink<MapLabel,MapLabel> e2 : vertex.linksOut()) {
									if (!bucketsE.containsKey(vertex.label().get(index))) {
										bucketsE.put(vertex.label().get(index), new Bucket<EdgeIndexPair>(vertex.label().get(index)));
									}
									bucketsE.get(vertex.label().get(index)).getContents().add(new EdgeIndexPair(e2, index - 1));
								}
							}
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


		for (DTGraph<MapLabel,MapLabel> graph : graphs) {
			for (DTNode<MapLabel,MapLabel> node : graph.nodes()) {
				for (int k : node.label().keySet()) {
					if (labelFreq.get(node.label().get(k)) < minFreq) {
						node.label().clear(k);
					}
				}
			}
			for (DTLink<MapLabel,MapLabel> link : graph.links()) {
				for (int k : link.tag().keySet()) {
					if (labelFreq.get(link.tag().get(k)) < minFreq) {
						link.tag().clear(k);
					}
				}
			}
		}

		// 3. Relabel to the labels in the buckets
		for (String keyV : keysV) {
			// Process vertices
			Bucket<VertexIndexPair> bucketV = bucketsV.get(keyV);			
			for (VertexIndexPair vp : bucketV.getContents()) {
				if (!vp.getVertex().label().getLastAdded(vp.getIndex()).equals(bucketV.getLabel()) || vp.getVertex().label().getLastAddedCount(vp.getIndex()) < maxLabelCard) {
					vp.getVertex().label().append(vp.getIndex(), "_");
					vp.getVertex().label().append(vp.getIndex(), bucketV.getLabel());				
				}
			}
		}
		for (String keyE : keysE) {
			// Process edges
			Bucket<EdgeIndexPair> bucketE = bucketsE.get(keyE);			
			for (EdgeIndexPair ep : bucketE.getContents()) {
				ep.getEdge().tag().append(ep.getIndex(), "_");
				ep.getEdge().tag().append(ep.getIndex(), bucketE.getLabel());			
			}
		}


		String label;
		for (DTGraph<MapLabel,MapLabel> graph : graphs) {		
			for (DTLink<MapLabel,MapLabel> edge : graph.links()) {						
				for (int i : edge.tag().keySet()) {
					if (trackPrevNBH) {
						String nb = edge.tag().get(i);
						if (nb.contains("_")) {
							nb = nb.substring(nb.indexOf("_"));
						} else {
							nb = "";
						}

						if (nb.equals(edge.tag().getPrevNBH(i))) {
							edge.tag().putSameAsPrev(i,true);
						}
						edge.tag().putPrevNBH(i,nb);
					}
					if (!edge.tag().getSameAsPrev(i)) {
						label = labelDict.get(edge.tag().get(i));						
						if (label == null) {					
							label = Integer.toString(labelDict.size());
							labelDict.put(edge.tag().get(i), label);				
						}
						edge.tag().clear(i);
						edge.tag().append(i,label);
					}
				}
			}

			for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {
				for (int i : vertex.label().keySet()) {
					if (trackPrevNBH) {
						String nb = vertex.label().get(i);
						if (nb.contains("_")) {
							nb = nb.substring(nb.indexOf("_"));
						} else {
							nb = "";
						}

						if (nb.equals(vertex.label().getPrevNBH(i))) {
							vertex.label().putSameAsPrev(i,true);
						}
						vertex.label().putPrevNBH(i,nb);
					}				
					if (!vertex.label().getSameAsPrev(i)) {
						label = labelDict.get(vertex.label().get(i));
						if (label == null) {
							label = Integer.toString(labelDict.size());
							labelDict.put(vertex.label().get(i), label);
						}
						vertex.label().clear(i);
						vertex.label().append(i, label);
					}
				}
			}
		}
	}



	private class VertexIndexPair {
		private DTNode<MapLabel,MapLabel> vertex;
		private int index;

		public VertexIndexPair(DTNode<MapLabel,MapLabel> vertex, int index) {
			this.vertex = vertex;
			this.index = index;
		}

		public DTNode<MapLabel,MapLabel> getVertex() {
			return vertex;
		}
		public int getIndex() {
			return index;
		}		
	}

	private class EdgeIndexPair {
		private DTLink<MapLabel,MapLabel> edge;
		private int index;

		public EdgeIndexPair(DTLink<MapLabel,MapLabel> edge, int index) {
			this.edge = edge;
			this.index = index;
		}

		public DTLink<MapLabel,MapLabel> getEdge() {
			return edge;
		}
		public int getIndex() {
			return index;
		}		
	}
}
