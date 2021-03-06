package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.GraphList;
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
 * Class implementing the Weisfeiler-Lehman graph kernel for Directed vertex and edge labelled graphs (i.e. DTGraphs).
 * 
 * <ul>
 * <li> iterations, determines the number of iterations of the WL algorithm, note that it takes a label 2 iterations from vertex to vertex, since edges also have labels
 * <li> reverse, determines the direction that the labels 'travel' along the edges. When reverse=true, they go in the opposite direction of the direction of the edge.
 * 			  This is however the logical direction since under this setting, the algorithm determines subtrees in the graphs.
 * <li> noDuplicateSubtrees, two different iterations of the WL algorithm can give a different label to the same subtree, counting the same subtree twice, setting this to true prevents this
 * <li> normalize, if true, the kernel/featurevectors are normalized
 * </ul>
 * 
 * @author Gerben 
 */
public class WLSubTreeKernel implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>>, ComputationTimeTracker, FeatureInspector {
	private int iterations;
	protected boolean normalize;
	private boolean reverse;
	private boolean noDuplicateSubtrees;
	private long compTime;
	
	private Map<String,String> dict;

	public WLSubTreeKernel(int iterations, boolean reverse, boolean noDuplicateSubtrees, boolean normalize) {
		this.reverse = reverse;
		this.noDuplicateSubtrees = noDuplicateSubtrees;
		this.normalize = normalize;
		this.iterations = iterations;
	}

	public WLSubTreeKernel(int iterations, boolean normalize) {
		this(iterations, true, true, normalize);
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

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<String,String>> data) {
		List<DTGraph<StringLabel,StringLabel>> graphs = copyGraphs(data.getGraphs());
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}

		WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> wl = new WeisfeilerLehmanDTGraphIterator(reverse, noDuplicateSubtrees);

		long tic = System.currentTimeMillis();
		
		wl.wlInitialize(graphs);	
		
		computeFVs(graphs, featureVectors, 1.0, wl.getLabelDict().size()-1);

		for (int i = 0; i < this.iterations; i++) {
			wl.wlIterate(graphs);
			computeFVs(graphs, featureVectors, 1.0, wl.getLabelDict().size()-1);
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

	public double[][] compute(GraphList<DTGraph<String,String>> data) {
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
	private void computeFVs(List<DTGraph<StringLabel,StringLabel>> graphs, SparseVector[] featureVectors, double weight, int lastIndex) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<StringLabel,StringLabel> vertex : graphs.get(i).nodes()) {
				if (!vertex.label().isSameAsPrev()) {
					index = Integer.parseInt(vertex.label().toString());	
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}

			for (DTLink<StringLabel,StringLabel> edge : graphs.get(i).links()) {
				if (!edge.tag().isSameAsPrev()) {
					index = Integer.parseInt(edge.tag().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
		}
	}

	private List<DTGraph<StringLabel,StringLabel>> copyGraphs(List<DTGraph<String,String>> oldGraphs) {
		List<DTGraph<StringLabel,StringLabel>> newGraphs = new ArrayList<DTGraph<StringLabel,StringLabel>>();	

		for (DTGraph<String,String> graph : oldGraphs) {
			LightDTGraph<StringLabel,StringLabel> newGraph = new LightDTGraph<StringLabel,StringLabel>();
			for (DTNode<String,String> vertex : graph.nodes()) {
				newGraph.add(new StringLabel(vertex.label()));
			}
			for (DTLink<String,String> edge : graph.links()) {
				newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), new StringLabel(edge.tag())); // ?
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
