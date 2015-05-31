package org.data2semantics.mustard.experiments.cluster;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.BGSLithoDataSet;
import org.data2semantics.mustard.experiments.data.MutagDataSet;
import org.data2semantics.mustard.experiments.data.SubsetDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWLSubTreeApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWalkCountApproxKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphHubRemovalWrapperFeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphHubRemovalWrapperKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphIntersectionPartialSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphIntersectionSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWalkCountIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountIDEQApproxKernelMkII;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWalkCountIDEQApproxKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWalkCountKernel;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdfvault.DTGraphGraphListURIPrefixKernel;
import org.openrdf.rio.RDFFormat;


/**
 * Parser for String[] args, for cluster experiments. It has grown into quite an ugly beast!
 * 
 * TODO For the future, what kernel to create should be done via reflection, instead of using String arguments.
 * 
 * @author Gerben
 *
 */
public class StringArgumentsParser {
	private String dataFile;
	private String dataset;
	private String kernel;
	private String[] kernelParms;
	private int depth;
	private int subset;
	private boolean inference;
	private int minHubCount;
	private int[] minHubCounts;
	private boolean optHubs;
	private boolean blankLabels;
	private boolean splitLiterals;
	private boolean leaveRootLabel;


	public StringArgumentsParser(String[] args) {
		kernelParms = new String[9];
		subset = 0;
		minHubCount = 0;
		optHubs = true;
		blankLabels = false;
		splitLiterals = false;
		leaveRootLabel = false;

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				dataFile = args[++i];
			}
			if (args[i].equals("-dataset")) {
				dataset = args[++i];
			}
			if (args[i].equals("-kernel")) {
				kernel = args[++i];
			}
			if (args[i].startsWith("-kernelParm")) {
				int index = Integer.parseInt(args[i].substring(11, 12)); // this is the parm index (1-9)
				kernelParms[index-1] = args[++i];
			}
			if (args[i].equals("-depth")) {
				depth = Integer.parseInt(args[++i]);
			}
			if (args[i].equals("-subset")) {
				subset = Integer.parseInt(args[++i]);
			}
			if (args[i].equals("-inference")) {
				inference = Boolean.parseBoolean(args[++i]);
			}
			if (args[i].equals("-minHubs")) {
				String t = args[++i];

				if (t.startsWith("[")) {
					minHubCounts = parseIntArray(t);
				} else {
					minHubCount = Integer.parseInt(t); 
				}
			}
			if (args[i].equals("-optHubs")) {
				optHubs = Boolean.parseBoolean(args[++i]);
			}
			if (args[i].equals("-blankLabels")) {
				blankLabels = Boolean.parseBoolean(args[++i]);
			}
			if (args[i].equals("-splitLiterals")) {
				splitLiterals = Boolean.parseBoolean(args[++i]);
			}
			if (args[i].equals("-leaveRootLabel")) {
				leaveRootLabel = Boolean.parseBoolean(args[++i]);
			}

		}
	}

	public List<? extends GraphKernel<SingleDTGraph>> graphKernel() {
		if (minHubCount == 0 && minHubCounts == null) {
			return graphKernelProxy();
		} else {
			List<? extends GraphKernel<SingleDTGraph>> kernels = graphKernelProxy();
			List<DTGraphHubRemovalWrapperKernel<GraphKernel<SingleDTGraph>>> kernels2 = new ArrayList<DTGraphHubRemovalWrapperKernel<GraphKernel<SingleDTGraph>>>(kernels.size());

			for (GraphKernel<SingleDTGraph> kernel : kernels) {
				if (minHubCounts != null) {
					if (optHubs) {
						for (int mhc : minHubCounts) {
							kernels2.add(new DTGraphHubRemovalWrapperKernel<GraphKernel<SingleDTGraph>>(kernel, mhc, true));
						}
					} else {
						kernels2.add(new DTGraphHubRemovalWrapperKernel<GraphKernel<SingleDTGraph>>(kernel, minHubCounts, true));
					}
				} else {
					kernels2.add(new DTGraphHubRemovalWrapperKernel<GraphKernel<SingleDTGraph>>(kernel, minHubCount, true));
				}
			}
			return kernels2;
		}
	}

	public List<? extends GraphKernel<SingleDTGraph>> graphKernelProxy() {
		if (kernel.equals("GraphBoL")) {
			return graphBagOfLabels(kernelParms);
		}
		if (kernel.equals("TreeBoL")) {
			return treeBagOfLabels(kernelParms);
		}
		if (kernel.equals("TreeWalks")) {
			return treeWalks(kernelParms);
		}
		if (kernel.equals("TreeSubtrees")) {
			return treeSubtrees(kernelParms);
		}
		if (kernel.equals("TreeWalksApprox")) {
			return treeWalksApprox(kernelParms);
		}
		if (kernel.equals("TreeSubtreesApprox")) {
			return treeSubtreesApprox(kernelParms);
		}
		if (kernel.equals("TreeWalksRoot")) {
			return treeWalksRoot(kernelParms);
		}
		if (kernel.equals("TreeSubtreesRoot")) {
			return treeSubtreesRoot(kernelParms);
		}
		if (kernel.equals("GraphWalks")) {
			return graphWalks(kernelParms);
		}
		if (kernel.equals("GraphWalksFast")) {
			return graphWalksFast(kernelParms);
		}
		if (kernel.equals("GraphSubtrees")) {
			return graphSubtrees(kernelParms);
		}
		if (kernel.equals("GraphSubtreesFast")) {
			return graphSubtreesFast(kernelParms);
		}
		if (kernel.equals("GraphWalksApprox")) {
			return graphWalksApprox(kernelParms);
		}
		if (kernel.equals("GraphWalksFastApprox")) {
			return graphWalksFastApprox(kernelParms);
		}
		if (kernel.equals("GraphSubtreesApprox")) {
			return graphSubtreesApprox(kernelParms);
		}
		if (kernel.equals("GraphSubtreesFastApprox")) {
			return graphSubtreesFastApprox(kernelParms);
		}
		if (kernel.equals("TreeWalksRootApprox")) {
			return treeWalksRootApprox(kernelParms);
		}
		if (kernel.equals("TreeSubtreesRootApprox")) {
			return treeSubtreesRootApprox(kernelParms);
		}
		if (kernel.equals("IntersectionSubTree")) {
			return intersectionSubTree(kernelParms);
		}
		if (kernel.equals("IntersectionPartialSubTree")) {
			return intersectionPartialSubTree(kernelParms);
		}
		if (kernel.equals("URIPrefix")) {
			return graphURIPrefix(kernelParms);
		}
		return null;
	}

	public List<? extends FeatureVectorKernel<SingleDTGraph>> graphFeatureVectorKernel() {
		if (minHubCount == 0 && minHubCounts == null) {
			return graphFeatureVectorKernelProxy();
		} else {
			List<? extends FeatureVectorKernel<SingleDTGraph>> kernels = graphFeatureVectorKernelProxy();
			List<DTGraphHubRemovalWrapperFeatureVectorKernel<FeatureVectorKernel<SingleDTGraph>>> kernels2 = new ArrayList<DTGraphHubRemovalWrapperFeatureVectorKernel<FeatureVectorKernel<SingleDTGraph>>>(kernels.size());

			for (FeatureVectorKernel<SingleDTGraph> kernel : kernels) {
				if (minHubCounts != null) {
					if (optHubs) {
						for (int mhc : minHubCounts) {
							kernels2.add(new DTGraphHubRemovalWrapperFeatureVectorKernel<FeatureVectorKernel<SingleDTGraph>>(kernel, mhc, true));
						}
					} else {
						kernels2.add(new DTGraphHubRemovalWrapperFeatureVectorKernel<FeatureVectorKernel<SingleDTGraph>>(kernel, minHubCounts, true));
					}
				} else {
					kernels2.add(new DTGraphHubRemovalWrapperFeatureVectorKernel<FeatureVectorKernel<SingleDTGraph>>(kernel, minHubCount, true));
				}
			}
			return kernels2;
		}
	}

	public List<? extends FeatureVectorKernel<SingleDTGraph>> graphFeatureVectorKernelProxy() {
		if (kernel.equals("GraphBoL")) {
			return graphBagOfLabels(kernelParms);
		}
		if (kernel.equals("TreeBoL")) {
			return treeBagOfLabels(kernelParms);
		}
		if (kernel.equals("TreeWalks")) {
			return treeWalks(kernelParms);
		}
		if (kernel.equals("TreeSubtrees")) {
			return treeSubtrees(kernelParms);
		}
		if (kernel.equals("TreeWalksApprox")) {
			return treeWalksApprox(kernelParms);
		}
		if (kernel.equals("TreeSubtreesApprox")) {
			return treeSubtreesApprox(kernelParms);
		}
		if (kernel.equals("TreeWalksRoot")) {
			return treeWalksRoot(kernelParms);
		}
		if (kernel.equals("TreeSubtreesRoot")) {
			return treeSubtreesRoot(kernelParms);
		}
		if (kernel.equals("GraphWalks")) {
			return graphWalks(kernelParms);
		}
		if (kernel.equals("GraphWalksFast")) {
			return graphWalksFast(kernelParms);
		}
		if (kernel.equals("GraphSubtrees")) {
			return graphSubtrees(kernelParms);
		}
		if (kernel.equals("GraphSubtreesFast")) {
			return graphSubtreesFast(kernelParms);
		}
		if (kernel.equals("GraphWalksApprox")) {
			return graphWalksApprox(kernelParms);
		}
		if (kernel.equals("GraphWalksFastApprox")) {
			return graphWalksFastApprox(kernelParms);
		}
		if (kernel.equals("GraphSubtreesApprox")) {
			return graphSubtreesApprox(kernelParms);
		}
		if (kernel.equals("GraphSubtreesFastApprox")) {
			return graphSubtreesFastApprox(kernelParms);
		}
		if (kernel.equals("TreeWalksRootApprox")) {
			return treeWalksRootApprox(kernelParms);
		}
		if (kernel.equals("TreeSubtreesRootApprox")) {
			return treeSubtreesRootApprox(kernelParms);
		}
		if (kernel.equals("URIPrefix")) {
			return graphURIPrefix(kernelParms);
		}
		return null;
	}


	public ClassificationDataSet createDataSet() {
		ClassificationDataSet ds = null;
		if (dataset.equals("AIFB")) {
			RDFDataSet tripleStore = new RDFFileDataSet(getDataFile(), RDFFormat.forFileName(getDataFile()));
			ds = new AIFBDataSet(tripleStore);
		}
		if (dataset.equals("LITHO")) {
			RDFDataSet tripleStore = new RDFFileDataSet(getDataFile(), RDFFormat.NTRIPLES);
			ds = new BGSLithoDataSet(tripleStore);
		}
		if (dataset.equals("AM") || dataset.equals("BGS")) {
			ds = new SubsetDataSet(getDataFile());
		}

		if (dataset.equals("MUTAG")) {
			RDFDataSet tripleStore = new RDFFileDataSet(getDataFile(), RDFFormat.forFileName(getDataFile()));
			ds = new MutagDataSet(tripleStore);
		}

		if (ds != null) {
			ds.create();
		}
		return ds;
	}



	public String getSaveFileString() {
		StringBuilder sb = new StringBuilder();

		sb.append(kernel);

		for (String p : kernelParms) {
			if (p != null) {
				sb.append("_");
				sb.append(p);
			}
		}
		sb.append("_minHubCount");
		if (minHubCounts == null) {		
			sb.append(minHubCount);
		} else {
			sb.append(Arrays.toString(minHubCounts));
		}		
		sb.append("_optHubs");
		sb.append(optHubs);
		sb.append("_depth");
		sb.append(depth);
		sb.append("_inference");
		sb.append(inference);
		sb.append("_blankLabels");
		sb.append(blankLabels);
		sb.append("_splitLiterals");
		sb.append(splitLiterals);
		sb.append("_leaveRootLabel");
		sb.append(leaveRootLabel);


		return sb.toString();
	}


	public int getDepth() {
		return depth;
	}

	public int getSubset() {
		return subset;
	}

	public boolean isInference() {
		return inference;
	}

	public String getDataFile() {
		return dataFile;
	}

	public boolean isBlankLabels() {
		return blankLabels;
	}

	public boolean isSplitLiterals() {
		return splitLiterals;
	}


	public boolean isLeaveRootLabel() {
		return leaveRootLabel;
	}

	/**
	 * -kernel URIPrefix
	 * -kernelParm1 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphGraphListURIPrefixKernel> graphURIPrefix(String[] parms) {
		List<DTGraphGraphListURIPrefixKernel> kernels = new ArrayList<DTGraphGraphListURIPrefixKernel>();
		int[] depths = new int[1];
		if (parms[0].startsWith("[")) {
			depths = parseIntArray(parms[0]);
		} else {
			depths[0] = Integer.parseInt(parms[0]); 
		}

		for (int depth : depths) {
			kernels.add(new DTGraphGraphListURIPrefixKernel(1.0, depth, true));
		}
		return kernels;
	}


	/**
	 * -kernel GraphBoL
	 * -kernelParm1 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphWLSubTreeKernel> graphBagOfLabels(String[] parms) {
		List<DTGraphWLSubTreeKernel> kernels = new ArrayList<DTGraphWLSubTreeKernel>();
		int[] depths = new int[1];
		if (parms[0].startsWith("[")) {
			depths = parseIntArray(parms[0]);
		} else {
			depths[0] = Integer.parseInt(parms[0]); 
		}

		for (int depth : depths) {
			kernels.add(new DTGraphWLSubTreeKernel(0, depth, false, false, true));
		}
		return kernels;
	}

	/**
	 * -kernel TreeBoL
	 * -kernelParm1 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphTreeWLSubTreeKernel> treeBagOfLabels(String[] parms) {
		List<DTGraphTreeWLSubTreeKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeKernel>();
		int[] depths = new int[1];
		if (parms[0].startsWith("[")) {
			depths = parseIntArray(parms[0]);
		} else {
			depths[0] = Integer.parseInt(parms[0]); 
		}

		for (int depth : depths) {
			kernels.add( new DTGraphTreeWLSubTreeKernel(0, depth, false, false, true));
		}
		return kernels; 
	}

	/**
	 * -kernel TreeWalks
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphTreeWalkCountKernel> treeWalks(String[] parms) {
		List<DTGraphTreeWalkCountKernel> kernels = new ArrayList<DTGraphTreeWalkCountKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}
		for (int depth : depths) {			
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				kernels.add(new DTGraphTreeWalkCountKernel(p, depth, true));
			}
		}
		return kernels;
	}

	/**
	 * -kernel TreeWalksRoot
	 * -kernelParm1 maxWalkLength (int)
	 * 
	 * @return
	 */
	public static List<DTGraphRootWalkCountKernel> treeWalksRoot(String[] parms) {
		List<DTGraphRootWalkCountKernel> kernels = new ArrayList<DTGraphRootWalkCountKernel>();
		int[] pathLengths = new int[1];

		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}

		for (int p : pathLengths) {
			kernels.add(new DTGraphRootWalkCountKernel(p, true));
		}
		return kernels;
	}

	/**
	 * -kernel GraphWalks
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphGraphListWalkCountKernel> graphWalks(String[] parms) {
		List<DTGraphGraphListWalkCountKernel> kernels = new ArrayList<DTGraphGraphListWalkCountKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				kernels.add(new DTGraphGraphListWalkCountKernel(p, depth, true));

			}
		}
		return kernels;
	}

	/**
	 * -kernel GraphWalksFast
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphWalkCountKernel> graphWalksFast(String[] parms) {
		List<DTGraphWalkCountKernel> kernels = new ArrayList<DTGraphWalkCountKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				kernels.add(new DTGraphWalkCountKernel(p, depth, true));

			}
		}
		return kernels;
	}


	/**
	 * -kernel TreeSubtrees
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 trackPrevNBH (boolean)
	 * -kernelParm4 reverse (boolean)
	 * 
	 * @return
	 */
	public static List<DTGraphTreeWLSubTreeKernel> treeSubtrees(String[] parms) {
		List<DTGraphTreeWLSubTreeKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		boolean trackPrev = parms[2] == null ? false : Boolean.parseBoolean(parms[2]);
		boolean reverse = parms[3] == null ? true : Boolean.parseBoolean(parms[3]);

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				kernels.add(new DTGraphTreeWLSubTreeKernel(p, depth, reverse, false, trackPrev, true));
			}
		}
		return kernels;
	}

	/**
	 * -kernel TreeSubtreesRoot
	 * -kernelParm1 numIterations (int)
	 * 
	 * @return
	 */
	public static List<DTGraphRootWLSubTreeKernel> treeSubtreesRoot(String[] parms) {
		List<DTGraphRootWLSubTreeKernel> kernels = new ArrayList<DTGraphRootWLSubTreeKernel>();
		int[] pathLengths = new int[1];

		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		for (int p : pathLengths) {
			kernels.add(new DTGraphRootWLSubTreeKernel(p, false, true));

		}
		return kernels;
	}

	/**
	 * -kernel GraphSubtrees
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 trackPrevNBH (boolean)
	 * -kernelParm4 reverse (boolean)
	 * 
	 * @return
	 */
	public static List<DTGraphGraphListWLSubTreeKernel> graphSubtrees(String[] parms) {
		List<DTGraphGraphListWLSubTreeKernel> kernels = new ArrayList<DTGraphGraphListWLSubTreeKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		boolean trackPrev = parms[2] == null ? false : Boolean.parseBoolean(parms[2]);
		boolean reverse = parms[3] == null ? true : Boolean.parseBoolean(parms[3]);

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				kernels.add(new DTGraphGraphListWLSubTreeKernel(p, depth, reverse, trackPrev, true));
			}
		}
		return kernels;
	}

	/**
	 * -kernel GraphSubtreesFast
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 trackPrevNBH (boolean)
	 * -kernelParm4 reverse (boolean)
	 * 
	 * @return
	 */
	public static List<DTGraphWLSubTreeKernel> graphSubtreesFast(String[] parms) {
		List<DTGraphWLSubTreeKernel> kernels = new ArrayList<DTGraphWLSubTreeKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		boolean trackPrev = parms[2] == null ? false : Boolean.parseBoolean(parms[2]);
		boolean reverse = parms[3] == null ? true : Boolean.parseBoolean(parms[3]);

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				kernels.add(new DTGraphWLSubTreeKernel(p, depth, reverse, false, trackPrev, true));
			}
		}
		return kernels;
	}


	/**
	 * -kernel TreeSubtreesApprox
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 minFreq (int)
	 * -kernelParm4 maxCard (int)
	 * 
	 * @return
	 */
	public static List<DTGraphTreeWLSubTreeIDEQApproxKernel> treeSubtreesApprox(String[] parms) {
		List<DTGraphTreeWLSubTreeIDEQApproxKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeIDEQApproxKernel>();

		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		int[] minFreqs = new int[1];
		if (parms[2].startsWith("[")) {
			minFreqs = parseIntArray(parms[2]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[2]);
		}

		int[] maxCards = new int[1];
		if (parms[3].startsWith("[")) {
			maxCards = parseIntArray(parms[3]);
		} else {
			maxCards[0] = Integer.parseInt(parms[3]);
		}

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				for (int minFreq : minFreqs) {
					for (int maxCard : maxCards) {
						int[] mpn = {p};
						int[] mc = {maxCard};
						int[] mf = {minFreq};
						// new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true)
						kernels.add(new DTGraphTreeWLSubTreeIDEQApproxKernel(p, depth, true, false, true, false, 1.0, 1.0, mpn, mc, mf, true));
					}
				}
			}
		}
		return kernels;
	}

	/**
	 * -kernel TreeSubtreesRootApprox
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 minFreq (int)
	 * -kernelParm3 maxCard (int)
	 * 
	 * @return
	 */
	public static List<DTGraphRootWLSubTreeIDEQApproxKernel> treeSubtreesRootApprox(String[] parms) {
		List<DTGraphRootWLSubTreeIDEQApproxKernel> kernels = new ArrayList<DTGraphRootWLSubTreeIDEQApproxKernel>();

		int[] pathLengths = new int[1];
		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}

		int[] minFreqs = new int[1];
		if (parms[1].startsWith("[")) {
			minFreqs = parseIntArray(parms[1]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[1]);
		}

		int[] maxCards = new int[1];
		if (parms[2].startsWith("[")) {
			maxCards = parseIntArray(parms[2]);
		} else {
			maxCards[0] = Integer.parseInt(parms[2]);
		}

		for (int p : pathLengths) {
			for (int minFreq : minFreqs) {
				for (int maxCard : maxCards) {
					int[] mpn = {p};
					int[] mc = {maxCard};
					int[] mf = {minFreq};
					// new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true)
					kernels.add(new DTGraphRootWLSubTreeIDEQApproxKernel(p, mpn, mc, mf, true));
				}
			}
		}

		return kernels;
	}

	/**
	 * -kernel GraphSubtreesApprox
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 minFreq (int)
	 * -kernelParm4 maxCard (int)
	 * 
	 * @return
	 */
	public static List<DTGraphGraphListWLSubTreeApproxKernel> graphSubtreesApprox(String[] parms) {
		List<DTGraphGraphListWLSubTreeApproxKernel> kernels = new ArrayList<DTGraphGraphListWLSubTreeApproxKernel>();

		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		int[] minFreqs = new int[1];
		if (parms[2].startsWith("[")) {
			minFreqs = parseIntArray(parms[2]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[2]);
		}

		int[] maxCards = new int[1];
		if (parms[3].startsWith("[")) {
			maxCards = parseIntArray(parms[3]);
		} else {
			maxCards[0] = Integer.parseInt(parms[3]);
		}

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				for (int minFreq : minFreqs) {
					for (int maxCard : maxCards) {
						int[] mpn = {p};
						int[] mc = {maxCard};
						int[] mf = {minFreq};
						// new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true)
						kernels.add(new DTGraphGraphListWLSubTreeApproxKernel(p, depth, true, true, 1.0, 1.0, mpn, mc, mf, true));
					}
				}
			}
		}
		return kernels;
	}


	/**
	 * -kernel GraphSubtreesFastApprox
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 minFreq (int)
	 * -kernelParm4 maxCard (int)
	 * 
	 * @return
	 */
	public static List<DTGraphWLSubTreeIDEQApproxKernel> graphSubtreesFastApprox(String[] parms) {
		List<DTGraphWLSubTreeIDEQApproxKernel> kernels = new ArrayList<DTGraphWLSubTreeIDEQApproxKernel>();

		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		int[] minFreqs = new int[1];
		if (parms[2].startsWith("[")) {
			minFreqs = parseIntArray(parms[2]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[2]);
		}

		int[] maxCards = new int[1];
		if (parms[3].startsWith("[")) {
			maxCards = parseIntArray(parms[3]);
		} else {
			maxCards[0] = Integer.parseInt(parms[3]);
		}

		for (int depth : depths) {
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				for (int minFreq : minFreqs) {
					for (int maxCard : maxCards) {
						int[] mpn = {p};
						int[] mc = {maxCard};
						int[] mf = {minFreq};
						// new RDFWLSubTreeIDEQApproxKernel(d*2, d, inf, reverseWL, false, true, false, depthWeight, depthDiffWeight, maxPrevNBH, maxCard, minFreq, true)
						kernels.add(new DTGraphWLSubTreeIDEQApproxKernel(p, depth, true, false, true, false, 1.0, 1.0, mpn, mc, mf, true));
					}
				}
			}
		}
		return kernels;
	}


	/**
	 * -kernel TreeWalksApprox
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 minFreq (int)
	 * 
	 * @return
	 */
	public static List<DTGraphTreeWalkCountIDEQApproxKernelMkII> treeWalksApprox(String[] parms) {
		List<DTGraphTreeWalkCountIDEQApproxKernelMkII> kernels = new ArrayList<DTGraphTreeWalkCountIDEQApproxKernelMkII>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		int[] minFreqs = new int[1];
		if (parms[2].startsWith("[")) {
			minFreqs = parseIntArray(parms[2]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[2]);
		}

		for (int depth : depths) {			
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				for (int minFreq : minFreqs) {
					kernels.add(new DTGraphTreeWalkCountIDEQApproxKernelMkII(p, depth, minFreq, true));
				}
			}
		}
		return kernels;
	}

	/**
	 * -kernel TreeWalksRootApprox
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 minFreq (int)
	 * 
	 * @return
	 */
	public static List<DTGraphRootWalkCountIDEQApproxKernel> treeWalksRootApprox(String[] parms) {
		List<DTGraphRootWalkCountIDEQApproxKernel> kernels = new ArrayList<DTGraphRootWalkCountIDEQApproxKernel>();
		int[] pathLengths = new int[1];
		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}

		int[] minFreqs = new int[1];
		if (parms[1].startsWith("[")) {
			minFreqs = parseIntArray(parms[1]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[1]);
		}

		for (int p : pathLengths) {
			for (int minFreq : minFreqs) {
				kernels.add(new DTGraphRootWalkCountIDEQApproxKernel(p, minFreq, true));
			}
		}
		return kernels;
	}


	/**
	 * -kernel GraphWalksFastApprox
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 minFreq (int)
	 * 
	 * @return
	 */
	public static List<DTGraphWalkCountIDEQApproxKernel> graphWalksFastApprox(String[] parms) {
		List<DTGraphWalkCountIDEQApproxKernel> kernels = new ArrayList<DTGraphWalkCountIDEQApproxKernel>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		int[] minFreqs = new int[1];
		if (parms[2].startsWith("[")) {
			minFreqs = parseIntArray(parms[2]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[2]);
		}

		for (int depth : depths) {			
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				for (int minFreq : minFreqs) {
					kernels.add(new DTGraphWalkCountIDEQApproxKernel(p, depth, minFreq, true));
				}
			}
		}
		return kernels;
	}


	/**
	 * -kernel GraphWalksApprox
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 minFreq (int)
	 * 
	 * @return
	 */
	public static List<DTGraphGraphListWalkCountApproxKernelMkII> graphWalksApprox(String[] parms) {
		List<DTGraphGraphListWalkCountApproxKernelMkII> kernels = new ArrayList<DTGraphGraphListWalkCountApproxKernelMkII>();
		int[] pathLengths = new int[1];
		boolean dTT = false;
		if (parms[0].equals("depthTimesTwo")) {
			dTT = true;
		} else if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		int[] minFreqs = new int[1];
		if (parms[2].startsWith("[")) {
			minFreqs = parseIntArray(parms[2]);
		} else {
			minFreqs[0] = Integer.parseInt(parms[2]);
		}

		for (int depth : depths) {			
			for (int p : pathLengths) {
				if (dTT) { // if depth times two, then pathLengths has one element
					p = depth * 2;
				}
				for (int minFreq : minFreqs) {
					kernels.add(new DTGraphGraphListWalkCountApproxKernelMkII(p, depth, minFreq, true));
				}
			}
		}
		return kernels;
	}



	/**
	 * -kernel IntersectionSubTree
	 * -kernelParm1 discountFactor (double)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphIntersectionSubTreeKernel> intersectionSubTree(String[] parms) {
		List<DTGraphIntersectionSubTreeKernel> kernels = new ArrayList<DTGraphIntersectionSubTreeKernel>();
		double[] df = new double[1];
		if (parms[0].startsWith("[")) {
			df = parseDoubleArray(parms[0]);
		} else {
			df[0] = Double.parseDouble(parms[0]); 
		}

		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		for (int depth : depths) {
			for (double d : df) {
				kernels.add(new DTGraphIntersectionSubTreeKernel(depth, d, true));

			}
		}
		return kernels;
	}

	/**
	 * -kernel IntersectionPartialSubTree
	 * -kernelParm1 discountFactor (double)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphIntersectionPartialSubTreeKernel> intersectionPartialSubTree(String[] parms) {
		List<DTGraphIntersectionPartialSubTreeKernel> kernels = new ArrayList<DTGraphIntersectionPartialSubTreeKernel>();
		double[] df = new double[1];
		if (parms[0].startsWith("[")) {
			df = parseDoubleArray(parms[0]);
		} else {
			df[0] = Double.parseDouble(parms[0]); 
		}
		int[] depths = new int[1];
		if (parms[1].startsWith("[")) {
			depths = parseIntArray(parms[1]);
		} else {
			depths[0] = Integer.parseInt(parms[1]); 
		}

		for (int depth : depths) {
			for (double d : df) {
				kernels.add(new DTGraphIntersectionPartialSubTreeKernel(depth, d, true));

			}
		}
		return kernels;
	}

	private static int[] parseIntArray(String input) {
		input = input.replace("[", "");
		input = input.replace("]", "");
		String[] vals = input.split(",");
		int[] ret = new int[vals.length];

		for (int i = 0; i < vals.length; i++) {
			ret[i] = Integer.parseInt(vals[i]);
		}
		return ret;
	}

	private static double[] parseDoubleArray(String input) {
		input = input.replace("[", "");
		input = input.replace("]", "");
		String[] vals = input.split(",");
		double[] ret = new double[vals.length];

		for (int i = 0; i < vals.length; i++) {
			ret[i] = Double.parseDouble(vals[i]);
		}
		return ret;
	}

}
