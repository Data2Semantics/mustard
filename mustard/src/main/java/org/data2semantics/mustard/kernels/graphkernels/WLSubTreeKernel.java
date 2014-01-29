package org.data2semantics.mustard.kernels.graphkernels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.weisfeilerlehman.Bucket;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;

import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.nodes.MapDTGraph;


/**
 * Class implementing the Weisfeiler-Lehman graph kernel for Multigraphs with a root node, which occurs in the RDF use case.
 * The current implementation can be made more efficient, since the compute function for test examples recomputes the label dictionary, instead
 * of reusing the one created during training. This makes the applicability of the implementation slightly more general.
 * 
 * TODO include a boolean for saving the labelDict to speed up computation of the kernel in the test phase.
 * 
 * 
 * @author Gerben
 *
 */
public class WLSubTreeKernel implements GraphKernel<DTGraph<String,String>>, FeatureVectorKernel<DTGraph<String,String>> {
	private int iterations = 2;
	protected String label;
	protected boolean normalize;
	private boolean reverse;

	
	public WLSubTreeKernel(int iterations, boolean normalize, boolean reverse) {
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
		this.label = "WL SubTree Kernel, it=" + iterations;
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

	public SparseVector[] computeFeatureVectors(List<DTGraph<String,String>> trainGraphs) {
		List<DTGraph<StringLabel,StringLabel>> graphs = copyGraphs(trainGraphs);
		SparseVector[] featureVectors = new SparseVector[graphs.size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	
		//double[][] featureVectors = new double[graphs.size()][];
		Map<String, String> labelDict = new HashMap<String,String>();

		int startLabel = 1;
		int currentLabel = 1;

		currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
		computeFVs(graphs, featureVectors, 1.0, currentLabel-1);
		// Math.sqrt(1.0 / ((double) (iterations + 1)))

		for (int i = 0; i < this.iterations; i++) {
			relabelGraphs2MultisetLabels(graphs, startLabel, currentLabel);
			startLabel = currentLabel;
			currentLabel = compressGraphLabels(graphs, labelDict, currentLabel);
			computeFVs(graphs, featureVectors, 1.0, currentLabel-1);
			// Math.sqrt((2.0 + i) / ((double) (iterations + 1)))
		}

		if (normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}

		return featureVectors;
	}


	public double[][] compute(List<DTGraph<String,String>> trainGraphs) {
		double[][] kernel = KernelUtils.initMatrix(trainGraphs.size(), trainGraphs.size());
		computeKernelMatrix(computeFeatureVectors(trainGraphs), kernel);				
		return kernel;
	}


	/**
	 * First step in the Weisfeiler-Lehman algorithm, applied to directedgraphs with edge labels.
	 * 
	 * @param graphs
	 * @param startLabel
	 * @param currentLabel
	 */
	private void relabelGraphs2MultisetLabels(List<DTGraph<StringLabel,StringLabel>> graphs, int startLabel, int currentLabel) {
		Map<String, Bucket<DTNode<StringLabel,StringLabel>>> bucketsV = new HashMap<String, Bucket<DTNode<StringLabel,StringLabel>>>();
		Map<String, Bucket<DTLink<StringLabel,StringLabel>>> bucketsE = new HashMap<String, Bucket<DTLink<StringLabel,StringLabel>>>();

		// Initialize buckets
		for (int i = startLabel; i < currentLabel; i++) {
			bucketsV.put(Integer.toString(i), new Bucket<DTNode<StringLabel,StringLabel>>(Integer.toString(i)));
			bucketsE.put(Integer.toString(i), new Bucket<DTLink<StringLabel,StringLabel>>(Integer.toString(i)));
		}

		// 1. Fill buckets 
		if (reverse) { // Labels "travel" in the root direction	
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					bucketsV.get(edge.tag().toString()).getContents().add(edge.from());
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {			
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksIn());
				}	
			}
		} else { // Labels "travel" in the fringe vertices direction
			for (DTGraph<StringLabel,StringLabel> graph : graphs) {
				// Add each edge source (i.e.) start vertex to the bucket of the edge label
				for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
					bucketsV.get(edge.tag().toString()).getContents().add(edge.to());
				}

				// Add each incident edge to the bucket of the node label
				for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {						
					bucketsE.get(vertex.label().toString()).getContents().addAll(vertex.linksOut());
				}	
			}
		}

		// 2. add bucket labels to existing labels
		// Change the original label to a prefix label
		for (DTGraph<StringLabel,StringLabel> graph : graphs) {
			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				edge.tag().append("_");
			}
			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				vertex.label().append("_");
			}
		}

		// 3. Relabel to the labels in the buckets
		for (int i = startLabel; i < currentLabel; i++) {
			// Process vertices
			Bucket<DTNode<StringLabel,StringLabel>> bucketV = bucketsV.get(Integer.toString(i));			
			for (DTNode<StringLabel,StringLabel> vertex : bucketV.getContents()) {
				vertex.label().append(bucketV.getLabel());
				vertex.label().append("_");
			}
			// Process edges
			Bucket<DTLink<StringLabel,StringLabel>> bucketE = bucketsE.get(Integer.toString(i));			
			for (DTLink<StringLabel,StringLabel> edge : bucketE.getContents()) {
				edge.tag().append(bucketE.getLabel());
				edge.tag().append("_");
			}
		}
	}

	/**
	 * Second step in the WL algorithm. We compress the long labels into new short labels
	 * 
	 * @param graphs
	 * @param labelDict
	 * @param currentLabel
	 * @return
	 */
	private int compressGraphLabels(List<DTGraph<StringLabel,StringLabel>> graphs, Map<String, String> labelDict, int currentLabel) {
		String label;

		for (DTGraph<StringLabel,StringLabel> graph : graphs) {

			for (DTLink<StringLabel,StringLabel> edge : graph.links()) {
				label = labelDict.get(edge.tag().toString());						
				if (label == null) {					
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(edge.tag().toString(), label);				
				}
				edge.tag().clear();
				edge.tag().append(label);			
			}

			for (DTNode<StringLabel,StringLabel> vertex : graph.nodes()) {
				label = labelDict.get(vertex.label().toString());
				if (label == null) {
					label = Integer.toString(currentLabel);
					currentLabel++;
					labelDict.put(vertex.label().toString(), label);
				}
				vertex.label().clear();
				vertex.label().append(label);
			}
		}
		return currentLabel;
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
				//kernel[i][j] += dotProduct(featureVectors[i], featureVectors[j]) * (((double) iteration) / ((double) this.iterations+1));
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
