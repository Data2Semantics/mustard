package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WLUtils;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * Like {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel} this kernel implements the WL algorithm on a single graph. When noDuplicateSubtrees = true, this 
 * implementation is faster and equal. When false the results differ slightly. 
 *
 * 
 * @author Gerben
 *
 */
public class DTGraphWLSubTreeIDEQKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker, FeatureInspector {

	private Map<DTNode<StringLabel,StringLabel>, Map<DTNode<StringLabel,StringLabel>, Integer>> instanceVertexIndexMap;
	private Map<DTNode<StringLabel,StringLabel>, Map<DTLink<StringLabel,StringLabel>, Integer>> instanceEdgeIndexMap;

	private DTGraph<StringLabel,StringLabel> rdfGraph;
	private List<DTNode<StringLabel,StringLabel>> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean reverse;
	private boolean noDuplicateSubtrees;

	private long compTime;
	private Map<String,String> dict;


	public DTGraphWLSubTreeIDEQKernel(int iterations, int depth, boolean reverse, boolean noDuplicateSubtrees, boolean normalize) {
		this.reverse = reverse;
		this.noDuplicateSubtrees = noDuplicateSubtrees;	
		this.normalize = normalize;
		this.depth = depth;
		this.iterations = iterations;
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}



	public long getComputationTime() {
		return compTime;
	}


	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {		

		SparseVector[] featureVectors = new SparseVector[data.getInstances().size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		init(data.getGraph(), data.getInstances());
		WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> wl = new WeisfeilerLehmanDTGraphIterator(reverse, noDuplicateSubtrees);

		List<DTGraph<StringLabel,StringLabel>> gList = new ArrayList<DTGraph<StringLabel,StringLabel>>();
		gList.add(rdfGraph);

		long tic = System.currentTimeMillis();

		wl.wlInitialize(gList);

		double weight = 1.0;

		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, 0);

		for (int i = 0; i < iterations; i++) {
			wl.wlIterate(gList);
			computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, i + 1);
		}

		compTime = System.currentTimeMillis() - tic;

		// Set the reverse label dict, to reverse engineer the features
		dict = new HashMap<String,String>();
		for (String key : wl.getLabelDict().keySet()) {
			dict.put(wl.getLabelDict().get(key), key);
		}

		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}


	public double[][] compute(SingleDTGraph data) {
		SparseVector[] featureVectors = computeFeatureVectors(data);
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		long tic = System.currentTimeMillis();
		kernel = KernelUtils.computeKernelMatrix(featureVectors, kernel);
		compTime += System.currentTimeMillis() - tic;
		return kernel;
	}



	private void init(DTGraph<String,String> graph, List<DTNode<String,String>> instances) {
		DTNode<StringLabel,StringLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<DTNode<StringLabel,StringLabel>, Integer> vertexIndexMap;
		Map<DTLink<StringLabel,StringLabel>, Integer> edgeIndexMap;
		Map<DTNode<StringLabel,StringLabel>, Boolean> vertexIgnoreMap;
		Map<DTLink<StringLabel,StringLabel>, Boolean> edgeIgnoreMap;
		Map<DTNode<String,String>, DTNode<StringLabel,StringLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<StringLabel,StringLabel>>();
		Map<DTLink<String,String>, DTLink<StringLabel,StringLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<StringLabel,StringLabel>>();

		rdfGraph = new LightDTGraph<StringLabel,StringLabel>();
		instanceVertices        = new ArrayList<DTNode<StringLabel,StringLabel>>();
		instanceVertexIndexMap  = new HashMap<DTNode<StringLabel,StringLabel>, Map<DTNode<StringLabel,StringLabel>, Integer>>();
		instanceEdgeIndexMap    = new HashMap<DTNode<StringLabel,StringLabel>, Map<DTLink<StringLabel,StringLabel>, Integer>>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new HashMap<DTNode<StringLabel,StringLabel>, Integer>();
			edgeIndexMap   = new HashMap<DTLink<StringLabel,StringLabel>, Integer>();
			vertexIgnoreMap = new HashMap<DTNode<StringLabel,StringLabel>, Boolean>();
			edgeIgnoreMap   = new HashMap<DTLink<StringLabel,StringLabel>, Boolean>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new StringLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().clear();
			startV.label().append(oldStartV.label());

			instanceVertices.add(startV);

			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);
		
			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.put(startV, depth);
			vertexIgnoreMap.put(startV, false);

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph						
							if (!vertexIndexMap.containsKey(vOldNewMap.get(edge.to())) || !reverse) { // we have not seen it for this instance or labels travel to the fringe vertices, in which case we want to have the lowest depth encounter
								vertexIndexMap.put(vOldNewMap.get(edge.to()), j);
								vertexIgnoreMap.put(vOldNewMap.get(edge.to()), false);
							}
							vOldNewMap.get(edge.to()).label().clear();
							vOldNewMap.get(edge.to()).label().append(edge.to().label()); // However, we should always include it in the graph at depth j
						} else {
							DTNode<StringLabel,StringLabel> newN = rdfGraph.add(new StringLabel());
							newN.label().clear();
							newN.label().append(edge.to().label());
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.put(newN, j);
							vertexIgnoreMap.put(newN, false);
						}

						if (eOldNewMap.containsKey(edge)) {
							// Process the edge, if we haven't seen it before
							if (!edgeIndexMap.containsKey(eOldNewMap.get(edge)) || !reverse) { // see comment for vertices
								edgeIndexMap.put(eOldNewMap.get(edge), j);
								edgeIgnoreMap.put(eOldNewMap.get(edge), false);
							}
							eOldNewMap.get(edge).tag().clear();
							eOldNewMap.get(edge).tag().append(edge.tag());
						} else {
							DTLink<StringLabel,StringLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new StringLabel());
							newE.tag().clear();
							newE.tag().append(edge.tag());
							eOldNewMap.put(edge, newE);
							edgeIndexMap.put(newE, j);
							edgeIgnoreMap.put(newE, false);
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
	private void computeFVs(DTGraph<StringLabel,StringLabel> graph, List<DTNode<StringLabel,StringLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex, int currentIt) {
		int index, depth;
		Map<DTNode<StringLabel,StringLabel>, Integer> vertexIndexMap;
		Map<DTLink<StringLabel,StringLabel>, Integer> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
	
			for (DTNode<StringLabel,StringLabel> vertex : vertexIndexMap.keySet()) {
				depth = vertexIndexMap.get(vertex);
				if ((!noDuplicateSubtrees || !vertex.label().isSameAsPrev()) && ((depth * 2) >=  currentIt)) { // (depth * 2) >= currentIt
					index = Integer.parseInt(vertex.label().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}

			for (DTLink<StringLabel,StringLabel> edge : edgeIndexMap.keySet()) {
				depth = edgeIndexMap.get(edge);
				if ((!noDuplicateSubtrees || !edge.tag().isSameAsPrev()) && (((depth * 2)+1) >=  currentIt)) { //edge are actually at d*2 - 1 // ((depth * 2)+1) >= currentIt)
					index = Integer.parseInt(edge.tag().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
		}
	}


	public List<String> getFeatureDescriptions(List<Integer> indices) {
		if (dict == null) {
			throw new RuntimeException("Should run computeFeatureVectors first");
		} else {
			List<String> desc = new ArrayList<String>();

			for (int index : indices) {
				desc.add(WLUtils.getFeatureDecription(dict, index));
			}
			return desc;
		}
	}




}
