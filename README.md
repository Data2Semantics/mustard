mustard - Machine learning Using Svms To Analyse Rdf Data
=========================================================

Version 0.1.0

Mustard is a machine learning library for learning from RDF data using kernel methods. Currently the library itself supports Support Vector Machines via the Java versions of the LibSVM and LibLINEAR libraries. Using mustard, classifiers can be learned for a set of nodes (i.e. resources, typically from the same rdfs:class) in an RDF graph/dataset. 

This repository consists of 4 projects.

- `mustard-kernels` is the heart of the library. It contains the graph kernels for RDF and the additional classes needed to handle RDF. This part of the library is the most well maintained and documented.
- `mustard-learners` wraps LibSVM and LibLINEAR for use with the kernels defined in `mustard-kernels`. Together with `mustard-kernels` you can build SVM classifiers for RDF data.
- `mustard-experiments` contains experiments and utility classes for experimenting with `mustard`.
- `mustard-ducktape-experiments` contains classes to perform experiments with `mustard` using `ducktape`, which is also located in de Data2Semantics github. This part of the library is hardly maintained at the moment.

Contents
--------
1. Usage
2. Kernel documentation
3. LOD extension
4. Dependencies
5. JWS paper

1. Usage
--------
The `mustard-kernels` and `mustard-learners` project can be used together to build classifiers for RDF datasets. Examples of how this works in an experimental context can be found in `mustard-experiments`. Note that the library is currently mainly used for experimental comparisons between algorithms. 

The general set up to build a classifier is as follows. Note that there are far more options and possibilities, the given example only contains the bare essentials.

First, we need a dataset, for which we use the `RDFDataSet` class. Obviously other filetypes than N3 are also supported.
```java
RDFDataSet tripleStore = new RDFFileDataSet("some_filename.n3", RDFFormat.N3);
```

The instances that we want to build a classifier for are nodes/resources in this RDF graph. This list of instances can be supplied externally, or they can be extracted from the RDF, for example as shown below, where all resources that are the subject of `some_relation` are extracted. Note that the object of this relation is used as a label.
```java
List<Statement> stmts = tripleStore.getStatements(null, some_relation, null);

List<Resource> instances = new ArrayList<Resource>();
List<Value> labels 	 = new ArrayList<Value>();

for (Statement stmt : stmts) {
	instances.add(stmt.getSubject());
	labels.add(stmt.getObject());
}
```
In a real-world classification task, the labels are typically only known for a part of the instances, i.e. the trainset. For the instances for which this label is unknown (i.e. the testset) we want to predict it. In our running example we know the label for all the instances.

After the instances we likely also need a blacklist, which is a list of statements that should be ignored. Typically, we need this because these statements include the actual labels of the instances, which we do not want in the training (RDF) graph. There is a simple utility method for this.

*NOTE 25/04/2017* - This method can be very dangerous if labels are primitives (like a boolean, int, etc.), since it puts any relation between the instance and its label on the blacklist (and also inverse relations). 
```java
List<Statement> blackList = DataSetUtils.createBlacklist(tripleStore, instances, labels);
```

With these object we can create an `RDFData` object, which can be used to compute a kernel matrix, as an example we take the `WLSubTreeKernel`, with some example parameters.
```java
RDFData data = new RDFData(tripleStore, instances, blackList);
GraphKernel<RDFData> kernel = new RDFWLSubTreeKernel(4,2,true,true);
double[][] matrix = kernel.compute(data);
```

This kernel matrix can be used to train a Support Vector Machine (SVM) classifier) as follows.
```java
Map<Value, Double> labelMap = new HashMap<Value,Double>();
List<Double> target = EvaluationUtils.createTarget(labels, labelMap); // create a training target for LibSVM

double[] cs = {1,10,100,1000};	// C values to optimize the SVM over
LibSVMParameters svmParms = new LibSVMParameters(LibSVMParameters.C_SVC, cs);
LibSVMModel model = LibSVM.trainSVMModel(matrix, target, svmParms);
```

This `model` can be used to make predictions using `LibSVM.testSVMModel()`. In a real-world scenario where not all the labels are known, the `KernelUtils` class contains utility functions to extract a train and test kernel matrix from a full  kernel matrix. This is needed because the kernel needs to be computed for both the training and test instances in one go. However, afterwards we need to get the part of the matrix that belongs to the trainset and the part that belongs to the testset.



2. Kernel documentation
-----------------------
Please see `/src/main/java/org/data2semantics/mustard/kernels/graphkernels/package-info.java` for the main documentation on the different graph kernels defined. Furthermore, see the Journal of Web Semantics 2015 paper: [“Substructure Counting Graph Kernels for Machine Learning from RDF data”, GKD de Vries, S de Rooij](http://www.sciencedirect.com/science/article/pii/S1570826815000657), for a detailed explanation and analysis of a large number of the kernels in this library.


3. LOD extension
----------------
Part of the kernels available in this library are also available in the Linked Open Data extension, developed at the University of Mannheim, for the popular RapidMiner data mining software. See <http://dws.informatik.uni-mannheim.de/en/research/rapidminer-lod-extension/> and <http://dws.informatik.uni-mannheim.de/en/research/rapidminer-lod-extension/rapid-miner-lod-extension-example-using-kernels-for-feature-generation/>.

4. Dependencies
---------------
Mustard depends on the `nodes` graph library which is part of the Data2Semantics github. Furtermore, it depends on the SESAME triplestore (<http://rdf4j.org/>) and `mustard-learners` depends on the Java version of LibLINEAR (<https://github.com/bwaldvogel/liblinear-java/>).

All the 4 projects are Eclipse projects and congfigured using Maven, which takes care of the dependencies. The first 2 projects (`mustard-kernels` and `mustard-learners`) are submodules of one maven parent project (see the `pom.xml` in the root dir of this project). If you want to use them without Maven then you should make sure that the correct JAR's are on the classpath.

If you want to use `mustard-kernels` and `mustard-learners` (or the `nodes` project) as Maven artifacts, then this can easily be achieved using the excellent JitPack service (<https://jitpack.io/>). Please see their documentation on multi module maven projects.


5. JWS paper
------------
This library was used for the Journal of Web Semantics 2015 paper: [“Substructure Counting Graph Kernels for Machine Learning from RDF data”, GKD de Vries, S de Rooij](http://www.sciencedirect.com/science/article/pii/S1570826815000657).


We used the following datasets in our experiments:
  - The AIFB research portal (<http://figshare.com/articles/AIFB_DataSet/745364>)
  - The British Geological Survey (<http://figshare.com/articles/British_Geological_Survey/745365>)
  - Amsterdam Museum (<http://datahub.io/dataset/amsterdam-museum-as-edm-lod>)
  - DL Learner Mutagenic (<http://dl-learner.org/community/carcinogenesis/>)


Most of the experiments were run on a computing cluster. Using the `org.data2semantics.mustard.experiments.cluster.LocalExecutor.java` class the experiments can be rerun on a single machine. For the small experiments different settings are given as String constants in the top of the class. To rerun the large experiments, parameter files and susbsets of the datasets have to be created with `ParamsCreator.java` and `SubSetCreator.java`. Both of these are configured with some parameter settings at the top of the main() methods.

The computation time experiments can be found in `org.data2semantics.mustard.experiments.JWS2015`. In the top of the file a String constant sets the location of the dataset.


Acknowledgements
----------------
This library was developed in the context of the Data2Semantics project, part of the Dutch national project COMMIT/.
