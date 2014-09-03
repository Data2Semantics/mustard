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
import org.data2semantics.mustard.util.Pair;
import org.data2semantics.mustard.weisfeilerlehman.MapLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphMapLabelIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
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
public class RDFDTGraphTreeWLSubTreeKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph> {

	private Map<DTNode<MapLabel,MapLabel>, List<Pair<DTNode<MapLabel,MapLabel>, Integer>>> instanceVertexIndexMap;
	private Map<DTNode<MapLabel,MapLabel>, List<Pair<DTLink<MapLabel,MapLabel>, Integer>>> instanceEdgeIndexMap;

	private DTGraph<MapLabel,MapLabel> rdfGraph;
	private List<DTNode<MapLabel,MapLabel>> instanceVertices;
	
	private int depth;
	private int iterations;
	private String label;
	private boolean normalize;
	private boolean reverse;
	private boolean iterationWeighting;


	public RDFDTGraphTreeWLSubTreeKernel(int iterations, int depth, boolean reverse, boolean iterationWeighting, boolean normalize) {
		this(iterations, depth, normalize);
		this.reverse = reverse;
		this.iterationWeighting = iterationWeighting;
		this.label = "RDF_DT_Graph_Tree_WL_Kernel_" + iterations + "_" + depth + "_" + reverse + "_" + iterationWeighting;
	}


	public RDFDTGraphTreeWLSubTreeKernel(int iterations, int depth, boolean normalize) {
		this.normalize = normalize;
		this.reverse = false;
		this.iterationWeighting = false;
		this.label = "RDF_DT_Graph_Tree_WL_Kernel_" + iterations + "_" + depth + "_" + reverse + "_" + iterationWeighting;

		instanceVertices = new ArrayList<DTNode<MapLabel,MapLabel>>();
		this.instanceVertexIndexMap = new HashMap<DTNode<MapLabel,MapLabel>, List<Pair<DTNode<MapLabel,MapLabel>, Integer>>>();
		this.instanceEdgeIndexMap = new HashMap<DTNode<MapLabel,MapLabel>, List<Pair<DTLink<MapLabel,MapLabel>, Integer>>>();

		this.depth = depth;
		this.iterations = iterations;
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

	public void setReverse(boolean reverse) {
		this.reverse = reverse;
	}
	
	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		SparseVector[] featureVectors = new SparseVector[data.getInstances().size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	
		
		init(data.getGraph(), data.getInstances());
		WeisfeilerLehmanIterator<DTGraph<MapLabel,MapLabel>> wl = new WeisfeilerLehmanDTGraphMapLabelIterator(reverse);
		
		List<DTGraph<MapLabel,MapLabel>> gList = new ArrayList<DTGraph<MapLabel,MapLabel>>();
		gList.add(rdfGraph);
		
		wl.wlInitialize(gList);
		
		double weight = 1.0;
		if (iterationWeighting) {
			weight = Math.sqrt(1.0 / ((double) (iterations + 1)));
		}
		
		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);
		
		for (int i = 0; i < iterations; i++) {
			if (iterationWeighting) {
				weight = Math.sqrt((2.0 + i) / ((double) (iterations + 1)));
			}
			wl.wlIterate(gList);
			computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1);
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
		DTNode<MapLabel,MapLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		List<Pair<DTNode<MapLabel,MapLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<MapLabel,MapLabel>, Integer>> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<MapLabel,MapLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<MapLabel,MapLabel>>();
		Map<DTLink<String,String>, DTLink<MapLabel,MapLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<MapLabel,MapLabel>>();
		
		rdfGraph = new MapDTGraph<MapLabel,MapLabel>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new ArrayList<Pair<DTNode<MapLabel,MapLabel>, Integer>>();
			edgeIndexMap   = new ArrayList<Pair<DTLink<MapLabel,MapLabel>, Integer>>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new MapLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().put(depth, new StringBuilder(oldStartV.label()));
			instanceVertices.add(startV);
			
			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);
			
			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.add(new Pair<DTNode<MapLabel,MapLabel>,Integer>(startV, depth));

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph
							vertexIndexMap.add(new Pair<DTNode<MapLabel,MapLabel>,Integer>(vOldNewMap.get(edge.to()), j));  
							vOldNewMap.get(edge.to()).label().put(j, new StringBuilder(edge.to().label())); 
						} else {
							DTNode<MapLabel,MapLabel> newN = rdfGraph.add(new MapLabel());
							newN.label().put(j, new StringBuilder(edge.to().label()));
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.add(new Pair<DTNode<MapLabel,MapLabel>,Integer>(newN, j)); 
						}
						
						if (eOldNewMap.containsKey(edge)) {
							edgeIndexMap.add(new Pair<DTLink<MapLabel,MapLabel>,Integer>(eOldNewMap.get(edge),j)); 		
							eOldNewMap.get(edge).tag().put(j, new StringBuilder(edge.tag()));
						} else {
							DTLink<MapLabel,MapLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new MapLabel());
							newE.tag().put(j, new StringBuilder(edge.tag()));
							eOldNewMap.put(edge, newE);
							edgeIndexMap.add(new Pair<DTLink<MapLabel,MapLabel>,Integer>(newE, j));
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
	private void computeFVs(DTGraph<MapLabel,MapLabel> graph, List<DTNode<MapLabel,MapLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex) {
		int index;
		List<Pair<DTNode<MapLabel,MapLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<MapLabel,MapLabel>, Integer>> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (Pair<DTNode<MapLabel,MapLabel>, Integer> vertex : vertexIndexMap) {
				index = Integer.parseInt(vertex.getFirst().label().get(vertex.getSecond()).toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
			for (Pair<DTLink<MapLabel,MapLabel>, Integer> edge : edgeIndexMap) {
				index = Integer.parseInt(edge.getFirst().tag().get(edge.getSecond()).toString());
				featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
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
}
