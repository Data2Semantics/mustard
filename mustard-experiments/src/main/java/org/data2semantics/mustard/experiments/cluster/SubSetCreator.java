package org.data2semantics.mustard.experiments.cluster;

import java.io.File;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.Set;

import org.data2semantics.mustard.experiments.data.AMDataSet;
import org.data2semantics.mustard.experiments.data.BGSDataSet;
import org.data2semantics.mustard.experiments.data.LargeClassificationDataSet;
import org.data2semantics.mustard.rdf.RDFDataSet;
import org.data2semantics.mustard.rdf.RDFFileDataSet;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFWriter;
import org.openrdf.rio.Rio;

/**
 * Simple script to create subsets of a dataset. Configure in your IDE to use. This probably shouldn't be Java code ;)
 * 
 * @author Gerben
 *
 */
public class SubSetCreator {
	private static final String AM_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\AM_data";
	private static final String BGS_FOLDER =  "C:\\Users\\Gerben\\Dropbox\\data_bgs_ac_uk_ALL";

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		boolean[] inference = {false, true};
		long[] seeds = {1,2,3,4,5,6,7,8,9,10};
		double fraction = 0.02;
		int minSize = 0;
		int maxClasses = 15;
		int depth = 2;
		
		/*
		String saveDir = "datasets/BGSsubset";
		String loadDir = BGS_FOLDER;
		RDFFormat format = RDFFormat.NTRIPLES;
		//*/
	
		///*
		String saveDir = "datasets/AMsubset";
		String loadDir = AM_FOLDER;
		RDFFormat format = RDFFormat.TURTLE;
		//*/
	
		
		RDFDataSet tripleStore = new RDFFileDataSet(loadDir, format);

		//LargeClassificationDataSet ds = new BGSDataSet(tripleStore, "http://data.bgs.ac.uk/ref/Lexicon/hasTheme", 10, 0.05, 5, 3);

		LargeClassificationDataSet ds = new AMDataSet(tripleStore,  10, 0.05, 5, 3, true);

		
		for (long seed : seeds) {
			for (boolean inf : inference) {
				ds.createSubSet(seed, fraction, minSize, maxClasses);

				System.out.println("Getting Statements...");
				Set<Statement> stmts = RDFUtils.getStatements4Depth(tripleStore, ds.getRDFData().getInstances(), depth, inf);
				System.out.println("# Statements: " + stmts.size());
				stmts.removeAll(new HashSet<Statement>(ds.getRDFData().getBlackList()));
				System.out.println("# Statements: " + stmts.size() + ", after blackList");


				File dir = new File(saveDir + seed + inf);
				dir.mkdirs();

				File file = new File(saveDir + seed + inf, "subset.ttl");
				File instFile = new File(saveDir + seed + inf, "instances.txt");
				File targetFile = new File(saveDir + seed + inf, "target.txt");
				
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
