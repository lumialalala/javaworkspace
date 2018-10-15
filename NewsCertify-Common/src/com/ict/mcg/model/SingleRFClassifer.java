package com.ict.mcg.model;

//import com.ict.mcg.model.RFClassifier;

import java.io.File;
import java.io.IOException;

import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class SingleRFClassifer {
	private static String[] options = {"-I","100", "-K","0", "-depth","5"};
	private static String trainFile = "Single_RF_bagging.arff";
	private static String rootPath = "./file/3day/";
	private Instances trainSets;
	private RandomForest randomForest;
	private static SingleRFClassifer classifier = null;
	
	public static SingleRFClassifer getInstance(String rootpath){
		if(classifier == null){
			rootPath = rootpath;
			classifier = new SingleRFClassifer();
		}
		
		return classifier;
	}
	
	public static SingleRFClassifer getInstance(){
		if(classifier == null){
			classifier = new SingleRFClassifer();
		}
		
		return classifier;
	}
	
	private SingleRFClassifer(){
		try{
			String trainpath = rootPath+trainFile;
			ArffLoader loader = new ArffLoader();
			loader.setFile(new File(trainpath));
			trainSets = loader.getDataSet();
			trainSets.setClassIndex(trainSets.numAttributes()-1);
			
//			System.out.println(trainSets.numAttributes());
			randomForest = new RandomForest();
			randomForest.setOptions(options);
			randomForest.buildClassifier(trainSets);
		}catch(Exception e){
			e.printStackTrace();
		}
	}
		
	public double predict(double[]inputs) throws Exception{
		Instance inst =  new Instance(inputs.length);
		for(int i = 0; i < inputs.length; i++){
			inst.setValue(i,inputs[i]);
		}
		inst.setDataset(trainSets);
		double pro = randomForest.distributionForInstance(inst)[0];
		return pro;
	}
	
	public static void main(String []args){
		SingleRFClassifer srfc = null;
		try {
			srfc = new SingleRFClassifer();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		double []input = new double[]{120.0,79.0,3.0,-0.2311111111111111,4.0,0.0,0.0,3.0,0.0,8.0,1.0,1.0,27.0,133.0,193.0,0.0,1.0,1.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,0.0,7.0,5.0,0.0};
//		System.out.println(input[0]+"\t"+input[1]+"\t"+input[2]);
		try {
			double pro = srfc.predict(input);
			System.out.println(pro);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
//	private static RFClassifier instance = null;
}
