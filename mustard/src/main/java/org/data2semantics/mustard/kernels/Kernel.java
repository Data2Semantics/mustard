package org.data2semantics.mustard.kernels;

/**
 * Kernel interface, mainly used as a marker interface, since this does not define on what we compute a kernel.
 * 
 * The interfaces {@link GraphKernel} and {@link FeatureVectorKernel} do define computation methods.
 * 
 * There are essentially three types of data, all subclasses of {@link GraphData} on which a graph kernel can be computed: 
 * - {@link RDFData}, which is defined in terms of a triple store, a list of instances and a blacklist
 * - {@SingleDTGraph}, which defines a single graph, with a list of all the instance nodes in that graph
 * - {@link GraphList}, which defines a list of DTGraphs, these graphs are the instances. * 
 * 
 * @author Gerben
 */
public interface Kernel {
	public String getLabel();
	public void setNormalize(boolean normalize);	
}
