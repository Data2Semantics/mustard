package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.weisfeilerlehman.ApproxStringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WLUtils;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxDTGraphIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;


/**
 * Class implementing the Weisfeiler-Lehman graph kernel for DTGraphs
 * 
 * @author Gerben *
 */
public class WLSubTreeApproxKernel implements GraphKernel<GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>>>, FeatureVectorKernel<GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>>>, ComputationTimeTracker, FeatureInspector {
	private int iterations;
	protected boolean normalize;
	private boolean reverse;
	private boolean noDuplicateNBH;

	private int[] minFreqs;
	private int[] maxLabelCards;
	private int[] maxPrevNBHs;

	private double depthWeight;
	private double depthDiffWeight;
	private int maxDepth;
	private long compTime;

	private Map<String,String> dict;
	private Map<String, Integer> labelFreq;

	
	public WLSubTreeApproxKernel(int iterations, boolean reverse, boolean noDuplicateNBH, double depthWeight, double depthDiffWeight, int[] maxPrevNBHs, int[] maxLabelCards, int[] minFreqs, boolean normalize) {
		this.reverse = reverse;
		this.noDuplicateNBH = noDuplicateNBH;
		this.normalize = normalize;
		this.iterations = iterations;
		this.maxPrevNBHs = maxPrevNBHs;
		this.maxLabelCards = maxLabelCards;
		this.minFreqs = minFreqs;
		this.depthWeight = depthWeight;
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

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>> data) {
		SparseVector[] featureVectors = new SparseVector[data.numInstances()];
		for (int j = 0; j < featureVectors.length; j++) {
			featureVectors[j] = new SparseVector();
		}

		WeisfeilerLehmanApproxIterator<DTGraph<ApproxStringLabel,ApproxStringLabel>,String> wl = new WeisfeilerLehmanApproxDTGraphIterator(reverse, 1, 1, 1);

		long tic = System.currentTimeMillis();

		// Initial FV (the bag of labels)
		// copy to avoid changing the original graphs
		List<DTGraph<ApproxStringLabel,ApproxStringLabel>> graphs = copyGraphs(data.getGraphs());
		wl.wlInitialize(graphs);
		computeFVs(graphs, featureVectors, 1.0, depthWeight, wl.getLabelDict().size()-1);

		boolean first = true;

		for (int minFreq : minFreqs) {
			for (int maxCard : maxLabelCards) {
				for (int maxPrevNBH : maxPrevNBHs) {
					if (!first) {
						graphs = copyGraphs(data.getGraphs());
						wl.wlInitialize(graphs);
					}
					first = false;

					wl.setMaxLabelCard(maxCard);
					wl.setMinFreq(minFreq);
					wl.setMaxPrevNBH(maxPrevNBH);

					for (int i = 0; i < this.iterations; i++) {
						computeLabelFreqs(graphs);	
						wl.wlIterate(graphs, labelFreq);
						computeFVs(graphs, featureVectors, 1.0, depthWeight, wl.getLabelDict().size()-1);
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

		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		return featureVectors;
	}

	public double[][] compute(GraphList<DTGraph<ApproxStringLabel,ApproxStringLabel>> data) {
		double[][] kernel = KernelUtils.initMatrix(data.getGraphs().size(), data.getGraphs().size());
		kernel = KernelUtils.computeKernelMatrix(computeFeatureVectors(data), kernel);				
		return kernel;
	}


	/**
	 * Compute feature vector for the graphs based on the label dictionary created in the previous two steps
	 * 
	 * @param graphs
	 * @param featureVectors
	 * @param startLabel
	 * @param currentLabel
	 */
	private void computeFVs(List<DTGraph<ApproxStringLabel,ApproxStringLabel>> graphs, SparseVector[] featureVectors, double weight, double depthWeight, int lastIndex) {
		int index;

		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i].setLastIndex((lastIndex * (maxDepth+1)) + maxDepth);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : graphs.get(i).nodes()) {
				String lab = vertex.label().toString();
				if (!noDuplicateNBH || vertex.label().getSameAsPrev() == 0) {
					index = Integer.parseInt(lab);
					
					for (int j = 0; j <= maxDepth; j++) {
						int index2 = (index * (maxDepth+1)) + j;
						double weight2 = weight / Math.pow(depthDiffWeight,Math.abs(j-maxDepth)); // farther away depths get lower weight, the distance is abs(j-depth)
						featureVectors[i].setValue(index2, featureVectors[i].getValue(index2) + weight2);
					}
					
					//featureVectors[i].setValue(index, featureVectors[i].getValue(index) + (weight / Math.pow((double) vertex.label().getDepth() + 1, depthWeight)));
				}
			}

			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : graphs.get(i).links()) {
				String lab = edge.tag().toString();
				if (!noDuplicateNBH || edge.tag().getSameAsPrev() == 0) {
					index = Integer.parseInt(lab);
					
					for (int j = 0; j <= maxDepth; j++) {
						int index2 = (index * (maxDepth+1)) + j;
						double weight2 = weight / Math.pow(depthDiffWeight,Math.abs(j-maxDepth)); // farther away depths get lower weight, the distance is abs(j-depth)
						featureVectors[i].setValue(index2, featureVectors[i].getValue(index2) + weight2);
					}
					//featureVectors[i].setValue(index, featureVectors[i].getValue(index) + (weight / Math.pow((double) edge.tag().getDepth() + 1, depthWeight)));
				}
			}
		}
	}

