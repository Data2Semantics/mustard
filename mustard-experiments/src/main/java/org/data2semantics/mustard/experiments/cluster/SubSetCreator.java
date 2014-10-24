package org.data2semantics.mustard.experiments.cluster;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AMDataSet;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

public class SubSetCreator {
	private static final String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean[] inference = {false, true};
		long[] seeds = {1,2,3,4,5,6,7,8,9,10};
		double fraction = 0.01;
		int minSize = 0;
		int maxClasses = 15;

		RDFDataSet tripleStore = new RDFFileDataSet(AM_FOLDER, RDFFormat.TURTLE);
		LargeClassificationDataSet ds = new AMDataSet(tripleStore, 10, 0.01, 5, 4, true);

		for (long seed : seeds) {
			for (boolean inf : inference) {
				ds.createSubSet(seed, fraction, minSize, maxClasses);

				System.out.println("Getting Statements...");
				Set<Statement> stmts = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), 3, inf);
				System.out.println("# Statements: " + stmts.size());
				stmts.removeAll(new HashSet<Statement>(ds.getRDFData().getBlackList()));
				System.out.println("# Statements: " + stmts.size() + ", after blackList");


				File dir = new File("datasets/AMsubset" + seed + inf);
				dir.mkdirs();

				File file = new File("datasets/AMsubset" + seed + inf, "subset.ttl");
				File instFile = new File("datasets/AMsubset" + seed + inf, "instances.txt");
				File targetFile = new File("datasets/AMsubset" + seed + inf, "target.txt");
				
				try {
					RDFWriter writer = Rio.createWriter(RDFFormat.TURTLE, new FileWriter(file));
					FileWriter instWriter = new FileWriter(instFile);
					FileWriter targetWriter = new FileWriter(targetFile);
					
					
					writer.startRDF();
					for (Statement stmt : stmts) {
						writer.handleStatement(stmt);
					}
					writer.endRDF();
					
					for (Resource inst : ds.getRDFData().getInstances()) {
						instWriter.write(inst.toString() + "\n");
					}
					instWriter.close();
					
					for (Double target : ds.getTarget()) {
						targetWriter.write(target.toString() + "\n");
					}
					targetWriter.close();

				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
