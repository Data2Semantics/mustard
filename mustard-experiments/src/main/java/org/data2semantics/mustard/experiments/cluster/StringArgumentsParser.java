package org.data2semantics.mustard.experiments.cluster;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphGraphListWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphRootWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWalkCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.DTGraphWLSubTreeKernel;

public class StringArgumentsParser {
	private String dataFile;
	private String kernel;
	private String[] kernelParms;
	private int depth;
	private boolean inference;

	public StringArgumentsParser(String[] args) {
		kernelParms = new String[9];

		for (int i = 0; i < args.length; i++) {
			if (args[i].equals("-file")) {
				dataFile = args[++i];
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
			if (args[i].equals("-inference")) {
				inference = Boolean.parseBoolean(args[++i]);
			}
		}
	}
	
	public List<? extends GraphKernel<SingleDTGraph>> graphKernel() {
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
		if (kernel.equals("TreeWalksRoot")) {
			return treeWalksRoot(kernelParms);
		}
		if (kernel.equals("TreeSubtreesRoot")) {
			return treeSubtreesRoot(kernelParms);
		}
		if (kernel.equals("GraphWalks")) {
			return graphWalks(kernelParms);
		}
		if (kernel.equals("GraphSubtrees")) {
			return graphSubtrees(kernelParms);
		}
		return null;
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
		sb.append("_");
		sb.append(depth);
		sb.append("_");
		sb.append(inference);

		return sb.toString();
	}


	public int getDepth() {
		return depth;
	}



	public boolean isInference() {
		return inference;
	}



	public String getDataFile() {
		return dataFile;
	}



	/**
	 * -kernel GraphBoL
	 * -kernelParm1 depth (int)
	 * 
	 * @return
	 */
	public static List<DTGraphWLSubTreeKernel> graphBagOfLabels(String[] parms) {
		List<DTGraphWLSubTreeKernel> kernels = new ArrayList<DTGraphWLSubTreeKernel>();
		int depth = Integer.parseInt(parms[0]);	
		kernels.add(new DTGraphWLSubTreeKernel(0, depth, false, false, true));
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
		int depth = Integer.parseInt(parms[0]);	
		kernels.add( new DTGraphTreeWLSubTreeKernel(0, depth, false, false, true));
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

		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int depth = Integer.parseInt(parms[1]);
		for (int p : pathLengths) {
			kernels.add(new DTGraphTreeWalkCountKernel(p, depth, true));

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

		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int depth = Integer.parseInt(parms[1]);
		for (int p : pathLengths) {
			kernels.add(new DTGraphGraphListWalkCountKernel(p, depth, true));

		}
		return kernels;
	}
	

	/**
	 * -kernel TreeSubtrees
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * -kernelParm3 trackPrevNBH (boolean)
	 * 
	 * @return
	 */
	public static List<DTGraphTreeWLSubTreeKernel> treeSubtrees(String[] parms) {
		List<DTGraphTreeWLSubTreeKernel> kernels = new ArrayList<DTGraphTreeWLSubTreeKernel>();
		int[] pathLengths = new int[1];

		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int depth = Integer.parseInt(parms[1]);
		boolean trackPrev = Boolean.parseBoolean(parms[2]);
		for (int p : pathLengths) {
			kernels.add(new DTGraphTreeWLSubTreeKernel(p, depth, true, false, trackPrev, true));

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
	 * 
	 * @return
	 */
	public static List<DTGraphGraphListWLSubTreeKernel> graphSubtrees(String[] parms) {
		List<DTGraphGraphListWLSubTreeKernel> kernels = new ArrayList<DTGraphGraphListWLSubTreeKernel>();
		int[] pathLengths = new int[1];

		if (parms[0].startsWith("[")) {
			pathLengths = parseIntArray(parms[0]);
		} else {
			pathLengths[0] = Integer.parseInt(parms[0]); 
		}
		int depth = Integer.parseInt(parms[1]);
		boolean trackPrev = Boolean.parseBoolean(parms[2]);
		for (int p : pathLengths) {
			kernels.add(new DTGraphGraphListWLSubTreeKernel(p, depth, true, trackPrev, true));

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
