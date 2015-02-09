package org.data2semantics.mustard.experiments.IFIPTM;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.experiments.FeatureInspectionExperiment;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.openrdf.rio.RDFFormat;

public class SteveFeatureInspectionExperiment {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// load all the RDF data
		RDFDataSet tripleStore = new RDFFileDataSet(SteveExperiment.STEVE_FOLDER, RDFFormat.RDFXML);
		LargeClassificationDataSet ds = new SteveDataSet(tripleStore, 10);
		ds.create();

		double[] cs = { 1, 10, 100, 1000 };

		LibLINEARParameters svmParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		svmParms.setNumFolds(5);

		boolean reverseWL = true; // WL should be in reverse mode, which means
									// regular subtrees
		boolean[] inference = { false };

		int[] depths = { 3 };

		int maxFeatures = 10;

		RDFData data = ds.getRDFData();
		List<Double> target = ds.getTarget();

		for (boolean inf : inference) {
			for (int d : depths) {
				List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();
				kernels.add(new RDFWLSubTreeKernel(d * 2, d, inf, reverseWL, false, true, true));

				FeatureInspectionExperiment<RDFData, RDFWLSubTreeKernel> exp = new FeatureInspectionExperiment<RDFData, RDFWLSubTreeKernel>(
						kernels, data, target, svmParms, maxFeatures);
				exp.run();
			}
		}
	}
}
