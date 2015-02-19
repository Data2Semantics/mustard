package org.data2semantics.mustard.experiments.cluster;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Bit of scripting to print parameter strings to use with Stopos on the Lisa cluster. Hack to what you need :)
 * 
 * @author Gerben
 *
 */
public class ParamsCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filePrefix = "AMsubset";
		String[] subsets = {"1","2","3","4","5","6","7","8","9","10"};
		String[] infs = {"false","true"};
		int[] depths = {1,2,3};
		boolean opt = false;
			String[] kernels = {"URIPrefix", "TreeBoL", "GraphBoL"}; //, "TreeWalksRoot", "TreeSubtreesRoot",
				//"GraphWalks", "GraphWalksFast", "TreeWalks",
				//"GraphSubtreesFast", "GraphSubtrees", "TreeSubtrees"}; 
				

		for (String subset : subsets) {
			for (String inf : infs) {
				for (int depth : depths) {
					for (String kernel : kernels) {
						KernelParms kps = new KernelParms(kernel, depth, opt);

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

		private void init() {
			if (kernel.equals("GraphBoL") || kernel.equals("TreeBoL") || kernel.equals("URIPrefix")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + depth);
			}
			else if (kernel.equals("TreeWalksRoot") || kernel.equals("TreeSubtreesRoot")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth));
			}
			else if (kernel.equals("GraphWalks") || kernel.equals("GraphWalksFast") || kernel.equals("TreeWalks")) {
				settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth);
			}
			else {
				//settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 false -kernelParm4 false");
				//settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 false -kernelParm4 true");
				//settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 true -kernelParm4 false");
				settings.add("-kernel " + kernel + " -kernelParm1 " + getIts(depth) + " -kernelParm2 " + depth + " -kernelParm3 true -kernelParm4 true");
			}
			it = settings.iterator();
		}

		public Iterator<String> iterator() {
			return it;
		}

		private String getIts(int depth) {
			if (allIts) {
				if (depth == 1) {
					return "[0,1,2]";
				}
				if (depth == 2) {
					return "[0,1,2,3,4]";
				}
				if (depth == 3) {
					return "[0,1,2,3,4,5,6]";
				}
			} else {
				return Integer.toString(depth*2);
			}
			return null;
		}
	}
}
