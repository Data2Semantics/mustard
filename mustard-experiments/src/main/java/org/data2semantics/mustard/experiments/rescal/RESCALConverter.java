package org.data2semantics.mustard.experiments.rescal;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;

public class RESCALConverter {
	private Map<Resource, Integer> entityMap;
	private Map<URI, Integer> relationMap;
	private Map<String, Integer> attributeMap;

	private Map<Integer, List<Integer>> rowMap;
	private Map<Integer, List<Integer>> colMap;
	private List<Integer> attributeRow;
	private List<Integer> attributeCol;


	public RESCALConverter() {
		entityMap    = new HashMap<Resource, Integer>();
		relationMap  = new HashMap<URI, Integer>();
		attributeMap = new HashMap<String, Integer>();		
	}

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
				if (!attributeMap.containsKey(stmt.getPredicate().toString() + stmt.getObject().toString() )) {
					attributeMap.put(stmt.getPredicate().toString() + stmt.getObject().toString(), attributeMap.size());
				}
				attributeRow.add(entityMap.get(stmt.getSubject()));
				attributeCol.add(attributeMap.get(stmt.getPredicate().toString() + stmt.getObject().toString()));
			}
		}
	}

	public Map<Integer, Resource> save(String dir) {
		Map<Integer, Resource> invEntityMap    = new TreeMap<Integer, Resource>();
		Map<Integer, String>   invAttributeMap = new TreeMap<Integer, String>(); 
		for (Resource e : entityMap.keySet()) {
			invEntityMap.put(entityMap.get(e), e);
		}
		for (String s : attributeMap.keySet()) {
			invAttributeMap.put(attributeMap.get(s), s);
		}


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


			for (URI k : relationMap.keySet()) {
				if (!rowMap.get(relationMap.get(k)).isEmpty()) {
					f = new FileWriter(saveDir.getAbsolutePath() +"/"+ relationMap.get(k) + "-rows");
					for (Integer idx : rowMap.get(relationMap.get(k))) {
						f.write(idx + " ");
					}
					f.close();
				}

				if (!colMap.get(relationMap.get(k)).isEmpty()) {
					f = new FileWriter(saveDir.getAbsolutePath() +"/"+ relationMap.get(k) + "-cols");
					for (Integer idx : colMap.get(relationMap.get(k))) {
						f.write(idx + " ");
					}
					f.close();	
				}
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
		return invEntityMap;
	}

}
