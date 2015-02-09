package org.data2semantics.mustard.experiments.playground;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.AMDataSet;
import org.data2semantics.mustard.experiments.data.BGSDataSet;
import org.data2semantics.mustard.experiments.data.BGSLithoDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.kernels.data.GraphList;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.data2semantics.mustard.utils.Pair;
import org.nodes.DTGraph;
import org.nodes.DTNode;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class GraphStatisticsExperiment {
	private static String AIFB_FILE = "datasets/aifb-fixed_complete.n3";
	private static String BGS_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";
	private static String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";
	private static String ISWC_FOLDER = "datasets/";



	/**
	 * @param args
	 */
	public static void main(String[] args) {

		RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		ClassificationDataSet ds = new AIFBDataSet(tripleStore);
	
		//RDFDataSet tripleStore = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
		//ClassificationDataSet ds = new BGSLithoDataSet(tripleStore);
		
		//RDFDataSet tripleStore = new RDFFileDataSet(AM_FOLDER, RDFFormat.TURTLE);
		//LargeClassificationDataSet ds = new AMDataSet(tripleStore, 10, 0.005, 5, 4, true);

		//RDFDataSet tripleStore = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
		//LargeClassificationDataSet ds = new BGSDataSet(tripleStore, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme", 10, 0.05, 5, 3);

		ds.create();



		boolean[] inference = {false, true};
		int[] depths = {1,2,3};


		for (boolean inf : inference) {
			for (int depth : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), depth, inf);
				st.removeAll(ds.getRDFData().getBlackList());
				SingleDTGraph graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_SPLIT_LITERALS, ds.getRDFData().getInstances(), true);


				GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph.getGraph(), graph.getInstances(), depth);
				System.out.println("Graph, inf: " + inf + ", depth: " + depth + ", " + computeGraphStatistics(graphs));

				graphs = RDFUtils.getSubTrees(graph.getGraph(), graph.getInstances(), depth);
				System.out.println("Tree,  inf: " + inf + ", depth: " + depth + ", " + computeGraphStatistics(graphs));
			}

		}


	}

	private static String computeGraphStatistics(GraphList<DTGraph<String,String>> graphs) {
		double v = 0;
		double e = 0;
		double inDeg = 0;
		double outDeg = 0;
		for (DTGraph<String,String> g : graphs.getGraphs()) {
			v += g.nodes().size();
			e += g.links().size();		
			for (DTNode<String,String> ver : g.nodes()) {
				if (ver.inDegree() > 1) {
					inDeg += 1;
				}
				if (ver.outDegree() < 1) {
					outDeg += 1;
				}
			}
		}


		inDeg /= graphs.numInstances();
		outDeg /= graphs.numInstances();

		v /= graphs.numInstances();
		e /= graphs.numInstances();

		return "average #vertices: " + v + ", average #edges: " + e + ", average #vertices inDegree > 1: " + inDeg + "(" + (inDeg/v) + "), average #vertices outDegree < 1: " + outDeg + "(" + (outDeg/v) + ")";
	}


}
