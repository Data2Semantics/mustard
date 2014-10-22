package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;


/**
 *
 * 
 * @author Gerben *
 */
public class WalkCountKernel implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>> {
	private int depth = 4;
	protected String label;
	protected boolean normalize;
	private Map<String, Integer> pathDict;
	private Map<String, Integer> labelDict;


	/**
	 * Construct a PathCountKernel
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public WalkCountKernel(int depth, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.label = "PathCountKernel, depth=" + depth + "_" + normalize;
	}	

	public WalkCountKernel(int iterations) {
		this(iterations, true);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<String,String>> data) {
		pathDict  = new HashMap<String,Integer>();
		labelDict = new HashMap<String,Integer>();
		List<DTGraph<String,String>> graphs = copyGraphs(data.getGraphs());

		// Initialize and compute the featureVectors
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();

			for (DTNode<String,String> v : graphs.get(i).nodes()) {
				countPathRec(featureVectors[i], v, "", depth);
			}
			
			for (DTLink<String,String> e : graphs.get(i).links()) {
				countPathRec(featureVectors[i], e, "", depth);
			}
		}

		// Set the correct last index
		for (SparseVector fv : featureVectors) {
			fv.setLastIndex(pathDict.size()-1);
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

	private void countPathRec(SparseVector fv, DTNode<String,String> vertex, String path, int depth) {
		// Count path
		path = path + vertex.label();

		if (!pathDict.containsKey(path)) {
			pathDict.put(path, pathDict.size());
		}
		fv.setValue(pathDict.get(path), fv.getValue(pathDict.get(path)) + 1);

		if (depth > 0) {
			for (DTLink<String,String> edge : vertex.linksOut()) {
				countPathRec(fv, edge, path, depth-1);
			}
		}	
	}

	private void countPathRec(SparseVector fv, DTLink<String,String> edge, String path, int depth) {
		// Count path
		path = path + edge.tag();

		if (!pathDict.containsKey(path)) {
			pathDict.put(path, pathDict.size());
		}
		fv.setValue(pathDict.get(path), fv.getValue(pathDict.get(path)) + 1);

		if (depth > 0) {
			countPathRec(fv, edge.to(), path, depth-1);
		}	
	}



	private List<DTGraph<String,String>> copyGraphs(List<DTGraph<String,String>> oldGraphs) {
		List<DTGraph<String,String>> newGraphs = new ArrayList<DTGraph<String,String>>();	

		for (DTGraph<String,String> graph : oldGraphs) {
			LightDTGraph<String,String> newGraph = new LightDTGraph<String,String>();
			for (DTNode<String,String> vertex : graph.nodes()) {
				if (!labelDict.containsKey(vertex.label())) {
					labelDict.put(vertex.label(), labelDict.size());
				}
				String lab = "_" + Integer.toString(labelDict.get(vertex.label()));

				newGraph.add(lab);
			}
			for (DTLink<String,String> edge : graph.links()) {
				if (!labelDict.containsKey(edge.tag())) {
					labelDict.put(edge.tag(), labelDict.size());
				}
				String lab = "_" + Integer.toString(labelDict.get(edge.tag()));

				newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), lab); // ?
			}
			newGraphs.add(newGraph);
		}
		return newGraphs;
	}
}
