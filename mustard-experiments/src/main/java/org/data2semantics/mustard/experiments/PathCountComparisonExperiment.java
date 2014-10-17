package org.data2semantics.mustard.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.data2semantics.mustard.experiments.rescal.RESCALKernel;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.CombinedKernel;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.PathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.PathCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.TreePathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFPathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootPathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreePathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class PathCountComparisonExperiment {
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



		boolean inference = false;

		int[] depths = {1,2,3};
		int[] pathDepths = {0,1,2,3,4,5,6};


		/*
		for (int d : depths) {

			Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inference);
			st.removeAll(blackList);
			DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
			List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, instances);
			graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
			List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, d);
			
			double avgNodes = 0;
			double avgLinks = 0;
			
			for (DTGraph<String,String> g : graphs){
				avgNodes += g.nodes().size();
				avgLinks += g.links().size();
			}
			avgNodes /= graphs.size();
			avgLinks /= graphs.size();
			
			System.out.println("Avg # nodes: " + avgNodes + " , avg # links: " + avgLinks);

			List<PathCountKernel> kernelsMG = new ArrayList<PathCountKernel>();

			for (int dd : pathDepths) {
				PathCountKernel kernel = new PathCountKernel(dd, true);			
				kernelsMG.add(kernel);
			}

			resTable.newRow(kernelsMG.get(0).getLabel() + "_" + inference);
			SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernelsMG, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

			System.out.println(kernelsMG.get(0).getLabel());
			exp2.run();

			for (Result res : exp2.getResults()) {
				resTable.addResult(res);
			}
			System.out.println(resTable);
		}
		//*/

		/*
		for (int d : depths) {

			Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inference);
			st.removeAll(blackList);
			//DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
			List<DTNode<String,String>> instanceNodes = new ArrayList<DTNode<String,String>>();
			DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS, instances, instanceNodes, true);
			//List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, instances);
			//graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
			List<DTGraph<String,String>> graphs = RDFUtils.getSubTrees(graph, instanceNodes, d);

			double avgNodes = 0;
			double avgLinks = 0;
			
			for (DTGraph<String,String> g : graphs){
				avgNodes += g.nodes().size();
				avgLinks += g.links().size();
			}
			avgNodes /= graphs.size();
			avgLinks /= graphs.size();
			
			System.out.println("Avg # nodes: " + avgNodes + " , avg # links: " + avgLinks);

			
			List<TreePathCountKernel> kernelsMG = new ArrayList<TreePathCountKernel>();

			for (int dd : pathDepths) {
				TreePathCountKernel kernel = new TreePathCountKernel(dd, true);			
				kernelsMG.add(kernel);
			}

			/*
			resTable.newRow(kernelsMG.get(0).getLabel() + "_" + inference);
			SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernelsMG, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

			System.out.println(kernelsMG.get(0).getLabel());
			exp2.run();

			for (Result res : exp2.getResults()) {
				resTable.addResult(res);
			}
			System.out.println(resTable);
			*/
		//}
		//*/


		RDFData data = new RDFData(dataset, instances, blackList);

		boolean WLreverse = false;
		


		/*
		List<RDFWLSubTreeKernel> kernelsWL = new ArrayList<RDFWLSubTreeKernel>();	

		kernelsWL.add(new RDFWLSubTreeKernel(0, 3, inference, false, true, true));
		//kernelsWL.add(new RDFWLSubTreeKernel(1, 3, inference, false, true, true));
		//kernelsWL.add(new RDFWLSubTreeKernel(2, 3, inference, false, true, true));
		//kernelsWL.add(new RDFWLSubTreeKernel(3, 3, inference, false, true, true));
		//kernelsWL.add(new RDFWLSubTreeKernel(4, 3, inference, false, true, true));
		//kernelsWL.add(new RDFWLSubTreeKernel(5, 3, inference, false, true, true));
		//kernelsWL.add(new RDFWLSubTreeKernel(6, 3, inference, false, true, true));

		//Collections.shuffle(target);
		SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsWL, data, target, svmParms, seeds, evalFuncs);

		resTable.newRow("WL");
		exp.run();

		for (Result res : exp.getResults()) {
			resTable.addResult(res);
		}
		//*/

		///*
		for (int d : depths) {
			List<RDFRootWLSubTreeKernel> kernelsWL = new ArrayList<RDFRootWLSubTreeKernel>();	

			for (int dd : pathDepths) {
				kernelsWL.add(new RDFRootWLSubTreeKernel(dd, d, inference, WLreverse, false, true));
			}
	
			//Collections.shuffle(target);
			SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsWL, data, target, svmParms, seeds, evalFuncs);

			resTable.newRow(kernelsWL.get(0).getLabel());
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		//*/
		
		///*
		for (int d : depths) {
			List<RDFTreeWLSubTreeKernel> kernelsWL = new ArrayList<RDFTreeWLSubTreeKernel>();	

			for (int dd : pathDepths) {
				kernelsWL.add(new RDFTreeWLSubTreeKernel(dd, d, inference, WLreverse, false, true));
			}
	
			//Collections.shuffle(target);
			SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsWL, data, target, svmParms, seeds, evalFuncs);

			resTable.newRow(kernelsWL.get(0).getLabel());
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		//*/
		
		
		///*
		for (int d : depths) {
			List<RDFWLSubTreeKernel> kernelsWL = new ArrayList<RDFWLSubTreeKernel>();	

			for (int dd : pathDepths) {
				kernelsWL.add(new RDFWLSubTreeKernel(dd, d, inference, WLreverse, false, true));
			}
	
			//Collections.shuffle(target);
			SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsWL, data, target, svmParms, seeds, evalFuncs);

			resTable.newRow(kernelsWL.get(0).getLabel());
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		//*/
		



		/*
		for (int d : depths) {
			List<RDFPathCountKernel> kernelsIT = new ArrayList<RDFPathCountKernel>();	

			for (int dd : pathDepths) {
				kernelsIT.add(new RDFPathCountKernel(dd, d, inference, true));
			}


			//Collections.shuffle(target);
			SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsIT, data, target, svmParms, seeds, evalFuncs);

			resTable.newRow(kernelsIT.get(0).getLabel());
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		//*/

		/*
		for (int d : depths) {
			List<RDFTreePathCountKernel> kernelsIT = new ArrayList<RDFTreePathCountKernel>();	


			//for (int dd : pathDepths) {
				kernelsIT.add(new RDFTreePathCountKernel(d*2, d, inference, true));
			//}



			//Collections.shuffle(target);
			SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsIT, data, target, svmParms, seeds, evalFuncs);

			resTable.newRow(kernelsIT.get(0).getLabel());
			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		//*/

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
