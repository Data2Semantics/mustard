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

import org.data2semantics.mustard.kernels.KernelUtils;
import org.data2semantics.mustard.kernels.data.RDFData;
import org.data2semantics.mustard.kernels.graphkernels.FeatureVectorKernel;
import org.data2semantics.mustard.kernels.graphkernels.GraphKernel;
import org.data2semantics.mustard.learners.SparseVector;
import org.data2semantics.mustard.rdf.RDFUtils;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

public class RESCALKernel implements GraphKernel<RDFData>, FeatureVectorKernel<RDFData> {
	public static final String PYTHON_EXE = "C:\\Users\\Gerben\\Dropbox\\D2S\\python_stuff\\WinPython-64bit-2.7.6.4\\python-2.7.6.amd64\\python.exe";
	public static final String RESCAL_DIR = "C:\\Users\\Gerben\\Dropbox\\D2S\\python_stuff\\Ext-RESCAL-master";

	private String outputDir;
	private double lambda;
	private int numLatent;
	private boolean normalize;
	private String label;
	private int depth;
	private boolean inference;

	public RESCALKernel(String outputDir, double lambda, int numLatent, int depth, boolean inference, boolean normalize) {
		super();
		this.outputDir = outputDir;
		this.lambda = lambda;
		this.numLatent = numLatent;
		this.normalize = normalize;
		this.depth = depth;
		this.inference = inference;
		this.label = "RESCAL kernel, " + numLatent + " " + lambda + " " + depth + " " + inference;

	}

	public String getLabel() {
		return label;
	}

	public void setNormalize(boolean normalize) {
		this.normalize = normalize;
	}



	public SparseVector[] computeFeatureVectors(RDFData data) {

		RESCALInput rc = new RESCALInput();
		List<Statement> all = null;
		if (depth == 0) {
			all = data.getDataset().getFullGraph(inference);
		} else {
			all = new ArrayList<Statement>(RDFUtils.getStatements4Depth(data.getDataset(), data.getInstances(), depth, inference));
		}

		all.removeAll(data.getBlackList());
		rc.convert(all);
		rc.save(outputDir);

		Runtime rt = Runtime.getRuntime();
		try {
			List<String> cmdList = new ArrayList<String>();
	
			cmdList.add(PYTHON_EXE);
			cmdList.add(RESCAL_DIR + "\\extrescal.py");
			cmdList.add("--latent");
			cmdList.add("" + numLatent);
			cmdList.add("--lmbda");
			cmdList.add("" + lambda);
			cmdList.add("--input");
			cmdList.add(".");
			cmdList.add("--outputentities");
			cmdList.add("entity.embeddings.csv");
			cmdList.add("--outputterms");
			cmdList.add("term.embeddings.csv");
			cmdList.add("--outputfactors");
			cmdList.add("latent.factors.csv");
			cmdList.add("--log");
			cmdList.add("extrescal.log");
			
			ProcessBuilder pb = new ProcessBuilder(cmdList);
			pb.directory(new File(outputDir));
			pb.redirectErrorStream(true);			
			Process proc = pb.start();
			
			Thread hand = new Thread(new StreamHandler(proc.getInputStream()));
			hand.start();
		
			proc.waitFor();

		} catch (Exception e) {
			e.printStackTrace();
		}

		SparseVector[] fv = null; 
		
		
		if (normalize) {
			fv = KernelUtils.normalize(fv);
		}
		return fv;
	}

	public double[][] compute(RDFData data) {
		double[][] kernel = KernelUtils.initMatrix(data.getInstances().size(), data.getInstances().size());
		KernelUtils.computeKernelMatrix(computeFeatureVectors(data), kernel);				
		return kernel;
	}

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
