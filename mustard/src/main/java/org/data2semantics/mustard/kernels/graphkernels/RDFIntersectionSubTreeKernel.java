package org.data2semantics.mustard.kernels.graphkernels;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFIntersectionSubTreeKernel implements GraphKernel<RDFData> {
	private int depth;
	private String label;
	private boolean inference;
	private RDFDTGraphIntersectionSubTreeKernel kernel;
	private DTGraph<String,String> graph;
	private List<DTNode<String,String>> instanceNodes;

	public RDFIntersectionSubTreeKernel(int depth, double discountFactor, boolean inference, boolean normalize) {
		super();
		this.label = "RDF_IST_Kernel_" + depth + "_" + discountFactor + "_" + inference + "_" + normalize;
		this.depth = depth;
		this.inference = inference;

		kernel = new RDFDTGraphIntersectionSubTreeKernel(depth, discountFactor, normalize);
	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		kernel.setNormalize(normalize);
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel.compute(new SingleDTGraph(graph, instanceNodes));
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		instanceNodes = new ArrayList<DTNode<String,String>>();
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, instanceNodes, false);
	
		//instanceNodes = RDFUtils.findInstances(graph, instances);
		//graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
	}	
}
