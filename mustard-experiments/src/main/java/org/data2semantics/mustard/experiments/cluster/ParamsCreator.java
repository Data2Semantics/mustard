package org.data2semantics.mustard.experiments.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Bit of scripting to print parameter strings to use with Stopos on the Lisa cluster. Hack to what you need :)
 * Intended to be executed from Eclipse or other IDE
 * 
 * @author Gerben
 *
 */
public class ParamsCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filePrefix = "../datasets/AMsubset";
		String[] subsets = {"1","2","3","4","5","6","7","8","9","10"};
		String[] infs = {"false","true"};
		int[] depths = {1};
		boolean opt = true; //"URIPrefix"
		String[] kernels = {"TreeBoL", "GraphBoL"};
		//String[] kernels = {"TreeBoL", "GraphBoL", "TreeWalksRoot", "TreeSubtreesRoot",
		//		"GraphWalksFast", "TreeWalks",
		//		"GraphSubtreesFast", "GraphSubtrees", "TreeSubtrees"}; 
		
		//String[] kernels = {"GraphWalks"};
		
		//String[] kernels = {"TreeWalksRoot_Approx", "TreeSubtreesRoot_Approx",
		//		"GraphWalksFast_Approx", "TreeWalks_Approx",
		//		"GraphSubtreesFast_Approx", "GraphSubtrees_Approx", "TreeSubtrees_Approx"}; 
		
		//String[] kernels = {"TreeSubtreesRoot_Approx", "GraphSubtreesFast_Approx", "GraphSubtrees_Approx", "TreeSubtrees_Approx"}; 
				
		//String[] kernels = {"GraphWalks_Approx"};
		

		for (String subset : subsets) {
			for (String inf : infs) {
				for (int depth : depths) {
					for (String kernel : kernels) {
						KernelParms kps = new KernelParms(kernel, depth, opt);
						//KernelParms kps = new KernelParms(kernel, depth, opt, "[0,4,8,16,32,64]", "10000");
						
						for (String kp : kps) {
							System.out.println("-file " + filePrefix + subset + inf + " -subset " + subset + " -inference " + inf + " -depth " + depth + " " + kp);
						}
					}
				}
			}
		}
	}


	private static class KernelParms implements Iterable<String> {
		private List<String> settings;
		private int depth;
		private String kernel;
		private boolean allIts;
		private Iterator<String> it;
		private String minFreq;
		private String maxCard;

		public KernelParms(String kernel, int depth) {
			this(kernel, depth, true);
		}

		public KernelParms(String kernel, int depth, boolean allIts) {
			this.kernel = kernel;
			this.depth = depth;
			settings = new ArrayList<String>();
			this.allIts = allIts;
			init();
		}

		public KernelParms(String kernel, int depth, boolean allIts, String minFreq, String maxCard) {
			this.kernel = kernel;
			this.depth = depth;
			settings = new ArrayList<String>();
			this.allIts = allIts;
			this.minFreq = minFreq;
			this.maxCard = maxCard;
			initApprox();
		}
		
		private void init() {
			if (kernel.equals("GraphBoL") || kernel.equals("TreeBoL") || kernel.equals("URIPrefix")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + getItsDepth(depth));
			}
			else if (kernel.equals("TreeWalksRoot") || kernel.equals("TreeSubtreesRoot")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth));
			}
			else if (kernel.equals("GraphWalks") || kernel.equals("GraphWalksFast") || kernel.equals("TreeWalks")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + "depthTimesTwo" + " -kernelParm2 " + getItsDepth(depth));
			}
			else {
				//settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 false -kernelParm4 false");
				//settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 false -kernelParm4 true");
				//settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 true -kernelParm4 false");
				settings.add("-kernel " + kernel + " -kernelParm1 " + "depthTimesTwo" + " -kernelParm2 " + getItsDepth(depth) + " -kernelParm3 true -kernelParm4 true");
			}
			it = settings.iterator();
		}
		
		private void initApprox() {
			if (kernel.equals("TreeWalksRoot_Approx")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + minFreq);
			}
			else if (kernel.equals("TreeSubtreesRoot_Approx")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + minFreq + " -kernelParm3 " + maxCard);
			}			
			else if (kernel.equals("GraphWalks_Approx") || kernel.equals("GraphWalksFast_Approx") || kernel.equals("TreeWalks_Approx")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + "depthTimesTwo" + " -kernelParm2 " + getItsDepth(depth) + " -kernelParm3 " + minFreq);
			}
			else {
				settings.add("-kernel " + kernel + " -kernelParm1 " + "depthTimesTwo" + " -kernelParm2 " + getItsDepth(depth) + " -kernelParm3 " + minFreq + " -kernelParm4 " + maxCard);
			}
			it = settings.iterator();
		}

		public Iterator<String> iterator() {
			return it;
		}

		private String getItsDepth(int depth) {
			if (allIts) {
				if (depth == 1) {
					return "[1]";
				}
				if (depth == 2) {
					return "[1,2]";
				}
				if (depth == 3) {
					return "[1,2,3]";
				}
			} else {
				return Integer.toString(depth);
			}
			return null;
		}
		
		private String getIts(int depth) {
			if (allIts) {
				if (depth == 1) {
					return "[2]";
				}
				if (depth == 2) {
					return "[2,4]";
				}
				if (depth == 3) {
					return "[2,4,6]";
				}
			} else {
				return Integer.toString(depth*2);
			}
			return null;
		}
	}
}
