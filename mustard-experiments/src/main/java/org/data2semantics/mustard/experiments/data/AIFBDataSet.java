package org.data2semantics.mustard.experiments.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

public class AIFBDataSet implements ClassificationDataSet {
	private RDFDataSet tripleStore;
	private RDFData data;
	private List<Double> target;

	public AIFBDataSet(RDFDataSet tripleStore) {
		super();
		this.tripleStore = tripleStore;
	}

	public void create() {
		List<Statement> stmts = tripleStore.getStatementsFromStrings(null, "http://swrc.ontoware.org/ontology#affiliation", null);

		List<Resource> instances = new ArrayList<Resource>();
		List<Value> labels = new ArrayList<Value>();

		for (Statement stmt : stmts) {
			instances.add(stmt.getSubject());
			labels.add(stmt.getObject());
		}

		List<Statement> blackList = DataSetUtils.createBlacklist(tripleStore, instances, labels);
		EvaluationUtils.removeSmallClasses(instances, labels, 5);
		
		Map<Value, Double> labelMap = new HashMap<Value,Double>();
		target = EvaluationUtils.createTarget(labels, labelMap);
		
		System.out.println("Label mapping: " + labelMap);
		
		data = new RDFData(tripleStore, instances, blackList);
	}

	public RDFData getRDFData() {
		return data;
	}

	public List<Double> getTarget() {
		return target;	
	}

}
