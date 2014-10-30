package org.data2semantics.mustard.experiments.cluster;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.SimpleGraphFeatureVectorKernelExperiment;
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class ClusterExperiment {

	public static void main(String[] args) {
		StringArgumentsParser parser = new StringArgumentsParser(args);

		// Build them dataset
		ClassificationDataSet ds = parser.createDataSet();
		Set<Statement> stmts = RDFUtils.getStatements4Depth(ds.getRDFData().getDataset(), ds.getRDFData().getInstances(), parser.getDepth(), parser.isInference());
		stmts.removeAll(new HashSet<Statement>(ds.getRDFData().getBlackList()));
		SingleDTGraph graph = null;
		graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, ds.getRDFData().getInstances(), true);
		if (parser.getNumHubs() > 0) {
			List<DTNode<String,String>> hubs = RDFUtils.findSigDegreeHubs(stmts, ds.getRDFData().getInstances(), parser.getNumHubs());
			Map<String, Integer> hubMap = RDFUtils.createHubMap(hubs, parser.getNumHubs());
			graph = RDFUtils.removeHubs(graph, hubMap);
		}

		// Eval funcs
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());
		double[] cs = {1,10,100,1000};

		List<Result> results = null;

		if (parser.getSubset() == 0) { // if we are not dealing with subsets, we do LibSVM
			//Setup the SVM	
			LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
			svmParms.setNumFolds(10);
			long[] seeds = {11,21,31,41,51,61,71,81,91,101};


			List<? extends GraphKernel<SingleDTGraph>> kernels = parser.graphKernel();
			SimpleGraphKernelExperiment<SingleDTGraph> exp = new SimpleGraphKernelExperiment<SingleDTGraph>(kernels, graph, ds.getTarget(), svmParms, seeds, evalFuncs);

			System.out.println("Running: " + parser.getSaveFileString());
			exp.run();
			results = exp.getResults();

		} else { // LibLINEAR
			//Setup the SVM	
			LibLINEARParameters svmParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
			svmParms.setNumFolds(5);
			long[] seeds = {11};

			List<? extends FeatureVectorKernel<SingleDTGraph>> kernels = parser.graphFeatureVectorKernel();
			SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernels, graph, ds.getTarget(), svmParms, seeds, evalFuncs);

			System.out.println("Running: " + parser.getSaveFileString());
			exp.run();
			results = exp.getResults();
		}


		try {
			FileWriter fw = new FileWriter(parser.getSaveFileString()+ "_" + parser.getSubset() + ".result");

			fw.write(parser.getSaveFileString() + ":" + parser.getSubset());
			fw.write("\n");
			System.out.println(parser.getSaveFileString() + ":" + parser.getSubset());
			for (Result res : results) {
				System.out.println(res);
				fw.write(res.toString());
				fw.write("\n");
			}
			fw.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
}