	private void computeLabelFreqs(List<DTGraph<ApproxStringLabel,ApproxStringLabel>> graphs) {
		// Build a new label Frequencies map
		labelFreq = new HashMap<String, Integer>();

		for (int i = 0; i < graphs.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : graphs.get(i).nodes()) {
				String lab = vertex.label().toString();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				} 
				if (!seen.contains(lab)) {
					labelFreq.put(lab, labelFreq.get(lab) + 1);
					seen.add(lab);
				}
			}

			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : graphs.get(i).links()) {
				String lab = edge.tag().toString();
				// Count
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

	private List<DTGraph<ApproxStringLabel,ApproxStringLabel>> copyGraphs(List<DTGraph<ApproxStringLabel,ApproxStringLabel>> oldGraphs) {
		List<DTGraph<ApproxStringLabel,ApproxStringLabel>> newGraphs = new ArrayList<DTGraph<ApproxStringLabel,ApproxStringLabel>>();	

		maxDepth = 0;
		for (DTGraph<ApproxStringLabel,ApproxStringLabel> graph : oldGraphs) {
			LightDTGraph<ApproxStringLabel,ApproxStringLabel> newGraph = new LightDTGraph<ApproxStringLabel,ApproxStringLabel>();
			for (DTNode<ApproxStringLabel,ApproxStringLabel> vertex : graph.nodes()) {
				newGraph.add(new ApproxStringLabel(vertex.label().toString(), vertex.label().getDepth()));
				maxDepth = Math.max(maxDepth, vertex.label().getDepth());
			}
			for (DTLink<ApproxStringLabel,ApproxStringLabel> edge : graph.links()) {
				newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), new ApproxStringLabel(edge.tag().toString(), edge.tag().getDepth())); // ?
				maxDepth = Math.max(maxDepth, edge.tag().getDepth());
			}
			newGraphs.add(newGraph);
		}
		return newGraphs;
	}

	public List<String> getFeatureDescriptions(List<Integer> indicesSV) {
		if (dict == null) {
			throw new RuntimeException("Should run computeFeatureVectors() first");
		} else {
			List<String> desc = new ArrayList<String>();

			for (int index : indicesSV) {
				desc.add(WLUtils.getFeatureDecription(dict, index));
			}
			return desc;
		}
	}


}
