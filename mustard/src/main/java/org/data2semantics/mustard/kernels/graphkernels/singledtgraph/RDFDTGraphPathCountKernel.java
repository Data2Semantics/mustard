package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.LightDTGraph;

/**
 * This class implements a WL kernel directly on an RDF graph. The difference with a normal WL kernel is that subgraphs are not 
 * explicitly extracted. However we use the idea of subgraph implicitly by tracking for each vertex/edge the distance from an instance vertex.
 * For one thing, this leads to the fact that 1 black list is applied to the entire RDF graph, instead of 1 (small) blacklist per graph. 
 * 
 *
 * 
 * @author Gerben
 *
 */
public class RDFDTGraphPathCountKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {

	private Map<DTNode<PathStringMapLabel,PathStringMapLabel>, Map<DTNode<PathStringMapLabel,PathStringMapLabel>, Integer>> instanceVertexIndexMap;
	private Map<DTNode<PathStringMapLabel,PathStringMapLabel>, Map<DTLink<PathStringMapLabel,PathStringMapLabel>, Integer>> instanceEdgeIndexMap;

	private DTGraph<PathStringMapLabel,PathStringMapLabel> rdfGraph;
	private List<DTNode<PathStringMapLabel,PathStringMapLabel>> instanceVertices;

	private int depth;
	private int pathLength;
	private String label;
	private boolean normalize;

	private Map<String, Integer> pathDict;
	private Map<String, Integer> labelDict;



	public RDFDTGraphPathCountKernel(int pathLength, int depth, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF_DT_Graph_PathCount_Kernel_" + pathLength + "_" + depth + "_" + normalize;

		instanceVertices = new ArrayList<DTNode<PathStringMapLabel,PathStringMapLabel>>();
		this.instanceVertexIndexMap = new HashMap<DTNode<PathStringMapLabel,PathStringMapLabel>, Map<DTNode<PathStringMapLabel,PathStringMapLabel>, Integer>>();
		this.instanceEdgeIndexMap = new HashMap<DTNode<PathStringMapLabel,PathStringMapLabel>, Map<DTLink<PathStringMapLabel,PathStringMapLabel>, Integer>>();

		this.depth = depth;
		this.pathLength = pathLength;
	}

	public String getLabel() {
		return label;
	}

	public void add2Label(String add) {
		this.label += add;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}


	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		pathDict  = new HashMap<String, Integer>();
		labelDict = new HashMap<String, Integer>();
		init(data.getGraph(), data.getInstances());

		// Initialize and compute the featureVectors
		SparseVector[] featureVectors = new SparseVector[data.numInstances()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}

		// initial count
		// Count paths
		Integer index = null;
		for (DTNode<PathStringMapLabel,PathStringMapLabel> v : rdfGraph.nodes()) {
			for (int d : v.label().getPathsMap().keySet()) {
				for (String path : v.label().getPathsMap().get(d)) {
					index = pathDict.get(path);
					if (index == null) {
						index = pathDict.size();
						pathDict.put(path, index);
					}
				}
			}

		}

		for (DTLink<PathStringMapLabel,PathStringMapLabel> e : rdfGraph.links()) {	
			for (int d : e.tag().getPathsMap().keySet()) {
				for (String path : e.tag().getPathsMap().get(d)) {
					index = pathDict.get(path);
					if (index == null) {
						index = pathDict.size();
						pathDict.put(path, index);
					}
				}
			}
		}

		computeFVs(rdfGraph, instanceVertices, featureVectors, pathDict.size()-1);



