package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.utils.Pair;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;

import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
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
public class DTGraphTreeWLSubTreeOneGraphKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker {

	private Map<DTNode<StringLabel,StringLabel>, List<Pair<DTNode<StringLabel,StringLabel>, Integer>>> instanceVertexIndexMap;
	private Map<DTNode<StringLabel,StringLabel>, List<Pair<DTLink<StringLabel,StringLabel>, Integer>>> instanceEdgeIndexMap;

	private DTGraph<StringLabel,StringLabel> rdfGraph;
	private List<DTNode<StringLabel,StringLabel>> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean reverse;
	private boolean iterationWeighting;
	private boolean trackPrevNBH;
	private long compTime;

	public DTGraphTreeWLSubTreeOneGraphKernel(int iterations, int depth, boolean reverse, boolean iterationWeighting, boolean trackPrevNBH, boolean normalize) {
		this.reverse = reverse;
		this.iterationWeighting = iterationWeighting;
		this.trackPrevNBH = trackPrevNBH;
		this.normalize = normalize;
		this.depth = depth;
		this.iterations = iterations;
	}


	public DTGraphTreeWLSubTreeOneGraphKernel(int iterations, int depth, boolean reverse, boolean iterationWeighting, boolean normalize) {
		this(iterations, depth, reverse, iterationWeighting, false, normalize);
	}


	public DTGraphTreeWLSubTreeOneGraphKernel(int iterations, int depth, boolean normalize) {
		this(iterations, depth, false, false, false, normalize);
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


	public SparseVector[] computeFeatureVectors(SingleDTGraph data) {
		SparseVector[] featureVectors = new SparseVector[data.getInstances().size()];
		for (int i = 0; i < featureVectors.length; i++) {
			featureVectors[i] = new SparseVector();
		}	

		init(data.getGraph(), data.getInstances());
			
		WeisfeilerLehmanIterator<DTGraph<StringLabel,StringLabel>> wl = new WeisfeilerLehmanDTGraphIterator(reverse, trackPrevNBH);

		List<DTGraph<StringLabel,StringLabel>> gList = new ArrayList<DTGraph<StringLabel,StringLabel>>();
		gList.add(rdfGraph);
		
		long tic = System.currentTimeMillis();

		wl.wlInitialize(gList);

		double weight = 1.0;
		if (iterationWeighting) {
			weight = Math.sqrt(1.0 / (iterations + 1));
		}
		
		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, 0);

		for (int i = 0; i < iterations; i++) {
			if (iterationWeighting) {
				weight = Math.sqrt((2.0 + i) / (iterations + 1));
			}
			wl.wlIterate(gList);
			computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, i+1);
		}
		
		compTime = System.currentTimeMillis() - tic;
		
		if (this.normalize) {
			featureVectors = KernelUtils.normalize(featureVectors);
		}
		return featureVectors;
	}


	public double[][] compute(SingleDTGraph data) {
		SparseVector[] featureVectors = computeFeatureVectors(data);
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		long tic = System.currentTimeMillis();
		kernel = KernelUtils.computeKernelMatrix(featureVectors, kernel);
		compTime += System.currentTimeMillis() - tic;
		return kernel;
	}



	private void init(DTGraph<String,String> graph, List<DTNode<String,String>> instances) {
		DTNode<StringLabel,StringLabel> startV;
		List<DTNode<String,String>> frontV, newFrontV;
		List<Pair<DTNode<StringLabel,StringLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<StringLabel,StringLabel>, Integer>> edgeIndexMap;
		Map<DTNode<String,String>, DTNode<StringLabel,StringLabel>> vOldNewMap = new HashMap<DTNode<String,String>,DTNode<StringLabel,StringLabel>>();
		Map<DTLink<String,String>, DTLink<StringLabel,StringLabel>> eOldNewMap = new HashMap<DTLink<String,String>,DTLink<StringLabel,StringLabel>>();

		instanceVertices = new ArrayList<DTNode<StringLabel,StringLabel>>();
		instanceVertexIndexMap = new HashMap<DTNode<StringLabel,StringLabel>, List<Pair<DTNode<StringLabel,StringLabel>, Integer>>>();
		instanceEdgeIndexMap = new HashMap<DTNode<StringLabel,StringLabel>, List<Pair<DTLink<StringLabel,StringLabel>, Integer>>>();
		rdfGraph = new LightDTGraph<StringLabel,StringLabel>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new ArrayList<Pair<DTNode<StringLabel,StringLabel>, Integer>>();
			edgeIndexMap   = new ArrayList<Pair<DTLink<StringLabel,StringLabel>, Integer>>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.add(new StringLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().clear();
			startV.label().append(oldStartV.label());
			instanceVertices.add(startV);

			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);

			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.add(new Pair<DTNode<StringLabel,StringLabel>,Integer>(startV, depth));

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph
							vertexIndexMap.add(new Pair<DTNode<StringLabel,StringLabel>,Integer>(vOldNewMap.get(edge.to()), j));  
							vOldNewMap.get(edge.to()).label().clear();
							vOldNewMap.get(edge.to()).label().append(edge.to().label());
						} else {
							DTNode<StringLabel,StringLabel> newN = rdfGraph.add(new StringLabel());
							newN.label().clear();
							newN.label().append(edge.to().label());
							vOldNewMap.put(edge.to(), newN);
							vertexIndexMap.add(new Pair<DTNode<StringLabel,StringLabel>,Integer>(newN, j)); 
						}

						if (eOldNewMap.containsKey(edge)) {
							edgeIndexMap.add(new Pair<DTLink<StringLabel,StringLabel>,Integer>(eOldNewMap.get(edge),j)); 		
							eOldNewMap.get(edge).tag().clear();
							eOldNewMap.get(edge).tag().append(edge.tag());
						} else {
							DTLink<StringLabel,StringLabel> newE = vOldNewMap.get(qV).connect(vOldNewMap.get(edge.to()), new StringLabel());
							newE.tag().clear();
							newE.tag().append(edge.tag());
							eOldNewMap.put(edge, newE);
							edgeIndexMap.add(new Pair<DTLink<StringLabel,StringLabel>,Integer>(newE, j));
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
	 * @param graph
	 * @param instances
	 * @param weight
	 * @param featureVectors
	 */
	private void computeFVs(DTGraph<StringLabel,StringLabel> graph, List<DTNode<StringLabel,StringLabel>> instances, double weight, SparseVector[] featureVectors, int lastIndex, int currentIt) {
		int index, depth;
		List<Pair<DTNode<StringLabel,StringLabel>, Integer>> vertexIndexMap;
		List<Pair<DTLink<StringLabel,StringLabel>, Integer>> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			for (Pair<DTNode<StringLabel,StringLabel>, Integer> vertex : vertexIndexMap) {
				depth = vertex.getSecond();
				if (!trackPrevNBH || !vertex.getFirst().label().isSameAsPrev()) {
					index = Integer.parseInt(vertex.getFirst().label().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));
			for (Pair<DTLink<StringLabel,StringLabel>, Integer> edge : edgeIndexMap) {
				depth = edge.getSecond();
				if (!trackPrevNBH || !edge.getFirst().tag().isSameAsPrev()) {
					index = Integer.parseInt(edge.getFirst().tag().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + weight);
				}
			}
		}
	}
}
