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
	private Map<String, Map<Integer, List<Result>>> combinedMap;

	public CombinedResults() {
		combinedMap = new TreeMap<String, Map<Integer, List<Result>>>();
	}

	public void readDirectory(String dir) {
		try {
			File fileDir = new File(dir);

			for (File file : fileDir.listFiles(new ResultsFilter())) {
				BufferedReader read = new BufferedReader(new FileReader(file));
				String readLine = read.readLine();
				List<Result> results = new ArrayList<Result>();
				String id = readLine.split(":")[0];
				Integer seed = Integer.parseInt(readLine.split(":")[1]);

				if (!combinedMap.containsKey(id)) {
					combinedMap.put(id, new TreeMap<Integer,List<Result>>());
				}
				combinedMap.get(id).put(seed, results);

				readLine = read.readLine();

				while (readLine != null) {
					results.add(new Result(readLine));
					readLine = read.readLine();
				}
				read.close();		
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public ResultsTable generateTable() {
		ResultsTable table = new ResultsTable();

		for (String id : combinedMap.keySet()) {
			table.newRow(id);
			for (int seed : combinedMap.get(id).keySet()) {
				for (Result res : combinedMap.get(id).get(seed)) {
					table.addResult(res);
				}
			}
		}
		return table;
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
		res.readDirectory(".");
		System.out.println(res.generateTable());

	}



}
