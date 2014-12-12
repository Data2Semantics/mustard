package org.data2semantics.mustard.experiments.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.inference.MannWhitneyUTest;
import org.apache.commons.math3.stat.inference.TTest;
import org.apache.commons.math3.stat.inference.WilcoxonSignedRankTest;

public class ResultsTable implements Serializable {

	private static final long serialVersionUID = -22861721525531471L;
	private List<List<Result>> table;
	private List<String> rowLabels;
	private List<Result> compRes;
	private boolean showStdDev;
	private boolean latex;
	private double pValue;
	private int digits;

	private SigTest significanceTest;
	
	
	public ResultsTable() {
		table = new ArrayList<List<Result>>();
		rowLabels = new ArrayList<String>();
		compRes = new ArrayList<Result>();
		significanceTest = SigTest.TTEST;
		showStdDev = false;
		latex = false;
		pValue = 0.05;
		digits = 2;
	}

	public void newRow(String rowLabel) {
		rowLabels.add(rowLabel);
		table.add(new ArrayList<Result>());
	}

	public void addResult(Result result) {
		table.get(table.size()-1).add(result);
	}

	public void addCompResult(Result res) {
		compRes.add(res);
	}

	public void addCompResults(List<Result> res) {
		compRes.addAll(res);
	}

	public String formatScore(double score) {
		return String.format("%1." + digits + "f", score);
		// Double.toString((Math.round(score * Math.pow(10, digits))) / Math.pow(10, digits))
	}

	@Override
	public String toString() {
		StringBuffer tableStr = new StringBuffer();		

		Map<String, Integer> compCount = new HashMap<String,Integer>();
		
		for (Result comp : compRes) {
			if (!compCount.containsKey(comp.getLabel())) {
				compCount.put(comp.getLabel(), 1);
			} else {
				compCount.put(comp.getLabel(), compCount.get(comp.getLabel())+1);
			}
		}
		List<Integer> counts = new ArrayList<Integer>(compCount.values());
		Collections.sort(counts);
		
		int maxTests = counts.isEmpty() ? 0 : counts.get(counts.size()-1);
		
		
		if(table.size() > 0) {

			List<Result> row1 = table.get(0);

			String signif = "";

			for(Result res : row1) {
				tableStr.append(res.getLabel());
				tableStr.append(" \t ");
			}
			tableStr.append("\n");

			for (int i = 0; i < table.size(); i++) {
				for (Result res : table.get(i)) {

					signif = "";
					int j = 0;
					int suc = 0;
					for (Result comp : compRes) {
						j++;
						if (comp.getLabel().equals(res.getLabel())) {
							if (comp.getScores().length > 1 && !signifTest(comp.getScores(), res.getScores())) {
								signif += "^"+j;
								suc++;
							} else {
								signif += "  ";
							}
						}
					}

					if (latex) {
						tableStr.append(" & ");
					}
					
					if (latex) {
						if (suc == maxTests) {
							tableStr.append("$\\textbf{" + formatScore(res.getScore()));
						}
						else if (suc == maxTests-1 && suc != 0) {
							tableStr.append("$\\textit{" + formatScore(res.getScore()));
						} else if (suc != 0){
							tableStr.append("$\\textrm{" + formatScore(res.getScore()) + signif);
						} else {
							tableStr.append("$\\textrm{" + formatScore(res.getScore()));
						}
						
						if (showStdDev) {
							tableStr.append("("+formatScore(res.getStdDev())+")");	
						}		
						tableStr.append("}$");
						
						
					} else {				
						tableStr.append(formatScore(res.getScore()) + signif);
						if (showStdDev) {
							tableStr.append("("+formatScore(res.getStdDev())+")");	
						}
					}
									
					tableStr.append(" \t ");
				}
				
				if (latex) {
					tableStr.append(" \\\\ ");
				}
				
				tableStr.append(rowLabels.get(i));
				tableStr.append("\n");
			}
		}
		return tableStr.toString();		
	}


	public String allScoresToString() {
		StringBuffer tableStr = new StringBuffer();
		if (table.size() > 0) {

			List<Result> row1 = table.get(0);

			for(Result res : row1) {
				tableStr.append(res.getLabel());
				tableStr.append(" \t ");
			}
			tableStr.append("\n");

			for (int i = 0; i < table.size(); i++) {
				for (Result res : table.get(i)) {
					tableStr.append(Arrays.toString(res.getScores()));
					tableStr.append(" \t ");
				}
				tableStr.append(rowLabels.get(i));
				tableStr.append("\n");
			}
		}
		return tableStr.toString();		
	}

	public List<Result> getBestResults() {
		return getBestResults(new ArrayList<Result>());
	}

	public List<Result> getBestResults(List<Result> bestResults) {

		for (int i = 0; i < table.size(); i++) {
			for (Result res : table.get(i)) {

				boolean newType = true;
				for (Result bestRes : bestResults) {
					if (res.getLabel().equals(bestRes.getLabel())) {
						newType = false;
						if (res.isBetterThan(bestRes)) {
							bestResults.remove(bestRes);
							bestResults.add(res);
							break;
						}
					}
				}
				if (newType) {
					bestResults.add(res);
				}
			}
		}
		return bestResults;
	}
	
	@Deprecated
	public void setTTest(double pValue) {
		significanceTest = SigTest.TTEST;
		this.pValue = pValue;
	}
	
	@Deprecated
	public void setManWU(double pValue) {
		significanceTest = SigTest.MANN_WHITNEY_U;
		this.pValue = pValue;
	}
	
	public void setDigits(int digits) {
		this.digits = digits;
	}
		
	public void setpValue(double pValue) {
		this.pValue = pValue;
	}

	public void setShowStdDev(boolean showStdDev) {
		this.showStdDev = showStdDev;
	}
	
	public void setLatex(boolean latex) {
		this.latex = latex;
	}

	public void setSignificanceTest(SigTest significanceTest) {
		this.significanceTest = significanceTest;
	}

	private boolean signifTest(double[] s1, double[] s2) {
		if (significanceTest == SigTest.TTEST) {
			TTest ttest = new TTest();
			return ttest.tTest(s1, s2, pValue);

		} 
		if (significanceTest == SigTest.PAIRED_TTEST) {
			TTest ttest = new TTest();
			return ttest.pairedTTest(s1, s2, pValue);

		}
		if (significanceTest == SigTest.MANN_WHITNEY_U) {
			MannWhitneyUTest mwuTest = new MannWhitneyUTest();
			return mwuTest.mannWhitneyUTest(s1, s2) < pValue;
		}
		if (significanceTest == SigTest.WILCOXON_SIGNED_RANK) {
			WilcoxonSignedRankTest wsrTest = new WilcoxonSignedRankTest();
			return wsrTest.wilcoxonSignedRankTest(s1, s2, true) < pValue;
		}		
		return false;
	}
	
	public enum SigTest {
		TTEST, PAIRED_TTEST, MANN_WHITNEY_U, WILCOXON_SIGNED_RANK;
	}
}
