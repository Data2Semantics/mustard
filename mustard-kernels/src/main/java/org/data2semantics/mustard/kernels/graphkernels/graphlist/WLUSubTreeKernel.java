package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanUGraphIterator;
import org.nodes.MapUTGraph;
import org.nodes.UGraph;
import org.nodes.ULink;
import org.nodes.UNode;


/**
 * Class implementing the Weisfeiler-Lehman graph kernel for general Undirected graphs.
 * This is the general Weisfeiler-Lehman algorithm and this implementation is mainly used to compare 'regular' versions of graph data to RDF versions.
 * 
 * @author Gerben
 *
 */
public class WLUSubTreeKernel implements GraphKernel<GraphList<UGraph<String>>>, FeatureVectorKernel<GraphList<UGraph<String>>> {
	private int iterations;
	protected boolean normalize;

	/**
	 * Construct a WLSubTreeKernel. 
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public WLUSubTreeKernel(int iterations, boolean normalize) {
		this.normalize = normalize;
		this.iterations = iterations;
	}	

	public WLUSubTreeKernel(int iterations) {
		this(iterations, true);
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}
	
	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(GraphList<UGraph<String>> data) {
		// Have to use UGraph implementation for copying.
		// List<UTGraph<StringLabel,?>> graphs = copyGraphs(trainGraphs);
		List<UGraph<StringLabel>> graphs = copyGraphs(data.getGraphs());

		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		WeisfeilerLehmanIterator<UGraph<StringLabel>> wl = new WeisfeilerLehmanUGraphIterator();
		
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

	public double[][] compute(GraphList<UGraph<String>> data) {
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
	private void computeFVs(List<UGraph<StringLabel>> graphs, SparseVector[] featureVectors, double weight, int lastIndex) {
		int index;
		for (int i = 0; i < graphs.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);
			
			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (UNode<StringLabel> vertex : graphs.get(i).nodes()) {
				index = Integer.parseInt(vertex.label().toString());	
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
			}
		}
	}

	private static List<UGraph<StringLabel>> copyGraphs(List<UGraph<String>> graphs) {
		List<UGraph<StringLabel>> newGraphs = new ArrayList<UGraph<StringLabel>>();
				
		for (UGraph<String> graph : graphs) {
			UGraph<StringLabel> newGraph = new MapUTGraph<StringLabel,Object>();
			for (UNode<String> vertex : graph.nodes()) {
				newGraph.add(new StringLabel(vertex.label()));
			}
			for (ULink<String> edge : graph.links()) {
				newGraph.nodes().get(edge.first().index()).connect(newGraph.nodes().get(edge.second().index()));
			}
			newGraphs.add(newGraph);
		}
		return newGraphs;
	}
	
}