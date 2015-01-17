package org.data2semantics.mustard.experiments.SDM2015;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.experiments.data.SteveDataSet;
import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.libsvm.LibSVMParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.util.Pair;
import org.nodes.DTGraph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class SteveExperiment {
	private static final String STEVE_FOLDER = "/Users/oosterman/Dropbox/STeve/RDF_data/";

	private static List<Double> target;
	private static RDFDataSet tripleStore;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// load all the RDF data
		tripleStore = new RDFFileDataSet(STEVE_FOLDER, RDFFormat.RDFXML);
		LargeClassificationDataSet ds = new SteveDataSet(tripleStore, 10, 0.01, 0, 1000);

		// Define evaluation functions
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		// Prepare result table
		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(2);
		resTable.setSignificanceTest(ResultsTable.SigTest.PAIRED_TTEST);
		resTable.setpValue(0.05);
		resTable.setShowStdDev(true);

		// Define experiment and SVM parameters
		long[] seeds = { 11 };
		// long[] seedsDataset = { 11, 21, 31, 41, 51, 61, 71, 81, 91, 101 };
		long[] seedsDataset = { 11 };
		// double[] cs = { 1, 10, 100, 1000 };
		double[] cs = { 1 };

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		double fraction = 0.01;
		int minClassSize = 0;
		int maxNumClasses = 10;
		// WL should be in reverse mode, which means regular subtrees
		boolean reverseWL = true;
		// We should not repeat vertices that get the same label after an
		// iteration of WL (regular WL does this)
		boolean trackPrevNBH = true;
		boolean normalize = true;
		boolean iterationWeigthing = false;

		boolean[] inferenceValues = { true };
		int[] depths = { 1, 2, 3 };
		int[] pathDepths = { 2, 4, 6 };
		int[] iterationsWL = { 2, 4, 6 };
		boolean depthTimesTwo = true;

		/*
		 * START Basic code needed to run once on all data
		 */

		int depth = 1;
		boolean inference = true;
		// Define kernel
		List<RDFWLSubTreeKernel> kernelsOnce = new ArrayList<RDFWLSubTreeKernel>();
		kernelsOnce.add(new RDFWLSubTreeKernel(depth * 2, depth, inference, reverseWL, iterationWeigthing,
				trackPrevNBH, normalize));
		// define experiment
		ds.create();
		target = ds.getTarget();
		SimpleGraphKernelExperiment<RDFData> expOnce = new SimpleGraphKernelExperiment<RDFData>(kernelsOnce,
				ds.getRDFData(), target, svmParms, seeds, evalFuncs);
		// run exp
		expOnce.run();
		// add result to table
		resTable.newRow("RDF WL. Inference: " + inference);
		for (Result res : expOnce.getResults()) {
			resTable.addResult(res);
		}
		// show results
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

		/*
		 * END Basic code needed to run once on all data
		 */

		// prepare a Map with all the subsets of the data we want to run our
		// experiment on.
		/*
		 * TODO The Datasetcache function needs to be updated for our
		 * experiment.
		 */
		Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> cache = createDataSetCache(ds,
				seedsDataset, fraction, minClassSize, maxNumClasses, depths, inferenceValues);
		// reset the triple store for memory reasons. All data needed is in the
		// cache
		tripleStore = null;

		computeGraphStatistics(cache, seedsDataset, inferenceValues, depths);

		for (boolean inf : inferenceValues) { // for each inference value
			resTable.newRow("RDF WL: " + inf);
			for (int d : depths) { // for each depth
				List<Result> tempRes = new ArrayList<Result>();
				for (long sDS : seedsDataset) { // for each subset of the data
					// get the data from the cash
					Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inf).get(d);
					// the graph without the prediction variable
					SingleDTGraph data = p.getFirst();
					// The values of the prediction variable
					target = p.getSecond();

					// Define the kernel to use
					List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();
					kernels.add(new RDFWLSubTreeKernel(depth * 2, depth, inference, reverseWL, iterationWeigthing,
							trackPrevNBH, normalize));

					// Run the experiment
					// TODO This now uses the full data everytime, we need to
					// make it work on a subset
					SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernels,
							ds.getRDFData(), target, svmParms, seeds, evalFuncs);
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

		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);

	}

	private static void computeGraphStatistics(
			Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> cache, long[] seeds,
			boolean[] inference, int[] depths) {
		Map<Boolean, Map<Integer, Pair<Double, Double>>> stats = new HashMap<Boolean, Map<Integer, Pair<Double, Double>>>();

		for (long seed : seeds) {
			for (boolean inf : inference) {
				if (!stats.containsKey(inf)) {
					stats.put(inf, new HashMap<Integer, Pair<Double, Double>>());
				}
				for (int depth : depths) {
					if (!stats.get(inf).containsKey(depth)) {
						stats.get(inf).put(depth, new Pair<Double, Double>(0.0, 0.0));
					}

					Pair<SingleDTGraph, List<Double>> p = cache.get(seed).get(inf).get(depth);
					GraphList<DTGraph<String, String>> graphs = RDFUtils.getSubGraphs(p.getFirst().getGraph(), p
							.getFirst().getInstances(), depth);

					double v = 0;
					double e = 0;
					for (DTGraph<String, String> graph : graphs.getGraphs()) {
						v += graph.nodes().size();
						e += graph.links().size();
					}
					v /= graphs.numInstances();
					e /= graphs.numInstances();

					v += stats.get(inf).get(depth).getFirst();
					e += stats.get(inf).get(depth).getSecond();

					stats.get(inf).put(depth, new Pair<Double, Double>(v, e));
				}
			}
		}

		for (boolean k1 : stats.keySet()) {
			System.out.println("Inference: " + k1);
			for (int k2 : stats.get(k1).keySet()) {
				System.out.println("Depth " + k2 + ", vertices: " + (stats.get(k1).get(k2).getFirst() / seeds.length)
						+ " , edges: " + (stats.get(k1).get(k2).getSecond() / seeds.length));
			}
		}
	}

	private static Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> createDataSetCache(
			LargeClassificationDataSet data, long[] seeds, double fraction, int minSize, int maxClasses, int[] depths,
			boolean[] inference) {
		Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> cache = new HashMap<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>>();

		for (long seed : seeds) {
			cache.put(seed, new HashMap<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>());
			data.createSubSet(seed, fraction, minSize, maxClasses);

			for (boolean inf : inference) {
				cache.get(seed).put(inf, new HashMap<Integer, Pair<SingleDTGraph, List<Double>>>());

				for (int depth : depths) {
					System.out.println("Getting Statements...");
					Set<Statement> stmts = RDFUtils.getStatements4Depth(tripleStore, data.getRDFData().getInstances(),
							depth, inf);
					System.out.println("# Statements: " + stmts.size());
					stmts.removeAll(new HashSet<Statement>(data.getRDFData().getBlackList()));
					System.out.println("# Statements: " + stmts.size() + ", after blackList");
					System.out.println("Building Graph...");
					SingleDTGraph graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, data.getRDFData()
							.getInstances(), true);
					System.out.println("Built Graph with " + graph.getGraph().nodes().size() + ", and "
							+ graph.getGraph().links().size() + " links");

					cache.get(seed)
							.get(inf)
							.put(depth,
									new Pair<SingleDTGraph, List<Double>>(graph,
											new ArrayList<Double>(data.getTarget())));
				}
			}
		}
		return cache;
	}
}
