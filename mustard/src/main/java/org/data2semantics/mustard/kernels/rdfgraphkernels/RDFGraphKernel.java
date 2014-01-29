package org.data2semantics.mustard.kernels.rdfgraphkernels;

import java.util.List;

import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Compute a kernel matrix on an RDFDataSet, instead of a list of graphs as in {@link org.data2semantics.proppred.kernels.GraphKernel}.
 * 
 * TODO, add a method for computation on a test set
 * 
 * @author Gerben
 *
 */
public interface RDFGraphKernel extends Kernel {
	public double[][] compute(RDFDataSet dataset, List<Resource> instances, List<Statement> blackList);
}