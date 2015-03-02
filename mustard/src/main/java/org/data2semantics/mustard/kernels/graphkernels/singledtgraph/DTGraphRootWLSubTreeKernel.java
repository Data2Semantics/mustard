package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.weisfeilerlehman.ArrayMapLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphMapLabelIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * This is a simple modified version of the WL sub tree kernel to compute only the trees with the root vertices. Not the fastest implementation, but it is
 * only for comparisons.
 * 

 * @author Gerben
 *
 */
public class DTGraphRootWLSubTreeKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {

	private Map<DTNode<ArrayMapLabel,ArrayMapLabel>, Map<DTNode<ArrayMapLabel,ArrayMapLabel>, Integer>> instanceVertexIndexMap;
	private Map<DTNode<ArrayMapLabel,ArrayMapLabel>, Map<DTLink<ArrayMapLabel,ArrayMapLabel>, Integer>> instanceEdgeIndexMap;

	private DTGraph<ArrayMapLabel,ArrayMapLabel> rdfGraph;
	private List<DTNode<ArrayMapLabel,ArrayMapLabel>> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean iterationWeighting;


	public DTGraphRootWLSubTreeKernel(int iterations, boolean iterationWeighting, boolean normalize) {
		this.iterationWeighting = iterationWeighting;			
		this.depth = (int) Math.round(iterations / 2.0);
		this.iterations = iterations;
	}


	public DTGraphRootWLSubTreeKernel(int iterations, boolean normalize) {
		this(iterations, false, normalize);
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		instanceVertices = new ArrayList<DTNode<ArrayMapLabel,ArrayMapLabel>>();
		this.instanceVertexIndexMap = new HashMap<DTNode<ArrayMapLabel,ArrayMapLabel>, Map<DTNode<ArrayMapLabel,ArrayMapLabel>, Integer>>();
		this.instanceEdgeIndexMap = new HashMap<DTNode<ArrayMapLabel,ArrayMapLabel>, Map<DTLink<ArrayMapLabel,ArrayMapLabel>, Integer>>();		
		
		SparseVector[] featureVectors = new SparseVector[data.getInstances().size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		init(data.getGraph(), data.getInstances());
		WeisfeilerLehmanIterator<DTGraph<ArrayMapLabel,ArrayMapLabel>> wl = new WeisfeilerLehmanDTGraphMapLabelIterator(true, false);

		List<DTGraph<ArrayMapLabel,ArrayMapLabel>> gList = new ArrayList<DTGraph<ArrayMapLabel,ArrayMapLabel>>();
		gList.add(rdfGraph);

		wl.wlInitialize(gList);

		double weight = 1.0;
		if (iterationWeighting) {
			weight = Math.sqrt(1.0 / (iterations + 1));
		}

		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);

		for (int i = 0; i < iterations; i++) {
			if (iterationWeighting) {
				weight = Math.sqrt((2.0 + i) / (iterations + 1));
			}
			wl.wlIterate(gList);
			computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);
		}
		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
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
		DTNode<ArrayMapLabel,ArrayMapLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<DTNode<ArrayMapLabel,ArrayMapLabel>, Integer> vertexIndexMap;
		Map<DTLink<ArrayMapLabel,ArrayMapLabel>, Integer> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<ArrayMapLabel,ArrayMapLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<ArrayMapLabel,ArrayMapLabel>>();
		Map<DTLink<String,String>, DTLink<ArrayMapLabel,ArrayMapLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<ArrayMapLabel,ArrayMapLabel>>();

		rdfGraph = new LightDTGraph<ArrayMapLabel,ArrayMapLabel>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new HashMap<DTNode<ArrayMapLabel,ArrayMapLabel>, Integer>();
			edgeIndexMap   = new HashMap<DTLink<ArrayMapLabel,ArrayMapLabel>, Integer>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new ArrayMapLabel());
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
							DTNode<ArrayMapLabel,ArrayMapLabel> newN = rdfGraph.add(new ArrayMapLabel());
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
							DTLink<ArrayMapLabel,ArrayMapLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new ArrayMapLabel());
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
	private void computeFVs(DTGraph<ArrayMapLabel,ArrayMapLabel> graph, List<DTNode<ArrayMapLabel,ArrayMapLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex) {
		int index;
		Map<DTNode<ArrayMapLabel,ArrayMapLabel>, Integer> vertexIndexMap;
		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (DTNode<ArrayMapLabel,ArrayMapLabel> vertex : vertexIndexMap.keySet()) {
			if (vertexIndexMap.get(vertex) == depth) {
					index = Integer.parseInt(vertex.label().get(vertexIndexMap.get(vertex)).toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
		}
	}
}
