package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.weisfeilerlehman.ApproxStringLabel;
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
public class DTGraphWLSubTreeGeoProbApproxKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker {

	private DTGraph<ApproxStringLabel,ApproxStringLabel> rdfGraph;
	private List<DTNode<ApproxStringLabel,ApproxStringLabel>> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;

	private int[] maxPrevNBHs;
	private int[] maxLabelCards;
	private int[] minFreqs;

	private double p;
	private double mean;
	private Map<Integer, Double> probs;

	private double depthDiffWeight;

	private Map<String,Integer> labelFreq;

	private long compTime;

	public DTGraphWLSubTreeGeoProbApproxKernel(int iterations, int depth, double mean, double depthDiffWeight, int[] maxPrevNBHs, int[] maxLabelCards, int[] minFreqs, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.iterations = iterations;
		this.maxPrevNBHs = maxPrevNBHs;
		this.maxLabelCards = maxLabelCards;
		this.minFreqs = minFreqs;
		this.mean = mean;
		this.depthDiffWeight = depthDiffWeight;
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

		probs = new HashMap<Integer, Double>();
		p = 1.0 / (mean + 1.0); // mean is (1-p)/p

		WeisfeilerLehmanApproxIterator<DTGraph<ApproxStringLabel,ApproxStringLabel>,String> wl = new WeisfeilerLehmanApproxDTGraphIterator(true, 1, 1, 1);

		long tic = System.currentTimeMillis();

		for (int minFreq : minFreqs) {
			for (int maxCard : maxLabelCards) {
				for (int maxPrevNBH : maxPrevNBHs) {
					long t = System.currentTimeMillis();				
					init(data.getGraph(), data.getInstances());
					System.out.println("init comp: " + (System.currentTimeMillis() - t));

					List<DTGraph<ApproxStringLabel,ApproxStringLabel>> gList = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();
					gList.add(rdfGraph);
					wl.wlInitialize(gList);

					wl.setMaxLabelCard(maxCard);
					wl.setMinFreq(minFreq);
					wl.setMaxPrevNBH(maxPrevNBH);

					for (int i = 0; i < iterations; i++) {
						computeLabelFreqs(rdfGraph, instanceVertices);
						wl.wlIterate(gList, labelFreq);

					}

					t = System.currentTimeMillis();			
					computeFVs(rdfGraph, instanceVertices, featureVectors, wl.getLabelDict().size()-1);				
					System.out.println("FV comp: " + (System.currentTimeMillis() - t));
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
		DTNode<ApproxStringLabel,ApproxStringLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<DTNode<String,String>, DTNode<ApproxStringLabel,ApproxStringLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<ApproxStringLabel,ApproxStringLabel>>();
		Map<DTLink<String,String>, DTLink<ApproxStringLabel,ApproxStringLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<ApproxStringLabel,ApproxStringLabel>>();

		rdfGraph = new LightDTGraph<ApproxStringLabel,ApproxStringLabel>();
		instanceVertices = new ArrayList<DTNode<ApproxStringLabel,ApproxStringLabel>>();

		int instanceIndex = 0;
		for (DTNode<String,String> oldStartV : instances) {				
			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new ApproxStringLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().clear();
			startV.label().append(oldStartV.label());

			startV.label().addInstanceIndex(instanceIndex);

			instanceVertices.add(startV);

			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);


			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (!vOldNewMap.containsKey(edge.to())) { // This vertex has not been added to rdfGraph						
							DTNode<ApproxStringLabel,ApproxStringLabel> newN = rdfGraph.add(new ApproxStringLabel());
							newN.label().clear();
							newN.label().append(edge.to().label());
							vOldNewMap.put(edge.to(), newN);
						}
						vOldNewMap.get(edge.to()).label().addInstanceIndex(instanceIndex);

						if (!eOldNewMap.containsKey(edge)) {
							DTLink<ApproxStringLabel,ApproxStringLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new ApproxStringLabel());
							newE.tag().clear();
							newE.tag().append(edge.tag());
							eOldNewMap.put(edge, newE);
						}
						eOldNewMap.get(edge).tag().addInstanceIndex(instanceIndex);

						// Add the vertex to the new front, if we go into a new round
						if (j > 0) {
							newFrontV.add(edge.to());
						}
					}
				}
				frontV = newFrontV;
			}
			instanceIndex++;
		}		
	}

	private void computeLabelFreqs(DTGraph<ApproxStringLabel,ApproxStringLabel> graph, List<DTNode<ApproxStringLabel,ApproxStringLabel>> instances) {
		// Build a new label Frequencies map
		labelFreq = new HashMap<String, Integer>();
		Map<String, Set<Integer>> labelFreqSets = new HashMap<String, Set<Integer>>();

		for (DTNode<ApproxStringLabel,ApproxStringLabel> node : rdfGraph.nodes()) {
			String lab = node.label().toString();
			if (!labelFreqSets.containsKey(lab)) {
				//labelFreq.put(lab, 0);
				labelFreqSets.put(lab, new HashSet<Integer>());
			}
			//labelFreq.put(lab, labelFreq.get(lab) + node.label().getInstanceIndexSet().size());
			labelFreqSets.get(lab).addAll(node.label().getInstanceIndexSet());
		}

		for (DTLink<ApproxStringLabel,ApproxStringLabel> link : rdfGraph.links()) {
			String lab = link.tag().toString();
			if (!labelFreqSets.containsKey(lab)) {
				//labelFreq.put(lab, 0);
				labelFreqSets.put(lab, new HashSet<Integer>());
			}
			//labelFreq.put(lab, labelFreq.get(lab) + link.tag().getInstanceIndexSet().size());
			labelFreqSets.get(lab).addAll(link.tag().getInstanceIndexSet());
		}

		
		for (String lab : labelFreqSets.keySet()) {
			labelFreq.put(lab, labelFreqSets.get(lab).size());
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
	private void computeFVs(DTGraph<ApproxStringLabel,ApproxStringLabel> graph, List<DTNode<ApproxStringLabel,ApproxStringLabel>> instances, SparseVector[] featureVectors, int lastIndex) {
		List<DTNode<ApproxStringLabel,ApproxStringLabel>> frontV, newFrontV;

		for (int i = 0; i < instances.size(); i++) {
			Set<DTNode<ApproxStringLabel,ApproxStringLabel>> seenNodes = new HashSet<DTNode<ApproxStringLabel,ApproxStringLabel>>();
			Set<DTLink<ApproxStringLabel,ApproxStringLabel>> seenLinks = new HashSet<DTLink<ApproxStringLabel,ApproxStringLabel>>();
			
			featureVectors[i].setLastIndex((lastIndex * (this.depth+1)) + this.depth);

			// process inst i
			setFV(featureVectors[i], instances.get(i).label().getIterations(), 0);

			frontV = new ArrayList<DTNode<ApproxStringLabel,ApproxStringLabel>>();
			frontV.add(instances.get(i));
			seenNodes.add(instances.get(i));

			for (int j = 1; j <=  this.depth; j++) {
				newFrontV = new ArrayList<DTNode<ApproxStringLabel,ApproxStringLabel>>();

				for (DTNode<ApproxStringLabel,ApproxStringLabel> node : frontV) {
					for (DTLink<ApproxStringLabel,ApproxStringLabel> link : node.linksOut()) {
						if(!seenLinks.contains(link)) {
							setFV(featureVectors[i], link.tag().getIterations(), (j * 2) - 1);
							seenLinks.add(link);
						}		
						if (!seenNodes.contains(link.to())) {
							setFV(featureVectors[i], link.to().label().getIterations(), j * 2);
							seenNodes.add(link.to());
							// Add the vertex to the new front, if we go into a new round
							if (j < this.depth) {
								newFrontV.add(link.to());
							}
						}	
					}
				}
				frontV = newFrontV;
			}
		}
	}	

	private void setFV(SparseVector fv, List<String> indices, int veDepth) {
		int it = 0;
		Set<String> prev = new HashSet<String>();
		
		for (String s : indices) {
			if (veDepth + it > iterations) { // stop if we reached our max depth
				break;
			}
			if (!s.equals("") && !prev.contains(s)) { // check for previous NBH and if the label is empty, since empty means do nothing
				int index = Integer.parseInt(s);	
				double weight = getProb(veDepth + it);
				for (int j = 0; j <= this.depth; j++) {
					int index2 = (index * (this.depth+1)) + j;
					double weight2 = weight / Math.pow(depthDiffWeight,Math.abs(j-(veDepth/2.0))); // farther away depths get lower weight, the distance is abs(j-depth)
					fv.setValue(index2, fv.getValue(index2) + weight2);
				}
				prev.add(s);
			}
			it++;
		}	
	}

	/**
	 * from wikipedia on geometric dist.
	 * 
	 * @param depth
	 * @return
	 */
	private double getProb(int depth) {
		if (!probs.containsKey(depth)) { // do caching
			probs.put(depth, Math.pow(1-p, depth) * p);
		}
		return probs.get(depth);		
	}


	private double getCumProb(int depth) {
		return 1-Math.pow(1-p, depth+1);		
	}
}
