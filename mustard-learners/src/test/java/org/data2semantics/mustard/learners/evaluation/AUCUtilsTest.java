package org.data2semantics.mustard.learners.evaluation;

import static org.junit.Assert.*;

import org.data2semantics.mustard.learners.evaluation.utils.AUCUtils;
import org.junit.Test;

public class AUCUtilsTest {

	@Test
	public void test() {
		double[] vals = {-0.5, -1.1, -2.0, 0.8, 0.1, 0.5};
		double[] vals2 = {0.5, 1.1, 2.0, -0.8, -0.1, -0.5};
		
		boolean[] labs = {false, true, false, false, true, true};
		boolean[] labs2 = {true, false, true, true, false, false};
		
		double[] vals3 = {-0.5, -1.1, 2.0, 0.8, 0.1, 0.5};
		double[] vals4 = {0.5, 1.1, -2.0, -0.8, -0.1, -0.5};
		
		boolean[] labs3 = {false, true, false, false, true, true};
		boolean[] labs4 = {true, false, true, true, false, false};
		
		double[] vals5 = {3,2,1,-1,-2,-3};
		double[] vals6 = {-3,-2,-1,1,2,3};
		
		boolean[] labs5 = {true,true,true,false,false,false};
		boolean[] labs6 = {false,false,false,true,true,true};
		
		
		
		System.out.println("ROC 1: " + AUCUtils.computeRocAuc(vals, labs));
		System.out.println("ROC 2: " + AUCUtils.computeRocAuc(vals2, labs2));
		System.out.println("PR 1: " + AUCUtils.computePRAuc(vals, labs));
		System.out.println("PR 2: " + AUCUtils.computePRAuc(vals2, labs2));
		System.out.println("ROC 3: " + AUCUtils.computeRocAuc(vals3, labs3));
		System.out.println("ROC 4: " + AUCUtils.computeRocAuc(vals4, labs4));
		System.out.println("PR 3: " + AUCUtils.computePRAuc(vals3, labs3));
		System.out.println("PR 4: " + AUCUtils.computePRAuc(vals4, labs4));
		System.out.println("ROC 5: " + AUCUtils.computeRocAuc(vals5, labs5));
		System.out.println("ROC 6: " + AUCUtils.computeRocAuc(vals6, labs6));
		System.out.println("PR 5: " + AUCUtils.computePRAuc(vals5, labs5));
		System.out.println("PR 6: " + AUCUtils.computePRAuc(vals6, labs6));

	}

}
