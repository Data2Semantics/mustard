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
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;


/**
 *
 * 
 * @author Gerben *
 */
public class PathCountKernelMkII implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>> {
	private int depth = 4;
	protected String label;
	protected boolean normalize;
	private Map<String, Integer> pathDict;


	/**
	 * Construct a PathCountKernel
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public PathCountKernelMkII(int depth, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.label = "PathCountKernel, depth=" + depth + "_" + normalize;
	}	

	public PathCountKernelMkII(int iterations) {
		this(iterations, true);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<String,String>> data) {
		pathDict = new HashMap<String,Integer>();
		List<DTGraph<PathStringLabel,PathStringLabel>> graphs = copyGraphs(data.getGraphs());

		// Initialize and compute the featureVectors
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();

			// initial count
			// Count paths
			for (DTNode<PathStringLabel,PathStringLabel> v : graphs.get(i).nodes()) {
				for (String path : v.label().getPaths()) {
					if (!pathDict.containsKey(path)) {
						pathDict.put(path, pathDict.size());
					}
					featureVectors[i].setValue(pathDict.get(path), featureVectors[i].getValue(pathDict.get(path)) + 1);
				}

			}

			for (DTLink<PathStringLabel,PathStringLabel> e : graphs.get(i).links()) {	
				for (String path : e.tag().getPaths()) {
					if (!pathDict.containsKey(path)) {
						pathDict.put(path, pathDict.size());
					}
					featureVectors[i].setValue(pathDict.get(path), featureVectors[i].getValue(pathDict.get(path)) + 1);
				}
			}
			
	
			// loop to create longer and longer paths
			for (int j = 0; j < depth; j++) {

				// Build new paths
				for (DTNode<PathStringLabel,PathStringLabel> v : graphs.get(i).nodes()) {
					for (DTLink<PathStringLabel,PathStringLabel> e : v.linksOut()) {
						v.label().addPaths(e.tag().getPaths());
					}
				}

				for (DTLink<PathStringLabel,PathStringLabel> e : graphs.get(i).links()) {	
					e.tag().addPaths(e.to().label().getPaths());	
				}

				// Count paths
				for (DTNode<PathStringLabel,PathStringLabel> v : graphs.get(i).nodes()) {
					v.label().setNewPaths();

					for (String path : v.label().getPaths()) {
						if (!pathDict.containsKey(path)) {
							pathDict.put(path, pathDict.size());
						}
						featureVectors[i].setValue(pathDict.get(path), featureVectors[i].getValue(pathDict.get(path)) + 1);
					}

				}

				for (DTLink<PathStringLabel,PathStringLabel> e : graphs.get(i).links()) {	
					e.tag().setNewPaths();	

					for (String path : e.tag().getPaths()) {
						if (!pathDict.containsKey(path)) {
							pathDict.put(path, pathDict.size());
						}
						featureVectors[i].setValue(pathDict.get(path), featureVectors[i].getValue(pathDict.get(path)) + 1);
					}
				}
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


	private List<DTGraph<PathStringLabel,PathStringLabel>> copyGraphs(List<DTGraph<String,String>> oldGraphs) {
		List<DTGraph<PathStringLabel,PathStringLabel>> newGraphs = new ArrayList<DTGraph<PathStringLabel,PathStringLabel>>();	

		for (DTGraph<String,String> graph : oldGraphs) {
			MapDTGraph<PathStringLabel,PathStringLabel> newGraph = new MapDTGraph<PathStringLabel,PathStringLabel>();
			for (DTNode<String,String> vertex : graph.nodes()) {
				if (!pathDict.containsKey(vertex.label())) {
					pathDict.put(vertex.label(), pathDict.size());
				}
				String lab = Integer.toString(pathDict.get(vertex.label()));

				newGraph.add(new PathStringLabel(lab));
			}
			for (DTLink<String,String> edge : graph.links()) {
				if (!pathDict.containsKey(edge.tag())) {
					pathDict.put(edge.tag(), pathDict.size());
				}
				String lab = Integer.toString(pathDict.get(edge.tag()));

				newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), new PathStringLabel(lab)); // ?
			}
			newGraphs.add(newGraph);
		}
		return newGraphs;
	}


	private class PathStringLabel {
		private String label;
		private List<String> paths;
		private List<String> newPaths;

		public PathStringLabel(String label) {
			this.label = label;
			paths = new ArrayList<String>();
			paths.add(label);
			newPaths = new ArrayList<String>();
		}

		public List<String> getPaths() {
			return paths;
		}

		public void addPaths(List<String> paths2) {
			for (String path : paths2) {
				newPaths.add(label + "_" + path);	
			}
		}

		public void setNewPaths() {
			if (!newPaths.isEmpty()) {
				paths = newPaths;
				newPaths = new ArrayList<String>();
			}					
		}
	}
}
