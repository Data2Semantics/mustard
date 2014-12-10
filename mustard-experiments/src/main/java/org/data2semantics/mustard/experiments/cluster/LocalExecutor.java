package org.data2semantics.mustard.experiments.cluster;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class LocalExecutor {
	private static final String parmsFile = "C:\\Users\\Gerben\\git\\mustard\\mustard-experiments\\aff_parms.txt";
	private static final String prefix = "-dataset AIFB -file datasets/aifb-fixed_complete.n3";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		List<String> lines = new ArrayList<String>();
		
		try {
			BufferedReader in = new BufferedReader(new FileReader(parmsFile));
			String line = in.readLine();
			while (line != null) {
				lines.add(line);
				line = in.readLine();
			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		
		SimpleScheduler sched = new SimpleScheduler(4);
		
		for (int i = 0; i < lines.size(); ) {
			String fullStr = prefix + " " + lines.get(i);
					
			if (sched.freeSlot()) {
				sched.addJob(new Thread(new Exp(fullStr.split(" "))));
				i++;
			} else {
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	
	private static class Exp implements Runnable {
		private String[] args;
		
		public Exp(String[] args) {
			this.args = args;
		}
		
		public void run() {
			StringBuilder str = new StringBuilder();
			for (String arg : args) {
				str.append(arg);
				str.append(" ");
			}
			System.out.println("Running: " + str.toString());
			ClusterExperiment.main(args);
		}
	}
	
	private static class SimpleScheduler {
		private Thread[] jobs;
		
		public SimpleScheduler(int size) {
			jobs = new Thread[size];
		}
		
		public void addJob(Thread job) {
			for (int i = 0; i < jobs.length; i++) {
				if (jobs[i] == null || !jobs[i].isAlive()) {
					jobs[i] = job;
					job.setDaemon(false);
					job.run();
					break;
				}
			}
		}
		
		public boolean freeSlot() {
			for (int i = 0; i < jobs.length; i++) {
				if (jobs[i] == null || !jobs[i].isAlive()) {
					return true;
				}
			}
			return false;
		}
	}
	
}