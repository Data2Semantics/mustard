package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.utils.Pair;
import org.data2semantics.mustard.weisfeilerlehman.ApproxMapLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxDTGraphMapLabelIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * @author Gerben
 *
 */
public class DTGraphTreeWLSubTreeApproxKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker {

	private Map<DTNode<ApproxMapLabel,ApproxMapLabel>, List<Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer>>> instanceVertexIndexMap;
	private Map<DTNode<ApproxMapLabel,ApproxMapLabel>, List<Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer>>> instanceEdgeIndexMap;

	private DTGraph<ApproxMapLabel,ApproxMapLabel> rdfGraph;
	private List<DTNode<ApproxMapLabel,ApproxMapLabel>> instanceVertices;

	private Map<String, Integer> labelFreq;

	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean reverse;
	private boolean iterationWeighting;
	private boolean noDuplicateNBH;
	private int[] maxLabelCards;
	private int[] minFreqs;
	private int[] maxPrevNBHs;

	private long compTime; // should be last, so that it is the last arg in the label

	public DTGraphTreeWLSubTreeApproxKernel(int iterations, int depth, boolean reverse, boolean iterationWeighting, boolean noDuplicateNBH, int[] maxPrevNBHs, int[] maxLabelCards, int[] minFreqs, boolean normalize) {
		this.reverse = reverse;
		this.iterationWeighting = iterationWeighting;
		this.noDuplicateNBH = noDuplicateNBH;
		this.normalize = normalize;
		this.depth = depth;
		this.iterations = iterations;
		this.maxLabelCards = maxLabelCards;
		this.minFreqs = minFreqs;
		this.maxPrevNBHs = maxPrevNBHs;
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

		WeisfeilerLehmanApproxIterator<DTGraph<ApproxMapLabel,ApproxMapLabel>,String> wl = new WeisfeilerLehmanApproxDTGraphMapLabelIterator(reverse, 1, 1, 1);

		long tic = System.currentTimeMillis();

		init(data.getGraph(), data.getInstances());
		List<DTGraph<ApproxMapLabel,ApproxMapLabel>> gList = new ArrayList<DTGraph<ApproxMapLabel,ApproxMapLabel>>();
		gList.add(rdfGraph);
		wl.wlInitialize(gList);

		double weight = 1.0;
		if (iterationWeighting) {
			weight = Math.sqrt(1.0 / (iterations + 1));
		}

		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);


