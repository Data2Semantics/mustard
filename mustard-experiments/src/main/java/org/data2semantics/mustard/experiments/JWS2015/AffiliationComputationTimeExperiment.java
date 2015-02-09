package org.data2semantics.mustard.experiments.JWS2015;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.data2semantics.mustard.experiments.GraphFeatureVectorKernelComputationTimeExperiment;
import org.data2semantics.mustard.experiments.GraphKernelComputationTimeExperiment;
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
import org.data2semantics.mustard.kernels.graphkernels.graphlist.WLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFGraphListWalkCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFHubRemovalWrapperFeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFIntersectionSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFTreeWalkCountKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWalkCountKernel;
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
import org.data2semantics.mustard.utils.Pair;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class AffiliationComputationTimeExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String file = AIFB_FILE;
		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				file = args[++i];
			}
		}		

		RDFDataSet tripleStore = new RDFFileDataSet(file, RDFFormat.N3);
		ClassificationDataSet ds = new AIFBDataSet(tripleStore);

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


		///* WARMUP ROUND
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

		///* BoL - Graph
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("BoL Graph: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	
						kernels.add(new RDFWLSubTreeKernel(0, d, inf, reverseWL, false, trackPrevNBH, true));

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

		///* BoL - Tree
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("BoL Tree: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFTreeWLSubTreeKernel> kernels = new ArrayList<RDFTreeWLSubTreeKernel>();	
						kernels.add(new RDFTreeWLSubTreeKernel(0, d, inf, reverseWL, false, trackPrevNBH, true));

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

		///* Root Walk Count
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("Root Walk Count: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFRootWalkCountKernel> kernels = new ArrayList<RDFRootWalkCountKernel>();	
						kernels.add(new RDFRootWalkCountKernel(d*2, inf, true));

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


		///* RDF Walk Count
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("RDF Walk Count: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFWalkCountKernel> kernels = new ArrayList<RDFWalkCountKernel>();	
						kernels.add(new RDFWalkCountKernel(d*2, d, inf, true));

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

		///* RDF Tree Walk Count MkII
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("RDF Tree Walk Count MkII: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFTreeWalkCountKernelMkII> kernels = new ArrayList<RDFTreeWalkCountKernelMkII>();	
						kernels.add(new RDFTreeWalkCountKernelMkII(d*2, d, inf, true));

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



		//resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);	

		
		///* Regular WC mkII
		for (boolean inf : inference) {			 
			for (int d : depths) {
				for (double frac : fractions) {
					resTable.newRow("Regular Walk Count MkII: " + inf);	
					List<Result> tempRes = new ArrayList<Result>();
					for (long seed : seeds) {
						RDFData dataSub = createRandomSubset(data, frac, seed);

						List<RDFGraphListWalkCountKernelMkII> kernels = new ArrayList<RDFGraphListWalkCountKernelMkII>();	
						kernels.add(new RDFGraphListWalkCountKernelMkII(d*2, d, inf, true));

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

					//resTable.addCompResults(resTable.getBestResults());
					System.out.println(resTable);
				}
			}
		}
		//*/

		//resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
	}

	private static RDFData createRandomSubset(RDFData data, double fraction, long seed) {
		List<Resource> i = data.getInstances();
		Collections.shuffle(i, new Random(seed));

		i = i.subList(0, (int) Math.round(fraction * i.size()));
		return new RDFData(data.getDataset(), i, data.getBlackList());
	}
}
