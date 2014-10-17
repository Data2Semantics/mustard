package org.data2semantics.mustard.experiments.cluster;

import java.util.ArrayList;
import java.util.List;

import org.data2semantics.mustard.kernels.data.SingleDTGraph;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.RDFDTGraphTreePathCountKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.RDFDTGraphTreeWLSubTreeKernel;
import org.data2semantics.mustard.kernels.graphkernels.singledtgraph.RDFDTGraphWLSubTreeKernel;

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
	
	

	public int getDepth() {
		return depth;
	}



	public boolean isInference() {
		return inference;
	}



	public String getDataFile() {
		return dataFile;
	}

	public GraphKernel<SingleDTGraph> graphKernel() {
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
		return null;
	}
	
	
	/**
	 * -kernel GraphBoL
	 * -kernelParm1 depth (int)
	 * 
	 * @return
	 */
	public static RDFDTGraphWLSubTreeKernel graphBagOfLabels(String[] parms) {
		int depth = Integer.parseInt(parms[0]);	
		return new RDFDTGraphWLSubTreeKernel(0, depth, false, false, true);
	}
	
	/**
	 * -kernel TreeBoL
	 * -kernelParm1 depth (int)
	 * 
	 * @return
	 */
	public static RDFDTGraphTreeWLSubTreeKernel treeBagOfLabels(String[] parms) {
		int depth = Integer.parseInt(parms[0]);	
		return new RDFDTGraphTreeWLSubTreeKernel(0, depth, false, false, true);
	}
	
	/**
	 * -kernel TreeWalks
	 * -kernelParm1 maxWalkLength (int)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static RDFDTGraphTreePathCountKernel treeWalks(String[] parms) {
		int pathLength = Integer.parseInt(parms[0]);
		int depth = Integer.parseInt(parms[1]);
		return new RDFDTGraphTreePathCountKernel(pathLength, depth, true);
	}
	
	/**
	 * -kernel TreeSubtrees
	 * -kernelParm1 numIterations (int)
	 * -kernelParm2 depth (int)
	 * 
	 * @return
	 */
	public static RDFDTGraphTreeWLSubTreeKernel treeSubtrees(String[] parms) {
		int numIterations = Integer.parseInt(parms[0]);
		int depth = Integer.parseInt(parms[1]);
		return new RDFDTGraphTreeWLSubTreeKernel(numIterations, depth, true, false, true);
	}
	
}
