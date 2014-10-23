package org.data2semantics.mustard.experiments.modules.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

@Module(name="DTGraph")
public class DTGraphModule {
	private RDFDataSet dataset;
	private List<Resource> instances;
	private List<Statement> blackList;
	private boolean inference;
	private int graphDepth;
	private DTGraph<String,String> graph;
	private List<DTNode<String,String>> instanceNodes;
	
	public DTGraphModule(
			@In(name="dataset") RDFDataSet dataset,
			@In(name="inference") Boolean  inference) {
		this(dataset, null, new ArrayList<Statement>(), inference, 0);
	}
	
	public DTGraphModule(
			@In(name="dataset") RDFDataSet dataset, 
			@In(name="instances") List<Resource> instances,
			@In(name="blackList") List<Statement> blackList,
			@In(name="inference") Boolean inference,
			@In(name="graphDepth") Integer graphDepth) {
		this.dataset = dataset;
		this.instances = instances;
		this.blackList = blackList;
		this.inference = inference;
		this.graphDepth = graphDepth;
	}

	
	@Main
	public DTGraph<String,String> createGraph() {
		Set<Statement> stmts;
		
		if (graphDepth == 0) { // graphDepth == 0, is get the full graph
			stmts = new HashSet<Statement>(dataset.getFullGraph(inference));
		} else {
			stmts = RDFUtils.getStatements4Depth(dataset, instances, graphDepth, inference);
		}
		
		stmts.removeAll(blackList);
		
		if (instances == null) { // No instances supplied, then we do not create instanceNodes
			graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS);
		} else {
			SingleDTGraph g = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, instances, false);
			graph = g.getGraph();
			instanceNodes = g.getInstances();
		}
		return graph;
	}

	@Out(name="graph")
	public DTGraph<String, String> getGraph() {
		return graph;
	}

	@Out(name="instanceNodes")
	public List<DTNode<String, String>> getInstanceNodes() {
		return instanceNodes;
	}
}