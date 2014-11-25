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
import org.nodes.LightDTGraph;


/**
 *
 * 
 * @author Gerben *
 */
@Deprecated
public class TreePathCountKernel implements GraphKernel<GraphList<DTGraph<String,String>>>, FeatureVectorKernel<GraphList<DTGraph<String,String>>> {
	private int depth;
	protected boolean normalize;
	private Map<String, Integer> pathDict;
	private Map<String, Integer> labelDict;


	/**
	 * Construct a PathCountKernel
	 * 
	 * @param iterations
	 * @param normalize
	 */
	public TreePathCountKernel(int depth, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
	}	

	public TreePathCountKernel(int depth) {
		this(depth, true);
	}

	public String getLabel() {
		return KernelUtils.createLabel(this);		
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}

	public SparseVector[] computeFeatureVectors(GraphList<DTGraph<String,String>> data) {
		pathDict  = new HashMap<String,Integer>();
		labelDict = new HashMap<String,Integer>();
		List<DTGraph<String,String>> graphs = copyGraphs(data.getGraphs());
		
		List<DTNode<String,String>> sf,nsf;
		List<DTLink<String,String>> sflinks;
		
		
		double avgLinks = 0;
		
		// Initialize and compute the featureVectors
		SparseVector[] featureVectors = new SparseVector[data.numInstances()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
			
			countPathRec(featureVectors[i], graphs.get(i).nodes().get(0), "", depth);
			
			sf = new ArrayList<DTNode<String,String>>();
			for (DTLink<String,String> e : graphs.get(i).nodes().get(0).linksOut()) {
				sf.add(e.to());
				//sf.addAll(instanceVertices.get(i).out());
			}
			
			sflinks = new ArrayList<DTLink<String,String>>();
			sflinks.addAll(graphs.get(i).nodes().get(0).linksOut());		
			avgLinks += graphs.get(i).nodes().get(0).linksOut().size();
			
			for (int j = depth - 1; j > 0; j = j - 2) {
				for (DTLink<String,String> e : sflinks) {
					countPathRec(featureVectors[i], e, "", j);
				}
				sflinks = new ArrayList<DTLink<String,String>>();
				nsf = new ArrayList<DTNode<String,String>>();
				
				for (DTNode<String,String> n : sf) {
					countPathRec(featureVectors[i], n, "", j-1);
					if (j - 1 > 0) {
						sflinks.addAll(n.linksOut());
						avgLinks += n.linksOut().size();
						for (DTLink<String,String> e : n.linksOut()) {
							nsf.add(e.to());
							//nsf.addAll(n.out());
						}
					}
				}
				sf = nsf;
			}		
		}
		
		avgLinks /= data.numInstances();
		
		System.out.println("Avg # links: " + avgLinks);
		

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
