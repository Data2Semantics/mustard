package org.data2semantics.mustard.kernels.rdfgraphkernels;

import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFWLSubTreeKernel implements RDFGraphKernel, RDFFeatureVectorKernel {
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

	public SparseVector[] computeFeatureVectors(RDFDataSet dataset,	List<Resource> instances, List<Statement> blackList) {
		init(dataset, instances, blackList);
		return kernel.computeFeatureVectors(graph, RDFUtils.findInstances(graph, instances));
	}

	public double[][] compute(RDFDataSet dataset, List<Resource> instances,	List<Statement> blackList) {
		init(dataset, instances, blackList);
		return kernel.compute(graph, RDFUtils.findInstances(graph, instances));
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REPEAT_LITERALS);
	}

}
