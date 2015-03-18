package org.data2semantics.mustard.experiments.rescal;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.experiments.data.AIFBDataSet;
import org.data2semantics.mustard.experiments.data.ClassificationDataSet;
import org.data2semantics.mustard.experiments.rescal.RESCALInput;
import org.data2semantics.mustard.experiments.rescal.RESCALKernel.StreamHandler;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;

public class RESCALTest {
	private static String dataFile = "datasets/aifb-fixed_complete.n3";	
	public static final String PYTHON_EXE = "C:\\Users\\Gerben\\Dropbox\\D2S\\python_stuff\\WinPython-64bit-2.7.6.4\\python-2.7.6.amd64\\python.exe";
	public static final String RESCAL_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\python_stuff\\Ext-RESCAL-master";


	@Ignore
	public void test() {
		RDFDataSet ts = new RDFFileDataSet(dataFile, RDFFormat.N3);
		ClassificationDataSet data = new AIFBDataSet(ts);
		data.create();
		
		List<Statement> all = new ArrayList<Statement>(RDFUtils.getStatements4Depth(data.getRDFData().getDataset(), data.getRDFData().getInstances(), 2, false));
		
		Collections.shuffle(all);
		List<Statement> train = all.subList(10, all.size());
		List<Statement> test  = all.subList(0, 10);
		
		RESCALLinkPredictor lp = new RESCALLinkPredictor(10, 0);
		lp.train(train);	
		
		for (Statement stmt : test) {
			System.out.println("Prediction: " + lp.predict(stmt) + ", " + stmt);
		}
	}

}
