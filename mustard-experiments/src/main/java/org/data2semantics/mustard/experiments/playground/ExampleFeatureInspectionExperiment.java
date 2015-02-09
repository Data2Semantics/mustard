package org.data2semantics.mustard.experiments.playground;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.experiments.FeatureInspectionExperiment;
import org.data2semantics.mustard.experiments.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class ExampleFeatureInspectionExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";
	

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		ClassificationDataSet ds = new AIFBDataSet(tripleStore);
		ds.create();

		double[] cs = {1,10,100,1000};	

		LibLINEARParameters svmParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		svmParms.setNumFolds(5);

		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean[] inference = {false};

		int[] depths = {2};
		
		int maxFeatures = 10;
		

		RDFData data = ds.getRDFData();
		List<Double> target = ds.getTarget();
		
		
		for (boolean inf : inference) {
			for (int d : depths) {
				List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	
				kernels.add(new RDFWLSubTreeKernel(d*2, d, inf, reverseWL, false, true, true));

				FeatureInspectionExperiment<RDFData,RDFWLSubTreeKernel> exp = new FeatureInspectionExperiment<RDFData,RDFWLSubTreeKernel>(kernels, data, target, svmParms, maxFeatures);
				exp.run();
			}
		}
	}
}
