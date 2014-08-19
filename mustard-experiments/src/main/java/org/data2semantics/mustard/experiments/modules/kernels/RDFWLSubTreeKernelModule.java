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
			@In(name="iterations") int iterations, 
			@In(name="depth") int depth,
			@In(name="inference") boolean inference, 
			@In(name="reverse") boolean reverse,
			@In(name="iterationWeighting") boolean iterationWeighting,
			@In(name="normalize") boolean normalize,
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
	public long getRuntime() {
		return super.getRuntime();
	}
	
	
}
