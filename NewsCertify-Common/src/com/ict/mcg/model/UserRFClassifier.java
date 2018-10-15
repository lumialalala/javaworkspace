package com.ict.mcg.model;

import java.io.File;



import com.ict.mcg.util.FileIO;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class UserRFClassifier {
	private static String[] options = {"-I","100", "-K","0", "-S","1", "-depth", "5"};
	private static String[] trainFiles = {"User_RF_bagging0.arff", "User_RF_bagging1.arff"};
	private static String rootPath = null;
	private Instances[] trainSets;
	private RandomForest[] randomForests;
	private static UserRFClassifier instance = null;
	
	public static UserRFClassifier getInstance(String rootPath) throws Exception {
		UserRFClassifier.rootPath = rootPath;
		if (instance == null) {
			instance = new UserRFClassifier();
		}
		return instance;
	}
	
	//用户可信度模型
	private UserRFClassifier() throws Exception {
		//load model
//		System.out.println(this.getClass().getPath());
		trainSets = new Instances[trainFiles.length];
		randomForests = new RandomForest[trainFiles.length];
		for (int i = 0; i < trainFiles.length; ++i) {
			String arffFile = rootPath + trainFiles[i];
			System.out.println(arffFile);
			ArffLoader loader = new ArffLoader();
			loader.setFile(new File(arffFile));
			trainSets[i] = loader.getDataSet();
			trainSets[i].setClassIndex(trainSets[i].numAttributes() - 1);
			
			randomForests[i] = new RandomForest();
			randomForests[i].setOptions(options);
			randomForests[i].buildClassifier(trainSets[i]);
		}
		
	}
	
	public double predict(double[] inputs) throws Exception {
		
		
		Instance inst = new Instance(inputs.length);
		for (int i = 0; i < inputs.length; ++i) {
			inst.setValue(i, inputs[i]);
		}
		inst.setDataset(trainSets[0]);
		double pros = 0.0;
		for (int i = 0; i < randomForests.length; ++i) {
			double pro = randomForests[i].distributionForInstance(inst)[1];
//			double pro = randomForests[i].classifyInstance(inst);
			System.out.println("User_RF "+i+" 输出："+ pro);
			if(i == 0)
				pros += 5 * pro;
			else
				pros += 3 * pro;
		}
		pros = pros / 8;
		
		return pros;
	}
	
	public void test(String testFile) throws Exception {
		ArffLoader loader = new ArffLoader();
		loader.setFile(new File(testFile));
		
		Instances testSet= loader.getDataSet();
		testSet.setClassIndex(testSet.numAttributes()-1);
		
		Evaluation testingEvaluation = new Evaluation(testSet);
		int length = testSet.numInstances();
		Instance testInst;
		
		for (int k = 0; k < randomForests.length; ++k) {
			for (int i =0; i < length; i++) {
			    testInst = testSet.instance(i);
			    testingEvaluation.evaluateModelOnceAndRecordPrediction(
			    		randomForests[k], testInst);
			}
			System.out.println(k+ "分类器的正确率：" + (1- testingEvaluation.errorRate()));
		}
		
		for (int i =0; i < length; i++) {
			testInst = testSet.instance(i);
			double res[] = randomForests[0].distributionForInstance(testInst);
//			System.out.println(testInst.classValue() +"\t"+res[0]+"\t"+res[1]);
			System.out.println(res[1]);
		}
	}
	
	public static void main(String[] args) {
		UserRFClassifier rfc = null;
		try {
			rfc = UserRFClassifier.getInstance("./src/resources/");
//			File file = new File("./src/resources/");
//		    File[] tempList = file.listFiles();
//		    for(File tem: tempList){
//		    	System.out.println(tem.toString());
//		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String testFile = "D:/user/zjq/dataset/SW-SYSTEM/newLabeled/all_newfeature_select2.arff";
//		String testFile = "D:/user/zjq/dataset/SW-SYSTEM/newLabeled/train_bagging/RF_bagging1.arff";
//		String testFile = "D:/user/zjq/dataset/SW-SYSTEM/newLabeled/RF_bagging/new_add_attrSel.arff";
		double [] input = new double[]{5,0.4,0,0.6,0.8,5162644.2,0.2,0.2,0,0,0.2,0.4,457.8,0,0.4,0.4,0.2,0.8,1,0.4,0.2,0,0,0.4,0,29341.2,0.6,0,0.4};
//		for (int i = 0; i < 19; ++i) {
//			input[i] = 0.1;
//		}
		
		try {
			System.out.println(rfc.predict(input));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
//		try {
//			rfc.test(testFile);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		
	}

}
