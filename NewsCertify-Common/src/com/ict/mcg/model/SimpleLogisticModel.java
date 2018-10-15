package com.ict.mcg.model;

import java.io.File;

import org.apache.log4j.Logger;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.SimpleLogistic;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

import com.ict.mcg.util.FileIO;


/**
 * boosting logistic模型，将svm，内容，人物，用户，传播四个模块打分融合
 * @author dell
 *
 */
public class SimpleLogisticModel {
	private SimpleLogistic[] simpleLogistics;
	private Instances[] trainSets;
	private static String[] trainFiles = { "0.arff", "1.arff", "2.arff"};
	private static String rootPath = null;

	private static SimpleLogisticModel instance = new SimpleLogisticModel();
	
	public static SimpleLogisticModel getInstance(String rootPath) {
		SimpleLogisticModel.rootPath = rootPath;
		return instance;
	}
	
	private SimpleLogisticModel() {
		
	}
	
	/**
	 * 初始化，获得训练数据，建立模型分类器
	 * @throws Exception
	 */
	public void init() throws Exception {
		trainSets = new Instances[trainFiles.length];
		simpleLogistics = new SimpleLogistic[trainFiles.length];
		
		for (int i = 0; i < trainFiles.length; ++i) {
			String arffFile = rootPath + trainFiles[i];
			ArffLoader loader = new ArffLoader();
			loader.setFile(new File(arffFile));
			trainSets[i] = loader.getDataSet();
			trainSets[i].setClassIndex(trainSets[i].numAttributes() - 1);
			
			simpleLogistics[i] = new SimpleLogistic();
			simpleLogistics[i].buildClassifier(trainSets[i]);
		
		}
		Logger.getRootLogger().info("logistic初始化完成");
		
//		String[] options = simpleLogistic.getOptions(); 
//		for (String option:options) {
//			System.out.println(option);
//		}
		
//		Instance testInst;
//        Evaluation testingEvaluation = new Evaluation(trainset);
//        int length = trainset.numInstances();
//        for (int i =0; i < length; i++) {
//            testInst = trainset.instance(i);
//            double rs = simpleLogistic.classifyInstance(testInst);
//            testingEvaluation.evaluateModelOnceAndRecordPrediction(
//            		simpleLogistic, testInst);
//        }
         
//        System.out.println( "分类器的正确率：" + (1- testingEvaluation.errorRate()));
	}
	
	/**
	 * 预测四个模块的融合结果
	 * @param svm
	 * @param p1
	 * @param p2
	 * @param p3
	 * @param p4
	 * @return
	 * @throws Exception
	 */
	public double predict(double svm,double p1,double p2,double p3,double p4) throws Exception {
	
	
		System.out.println("logistic 输入："+svm+","+p1+","+p2+","+p3+","+p4);
		
		Instance inst = new Instance(5);
		inst.setValue(0, svm);
		inst.setValue(1, p1);
		inst.setValue(2, p2);
		inst.setValue(3, p3);
		inst.setValue(4, p4);
		
//		double[] pro = simpleLogistic.distributionForInstance(inst);
//		System.out.println(pro[0]+" vs "+pro[1]);
//		return simpleLogistic.classifyInstance(inst);
		double pros = 0.0;
		for (int i = 0; i < simpleLogistics.length; ++i) {
			double pro = simpleLogistics[i].distributionForInstance(inst)[1];
			System.out.println("logistic "+i+" 输出："+ pro);
			pros += pro; 
		}
		pros = pros / simpleLogistics.length;
		
		System.out.println("logistic 输出："+ pros);
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
		
		for (int k = 0; k < 6; ++k) {
			for (int i =0; i < length; i++) {
			    testInst = testSet.instance(i);
			    testingEvaluation.evaluateModelOnceAndRecordPrediction(
			    		simpleLogistics[k], testInst);
			}
			System.out.println(k+ "分类器的正确率：" + (1- testingEvaluation.errorRate()));
		}
		   
	}

	public static void main(String[] args) {
		SimpleLogisticModel model = new SimpleLogisticModel();
		long start = System.currentTimeMillis();
		
		try {
			model.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		long end1 = System.currentTimeMillis();
		System.out.println("time : " + (end1-start)/1000);
		
		try {
//			System.out.println("1:"+model.predict(-0.468990457, 100, 30, 0, -100));
//			System.out.println("2:"+model.predict(-0.452124279, -29, 2, 0, 5));
//			System.out.println("3:"+model.predict(-0.413267112, -29, 14, 0, 57));
//			System.out.println("4:"+model.predict(0.286738107,13,9,0,100));
//			System.out.println("5:"+model.predict(0.292021448,-9,16,-60,-15));
			// 0.1093057702641013  -15 30  0   0   5
//			System.out.println("6:"+model.predict(0.04552800953388214,-19.0,9.0,-30.0,0.0));
			model.test("./file/test.arff");
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}
