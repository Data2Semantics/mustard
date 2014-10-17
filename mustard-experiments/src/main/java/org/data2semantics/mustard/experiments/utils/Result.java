package org.data2semantics.mustard.experiments.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.StatUtils;
import org.data2semantics.mustard.learners.evaluation.EvaluationFunction;

public class Result implements Serializable {
	private static final long serialVersionUID = 1809158002144017691L;
	
	private boolean higherIsBetter;
	private double[] scores;
	private String label;
	private EvaluationFunction eval;

	public Result() {
		this.scores = null;
		this.label = "Empty Result";
		this.higherIsBetter = true;

	}
	
	public Result(EvaluationFunction eval) {
		this();
		this.eval = eval;
		this.label = eval.getLabel();
		this.higherIsBetter = eval.isHigherIsBetter();
	}

	public Result(double[] scores, String label) {
		this();
		this.scores = scores;
		this.label = label;
	}
	
	public Result(String input) {
		label = input.split(":")[0];
		String vals = input.split(":")[1];
		vals = vals.replace("[", "");
		vals = vals.replace("]", "");
		String[] vals2 = vals.split(",");
		scores = new double[vals2.length];
		
		for (int i = 0; i < vals2.length; i++) {
			scores[i] = Double.parseDouble(vals2[i]);
		}
	}
	
	public void addResult(Result res) {
		if (this.scores == null) {
			this.scores = res.getScores();
			this.label = res.getLabel();
		} else {
			double[] newScores = new double[scores.length + res.getScores().length];
			for(int i = 0; i < scores.length; i++) {
				newScores[i] = scores[i];
			}
			double[] scores2 = res.getScores();
			for(int i = 0; i < scores2.length; i++) {
				newScores[i + scores.length] = scores2[i];
			}
			this.scores = newScores;
		}
	}

	public double[] getScores() {
		return scores;
	}

	public void setScores(double[] scores) {
		this.scores = scores;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
	
	public EvaluationFunction getEval() {
		return eval;
	}

	public void setEval(EvaluationFunction eval) {
		this.eval = eval;
		this.higherIsBetter = eval.isHigherIsBetter();
	}

	public boolean isHigherIsBetter() {
		return higherIsBetter;
	}

	public void setHigherIsBetter(boolean higherIsBetter) {
		this.higherIsBetter = higherIsBetter;
	}

	public double getScore() {
		double total = 0;
		for (double score : scores) {
			total += score;
		}
		return total / scores.length;
	}
	
	public double getStdDev() {
		return Math.sqrt(StatUtils.variance(scores));
	}

	public static List<Result> mergeResultLists(List<List<Result>> results) {
		List<Result> newRes = new ArrayList<Result>();

		for (int i = 0; i < results.size(); i++) {
			for (int j = 0; j < results.get(i).size(); j++) {
				if (i == 0) {
					Result res = new Result();
					res.addResult(results.get(i).get(j));
					newRes.add(res);
				} else {
					newRes.get(j).addResult(results.get(i).get(j));
				}
			}
		}
		return newRes;
	}
	
	public boolean isBetterThan(Result res) {
		if (this.higherIsBetter) {
			return getScore() > res.getScore();
		} else {
			return getScore() < res.getScore();
		}
	}
	
	public String toString() {
		return label + ":"  + Arrays.toString(getScores());
	}

}
