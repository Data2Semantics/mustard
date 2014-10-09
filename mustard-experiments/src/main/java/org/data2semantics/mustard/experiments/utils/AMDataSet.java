package org.data2semantics.mustard.experiments.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class AMDataSet implements LargeClassificationDataSet {
	private RDFDataSet tripleStore;
	private int minClassSize;
	private long seed;
	private double fraction;
	private int maxNumClasses;

	private List<Double> target;
	private RDFData rdfData;



	public AMDataSet(RDFDataSet tripleStore, long seed, double fraction, int minClassSize, int maxNumClasses) {
		this.tripleStore = tripleStore;
		this.minClassSize = minClassSize;
		this.maxNumClasses = maxNumClasses;
		this.seed = seed;
		
		this.fraction = fraction;
	}

	public void create() {
		createSubSet(seed, fraction, minClassSize, maxNumClasses);	
	}
	
	
	
	public void createSubSet(long seed, double fraction, int minClassSize, int maxNumClasses) {

		Random rand = new Random(seed);

		List<Statement> stmts = tripleStore.getStatementsFromStrings(null, "http://purl.org/collections/nl/am/objectCategory", null);

		System.out.println(tripleStore.getLabel() + " # objects: " + stmts.size());

		List<Resource> instances2 = new ArrayList<Resource>();
		List<Value> labels2 = new ArrayList<Value>();
		List<Statement> blackList = new ArrayList<Statement>();

		for (Statement stmt : stmts) {
			instances2.add(stmt.getSubject());
			labels2.add(stmt.getObject());
		}
		
		target = EvaluationUtils.createTarget(labels2);	
		System.out.println("# classes: " + EvaluationUtils.computeClassCounts(target).keySet().size());

		blackList = DataSetUtils.createBlacklist(tripleStore, instances2, labels2);
		EvaluationUtils.keepLargestClasses(instances2, labels2, maxNumClasses);
		
		
		// --- Materials blacklist ---
		blackList.addAll(tripleStore.getStatementsFromStrings(null, "http://purl.org/collections/nl/am/material", null));	
		// ---------------------------
				
		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();

		for (int i = 0; i < instances2.size(); i++) {
			if (rand.nextDouble() < fraction) {
				instances.add(instances2.get(i));
				labels.add(labels2.get(i));
			}
		}
	
		//EvaluationUtils.keepLargestClasses(instances, labels, maxNumClasses);
		EvaluationUtils.removeSmallClasses(instances, labels, minClassSize);

		rdfData = new RDFData(tripleStore, instances, blackList);
		target = EvaluationUtils.createTarget(labels);	
		System.out.println("Subset class count: " + EvaluationUtils.computeClassCounts(target));
	}

	public RDFData getRDFData() {
		return rdfData;
	}

	public List<Double> getTarget() {

		return target;
	}


}
