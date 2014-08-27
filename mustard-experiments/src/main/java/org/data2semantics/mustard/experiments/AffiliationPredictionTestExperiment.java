package org.data2semantics.mustard.experiments;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.PathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.PathCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeHubRemovalKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.RDFDTGraphWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class AffiliationPredictionTestExperiment {
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

		// --- HUB removal
		int maxHubs = 100;
		List<Statement> all = dataset.getFullGraph();
		all.removeAll(blackList);
		List<DTNode<String,String>> hubs = RDFUtils.findSigDegreeHubs(new HashSet<Statement>(all), instances, maxHubs);	
		// ---



		// 0.0001,0.001,0.01,0.1,
		double[] cs = {1, 10, 100, 1000, 10000};	
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};

		int[] iterations  = {0};
		int[] pathDepths = {3};
		int[] depths = {3};
		boolean[] inferencing = {false};
		boolean[] reversal = {false};


		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);	

		/*
		svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		svmParms.setWeights(EvaluationUtils.computeWeights(target));
		//*/


		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(3);

		int[] nrHubs = {30};

		/*
		for (boolean inf : inferencing) {	
			for (int d : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inf);
				st.removeAll(blackList);
				DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
				List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, instances);
				graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
				List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, d);

				List<PathCountKernel> kernelsMG = new ArrayList<PathCountKernel>();

				for (int dd : pathDepths) {
					PathCountKernel kernel = new PathCountKernel(dd, true);			
					kernelsMG.add(kernel);
				}

				resTable.newRow(kernelsMG.get(0).getLabel() + "_" + inf);
				SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernelsMG, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

				System.out.println(kernelsMG.get(0).getLabel());
				exp2.run();

				for (Result res : exp2.getResults()) {
					resTable.addResult(res);
				}
				System.out.println(resTable);
			}

		}
		//*/
		
		///*
		for (boolean inf : inferencing) {	
			for (int d : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inf);
				st.removeAll(blackList);
				DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
				List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, instances);
				graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
				List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, d);

				List<PathCountKernelMkII> kernelsMG = new ArrayList<PathCountKernelMkII>();

				for (int dd : pathDepths) {
					PathCountKernelMkII kernel = new PathCountKernelMkII(dd, true);			
					kernelsMG.add(kernel);
				}

				resTable.newRow(kernelsMG.get(0).getLabel() + "_" + inf);
				SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernelsMG, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

				System.out.println(kernelsMG.get(0).getLabel());
				exp2.run();

				for (Result res : exp2.getResults()) {
					resTable.addResult(res);
				}
				System.out.println(resTable);
			}

		}
		//*/


		/*
		for (boolean inf : inferencing) {	
			for (boolean rev : reversal) {	
				for (int d : depths) {

					Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inf);
					st.removeAll(blackList);
					DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
					List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, instances);
					graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
					List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, d);

					List<WLSubTreeKernel> kernelsMG = new ArrayList<WLSubTreeKernel>();


					for (int it : iterations) {
						WLSubTreeKernel kernel = new WLSubTreeKernel(it, rev, true);			
						kernelsMG.add(kernel);
					}


					resTable.newRow(kernelsMG.get(0).getLabel() + "_" + inf + "_" + rev);
					SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernelsMG, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

					System.out.println(kernelsMG.get(0).getLabel());
					exp2.run();

					for (Result res : exp2.getResults()) {
						resTable.addResult(res);
					}
					System.out.println(resTable);
				}
			}
		}
		//*/

		/*
		List<RDFWLSubTreeHubRemovalKernel> kernels = new ArrayList<RDFWLSubTreeHubRemovalKernel>();
		for (boolean inf : inferencing) {
			for (boolean rev : reversal) {	
				for (int d : depths) {
					for (int it : iterations) {
						Map<String, Integer> hMap = RDFUtils.createHubMap(hubs, 0);			
						RDFWLSubTreeHubRemovalKernel kernel = new RDFWLSubTreeHubRemovalKernel(it, d, inf, hMap, rev, false, true);				
						kernels.add(kernel);
					}
				}
			}
		}
		resTable.newRow(kernels.get(0).getLabel());
		SimpleGraphKernelExperiment<RDFData> exp2 = new SimpleGraphKernelExperiment<RDFData>(kernels, new RDFData(dataset, instances, blackList), target, svmParms, seeds, evalFuncs);

		System.out.println(kernels.get(0).getLabel());
		exp2.run();

		for (Result res : exp2.getResults()) {
			resTable.addResult(res);
		}
		System.out.println(resTable);


		kernels = new ArrayList<RDFWLSubTreeHubRemovalKernel>();
		for (boolean inf : inferencing) {
			for (boolean rev : reversal) {	
				for (int d : depths) {
					for (int it : iterations) {
						for (int h : nrHubs) {
							Map<String, Integer> hMap = RDFUtils.createHubMap(hubs, h);			
							RDFWLSubTreeHubRemovalKernel kernel = new RDFWLSubTreeHubRemovalKernel(it, d, inf, hMap, rev, false, true);				
							kernels.add(kernel);
						}
					}
				}
			}
		}
		resTable.newRow(kernels.get(0).getLabel());
		exp2 = new SimpleGraphKernelExperiment<RDFData>(kernels, new RDFData(dataset, instances, blackList), target, svmParms, seeds, evalFuncs);

		System.out.println(kernels.get(0).getLabel());
		exp2.run();

		for (Result res : exp2.getResults()) {
			resTable.addResult(res);
		}
		System.out.println(resTable);

		/*
		kernels = new ArrayList<RDFWLSubTreeHubRemovalKernel>();
		for (int d : depths) {
			for (int it : iterations) {
				kernels.add(new RDFWLSubTreeHubRemovalKernel(it, d, true, 15, true, false, true));
			}
		}
		resTable.newRow(kernels.get(0).getLabel());
		exp2 = new SimpleGraphKernelExperiment<RDFData>(kernels, new RDFData(dataset, instances, blackList), target, svmParms, seeds, evalFuncs);

		System.out.println(kernels.get(0).getLabel());
		exp2.run();

		for (Result res : exp2.getResults()) {
			resTable.addResult(res);
		}
		System.out.println(resTable);
		 */



		/*
		for (boolean inf : inferencing) {
			for (boolean rev : reversal) {
				for (int d : depths) {

					List<RDFWLSubTreeKernel> kernels2 = new ArrayList<RDFWLSubTreeKernel>();
					for (int it : iterations) {
						kernels2.add(new RDFWLSubTreeKernel(it, d, inf, rev, false, true));
					}

					resTable.newRow(kernels2.get(0).getLabel() + "_" + inf + "_" + rev);
					SimpleGraphKernelExperiment<RDFData> exp2 = new SimpleGraphKernelExperiment<RDFData>(kernels2, new RDFData(dataset, instances, blackList), target, svmParms, seeds, evalFuncs);

					System.out.println(kernels2.get(0).getLabel());
					exp2.run();

					for (Result res : exp2.getResults()) {
						resTable.addResult(res);
					}
					System.out.println(resTable);
				}
			}
		}
		//*/

		/*
		for (boolean inf : inferencing) {
			for (int d : depths) {

				List<RDFIntersectionTreeEdgeVertexPathKernel> kernels2 = new ArrayList<RDFIntersectionTreeEdgeVertexPathKernel>();
				kernels2.add(new RDFIntersectionTreeEdgeVertexPathKernel(d, inf, true));

				resTable.newRow(kernels2.get(0).getLabel());
				SimpleGraphKernelExperiment<RDFData> exp2 = new SimpleGraphKernelExperiment<RDFData>(kernels2, new RDFData(dataset, instances, blackList), target, svmParms, seeds, evalFuncs);

				System.out.println(kernels2.get(0).getLabel());
				exp2.run();

				for (Result res : exp2.getResults()) {
					resTable.addResult(res);
				}
				System.out.println(resTable);
			}
		}
		//*/




		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

	}

}
