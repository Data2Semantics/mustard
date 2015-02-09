package org.data2semantics.mustard.experiments.playground;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.experiments.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.evaluation.utils.EvaluationUtils;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class PathvsTreeExperiment {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RDFDataSet dataset = new RDFFileDataSet(dataFile, RDFFormat.N3);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();

		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}

		EvaluationUtils.removeSmallClasses(instances, labels, 5);
		List<Statement> blackList = DataSetUtils.createBlacklist(dataset, instances, labels);

		List<Double> target = EvaluationUtils.createTarget(labels);

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(3);

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};

		double[] cs = {1, 10, 100, 1000, 10000};	

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);
		
		/*
		svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		svmParms.setWeights(EvaluationUtils.computeWeights(target));
		//*/
		

		RDFData data = new RDFData(dataset, instances, blackList);

		boolean inference = false;
		
		
		
		///*
		List<RDFWLSubTreeKernel> kernelsWL = new ArrayList<RDFWLSubTreeKernel>();	
				 
		kernelsWL.add(new RDFWLSubTreeKernel(0, 3, inference, false, true, true));
		kernelsWL.add(new RDFWLSubTreeKernel(1, 3, inference, false, true, true));
		kernelsWL.add(new RDFWLSubTreeKernel(2, 3, inference, false, true, true));
		kernelsWL.add(new RDFWLSubTreeKernel(3, 3, inference, false, true, true));
		kernelsWL.add(new RDFWLSubTreeKernel(4, 3, inference, false, true, true));
		kernelsWL.add(new RDFWLSubTreeKernel(5, 3, inference, false, true, true));
		kernelsWL.add(new RDFWLSubTreeKernel(6, 3, inference, false, true, true));

		//Collections.shuffle(target);
		SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsWL, data, target, svmParms, seeds, evalFuncs);

		resTable.newRow("WL");
		exp.run();

		for (Result res : exp.getResults()) {
			resTable.addResult(res);
		}
		//*/

		List<RDFWalkCountKernel> kernelsIT = new ArrayList<RDFWalkCountKernel>();	
		
		kernelsIT.add(new RDFWalkCountKernel(0, 3, inference, true));
		kernelsIT.add(new RDFWalkCountKernel(1, 3, inference, true));
		kernelsIT.add(new RDFWalkCountKernel(2, 3, inference, true));
		kernelsIT.add(new RDFWalkCountKernel(3, 3, inference, true));
		kernelsIT.add(new RDFWalkCountKernel(4, 3, inference, true));		
		kernelsIT.add(new RDFWalkCountKernel(5, 3, inference, true));
		kernelsIT.add(new RDFWalkCountKernel(6, 3, inference, true));
		
		
		//Collections.shuffle(target);
		exp = new SimpleGraphKernelExperiment<RDFData>(kernelsIT, data, target, svmParms, seeds, evalFuncs);

		resTable.newRow("IT");
		exp.run();

		for (Result res : exp.getResults()) {
			resTable.addResult(res);
		}

		/*
		List<GraphKernel<RDFData>> kernelsComb = new ArrayList<GraphKernel<RDFData>>();	
		List<GraphKernel<RDFData>> kernelComb = new ArrayList<GraphKernel<RDFData>>();	
		
		kernelsComb.add(new RDFPathCountKernel(6, 3, inference, true));
		kernelsComb.add(new RDFWLSubTreeKernel(6, 3, inference, true, false, true));
		
		kernelComb.add(new CombinedKernel<RDFData>(kernelsComb, true));
		
		//Collections.shuffle(target);
		exp = new SimpleGraphKernelExperiment<RDFData>(kernelComb, data, target, svmParms, seeds, evalFuncs);

		resTable.newRow("Comb");
		exp.run();

		for (Result res : exp.getResults()) {
			resTable.addResult(res);
		}
		//*/
		

		System.out.println(resTable);


	}

}