		// loop to create longer and longer paths
		for (int j = 0; j < pathLength; j++) {

			// Build new paths
			for (DTNode<PathStringMapLabel,PathStringMapLabel> v : rdfGraph.nodes()) {
				for (DTLink<PathStringMapLabel,PathStringMapLabel> e : v.linksOut()) {
					for (int d : v.label().getPathsMap().keySet()) {
						if (d > 0) {
							v.label().addPaths(e.tag().getPathsMap().get(d-1),d);
						}
					}
				}
			}
			for (DTLink<PathStringMapLabel,PathStringMapLabel> e : rdfGraph.links()) {	
				for (int d : e.tag().getPathsMap().keySet()) {
					e.tag().addPaths(e.to().label().getPathsMap().get(d),d);
				}
			}

			// Count paths
			for (DTNode<PathStringMapLabel,PathStringMapLabel> v : rdfGraph.nodes()) {
				v.label().setNewPaths();

				for (int d : v.label().getPathsMap().keySet()) {
					for (String path : v.label().getPathsMap().get(d)) {
						index = pathDict.get(path);
						if (index == null) {
							index = pathDict.size();
							pathDict.put(path, index);
						}
					}
				}
			}
			for (DTLink<PathStringMapLabel,PathStringMapLabel> e : rdfGraph.links()) {	
				e.tag().setNewPaths();
				
				for (int d : e.tag().getPathsMap().keySet()) {
					for (String path : e.tag().getPathsMap().get(d)) {
						index = pathDict.get(path);
						if (index == null) {
							index = pathDict.size();
							pathDict.put(path, index);
						}
					}
				}
			}		
			computeFVs(rdfGraph, instanceVertices, featureVectors, pathDict.size()-1);
		}

		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}


	public double[][] compute(SingleDTGraph data) {
		SparseVector[] featureVectors = computeFeatureVectors(data);
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		computeKernelMatrix(featureVectors, kernel);
		return kernel;
	}



	private void init(DTGraph<String,String> graph, List<DTNode<String,String>> instances) {
		DTNode<PathStringMapLabel,PathStringMapLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<DTNode<PathStringMapLabel,PathStringMapLabel>, Integer> vertexIndexMap;
		Map<DTLink<PathStringMapLabel,PathStringMapLabel>, Integer> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<PathStringMapLabel,PathStringMapLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<PathStringMapLabel,PathStringMapLabel>>();
		Map<DTLink<String,String>, DTLink<PathStringMapLabel,PathStringMapLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<PathStringMapLabel,PathStringMapLabel>>();

		rdfGraph = new LightDTGraph<PathStringMapLabel,PathStringMapLabel>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new HashMap<DTNode<PathStringMapLabel,PathStringMapLabel>, Integer>();
			edgeIndexMap   = new HashMap<DTLink<PathStringMapLabel,PathStringMapLabel>, Integer>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				if (!labelDict.containsKey(oldStartV.label())) {
					labelDict.put(oldStartV.label(), labelDict.size());
				}	
				startV = rdfGraph.add(new PathStringMapLabel(Integer.toString(labelDict.get(oldStartV.label()))));
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().initDepth(depth);
			instanceVertices.add(startV);

			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);

			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.put(startV, depth);

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph
							if (!vertexIndexMap.containsKey(vOldNewMap.get(edge.to()))) { // we have not seen it for this instance
								vertexIndexMap.put(vOldNewMap.get(edge.to()), j);
							}
							vOldNewMap.get(edge.to()).label().initDepth(j); // However, we should always include it in the graph at depth j
						} else {			
							if (!labelDict.containsKey(edge.to().label())) {
								labelDict.put(edge.to().label(), labelDict.size());
							}										
							DTNode<PathStringMapLabel,PathStringMapLabel> newN = rdfGraph.add(new PathStringMapLabel("_" + Integer.toString(labelDict.get(edge.to().label()))));
							newN.label().initDepth(j);
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.put(newN, j);
						}

						if (eOldNewMap.containsKey(edge)) {
							// Process the edge, if we haven't seen it before
							if (!edgeIndexMap.containsKey(eOldNewMap.get(edge))) {
								edgeIndexMap.put(eOldNewMap.get(edge), j);
							}
							eOldNewMap.get(edge).tag().initDepth(j);
						} else {
							if (!labelDict.containsKey(edge.tag())) {
								labelDict.put(edge.tag(), labelDict.size());
							}
							DTLink<PathStringMapLabel,PathStringMapLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new PathStringMapLabel("_" + Integer.toString(labelDict.get(edge.tag()))));
							newE.tag().initDepth(j);
							eOldNewMap.put(edge, newE);
							edgeIndexMap.put(newE, j);
						}

						// Add the vertex to the new front, if we go into a new round
						if (j > 0) {
							newFrontV.add(edge.to());
						}
					}
				}
				frontV = newFrontV;
			}
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
	private void computeFVs(DTGraph<PathStringMapLabel,PathStringMapLabel> graph, List<DTNode<PathStringMapLabel,PathStringMapLabel>> instances, SparseVector[] featureVectors, int lastIndex) {
		int index;
		Map<DTNode<PathStringMapLabel,PathStringMapLabel>, Integer> vertexIndexMap;
		Map<DTLink<PathStringMapLabel,PathStringMapLabel>, Integer> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (DTNode<PathStringMapLabel,PathStringMapLabel> vertex : vertexIndexMap.keySet()) {
				for (String path : vertex.label().getPathsMap().get(vertexIndexMap.get(vertex))) {
					index = pathDict.get(path);
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1.0);
				}
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
			for (DTLink<PathStringMapLabel,PathStringMapLabel> edge : edgeIndexMap.keySet()) {
				for (String path : edge.tag().getPathsMap().get(edgeIndexMap.get(edge))) {
					index = pathDict.get(path);
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + 1.0);
				}
			}
		}
	}

	private void computeKernelMatrix(SparseVector[] featureVectors, double[][] kernel) {
		for (int i = 0; i < featureVectors.length; i++) {
			for (int j = i; j < featureVectors.length; j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}
	}

	private class PathStringMapLabel {
		private String label;
		private Map<Integer, List<String>> pathsMap;
		private Map<Integer, List<String>> newPathsMap;

		public PathStringMapLabel(String label) {
			this.label = label;
			pathsMap = new HashMap<Integer, List<String>>();
			newPathsMap = new HashMap<Integer, List<String>>();
		}

		public void initDepth(int depth) {
			pathsMap.put(depth, new ArrayList<String>());
			newPathsMap.put(depth, new ArrayList<String>());
			pathsMap.get(depth).add(label);
		}

		public Map<Integer, List<String>> getPathsMap() {
			return pathsMap;
		}

		public void addPaths(List<String> paths2, int depth) {
			for (String path : paths2) {
				newPathsMap.get(depth).add(label + path);			}
		}

		public void setNewPaths() {
			for (int d : pathsMap.keySet()) {
				pathsMap.put(d, newPathsMap.get(d));
				newPathsMap.put(d, new ArrayList<String>());
			}
		}
	}
}
