package org.data2semantics.mustard.kernels.graphkernels.singledtgraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.simplegraph.SimpleGraph;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.data2semantics.mustard.weisfeilerlehman.WLUtils;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanApproxIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanDTGraphIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanIterator;
import org.data2semantics.mustard.weisfeilerlehman.WeisfeilerLehmanSimpleGraphIterator;
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
public class DTGraphWLSubTreeGeoProbKernel implements GraphKernel<SingleDTGraph>, FeatureVectorKernel<SingleDTGraph>, ComputationTimeTracker, FeatureInspector {

	private Map<SimpleGraph<StringLabel,StringLabel>.Node, Map<SimpleGraph<StringLabel,StringLabel>.Node, Integer>> instanceVertexIndexMap;
	private Map<SimpleGraph<StringLabel,StringLabel>.Node, Map<SimpleGraph<StringLabel,StringLabel>.Link, Integer>> instanceEdgeIndexMap;

	//private Map<DTNode<StringLabel,StringLabel>, Map<DTNode<StringLabel,StringLabel>, Boolean>> instanceVertexIgnoreMap;
	//private Map<DTNode<StringLabel,StringLabel>, Map<DTLink<StringLabel,StringLabel>, Boolean>> instanceEdgeIgnoreMap;

	private SimpleGraph<StringLabel,StringLabel> rdfGraph;
	private List<SimpleGraph<StringLabel,StringLabel>.Node> instanceVertices;

	private int depth;
	private int iterations;
	private boolean normalize;
	private boolean reverse;
	private boolean iterationWeighting;

	private long compTime;
	private Map<String,String> dict;

	private double p;
	private double mean;
	private Map<Integer, Double> probs;


	public DTGraphWLSubTreeGeoProbKernel(int iterations, int depth, boolean reverse, boolean iterationWeighting, double mean, boolean normalize) {
		this.reverse = reverse;
		this.iterationWeighting = iterationWeighting;
		this.normalize = normalize;
		this.depth = depth;
		this.iterations = iterations;
		this.mean = mean;
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

		probs = new HashMap<Integer, Double>();
		p = 1.0 / (mean + 1.0); // mean is (1-p)/p


		System.out.println("Depth threshold info");

		for (int i = 0; i < 20; i++) {
			System.out.print(i + ": " + getCumProb(i) + ", ");
		}
		System.out.println("");

		long tic2 = System.currentTimeMillis();

		init(data.getGraph(), data.getInstances());

		System.out.println("DTGraph init (ms): " + (System.currentTimeMillis() - tic2));

		WeisfeilerLehmanIterator<SimpleGraph<StringLabel,StringLabel>> wl = new WeisfeilerLehmanSimpleGraphIterator(reverse, true);

		List<SimpleGraph<StringLabel,StringLabel>> gList = new ArrayList<SimpleGraph<StringLabel,StringLabel>>();
		gList.add(rdfGraph);

		long tic = System.currentTimeMillis();
		wl.wlInitialize(gList);
		compTime = System.currentTimeMillis() - tic;

		double weight = 1.0;
		if (iterationWeighting) {
			weight = Math.sqrt(1.0 / (iterations + 1));
		}


		computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, 0);

		for (int i = 0; i < iterations; i++) {
			if (iterationWeighting) {
				weight = Math.sqrt((2.0 + i) / (iterations + 1));
			}

			tic = System.currentTimeMillis();
			wl.wlIterate(gList);
			compTime += System.currentTimeMillis() - tic;

			computeFVs(rdfGraph, instanceVertices, weight, featureVectors, wl.getLabelDict().size()-1, i + 1);
		}

		//compTime = System.currentTimeMillis() - tic;

		System.out.println("DTGraph WL (ms): " + compTime);

