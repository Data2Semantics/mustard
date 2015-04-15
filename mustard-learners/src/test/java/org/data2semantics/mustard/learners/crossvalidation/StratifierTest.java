package org.data2semantics.mustard.learners.crossvalidation;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.data2semantics.mustard.learners.utils.Stratifier;
import org.junit.Test;

public class StratifierTest {

	@Test
	public void test() {
		
		//double[] labels = {1.0, 2.0, 2.0, 2.0, 2.0, 4.0, 3.0, 1.0};
		double[] labels = {1.0, 2.0, 2.0, 1.0};
		
		double[][] kernel = {{2.0, 0.1, 0.0, 0.4}, {0.1, 1.0, 0.0, 0.0}, {0.0, 0.0, 1.0, 0.0}, {0.4, 0.0, 0.0, 1.0}};
		
		List<Integer> ind = Stratifier.stratifyFolds(labels, 2);
		labels = Stratifier.shuffle(labels, ind);
		kernel = Stratifier.shuffle(kernel, ind);
		
		System.out.println(Arrays.toString(labels));
		
		for (int i = 0; i < kernel.length; i++) {
			System.out.println(Arrays.toString(kernel[i]));
		}
		
		ind = Stratifier.stratifySplit(labels, 0.5);
		labels = Stratifier.shuffle(labels, ind);
		kernel = Stratifier.shuffle(kernel, ind);
		
		
		System.out.println(Arrays.toString(labels));
		
		for (int i = 0; i < kernel.length; i++) {
			System.out.println(Arrays.toString(kernel[i]));
		}
	}

}
