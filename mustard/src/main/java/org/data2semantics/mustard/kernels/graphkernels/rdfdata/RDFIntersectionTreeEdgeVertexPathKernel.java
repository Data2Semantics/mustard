package org.data2semantics.mustard.kernels.graphkernels.rdfdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

/**
 * Implementation of the Intersection Tree Path kernel, as described in the DMoLD paper. This is the variant in the paper, with path's described by edge and vertex labels.
 * With the probabilities boolean in the constructor set to true, the values in the featureVectors will be computed as transition probabilities.
 * 
 * The Intersection Tree Path kernel has performance comparable to the IntersectionSubTree kernel but is much, much faster to compute.
 * 
 * @author Gerben
 *
 */
@Deprecated
public class RDFIntersectionTreeEdgeVertexPathKernel implements GraphKernel<RDFData>, FeatureVectorKernel<RDFData> {
	private int depth;
	private boolean inference;
	protected Map<Value, Integer> uri2int;
	private Map<List<Integer>, Integer> path2index;
	private Map<Integer, List<Integer>> index2path;
	private RDFDataSet dataset;
	private Set<Statement> blackList;
	private Set<Resource> instances;
	private boolean normalize;
	private String label;
	protected int pathLen;
	private Value rootValue;
	private boolean probabilities;
	
	
	public RDFIntersectionTreeEdgeVertexPathKernel(int depth, boolean inference, boolean normalize) {
		this(depth, false, inference, normalize);
	}
	
	public RDFIntersectionTreeEdgeVertexPathKernel(int depth, boolean probabilities, boolean inference, boolean normalize) {
		this.normalize = normalize;
		this.depth = depth;
		this.inference = inference;
		this.probabilities = probabilities;

		uri2int = new HashMap<Value, Integer>();
		path2index = new HashMap<List<Integer>, Integer>();
		index2path = new HashMap<Integer, List<Integer>>();
		blackList = new HashSet<Statement>();
		instances = new HashSet<Resource>();
		this.pathLen = 2;
		
		this.label = "IPT_" + depth + "_" + probabilities + "_" + inference + "_" + normalize;
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;		
	}
	
	public SparseVector[] computeFeatureVectors(RDFData data) {
		this.dataset = data.getDataset();	
		this.blackList.addAll(data.getBlackList());
		this.instances.addAll(data.getInstances());

		rootValue = dataset.createLiteral(KernelUtils.ROOTID);
		
		SparseVector[] ret = new SparseVector[data.getInstances().size()];

		for (int i = 0; i < data.getInstances().size(); i++) {
			ret[i] = processVertex(data.getInstances().get(i));
		}
		
		for (SparseVector fv : ret) {
			fv.setLastIndex(path2index.size());
		}
		if (normalize) {
			ret = KernelUtils.normalize(ret);
		}
		return ret;
	}
	
	private SparseVector processVertex(Resource root) {
		SparseVector features = new SparseVector();
		processVertexRec(root, new ArrayList<Integer>(), features, depth, root);
		if (probabilities) {
			features = normalizeFeatures(features);
		}
		return features;
	}
	
	private SparseVector normalizeFeatures(SparseVector features) {
		SparseVector res = new SparseVector();
		for (int key : features.getIndices()) {
			List<Integer> path = index2path.get(key);
			if (path.size()==0) {
				res.setValue(key, 1.0);
			} else {
				List<Integer> parent = path.subList(0, path.size()-pathLen);
				int parentKey = path2index.get(parent);
				res.setValue(key, features.getValue(key)/features.getValue(parentKey));
			}
		}
		return res;
	}
	
	private void processVertexRec(Value v1, List<Integer> path, SparseVector vec, int maxDepth, Resource instance) {

		// Count
		Integer index = path2index.get(path);
		if (index == null) {
			index = path2index.size()+1;
			path2index.put(path, index);
			index2path.put(index, path);
		}
		vec.setValue(index, vec.getValue(index)+1);

		// Bottom out
		if (maxDepth > 0 && (v1 instanceof Resource)) {

			// Recurse
			List<Statement> result = dataset.getStatements((Resource)v1, null, null, inference);

			for (Statement stmt : result) {
				if (!blackList.contains(stmt)) {
					List<Integer> newPath = createPath(stmt, path, instance);
					processVertexRec(stmt.getObject(), newPath, vec, maxDepth-1, instance);
				}
			}		
		}
	}
	
	protected List<Integer> createPath(Statement stmt, List<Integer> path, Resource instance) {
		
		Integer key = uri2int.get(stmt.getPredicate());
		if (key == null) {
			key = new Integer(uri2int.size());
			uri2int.put(stmt.getPredicate(), key);
		}
		
		// Set the instance nodes to one identical rootValue node
		Value obj = stmt.getObject();
		if (obj instanceof Resource && instances.contains((Resource) obj)) {
			obj = rootValue;
		}
		
		Integer key2 = uri2int.get(obj);
		if (key2 == null) {
			key2 = new Integer(uri2int.size());
			uri2int.put(obj, key2);
		}
		
		List<Integer> newPath = new ArrayList<Integer>(path);
		newPath.add(key);
		newPath.add(key2);

		return newPath;
	}
	
	public double[][] compute(RDFData data) {		
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		SparseVector[] featureVectors = computeFeatureVectors(data);
		for (int i = 0; i < data.getInstances().size(); i++) {
			for (int j = i; j < data.getInstances().size(); j++) {
				kernel[i][j] += featureVectors[i].dot(featureVectors[j]);
				kernel[j][i] = kernel[i][j];
			}
		}		
		return kernel;
	}

}
