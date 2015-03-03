package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.text.html.HTML.Tag;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WLUtils;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;


/**
 * Class implementing the Weisfeiler-Lehman graph kernel for DTGraphs
 * 
 * @author Gerben *
 */
public class WLSubTreeApproxKernel implements GraphKernel<GraphList<DTGraph<StringLabel,StringLabel>>>, FeatureVectorKernel<GraphList<DTGraph<StringLabel,StringLabel>>>, ComputationTimeTracker, FeatureInspector {
	private int iterations;
	protected boolean normalize;
	private boolean reverse;
	private boolean trackPrevNBH;
	private double minFreq;
	private int maxLabelCard;
	private boolean skipSamePrevNBH;
	private double depthWeight;
	private int maxDepth;
	private long compTime;

	private Map<String,String> dict;
	private Map<String, Double> labelFreq;

	public WLSubTreeApproxKernel(int iterations, boolean reverse, boolean trackPrevNBH, boolean skipSamePrevNBH, int maxLabelCard, double minFreq, double depthWeight, boolean normalize) {
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
		this.normalize = normalize;
		this.iterations = iterations;
		this.skipSamePrevNBH = skipSamePrevNBH;
		this.maxLabelCard = maxLabelCard;
		this.minFreq = minFreq;
		this.depthWeight = depthWeight;
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

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<StringLabel,StringLabel>> data) {
		// copy to avoid changing the original graphs

		SparseVector[] featureVectors = new SparseVector[data.numInstances()];
		for (int j = 0; j < featureVectors.length; j++) {
			featureVectors[j] = new SparseVector();
		}

		WeisfeilerLehmanApproxIterator<DTGraph<StringLabel,StringLabel>,String> wl = new WeisfeilerLehmanApproxDTGraphIterator(reverse, trackPrevNBH, skipSamePrevNBH, maxLabelCard, minFreq);

		long tic = System.currentTimeMillis();

		double[] minFreqs = {0.0}; //{1 / (double) (data.numInstances()-1), 2 / (double) (data.numInstances()-1), 3 / (double) (data.numInstances()-1), 4 / (double) (data.numInstances()-1)};
		int[] maxCards = {1000000}; //{1,2,3,4};

		// Initial FV (the bag of labels)
		List<DTGraph<StringLabel,StringLabel>> graphs = copyGraphs(data.getGraphs());
		wl.wlInitialize(graphs);
		computeFVs(graphs, featureVectors, 1.0, depthWeight, wl.getLabelDict().size()-1);

		boolean first = true;
		
		for (double minFreq : minFreqs) {
			for (int maxCard : maxCards) {
				if (!first) {
					graphs = copyGraphs(data.getGraphs());
					wl.wlInitialize(graphs);
				}
				first = false;
				
				wl.setMaxLabelCard(maxCard);
				wl.setMinFreq(minFreq);
				
				for (int i = 0; i < this.iterations; i++) {
					computeLabelFreqs(graphs);	
					wl.wlIterate(graphs, labelFreq);
					computeFVs(graphs, featureVectors, 1.0, depthWeight, wl.getLabelDict().size()-1);
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

	public double[][] compute(GraphList<DTGraph<StringLabel,StringLabel>> data) {
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
	private void computeFVs(List<DTGraph<StringLabel,StringLabel>> graphs, SparseVector[] featureVectors, double weight, double depthWeight, int lastIndex) {
		int index;

	for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);
	
			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<StringLabel,StringLabel> vertex : graphs.get(i).nodes()) {
				String lab = vertex.label().toString();
				if (!vertex.label().isSameAsPrev()) {
					index = Integer.parseInt(lab);
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + (weight / Math.pow((double) vertex.label().getDepth() + 1, depthWeight)));
				}
			}

			for (DTLink<StringLabel,StringLabel> edge : graphs.get(i).links()) {
				String lab = edge.tag().toString();
				if (!edge.tag().isSameAsPrev()) {
					index = Integer.parseInt(lab);
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + (weight / Math.pow((double) edge.tag().getDepth() + 1, depthWeight)));
				}
			}
		}
	}
	
	private void computeLabelFreqs(List<DTGraph<StringLabel,StringLabel>> graphs) {
		int index;

		// Build a new label Frequencies map
		labelFreq = new HashMap<String, Double>();

		for (int i = 0; i < graphs.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<StringLabel,StringLabel> vertex : graphs.get(i).nodes()) {
				String lab = vertex.label().toString();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0.0);
				} 
				if (!seen.contains(lab)) {
					labelFreq.put(lab, labelFreq.get(lab) + 1);
					seen.add(lab);
				}
			}

			for (DTLink<StringLabel,StringLabel> edge : graphs.get(i).links()) {
				String lab = edge.tag().toString();
				// Count
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0.0);
				} 
				if (!seen.contains(lab)) {
					labelFreq.put(lab, labelFreq.get(lab) + 1);
					seen.add(lab);
				}
			}
		}
		// normalize to #occur/#instances.
		for (String k : labelFreq.keySet()) {
			labelFreq.put(k, labelFreq.get(k) / ((double) graphs.size()));
		}
	}

	private List<DTGraph<StringLabel,StringLabel>> copyGraphs(List<DTGraph<StringLabel,StringLabel>> oldGraphs) {
		List<DTGraph<StringLabel,StringLabel>> newGraphs = new ArrayList<DTGraph<StringLabel,StringLabel>>();	

		maxDepth = 0;
		for (DTGraph<StringLabel,StringLabel> graph : oldGraphs) {
			LightDTGraph<StringLabel,StringLabel> newGraph = new LightDTGraph<StringLabel,StringLabel>();
			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				newGraph.add(new StringLabel(vertex.label().toString(), vertex.label().getDepth()));
				maxDepth = Math.max(maxDepth, vertex.label().getDepth());
			}
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), new StringLabel(edge.tag().toString(), edge.tag().getDepth())); // ?
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
