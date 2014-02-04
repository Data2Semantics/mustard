package org.data2semantics.mustard.kernels.graphkernels;

import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFWLSubTreeKernel implements GraphKernel<RDFData>, FeatureVectorKernel<RDFData> {
	private int depth;
	private String label;
	private boolean inference;
	private RDFDTGraphWLSubTreeKernel kernel;
	private DTGraph<String,String> graph;

	public RDFWLSubTreeKernel(int iterations, int depth, boolean inference, boolean normalize) {
		this(iterations, depth, inference, normalize, false);
	}

	public RDFWLSubTreeKernel(int iterations, int depth, boolean inference, boolean normalize, boolean reverse) {
		super();
		this.label = "RDF_WL_Kernel_" + depth + "_" + iterations;
		this.depth = depth;
		this.inference = inference;

		kernel = new RDFDTGraphWLSubTreeKernel(iterations, depth, normalize, reverse);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		kernel.setNormalize(normalize);
	}

	public SparseVector[] computeFeatureVectors(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel.computeFeatureVectors(new SingleDTGraph(graph, RDFUtils.findInstances(graph, data.getInstances())));
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel.compute(new SingleDTGraph(graph, RDFUtils.findInstances(graph, data.getInstances())));
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REPEAT_LITERALS);
	}

}
