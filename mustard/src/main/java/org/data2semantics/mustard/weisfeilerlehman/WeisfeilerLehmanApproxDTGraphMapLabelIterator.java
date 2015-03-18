package org.data2semantics.mustard.weisfeilerlehman;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;

public class WeisfeilerLehmanApproxDTGraphMapLabelIterator extends WeisfeilerLehmanApproxIterator<DTGraph<ApproxMapLabel,ApproxMapLabel>, String> {
	private boolean reverse;

	public WeisfeilerLehmanApproxDTGraphMapLabelIterator(boolean reverse) {
		this(reverse, 1, 1, 1);
	}

	public WeisfeilerLehmanApproxDTGraphMapLabelIterator(boolean reverse, int maxPrevNBH, int maxLabelCard, int minFreq) {
		super(maxPrevNBH, maxLabelCard, minFreq);
		this.reverse = reverse;

	}

	@Override
	public void wlInitialize(List<DTGraph<ApproxMapLabel, ApproxMapLabel>> graphs) {
		for (DTGraph<ApproxMapLabel, ApproxMapLabel> graph : graphs) {
			for (DTNode<ApproxMapLabel,ApproxMapLabel> node : graph.nodes()) {
				for (Integer k : node.label().keySet()) {
					String oldLab = node.label().get(k);
					String lab = labelDict.get(oldLab);

					if (lab == null) {
						lab = Integer.toString(labelDict.size());
						labelDict.put(oldLab, lab);
					}
					node.label().clear(k);
					node.label().append(k, lab);

					node.label().putPrevNBH(k, "");
					node.label().putSameAsPrev(k, 0);
				}
			}
			for (DTLink<ApproxMapLabel,ApproxMapLabel> link : graph.links()) {
				for (Integer k : link.tag().keySet()) {
					String oldLab = link.tag().get(k);
					String lab = labelDict.get(oldLab);

					if (lab == null) {
						lab = Integer.toString(labelDict.size());
						labelDict.put(oldLab, lab);
					}
					link.tag().clear(k);
					link.tag().append(k, lab);

					link.tag().putPrevNBH(k, "");
					link.tag().putSameAsPrev(k, 0);

				}
			}
		}
	}

