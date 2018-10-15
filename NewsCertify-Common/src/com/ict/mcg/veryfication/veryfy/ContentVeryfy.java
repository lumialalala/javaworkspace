package com.ict.mcg.veryfication.veryfy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.ict.mcg.category.Word2VectorClassifier;
import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.model.RFClassifier;
import com.ict.mcg.veryfication.feature.TopicFeatureExtractor;
import com.ict.mcg.veryfication.libsvm.SVMInterface;

/*
 * main process
 */
public class ContentVeryfy implements Callable<Float>{
//	private static String[] models = { "1.model", "2.model", "3.model",
//	"0.model" };
	private static String[] models = { "0.model", "1.model", "2.model", "3.model", "4.model", "5.model"};
//	private static String[] models = { "0.model", "1.model"};
	private String rootpath = "";

	// parameter settings
//	private double minsim = 0.8;
	private double minsim = 0.6;
	private double p[] = { 0.3, 0.06, 0.06, 0.5 };
	private int iteration = 100;
	private ArrayList<WeiboEntity> welist;
	private Logger logger = Logger.getLogger(ContentVeryfy.class);

	public String weiboFile = "";
	
	public ContentVeryfy(ArrayList<WeiboEntity> welist, String rootpath) {
		this.rootpath = rootpath;
		this.welist = welist;
		Collections.sort(this.welist, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity o1, WeiboEntity o2) {
				long diff = Long.parseLong(o1.getTime()) - Long.parseLong(o2.getTime());
				if (diff > 0) {
					return 1;
				} else if (diff < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});
	}

	//veryfyEvent
	/*public Double call_bak() {
		// get svm training path
		if (rootpath.equals(""))
			rootpath = FileIO.getSVMPath();
		// get id list and userurl list
		ArrayList<String> idlist = new ArrayList<String>();
		ArrayList<String> urllist = new ArrayList<String>();
		for (WeiboEntityWrapper we : welist) {
			idlist.add(we.getMid());
			urllist.add(we.getUserurl());
		}

		// extract features
		long start = System.currentTimeMillis();
		WeiboFeatureExtractor wfe = new WeiboFeatureExtractor();
		String features = rootpath + "/result/" + System.currentTimeMillis()
				+ ".data";
		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		wfe.extractAll(welist, features, toRemove);
		for (int i = toRemove.size() - 1; i >= 0; i--) {
			idlist.remove(i);
			urllist.remove(i);
		}
		long end = System.currentTimeMillis();
		logger.info("SVM特征提取用时 ： " + (end-start)/1000+"s");

		// predict result
		start = end;
		SVMInterface svm = new SVMInterface();
		String allCredit = svm.predictEvent(features, idlist, models, rootpath);
		end = System.currentTimeMillis();
		logger.info("SVM预测用时 ： " + (end-start)/1000+"s");

		// credibility propagation
		start = end;
		NewsCP bg = new NewsCP();
		bg.setParameters(p, minsim);
		double d = bg.run(allCredit, welist, iteration);
		System.out.println("content veryfy result: "+d);
		end = System.currentTimeMillis();
		logger.info("SVM propagation用时 ： " + (end-start)/1000+"s");
		return d;
	}*/
	
	public Float call_bak() {

		// extract features
		long start = System.currentTimeMillis();
		String features = rootpath + "/result/" + System.currentTimeMillis()
				+ ".data";
		
		String category = Word2VectorClassifier.getInstance().classify(welist);

		TopicFeatureExtractor tfe = new TopicFeatureExtractor();
		tfe.extractOneTopic(welist, 1, category, features);
		
		long end = System.currentTimeMillis();
		logger.info("SVM特征提取用时 ： " + (end-start)/1000+"s");

		// predict result
		start = end;
		SVMInterface svm = new SVMInterface();
		float d = svm.predictEvent(features, models, rootpath);
		end = System.currentTimeMillis();
		logger.info("SVM预测用时 ： " + (end-start)/1000+"s");

		return d;
	}
	
	public Float call() {
		TopicFeatureExtractor tfe = new TopicFeatureExtractor();
		double[] features = tfe.extractOneTopic(welist, 1);
		RFClassifier classifier = null;
		Float result = 0.0f;
		try {
			classifier = RFClassifier.getInstance(rootpath);
			result = (float) classifier.predict(features);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
