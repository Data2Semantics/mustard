package org.data2semantics.mustard.experiments.modules.kernels;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;

@Module(name="RDFWLSubTreeKernel")
public class RDFWLSubTreeKernelModule extends AbstractKernelModule<RDFData> {
	
	public RDFWLSubTreeKernelModule(
			@In(name="iterations") Integer iterations, 
			@In(name="depth") Integer depth,
			@In(name="inference") Boolean inference, 
			@In(name="reverse") Boolean reverse,
			@In(name="iterationWeighting") Boolean iterationWeighting,
			@In(name="normalize") Boolean normalize,
			@In(name="graphData") RDFData graphData) {
		
			super(new RDFWLSubTreeKernel(iterations, depth, inference, reverse, iterationWeighting, normalize), graphData);
	}
	
	@Main
	public double[][] compute() {
		return super.compute();
	}
	
	@Out(name="matrix")
	public double[][] getMatrix() {
		return super.getMatrix();
	}

	@Out(name="runtime")
	public Long getRuntime() {
		return super.getRuntime();
	}
	
	
}
