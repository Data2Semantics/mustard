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
import org.data2semantics.mustard.experiments.utils.SimpleGraphKernelExperiment;
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

public class SimpleGraphFeaturesExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";
	private static String BGS_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	private static String ISWC_FOLDER = "datasets/";

	private static List<Resource> instances;
	private static List<Value> labels;
	private static List<Statement> blackList;
	private static List<Double> target;
	private static RDFDataSet dataset;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//createAffiliationPredictionDataSet();
		//createGeoDataSet(1, 1, 10, "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis");
		createCommitteeMemberPredictionDataSet();
		
		
		List<EvaluationFunction> evalFuncs = new ArrayList<EvaluationFunction>();
		evalFuncs.add(new Accuracy());
		evalFuncs.add(new F1());

		ResultsTable resTable = new ResultsTable();
		resTable.setDigits(3);

		long[] seeds = {11,21,31,41,51,61,71,81,91,101};
		double[] cs = {1, 10, 100, 1000, 10000, 100000};	

		LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
		svmParms.setNumFolds(10);

		/*
		svmParms.setWeightLabels(EvaluationUtils.computeWeightLabels(target));
		svmParms.setWeights(EvaluationUtils.computeWeights(target));
		//*/


		boolean reverseWL = true; // WL should be in reverse mode, which means regular subtrees
		boolean[] inference = {false,true};

		int[] depths = {3};
		int[] pathDepths = {0,1,2,3,4,5,6};
		int[] iterationsWL = {0,1,2,3,4,5,6};


		RDFData data = new RDFData(dataset, instances, blackList);

		///* The baseline experiment, BoW (or BoL if you prefer)
		for (boolean inf : inference) {
			resTable.newRow("Baseline BoL: " + inf);		 
			for (int d : depths) {

				List<RDFWLSubTreeKernel> kernelsBaseline = new ArrayList<RDFWLSubTreeKernel>();	
				kernelsBaseline.add(new RDFWLSubTreeKernel(0, d, inf, reverseWL, false, true));

				//Collections.shuffle(target);
				SimpleGraphKernelExperiment<RDFData> exp = new SimpleGraphKernelExperiment<RDFData>(kernelsBaseline, data, target, svmParms, seeds, evalFuncs);

				exp.run();

				for (Result res : exp.getResults()) {
					resTable.addResult(res);
				}
			}
		}
		//*/

		///* Path Count Root
		for (boolean inf : inference) {
			resTable.newRow("Path Count through root: " + inf);		 
			for (int d : depths) {

				List<RDFRootPathCountKernel> kernels = new ArrayList<RDFRootPathCountKernel>();	

				for (int dd : pathDepths) {
					kernels.add(new RDFRootPathCountKernel(dd, d, true, inf, true));
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

		///* WL Root
		for (boolean inf : inference) {
			resTable.newRow("WL through root: " + inf);		 
			for (int d : depths) {

				List<RDFWLRootSubTreeKernel> kernels = new ArrayList<RDFWLRootSubTreeKernel>();	

				for (int dd : iterationsWL) {
					kernels.add(new RDFWLRootSubTreeKernel(dd, d, inf, reverseWL, false, true));
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
			resTable.newRow("Path Count Tree: " + inf);		 
			for (int d : depths) {

				List<RDFTreePathCountKernel> kernels = new ArrayList<RDFTreePathCountKernel>();	

				for (int dd : pathDepths) {
					kernels.add(new RDFTreePathCountKernel(dd, d, inf, true));
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

				List<RDFTreeWLSubTreeKernel> kernels = new ArrayList<RDFTreeWLSubTreeKernel>();	

				for (int dd : iterationsWL) {
					kernels.add(new RDFTreeWLSubTreeKernel(dd, d, inf, reverseWL, false, true));
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

	
		
		///* RDF Path Count 
		for (boolean inf : inference) {
			resTable.newRow("RDF Path Count: " + inf);		 
			for (int d : depths) {

				List<RDFPathCountKernel> kernels = new ArrayList<RDFPathCountKernel>();	

				for (int dd : pathDepths) {
					kernels.add(new RDFPathCountKernel(dd, d, inf, true));
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

		///* RDF WL 
		for (boolean inf : inference) {
			resTable.newRow("RDF WL: " + inf);		 
			for (int d : depths) {

				List<RDFWLSubTreeKernel> kernels = new ArrayList<RDFWLSubTreeKernel>();	

				for (int dd : iterationsWL) {
					kernels.add(new RDFWLSubTreeKernel(dd, d, inf, reverseWL, false, true));
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
		
		
		///* Regular WL
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
				SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

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
		
		///* Path Count full
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
				SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>> exp2 = new SimpleGraphKernelExperiment<GraphList<DTGraph<String,String>>>(kernels, new GraphList<DTGraph<String,String>>(graphs), target, svmParms, seeds, evalFuncs);

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
	private static void createAffiliationPredictionDataSet() {
		dataset = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}

		EvaluationUtils.removeSmallClasses(instances, labels, 5);
		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);

		target = EvaluationUtils.createTarget(labels);
	}
	
	
	private static void createGeoDataSet(long seed, double fraction, int minSize, String property) {
		dataset = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
		
		String majorityClass = "http://data.bgs.ac.uk/id/Lexicon/Class/LS";
		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		System.out.println(dataset.getLabel());

		System.out.println("Component Rock statements: " + stmts.size());
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		// http://data.bgs.ac.uk/ref/Lexicon/hasRockUnitRank
		// http://data.bgs.ac.uk/ref/Lexicon/hasTheme

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = dataset.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

			for (Statement stmt2 : stmts2) {

				if (rand.nextDouble() <= fraction) {
					instances.add(stmt2.getSubject());

					labels.add(stmt2.getObject());
					/*
				if (stmt2.getObject().toString().equals(majorityClass)) {
					labels.add(ds.createLiteral("pos"));
				} else {
					labels.add(ds.createLiteral("neg"));
				}
					 */
				}
			}
		}


		//capClassSize(50, seed);
		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);
		target = EvaluationUtils.createTarget(labels);
	}

	
	private static void createCommitteeMemberPredictionDataSet() {
		RDFFileDataSet testSetA = new RDFFileDataSet(ISWC_FOLDER + "iswc-2011-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2011-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2012-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2008-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/eswc-2009-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2012-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2011-complete.rdf", RDFFormat.RDFXML);
		testSetA.addFile(ISWC_FOLDER + "iswc-2010-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2009-complete.rdf", RDFFormat.RDFXML);
		//testSetA.addFile("datasets/iswc-2008-complete.rdf", RDFFormat.RDFXML);

		RDFFileDataSet testSetB = new RDFFileDataSet(ISWC_FOLDER + "iswc-2012-complete.rdf", RDFFormat.RDFXML);

		instances = new ArrayList<Resource>();
		List<Resource> instancesB = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		List<Statement> stmts = testSetA.getStatementsFromStrings(null, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type", "http://xmlns.com/foaf/0.1/Person");
		for (Statement stmt : stmts) {
			instancesB.add(stmt.getSubject()); 
		}	

		int pos = 0, neg = 0;
		for (Resource instance : instancesB) {
			if (!testSetB.getStatements(instance, null, null).isEmpty()) {
				instances.add(instance);
				if (testSetB.getStatementsFromStrings(instance.toString(), "http://data.semanticweb.org/ns/swc/ontology#holdsRole", "http://data.semanticweb.org/conference/iswc/2012/pc-member/research", false).size() > 0) {
					labels.add(testSetA.createLiteral("true"));
					pos++;
				} else {
					labels.add(testSetA.createLiteral("false"));
					neg++;
				}
			}
		}

		dataset = testSetA;		
		blackList = new ArrayList<Statement>();
		target = EvaluationUtils.createTarget(labels);

		System.out.println("Pos and Neg: " + pos + " " + neg);
		System.out.println("Baseline acc: " + Math.max(pos, neg) / ((double)pos+neg));
	}
}