		// Set the reverse label dict, to reverse engineer the features
		dict = new HashMap<String,String>();
		for (String key : wl.getLabelDict().keySet()) {
			dict.put(wl.getLabelDict().get(key), key);
		}

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
		SimpleGraph<StringLabel,StringLabel>.Node startV;
		List<DTNode<String,String>> frontV, newFrontV;
		Map<SimpleGraph<StringLabel,StringLabel>.Node, Integer> vertexIndexMap;
		Map<SimpleGraph<StringLabel,StringLabel>.Link, Integer> edgeIndexMap;
		//Map<DTNode<StringLabel,StringLabel>, Boolean> vertexIgnoreMap;
		//Map<DTLink<StringLabel,StringLabel>, Boolean> edgeIgnoreMap;
		Map<DTNode<String,String>, SimpleGraph<StringLabel,StringLabel>.Node> vOldNewMap = new HashMap<DTNode<String,String>,SimpleGraph<StringLabel,StringLabel>.Node>();
		Map<DTLink<String,String>, SimpleGraph<StringLabel,StringLabel>.Link> eOldNewMap = new HashMap<DTLink<String,String>,SimpleGraph<StringLabel,StringLabel>.Link>();

		rdfGraph = new SimpleGraph<StringLabel,StringLabel>();
		instanceVertices        = new ArrayList<SimpleGraph<StringLabel,StringLabel>.Node>();
		instanceVertexIndexMap  = new HashMap<SimpleGraph<StringLabel,StringLabel>.Node, Map<SimpleGraph<StringLabel,StringLabel>.Node, Integer>>();
		instanceEdgeIndexMap    = new HashMap<SimpleGraph<StringLabel,StringLabel>.Node, Map<SimpleGraph<StringLabel,StringLabel>.Link, Integer>>();
		//instanceVertexIgnoreMap = new HashMap<DTNode<StringLabel,StringLabel>, Map<DTNode<StringLabel,StringLabel>, Boolean>>();
		//instanceEdgeIgnoreMap   = new HashMap<DTNode<StringLabel,StringLabel>, Map<DTLink<StringLabel,StringLabel>, Boolean>>();

