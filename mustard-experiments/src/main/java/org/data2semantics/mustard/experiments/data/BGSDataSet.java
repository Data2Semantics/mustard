package org.data2semantics.mustard.experiments.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class BGSDataSet implements LargeClassificationDataSet {
	private RDFDataSet tripleStore;
	private String property;
	private int minClassSize;
	private int maxNumClasses;
	private long seed;
	private double fraction;

	private List<Double> target;
	private RDFData rdfData;
	
	private boolean equalClassSizes = false;

	public BGSDataSet(RDFDataSet tripleStore, String property, long seed, double fraction, int minClassSize, int maxNumClasses) {
		this.tripleStore = tripleStore;
		this.property = property;
		this.minClassSize = minClassSize;
		this.maxNumClasses = maxNumClasses;
		this.seed = seed;
		this.fraction = fraction;
	}

	public void create() {
		createSubSet(seed, fraction, minClassSize, maxNumClasses);	
	}
	
	
	
	public void createSubSet(long seed, double fraction, int minClassSize,
			int maxNumClasses) {
		Random rand = new Random(seed);

		List<Statement> stmts = tripleStore.getStatementsFromStrings(null, "http://www.w3.org/2000/01/rdf-schema#isDefinedBy", "http://data.bgs.ac.uk/ref/Lexicon/NamedRockUnit");
		System.out.println(tripleStore.getLabel());
		System.out.println("Component Rock statements: " + stmts.size());
		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();
		List<Resource> instances2 = new ArrayList<Resource>();
		List<Value> labels2 = new ArrayList<Value>();

		List<Statement> blackList = new ArrayList<Statement>();

		// http://data.bgs.ac.uk/ref/Lexicon/hasRockUnitRank
		// http://data.bgs.ac.uk/ref/Lexicon/hasTheme

		for(Statement stmt: stmts) {
			List<Statement> stmts2 = tripleStore.getStatementsFromStrings(stmt.getSubject().toString(), property, null);

			if (stmts2.size() > 1) {
				System.out.println("more than 1 Class");
			}

			for (Statement stmt2 : stmts2) {
				instances2.add(stmt2.getSubject());
				labels2.add(stmt2.getObject());		

				if (rand.nextDouble() < fraction) {
					instances.add(stmt2.getSubject());
					labels.add(stmt2.getObject());					 
				}
			}
		}
		
		EvaluationUtils.removeSmallClasses(instances, labels, minClassSize);
		EvaluationUtils.keepLargestClasses(instances, labels, maxNumClasses);
		
		
		blackList = DataSetUtils.createBlacklist(tripleStore, instances2, labels2);
		target = EvaluationUtils.createTarget(labels);
		
		Map<Double,Double> counts = EvaluationUtils.computeClassCounts(target);
		
		if (equalClassSizes) {
			Map<Double,Integer> newCounts = new HashMap<Double,Integer>();
			
			Collections.shuffle(instances, new Random(seed));
			Collections.shuffle(labels, new Random(seed));
			Collections.shuffle(target, new Random(seed));
			
			int smallest = instances.size();
			for (double key : counts.keySet()) {
				if (counts.get(key) < smallest) {
					smallest = (int) Math.round(counts.get(key));
				}
				newCounts.put(key, 0);
			}
			
			List<Resource> instances3 = new ArrayList<Resource>();
			List<Value> labels3 = new ArrayList<Value>();
			
			for (int i = 0; i < instances.size(); i++) {
				if (newCounts.get(target.get(i)) < smallest) {
					newCounts.put(target.get(i), (Math.round(newCounts.get(target.get(i)))) + 1);
					instances3.add(instances.get(i));
					labels3.add(labels.get(i));
				}			
			}
			instances = instances3;
			labels = labels3;		
		}
		
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
