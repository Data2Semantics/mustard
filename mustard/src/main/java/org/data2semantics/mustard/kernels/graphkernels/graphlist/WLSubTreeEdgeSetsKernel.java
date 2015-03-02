package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanEdgeSetsDTGraphIterator;
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
public class WLSubTreeEdgeSetsKernel implements GraphKernel<GraphList<DTGraph<StringLabel,StringLabel>>>, FeatureVectorKernel<GraphList<DTGraph<StringLabel,StringLabel>>>, ComputationTimeTracker, FeatureInspector {
	private int iterations;
	protected boolean normalize;
	private boolean reverse;
	private boolean trackPrevNBH;
	private double minFreq;
	private int maxLabelCard;
	private double depthWeight;
	private int maxDepth;
	private long compTime;
	
	private Map<String,String> dict;

	public WLSubTreeEdgeSetsKernel(int iterations, boolean reverse, boolean trackPrevNBH, int maxLabelCard, double minFreq, double depthWeight, boolean normalize) {
		this.reverse = reverse;
		this.trackPrevNBH = trackPrevNBH;
		this.normalize = normalize;
		this.iterations = iterations;
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
		List<DTGraph<StringLabel,StringLabel>> graphs = copyGraphs(data.getGraphs());

		Map<Integer, SparseVector[]> fvMap = new TreeMap<Integer, SparseVector[]>();

		for (int i = 0; i <= maxDepth; i++) {
			SparseVector[] featureVectors = new SparseVector[data.numInstances()];
			fvMap.put(i, featureVectors);
			for (int j = 0; j < featureVectors.length; j++) {
				featureVectors[j] = new SparseVector();
			}
		}


		WeisfeilerLehmanEdgeSetsDTGraphIterator wl = new WeisfeilerLehmanEdgeSetsDTGraphIterator(reverse, trackPrevNBH, maxLabelCard, minFreq);

		long tic = System.currentTimeMillis();

		wl.wlInitialize(graphs);
		computeFVs(graphs, fvMap, 1.0, depthWeight, wl.getLabelDict().size()-1);

		for (int i = 0; i < this.iterations; i++) {
			wl.wlIterate(graphs);
			computeFVs(graphs, fvMap, 1.0, depthWeight, wl.getLabelDict().size()-1);
		}

		compTime = System.currentTimeMillis() - tic;

		// Set the reverse label dict, to reverse engineer the features
		dict = new HashMap<String,String>();
		for (String key : wl.getLabelDict().keySet()) {
			dict.put(wl.getLabelDict().get(key), key);
		}

		SparseVector[] featureVectors = fvMap.get(0);
		for (int i = 1; i <= maxDepth; i++) {
			SparseVector[] fv2 = fvMap.get(i);
			for (int j = 0; j < featureVectors.length; j++) {
				featureVectors[j].addVector(fv2[j]);
			}
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
	private void computeFVs(List<DTGraph<StringLabel,StringLabel>> graphs, Map<Integer, SparseVector[]> featureVectors, double weight, double depthWeight, int lastIndex) {
		int index, depth;
		for (int i = 0; i < graphs.size(); i++) {
			for (int j = 0; j <= maxDepth; j++) {
				featureVectors.get(j)[i].setLastIndex(lastIndex * (maxDepth + 1) + maxDepth);
			}

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<StringLabel,StringLabel> vertex : graphs.get(i).nodes()) {
				if (!vertex.label().isSameAsPrev()) {
					index = Integer.parseInt(vertex.label().toString()) * (maxDepth + 1);
					depth = vertex.label().getDepth();
					for (int j = 0; j <= maxDepth; j++) {
						if (true) { // set bits for the depth that we are not on, counterintuitive, but makes a nice FV
							featureVectors.get(j)[i].setValue(index + j, featureVectors.get(j)[i].getValue(index + j) + (weight / Math.pow((double) vertex.label().getDepth()+1, depthWeight)));
						}
					}
				}
			}

			for (DTLink<StringLabel,StringLabel> edge : graphs.get(i).links()) {
				if (!edge.tag().isSameAsPrev()) {
					index = Integer.parseInt(edge.tag().toString()) * (maxDepth + 1);
					depth = edge.tag().getDepth();
					for (int j = 0; j <= maxDepth; j++) {
						if (true) {
							featureVectors.get(j)[i].setValue(index + j, featureVectors.get(j)[i].getValue(index + j) + (weight / Math.pow((double) edge.tag().getDepth()+1, depthWeight)));
						}
					}
				}
			}
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
