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
import org.data2semantics.mustard.experiments.utils.SimpleGraphFeatureVectorKernelExperiment;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel;
import org.data2semantics.mustard.learners.evaluation.Accuracy;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;
import org.data2semantics.mustard.learners.evaluation.F1;
import org.data2semantics.mustard.learners.liblinear.LibLINEARParameters;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.util.Pair;
import org.nodes.DTGraph;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class SteveExperiment {
	public static final String STEVE_FOLDER = "/Users/oosterman/Dropbox/STeve/RDF_data/";

	private static List<Double> target;
	private static RDFDataSet tripleStore;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// load all the RDF data
		tripleStore = new RDFFileDataSet(STEVE_FOLDER, RDFFormat.RDFXML);
		LargeClassificationDataSet ds = new SteveDataSet(tripleStore, 10);

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

		/*
		 * LIBSVM parameters
		 */
		// the c parameter for the SVM. Higher values mean more training with
		// chance of overtraining
		double[] cs = { 1 }; // { 1, 10, 100, 1000 };
		LibLINEARParameters svmParms = new LibLINEARParameters(LibLINEARParameters.SVC_DUAL, cs);
		svmParms.setNumFolds(5);

		/*
		 * Random generator seeds so the experiment is reproducible
		 */
		// Repeat experiment for same dataset (only once since we have the
		// subsets)
		long[] seeds = { 1 };
		// the seeds used for generating random subsets
		long[] seedsDataset = { 11, 21, 31, 41, 51, 61, 71, 81, 91, 101 };

		// WL should be in reverse mode, which means regular subtrees
		boolean reverseWL = true;
		// We should not repeat vertices that get the same label after an
		// iteration of WL (regular WL does this)
		boolean trackPrevNBH = true;
		boolean normalize = true;
		boolean iterationWeigthing = false;
		// always use inference (although in our case it has no effect)
		boolean inference = true;
		int[] depths = { 1, 2, 3 };

		// prepare a Map with all the subsets of the data we want to run our
		// experiment on.
		Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> cache = createDataSetCache(ds,
				seedsDataset, depths, inference);
		// reset the triple store for memory reasons. All data needed is in the
		// cache
		tripleStore = null;

		computeGraphStatistics(cache, seedsDataset, inference, depths);

		for (int depth : depths) { // for each depth
			resTable.newRow("Depth: " + depth);
			List<Result> tempRes = new ArrayList<Result>();
			for (long sDS : seedsDataset) { // for each subset of the data
				// get the data from the cash
				Pair<SingleDTGraph, List<Double>> p = cache.get(sDS).get(inference).get(depth);
				// the graph without the prediction variable
				SingleDTGraph data = p.getFirst();
				// The values of the prediction variable
				target = p.getSecond();

				// Define the kernel to use
				List<DTGraphWLSubTreeKernel> kernels = new ArrayList<DTGraphWLSubTreeKernel>();
				kernels.add(new DTGraphWLSubTreeKernel(depth * 2, depth, reverseWL, iterationWeigthing, trackPrevNBH,
						normalize));

				// Run the experiment
				SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph> exp = new SimpleGraphFeatureVectorKernelExperiment<SingleDTGraph>(
						kernels, data, target, svmParms, seeds, evalFuncs);
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
		resTable.addCompResults(resTable.getBestResults());
		System.out.println(resTable);
		System.out.println(resTable.allScoresToString());

	}

	private static void computeGraphStatistics(
			Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> cache, long[] seeds,
			boolean inference, int[] depths) {
		Map<Boolean, Map<Integer, Pair<Double, Double>>> stats = new HashMap<Boolean, Map<Integer, Pair<Double, Double>>>();

		for (long seed : seeds) {

			if (!stats.containsKey(inference)) {
				stats.put(inference, new HashMap<Integer, Pair<Double, Double>>());
			}
			for (int depth : depths) {
				if (!stats.get(inference).containsKey(depth)) {
					stats.get(inference).put(depth, new Pair<Double, Double>(0.0, 0.0));
				}

				Pair<SingleDTGraph, List<Double>> p = cache.get(seed).get(inference).get(depth);
				GraphList<DTGraph<String, String>> graphs = RDFUtils.getSubGraphs(p.getFirst().getGraph(), p.getFirst()
						.getInstances(), depth);

				double v = 0;
				double e = 0;
				for (DTGraph<String, String> graph : graphs.getGraphs()) {
					v += graph.nodes().size();
					e += graph.links().size();
				}
				v /= graphs.numInstances();
				e /= graphs.numInstances();

				v += stats.get(inference).get(depth).getFirst();
				e += stats.get(inference).get(depth).getSecond();

				stats.get(inference).put(depth, new Pair<Double, Double>(v, e));
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
			LargeClassificationDataSet data, long[] seeds, int[] depths, boolean inference) {
		Map<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>> cache = new HashMap<Long, Map<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>>();

		for (long seed : seeds) {
			cache.put(seed, new HashMap<Boolean, Map<Integer, Pair<SingleDTGraph, List<Double>>>>());
			data.createSubSet(seed, 0, 50, 0);
			cache.get(seed).put(inference, new HashMap<Integer, Pair<SingleDTGraph, List<Double>>>());

			for (int depth : depths) {
				System.out.println("Getting Statements...");
				Set<Statement> stmts = RDFUtils.getStatements4Depth(tripleStore, data.getRDFData().getInstances(),
						depth, inference);
				System.out.println("# Statements: " + stmts.size());
				stmts.removeAll(new HashSet<Statement>(data.getRDFData().getBlackList()));
				System.out.println("# Statements: " + stmts.size() + ", after blackList");
				System.out.println("Building Graph...");
				SingleDTGraph graph = RDFUtils.statements2Graph(stmts, RDFUtils.REGULAR_LITERALS, data.getRDFData()
						.getInstances(), true);
				System.out.println("Built Graph with " + graph.getGraph().nodes().size() + ", and "
						+ graph.getGraph().links().size() + " links");

				cache.get(seed)
						.get(inference)
						.put(depth,
								new Pair<SingleDTGraph, List<Double>>(graph, new ArrayList<Double>(data.getTarget())));
			}
		}
		return cache;
	}
}
