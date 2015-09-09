package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.weisfeilerlehman.MapLabel;
import org.data2semantics.mustard.weisfeilerlehman.WLUtils;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphMapLabelIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * 
 * Root version (i.e. only walks that start in the root node are counted) of {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel}.
 * 
 * Simple implementation of this idea, not necessarily the fastest.
 * 
 * @author Gerben
 *
 */
public class DTGraphRootWLSubTreeKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> , FeatureInspector {

	private Map<DTNode<MapLabel,MapLabel>, Map<DTNode<MapLabel,MapLabel>, Integer>> instanceVertexIndexMap;
	private Map<DTNode<MapLabel,MapLabel>, Map<DTLink<MapLabel,MapLabel>, Integer>> instanceEdgeIndexMap;

	private DTGraph<MapLabel,MapLabel> rdfGraph;
	private List<DTNode<MapLabel,MapLabel>> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;
	
	private Map<String, String> dict;


	public DTGraphRootWLSubTreeKernel(int iterations, boolean normalize) {		
		this.depth = (int) Math.round(iterations / 2.0);
		this.iterations = iterations;
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		instanceVertices = new ArrayList<DTNode<MapLabel,MapLabel>>();
		this.instanceVertexIndexMap = new HashMap<DTNode<MapLabel,MapLabel>, Map<DTNode<MapLabel,MapLabel>, Integer>>();
		this.instanceEdgeIndexMap = new HashMap<DTNode<MapLabel,MapLabel>, Map<DTLink<MapLabel,MapLabel>, Integer>>();		
		
		SparseVector[] featureVectors = new SparseVector[data.getInstances().size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		init(data.getGraph(), data.getInstances());
		WeisfeilerLehmanIterator<DTGraph<MapLabel,MapLabel>> wl = new WeisfeilerLehmanDTGraphMapLabelIterator(true, false);

		List<DTGraph<MapLabel,MapLabel>> gList = new ArrayList<DTGraph<MapLabel,MapLabel>>();
		gList.add(rdfGraph);

		wl.wlInitialize(gList);

		double weight = 1.0;

		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);

		for (int i = 0; i < iterations; i++) {
			wl.wlIterate(gList);
			computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);
		}
		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		
		// Set the reverse label dict, to reverse engineer the features
		dict = new HashMap<String,String>();
		for (String key : wl.getLabelDict().keySet()) {
			dict.put(wl.getLabelDict().get(key), key);
		}

		return featureVectors;
	}


	public double[][] compute(SingleDTGraph data) {
		SparseVector[] featureVectors = computeFeatureVectors(data);
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		kernel = KernelUtils.computeKernelMatrix(featureVectors, kernel);
		return kernel;
	}



	private void init(DTGraph<String,String> graph, List<DTNode<String,String>> instances) {
		DTNode<MapLabel,MapLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<DTNode<MapLabel,MapLabel>, Integer> vertexIndexMap;
		Map<DTLink<MapLabel,MapLabel>, Integer> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<MapLabel,MapLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<MapLabel,MapLabel>>();
		Map<DTLink<String,String>, DTLink<MapLabel,MapLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<MapLabel,MapLabel>>();

		rdfGraph = new LightDTGraph<MapLabel,MapLabel>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new HashMap<DTNode<MapLabel,MapLabel>, Integer>();
			edgeIndexMap   = new HashMap<DTLink<MapLabel,MapLabel>, Integer>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new MapLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().put(depth, new StringBuilder(oldStartV.label()));
			instanceVertices.add(startV);

			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);

			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.put(startV, depth);

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph
							if (!vertexIndexMap.containsKey(vOldNewMap.get(edge.to()))) { // we have not seen it for this instance
								vertexIndexMap.put(vOldNewMap.get(edge.to()), j);
							}
							vOldNewMap.get(edge.to()).label().put(j, new StringBuilder(edge.to().label())); // However, we should always include it in the graph at depth j
						} else {
							DTNode<MapLabel,MapLabel> newN = rdfGraph.add(new MapLabel());
							newN.label().put(j, new StringBuilder(edge.to().label()));
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.put(newN, j);
						}

						if (eOldNewMap.containsKey(edge)) {
							// Process the edge, if we haven't seen it before
							if (!edgeIndexMap.containsKey(eOldNewMap.get(edge))) { // see comment for vertices
								edgeIndexMap.put(eOldNewMap.get(edge), j);
							}
							eOldNewMap.get(edge).tag().put(j, new StringBuilder(edge.tag()));
						} else {
							DTLink<MapLabel,MapLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new MapLabel());
							newE.tag().put(j, new StringBuilder(edge.tag()));
							eOldNewMap.put(edge, newE);
							edgeIndexMap.put(newE, j);
						}

						// Add the vertex to the new front, if we go into a new round
						if (j > 0) {
							newFrontV.add(edge.to());
						}
					}
				}
				frontV = newFrontV;
			}
		}		
	}





	/**
	 * The computation of the feature vectors assumes that each edge and vertex is only processed once. We can encounter the same
	 * vertex/edge on different depths during computation, this could lead to multiple counts of the same vertex, possibly of different
	 * depth labels.
	 * 
	 * @param graph
	 * @param instances
	 * @param weight
	 * @param featureVectors
	 */
	private void computeFVs(DTGraph<MapLabel,MapLabel> graph, List<DTNode<MapLabel,MapLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex) {
		int index;
		Map<DTNode<MapLabel,MapLabel>, Integer> vertexIndexMap;
		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (DTNode<MapLabel,MapLabel> vertex : vertexIndexMap.keySet()) {
			if (vertexIndexMap.get(vertex) == depth) {
					index = Integer.parseInt(vertex.label().get(vertexIndexMap.get(vertex)).toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
		}
	}


	public List<String> getFeatureDescriptions(List<Integer> indicesSV) {
		if (dict == null) {
			throw new RuntimeException("Should run computeFeatureVectors first");
		} else {
			List<String> desc = new ArrayList<String>();
			
			for (int index : indicesSV) {
				desc.add(WLUtils.getFeatureDecription(dict, index));
			}
			return desc;
		}
	}
	
	
}
