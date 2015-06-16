package org.data2semantics.mustard.experiments.playground;

import java.util.ArrayList;
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
import org.data2semantics.mustard.experiments.data.MutagDataSet;
import org.data2semantics.mustard.experiments.data.SubsetDataSet;
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
	private static String BGS_FOLDER =  "C:\\Users\\Gerben\\onedrive\\d2s\\data_bgs_ac_uk_ALL";



	/**
	 * @param args
	 */
	public static void main(String[] args) {

		//RDFDataSet tripleStore = new RDFFileDataSet(AIFB_FILE, RDFFormat.N3);
		//ClassificationDataSet ds = new AIFBDataSet(tripleStore);

		//RDFDataSet tripleStore = new RDFFileDataSet(BGS_FOLDER, RDFFormat.NTRIPLES);
		//ClassificationDataSet ds = new BGSLithoDataSet(tripleStore);
		
		RDFDataSet tripleStore = new RDFFileDataSet("datasets/carcinogenesis.owl", RDFFormat.forFileName("datasets/carcinogenesis.owl"));
		ClassificationDataSet ds = new MutagDataSet(tripleStore);

		ds.create();



		boolean[] inference = {false, true};
		int[] depths = {1};


		
		for (boolean inf : inference) {
			for (int depth : depths) {

				Set<Statement> st = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), depth, inf);
				st.removeAll(ds.getRDFData().getBlackList());
				SingleDTGraph graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS, ds.getRDFData().getInstances(), true);


				GraphList<DTGraph<String,String>> graphs = RDFUtils.getSubGraphs(graph.getGraph(), graph.getInstances(), depth);
				System.out.println("Graph, inf: " + inf + ", depth: " + depth + ", " + computeGraphStatistics(graphs));

				graphs = RDFUtils.getSubTrees(graph.getGraph(), graph.getInstances(), depth);
				System.out.println("Tree,  inf: " + inf + ", depth: " + depth + ", " + computeGraphStatistics(graphs));
			}

		}
		
		/*
		int[] depths = {1};

		String[] subsets = {"datasets/AMsubset1false", "datasets/AMsubset2false", "datasets/AMsubset3false", "datasets/AMsubset4false", 
				"datasets/AMsubset5false", "datasets/AMsubset6false", "datasets/AMsubset7false",
				"datasets/AMsubset8false", "datasets/AMsubset9false", "datasets/AMsubset10false"};
		
		*/
		
		/*
		String[] subsets = {"datasets/AMsubset1true", "datasets/AMsubset2true", "datasets/AMsubset3true", "datasets/AMsubset4true", 
				"datasets/AMsubset5true", "datasets/AMsubset6true", "datasets/AMsubset7true",
				"datasets/AMsubset8true", "datasets/AMsubset9true", "datasets/AMsubset10true"};
		
		String[] subsets = {"datasets/BGSsubset1false", "datasets/BGSsubset2false", "datasets/BGSsubset3false", "datasets/BGSsubset4false", 
				"datasets/BGSsubset5false", "datasets/BGSsubset6false", "datasets/BGSsubset7false",
				"datasets/BGSsubset8false", "datasets/BGSsubset9false", "datasets/BGSsubset10false"};
		
		String[] subsets = {"datasets/BGSsubset1true", "datasets/BGSsubset2true", "datasets/BGSsubset3true", "datasets/BGSsubset4true", 
				"datasets/BGSsubset5true", "datasets/BGSsubset6true", "datasets/BGSsubset7true",
				"datasets/BGSsubset8true", "datasets/BGSsubset9true", "datasets/BGSsubset10true"};
				
				//*/
		
	
		/*
		for (int depth : depths) {
			GraphList<DTGraph<String,String>> graphs = new GraphList<DTGraph<String,String>>(new ArrayList<DTGraph<String,String>>());
			GraphList<DTGraph<String,String>> trees = new GraphList<DTGraph<String,String>>(new ArrayList<DTGraph<String,String>>());
			
			for (String subset : subsets) {
				ClassificationDataSet ds = new SubsetDataSet(subset);
				ds.create();
				Set<Statement> st = RDFUtils.getStatements4Depth(ds.getRDFData().getDataset(), ds.getRDFData().getInstances(), depth, false);
				st.removeAll(ds.getRDFData().getBlackList());
				SingleDTGraph graph = RDFUtils.statements2Graph(st, RDFUtils.REGULAR_LITERALS, ds.getRDFData().getInstances(), true);

				graphs.getGraphs().addAll(RDFUtils.getSubGraphs(graph.getGraph(), graph.getInstances(), depth).getGraphs());
				trees.getGraphs().addAll(RDFUtils.getSubTrees(graph.getGraph(), graph.getInstances(), depth).getGraphs());
			}
			
			System.out.println("Graph, depth: " + depth + ", " + computeGraphStatistics(graphs));		
			System.out.println("Tree, depth: " + depth + ", " + computeGraphStatistics(trees));
		}
		*/
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
