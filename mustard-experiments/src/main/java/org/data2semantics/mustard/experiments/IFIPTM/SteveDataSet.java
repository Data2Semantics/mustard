package org.data2semantics.mustard.experiments.IFIPTM;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class SteveDataSet implements LargeClassificationDataSet {
	private RDFDataSet tripleStore;
	private long seed;

	private List<Double> target;
	private RDFData rdfData;

	public SteveDataSet(RDFDataSet tripleStore, long seed) {
		this.tripleStore = tripleStore;
		this.seed = seed;
	}

	public void create() {
		createSubSet(seed, 0, 50, 0);
	}

	public void createSubSet(long seed, double fraction, int minClassSize, int maxNumClasses) {
		Random rand = new Random(seed);

		List<Statement> stmts = tripleStore.getStatementsFromStrings(null,
				"http://www.w3.org/ns/openannotation/core/hasReview", null);

		System.out.println(tripleStore.getLabel() + " # objects: " + stmts.size());

		// Containers for ALL the resources
		List<Resource> instances2 = new ArrayList<Resource>();
		List<Value> labels2 = new ArrayList<Value>();
		List<Statement> blackList = new ArrayList<Statement>();

		for (Statement stmt : stmts) {
			instances2.add(stmt.getSubject());
			labels2.add(stmt.getObject());
		}

		// convert the prediction variable to a double (only used for counting
		// the classes)
		target = EvaluationUtils.createTarget(labels2);
		Map<Double, Double> classCounts = EvaluationUtils.computeClassCounts(target);

		// identify the ID and size of the largest class
		double largestClassID = -1;
		double largestClassCount = -1;
		double totalClassCount = 0;
		for (Double clazz : classCounts.keySet()) {
			double val = classCounts.get(clazz);
			totalClassCount += val;
			if (val > largestClassCount) {
				largestClassID = clazz;
				largestClassCount = val;
			}
		}
		// The largest class should match all other classes in size. Determine
		// how large that class should be
		double largestClassDesiredSize = totalClassCount - largestClassCount;

		// identify the triples in the graph that contain the prediction
		// variable
		blackList = DataSetUtils.createBlacklist(tripleStore, instances2, labels2);

		// Containers for the SUBSET
		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();

		// Determine the fraction of the largest class that we should keep
		fraction = largestClassDesiredSize / largestClassCount;
		for (int i = 0; i < instances2.size(); i++) {
			boolean add = false;
			// if the instance comes from the largest class
			if (target.get(i).equals(largestClassID)) {
				// Use the fraction to decide whether to keep it.
				add = rand.nextDouble() < fraction;
			} else {
				// always add other classes
				add = true;
			}
			if (add) {
				instances.add(instances2.get(i));
				labels.add(labels2.get(i));
			}
		}

		EvaluationUtils.removeSmallClasses(instances, labels, minClassSize);

		// create the RDFData object form the SUBSET and remove the blacklist
		// triples
		rdfData = new RDFData(tripleStore, instances, blackList);
		// convert the prediction variables of the SUBSET to a double
		Map<Value, Double> labelMap = new HashMap<Value, Double>();
		target = EvaluationUtils.createTarget(labels, labelMap);
		System.out.println("Label mapping: " + labelMap);

		classCounts = EvaluationUtils.computeClassCounts(target);
		System.out.println("# classes: " + classCounts.keySet().size());
		System.out.println("Subset class count: " + classCounts);
	}

	public RDFData getRDFData() {
		return rdfData;
	}

	public List<Double> getTarget() {

		return target;
	}

}
