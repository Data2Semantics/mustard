package org.data2semantics.mustard.kernels.graphkernels;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;


/**
 * Class implementing the Weisfeiler-Lehman graph kernel for DTGraphs
 * 
 * @author Gerben *
 */
public class WLSubTreeKernel implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>> {
	private int iterations = 2;
	protected String label;
	protected boolean normalize;
	private boolean reverse;


	public WLSubTreeKernel(int iterations, boolean reverse, boolean normalize) {
		this(iterations, normalize);
		this.reverse = reverse;
	}


	/**
	 * Construct a WLSubTreeKernel. 
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public WLSubTreeKernel(int iterations, boolean normalize) {
		this.normalize = normalize;
		this.iterations = iterations;
		this.reverse = false;
		this.label = "WL SubTree Kernel, it=" + iterations + "_" + reverse + "_" + normalize;
	}	

	public WLSubTreeKernel(int iterations) {
		this(iterations, true);
	}


	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}	

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<String,String>> data) {
		List<DTGraph<StringLabel,StringLabel>> graphs = copyGraphs(data.getGraphs());
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}

		WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> wl = new WeisfeilerLehmanDTGraphIterator(reverse);

		wl.wlInitialize(graphs);

		computeFVs(graphs, featureVectors, 1.0, wl.getLabelDict().size()-1);

		for (int i = 0; i < this.iterations; i++) {
			wl.wlIterate(graphs);
			computeFVs(graphs, featureVectors, 1.0, wl.getLabelDict().size()-1);
		}

		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		return featureVectors;
	}

	public double[][] compute(GraphList<DTGraph<String,String>> data) {
		double[][] kernel = KernelUtils.initMatrix(data.getGraphs().size(), data.getGraphs().size());
		computeKernelMatrix(computeFeatureVectors(data), kernel);				
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
				index = Integer.parseInt(vertex.label().toString());	
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
			}

			for (DTLink<StringLabel,StringLabel> edge : graphs.get(i).links()) {
				index = Integer.parseInt(edge.tag().toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
			}
		}
	}



	/**
	 * Use the feature vectors to compute a kernel matrix.
	 * 
	 * @param graphs
	 * @param featureVectors
	 * @param kernel
	 * @param iteration
	 */
	private void computeKernelMatrix(SparseVector[] featureVectors, double[][] kernel) {
		for (int i = 0; i < featureVectors.length; i++) {
			for (int j = i; j < featureVectors.length; j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}


	private List<DTGraph<StringLabel,StringLabel>> copyGraphs(List<DTGraph<String,String>> oldGraphs) {
		List<DTGraph<StringLabel,StringLabel>> newGraphs = new ArrayList<DTGraph<StringLabel,StringLabel>>();	

		for (DTGraph<String,String> graph : oldGraphs) {
			MapDTGraph<StringLabel,StringLabel> newGraph = new MapDTGraph<StringLabel,StringLabel>();
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
}
