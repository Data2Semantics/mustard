package org.data2semantics.mustard.kernels.graphkernels.graphlist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;


/**
 *
 * 
 * @author Gerben *
 */
public class WalkCountApproxKernelMkII implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>>, ComputationTimeTracker {
	private int depth;
	protected boolean normalize;
	private int minFreq;
	private long compTime;
	private Map<String, Integer> pathDict;
	private Map<String, Integer> labelDict;

	private Map<String,Integer> labelFreq;
	private Map<String,Integer> pathFreq;



	/**
	 * Construct a PathCountKernel
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public WalkCountApproxKernelMkII(int depth, int minFreq, boolean normalize) {
		this.normalize = normalize;
		this.minFreq = minFreq;
		this.depth = depth;
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
		pathDict  = new HashMap<String,Integer>();
		labelDict = new HashMap<String,Integer>();

		List<DTGraph<PathStringLabel,PathStringLabel>> graphs = copyGraphs(data.getGraphs());

		// Initialize and compute the featureVectors
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}

		long tic = System.currentTimeMillis();

		for (int i = 0; i < featureVectors.length; i++) {
			// initial count
			// Count paths
			Integer index = null;
			for (DTNode<PathStringLabel,PathStringLabel> v : graphs.get(i).nodes()) {
				for (String path : v.label().getPaths()) {
					index = pathDict.get(path);
					if (index == null) {
						index = pathDict.size();
						pathDict.put(path, index);
					}
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1);
				}

			}

			for (DTLink<PathStringLabel,PathStringLabel> e : graphs.get(i).links()) {	
				for (String path : e.tag().getPaths()) {
					index = pathDict.get(path);
					if (index == null) {
						index = pathDict.size();
						pathDict.put(path, index);
					}
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1);
				}
			}
			
			computeLabelFreqs(graphs);	

			// loop to create longer and longer paths
			for (int j = 0; j < depth; j++) {
				computePathFreqs(graphs);	

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
						index = pathDict.get(path);
						if (index == null) {
							index = pathDict.size();
							pathDict.put(path, index);
						}
						featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1);
					}

				}

				for (DTLink<PathStringLabel,PathStringLabel> e : graphs.get(i).links()) {	
					e.tag().setNewPaths();	

					for (String path : e.tag().getPaths()) {
						index = pathDict.get(path);
						if (index == null) {
							index = pathDict.size();
							pathDict.put(path, index);
						}
						featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1);
					}
				}
			}
		}

		// Set the correct last index
		for (SparseVector fv : featureVectors) {
			fv.setLastIndex(pathDict.size()-1);
		}

		compTime = System.currentTimeMillis() - tic;

		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		return featureVectors;
	}

	public double[][] compute(GraphList<DTGraph<String,String>> data) {
		double[][] kernel = KernelUtils.initMatrix(data.getGraphs().size(), data.getGraphs().size());
		KernelUtils.computeKernelMatrix(computeFeatureVectors(data), kernel);				
		return kernel;
	}

	private List<DTGraph<PathStringLabel,PathStringLabel>> copyGraphs(List<DTGraph<String,String>> oldGraphs) {
		List<DTGraph<PathStringLabel,PathStringLabel>> newGraphs = new ArrayList<DTGraph<PathStringLabel,PathStringLabel>>();	

		for (DTGraph<String,String> graph : oldGraphs) {
			LightDTGraph<PathStringLabel,PathStringLabel> newGraph = new LightDTGraph<PathStringLabel,PathStringLabel>();
			for (DTNode<String,String> vertex : graph.nodes()) {
				if (!labelDict.containsKey(vertex.label())) {
					labelDict.put(vertex.label(), labelDict.size());
				}
				String lab = "_" + Integer.toString(labelDict.get(vertex.label()));

				newGraph.add(new PathStringLabel(lab));
			}
			for (DTLink<String,String> edge : graph.links()) {
				if (!labelDict.containsKey(edge.tag())) {
					labelDict.put(edge.tag(), labelDict.size());
				}
				String lab = "_" + Integer.toString(labelDict.get(edge.tag()));

				newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), new PathStringLabel(lab)); // ?
			}
			newGraphs.add(newGraph);
		}
		return newGraphs;
	}

	private void computeLabelFreqs(List<DTGraph<PathStringLabel,PathStringLabel>> graphs) {
		// Build a new label Frequencies map
		labelFreq = new HashMap<String, Integer>();

		for (int i = 0; i < graphs.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<PathStringLabel,PathStringLabel> vertex : graphs.get(i).nodes()) {
				String lab = vertex.label().getLabel();
				if (!labelFreq.containsKey(lab)) {
					labelFreq.put(lab, 0);
				} 
				if (!seen.contains(lab)) {
					labelFreq.put(lab, labelFreq.get(lab) + 1);
					seen.add(lab);
				}
			}

			for (DTLink<PathStringLabel,PathStringLabel> edge : graphs.get(i).links()) {
				String lab = edge.tag().getLabel();
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

	private void computePathFreqs(List<DTGraph<PathStringLabel,PathStringLabel>> graphs) {
		// Build a new label Frequencies map
		pathFreq = new HashMap<String, Integer>();

		for (int i = 0; i < graphs.size(); i++) {
			Set<String> seen = new HashSet<String>(); // to track seen label for this instance

			// for each vertex, use the label as index into the feature vector and do a + 1,
			for (DTNode<PathStringLabel,PathStringLabel> vertex : graphs.get(i).nodes()) {
				for (String lab : vertex.label().getPaths()) {
					if (!pathFreq.containsKey(lab)) {
						pathFreq.put(lab, 0);
					} 
					if (!seen.contains(lab)) {
						pathFreq.put(lab, pathFreq.get(lab) + 1);
						seen.add(lab);
					}
				}
			}

			for (DTLink<PathStringLabel,PathStringLabel> edge : graphs.get(i).links()) {
				for (String lab : edge.tag().getPaths()) {
					if (!pathFreq.containsKey(lab)) {
						pathFreq.put(lab, 0);
					} 
					if (!seen.contains(lab)) {
						pathFreq.put(lab, pathFreq.get(lab) + 1);
						seen.add(lab);
					}
				}
			}
		}
	}

	private class PathStringLabel {
		private String label;
		private List<String> paths;
		private List<String> newPaths;

		public PathStringLabel(String label) {
			this.label = label;
			paths = new ArrayList<String>();
			paths.add(new String(label));
			newPaths = new ArrayList<String>();
		}

		public List<String> getPaths() {
			return paths;
		}

		public String getLabel() {
			return label;
		}

		public void addPaths(List<String> paths2) {
			String label = (labelFreq.get(this.label) > minFreq) ? this.label : ""; 

			for (String path : paths2) {
				path = (pathFreq.get(path) > minFreq) ? path : "";
				newPaths.add(label + path);			
			}
		}

		public void setNewPaths() {
			//if (!newPaths.isEmpty()) { // If we add a check on emptiness of newPaths, than we get behavior similar to standard WL. 
			//Without the check, paths will be empty in the next iteration so the same paths will not be propagated again
			paths.clear();
			paths.addAll(newPaths);
			newPaths.clear(); // clearing instead of new to save some GC
			//}					
		}
	}
}
