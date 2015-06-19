package org.data2semantics.mustard.experiments.playground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.experiments.GraphFeatureVectorKernelComputationTimeExperiment;
import org.data2semantics.mustard.experiments.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.AMDataSet;
import org.data2semantics.mustard.experiments.data.BGSDataSet;
import org.data2semantics.mustard.experiments.data.BGSLithoDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFHubRemovalWrapperFeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWalkCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWalkCountKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.utils.Pair;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class ComputationTimeLargeExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";
	private static String BGS_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	private static String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";
	private static String ISWC_FOLDER = "datasets/";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String fileDir = BGS_FOLDER;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-dir")) {
				fileDir = args[++i];
			}
		}
	
		//RDFDataSet tripleStore = new RDFFileDataSet(fileDir, RDFFormat.NTRIPLES);
		//LargeClassificationDataSet ds = new BGSDataSet(tripleStore, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme", 10, 0.02, 5, 3);

		RDFDataSet tripleStore = new RDFFileDataSet(AM_FOLDER, RDFFormat.TURTLE);
		LargeClassificationDataSet ds = new AMDataSet(tripleStore, 10, 0.003, 5, 4, true);

		long[] seedsDataset = {1,2,3,4,5,6,7,8,9,10};
		//double[] fractions = {0.02, 0.04, 0.06, 0.08, 0.1, 0.12, 0.14, 0.16, 0.18, 0.20};
		double[] fractions = {0.004, 0.008, 0.012, 0.016, 0.02};
		
		int minClassSize = 0;
		int maxNumClasses = 100;
		int[] depths = {2};
		boolean[] inference = {true};

	
		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean trackPrevNBH = true; // We should not repeat vertices that get the same label after an iteration of WL (regular WL does this)

		
		List<ResultsTable> tables = new ArrayList<ResultsTable>();
		
		ResultsTable resTableBoLGraph = new ResultsTable();
		resTableBoLGraph.setShowStdDev(true);		
		tables.add(resTableBoLGraph);
		
		ResultsTable resTableBoLTree = new ResultsTable();
		resTableBoLTree.setShowStdDev(true);		
		tables.add(resTableBoLTree);
		
		ResultsTable resTableRootWC = new ResultsTable();
		resTableRootWC.setShowStdDev(true);		
		tables.add(resTableRootWC);	
		
		ResultsTable resTableRDFWC = new ResultsTable();
		resTableRDFWC.setShowStdDev(true);		
		tables.add(resTableRDFWC);
		
		ResultsTable resTableTreeWCMkII = new ResultsTable();
		resTableTreeWCMkII.setShowStdDev(true);		
		tables.add(resTableTreeWCMkII);
		
		ResultsTable resTableTreeWC = new ResultsTable();
		resTableTreeWC.setShowStdDev(true);		
		tables.add(resTableTreeWC);
		
		ResultsTable resTableWL = new ResultsTable();
		resTableWL.setShowStdDev(true);		
		tables.add(resTableWL);
		
		ResultsTable resTableRDFWL = new ResultsTable();
		resTableRDFWL.setShowStdDev(true);		
		tables.add(resTableRDFWL);
		
		ResultsTable resTableTreeWL = new ResultsTable();
		resTableTreeWL.setShowStdDev(true);		
		tables.add(resTableTreeWL);
		

		for (double fraction : fractions) {
			Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> cache = createDataSetCache(tripleStore, ds, seedsDataset, fraction, minClassSize, maxNumClasses, depths, inference);

			///* BoL Graph	
			for (boolean inf : inference) {
				resTableBoLGraph.newRow("BoL Graph: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphWLSubTreeKernel> kernels = new ArrayList<DTGraphWLSubTreeKernel>();	
						kernels.add(new DTGraphWLSubTreeKernel(0, d, reverseWL, trackPrevNBH, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableBoLGraph.addResult(res);
					}
				}
			}
			//*/
			
			///* BoL Tree	
			for (boolean inf : inference) {
				resTableBoLTree.newRow("BoL Tree: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphTreeWLSubTreeKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeKernel>();	
						kernels.add(new DTGraphTreeWLSubTreeKernel(0, d, reverseWL, trackPrevNBH, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableBoLTree.addResult(res);
					}
				}
			}
			//*/
			
			///* Root WC	
			for (boolean inf : inference) {
				resTableRootWC.newRow("Root WC: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphRootWalkCountKernel> kernels = new ArrayList<DTGraphRootWalkCountKernel>();	
						kernels.add(new DTGraphRootWalkCountKernel(d*2, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableRootWC.addResult(res);
					}
				}
			}
			//*/
				
			
			///* RDF WC	
			for (boolean inf : inference) {
				resTableRDFWC.newRow("RDF WC: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphWalkCountKernel> kernels = new ArrayList<DTGraphWalkCountKernel>();	
						kernels.add(new DTGraphWalkCountKernel(d*2, d, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableRDFWC.addResult(res);
					}
				}
			}
			//*/
			
			///* Tree WC MkII	
			for (boolean inf : inference) {
				resTableTreeWCMkII.newRow("Tree WC MkII: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphTreeWalkCountKernelMkII> kernels = new ArrayList<DTGraphTreeWalkCountKernelMkII>();	
						kernels.add(new DTGraphTreeWalkCountKernelMkII(d*2, d, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableTreeWCMkII.addResult(res);
					}
				}
			}
			//*/

			
			/* Tree WC	
			for (boolean inf : inference) {
				resTableTreeWC.newRow("Tree WC: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphTreeWalkCountKernel> kernels = new ArrayList<DTGraphTreeWalkCountKernel>();	
						kernels.add(new DTGraphTreeWalkCountKernel(d*2, d, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableTreeWC.addResult(res);
					}
				}
			}
			//*/
			
			
			///* Regular WL 
			for (boolean inf : inference) {
				resTableWL.newRow("Regular WL: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphGraphListWLSubTreeKernel> kernels = new ArrayList<DTGraphGraphListWLSubTreeKernel>();	
						kernels.add(new DTGraphGraphListWLSubTreeKernel(d*2, d, reverseWL, trackPrevNBH, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableWL.addResult(res);
					}
				}
			}
			//*/
			
			///* RDF WL		
			for (boolean inf : inference) {
				resTableRDFWL.newRow("RDF WL: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphWLSubTreeKernel> kernels = new ArrayList<DTGraphWLSubTreeKernel>();	
						kernels.add(new DTGraphWLSubTreeKernel(d*2, d, reverseWL, trackPrevNBH, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableRDFWL.addResult(res);
					}
				}
			}
			//*/
			
			///* Tree WL		
			for (boolean inf : inference) {
				resTableTreeWL.newRow("Tree WL: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphTreeWLSubTreeKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeKernel>();	
						kernels.add(new DTGraphTreeWLSubTreeKernel(d*2, d, reverseWL, trackPrevNBH, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableTreeWL.addResult(res);
					}
				}
			}
			//*/


		}


		for (ResultsTable table : tables) {
			System.out.println(table);
		}
		
		
		ResultsTable resTableWCMkII = new ResultsTable();
		resTableWCMkII.setShowStdDev(true);		
		tables.add(resTableWCMkII);
	
		
		ResultsTable resTableWC = new ResultsTable();
		resTableWC.setShowStdDev(true);		
		tables.add(resTableWC);
	
		
		
		for (double fraction : fractions) {
			Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> cache = createDataSetCache(tripleStore, ds, seedsDataset, fraction, minClassSize, maxNumClasses, depths, inference);

			///* WC	mk2
			for (boolean inf : inference) {
				resTableWCMkII.newRow("WC mkII: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphGraphListWalkCountKernelMkII> kernels = new ArrayList<DTGraphGraphListWalkCountKernelMkII>();	
						kernels.add(new DTGraphGraphListWalkCountKernelMkII(d*2, d, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableWCMkII.addResult(res);
					}
				}
			}
			//*/
			
			for (ResultsTable table : tables) {
				System.out.println(table);
			}

			
			
			///* WC	
			for (boolean inf : inference) {
				resTableWC.newRow("WC: " + inf);		 
				for (int d : depths) {

					List<Result> tempRes = new ArrayList<Result>();
					for (long sDS : seedsDataset) {
						Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
						SingleDTGraph data = p.getFirst();

						List<DTGraphGraphListWalkCountKernel> kernels = new ArrayList<DTGraphGraphListWalkCountKernel>();	
						kernels.add(new DTGraphGraphListWalkCountKernel(d*2, d, true));

						GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph> exp = new GraphFeatureVectorKernelComputationTimeExperiment<SingleDTGraph>(kernels, data, null);

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
						resTableWC.addResult(res);
					}
				}
			}
			//*/
			
			for (ResultsTable table : tables) {
				System.out.println(table);
			}
		}


		for (ResultsTable table : tables) {
			System.out.println(table);
		}
	}

	private static void computeGraphStatistics(RDFDataSet tripleStore, ClassificationDataSet ds, boolean[] inference, int[] depths) {
		Map<Boolean, Map<Integer, Pair<Double, Double>>> stats = new HashMap<Boolean, Map<Integer, Pair<Double, Double>>>();


		for (boolean inf : inference) {
			stats.put(inf, new HashMap<Integer, Pair<Double, Double>>());
			for (int depth : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), depth, inf);
				st.removeAll(ds.getRDFData().getBlackList());
				DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
				List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, ds.getRDFData().getInstances());
				graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
				GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, depth);

				double v = 0;
				double e = 0;
				for (DTGraph<String,String> g : graphs.getGraphs()) {
					v += g.nodes().size();
					e += g.links().size();
				}
				v /= graphs.numInstances();
				e /= graphs.numInstances();

				stats.get(inf).put(depth, new Pair<Double,Double>(v,e));
			}

		}

		for (boolean k1 : stats.keySet()) {
			System.out.println("Inference: " + k1);
			for (int k2 : stats.get(k1).keySet()) {
				System.out.println("Depth " + k2 + ", vertices: " + (stats.get(k1).get(k2).getFirst()) + " , edges: " + (stats.get(k1).get(k2).getSecond()));
			}
		}
	}

	

	private static Map<Long, Map<Boolean, Map<Integer,Pair<SingleDTGraph, List<Double>>>>> createDataSetCache(RDFDataSet tripleStore, LargeClassificationDataSet data, long[] seeds, double fraction, int minSize, int maxClasses, int[] depths, boolean[] inference) {
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

					SingleDTGraph graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, data.getRDFData().getInstances(), true);

					System.out.println("Built Graph with " + graph.getGraph().nodes().size() + ", and " + graph.getGraph().links().size() + " links");

					cache.get(seed).get(inf).put(depth, new Pair<SingleDTGraph,List<Double>>(graph, new ArrayList<Double>(data.getTarget())));
				}
			}
		}
		return cache;
	}
}
