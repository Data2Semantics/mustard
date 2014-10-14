package org.data2semantics.mustard.experiments.modules.kernels;

import org.data2semantics.mustard.kernels.data.GraphData;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.platform.Global;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;



/**
 * AbstractKernelModule generalizes the module stuff needed for GraphKernel's
 * 
 * @author Gerben
 *
 */
@Module(name="AbstractKernel")
public abstract class AbstractKernelModule<G extends GraphData> {
	protected GraphKernel<G> kernel; 
	protected G graphData;
	protected double[][] matrix;
	protected long runtime;
	
	public AbstractKernelModule(
			@In(name="kernel") GraphKernel<G> kernel,
			@In(name="graphData") G graphData) {

		this.kernel = kernel;
		this.graphData = graphData;
	}
	
	@Main
	public double[][] compute() {
		long tic = System.currentTimeMillis();
		matrix = kernel.compute(graphData);
		long toc = System.currentTimeMillis();
		runtime = toc - tic;
		
		Global.log().info("Computed kernel: " + kernel.getLabel() + ", in: " + runtime + " msecs.");
		
		return matrix;
	}
	
	@Out(name="matrix")
	public double[][] getMatrix() {
		return matrix;
	}

	@Out(name="runtime")
	public Long getRuntime() {
		return runtime;
	}
	
	
}
