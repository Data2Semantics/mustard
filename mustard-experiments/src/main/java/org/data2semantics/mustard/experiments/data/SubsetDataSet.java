package org.data2semantics.mustard.experiments.data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.learners.evaluation.utils.EvaluationUtils;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class SubsetDataSet implements ClassificationDataSet {
	private RDFDataSet tripleStore;
	private List<Resource> instances;
	private List<Double> target;
	private RDFData rdfData;

	public SubsetDataSet(String dir) {
		tripleStore = new RDFFileDataSet(dir + "/subset.ttl", RDFFormat.TURTLE);
		instances = new ArrayList<Resource>();
		target = new ArrayList<Double>();
		
		try {
			BufferedReader instReader = new BufferedReader(new FileReader(dir + "/instances.txt"));

			String line = instReader.readLine();
			while (line != null) {
				instances.add(tripleStore.createURI(line));
				line = instReader.readLine();
			}
			instReader.close();
			
			BufferedReader targetReader = new BufferedReader(new FileReader(dir + "/target.txt"));

			line = targetReader.readLine();
			while (line != null) {
				target.add(Double.parseDouble(line));
				line = targetReader.readLine();
			}
			targetReader.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void create() {
		List<Statement> blackList = new ArrayList<Statement>();
		rdfData = new RDFData(tripleStore, instances, blackList);
		System.out.println("Subset class count: " + EvaluationUtils.computeClassCounts(target));
	}

	public RDFData getRDFData() {
		return rdfData;
	}

	public List<Double> getTarget() {
		return target;
	}


}
