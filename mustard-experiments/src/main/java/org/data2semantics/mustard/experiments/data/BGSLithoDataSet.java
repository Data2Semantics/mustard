package org.data2semantics.mustard.experiments.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.utils.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class BGSLithoDataSet implements ClassificationDataSet {
	private RDFDataSet tripleStore;
	
	private List<Double> target;
	private RDFData rdfData;



	public BGSLithoDataSet(RDFDataSet tripleStore) {
		this.tripleStore = tripleStore;
	}

	public void create() {
		createSubSet(1, 1, 5, 100);	
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
			List<Statement> stmts2 = tripleStore.getStatementsFromStrings(stmt.getSubject().toString(), "http://data.bgs.ac.uk/ref/Lexicon/hasLithogenesis", null);

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
