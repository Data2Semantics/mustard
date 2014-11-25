package org.data2semantics.mustard.experiments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.rdfdata.RDFWLSubTreeKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.rio.RDFFormat;

public class FeatureCountAMExperiment {
	private static String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";

	private static List<Resource> instances;
	private static List<Value> labels;
	private static List<Statement> blackList;
	private static List<Double> target;
	private static RDFDataSet dataset;

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		dataset = new RDFFileDataSet(AM_FOLDER, RDFFormat.TURTLE);

		long[] seedsDataset = {11,21,31,41,51}; //,61,71,81,91,101};
	
		int[] subsetSize = {100, 200, 400, 800};

	

		for (int ss : subsetSize) {
			double avgInstances = 0;
			double avgNumFeatures = 0;
			double avgNonZeroFeatures = 0;
			for (long sDS : seedsDataset) {
				createAMDataSet(dataset, sDS, ss, 10);
				RDFData data = new RDFData(dataset, instances, blackList);
				RDFWLSubTreeKernel kernel = new RDFWLSubTreeKernel(4, 2, false, true, false, true);
				SparseVector[] fv = kernel.computeFeatureVectors(data);
				
				avgInstances += fv.length;
				avgNumFeatures += fv[0].getLastIndex()+1;
				double avg = 0;
				for (SparseVector v : fv) {
					avg += v.size();
				}
				avgNonZeroFeatures += (avg / (fv.length));
			}
			
			avgInstances /= (seedsDataset.length);
			avgNumFeatures /= (seedsDataset.length);
			avgNonZeroFeatures /= (seedsDataset.length);
			
			System.out.println("# instances: " + avgInstances + ", # features: " + avgNumFeatures +  ", # non-zero features: " + avgNonZeroFeatures);
		}





	}


	private static void createAMDataSet(RDFDataSet dataset, long seed, int subsetSize, int minSize) {

		Random rand = new Random(seed);

		List<Statement> stmts = dataset.getStatementsFromStrings(null, "http://purl.org/collections/nl/am/objectCategory", null);
		System.out.println(dataset.getLabel());

		System.out.println("objects in AM: " + stmts.size());


		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		blackList = new ArrayList<Statement>();

		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}

		//		
		//		
		blackList = DataSetUtils.createBlacklist(dataset, instances, labels);
		//System.out.println(EvaluationUtils.computeClassCounts(target));

		Collections.shuffle(instances, new Random(seed));
		Collections.shuffle(labels, new Random(seed));

		instances = instances.subList(0, subsetSize);
		labels = labels.subList(0, subsetSize);

		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
		target = EvaluationUtils.createTarget(labels);

		System.out.println("Subset: ");
		System.out.println(EvaluationUtils.computeClassCounts(target));

	}
}
