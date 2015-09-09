package org.data2semantics.mustard.kernels.graphkernels.rdfdata;

import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.ComputationTimeTracker;
import org.data2semantics.mustard.kernels.FeatureInspector;
import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeIDEQKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Wrapper kernel for {@link org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeIDEQKernel}. 
 * 
 * @author Gerben
 */
public class RDFWLSubTreeIDEQKernel implements GraphKernel<RDFData>, FeatureVectorKernel<RDFData>, ComputationTimeTracker, FeatureInspector {
	private int depth;
	private boolean inference;
	private DTGraphWLSubTreeIDEQKernel kernel;
	private SingleDTGraph graph;

	public RDFWLSubTreeIDEQKernel(int iterations, int depth, boolean inference, boolean reverse, boolean noDuplicateSubtrees, boolean normalize) {
		super();
		this.depth = depth;
		this.inference = inference;

		kernel = new DTGraphWLSubTreeIDEQKernel(iterations, depth, reverse, noDuplicateSubtrees, normalize);
	}
	
	public String getLabel() {
		return KernelUtils.createLabel(this) + "_" + kernel.getLabel();			
	}

	public void setNormalize(boolean normalize) {
		kernel.setNormalize(normalize);
	}

	public SparseVector[] computeFeatureVectors(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel.computeFeatureVectors(graph);
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel.compute(graph);
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, true);
	}

	public long getComputationTime() {
		return kernel.getComputationTime();
	}

	public List<String> getFeatureDescriptions(List<Integer> indicesSV) {
		return kernel.getFeatureDescriptions(indicesSV);
	}
	
	
	
}
