package org.data2semantics.mustard.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.AMDataSet;
import org.data2semantics.mustard.experiments.data.BGSDataSet;
import org.data2semantics.mustard.experiments.data.BGSLithoDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.experiments.utils.GraphFeatureVectorKernelComputationTimeExperiment;
import org.data2semantics.mustard.experiments.utils.GraphKernelComputationTimeExperiment;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFHubRemovalWrapperFeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFIntersectionSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.util.Pair;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class ComputationTimeExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";
	private static String BGS_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	private static String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";
	private static String ISWC_FOLDER = "datasets/";

	//private static List<Resource> instances;
	//private static List<Value> labels;
	//private static List<Statement> blackList;
	//private static List<Double> target;
	//private static RDFDataSet dataset;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		//ClassificationDataSet ds = new AIFBDataSet(tripleStore);

		RDFDataSet tripleStore = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
		ClassificationDataSet ds = new BGSLithoDataSet(tripleStore);

		ds.create();

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);
		resTable.setSignificanceTest(ResultsTable.SigTest.PAIRED_TTEST);
		resTable.setpValue(0.05);
		resTable.setShowStdDev(true);



		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean trackPrevNBH = true; // We should not repeat vertices that get the same label after an iteration of WL (regular WL does this)
		boolean[] inference = {true};
		int[] depths = {3};

		double[] fractions = {0.2, 0.4, 0.6, 0.8, 1.0};
		long[] seeds = {11,21,31,41,51,61,71,81,91,101};


		RDFData data = ds.getRDFData();


		///* RDF WL 
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("RDF WL: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	
						kernels.add(new RDFWLSubTreeKernel(d*2, d, inf, reverseWL, false, trackPrevNBH, true));

						GraphKernelComputationTimeExperiment<RDFData> exp = new GraphKernelComputationTimeExperiment<RDFData>(kernels, dataSub, null);

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
		}
		//*/

		
		///* Regular WL 
		for (boolean inf : inference) {		 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("Regular WL: " + inf);
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFGraphListWLSubTreeKernel> kernels = new ArrayList<RDFGraphListWLSubTreeKernel>();	
						kernels.add(new RDFGraphListWLSubTreeKernel(d*2, d, inf, reverseWL, trackPrevNBH, true));

						GraphKernelComputationTimeExperiment<RDFData> exp = new GraphKernelComputationTimeExperiment<RDFData>(kernels, dataSub, null);

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
		}
		//*/
		
		///* RDF WL 
		for (boolean inf : inference) {	 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("RDF WL: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	
						kernels.add(new RDFWLSubTreeKernel(d*2, d, inf, reverseWL, false, trackPrevNBH, true));

						GraphKernelComputationTimeExperiment<RDFData> exp = new GraphKernelComputationTimeExperiment<RDFData>(kernels, dataSub, null);

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
		}
		//*/

		
		///* Tree WL 
		for (boolean inf : inference) {	 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("RDF WL Tree: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFTreeWLSubTreeKernel> kernels = new ArrayList<RDFTreeWLSubTreeKernel>();	
						kernels.add(new RDFTreeWLSubTreeKernel(d*2, d, inf, reverseWL, trackPrevNBH, true));

						GraphKernelComputationTimeExperiment<RDFData> exp = new GraphKernelComputationTimeExperiment<RDFData>(kernels, dataSub, null);

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
		}
		//*/

		
		///*  IST
		for (boolean inf : inference) {	 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("IST: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFIntersectionSubTreeKernel> kernels = new ArrayList<RDFIntersectionSubTreeKernel>();	
						kernels.add(new RDFIntersectionSubTreeKernel(d, 1, inf, true));

						GraphKernelComputationTimeExperiment<RDFData> exp = new GraphKernelComputationTimeExperiment<RDFData>(kernels, dataSub, null);

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
		}
		//*/

	
		/* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL: " + inf);		 
			for (int d : depths) {
				List<RDFWLSubTreeKernel> kernelsBaseline = new ArrayList<RDFWLSubTreeKernel>();	
				kernelsBaseline.add(new RDFWLSubTreeKernel(0, d, inf, reverseWL, false, trackPrevNBH, true));

				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		/* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL Hubs: " + inf);		 
			for (int d : depths) {
				List<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>>kernelsBaseline = new ArrayList<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>>();	

				for (int minHubSize : mhs) {
					kernelsBaseline.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>(new DTGraphWLSubTreeKernel(0, d, reverseWL, false, trackPrevNBH, true), d, inf, minHubSize, true));
				}
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

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
				List<RDFTreeWLSubTreeKernel> kernelsBaseline = new ArrayList<RDFTreeWLSubTreeKernel>();	
				kernelsBaseline.add(new RDFTreeWLSubTreeKernel(0, d, inf, reverseWL, false, trackPrevNBH, true));

				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		/* The baseline experiment, BoW (or BoL if you prefer) Tree variant
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL Tree Hubs: " + inf);		 
			for (int d : depths) {
				List<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>>kernelsBaseline = new ArrayList<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>>();	
				for (int minHubSize : mhs) {
					kernelsBaseline.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>(new DTGraphTreeWLSubTreeKernel(0, d, reverseWL, false, trackPrevNBH, true), d, inf, minHubSize, true));
				}
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
				kernels.add(new RDFRootWalkCountKernel(d*2, inf, true));

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
			resTable.newRow("Path Count through root Hubs: " + inf);		 
			for (int d : depths) {
				List<RDFHubRemovalWrapperKernel<DTGraphRootWalkCountKernel>>kernels = new ArrayList<RDFHubRemovalWrapperKernel<DTGraphRootWalkCountKernel>>();	
				kernels.add(new RDFHubRemovalWrapperKernel<DTGraphRootWalkCountKernel>(new DTGraphRootWalkCountKernel(d*2, true), d, inf, maxHubs, stepSize, true));

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
				kernels.add(new RDFRootWLSubTreeKernel(d*2, inf, false, true));

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
			resTable.newRow("WL through root Hubs: " + inf);		 
			for (int d : depths) {
				List<RDFHubRemovalWrapperKernel<DTGraphRootWLSubTreeKernel>>kernels = new ArrayList<RDFHubRemovalWrapperKernel<DTGraphRootWLSubTreeKernel>>();	
				kernels.add(new RDFHubRemovalWrapperKernel<DTGraphRootWLSubTreeKernel>(new DTGraphRootWLSubTreeKernel(d*2, true), d, inf, maxHubs, stepSize, true));

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
			resTable.newRow("Path Count Tree Hubs: " + inf);		 
			for (int d : depths) {
				List<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWalkCountKernel>>kernels = new ArrayList<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWalkCountKernel>>();	

				if (depthTimesTwo) {
					for (int minHubSize : mhs) {
						kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWalkCountKernel>(new DTGraphTreeWalkCountKernel(d*2, d, true), d, inf, minHubSize, true));
					}
				} else {
					for (int dd : iterationsWL) {
						for (int minHubSize : mhs) {
							kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWalkCountKernel>(new DTGraphTreeWalkCountKernel(dd, d, true), d, inf, minHubSize, true));
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

				List<RDFTreeWLSubTreeKernel> kernels = new ArrayList<RDFTreeWLSubTreeKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFTreeWLSubTreeKernel(d*2, d, inf, reverseWL, false, trackPrevNBH, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFTreeWLSubTreeKernel(dd, d, inf, reverseWL, false, trackPrevNBH, true));
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
			resTable.newRow("WL Tree Hubs: " + inf);		 
			for (int d : depths) {

				List<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>>kernels = new ArrayList<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>>();	

				if (depthTimesTwo) {
					for (int minHubSize : mhs) {
						kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>(new DTGraphTreeWLSubTreeKernel(d*2, d, true, false, trackPrevNBH, true), d, inf, minHubSize, true));
					}
				} else {
					for (int dd : iterationsWL) {
						for (int minHubSize : mhs) {
							kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphTreeWLSubTreeKernel>(new DTGraphTreeWLSubTreeKernel(dd, d, true, false, trackPrevNBH, true), d, inf, minHubSize, true));
						}
					}
				}
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/



		/* RDF Path Count 
		for (boolean inf : inference) {
			resTable.newRow("RDF Path Count: " + inf);		 
			for (int d : depths) {

				List<RDFPathCountKernel> kernels = new ArrayList<RDFPathCountKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFPathCountKernel(d*2, d, inf, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFPathCountKernel(dd, d, inf, true));
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

		/* RDF Path Count 
		for (boolean inf : inference) {
			resTable.newRow("RDF Path Count hubs: " + inf);		 
			for (int d : depths) {

				List<RDFHubRemovalWrapperKernel<DTGraphWalkCountKernel>>kernels = new ArrayList<RDFHubRemovalWrapperKernel<DTGraphWalkCountKernel>>();	

				if (depthTimesTwo) {
					kernels.add(new RDFHubRemovalWrapperKernel<DTGraphWalkCountKernel>(new DTGraphWalkCountKernel(d*2, d, true), d, inf, maxHubs, stepSize, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFHubRemovalWrapperKernel<DTGraphWalkCountKernel>(new DTGraphWalkCountKernel(dd, d, true), d, inf, maxHubs, stepSize, true));
					}
				}

				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		/* RDF WL 
		for (boolean inf : inference) {
			resTable.newRow("RDF WL: " + inf);		 
			for (int d : depths) {

				List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFWLSubTreeKernel(d*2, d, inf, reverseWL, false, trackPrevNBH, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFWLSubTreeKernel(dd, d, inf, reverseWL, false, trackPrevNBH, true));
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

		/* RDF WL 
		for (boolean inf : inference) {
			resTable.newRow("RDF WL Hubs: " + inf);		 
			for (int d : depths) {

				List<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>>kernels = new ArrayList<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>>();	

				if (depthTimesTwo) {
					for (int minHubSize : mhs) {
						kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>(new DTGraphWLSubTreeKernel(d*2, d, true, false, trackPrevNBH, true), d, inf, minHubSize, true));
					}
				} else {
					for (int dd : iterationsWL) {
						for (int minHubSize : mhs) {
							kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphWLSubTreeKernel>(new DTGraphWLSubTreeKernel(dd, d, true, false, trackPrevNBH, true), d, inf, minHubSize, true));
						}
					}
				}

				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/


		/* Regular WL 
		for (boolean inf : inference) {
			resTable.newRow("WL: " + inf);		 
			for (int d : depths) {

				List<RDFGraphListWLSubTreeKernel> kernels = new ArrayList<RDFGraphListWLSubTreeKernel>();	

				if (depthTimesTwo) {
					kernels.add(new RDFGraphListWLSubTreeKernel(d*2, d, inf, reverseWL, trackPrevNBH, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFGraphListWLSubTreeKernel(dd, d, inf, reverseWL, trackPrevNBH, true));
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

		/* WL with HubRemoval
		for (boolean inf : inference) {
			resTable.newRow("WL Hubs: " + inf);		 
			for (int d : depths) {

				List<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphGraphListWLSubTreeKernel>> kernels = new ArrayList<RDFHubRemovalWrapperFeatureVectorKernel<DTGraphGraphListWLSubTreeKernel>>();	

				if (depthTimesTwo) {
					for (int minHubSize : mhs) {
						kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphGraphListWLSubTreeKernel>(new DTGraphGraphListWLSubTreeKernel(d*2, d, reverseWL, trackPrevNBH, true), d, inf, minHubSize, true));
					}
				} else {
					for (int dd : iterationsWL) {
						for (int minHubSize : mhs) {
							kernels.add(new RDFHubRemovalWrapperFeatureVectorKernel<DTGraphGraphListWLSubTreeKernel>(new DTGraphGraphListWLSubTreeKernel(dd, d, reverseWL, trackPrevNBH, true), d, inf, minHubSize, true));
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


		/* Regular WL
		for (boolean inf : inference) {
			resTable.newRow("Regular WL: " + inf);		
			for (int d : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), d, inf);
				st.removeAll(ds.getRDFData().getBlackList());
				DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
				List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, ds.getRDFData().getInstances());
				graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
				GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, d);

				double avgNodes = 0;
				double avgLinks = 0;

				for (DTGraph<String,String> g : graphs.getGraphs()){
					avgNodes += g.nodes().size();
					avgLinks += g.links().size();
				}
				avgNodes /= graphs.numInstances();
				avgLinks /= graphs.numInstances();

				System.out.println("Avg # nodes: " + avgNodes + " , avg # links: " + avgLinks);

				List<WLSubTreeKernel> kernels = new ArrayList<WLSubTreeKernel>();

				if (depthTimesTwo) {
					WLSubTreeKernel kernel = new WLSubTreeKernel(d*2, reverseWL, true, true);			
					kernels.add(kernel);
				} else {
					for (int dd : iterationsWL) {
						WLSubTreeKernel kernel = new WLSubTreeKernel(dd, reverseWL, true, true);			
						kernels.add(kernel);
					}
				}

				//resTable.newRow(kernels.get(0).getLabel() + "_" + inf);
				SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, graphs, target, svmParms, seeds, evalFuncs);

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

		/* Walk Count full with HubRemoval
		for (boolean inf : inference) {
			resTable.newRow("Walk Count full Hubs: " + inf);		 
			for (int d : depths) {

				List<RDFHubRemovalWrapperKernel<DTGraphGraphListWalkCountKernel>> kernels = new ArrayList<RDFHubRemovalWrapperKernel<DTGraphGraphListWalkCountKernel>>();	

				if (depthTimesTwo) {
					kernels.add(new RDFHubRemovalWrapperKernel<DTGraphGraphListWalkCountKernel>(new DTGraphGraphListWalkCountKernel(d*2, d, true), d, inf, maxHubs, stepSize, true));
				} else {
					for (int dd : iterationsWL) {
						kernels.add(new RDFHubRemovalWrapperKernel<DTGraphGraphListWalkCountKernel>(new DTGraphGraphListWalkCountKernel(dd, d, true), d, inf, maxHubs, stepSize, true));
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


		/* Path Count full
		for (boolean inf : inference) {
			resTable.newRow("Path Count Full: " + inf);		
			for (int d : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), d, inf);
				st.removeAll(ds.getRDFData().getBlackList());
				DTGraph<String,String> graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS);
				List<DTNode<String,String>> instanceNodes = RDFUtils.findInstances(graph, ds.getRDFData().getInstances());
				graph = RDFUtils.simplifyInstanceNodeLabels(graph, instanceNodes);
				GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph, instanceNodes, d);

				double avgNodes = 0;
				double avgLinks = 0;

				for (DTGraph<String,String> g : graphs.getGraphs()){
					avgNodes += g.nodes().size();
					avgLinks += g.links().size();
				}
				avgNodes /= graphs.numInstances();
				avgLinks /= graphs.numInstances();

				System.out.println("Avg # nodes: " + avgNodes + " , avg # links: " + avgLinks);

				List<WalkCountKernel> kernels = new ArrayList<WalkCountKernel>();

				if (depthTimesTwo) {
					WalkCountKernel kernel = new WalkCountKernel(d*2, true);			
					kernels.add(kernel);
				} else {
					for (int dd : iterationsWL) {
						WalkCountKernel kernel = new WalkCountKernel(dd, true);			
						kernels.add(kernel);
					}
				}

				//resTable.newRow(kernels.get(0).getLabel() + "_" + inf);
				SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, graphs, target, svmParms, seeds, evalFuncs);

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

	//	private static void createAffiliationPredictionDataSet() {
	//		dataset = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
	//
	//		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);
	//
	//		instances = new ArrayList<Resource>();
	//		labels = new ArrayList<Value>();
	//
	//		for (Statement stmt : stmts) {
	//			instances.add(stmt.getSubject());
	//			labels.add(stmt.getObject());
	//		}
	//
	//		EvaluationUtils.removeSmallClasses(instances, labels, 5);
	//		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);
	//
	//		target = EvaluationUtils.createTarget(labels);
	//	}
	//	
	//	
	//	private static void createGeoDataSet(long seed, double fraction, int minSize, String property) {
	//		dataset = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
	//		
	//		String majorityClass = "http://data.bgs.ac.uk/id/Lexicon/Class/LS";
	//		Random rand = new Random(seed);
	//
	//		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
	//		System.out.println(dataset.getLabel());
	//
	//		System.out.println("Component Rock statements: " + stmts.size());
	//		instances = new ArrayList<Resource>();
	//		labels = new ArrayList<Value>();
	//		blackList = new ArrayList<Statement>();
	//
	//		// http://data.bgs.ac.uk/ref/Lexicon/hasRockUnitRank
	//		// http://data.bgs.ac.uk/ref/Lexicon/hasTheme
	//
	//		for(Statement stmt: stmts) {
	//			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);
	//
	//			if (stmts2.size() > 1) {
	//				System.out.println("more than 1 Class");
	//			}
	//
	//			for (Statement stmt2 : stmts2) {
	//
	//				if (rand.nextDouble() <= fraction) {
	//					instances.add(stmt2.getSubject());
	//
	//					labels.add(stmt2.getObject());
	//					/*
	//				if (stmt2.getObject().toString().equals(majorityClass)) {
	//					labels.add(ds.createLiteral("pos"));
	//				} else {
	//					labels.add(ds.createLiteral("neg"));
	//				}
	//					 */
	//				}
	//			}
	//		}
	//
	//
	//		//capClassSize(50, seed);
	//		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
	//		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);
	//		target = EvaluationUtils.createTarget(labels);
	//	}
	//
	//	
	//	private static void createCommitteeMemberPredictionDataSet() {
	//		RDFFileDataSet testSetA = new RDFFileDataSet(ISWC_FOLDER + "iswc-2011-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/eswc-2011-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/eswc-2012-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/eswc-2008-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/eswc-2009-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/iswc-2012-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/iswc-2011-complete.rdf", RDFFormat.RDFXML);
	//		testSetA.addFile(ISWC_FOLDER + "iswc-2010-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/iswc-2009-complete.rdf", RDFFormat.RDFXML);
	//		//testSetA.addFile("datasets/iswc-2008-complete.rdf", RDFFormat.RDFXML);
	//
	//		RDFFileDataSet testSetB = new RDFFileDataSet(ISWC_FOLDER + "iswc-2012-complete.rdf", RDFFormat.RDFXML);
	//
	//		instances = new ArrayList<Resource>();
	//		List<Resource> instancesB = new ArrayList<Resource>();
	//		labels = new ArrayList<Value>();
	//		List<Statement> stmts = testSetA.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person");
	//		for (Statement stmt : stmts) {
	//			instancesB.add(stmt.getSubject()); 
	//		}	
	//
	//		int pos = 0, neg = 0;
	//		for (Resource instance : instancesB) {
	//			if (!testSetB.getStatements(instance, null, null).isEmpty()) {
	//				instances.add(instance);
	//				if (testSetB.getStatementsFromStrings(instance.toString(), "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/conference/iswc/2012/pc-member/research", false).size() > 0) {
	//					labels.add(testSetA.createLiteral("true"));
	//					pos++;
	//				} else {
	//					labels.add(testSetA.createLiteral("false"));
	//					neg++;
	//				}
	//			}
	//		}
	//
	//		dataset = testSetA;		
	//		blackList = new ArrayList<Statement>();
	//		target = EvaluationUtils.createTarget(labels);
	//
	//		System.out.println("Pos and Neg: " + pos + " " + neg);
	//		System.out.println("Baseline acc: " + Math.max(pos, neg) / ((double)pos+neg));
	//	}
	//	
	//	private static void createAMDataSet(long seed, double fraction, int minSize) {
	//		dataset = new RDFFileDataSet(AM_FOLDER, RDFFormat.TURTLE);
	//		
	//		Random rand = new Random(seed);
	//
	//		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://purl.org/collections/nl/am/objectCategory", null);
	//		System.out.println(dataset.getLabel());
	//
	//		System.out.println("objects in AM: " + stmts.size());
	//		
	//		
	//		instances = new ArrayList<Resource>();
	//		labels = new ArrayList<Value>();
	//		blackList = new ArrayList<Statement>();
	//
	//		for (Statement stmt : stmts) {
	//			instances.add(stmt.getSubject());
	//			labels.add(stmt.getObject());
	//		}
	//
	////		
	////		
	//		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);
	//		System.out.println(EvaluationUtils.computeClassCounts(target));
	//		
	//		Collections.shuffle(instances, new Random(seed));
	//		Collections.shuffle(labels, new Random(seed));
	//		
	//		instances = instances.subList(0, 200);
	//		labels = labels.subList(0, 200);
	//		
	//		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
	//		target = EvaluationUtils.createTarget(labels);
	//		
	//		System.out.println("Subset: ");
	//		System.out.println(EvaluationUtils.computeClassCounts(target));
	//		
	//	}

	private static RDFData createRandomSubset(RDFData data, double fraction, long seed) {
		List<Resource> i = data.getInstances();
		Collections.shuffle(i, new Random(seed));

		i = i.subList(0, (int) Math.round(fraction * i.size()));
		return new RDFData(data.getDataset(), i, data.getBlackList());
	}
}
