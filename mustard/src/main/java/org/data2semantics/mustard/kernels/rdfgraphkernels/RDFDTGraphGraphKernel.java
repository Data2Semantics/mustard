package org.data2semantics.mustard.kernels.rdfgraphkernels;

import java.util.List;

import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Compute a kernel matrix on an DTGraph representation of one RDF graph (i.e. all the instances are part of the graph).
 * 
 * TODO, add a method for computation on a test set
 * 
 * @author Gerben
 *
 */
public interface RDFDTGraphGraphKernel extends Kernel {
	public double[][] compute(DTGraph<String,String> graph, List<DTNode<String,String>> instances);
}