		boolean first = true;
		for (int minFreq : minFreqs) {
			for (int maxCard : maxLabelCards) {
				for (int maxPrevNBH : maxPrevNBHs) {
					if (!first) {
						init(data.getGraph(), data.getInstances());
						gList = new ArrayList<DTGraph<ApproxMapLabel,ApproxMapLabel>>();
						gList.add(rdfGraph);
						wl.wlInitialize(gList);
					}
					first = false;

					wl.setMaxLabelCard(maxCard);
					wl.setMinFreq(minFreq);
					wl.setMaxPrevNBH(maxPrevNBH);

					for (int i = 0; i < iterations; i++) {
						if (iterationWeighting) {
							weight = Math.sqrt((2.0 + i) / (iterations + 1));
						}
						computeLabelFreqs(rdfGraph, instanceVertices);
						wl.wlIterate(gList, labelFreq);
						computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);
					}
				}
			}
		}

		compTime = System.currentTimeMillis() - tic;

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
		DTNode<ApproxMapLabel,ApproxMapLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		List<Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer>> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<ApproxMapLabel,ApproxMapLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<ApproxMapLabel,ApproxMapLabel>>();
		Map<DTLink<String,String>, DTLink<ApproxMapLabel,ApproxMapLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<ApproxMapLabel,ApproxMapLabel>>();

		instanceVertices       = new ArrayList<DTNode<ApproxMapLabel,ApproxMapLabel>>();
		instanceVertexIndexMap = new HashMap<DTNode<ApproxMapLabel,ApproxMapLabel>, List<Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer>>>();
		instanceEdgeIndexMap   = new HashMap<DTNode<ApproxMapLabel,ApproxMapLabel>, List<Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer>>>();
		rdfGraph = new LightDTGraph<ApproxMapLabel,ApproxMapLabel>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new ArrayList<Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer>>();
			edgeIndexMap   = new ArrayList<Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer>>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new ApproxMapLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().clear(depth);
			startV.label().append(depth, oldStartV.label());
			instanceVertices.add(startV);

			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);

			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.add(new Pair<DTNode<ApproxMapLabel,ApproxMapLabel>,Integer>(startV, depth));

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph
							vertexIndexMap.add(new Pair<DTNode<ApproxMapLabel,ApproxMapLabel>,Integer>(vOldNewMap.get(edge.to()), j));  
							vOldNewMap.get(edge.to()).label().clear(j);
							vOldNewMap.get(edge.to()).label().append(j, edge.to().label()); 
						} else {
							DTNode<ApproxMapLabel,ApproxMapLabel> newN = rdfGraph.add(new ApproxMapLabel());
							newN.label().clear(j);
							newN.label().append(j, edge.to().label());
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.add(new Pair<DTNode<ApproxMapLabel,ApproxMapLabel>,Integer>(newN, j)); 
						}

						if (eOldNewMap.containsKey(edge)) {
							edgeIndexMap.add(new Pair<DTLink<ApproxMapLabel,ApproxMapLabel>,Integer>(eOldNewMap.get(edge),j)); 		
							eOldNewMap.get(edge).tag().clear(j);
							eOldNewMap.get(edge).tag().append(j, edge.tag());
						} else {
							DTLink<ApproxMapLabel,ApproxMapLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new ApproxMapLabel());
							newE.tag().clear(j);
							newE.tag().append(j, edge.tag());
							eOldNewMap.put(edge, newE);
							edgeIndexMap.add(new Pair<DTLink<ApproxMapLabel,ApproxMapLabel>,Integer>(newE, j));
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
	 * @param graph
	 * @param instances
	 * @param weight
	 * @param featureVectors
	 */
	private void computeFVs(DTGraph<ApproxMapLabel,ApproxMapLabel> graph, List<DTNode<ApproxMapLabel,ApproxMapLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex) {
		int index;
		List<Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer>> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer> vertex : vertexIndexMap) {
				if (!noDuplicateNBH || vertex.getFirst().label().getSameAsPrev(vertex.getSecond()) == 0) {
					index = Integer.parseInt(vertex.getFirst().label().get(vertex.getSecond()));
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
			for (Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer> edge : edgeIndexMap) {
				if (!noDuplicateNBH || edge.getFirst().tag().getSameAsPrev(edge.getSecond()) == 0) {
					index = Integer.parseInt(edge.getFirst().tag().get(edge.getSecond()));
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
		}
	}

	private void computeLabelFreqs(DTGraph<ApproxMapLabel,ApproxMapLabel> graph, List<DTNode<ApproxMapLabel,ApproxMapLabel>> instances) {
		List<Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer>> edgeIndexMap;

		// Build a new label Frequencies map
		labelFreq = new HashMap<String, Integer>();

		for (int i = 0; i < instances.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (Pair<DTNode<ApproxMapLabel,ApproxMapLabel>, Integer> vertex : vertexIndexMap) {
				String lab = vertex.getFirst().label().get(vertex.getSecond());
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				} 
				if (!seen.contains(lab)) {
					labelFreq.put(lab, labelFreq.get(lab) + 1);
					seen.add(lab);
				}
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
			for (Pair<DTLink<ApproxMapLabel,ApproxMapLabel>, Integer> edge : edgeIndexMap) {
				String lab = edge.getFirst().tag().get(edge.getSecond());
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				} 
				if (!seen.contains(lab)) {
					labelFreq.put(lab, labelFreq.get(lab) + 1);
					seen.add(lab);
				}
			}
		}
	}
}
