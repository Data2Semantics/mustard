package org.data2semantics.mustard.kernels;

/**
 * <p>
 * Kernel interface, mainly used as a marker interface, since this does not define on what type of data a kernel is computed.
 * </p>
 * <p>
 * The interfaces {@link org.data2semantics.mustard.kernels.graphkernels.GraphKernel} and {@link org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel} do define computation methods.
 * </p>
 * 
 * <p>
 * There are essentially three types of data, all subclasses of {@link org.data2semantics.mustard.kernels.data.GraphData}, on which a graph kernel can be computed: 
 * <ul>
 * <li>{@link org.data2semantics.mustard.kernels.data.RDFData}, which is defined in terms of a triple store, a list of instances and a blacklist. </li>
 * <li>{@link org.data2semantics.mustard.kernels.data.SingleDTGraph}, which defines a single graph, with a list of all the instance nodes in that graph. </li>
 * <li>{@link org.data2semantics.mustard.kernels.data.GraphList}, which defines a list of DTGraphs, these graphs are the instances.</li>
 * </ul>
 * </p> 
 * 
 * @author Gerben
 */
public interface Kernel {
	
	/**
	 * Get a string label for this kernel. Eg. to use it in tables, etc.
	 * 
	 * @return label
	 */
	public String getLabel();
	
	/**
	 * Used to set whether the computed kernel should be normalized (between 0 and 1) or not.
	 * 
	 * @param normalize
	 */
	public void setNormalize(boolean normalize);	
}
