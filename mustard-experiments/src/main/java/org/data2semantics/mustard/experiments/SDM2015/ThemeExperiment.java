package org.data2semantics.mustard.experiments.SDM2015;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AMDataSet;
import org.data2semantics.mustard.experiments.data.BGSDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.experiments.rescal.RESCALKernel;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.experiments.utils.SimpleGraphFeatureVectorKernelExperiment;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.CombinedKernel;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.PathCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.TreePathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFIntersectionTreeEdgeVertexPathKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFSingleDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.util.Pair;
import org.data2semantics.mustard.weisfeilerlehman.StringLabel;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class ThemeExperiment {
	private static final String BGS_FOLDER = "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";

	private static List<Double> target;
	private static RDFDataSet tripleStore;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		tripleStore = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
		LargeClassificationDataSet ds = new BGSDataSet(tripleStore, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme", 10, 0.05, 5, 3);

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);
		resTable.setSignificanceTest(ResultsTable.SigTest.PAIRED_TTEST);
		resTable.setpValue(0.05);
		resTable.setShowStdDev(true);

		long[] seeds = {11};
		long[] seedsDataset = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {1, 10, 100, 1000};	

		LibLINEARParameters svmParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		svmParms.setNumFolds(5);


		double fraction = 0.05;
		int minClassSize = 0;
		int maxNumClasses = 3;


		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean[] inference = {false,true};

		int[] depths = {1,2,3};
		int[] pathDepths = {2,4,6};
		int[] iterationsWL = {2,4,6};

		boolean depthTimesTwo = true;



		Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> cache = createDataSetCache(ds, seedsDataset, fraction, minClassSize, maxNumClasses, depths, inference);
		tripleStore = null;

		computeGraphStatistics(cache, seedsDataset, inference, depths);

		///* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL: " + inf);
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();


					List<DTGraphWLSubTreeKernel> kernelsBaseline = new ArrayList<DTGraphWLSubTreeKernel>();	
					kernelsBaseline.add(new DTGraphWLSubTreeKernel(0, d, reverseWL, false, true));

					//Collections.shuffle(target);
					SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);
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
		
		///* The baseline experiment, BoW (or BoL if you prefer) Tree Variant
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL Tree: " + inf);
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();


					List<DTGraphTreeWLSubTreeKernel> kernelsBaseline = new ArrayList<DTGraphTreeWLSubTreeKernel>();	
					kernelsBaseline.add(new DTGraphTreeWLSubTreeKernel(0, d, reverseWL, false, true));

					//Collections.shuffle(target);
					SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);
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

		///*
		for (boolean inf : inference) {
			resTable.newRow("Path Count through root: " + inf);	
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();

					List<DTGraphRootWalkCountKernel> kernels = new ArrayList<DTGraphRootWalkCountKernel>();	

					if (depthTimesTwo) {
						kernels.add(new DTGraphRootWalkCountKernel(d*2, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new DTGraphRootWalkCountKernel(dd, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernels, data, target, svmParms, seeds, evalFuncs);
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

		///*
		for (boolean inf : inference) {
			resTable.newRow("WL through root: " + inf);
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();

					List<DTGraphRootWLSubTreeKernel> kernels = new ArrayList<DTGraphRootWLSubTreeKernel>();	

					if (depthTimesTwo) {
						kernels.add(new DTGraphRootWLSubTreeKernel(d*2, false, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new DTGraphRootWLSubTreeKernel(dd, false, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernels, data, target, svmParms, seeds, evalFuncs);
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

		///*
		for (boolean inf : inference) {
			resTable.newRow("Path Count Tree: " + inf);	

			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();

					List<DTGraphTreeWalkCountKernel> kernels = new ArrayList<DTGraphTreeWalkCountKernel>();		

					if (depthTimesTwo) {
						kernels.add(new DTGraphTreeWalkCountKernel(d*2, d, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new DTGraphTreeWalkCountKernel(dd, d, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernels, data, target, svmParms, seeds, evalFuncs);
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

		///*
		for (boolean inf : inference) {
			resTable.newRow("WL Tree: " + inf);	

			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();

					List<DTGraphTreeWLSubTreeKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeKernel>();	

					if (depthTimesTwo) {
						kernels.add(new DTGraphTreeWLSubTreeKernel(d*2, d, reverseWL, false, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new DTGraphTreeWLSubTreeKernel(dd, d, reverseWL, false, true));
						}
					}

					SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(kernels, data, target, svmParms, seeds, evalFuncs);
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


		///* Regular WL
		for (boolean inf : inference) {
			resTable.newRow("Regular WL: " + inf);		
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();

					List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(data.getGraph(), data.getInstances(), d);

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

					if (depthTimesTwo) {
						kernels.add(new WLSubTreeKernel(d*2, reverseWL, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new WLSubTreeKernel(dd, reverseWL, true));
						}
					}	
				
					//resTable.newRow(kernels.get(0).getLabel() + "_" + inf);
					SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

					//System.out.println(kernels.get(0).getLabel());
					exp2.run();
					
					if (tempRes.isEmpty()) {
						for (Result res : exp2.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp2.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
				System.out.println(resTable);
			}
		}
		//*/


		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

		
		///* Path Count full
		for (boolean inf : inference) {
			resTable.newRow("Path Count Full: " + inf);		
			for (int d : depths) {
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) {
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					SingleDTGraph data = p.getFirst();
					target = p.getSecond();

					List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(data.getGraph(), data.getInstances(), d);

					double avgNodes = 0;
					double avgLinks = 0;

					for (DTGraph<String,String> g : graphs){
						avgNodes += g.nodes().size();
						avgLinks += g.links().size();
					}
					avgNodes /= graphs.size();
					avgLinks /= graphs.size();

					System.out.println("Avg # nodes: " + avgNodes + " , avg # links: " + avgLinks);

					List<WalkCountKernel> kernels = new ArrayList<WalkCountKernel>();

					if (depthTimesTwo) {
						kernels.add(new WalkCountKernel(d*2, true));
					} else {
						for (int dd : pathDepths) {
							kernels.add(new WalkCountKernel(dd, true));
						}
					}
		
					//resTable.newRow(kernels.get(0).getLabel() + "_" + inf);
					SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphFeatureVectorKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

					//System.out.println(kernels.get(0).getLabel());
					exp2.run();
					
					if (tempRes.isEmpty()) {
						for (Result res : exp2.getResults()) {
							tempRes.add(res);
						}
					} else {
						for (int i = 0; i < tempRes.size(); i++) {
							tempRes.get(i).addResult(exp2.getResults().get(i));
						}
					}
				}
				for (Result res : tempRes) {
					resTable.addResult(res);
				}
				System.out.println(resTable);
			}
		}
		//*/



		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);


	}

	private static void computeGraphStatistics(Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> cache, long[] seeds, boolean[] inference, int[] depths) {
		Map<Boolean, Map<Integer, Pair<Double, Double>>> stats = new HashMap<Boolean, Map<Integer, Pair<Double, Double>>>();
		
		for (long seed : seeds) {
			for (boolean inf : inference) {
				if (!stats.containsKey(inf)) {
					stats.put(inf, new HashMap<Integer, Pair<Double, Double>>());
				}
				for (int depth : depths) {					
					if (!stats.get(inf).containsKey(depth)) {
						stats.get(inf).put(depth, new Pair<Double,Double>(0.0,0.0));
					}
					
					Pair<SingleDTGraph, List<Double>> p = cache.get(seed).get(inf).get(depth);
					List<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(p.getFirst().getGraph(), p.getFirst().getInstances(), depth);
					
					double v = 0;
					double e = 0;
					for (DTGraph<String,String> graph : graphs) {
						v += graph.nodes().size();
						e += graph.links().size();
					}
					v /= graphs.size();
					e /= graphs.size();
					
					v += stats.get(inf).get(depth).getFirst();
					e += stats.get(inf).get(depth).getSecond();
					
					stats.get(inf).put(depth, new Pair<Double,Double>(v,e));
				}
			}
		}
		
		for (boolean k1 : stats.keySet()) {
			System.out.println("Inference: " + k1);
			for (int k2 : stats.get(k1).keySet()) {
				System.out.println("Depth " + k2 + ", vertices: " + (stats.get(k1).get(k2).getFirst()/seeds.length) + " , edges: " + (stats.get(k1).get(k2).getSecond()/seeds.length));
			}
		}
	}
	
	
	private static Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> createDataSetCache(LargeClassificationDataSet data, long[] seeds, double fraction, int minSize, int maxClasses, int[] depths, boolean[] inference) {
		Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> cache = new HashMap<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>>();

		for (long seed : seeds) {
			cache.put(seed, new HashMap<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>());
			data.createSubSet(seed, fraction, minSize, maxClasses);

			for (boolean inf : inference) {
				cache.get(seed).put(inf, new HashMap<Integer,Pair<SingleDTGraph, List<Double>>>());

				for (int depth : depths) {
					System.out.println("Getting Statements...");
					Set<Statement> stmts = RDFUtils.getStatements4Depth(tripleStore, data.getRDFData().getInstances(), depth, inf);
					System.out.println("# Statements: " + stmts.size());
					stmts.removeAll(new HashSet<Statement>(data.getRDFData().getBlackList()));
					System.out.println("# Statements: " + stmts.size() + ", after blackList");
					System.out.println("Building Graph...");
					List<DTNode<String,String>> instanceNodes = new ArrayList<DTNode<String,String>>();
					DTGraph<String,String> graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, data.getRDFData().getInstances(), instanceNodes, true);
					SingleDTGraph g = new SingleDTGraph(graph, instanceNodes);
					System.out.println("Built Graph with " + graph.nodes().size() + ", and " + graph.links().size() + " links");

					cache.get(seed).get(inf).put(depth, new Pair<SingleDTGraph,List<Double>>(g, new ArrayList<Double>(data.getTarget())));
				}
			}
		}
		return cache;
	}
}