	@Override
	public void wlIterate(List<DTGraph<ApproxMapLabel, ApproxMapLabel>> graphs, Map<String, Integer> labelFreq) {
		Map<String, Bucket<VertexIndexPair>> bucketsV = new HashMap<String, Bucket<VertexIndexPair>>();
		Map<String, Bucket<EdgeIndexPair>> bucketsE   = new HashMap<String, Bucket<EdgeIndexPair>>();

		for (DTGraph<ApproxMapLabel,ApproxMapLabel> graph : graphs) {

			// 1. Fill buckets 
			if (reverse) { // Labels "travel" to the root node

				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<ApproxMapLabel,ApproxMapLabel> edge : graph.links()) {
					// for each label we add a vertex-index-pair to the bucket
					for (int index : edge.tag().keySet()) {
						if (edge.tag().getSameAsPrev(index) < maxPrevNBH) {
							if (labelFreq.get(edge.tag().get(index)) > minFreq) {
								if (!bucketsV.containsKey(edge.tag().get(index))) {
									bucketsV.put(edge.tag().get(index), new Bucket<VertexIndexPair>(edge.tag().get(index)));
								}					
								bucketsV.get(edge.tag().get(index)).getContents().add(new VertexIndexPair(edge.from(), index + 1));
							}
						}
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<ApproxMapLabel,ApproxMapLabel> vertex : graph.nodes()) {			
					for (int index : vertex.label().keySet()) {
						if (vertex.label().getSameAsPrev(index) < maxPrevNBH) {
							if (labelFreq.get(vertex.label().get(index)) > minFreq) {
								for (DTLink<ApproxMapLabel,ApproxMapLabel> e2 : vertex.linksIn()) {
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
				}

			} else { // Labels "travel" to the fringe nodes

				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<ApproxMapLabel,ApproxMapLabel> edge : graph.links()) {
					// for each label we add a vertex-index-pair to the bucket
					for (int index : edge.tag().keySet()) {
						if (edge.tag().getSameAsPrev(index) < maxPrevNBH) {
							if (labelFreq.get(edge.tag().get(index)) > minFreq) {
								if (!bucketsV.containsKey(edge.tag().get(index))) {
									bucketsV.put(edge.tag().get(index), new Bucket<VertexIndexPair>(edge.tag().get(index)));
								}
								bucketsV.get(edge.tag().get(index)).getContents().add(new VertexIndexPair(edge.to(), index));
							}
						}
					}
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<ApproxMapLabel,ApproxMapLabel> vertex : graph.nodes()) {			
					for (int index : vertex.label().keySet()) {
						if (vertex.label().getSameAsPrev(index) < maxPrevNBH) {
							if (labelFreq.get(vertex.label().get(index)) > minFreq) {
								if (index > 0) { // If index is 0 then we treat it as a fringe node, thus the label will not be propagated to the edges
									for (DTLink<ApproxMapLabel,ApproxMapLabel> e2 : vertex.linksOut()) {
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
		}

		// Since the labels are not necessarily neatly from i to n+i, we sort them
		List<String> keysE = new ArrayList<String>(bucketsE.keySet());
		Collections.sort(keysE);
		List<String> keysV = new ArrayList<String>(bucketsV.keySet());
		Collections.sort(keysV);


		for (DTGraph<ApproxMapLabel,ApproxMapLabel> graph : graphs) {
			for (DTNode<ApproxMapLabel,ApproxMapLabel> node : graph.nodes()) {
				for (int k : node.label().keySet()) {
					if (labelFreq.get(node.label().get(k)) <= minFreq) {
						node.label().clear(k);
					}
				}
			}
			for (DTLink<ApproxMapLabel,ApproxMapLabel> link : graph.links()) {
				for (int k : link.tag().keySet()) {
					if (labelFreq.get(link.tag().get(k)) <= minFreq) {
						link.tag().clear(k);
					}
				}
			}
		}

		// 3. Relabel to the labels in the buckets
		String lab;
		for (String keyV : keysV) {
			// Process vertices
			Bucket<VertexIndexPair> bucketV = bucketsV.get(keyV);			
			for (VertexIndexPair vp : bucketV.getContents()) {
				if (!bucketV.getLabel().equals("")) { // should not concat with an empty label, else the purpose of the empty label is lost, because it implicitly gets meaning which it should not get
					lab = "_" + bucketV.getLabel();
					if (!vp.getVertex().label().getLastAdded(vp.getIndex()).equals(lab) || vp.getVertex().label().getLastAddedCount(vp.getIndex()) < maxLabelCard) {
						vp.getVertex().label().append(vp.getIndex(), lab);		 // use 1 string, so that lastadded works		
					}
				}
			}
		}
		for (String keyE : keysE) {
			// Process edges
			Bucket<EdgeIndexPair> bucketE = bucketsE.get(keyE);			
			for (EdgeIndexPair ep : bucketE.getContents()) {
				if (!bucketE.getLabel().equals("")) { // should not concat with an empty label, else the purpose of the empty label is lost, because it implicitly gets meaning which it should not get
					ep.getEdge().tag().append(ep.getIndex(), "_");
					ep.getEdge().tag().append(ep.getIndex(), bucketE.getLabel());
				}
			}
		}


		String label;
		for (DTGraph<ApproxMapLabel,ApproxMapLabel> graph : graphs) {		
			for (DTLink<ApproxMapLabel,ApproxMapLabel> edge : graph.links()) {						
				for (int i : edge.tag().keySet()) {
					String nb = edge.tag().get(i);
					if (nb.contains("_")) {
						nb = nb.substring(nb.indexOf("_"));
					} else {
						nb = "";
					}

					if (nb.equals(edge.tag().getPrevNBH(i))) {
						edge.tag().putSameAsPrev(i,edge.tag().getSameAsPrev(i) + 1);
					}
					else {
						edge.tag().putSameAsPrev(i,0);
					}

					edge.tag().putPrevNBH(i,nb);

					if (edge.tag().getSameAsPrev(i) == 0) {
						label = labelDict.get(edge.tag().get(i));						
						if (label == null) {					
							label = Integer.toString(labelDict.size());
							labelDict.put(edge.tag().get(i), label);				
						}
						edge.tag().clear(i);
						edge.tag().append(i,label);
					} else { // retain old label
						String old = edge.tag().get(i);
						if (old.contains("_")) {
							old = old.substring(0, old.indexOf("_"));
							edge.tag().clear(i);
							edge.tag().append(i, old);
						} 
					}
				}
			}

			for (DTNode<ApproxMapLabel,ApproxMapLabel> vertex : graph.nodes()) {
				for (int i : vertex.label().keySet()) {
					String nb = vertex.label().get(i);
					if (nb.contains("_")) {
						nb = nb.substring(nb.indexOf("_"));
					} else {
						nb = "";
					}

					if (nb.equals(vertex.label().getPrevNBH(i))) {
						vertex.label().putSameAsPrev(i,vertex.label().getSameAsPrev(i) + 1);
					}
					else {
						vertex.label().putSameAsPrev(i,0);
					}

					vertex.label().putPrevNBH(i,nb);

					if (vertex.label().getSameAsPrev(i) == 0) {
						label = labelDict.get(vertex.label().get(i));
						if (label == null) {
							label = Integer.toString(labelDict.size());
							labelDict.put(vertex.label().get(i), label);
						}
						vertex.label().clear(i);
						vertex.label().append(i, label);
					} else { // retain old label
						String old = vertex.label().get(i);
						if (old.contains("_")) {
							old = old.substring(0, old.indexOf("_"));
							vertex.label().clear(i);
							vertex.label().append(i, old);
						} 
					}
				}
			}
		}
	}



	private class VertexIndexPair {
		private DTNode<ApproxMapLabel,ApproxMapLabel> vertex;
		private int index;

		public VertexIndexPair(DTNode<ApproxMapLabel,ApproxMapLabel> vertex, int index) {
			this.vertex = vertex;
			this.index = index;
		}

		public DTNode<ApproxMapLabel,ApproxMapLabel> getVertex() {
			return vertex;
		}
		public int getIndex() {
			return index;
		}		
	}

	private class EdgeIndexPair {
		private DTLink<ApproxMapLabel,ApproxMapLabel> edge;
		private int index;

		public EdgeIndexPair(DTLink<ApproxMapLabel,ApproxMapLabel> edge, int index) {
			this.edge = edge;
			this.index = index;
		}

		public DTLink<ApproxMapLabel,ApproxMapLabel> getEdge() {
			return edge;
		}
		public int getIndex() {
			return index;
		}		
	}
}
