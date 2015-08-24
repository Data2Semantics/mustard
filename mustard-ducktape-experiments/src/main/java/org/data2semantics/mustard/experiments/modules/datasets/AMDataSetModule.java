package org.data2semantics.mustard.experiments.modules.datasets;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.utils.EvaluationUtils;
import org.data2semantics.mustard.rdf.DataSetUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.platform.annotation.In;
import org.data2semantics.platform.annotation.Main;
import org.data2semantics.platform.annotation.Module;
import org.data2semantics.platform.annotation.Out;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;

@Module(name="AMDataSet")
public class AMDataSetModule {
	private RDFDataSet dataset;
	private int minSize;
	private double fraction;
	private long seed;
	private String property;
	
	private RDFData rdfData;
	
	private List<Resource> instances;
	private List<Value> labels;
	private List<Double> target;
	private List<Statement> blacklist;
	
	public AMDataSetModule(
			@In(name="dataset") RDFDataSet dataset,
			@In(name="minSize") int minSize,
			@In(name="fraction") double fraction,
			@In(name="seed") int seed,
			@In(name="property") String property
			) {
		this.dataset = dataset;
		this.minSize = minSize;
		this.fraction = fraction;
		this.seed = seed;
		this.property = property;
	}
	
	@Main
	public RDFData createDataSet() {
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();
		
		Random rand = new Random(seed);
		
		// Extract all triples with the affiliation predicate
		List<Statement> stmts = dataset.getStatementsFromStrings(null, property, null);

		// initialize the lists of instances and labels
		instances = new ArrayList<Resource>();
		labels = new ArrayList<Value>();

		// The subjects of the affiliation triples will we our instances and the objects our labels
		for (Statement stmt : stmts) {
			if (rand.nextDouble() <= fraction) {
				instances.add(stmt.getSubject());
				labels.add(stmt.getObject());
			}
		}

		EvaluationUtils.removeSmallClasses(instances, labels, minSize);
		blacklist = DataSetUtils.createBlacklist(dataset, instances, labels);
		target = EvaluationUtils.createTarget(labels);
		
		rdfData = new RDFData(dataset, instances, blacklist);
		return rdfData;
	}
	
	@Out(name="rdfData")
	public RDFData getRDFData() {
		return rdfData;
	}
	
	@Out(name="labels")
	public List<Value> getLabels() {
		return labels;
	}
	
	@Out(name="target")
	public List<Double> getTarget() {
		return target;
	}
}
