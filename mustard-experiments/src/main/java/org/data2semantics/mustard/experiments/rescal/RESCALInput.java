package org.data2semantics.mustard.experiments.rescal;

import java.io.File;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.data2semantics.mustard.utils.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;


/**
 * This class can create a conversion to RESCAL format from an RDF graph (List of Statements)
 * Using a set of Mappings to and from Integers for entities (Resources), relations (URI's/predicates) and attributes (Literals as Strings).
 * 
 * @author Gerben
 *
 */
public class RESCALInput {
	private Map<Resource, Integer> entityMap;
	private Map<URI, Integer> relationMap;
	private Map<Pair<URI,Literal>, Integer> attributeMap;
	private Map<String, Integer> additionalSliceMap;

	private Map<Integer, Resource> 	invEntityMap;
	private Map<Integer, URI> 		invRelationMap;
	private Map<Integer, Pair<URI,Literal>> 	invAttributeMap;
	private Map<Integer, String> invAdditionalSliceMap;

	private Map<Integer, List<Integer>> rowMap;
	private Map<Integer, List<Integer>> colMap;
	private List<Integer> attributeRow;
	private List<Integer> attributeCol;

	private Map<Integer, List<Double>> valMap;

	private int numRelations;


	public RESCALInput() {
		entityMap    = new HashMap<Resource, Integer>();
		relationMap  = new HashMap<URI, Integer>();
		attributeMap = new HashMap<Pair<URI,Literal>, Integer>();
		additionalSliceMap = new HashMap<String, Integer>();
	}

	public Map<Integer, Resource> getInvEntityMap() {
		return invEntityMap;
	}

	public Map<Integer, URI> getInvRelationMap() {
		return invRelationMap;
	}

	public Map<Integer, Pair<URI,Literal>> getInvAttributeMap() {
		return invAttributeMap;
	}

	public Map<Integer, String> getInvAdditionalSliceMap() {
		return invAdditionalSliceMap;
	}

	/**
	 * Convert a list of Statement's 
	 * 
	 * TODO, also implement this for a DTGraph
	 * 
	 * @param rdf
	 */
	public void convert(List<Statement> rdf) {
		rowMap = new HashMap<Integer, List<Integer>>();
		colMap = new HashMap<Integer, List<Integer>>();
		attributeRow = new ArrayList<Integer>();
		attributeCol = new ArrayList<Integer>();

		for (Statement stmt : rdf) {
			if (!relationMap.containsKey(stmt.getPredicate())) {
				relationMap.put(stmt.getPredicate(), relationMap.size());			
				rowMap.put(relationMap.get(stmt.getPredicate()), new ArrayList<Integer>());
				colMap.put(relationMap.get(stmt.getPredicate()), new ArrayList<Integer>());
			} 
			if (!entityMap.containsKey(stmt.getSubject())) {
				entityMap.put(stmt.getSubject(), entityMap.size());
			}
			if (stmt.getObject() instanceof Resource) {
				if (!entityMap.containsKey(stmt.getObject())) {
					entityMap.put((Resource) stmt.getObject(), entityMap.size());
				}
				rowMap.get(relationMap.get(stmt.getPredicate())).add(entityMap.get(stmt.getSubject()));
				colMap.get(relationMap.get(stmt.getPredicate())).add(entityMap.get(stmt.getObject()));
			} else { // Literal
				Pair<URI, Literal> p = new Pair<URI,Literal>(stmt.getPredicate(), (Literal) stmt.getObject());
				if (!attributeMap.containsKey(p)) {
					attributeMap.put(p, attributeMap.size());
				}
				attributeRow.add(entityMap.get(stmt.getSubject()));
				attributeCol.add(attributeMap.get(p));
			}
		}

		numRelations = relationMap.size();

		// Clean up empty relations (because they all point to literals and are part of the attributeMap
		Map<URI, Integer> relationMap2  = new HashMap<URI, Integer>();
		for (URI k : relationMap.keySet()) {
			if (!rowMap.get(relationMap.get(k)).isEmpty()) {
				relationMap2.put(k, relationMap.get(k));
			}
		}
		relationMap = relationMap2;

		invEntityMap    = new TreeMap<Integer, Resource>();
		invAttributeMap = new TreeMap<Integer, Pair<URI,Literal>>(); 
		invRelationMap  = new TreeMap<Integer, URI>(); 
		for (Resource e : entityMap.keySet()) {
			invEntityMap.put(entityMap.get(e), e);
		}
		for (URI e : relationMap.keySet()) {
			invRelationMap.put(relationMap.get(e), e);
		}
		for (Pair<URI,Literal> s : attributeMap.keySet()) {
			invAttributeMap.put(attributeMap.get(s), s);
		}
	}

