package org.data2semantics.mustard.kernels.graphkernels.rdfdata;

import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphHubRemovalWrapperKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFHubRemovalWrapperKernel<K extends GraphKernel<SingleDTGraph>> implements GraphKernel<RDFData> {
	private int depth;
	private boolean inference;
	private DTGraphHubRemovalWrapperKernel<K> kernel2;
	private SingleDTGraph graph;
	
	public RDFHubRemovalWrapperKernel(K kernel, int depth, boolean inference, int[] minHubSizes, boolean normalize) {
		super();
		this.depth = depth;
		this.inference = inference;
		kernel2 = new DTGraphHubRemovalWrapperKernel<K>(kernel, minHubSizes, normalize);
	}

	public RDFHubRemovalWrapperKernel(K kernel, int depth, boolean inference, int minHubSize, boolean normalize) {
		super();
		this.depth = depth;
		this.inference = inference;
		kernel2 = new DTGraphHubRemovalWrapperKernel<K>(kernel, minHubSize, normalize);
	}

	public String getLabel() {
		return KernelUtils.createLabel(this) + "_" + kernel2.getLabel();		
	}

	public void setNormalize(boolean normalize) {
		kernel2.setNormalize(normalize);
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel2.compute(graph);
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, true);
	}
}
