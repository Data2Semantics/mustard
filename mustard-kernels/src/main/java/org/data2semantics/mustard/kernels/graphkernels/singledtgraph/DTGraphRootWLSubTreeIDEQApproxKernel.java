package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.weisfeilerlehman.ApproxStringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WLUtils;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * 
 * @author Gerben
 *
 */
public class DTGraphRootWLSubTreeIDEQApproxKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker, FeatureInspector {

	private Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Integer>> instanceVertexIndexMap;
	private Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Map<DTLink<ApproxStringLabel,ApproxStringLabel>, Integer>> instanceEdgeIndexMap;

	private DTGraph<ApproxStringLabel,ApproxStringLabel> rdfGraph;
	private List<DTNode<ApproxStringLabel,ApproxStringLabel>> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;

	private int[] maxPrevNBHs;
	private int[] maxLabelCards;
	private int[] minFreqs;

	private Map<String,String> dict;

	private Map<String,Integer> labelFreq;

	private long compTime;

	public DTGraphRootWLSubTreeIDEQApproxKernel(int iterations, int[] maxPrevNBHs, int[] maxLabelCards, int[] minFreqs, boolean normalize) {
		this.normalize = normalize;
		this.depth = (int) Math.round(iterations / 2.0);
		this.iterations = iterations;
		this.maxPrevNBHs = maxPrevNBHs;
		this.maxLabelCards = maxLabelCards;
		this.minFreqs = minFreqs;
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
		SparseVector[] featureVectors = new SparseVector[data.numInstances()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}

		WeisfeilerLehmanApproxIterator<DTGraph<ApproxStringLabel,ApproxStringLabel>,String> wl = new WeisfeilerLehmanApproxDTGraphIterator(true, 1, 1, 1);

		double numK = (minFreqs.length) * (maxLabelCards.length) * (maxPrevNBHs.length); // number of different kernels that have to be computed computed	

		long tic = System.currentTimeMillis();

		/*
		init(data.getGraph(), data.getInstances());
		List<DTGraph<ApproxStringLabel,ApproxStringLabel>> gList = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();
		gList.add(rdfGraph);
		wl.wlInitialize(gList);

		double weight = 1.0;
		if (iterationWeighting) {
			weight = Math.sqrt(1.0 / (iterations + 1));
		}

		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, 0);
		 */	

		for (int minFreq : minFreqs) {
			for (int maxCard : maxLabelCards) {
				for (int maxPrevNBH : maxPrevNBHs) {
					init(data.getGraph(), data.getInstances());
					List<DTGraph<ApproxStringLabel,ApproxStringLabel>> gList = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();
					gList.add(rdfGraph);
					wl.wlInitialize(gList);

					wl.setMaxLabelCard(maxCard);
					wl.setMinFreq(minFreq);
					wl.setMaxPrevNBH(maxPrevNBH);

					double weight = 1.0 / numK;

					//if (iterationWeighting) {
					//	weight = Math.sqrt(1.0 / (iterations + 1));
					//}

					computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, 0);

					for (int i = 0; i < iterations; i++) {
						//if (iterationWeighting) {
						//	weight = Math.sqrt((2.0 + i) / (iterations + 1));
						//}
						weight = (1.0 + ((i+1) * ((numK-1.0) / iterations))) / numK;

						computeLabelFreqs(rdfGraph, instanceVertices, i + 1);
						wl.wlIterate(gList, labelFreq);
						computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, i + 1);
					}
				}
			}
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
		DTNode<ApproxStringLabel,ApproxStringLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Integer> vertexIndexMap;
		Map<DTLink<ApproxStringLabel,ApproxStringLabel>, Integer> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<ApproxStringLabel,ApproxStringLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>();
		Map<DTLink<String,String>, DTLink<ApproxStringLabel,ApproxStringLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<ApproxStringLabel,ApproxStringLabel>>();

		rdfGraph = new LightDTGraph<ApproxStringLabel,ApproxStringLabel>();
		instanceVertices = new ArrayList<DTNode<ApproxStringLabel,ApproxStringLabel>>();
		instanceVertexIndexMap = new HashMap<DTNode<ApproxStringLabel,ApproxStringLabel>, Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Integer>>();
		instanceEdgeIndexMap = new HashMap<DTNode<ApproxStringLabel,ApproxStringLabel>, Map<DTLink<ApproxStringLabel,ApproxStringLabel>, Integer>>();


		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new HashMap<DTNode<ApproxStringLabel,ApproxStringLabel>, Integer>();
			edgeIndexMap   = new HashMap<DTLink<ApproxStringLabel,ApproxStringLabel>, Integer>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new ApproxStringLabel());
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

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph						
							if (!vertexIndexMap.containsKey(vOldNewMap.get(edge.to()))) { // we have not seen it for this instance or labels travel to the fringe vertices, in which case we want to have the lowest depth encounter
								vertexIndexMap.put(vOldNewMap.get(edge.to()), j);
							}
							vOldNewMap.get(edge.to()).label().clear();
							vOldNewMap.get(edge.to()).label().append(edge.to().label()); // However, we should always include it in the graph at depth j
						} else {
							DTNode<ApproxStringLabel,ApproxStringLabel> newN = rdfGraph.add(new ApproxStringLabel());
							newN.label().clear();
							newN.label().append(edge.to().label());
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.put(newN, j);
						}

						if (eOldNewMap.containsKey(edge)) {
							// Process the edge, if we haven't seen it before
							if (!edgeIndexMap.containsKey(eOldNewMap.get(edge))) { // see comment for vertices
								edgeIndexMap.put(eOldNewMap.get(edge), j);
							}
							eOldNewMap.get(edge).tag().clear();
							eOldNewMap.get(edge).tag().append(edge.tag());
						} else {
							DTLink<ApproxStringLabel,ApproxStringLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new ApproxStringLabel());
							newE.tag().clear();
							newE.tag().append(edge.tag());
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

	private void computeLabelFreqs(DTGraph<ApproxStringLabel,ApproxStringLabel> graph, List<DTNode<ApproxStringLabel,ApproxStringLabel>> instances, int currentIt) {
		Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Integer> vertexIndexMap;
		Map<DTLink<ApproxStringLabel,ApproxStringLabel>, Integer> edgeIndexMap;

		// Build a new label Frequencies map
		labelFreq = new HashMap<String, Integer>();

		for (int i = 0; i < instances.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : vertexIndexMap.keySet()) {
				String lab = vertex.label().toString();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				} 
				if (vertexIndexMap.get(vertex) * 2 >= currentIt-3) { // only count it if we are interested for some instances (ie it is still relevant if it was counted in the previous round
					if (!seen.contains(lab)) {
						labelFreq.put(lab, labelFreq.get(lab) + 1);
						seen.add(lab);
					}
				}
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : edgeIndexMap.keySet()) {
				String lab = edge.tag().toString();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				} 
				if (edgeIndexMap.get(edge) * 2 >= currentIt-2) {
					if (!seen.contains(lab)) {
						labelFreq.put(lab, labelFreq.get(lab) + 1);
						seen.add(lab);
					}
				}
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
	private void computeFVs(DTGraph<ApproxStringLabel,ApproxStringLabel> graph, List<DTNode<ApproxStringLabel,ApproxStringLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex, int currentIt) {
		int index, depth;
		Map<DTNode<ApproxStringLabel,ApproxStringLabel>, Integer> vertexIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : vertexIndexMap.keySet()) {
				depth = vertexIndexMap.get(vertex);
				if (depth == this.depth && vertex.label().getSameAsPrev() == 0) {
					index = Integer.parseInt(vertex.label().toString());
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