	/**
	 * Method to add additional slices of similarity information between entities, the indices in the matrix are assumed to correspond to the keys of the inverse Entity map
	 * After this method, convert() should not be called anymore
	 * 
	 * @param slice
	 */
	public void addEntitySimilaritySlice(double[][] slice, String sliceName) {		
		additionalSliceMap.put(sliceName, numRelations + additionalSliceMap.size());
		int sliceIdx = additionalSliceMap.get(sliceName);

		if (valMap == null) {
			valMap = new HashMap<Integer, List<Double>>();
		}

		rowMap.put(sliceIdx, new ArrayList<Integer>());
		colMap.put(sliceIdx, new ArrayList<Integer>());
		valMap.put(sliceIdx, new ArrayList<Double>());

		for (int i = 0; i < slice.length; i++) {
			for (int j = 0; j < slice[i].length; j++) {
				if (slice[i][j] != 0.0) {
					rowMap.get(sliceIdx).add(i);
					colMap.get(sliceIdx).add(j);
					valMap.get(sliceIdx).add(slice[i][j]);
				}
			}
		}

		invAdditionalSliceMap = new TreeMap<Integer, String>();
		for (String e : additionalSliceMap.keySet()) {
			invAdditionalSliceMap.put(additionalSliceMap.get(e), e);
		}
	}


	/**
	 * Save to a directory
	 * 
	 * @param dir
	 * @return
	 */
	public void save(String dir) {

		try {
			File saveDir = new File(dir);
			saveDir.mkdirs();

			for(File file: saveDir.listFiles()) {
				file.delete();
			}

			FileWriter f = null;

			f = new FileWriter(saveDir.getAbsolutePath() +"/"+ "entity-ids");
			for (Integer i : invEntityMap.keySet()) {
				f.write(invEntityMap.get(i) + "\n");
			}
			f.close();


			// To pad additional zero digits, so that the ordering remains correct on the file system
			int digits = Integer.toString(relationMap.size() + additionalSliceMap.size() -1).length();

			for (URI k : relationMap.keySet()) {
				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ String.format("%0" + digits + "d", relationMap.get(k)) + "-rows");
				for (Integer idx : rowMap.get(relationMap.get(k))) {
					f.write(idx + " ");
				}
				f.close();

				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ String.format("%0" + digits + "d", relationMap.get(k)) + "-cols");
				for (Integer idx : colMap.get(relationMap.get(k))) {
					f.write(idx + " ");
				}
				f.close();	
			}

			for (String k : additionalSliceMap.keySet()) {
				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ String.format("%0" + digits + "d", additionalSliceMap.get(k)) + "-rows");
				for (Integer idx : rowMap.get(additionalSliceMap.get(k))) {
					f.write(idx + " ");
				}
				f.close();

				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ String.format("%0" + digits + "d", additionalSliceMap.get(k)) + "-cols");
				for (Integer idx : colMap.get(additionalSliceMap.get(k))) {
					f.write(idx + " ");
				}
				f.close();	

				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ String.format("%0" + digits + "d", additionalSliceMap.get(k)) + "-elements");
				for (Double d : valMap.get(additionalSliceMap.get(k))) {
					f.write(d + " ");
				}
				f.close();	
			}

			if (!invAttributeMap.keySet().isEmpty()) {

				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ "words");
				for (Integer i : invAttributeMap.keySet()) {
					f.write(invAttributeMap.get(i) + "\n");
				}
				f.close();

				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ "ext-matrix-rows");
				for (Integer idx : attributeRow) {
					f.write(idx + " ");
				}
				f.close();

				f = new FileWriter(saveDir.getAbsolutePath() +"/"+ "ext-matrix-cols");
				for (Integer idx : attributeCol) {
					f.write(idx + " ");
				}
				f.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
