package org.data2semantics.mustard.experiments.rescal;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.utils.Pair;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class RESCALOutput {
	private Map<Resource, double[]> entityEmb;
	private Map<Pair<URI,Literal>, double[]>   attributeEmb;
	private Map<URI, double[][]>    latentFac;
	private Map<String, double[][]> additionalLatentFac;

	public RESCALOutput() {
		entityEmb 	   = new HashMap<Resource, double[]>();
		attributeEmb   = new HashMap<Pair<URI,Literal>, double[]>();
		latentFac	   = new HashMap<URI, double[][]>();
		additionalLatentFac = new HashMap<String, double[][]>();
	}
	
	public Map<Resource, double[]> getEntityEmb() {
		return entityEmb;
	}

	public Map<Pair<URI,Literal>, double[]> getAttributeEmb() {
		return attributeEmb;
	}

	public Map<URI, double[][]> getLatentFac() {
		return latentFac;
	}

	public Map<String, double[][]> getAdditionalLatentFac() {
		return additionalLatentFac;
	}

	public void loadEntityEmb(String filename, Map<Integer, Resource> invEntityMap) {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(filename));
			String line;

			for (int i = 0; (line = fr.readLine()) != null; i++) {
				int numFacs = line.split(" ").length;
				double[] facs = new double[numFacs];
				entityEmb.put(invEntityMap.get(i), facs);

				int j = 0;
				for (String val : line.split(" ")) {
					facs[j] = Double.parseDouble(val);
					j++;
				}

			}
			fr.close();

		} catch (Exception e) {
			throw new RuntimeException(e);		
		}
	}

	public void loadAttributeEmb(String filename, Map<Integer, Pair<URI,Literal>> invAttributeMap) {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(filename));
			String line;

			for (int i = 0; (line = fr.readLine()) != null; i++) {
				int numFacs = line.split(" ").length;
				double[] facs = new double[numFacs];
				attributeEmb.put(invAttributeMap.get(i), facs);

				int j = 0;
				for (String val : line.split(" ")) {
					facs[j] = Double.parseDouble(val);
					j++;
				}

			}
			fr.close();

		} catch (Exception e) {
			throw new RuntimeException(e);		
		}
	}

	/**
	 * The keys of the map are assumed to be ordered from small to large, might make sense to therefore change the type to a TreeMap
	 * OR alternatively the order of the Map is assumed to also be the order of slices in the input file
	 * 
	 * @param filename
	 * @param invRelationMap
	 */
	public void loadLatentFac(String filename, Map<Integer, URI> invRelationMap) {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(filename));
			String line;

			int numFacs = 0;
			
			int slice = -1;
			double[][] facs = new double[numFacs][numFacs]; 
			
			List<Integer> keys = new ArrayList<Integer>(invRelationMap.keySet()); // get the keys for the slices, they are assumed ordered (since they are a TreeMap).
			
			for (int i = 0; (line = fr.readLine()) != null; i++) {
				if (i == 0) { // determine the number of latent factors from the first line
					numFacs = line.split(" ").length;				
				}
				if (i % numFacs == 0) {
					facs = new double[numFacs][]; 
					slice++;
				}
				facs[i % numFacs] = new double[numFacs];
				
				if (slice < keys.size()) { // only save the regular slices, not the additional ones
					latentFac.put(invRelationMap.get(keys.get(slice)), facs);
				}

				int j = 0;
				for (String val : line.split(" ")) {
					facs[i % numFacs][j] = Double.parseDouble(val);
					j++;
				}
			}
			fr.close();

		} catch (Exception e) {
			throw new RuntimeException(e);		
		}
	}
	

	public void loadAdditionalLatentFac(String filename, Map<Integer, String> invAdditionalSliceMap, int numRelations) {
		try {
			BufferedReader fr = new BufferedReader(new FileReader(filename));
			String line;

			int numFacs = 0;
			
			int slice = -1;
			double[][] facs = new double[numFacs][numFacs]; 
			
			List<Integer> keys = new ArrayList<Integer>(invAdditionalSliceMap.keySet()); // get the keys for the slices, they are assumed ordered (since they are a TreeMap).
			
			for (int i = 0; (line = fr.readLine()) != null; i++) {
				if (i == 0) { // determine the number of latent factors from the first line
					numFacs = line.split(" ").length;				
				}
				if (i % numFacs == 0) {
					facs = new double[numFacs][]; 
					slice++;
				}
				facs[i % numFacs] = new double[numFacs];
				
				if (slice >= numRelations) { // bit sloppy, but only save the later slices
					additionalLatentFac.put(invAdditionalSliceMap.get(keys.get(slice - numRelations)), facs);
				}

				int j = 0;
				for (String val : line.split(" ")) {
					facs[i % numFacs][j] = Double.parseDouble(val);
					j++;
				}
			}
			fr.close();

		} catch (Exception e) {
			throw new RuntimeException(e);		
		}
	}



}
