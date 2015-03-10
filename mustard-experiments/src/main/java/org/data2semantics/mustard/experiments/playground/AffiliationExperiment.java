package org.data2semantics.mustard.experiments.playground;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.experiments.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.BGSLithoDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.data.MutagDataSet;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWLSubTreeApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeIDEQKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeIDEQKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountKernel;
import org.data2semantics.mustard.learners.evaluation.AUCPR;
import org.data2semantics.mustard.learners.evaluation.AUCROC;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.evaluation.Precision;
import org.data2semantics.mustard.learners.evaluation.Recall;
import org.data2semantics.mustard.learners.evaluation.SingleClassAccuracy;
import org.data2semantics.mustard.learners.evaluation.SingleClassF1;
import org.data2semantics.mustard.learners.evaluation.SingleClassPrecision;
import org.data2semantics.mustard.learners.evaluation.SingleClassRecall;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.rdfvault.RDFGraphListURIPrefixKernel;
import org.data2semantics.mustard.utils.Pair;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class AffiliationExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		RDFDataSet tripleStore = new RDFFileDataSet("datasets/carcinogenesis.owl", RDFFormat.forFileName("datasets/carcinogenesis.owl"));
		//RDFDataSet tripleStore = new RDFFileDataSet("C:\\Users\\Gerben\\OneDrive\\D2S\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);

		//ClassificationDataSet ds = new AIFBDataSet(tripleStore, false);
		ClassificationDataSet ds = new MutagDataSet(tripleStore);
		//ClassificationDataSet ds = new BGSLithoDataSet(tripleStore);
		ds.create();

		System.out.println(ds.getRDFData().getInstances());

		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());
		//evalFuncs.add(new Precision());
		//evalFuncs.add(new Recall());
		//evalFuncs.add(new AUCROC());
		//evalFuncs.add(new AUCPR());


		/*
		Set<Double> set = new HashSet<Double>();
		for (double d : ds.getTarget()) {
			if (!set.contains(d)) {
				evalFuncs.add(new SingleClassAccuracy(d));
				evalFuncs.add(new SingleClassF1(d));
				evalFuncs.add(new SingleClassPrecision(d));
				evalFuncs.add(new SingleClassRecall(d));
				set.add(d);
			}
		}
		 */

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(3);
		resTable.setSignificanceTest(ResultsTable.SigTest.PAIRED_TTEST);
		resTable.setpValue(0.05);
		resTable.setShowStdDev(true);

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {1,10,100,1000};	

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(5);
		svmParms.setProbEstimates(false);

		//svmParms.setEvalFunction(new AUCPR());

		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean[] inference = {false,true};

		int[] depths = {1,2,3};
		int[] pathDepths = {0,1,2,3,4,5,6};
		int[] iterationsWL = {0,1,2,3,4,5,6};

		boolean depthTimesTwo = true;


		double[] lambdas = {1.0};

		double[] depthDecays = {2 / Math.sqrt(2), 1 / Math.sqrt(2), 1, Math.sqrt(2), 2};
		//int[][] maxPrevNBHs = {{1},{2},{100000}};
		int[][] maxPrevNBHs = {{1}};
		//int[][] maxCards    = {{1},{2},{100000}};
		int[][] maxCards    = {{6}};
		int[][] minFreqs    = {{1,2,4,8}};

		double[] depthWeights = {0.0};

		RDFData data = ds.getRDFData();
		List<Double> target = ds.getTarget();

		//computeGraphStatistics(tripleStore, ds, inference, depths);


		/*
		for (boolean inf : inference) {
			resTable.newRow("URI Prefix: " + inf);
			List<RDFGraphListURIPrefixKernel> kernelsBaseline = new ArrayList<RDFGraphListURIPrefixKernel>();	

			for (int d : depths) {				 
				for (double l : lambdas) {
					kernelsBaseline.add(new RDFGraphListURIPrefixKernel(l, d, inf, true));
				}
			}
			SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

			exp.run();

			for (Result res : exp.getResults()) {
				resTable.addResult(res);
			}
		}
		//*/

		///* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL: " + inf);

			for (int d : depths) {
				List<RDFWLSubTreeKernel> kernelsBaseline = new ArrayList<RDFWLSubTreeKernel>();	
				kernelsBaseline.add(new RDFWLSubTreeKernel(0, d, inf, reverseWL, false, true));

				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		///* The baseline experiment, BoW (or BoL if you prefer) Tree variant
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL Tree: " + inf);		 		

			for (int d : depths) {
				List<RDFTreeWLSubTreeKernel> kernelsBaseline = new ArrayList<RDFTreeWLSubTreeKernel>();	
				kernelsBaseline.add(new RDFTreeWLSubTreeKernel(0, d, inf, reverseWL, false, true));

				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}

		}
		//*/

		/* Path Count Root
		for (boolean inf : inference) {
			resTable.newRow("Path Count through root: " + inf);		 
			for (int d : depths) {

				List<RDFRootWalkCountKernel> kernels = new ArrayList<RDFRootWalkCountKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFRootWalkCountKernel(d*2, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFRootWalkCountKernel(dd, inf, true));
					}
				}				

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		/* WL Root
		for (boolean inf : inference) {
			resTable.newRow("WL through root: " + inf);		 
			for (int d : depths) {

				List<RDFRootWLSubTreeKernel> kernels = new ArrayList<RDFRootWLSubTreeKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFRootWLSubTreeKernel(d*2, inf, false, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFRootWLSubTreeKernel(dd, inf, false, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		/* Path Count Tree
		for (boolean inf : inference) {
			resTable.newRow("Path Count Tree: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWalkCountKernel> kernels = new ArrayList<RDFTreeWalkCountKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWalkCountKernel(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWalkCountKernel(dd, d, inf, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		/* Path Count Fast
		for (boolean inf : inference) {
			resTable.newRow("Path Count Fast: " + inf);		 
			for (int d : depths) {

				List<RDFWalkCountKernel> kernels = new ArrayList<RDFWalkCountKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFWalkCountKernel(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWalkCountKernel(dd, d, inf, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/


		///* WL
		for (boolean inf : inference) {
			resTable.newRow("WL: " + inf);		 


			for (int d : depths) {
				List<RDFGraphListWLSubTreeKernel> kernels = new ArrayList<RDFGraphListWLSubTreeKernel>();


				if (depthTimesTwo) {
					kernels.add(new RDFGraphListWLSubTreeKernel(d*2, d, inf, reverseWL, true, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFGraphListWLSubTreeKernel(dd, d, inf, reverseWL, true, true));
					}
				}


				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/


		///* WL EdgeSets
		for (boolean inf : inference) {
			resTable.newRow("WL Approx: " + inf);		 
			for (int d : depths) {

				List<RDFGraphListWLSubTreeApproxKernel> kernels = new ArrayList<RDFGraphListWLSubTreeApproxKernel>();	

				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDecay : depthDecays) { 
								for (double depthWeight : depthWeights) {
									if (depthTimesTwo) {
										kernels.add(new RDFGraphListWLSubTreeApproxKernel(d*2, d, inf, reverseWL, true, depthDecay, maxPrevNBH, maxCard, minFreq, depthWeight, true));
									} else {
										for (int dd : iterationsWL) {
											kernels.add(new RDFGraphListWLSubTreeApproxKernel(dd, d, inf, reverseWL, true, depthDecay, maxPrevNBH, maxCard, minFreq, depthWeight, true));
										}
									}
								}
							}
						}
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		///* WL Tree
		for (boolean inf : inference) {
			resTable.newRow("WL Tree: " + inf);		 

			for (int d : depths) {
				List<RDFTreeWLSubTreeIDEQKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWLSubTreeIDEQKernel(d*2, d, inf, reverseWL, false, true, false,true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWLSubTreeIDEQKernel(dd, d, inf, reverseWL, false, true, false, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}

		}
		//*/

		///* WL Tree OneGraph
		for (boolean inf : inference) {
			resTable.newRow("WL Tree OneGraph: " + inf);		 

			for (int d : depths) {
				List<RDFTreeWLSubTreeIDEQKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWLSubTreeIDEQKernel(d*2, d, inf, reverseWL, false, true, true, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWLSubTreeIDEQKernel(dd, d, inf, reverseWL, false, true, true, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}

		}
		//*/


		///* WL EdgeSets Tree
		for (boolean inf : inference) {
			resTable.newRow("WL Tree Approx: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQApproxKernel>();	

				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDecay : depthDecays) { 
								if (depthTimesTwo) {
									kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthDecay, maxPrevNBH, maxCard, minFreq, true));
								} else {
									for (int dd : iterationsWL) {
										kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, false, depthDecay, maxPrevNBH, maxCard, minFreq, true));
									}
								}
							}
						}
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/


		///* WL EdgeSets Tree
		for (boolean inf : inference) {
			resTable.newRow("WL Tree Approx One Graph: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQApproxKernel>();	

				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDecay : depthDecays) { 
								if (depthTimesTwo) {
									kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, true, depthDecay, maxPrevNBH, maxCard, minFreq, true));
								} else {
									for (int dd : iterationsWL) {
										kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, true, depthDecay, maxPrevNBH, maxCard, minFreq, true));
									}
								}
							}
						}
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/



		///* WL Fast
		for (boolean inf : inference) {
			resTable.newRow("WL Fast: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeIDEQKernel> kernels = new ArrayList<RDFWLSubTreeIDEQKernel>();	
				if (depthTimesTwo) {
					kernels.add(new RDFWLSubTreeIDEQKernel(d*2, d, inf, reverseWL, false, true, false, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWLSubTreeIDEQKernel(dd, d, inf, reverseWL, false, true, false, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		///* WL Fast One Graph
		for (boolean inf : inference) {
			resTable.newRow("WL Fast OneGraph: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeIDEQKernel> kernels = new ArrayList<RDFWLSubTreeIDEQKernel>();	
				if (depthTimesTwo) {
					kernels.add(new RDFWLSubTreeIDEQKernel(d*2, d, inf, reverseWL, false, true, true, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWLSubTreeIDEQKernel(dd, d, inf, reverseWL, false, true, true, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/


		///* WL Fast Approx
		for (boolean inf : inference) {
			resTable.newRow("WL Fast Approx: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFWLSubTreeIDEQApproxKernel>();	
				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDecay : depthDecays) {
								if (depthTimesTwo) {
									kernels.add(new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthDecay, maxPrevNBH, maxCard, minFreq, true));
								} else {
									for (int dd : iterationsWL) {
										kernels.add(new RDFWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, false, depthDecay, maxPrevNBH, maxCard, minFreq, true));
									}
								}
							}
						}
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/


		///* WL Fast Approx One Graph
		for (boolean inf : inference) {
			resTable.newRow("WL Fast Approx One Graph: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFWLSubTreeIDEQApproxKernel>();	
				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDecay : depthDecays) {
								if (depthTimesTwo) {
									kernels.add(new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, true, depthDecay, maxPrevNBH, maxCard, minFreq, true));
								} else {
									for (int dd : iterationsWL) {
										kernels.add(new RDFWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, true, depthDecay, maxPrevNBH, maxCard, minFreq, true));
									}
								}
							}
						}
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/





		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

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
}
