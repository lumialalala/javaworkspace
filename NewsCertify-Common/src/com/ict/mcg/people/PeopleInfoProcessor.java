package com.ict.mcg.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.Cluster;
import com.ict.mcg.processs.SentimentAnalysis;
import com.ict.mcg.util.ICTSegmentation;

public class PeopleInfoProcessor {
	private double minSim = 0.5;

	public NoneWeiboUser getNoneWeiboUser(ArrayList<WeiboEntity> weiboList, String name) {
//System.out.println(name+" : " + weiboList.size());		
		ArrayList<WeiboEntity> allWeibo = this.weiboFilter(weiboList, name);
//System.out.println(name+" fileter : " + allWeibo.size());		

		allWeibo = setHotrate(allWeibo);

		ArrayList<ArrayList<WeiboEntity>> clusters = new Cluster()
				.singlePassCluster(allWeibo, minSim);
		
		if (clusters==null||clusters.size() == 0) {
			System.out.println("没有抓取到关于{"+name +"}的微博！");
			return null;
		}
		List<String> allWeiboContent = new ArrayList<String>();
		for (ArrayList<WeiboEntity> oneWeiboEntityList : clusters) {
			for (WeiboEntity weiboEntity : oneWeiboEntityList) {
				allWeiboContent.add(weiboEntity.getContent());
			}
		}
		ICTSegmentation segmentation = new ICTSegmentation();
		List<List<String>> sentencesList = segmentation.getSentencesList(allWeiboContent);
		
		SentimentAnalysis sentimentAnalysis = new SentimentAnalysis();
		NoneWeiboUser mNonWeiboUser = new NoneWeiboUser();
		mNonWeiboUser.name = name;
		double val = sentimentAnalysis.getEmotionFromSentenceList(sentencesList);
		mNonWeiboUser.setPosWordcloud(sentimentAnalysis.getPosWordcloud());
		mNonWeiboUser.setNegWordcloud(sentimentAnalysis.getNegWordcloud());
		mNonWeiboUser.setEmotionValue(val);
		
		return mNonWeiboUser;
	}
	
	private ArrayList<WeiboEntity> weiboFilter(ArrayList<WeiboEntity> allweibo,
			String keyword) {
		ArrayList<WeiboEntity> result = new ArrayList<WeiboEntity>();
		if (allweibo == null || allweibo.size() < 1)
			return result;
		HashSet<String> midlist = new HashSet<String>();
		for (WeiboEntity w : allweibo) {
			String url = w.getUrl();
			if (midlist.contains(url)) {
				continue;// 去重复
			} else {
				midlist.add(url);
			}
			// 去除不相关微博
			if(w.getContent().contains(keyword)){
				result.add(w);
			}
		}
		// 排序结果
		Collections.sort(result, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				// 先按热度排序
				int j = (int) (Integer.parseInt(w1.getComment())
						+ Integer.parseInt(w1.getForword())
						- Integer.parseInt(w0.getComment()) - Integer
						.parseInt(w0.getForword()));

				if (j == 0) {
					// 再按时间排序
					long k = 0;
					try {
						k = Long.parseLong(w0.getTime()) - Long.parseLong(w1
								.getTime());
					} catch (Exception e) {
						e.printStackTrace();
					}
					if (k > 0) {
						return 1;
					} else if (k < 0){
						return -1;
					} else {
						return 0;
					}
				}
				return j;
			}
		});
		return result;
	}
	
	private ArrayList<WeiboEntity> setHotrate(ArrayList<WeiboEntity> allWeibo) {
		ArrayList<WeiboEntity> result = new ArrayList<WeiboEntity>();
		int max = 0;
		for (WeiboEntity we : allWeibo) {
			int hot = Integer.parseInt(we.getForword())
					+ Integer.parseInt(we.getComment());
			if (max < hot)
				max = hot;
		}

		for (WeiboEntity we : allWeibo) {
			int hot = Integer.parseInt(we.getForword())
					+ Integer.parseInt(we.getComment());
			double hotrate = Math.sqrt(hot) / Math.sqrt(max);
			we.setHotrate(hotrate);
			result.add(we);
		}
		return result;
	}
}
