package com.ict.mcg.veryfication.veryfy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.ict.mcg.category.Word2VectorClassifier;
import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.model.RFClassifier;
import com.ict.mcg.model.UserRFClassifier;
import com.ict.mcg.veryfication.feature.TopicFeatureExtractor;
import com.ict.mcg.veryfication.feature.UserFeatureExtractor;
import com.ict.mcg.veryfication.libsvm.SVMInterface;

/*
 * main process
 */
public class UserVeryfy implements Callable<Float>{

	private String rootpath = "";

	// parameter settings
//	private double minsim = 0.8;
	private ArrayList<WeiboEntity> welist = new ArrayList<WeiboEntity>();
	private Logger logger = Logger.getLogger(UserVeryfy.class);

	public String weiboFile = "";
	
	public UserVeryfy(ArrayList<WeiboEntity> welist, String rootpath) {
		this.rootpath = rootpath;
		this.welist = welist;
	}
	

	
	public Float call() {
		logger.info("UserVerrfy Starting...");
		UserFeatureExtractor ufe = new UserFeatureExtractor();
		double[] features = ufe.userFeatureExtract(welist);
//		for(double feat: features){
//			System.out.print(feat + " ");
//		}
//		System.out.println();
		UserRFClassifier classifier = null;
		Float result = 0.0f;
		try {
			classifier = UserRFClassifier.getInstance(rootpath);
			result = (float) classifier.predict(features);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("UserVerrfy Error...");
		}
		
		return result;
	}
}
