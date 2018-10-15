package com.ict.mcg.veryfication.veryfy;

import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.model.SingleRFClassifer;
import com.ict.mcg.veryfication.feature.SingleWeiboFeatureExtractor;

public class SingleWeiboVerify implements Callable<Float> {
	private Logger logger = Logger.getLogger(SingleWeiboVerify.class);
	private WeiboEntity weiboEntity = null;
	private String rootpath = "";
	
	public SingleWeiboVerify(WeiboEntity entity,String rootpath){
		weiboEntity = entity;
		this.rootpath = rootpath;
	}
	public Float call(){
		SingleWeiboFeatureExtractor swfe = SingleWeiboFeatureExtractor.getInstance();
		Float result = -1f;
		SingleRFClassifer classifier = null;
		try {
			double []features = swfe.extractFeature(weiboEntity); 
			classifier = SingleRFClassifer.getInstance(rootpath);
			result = (float)classifier.predict(features);
		} catch (Exception e) {
			logger.warn(e);
		}
		return result;
	}
}
