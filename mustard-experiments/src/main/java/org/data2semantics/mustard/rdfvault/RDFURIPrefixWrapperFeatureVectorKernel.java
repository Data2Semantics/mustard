package org.data2semantics.mustard.rdfvault;

import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.SparseVector;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphHubRemovalWrapperFeatureVectorKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.nodes.DTLink;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RDFURIPrefixWrapperFeatureVectorKernel<K extends FeatureVectorKernel<SingleDTGraph>> implements GraphKernel<RDFData>, FeatureVectorKernel<RDFData> {
	private int depth;
	private boolean inference;
	private DTGraphHubRemovalWrapperFeatureVectorKernel<K> kernel2;
	private SingleDTGraph graph;
	private K kernel;

	public RDFURIPrefixWrapperFeatureVectorKernel(K kernel, int depth, boolean inference) {
		super();
		this.depth = depth;
		this.inference = inference;
		this.kernel = kernel;
	}


	public String getLabel() {
		return KernelUtils.createLabel(this) + "_" + kernel.getLabel();		
	}

	public void setNormalize(boolean normalize) {
		kernel2.setNormalize(normalize);
	}

	public SparseVector[] computeFeatureVectors(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel2.computeFeatureVectors(graph);
	}

	public double[][] compute(RDFData data) {
		init(data.getDataset(), data.getInstances(), data.getBlackList());
		return kernel2.compute(graph);
	}

	private void init(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList) {	
		Set<Statement> stmts = RDFUtils.getStatements4Depth(dataset, instances, depth, inference);
		stmts.removeAll(blackList);
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, true);

		StringTree st = new StringTree();
		for (DTNode<String,String> node : graph.getGraph().nodes()) {
			st.store(node.label());
		}
		for (DTLink<String,String> link : graph.getGraph().links()) {
			st.store(link.tag());
		}

		

	}
}
