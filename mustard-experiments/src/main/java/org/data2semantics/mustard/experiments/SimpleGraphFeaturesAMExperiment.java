package org.data2semantics.mustard.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.mustard.experiments.rescal.RESCALKernel;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.experiments.utils.SimpleGraphFeatureVectorKernelExperiment;
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
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLRootSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;
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

public class SimpleGraphFeaturesAMExperiment {
	private static String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";

	private static List<Resource> instances;
	private static List<Value> labels;
	private static List<Statement> blackList;
	private static List<Double> target;
	private static RDFDataSet dataset;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		dataset = new RDFFileDataSet(AM_FOLDER, RDFFormat.TURTLE);

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(3);

		long[] seeds = {11};
		long[] seedsDataset = {11,21,31,41,51}; //,61,71,81,91,101};
		double[] cs = {1, 10, 100, 1000};	

		LibLINEARParameters svmParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		svmParms.setDoCrossValidation(false);
		svmParms.setNumFolds(3);

		/*
		svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		svmParms.setWeights(EvaluationUtils.computeWeights(target));
		//*/


		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean[] inference = {false,true};

		int subsetSize = 400;

		int[] depths = {1,2};
		int[] pathDepths = {2,4,6};
		int[] iterationsWL = {2,4,6};

		boolean depthTimesTwo = true;


		///* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL: " + inf);
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					long tic = System.currentTimeMillis();
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);
					long toc = System.currentTimeMillis();
					
					System.out.println("Dataset time: " + (toc - tic));

					List<RDFWLSubTreeKernel> kernelsBaseline = new ArrayList<RDFWLSubTreeKernel>();	
					kernelsBaseline.add(new RDFWLSubTreeKernel(0, d, inf, reverseWL, false, true));

					//Collections.shuffle(target);
					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/

		//*/
		for (boolean inf : inference) {
			resTable.newRow("Path Count through root: " + inf);	
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);

					List<RDFRootPathCountKernel> kernels = new ArrayList<RDFRootPathCountKernel>();	

					if (depthTimesTwo) {
						kernels.add(new RDFRootPathCountKernel(d*2, d, true, inf, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new RDFRootPathCountKernel(dd, d, true, inf, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/
		
		//*/
		for (boolean inf : inference) {
			resTable.newRow("WL through root: " + inf);
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);

					List<RDFWLRootSubTreeKernel> kernels = new ArrayList<RDFWLRootSubTreeKernel>();	

					if (depthTimesTwo) {
						kernels.add(new RDFWLRootSubTreeKernel(d*2, d, inf, reverseWL, false, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new RDFWLRootSubTreeKernel(dd, d, inf, reverseWL, false, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/

		//*/
		for (boolean inf : inference) {
			resTable.newRow("Path Count Tree: " + inf);	
			
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);

					List<RDFTreePathCountKernel> kernels = new ArrayList<RDFTreePathCountKernel>();		

					if (depthTimesTwo) {
						kernels.add(new RDFTreePathCountKernel(d*2, d, inf, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new RDFTreePathCountKernel(dd, d, inf, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/
		
		//*/
		for (boolean inf : inference) {
			resTable.newRow("WL Tree: " + inf);	
			
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);

					List<RDFTreeWLSubTreeKernel> kernels = new ArrayList<RDFTreeWLSubTreeKernel>();	

					if (depthTimesTwo) {
						kernels.add(new RDFTreeWLSubTreeKernel(d*2, d, inf, reverseWL, false, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new RDFTreeWLSubTreeKernel(dd, d, inf, reverseWL, false, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/
		

		///* RDF Path Count 
		for (boolean inf : inference) {
			resTable.newRow("RDF Path Count: " + inf);
			
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);

					List<RDFPathCountKernel> kernels = new ArrayList<RDFPathCountKernel>();	

					if (depthTimesTwo) {
						kernels.add(new RDFPathCountKernel(d*2, d, inf, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new RDFPathCountKernel(dd, d, inf, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/


		///* RDF WL
		for (boolean inf : inference) {
			resTable.newRow("RDF WL: " + inf);
			
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					createAMDataSet(dataset, sDS, subsetSize, 10);
					RDFData data = new RDFData(dataset, instances, blackList);

					List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	

					if (depthTimesTwo) {
						kernels.add(new RDFWLSubTreeKernel(d*2, d, inf, reverseWL, false, true));
					} else {
						for (int dd : iterationsWL) {
							kernels.add(new RDFWLSubTreeKernel(dd, d, inf, reverseWL, false, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<RDFData> exp = new SimpleGraphFeatureVectorKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);
					exp.run();

					if (tempRes.isEmpty()) {
						for (Result res : exp.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
			}
		}
		
		System.out.println(resTable);

		//*/




		/* Regular WL
		for (boolean inf : inference) {
			resTable.newRow("Regular WL: " + inf);		
			for (int d : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inf);
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

				List<WLSubTreeKernel> kernels = new ArrayList<WLSubTreeKernel>();

				for (int dd : iterationsWL) {
					WLSubTreeKernel kernel = new WLSubTreeKernel(dd, reverseWL, true);			
					kernels.add(kernel);
				}

				//resTable.newRow(kernels.get(0).getLabel() + "_" + inf);
				SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

				//System.out.println(kernels.get(0).getLabel());
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

		/* Path Count full
		for (boolean inf : inference) {
			resTable.newRow("Path Count Full: " + inf);		
			for (int d : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(dataset, instances, d, inf);
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

				List<PathCountKernel> kernels = new ArrayList<PathCountKernel>();

				for (int dd : pathDepths) {
					PathCountKernel kernel = new PathCountKernel(dd, true);			
					kernels.add(kernel);
				}

				//resTable.newRow(kernels.get(0).getLabel() + "_" + inf);
				SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

				//System.out.println(kernels.get(0).getLabel());
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


	private static void createAMDataSet(RDFDataSet dataset, long seed, int subsetSize, int minSize) {

		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://purl.org/collections/nl/am/objectCategory", null);
		System.out.println(dataset.getLabel());

		System.out.println("objects in AM: " + stmts.size());


		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}

		//		
		//		
		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);
		//System.out.println(EvaluationUtils.computeClassCounts(target));

		Collections.shuffle(instances, new Random(seed));
		Collections.shuffle(labels, new Random(seed));

		instances = instances.subList(0, subsetSize);
		labels = labels.subList(0, subsetSize);

		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
		target = EvaluationUtils.createTarget(labels);

		System.out.println("Subset: ");
		System.out.println(EvaluationUtils.computeClassCounts(target));

	}
}
