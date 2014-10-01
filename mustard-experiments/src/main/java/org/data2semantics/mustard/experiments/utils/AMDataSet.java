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

public class AMDataSet implements ClassificationDataSet {
	private RDFDataSet tripleStore;
	private String property;
	private int minClassSize;
	private long seed;
	private double fraction;

	private List<Double> target;
	private RDFData rdfData;



	public AMDataSet(RDFDataSet tripleStore, String property,
			int minClassSize, long seed, double fraction) {
		this.tripleStore = tripleStore;
		this.property = property;
		this.minClassSize = minClassSize;
		this.seed = seed;
		this.fraction = fraction;
	}

	public void create() {	
		Random rand = new Random(seed);

		List<Statement> stmts = tripleStore.getStatementsFromStrings(null, "http://purl.org/collections/nl/am/objectCategory", null);

		System.out.println(tripleStore.getLabel() + " # objects: " + stmts.size());

		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();
		List<Statement> blackList = new ArrayList<Statement>();

		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}

		blackList = DataSetUtils.createBlacklist(tripleStore, instances, labels);

		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		for (Statement stmt : stmts) {
			if (rand.nextDouble() < fraction) {
				instances.add(stmt.getSubject());
				labels.add(stmt.getObject());
			}
		}
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
