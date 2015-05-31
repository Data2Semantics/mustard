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
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWalkCountApproxKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWalkCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeIDEQKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountIDEQApproxKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountIDEQKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeGeoProbApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeGeoProbKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeIDEQKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountIDEQKernel;
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
		RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		//RDFDataSet tripleStore = new RDFFileDataSet("datasets/carcinogenesis.owl", RDFFormat.forFileName("datasets/carcinogenesis.owl"));
		//RDFDataSet tripleStore = new RDFFileDataSet("C:\\Users\\Gerben\\OneDrive\\D2S\\data_bgs_ac_uk_ALL", RDFFormat.NTRIPLES);

		ClassificationDataSet ds = new AIFBDataSet(tripleStore, false);
		//ClassificationDataSet ds = new MutagDataSet(tripleStore);
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

		int[] depths = {1,2};
		int[] pathDepths = {0,1,2,3,4,5,6};
		int[] iterationsWL = {0,1,2,3,4,5,6};

		boolean depthTimesTwo = true;



		double[] depthWeights = {1.0}; //, 1.0, 2.0};
		//double[] depthDiffWeights = {0.5, 0.7, 1.0, 1.4, 2};
		double[] depthDiffWeights = {1.0}; //, 1.0, 2.0};
		//int[][] maxPrevNBHs = {{1},{2},{100000}};
		int[][] maxPrevNBHs = {{6}};
		//int[][] maxCards    = {{1},{2},{100000}};
		int[][] maxCards    = {{1000}};
		int[][] minFreqs    = {{1}}; // {{1},{2},{4},{8},{16}};


		int[] baseMaxPrevNBH = {6};
		int[] baseMaxCard = {100000};
		int[] baseMinFreq = {0};


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

		/* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL: " + inf);		 
			for (int d : depths) {

				List<RDFWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFWLSubTreeIDEQApproxKernel>();	

				for (double depthDiffWeight : depthDiffWeights) { 
					for (double depthWeight : depthWeights) {
						if (depthTimesTwo) {
							kernels.add(new RDFWLSubTreeIDEQApproxKernel(0, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, baseMaxPrevNBH, baseMaxCard, baseMinFreq, true));
						} else {
							for (int dd : iterationsWL) {
								kernels.add(new RDFWLSubTreeIDEQApproxKernel(0, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, baseMaxPrevNBH, baseMaxCard, baseMinFreq, true));
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

		/* The baseline experiment, BoW (or BoL if you prefer) Tree variant
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL Tree: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQApproxKernel>();	

				for (double depthDiffWeight : depthDiffWeights) { 
					for (double depthWeight : depthWeights) {
						if (depthTimesTwo) {
							kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(0, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, baseMaxPrevNBH, baseMaxCard, baseMinFreq, true));
						} else {
							for (int dd : iterationsWL) {
								kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(0, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, baseMaxPrevNBH, baseMaxCard, baseMinFreq, true));
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

		/* Path Count Tree
		for (boolean inf : inference) {
			resTable.newRow("Path Count Tree MkII: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWalkCountKernelMkII> kernels = new ArrayList<RDFTreeWalkCountKernelMkII>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWalkCountKernelMkII(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWalkCountKernelMkII(dd, d, inf, true));
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

		///* Path Count Tree
		for (boolean inf : inference) {
			resTable.newRow("Path Count Tree IDEQ: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWalkCountIDEQKernelMkII> kernels = new ArrayList<RDFTreeWalkCountIDEQKernelMkII>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWalkCountIDEQKernelMkII(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWalkCountIDEQKernelMkII(dd, d, inf, true));
					}
				}

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();
				
				System.out.println(exp.getUsedKernels());

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		///* Path Count Tree Approx
		for (boolean inf : inference) {
			resTable.newRow("Path Count Tree IDEQ Approx: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWalkCountIDEQApproxKernelMkII> kernels = new ArrayList<RDFTreeWalkCountIDEQApproxKernelMkII>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWalkCountIDEQApproxKernelMkII(d*2, d, inf, 1, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWalkCountIDEQApproxKernelMkII(dd, d, inf, 1, true));
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

		///* Path Count Fast
		for (boolean inf : inference) {
			resTable.newRow("Path Count Fast IDEQ: " + inf);		 
			for (int d : depths) {

				List<RDFWalkCountIDEQKernel> kernels = new ArrayList<RDFWalkCountIDEQKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFWalkCountIDEQKernel(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWalkCountIDEQKernel(dd, d, inf, true));
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
		
		///* Path Count Fast Approx
		for (boolean inf : inference) {
			resTable.newRow("Path Count Fast IDEQ Approx: " + inf);		 
			for (int d : depths) {

				List<RDFWalkCountIDEQApproxKernel> kernels = new ArrayList<RDFWalkCountIDEQApproxKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFWalkCountIDEQApproxKernel(d*2, d, inf, 1, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWalkCountIDEQApproxKernel(dd, d, inf, 1, true));
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
		
		///* Path Count Full
		for (boolean inf : inference) {
			resTable.newRow("Path Count Full: " + inf);		 
			for (int d : depths) {

				List<RDFGraphListWalkCountKernelMkII> kernels = new ArrayList<RDFGraphListWalkCountKernelMkII>();	

				if (depthTimesTwo) {
					kernels.add(new RDFGraphListWalkCountKernelMkII(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFGraphListWalkCountKernelMkII(dd, d, inf, true));
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
		
		///* Path Count Full Approx
		for (boolean inf : inference) {
			resTable.newRow("Path Count Full Approx: " + inf);		 
			for (int d : depths) {

				List<RDFGraphListWalkCountApproxKernelMkII> kernels = new ArrayList<RDFGraphListWalkCountApproxKernelMkII>();	

				if (depthTimesTwo) {
					kernels.add(new RDFGraphListWalkCountApproxKernelMkII(d*2, d, inf, 1, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFGraphListWalkCountApproxKernelMkII(dd, d, inf, 1, true));
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
							for (double depthDiffWeight : depthDiffWeights) { 
								for (double depthWeight : depthWeights) {
									if (depthTimesTwo) {
										kernels.add(new RDFGraphListWLSubTreeApproxKernel(d*2, d, inf, reverseWL, true, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
									} else {
										for (int dd : iterationsWL) {
											kernels.add(new RDFGraphListWLSubTreeApproxKernel(dd, d, inf, reverseWL, true, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
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

		/* WL Tree
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

		/* WL Tree OneGraph
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


		/* WL EdgeSets Tree
		for (boolean inf : inference) {
			resTable.newRow("WL Tree Approx: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQApproxKernel>();	

				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDiffWeight : depthDiffWeights) { 
								for (double depthWeight : depthWeights) {
									if (depthTimesTwo) {
										kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
									} else {
										for (int dd : iterationsWL) {
											kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
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


		/* WL EdgeSets Tree
		for (boolean inf : inference) {
			resTable.newRow("WL Tree Approx One Graph: " + inf);		 
			for (int d : depths) {

				List<RDFTreeWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFTreeWLSubTreeIDEQApproxKernel>();	

				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDiffWeight : depthDiffWeights) { 
								for (double depthWeight : depthWeights) {
									if (depthTimesTwo) {
										kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, true, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
									} else {
										for (int dd : iterationsWL) {
											kernels.add(new RDFTreeWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, true, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
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

		/* WL Fast
		for (boolean inf : inference) {
			resTable.newRow("WL Fast: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	
				if (depthTimesTwo) {
					kernels.add(new RDFWLSubTreeKernel(d*2, d, inf, reverseWL, false, true, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWLSubTreeKernel(dd, d, inf, reverseWL, false, true, true));
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


		/* WL Fast
		for (boolean inf : inference) {
			resTable.newRow("WL Fast IDEQ: " + inf);		 

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

		/* WL Fast One Graph
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


		/* WL Geo Prob
		for (boolean inf : inference) {
			resTable.newRow("WL Geo Prob: " + inf);		 

			int[] blu = {1,2,3};

			for (int d : blu) {
				List<RDFWLSubTreeGeoProbKernel> kernels = new ArrayList<RDFWLSubTreeGeoProbKernel>();	
				if (depthTimesTwo) {
					kernels.add(new RDFWLSubTreeGeoProbKernel(d*2, d, inf, false, (double) d, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWLSubTreeGeoProbKernel(dd, d, inf, false, (double) d, true));
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

		/* WL Geo Prob Approx
		for (boolean inf : inference) {
			resTable.newRow("WL Geo Prob Approx: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeGeoProbApproxKernel> kernels = new ArrayList<RDFWLSubTreeGeoProbApproxKernel>();	
				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDiffWeight : depthDiffWeights) {
								if (depthTimesTwo) {
									kernels.add(new RDFWLSubTreeGeoProbApproxKernel((d+1)*2, d+1, inf, (double) d, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));						
								} else {
									for (int dd : iterationsWL) {
										kernels.add(new RDFWLSubTreeGeoProbApproxKernel(dd, d, inf, (double) d, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
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



		///* WL Fast Approx
		for (boolean inf : inference) {
			resTable.newRow("WL Fast Approx: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFWLSubTreeIDEQApproxKernel>();	
				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDiffWeight : depthDiffWeights) {
								for (double depthWeight : depthWeights) {
									if (depthTimesTwo) {
										kernels.add(new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
									} else {
										for (int dd : iterationsWL) {
											kernels.add(new RDFWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
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


		/* WL Fast Approx One Graph
		for (boolean inf : inference) {
			resTable.newRow("WL Fast Approx One Graph: " + inf);		 

			for (int d : depths) {
				List<RDFWLSubTreeIDEQApproxKernel> kernels = new ArrayList<RDFWLSubTreeIDEQApproxKernel>();	
				for (int[] minFreq : minFreqs) {
					for (int[] maxCard : maxCards) {
						for (int[] maxPrevNBH : maxPrevNBHs) {
							for (double depthDiffWeight : depthDiffWeights) {
								for (double depthWeight : depthWeights) {
									if (depthTimesTwo) {
										kernels.add(new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, true, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
									} else {
										for (int dd : iterationsWL) {
											kernels.add(new RDFWLSubTreeIDEQApproxKernel(dd, d, inf, reverseWL, false, true, true, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true));
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
