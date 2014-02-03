package org.data2semantics.mustard.kernels.rdfgraphkernels;

import java.util.List;

import org.data2semantics.mustard.kernels.Kernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

/**
 * Compute SparseVector feature vectors, instead of a kernel matrix on an RDFDataSet.
 * 
 * TODO, add a method for computation on a test set
 * 
 * @author Gerben
 *
 */
public interface RDFDTGraphFeatureVectorKernel extends Kernel {
	public SparseVector[] computeFeatureVectors(DTGraph<String,String> graph, List<DTNode<String,String>> instances);	
}
