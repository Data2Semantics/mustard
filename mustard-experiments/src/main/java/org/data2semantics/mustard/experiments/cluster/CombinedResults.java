package org.data2semantics.mustard.experiments.cluster;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.data2semantics.mustard.experiments.utils.Result;
import org.data2semantics.mustard.experiments.utils.ResultsTable;

public class CombinedResults {
	private Map<String, Map<String, Map<Integer, List<Result>>>> combinedMap;

	public CombinedResults() {
		combinedMap = new TreeMap<String, Map<String, Map<Integer, List<Result>>>>();
	}

	public void readDirectory(String dir) {
		try {
			File fileDir = new File(dir);

			for (File file : fileDir.listFiles(new ResultsFilter())) {
				BufferedReader read = new BufferedReader(new FileReader(file));
				String readLine = read.readLine();
				String id = readLine.split(":")[0];
				Integer seed = Integer.parseInt(readLine.split(":")[1]);

				if (!combinedMap.containsKey(id)) {
					combinedMap.put(id, new TreeMap<String, Map<Integer,List<Result>>>());
				}
				readLine = read.readLine();

				while (readLine != null) {
					Result res = new Result(readLine);

					if (!combinedMap.get(id).containsKey(res.getLabel())) {
						combinedMap.get(id).put(res.getLabel(), new TreeMap<Integer, List<Result>>());
					}
					if (!combinedMap.get(id).get(res.getLabel()).containsKey(seed)) {
						combinedMap.get(id).get(res.getLabel()).put(seed, new ArrayList<Result>());
					}
					List<Result> results = combinedMap.get(id).get(res.getLabel()).get(seed);
					results.add(res);
		
					if (res.getLabel().equals("Accuracy")) {
						res.setHigherIsBetter(true);
					}
					if (res.getLabel().equals("F1")) {
						res.setHigherIsBetter(true);
					}
					readLine = read.readLine();
				}
				read.close();		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, ResultsTable> generateTables(int splits) {
		Map<String, ResultsTable> tables = new TreeMap<String, ResultsTable>();
		
		for (String id : combinedMap.keySet()) {
			String[] idSplits = id.split("_");
			String tableId = "";
			for (int i = (idSplits.length - splits); i < idSplits.length; i++) {
				tableId += idSplits[i];
			}
			
			if (!tables.containsKey(tableId)) {
				tables.put(tableId, new ResultsTable());
			}
			ResultsTable table = tables.get(tableId);
			
			table.newRow(id);
			for (String label : combinedMap.get(id).keySet()) {
				Result res = null;
				for (int seed : combinedMap.get(id).get(label).keySet()) {
					for (Result result : combinedMap.get(id).get(label).get(seed)) {
						if (res == null) {
							res = result;
							table.addResult(res);
						} else {
							res.addResult(result);
						}
					}
				}
			}
		}
		return tables;
	}

	class ResultsFilter implements FileFilter {

		public boolean accept(File file) {
			if (file.getName().endsWith(".result")) {
				return true;
			}
			return false;
		}
	}




	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CombinedResults res = new CombinedResults();

		//res.readDirectory("am_results_hubs");
		
		res.readDirectory("C:\\Users\\Gerben\\Dropbox\\D2S\\workspace_TeX\\JWS\\results_bgs_hubs");
		Map<String, ResultsTable> tables = res.generateTables(3);
		
		CombinedResults res2 = new CombinedResults();
		
		res2.readDirectory("C:\\Users\\Gerben\\Dropbox\\D2S\\workspace_TeX\\JWS\\results_bgs");
		//Map<String, ResultsTable> tables2 = null;
		Map<String, ResultsTable> tables2 = res2.generateTables(3);
		
		
		List<Result> overallBest = new ArrayList<Result>();
		
		for (String key : tables.keySet()) {
			overallBest = tables.get(key).getBestResults(overallBest); // update overall best results
		}
		
		for (String key : tables.keySet()) {
			tables.get(key).addCompResults(overallBest); // add overall best results
			tables.get(key).addCompResults(tables.get(key).getBestResults()); // add local best results
			tables.get(key).setSignificanceTest(ResultsTable.SigTest.PAIRED_TTEST);
			tables.get(key).setDigits(2);
			tables.get(key).setShowStdDev(true);
			tables.get(key).setLatex(true);
			//labelless hack
			//String key2 = key.substring(0, key.length()-4) + "false";
			
			if (tables2 != null) {
				tables.get(key).setCompareTable(tables2.get(key));
			}
			
			System.out.println(tables.get(key));
		}
	}
}
