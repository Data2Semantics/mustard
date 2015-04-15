package org.data2semantics.mustard.experiments.rescal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.data2semantics.mustard.kernels.SparseVector;
import org.openrdf.model.Resource;



public class RESCALWrapper {
	private String pythonDir;
	private String rescalDir;
	private String workingDir;
	
	public RESCALWrapper(String pythonDir, String rescalDir, String workingDir) {
		this.pythonDir = pythonDir;
		this.rescalDir = rescalDir;
		this.workingDir = workingDir;
	}
	
	/**
	 * Factorize the RDF graph given as RESCALInput
	 * 
	 * 
	 * @param numFactors, number of latent factors
	 * @param lambda, lambda parameter
	 */
	public RESCALOutput factorize(RESCALInput in, int numFactors, double lambda) {		
		RESCALOutput out = new RESCALOutput();	
		
		try {			
			in.save(rescalDir + "/" + workingDir);
			
			List<String> cmdList = new ArrayList<String>();
	
			cmdList.add(pythonDir);
			cmdList.add(rescalDir + "/extrescal.py");
			cmdList.add("--latent");
			cmdList.add("" + numFactors);
			cmdList.add("--lmbda");
			cmdList.add("" + lambda);
			cmdList.add("--input");
			cmdList.add(workingDir);
			cmdList.add("--outputentities");
			cmdList.add(workingDir + "/entity.embeddings.csv");
			cmdList.add("--outputterms");
			cmdList.add(workingDir + "/attribute.embeddings.csv");
			cmdList.add("--outputfactors");
			cmdList.add(workingDir + "/latent.factors.csv");
			cmdList.add("--log");
			cmdList.add(workingDir + "/extrescal.log");
			
			ProcessBuilder pb = new ProcessBuilder(cmdList);
			pb.directory(new File(rescalDir));
			pb.redirectErrorStream(true);			
			Process proc = pb.start();
			
			Thread hand = new Thread(new StreamHandler(proc.getInputStream()));
			hand.start();
		
			proc.waitFor();
			
			out.loadEntityEmb(rescalDir + "/" + workingDir + "/entity.embeddings.csv", in.getInvEntityMap());
			out.loadAttributeEmb(rescalDir + "/" + workingDir + "/attribute.embeddings.csv", in.getInvAttributeMap());
			out.loadLatentFac(rescalDir + "/" + workingDir + "/latent.factors.csv", in.getInvRelationMap());
			out.loadAdditionalLatentFac(rescalDir + "/" + workingDir + "/latent.factors.csv", in.getInvAdditionalSliceMap(), in.getInvRelationMap().size());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}	
		return out;
	}
	
	/*
	private SparseVector[] readInstanceEmbeddings(List<Resource> instances, Map<Integer, Resource> invEntityMap) {
		Map<Resource, Integer> instanceMap = new HashMap<Resource, Integer>();
		for (int i = 0; i < instances.size(); i++) {
			instanceMap.put(instances.get(i), i);
		}
		SparseVector[] fv = new SparseVector[instances.size()];

		try {
			BufferedReader fr = new BufferedReader(new FileReader(outputDir + "/entity.embeddings.csv"));
			String line;

			for (int i = 0; (line = fr.readLine()) != null; i++) {
				if (instanceMap.containsKey(invEntityMap.get(i))) {
					int index = instanceMap.get(invEntityMap.get(i));
					fv[index] = new SparseVector();
					int j = 0;
					for (String val : line.split(" ")) {
						fv[index].setValue(j, Double.parseDouble(val));
						j++;
					}
				}
			}
			fr.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return fv;
	}
	*/
	
	
	class StreamHandler implements Runnable {
		InputStream in;
		
		public StreamHandler(InputStream in) {
			this.in = in;
		}
				
		public void run() {
			try {
	            InputStreamReader isr = new InputStreamReader(in);
	            BufferedReader br = new BufferedReader(isr);
	            
				String line = null;
				while((line = br.readLine()) != null) {
					System.out.println(line);
				}		
				
			} catch (Exception e) {
				e.printStackTrace();
			}		
		}	
	}
	
}
