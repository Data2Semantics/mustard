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
import org.nodes.MapDTGraph;

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
public class RDFDTGraphRootPathCountKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {

	private DTGraph<String,String> rdfGraph;
	private List<DTNode<String,String>> instanceVertices;

	private int pathLength;
	private String label;
	private boolean normalize;
	private boolean withRoot;

	private Map<String, Integer> pathDict;
	private Map<String, Integer> labelDict;



	public RDFDTGraphRootPathCountKernel(int pathLength, boolean withRoot, boolean normalize) {
		this.normalize = normalize;
		this.label = "RDF_DT_Graph_Root_PathCount_Kernel_" + pathLength + "_" + withRoot + "_" + normalize;
		this.withRoot = withRoot;

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
			if (withRoot) {
				countPathRec(featureVectors[i], instanceVertices.get(i), "", pathLength);
			} else {
				countPathRec(featureVectors[i], instanceVertices.get(i), pathLength);
			}
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

	// withRoot == false
	private String countPathRec(SparseVector fv, DTNode<String,String> vertex, int depth) {
		String path = "";
		if (depth > 0) {
			for (DTLink<String,String> edge : vertex.linksOut()) {
				path = countPathRec(fv, edge, depth-1);
			}
		}	

		// Count path
		path = vertex.label() + path;

		if (!pathDict.containsKey(path)) {
			pathDict.put(path, pathDict.size());
		}
		fv.setValue(pathDict.get(path), fv.getValue(pathDict.get(path)) + 1);

		return path;
	}

	// withRoot == false
	private String countPathRec(SparseVector fv, DTLink<String,String> edge, int depth) {
		String path = "";
		if (depth > 0) {
			path = countPathRec(fv, edge.to(), depth-1);
		}	
		// Count path
		path = edge.tag() + path;

		if (!pathDict.containsKey(path)) {
			pathDict.put(path, pathDict.size());
		}
		fv.setValue(pathDict.get(path), fv.getValue(pathDict.get(path)) + 1);
		return path;
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
				countPathRec(fv, edge, "", depth-1);
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
			countPathRec(fv, edge.to(), "", depth-1);
		}	
	}


	private void init(DTGraph<String,String> graph, List<DTNode<String,String>> instances) {
		rdfGraph = new MapDTGraph<String,String>();
		instanceVertices = new ArrayList<DTNode<String,String>>();
		Map<DTNode<String,String>, Integer> instanceIndexMap = new HashMap<DTNode<String,String>, Integer>();

		for (int i = 0; i < instances.size(); i++) {
			instanceIndexMap.put(instances.get(i), i);
			instanceVertices.add(null);
		}

		MapDTGraph<String,String> newGraph = new MapDTGraph<String,String>();
		for (DTNode<String,String> vertex : graph.nodes()) {
			if (!labelDict.containsKey(vertex.label())) {
				labelDict.put(vertex.label(), labelDict.size());
			}
			String lab = "_" + Integer.toString(labelDict.get(vertex.label()));

			if (instanceIndexMap.containsKey(vertex)) {
				instanceVertices.set(instanceIndexMap.get(vertex), newGraph.add(lab));
			} else {
				newGraph.add(lab);
			}


		}
		for (DTLink<String,String> edge : graph.links()) {
			if (!labelDict.containsKey(edge.tag())) {
				labelDict.put(edge.tag(), labelDict.size());
			}
			String lab = "_" + Integer.toString(labelDict.get(edge.tag()));

			newGraph.nodes().get(edge.from().index()).connect(newGraph.nodes().get(edge.to().index()), lab); // ?
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
}