		for (DTNode<String,String> oldStartV : instances) {				
			vertexIndexMap = new HashMap<SimpleGraph<StringLabel,StringLabel>.Node, Integer>();
			edgeIndexMap   = new HashMap<SimpleGraph<StringLabel,StringLabel>.Link, Integer>();
			//vertexIgnoreMap = new HashMap<DTNode<StringLabel,StringLabel>, Boolean>();
			//edgeIgnoreMap   = new HashMap<DTLink<StringLabel,StringLabel>, Boolean>();

			// Get the start node
			if (vOldNewMap.containsKey(oldStartV)) {
				startV = vOldNewMap.get(oldStartV);
			} else { 
				startV = rdfGraph.new Node(new StringLabel());
				vOldNewMap.put(oldStartV, startV);
			}
			startV.label().clear();
			startV.label().append(oldStartV.label());

			instanceVertices.add(startV);

			instanceVertexIndexMap.put(startV, vertexIndexMap);
			instanceEdgeIndexMap.put(startV, edgeIndexMap);
			//instanceVertexIgnoreMap.put(startV, vertexIgnoreMap);
			//instanceEdgeIgnoreMap.put(startV, edgeIgnoreMap);

			frontV = new ArrayList<DTNode<String,String>>();
			frontV.add(oldStartV);

			// Process the start node
			vertexIndexMap.put(startV, depth);
			//vertexIgnoreMap.put(startV, false);

			for (int j = depth - 1; j >= 0; j--) {
				newFrontV = new ArrayList<DTNode<String,String>>();
				for (DTNode<String,String> qV : frontV) {
					for (DTLink<String,String> edge : qV.linksOut()) {
						if (!vertexIndexMap.containsKey(vOldNewMap.get(edge.to())) || !reverse) { // we have not seen it for this instance or labels travel to the fringe vertices, in which case we want to have the lowest depth encounter
							if (vOldNewMap.containsKey(edge.to())) { // This vertex has been added to rdfGraph						
								vertexIndexMap.put(vOldNewMap.get(edge.to()), j);
								//vertexIgnoreMap.put(vOldNewMap.get(edge.to()), false);
							}
							//vOldNewMap.get(edge.to()).label().clear();
							//vOldNewMap.get(edge.to()).label().append(edge.to().label()); 
							else {
								SimpleGraph<StringLabel,StringLabel>.Node newN = rdfGraph.new Node(new StringLabel());
								newN.label().clear();
								newN.label().append(edge.to().label());
								vOldNewMap.put(edge.to(), newN);
								vertexIndexMap.put(newN, j);
								//vertexIgnoreMap.put(newN, false);
							}
						}

						if (!edgeIndexMap.containsKey(eOldNewMap.get(edge)) || !reverse) { // see comment for vertices
							if (eOldNewMap.containsKey(edge)) {
								// Process the edge, if we haven't seen it before
								edgeIndexMap.put(eOldNewMap.get(edge), j);
								//edgeIgnoreMap.put(eOldNewMap.get(edge), false);
							}
							//eOldNewMap.get(edge).tag().clear();
							//eOldNewMap.get(edge).tag().append(edge.tag());
							else {
								SimpleGraph<StringLabel,StringLabel>.Link newE = rdfGraph.new Link(vOldNewMap.get(qV), vOldNewMap.get(edge.to()), new StringLabel());
								newE.tag().clear();
								newE.tag().append(edge.tag());
								eOldNewMap.put(edge, newE);
								edgeIndexMap.put(newE, j);
								//edgeIgnoreMap.put(newE, false);
							}
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
	private void computeFVs(SimpleGraph<StringLabel,StringLabel> graph, List<SimpleGraph<StringLabel,StringLabel>.Node> instances, double weight, SparseVector[] featureVectors, int lastIndex, int currentIt) {
		int index, depth;
		Map<SimpleGraph<StringLabel,StringLabel>.Node, Integer> vertexIndexMap;
		Map<SimpleGraph<StringLabel,StringLabel>.Link, Integer> edgeIndexMap;

		for (int i = 0; i < instances.size(); i++) {
			featureVectors[i].setLastIndex(lastIndex);

			vertexIndexMap = instanceVertexIndexMap.get(instances.get(i));
			edgeIndexMap = instanceEdgeIndexMap.get(instances.get(i));

			for (SimpleGraph<StringLabel,StringLabel>.Node vertex : vertexIndexMap.keySet()) {
				depth = vertexIndexMap.get(vertex);

				if (!vertex.label().isSameAsPrev() && (depth * 2) >= currentIt) { 
					index = Integer.parseInt(vertex.label().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + getProb(((this.depth - depth) * 2) + currentIt)); // depth counts only vertices, we want it combined vert + edges here
				}
			}

			for (SimpleGraph<StringLabel,StringLabel>.Link edge : edgeIndexMap.keySet()) {
				depth = edgeIndexMap.get(edge);

				if (!edge.tag().isSameAsPrev() && ((depth * 2)+1) >= currentIt) { 
					index = Integer.parseInt(edge.tag().toString());
					featureVectors[i].setValue(index, featureVectors[i].getValue(index) + getProb(((this.depth - depth) * 2) - 1 + currentIt)); // see above
				}
			}
		}
	}


	public List<String> getFeatureDescriptions(List<Integer> indices) {
		if (dict == null) {
			throw new RuntimeException("Should run computeFeatureVectors first");
		} else {
			List<String> desc = new ArrayList<String>();

			for (int index : indices) {
				desc.add(WLUtils.getFeatureDecription(dict, index));
			}
			return desc;
		}
	}


	/**
	 * from wikipedia on geometric dist.
	 * 
	 * @param depth
	 * @return
	 */
	private double getProb(int depth) {
		if (!probs.containsKey(depth)) { // do caching
			probs.put(depth, Math.pow(1-p, depth) * p);
		}
		return probs.get(depth);		
	}


	private double getCumProb(int depth) {
		return 1-Math.pow(1-p, depth+1);		
	}


}
