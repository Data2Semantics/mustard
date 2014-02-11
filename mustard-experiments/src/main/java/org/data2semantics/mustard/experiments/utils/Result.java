package org.data2semantics.mustard.experiments.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

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

}
