package org.data2semantics.mustard.experiments.modules.utils;

import java.util.List;

import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;

@Module(name="LibLINEARParms")
public class LibSVMParmsModule {
	private List<Double> cs;
	private LibSVMParameters parms;

	public LibSVMParmsModule(
			@In(name="cs") List<Double> cs 
			) {
		this.cs = cs;
	}

	@Main
	public LibSVMParameters createParms() {
		double[] csA = new double[cs.size()];
		for (int i=0;i<csA.length;i++) {
			csA[i] = cs.get(i);
		}

		parms = new LibSVMParameters(LibSVMParameters.C_SVC);
		parms.setItParams(csA);

		return parms;
	}

	@Out(name="parameters")
	public LibSVMParameters getParms() {
		return parms;
	}

}
