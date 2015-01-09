package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WeisfeilerLehmanDTGraphMapLabelIterator extends WeisfeilerLehmanIterator<DTGraph<MapLabel,MapLabel>> {
	private boolean reverse;
	private boolean trackPrevNBH;

	public WeisfeilerLehmanDTGraphMapLabelIterator(boolean reverse) {
		this(reverse, false);
	}

	public WeisfeilerLehmanDTGraphMapLabelIterator(boolean reverse, boolean trackPrevNBH) {
		super();
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
	}

	@Override
	public void wlInitialize(List<DTGraph<MapLabel, MapLabel>> graphs) {
		for (DTGraph<MapLabel, MapLabel> graph : graphs) {
			for (DTNode<MapLabel,MapLabel> node : graph.nodes()) {
				for (Integer k : node.label().keySet()) {
					String oldLab = node.label().get(k).toString();
					String lab = labelDict.get(oldLab);

					if (lab == null) {
						lab = Integer.toString(labelDict.size());
						labelDict.put(oldLab, lab);
					}
					node.label().clear(k);
					node.label().get(k).append(lab);
					
					if (trackPrevNBH) {
						node.label().putPrevNBH(k, "");
					}
					
					//node.label().put(k, new StringBuilder(lab));
				}
			}
			for (DTLink<MapLabel,MapLabel> link : graph.links()) {
				for (Integer k : link.tag().keySet()) {
					String oldLab = link.tag().get(k).toString();
					String lab = labelDict.get(oldLab);

					if (lab == null) {
						lab = Integer.toString(labelDict.size());
						labelDict.put(oldLab, lab);
					}
					link.tag().clear(k);
					link.tag().get(k).append(lab);
					
					if (trackPrevNBH) {
						link.tag().putPrevNBH(k, "");
					}		
					
					//link.tag().put(k, new StringBuilder(lab));
				}
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<MapLabel, MapLabel>> graphs) {
		Map<String, Bucket<VertexIndexPair>> bucketsV = new HashMap<String, Bucket<VertexIndexPair>>();
		Map<String, Bucket<EdgeIndexPair>> bucketsE   = new HashMap<String, Bucket<EdgeIndexPair>>();

		for (DTGraph<MapLabel,MapLabel> graph :graphs) {

			// 1. Fill buckets 
			if (reverse) { // Labels "travel" to the root node

				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
					// for each label we add a vertex-index-pair to the bucket
					for (int index : edge.tag().keySet()) {
						if (!bucketsV.containsKey(edge.tag().get(index).toString())) {
							bucketsV.put(edge.tag().get(index).toString(), new Bucket<VertexIndexPair>(edge.tag().get(index).toString()));
						}					
						bucketsV.get(edge.tag().get(index).toString()).getContents().add(new VertexIndexPair(edge.from(), index + 1));
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {			
					for (int index : vertex.label().keySet()) {
						for (DTLink<MapLabel,MapLabel> e2 : vertex.linksIn()) {
							if (e2.tag().containsKey(index)) {
								if (!bucketsE.containsKey(vertex.label().get(index).toString())) {
									bucketsE.put(vertex.label().get(index).toString(), new Bucket<EdgeIndexPair>(vertex.label().get(index).toString()));
								}
								bucketsE.get(vertex.label().get(index).toString()).getContents().add(new EdgeIndexPair(e2, index));
							}
						}
					}
				}

			} else { // Labels "travel" to the fringe nodes

				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
					// for each label we add a vertex-index-pair to the bucket
					for (int index : edge.tag().keySet()) {
						if (!bucketsV.containsKey(edge.tag().get(index).toString())) {
							bucketsV.put(edge.tag().get(index).toString(), new Bucket<VertexIndexPair>(edge.tag().get(index).toString()));
						}
						bucketsV.get(edge.tag().get(index).toString()).getContents().add(new VertexIndexPair(edge.to(), index));
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {			
					for (int index : vertex.label().keySet()) {
						if (index > 0) { // If index is 0 then we treat it as a fringe node, thus the label will not be propagated to the edges
							for (DTLink<MapLabel,MapLabel> e2 : vertex.linksOut()) {
								if (!bucketsE.containsKey(vertex.label().get(index).toString())) {
									bucketsE.put(vertex.label().get(index).toString(), new Bucket<EdgeIndexPair>(vertex.label().get(index).toString()));
								}
								bucketsE.get(vertex.label().get(index).toString()).getContents().add(new EdgeIndexPair(e2, index - 1));
							}
						}
					}
				}
			}

			// 2. add bucket labels to existing labels
			// Change the original label to a prefix label

			/*
			for (DTLink<MapLabel,MapLabel> edge : graph.links()) {
				for (int i : edge.tag().keySet()) {
					edge.tag().get(i).append("_");   
				}

			}
			for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {
				for (int i : vertex.label().keySet()) {
					vertex.label().get(i).append("_"); 
				}
			}
			 */

			// Since the labels are not necessarily neatly from i to n+i, we sort them
			List<String> keysE = new ArrayList<String>(bucketsE.keySet());
			Collections.sort(keysE);
			List<String> keysV = new ArrayList<String>(bucketsV.keySet());
			Collections.sort(keysV);

			// 3. Relabel to the labels in the buckets
			for (String keyV : keysV) {
				// Process vertices
				Bucket<VertexIndexPair> bucketV = bucketsV.get(keyV);			
				for (VertexIndexPair vp : bucketV.getContents()) {
					vp.getVertex().label().get(vp.getIndex()).append("_");
					vp.getVertex().label().get(vp.getIndex()).append(bucketV.getLabel());				
				}
			}
			for (String keyE : keysE) {
				// Process edges
				Bucket<EdgeIndexPair> bucketE = bucketsE.get(keyE);			
				for (EdgeIndexPair ep : bucketE.getContents()) {
					ep.getEdge().tag().get(ep.getIndex()).append("_");
					ep.getEdge().tag().get(ep.getIndex()).append(bucketE.getLabel());			
				}
			}
		}

		String label;
		for (DTGraph<MapLabel,MapLabel> graph : graphs) {		
			for (DTLink<MapLabel,MapLabel> edge : graph.links()) {						
				for (int i : edge.tag().keySet()) {
					if (trackPrevNBH) {
						String nb = edge.tag().get(i).toString();
						nb = nb.substring(nb.indexOf("_"));

						if (nb.equals(edge.tag().getPrevNBH(i))) {
							edge.tag().putSameAsPrev(i,true);
						}
						edge.tag().putPrevNBH(i,nb);
					}
					if (!edge.tag().getSameAsPrev(i)) {
						label = labelDict.get(edge.tag().get(i).toString());						
						if (label == null) {					
							label = Integer.toString(labelDict.size());
							labelDict.put(edge.tag().get(i).toString(), label);				
						}
						edge.tag().clear(i);
						edge.tag().get(i).append(label);
						//edge.tag().put(i, new StringBuilder(label));
					}
				}
			}

			for (DTNode<MapLabel,MapLabel> vertex : graph.nodes()) {
				for (int i : vertex.label().keySet()) {
					if (trackPrevNBH) {
						String nb = vertex.label().get(i).toString();
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
						label = labelDict.get(vertex.label().get(i).toString());
						if (label == null) {
							label = Integer.toString(labelDict.size());
							labelDict.put(vertex.label().get(i).toString(), label);
						}
						vertex.label().clear(i);
						vertex.label().get(i).append(label);
						//vertex.label().put(i, new StringBuilder(label));
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
