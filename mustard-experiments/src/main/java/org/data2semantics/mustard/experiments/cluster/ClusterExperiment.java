package org.data2semantics.mustard.experiments.cluster;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreePathCountKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
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
		RDFDataSet tripleStore = new RDFFileDataSet(parser.getDataFile(), RDFFormat.forFileName(parser.getDataFile()));
		ClassificationDataSet ds = new AIFBDataSet(tripleStore);
		ds.create();

		//Setup the SVM	
		double[] cs = {1,10,100,1000};
		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		// Eval funcs
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};

		// Create SingleDTGraph
		Set<Statement> stmts = RDFUtils.getStatements4Depth(ds.getRDFData().getDataset(), ds.getRDFData().getInstances(), parser.getDepth(), parser.isInference());
		stmts.removeAll(new HashSet<Statement>(ds.getRDFData().getBlackList()));
		List<DTNode<String,String>> instanceNodes = new ArrayList<DTNode<String,String>>();
		DTGraph<String,String> graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, ds.getRDFData().getInstances(), instanceNodes, true);

		// get the kernel
		List<GraphKernel<SingleDTGraph>> kernels = new ArrayList<GraphKernel<SingleDTGraph>>();	
		kernels.add(parser.graphKernel());

		SimpleGraphKernelExperiment<SingleDTGraph> exp = new SimpleGraphKernelExperiment<SingleDTGraph>(kernels, new SingleDTGraph(graph, instanceNodes), ds.getTarget(), svmParms, seeds, evalFuncs);

		System.out.println("Running: " + kernels.get(0).getLabel());
		exp.run();

		try {
			FileWriter fw = new FileWriter(kernels.get(0).getLabel() + "_" + parser.getDepth() + "_" + parser.isInference() + ".result");

			fw.write(kernels.get(0).getLabel() + "_" + parser.getDepth() + "_" + parser.isInference() + ":" + 1);
			fw.write("\n");
			System.out.println(kernels.get(0).getLabel() + "_" + parser.getDepth() + "_" + parser.isInference() + ":" + 1);
			for (Result res : exp.getResults()) {
				System.out.println(res);
				fw.write(res.toString());
				fw.write("\n");
			}
			fw.close();
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}
}
