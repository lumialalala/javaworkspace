package com.ict.mcg.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ict.mcg.event.EventProfile;
import com.ict.mcg.event.KeyWords;
import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.gather.entity.WeiboEntityWrapper;
import com.ict.mcg.model.TFDFModel;
import com.ict.mcg.processs.Cluster;
import com.ict.mcg.processs.NamedEntity;
import com.ict.mcg.processs.PhraseGenerator;
import com.ict.mcg.processs.SentimentAnalysis;
import com.ict.mcg.processs.WeiboEntityProcessor;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.util.FileIO;
import com.ict.mcg.util.GenerateEventJSON;
import com.ict.mcg.util.GenerateTimelineJSON;
import com.ict.mcg.util.GenerateWordCloudJSON;
import com.ict.mcg.util.ICTMongoClient;
import com.ict.mcg.util.ParamUtil;
import com.ict.mcg.util.TimeConvert;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class EventService {
	private ArrayList<String[]> imglist = new ArrayList<String[]>();
	private ArrayList<String> externalURL;
	private ArrayList<WeiboEntity> sourceUsers;
	private ArrayList<WeiboEntity> keyUsers;
	private int minweibo = 1;
	private int keywordlimit = 5;
	private String[] profile;
	private String[] eventemo;
	private String[] timeMessage; // 运行时间信息
	private double minSim = 0.3; // 聚类最小相似度阈值
	private int maxIncluster = 20;
	private int minClusterLimit = 5;
	private ArrayList<String> keyurllist;
	private int minIsoRate = 50;
	private int minSumRate = 1000;
	private int isoWeiboLimit = 10;
	private int maxTimelineCluster = 5;
	private int hotForwardCountThreshold = 300;
	private int twiceFilterWeiboCount = 200;
//	private ArrayList<String> partynamelist;
	private double eventemovalue;
	private String jsonfile;
	private String eventjson;
	private JSONArray wordCloudJson = null;
	// 线索重要程度
	private double keyValue = 0;
	private double speed = 0;
	private final static String searchNumMap="searchNumMap";
	// private double credit;
//	private List<String[]> weibotimeline;
	private ArrayList<ArrayList<WeiboEntity>> clusters;
	private ArrayList<WeiboEntity> filteredWeibos = new ArrayList<WeiboEntity>();
	private int maxSVMWeibo = 500; // 进行svm分类最多的微博数
	private ArrayList<WeiboEntityWrapper> weiboWrapperList;
	private Logger log = Logger.getLogger(EventService.class);

	public static long timestamp = 0L;
	
//	public String execute(String keyword, String startdate, String enddate,
//			String refresh) {
	public String execute(ArrayList<WeiboEntity> allWeibo, String keyword) {
//		System.out.println("keword: " + keyword);

		/*======================================*/
		timeMessage = new String[3];
		long begin = System.currentTimeMillis();
		/*======================================*/
		
		/**
		 * 1.按照时间排序
		 */
		Collections.sort(allWeibo, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				long j = 0;
				try {
					j = Long.parseLong(w0.getTime())
							- Long.parseLong(w1.getTime());
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				if (j == 0)
					return 0;
				else if (j > 0)
					return 1;
				else
					return -1;
			}
		});
		
		/**
		 * 2.对采集到的微博进行分词
		 */
		for (WeiboEntity we : allWeibo) {
			String content = we.getContent();
			ArrayList<WordNode> segs = WeiboEntityProcessor
					.getSegments(content);
			we.setSegs(segs);
		}
		WeiboEntityProcessor.setHotrate(allWeibo);
		
		/**
		 * 3. 进行事件聚类
		 */
		
		//聚类前先过滤不相关的微博
		String[] keywords=keyword.split(" ");
		ArrayList<WeiboEntity> tempallWeibo=new ArrayList<WeiboEntity>();
		tempallWeibo.addAll(allWeibo);
		if(keywords.length>=2)
		{
			allWeibo.clear();
			for(WeiboEntity we : tempallWeibo)
			{
				if(we.getContent().contains(keywords[0])&&we.getContent().contains(keywords[1]))
				{
					allWeibo.add(we);
				}
			}
		}
		
		clusters = cluster(allWeibo);
		if (clusters.size() == 0) {
			return "cluster0";
		}
		
		/**
		 * 4. 获取事件profile
		 */
		profile = new String[7];
		
		/**
		 * 4.1. 获取事件profile--关键词
		 */
		TFDFModel tfdfModel = new TFDFModel(clusters);
		String topWords = tfdfModel.getTopWordStr();
		profile[1] = topWords;
		
		//根据事件提取的关键词，判断是否需要二次过滤
		String[] topKeywords = topWords.split(" ");
		if (allWeibo.size() > twiceFilterWeiboCount && topKeywords.length > 2) {
			ArrayList<WeiboEntity> tmpList = new ArrayList<WeiboEntity>();
			tmpList.addAll(allWeibo);
			
			allWeibo.clear();
			for (WeiboEntity entity : tmpList) {
				if (entity.getContent().contains(topKeywords[0]) && entity.getContent().contains(topKeywords[1])) {
					allWeibo.add(entity);
				}
			}
			
			//System.out.println("二次过滤后微博总数：" + allWeibo.size());
			
			//重新获取聚类结果和事件关键词
			clusters = cluster(allWeibo);
			if (clusters.size() == 0) {
				return "cluster0";
			}
			tfdfModel = new TFDFModel(clusters);
			topWords = tfdfModel.getTopWordStr();
			profile[1] = topWords;
		}

		//聚类后判断各个类是否包含至少两个最重要的关键词，测试后效果不好，已放弃
		
		// 计算事件发展速度 (微博总数/时间段h)
		int weiboCount = allWeibo.size();
		if (allWeibo.size() > 1) {
			try {
				long firstWeiboTime = Long.parseLong(allWeibo.get(0).getTime());
				long lastWeiboTime = Long.parseLong(allWeibo.get(allWeibo.size() - 1).getTime());
				speed = (double)weiboCount / (lastWeiboTime - firstWeiboTime) * 1000 * 3600;
				//System.out.println("[EventService] firstWeibo" + TimeConvert.getStringTime(firstWeiboTime) + "speed:" + speed);
			} catch (NumberFormatException e) {
				System.err.println("[EventService] bad first weibo time");
			}
		}
				
		/**
		 * 4.2. 获取事件profile--描述短语
		 */
		PhraseGenerator pg = new PhraseGenerator();
//		profile[0] = pg.generateForAll(clusters, keyword);
		profile[0]=pg.generateTitle(clusters, keyword);//新版摘要提取算法
		

		/**
		 * 4.3. 获取事件profile--起止时间
		 */
		EventProfile ep = new EventProfile(allWeibo);
		String[] time = ep.getTime();
		profile[2] = time[0] + "--" + time[1];
		
		/**
		 * 4.4. 获取事件profile--发生地点
		 */
		ArrayList<String> loc = ep.getKeyWordsByProps(NamedEntity.REGION);
		String locstr = "";
		for (int i = 0; i < loc.size(); i++) {
			if (i >= keywordlimit)
				break;
			String s = loc.get(i);
			locstr = locstr + " " + s;
		}
		profile[3] = locstr;
	/*	ArrayList<String> org = ep.getKeyWordsByProps(NamedEntity.ORGANIZATION);
		String orgstr = "";
		for (int i = 0; i < org.size(); i++) {
			if (i >= keywordlimit)
				break;
			String s = org.get(i);
			orgstr = orgstr + " " + s;
		}
		profile[4] = orgstr;*/
		/**
		 * 4.5. 获取事件profile--关键人物
		 */
		ArrayList<String> peo = ep.getKeyWordsByProps(NamedEntity.PERSON);
		String peostr = "";
		for (int i = 0; i < peo.size(); i++) {
			if (i >= keywordlimit)
				break;
			String s = peo.get(i);
			if (s.charAt(0) == '@') {
				break;
			}
			peostr = peostr + " " + s;
		}
		profile[5] = peostr;
		
		/**
		 * 4.6. 获取事件profile--信息源头
		 */
		profile[4] = "";
		Set<String> sourceSet = new HashSet<String>();
		List<WeiboEntity> sourceList = new ArrayList<WeiboEntity>();
		
		for (WeiboEntity entity : allWeibo) {
			try {
				if (sourceList.size() < 5 && Integer.parseInt(entity.getForword()) >= 3) {
					sourceList.add(entity);
				} else if (sourceList.size() >= 5){
					break;
				}
			} catch (Exception e) {
				continue;
			}
		}
		
		sourceUsers = (ArrayList<WeiboEntity>)sourceList; 
		
		int count = 0;
		for (WeiboEntity entity : sourceList) {
			//String name = entity.getName();
			//if (sourceSet.contains(name)) {
			//	continue;
			//}
			//profile[4] += name + "|,|" + entity.getUserurl();
			//sourceSet.add(entity.getName());
			count++;
			if (count < 3) {
				profile[4] += " ";
			} else {
				break;
			}
		}
		
		//外部信息源
		ep = new EventProfile(allWeibo);
		ArrayList<String> externalSource = ep.getExternalSource();
		for (String source:externalSource) {
			if (sourceSet.contains(source)) {
				continue;
			}
			profile[4] += " " + source;
			sourceSet.add(source);
			count++;
			break;
		}
		
		
		/**
		 * 4.7. 获取事件profile--关键用户
		 */
		List<String> hotUserList = new ArrayList<String>();
		List<String> hotUserURLList = new ArrayList<String>();
		keyUsers = new ArrayList<WeiboEntity>();
		List<WeiboEntity> hotWeiboList = new ArrayList<WeiboEntity>();
		//List<WeiboEntity> keyValueWeiboList = new ArrayList<WeiboEntity>();
		int allForwardCount = 0;
		for (WeiboEntity entity:allWeibo) {
			try {
				allForwardCount += Integer.parseInt(entity.getForword());
				hotWeiboList.add(entity);
				//keyValueWeiboList.add(entity);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		hotForwardCountThreshold = (int) (allForwardCount * 0.2);
		//按照 转发数/粉丝数选择
		List<WeiboEntity> unusualList = new ArrayList<WeiboEntity>();
		Collections.sort(hotWeiboList, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				int w1Forward = 0;
				int w0Forward = 0;
				int w1UserFanCount = 1;
				int w0UserFanCount = 1;
				
				try {
					w1Forward = Integer.parseInt(w1.getForword());
				} catch(Exception e) {
					System.out.println("[EventService]forward parse error:" + w1.getMid() + ","  + w1.getForword());
				}
				try {
					w0Forward = Integer.parseInt(w0.getForword());
				} catch(Exception e) {
					System.out.println("[EventService]forward parse error:" + w1.getMid() + ","  + w0.getForword());
				}
				/*try {
					w1UserFanCount = Integer.parseInt(w1.getUserFanCount());
				} catch(Exception e) {
					System.out.println("[EventService]fancount parse error:" + w1.getMid() + ","  + w1.getUserFanCount());
				}
				try {
					w0UserFanCount = Integer.parseInt(w0.getUserFanCount());
				} catch(Exception e) {
					System.out.println("[EventService]fancount parse error:" + w1.getMid() + "," + w0.getUserFanCount());
				}*/
				
				double j =  (double)w1Forward / w1UserFanCount - (double)w0Forward / w0UserFanCount;
				if (j > 0)
					return 1;
				else if (j < 0)
					return -1;
				else
					return 0;
			}
		});
		
		/*
		 	Collections.sort(keyValueWeiboList, new Comparator<WeiboEntity>() {
			public final double current = (double)System.currentTimeMillis(); 
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				double j = 0;
				try {
					double forward1 = Double.parseDouble(w1.getForword());
					double fan1 = (Double.parseDouble(w1.getUserFanCount())+1);
					// 微博发布时间到当前时间， 对即时线索更有利
					double interval1 = current - Double.parseDouble(w1.getTime()) + 1;
					
					double forward0 = Double.parseDouble(w0.getForword());
					double fan0 = (Double.parseDouble(w0.getUserFanCount())+1);
					double interval0 = current - Double.parseDouble(w0.getTime()) + 1;
					j = forward1 / fan1 / interval1 - forward0 / fan0 / interval0;
				} catch(Exception e) {
					System.err.println("[EventService] parse error");
					e.printStackTrace();
				}
				
				if (j > 0)
					return 1;
				else if (j < 0)
					return -1;
				else
					return 0;
			}
		});
		*/
		
		// 计算线索重要程度，（转发/粉丝/发布间隔） 最大的5个值平均值
		/* 指标效果相当不明显
		int keyValueSize = 0;
		double keyValueSum = 0;
		StringBuilder sb = new StringBuilder();
		double current = (double)System.currentTimeMillis();
		for (WeiboEntity kwb : keyValueWeiboList) {
			double forward = Double.parseDouble(kwb.getForword());
			if (keyValueSize < 5 && forward > hotForwardCountThreshold) {
				double fan = (Double.parseDouble(kwb.getUserFanCount())+1);
				double interval = current - Double.parseDouble(kwb.getTime()) + 1;
				double ratio = Math.log(forward / fan + 1) / interval * 3600 * 1000;
				keyValueSum += ratio;
				sb.append(ratio + " ");
				keyValueSize += 1;
			} else {
				break;
			}
		}
		if (keyValueSize == 0) {
			WeiboEntity kwb = keyValueWeiboList.get(0);
			double forward = Double.parseDouble(kwb.getForword());
			double fan = (Double.parseDouble(kwb.getUserFanCount())+1);
			double interval = current - Double.parseDouble(kwb.getTime()) + 1;
			double ratio = Math.log(forward / fan + 1) / interval * 3600 * 1000;
			keyValueSum += ratio;
			sb.append(ratio + " ");
			keyValueSize += 1;
		}
		keyValue = keyValueSum / keyValueSize;
		System.out.println("[EventService] keyValue:" + keyValue + "(" + sb.toString() + ")");
		*/

		for (WeiboEntity entity:hotWeiboList) {
			if (Integer.parseInt(entity.getForword()) > 100) {
				if (unusualList.size() < 10) {
					unusualList.add(entity);
				} else {
					break;
				}
			}
		}
		
		if (unusualList.size() > 0) {
			Collections.sort(unusualList, new Comparator<WeiboEntity>() {
				public int compare(WeiboEntity w0, WeiboEntity w1) {
					int j = 0;
					try {
						j = Integer.parseInt(w1.getForword())
								- Integer.parseInt(w0.getForword());
					} catch(Exception e) {
						e.printStackTrace();
					}
					
					if (j > 0)
						return 1;
					else if (j < 0)
						return -1;
					else
						return 0;
				}
			});
			
			/*for (WeiboEntity entity:unusualList) {
				if (hotUserList.size() < 4 && !hotUserList.contains(entity.getName())) {
					hotUserList.add(entity.getName());
					hotUserURLList.add(entity.getUserurl());
					keyUsers.add(entity);
				} else {
					break;
				}
			}*/
		} else {
			// 按热度顺序排序
			Collections.sort(hotWeiboList, new Comparator<WeiboEntity>() {
					public int compare(WeiboEntity w0, WeiboEntity w1) {
						int j = 0;
						try {
							j = Integer.parseInt(w1.getForword())
									- Integer.parseInt(w0.getForword());
						} catch(Exception e) {
							e.printStackTrace();
						}
						
						if (j > 0)
							return 1;
						else if (j < 0)
							return -1;
						else
							return 0;
					}
				});
				/*for (WeiboEntity entity:hotWeiboList) {
					if (hotUserList.size() == 0) {
						//hotUserList.add(entity.getName());
						//hotUserURLList.add(entity.getUserurl());
						keyUsers.add(entity);
					} else if (hotUserList.size() < 4 && 
							Integer.parseInt(entity.getForword()) > hotForwardCountThreshold 
							//&& !hotUserList.contains(entity.getName())) {
						hotUserList.add(entity.getName());
						hotUserURLList.add(entity.getUserurl());
						keyUsers.add(entity);
					} else {
						break;
					}
				}*/
		}
		
		for (int i = 0; i < peo.size(); i++) {
			String s = peo.get(i);
			if (s.charAt(0) == '@') {
				s = s.substring(1);
				if (!hotUserList.contains(s)) {
					hotUserList.add(s);
					break;
				}
			}
		}
		
		String atstr = "";
		for (int i = 0; i < hotUserList.size(); i++) {
			String s = hotUserList.get(i);
			if (i < hotUserURLList.size()) {
				s = s + "|,|" + hotUserURLList.get(i);
			}
			atstr = atstr + " " + s;
		}
		profile[6] = atstr;
		
		/**
		 * 4.8. 获取事件profile--外部url源
		 */
		externalURL = ep.getExternalUrl();
		
		/**
		 * 4.9. 获取事件profile--相关图片
		 */
		imglist.clear();
		for (WeiboEntity we : allWeibo) {
			filteredWeibos.add(we);
			ArrayList<String> imgs = we.getPiclist();
			String wu = we.getUrl();
			String t = TimeConvert.getStringTime(Long.parseLong(we.getTime()));
			if(imgs!=null){
				for (String img : imgs) {
					String[] imgObj = new String[]{img, wu, t};
					imglist.add(imgObj);
				}
			}
		}

		/**
		 * 为SVM算法输入提供封装
		 */
		weiboWrapperList = new ArrayList<WeiboEntityWrapper>();

		// 微博数太少，就不过滤
		if (allWeibo.size() < maxSVMWeibo) {
			for (WeiboEntity entity : allWeibo) {
				weiboWrapperList.add(new WeiboEntityWrapper(entity));
			}
		} else {

			/*Collections.sort(allWeibo, new Comparator<WeiboEntity>() {
				public int compare(WeiboEntity w0, WeiboEntity w1) {
					//double d = w1.getHotrate() - w0.getHotrate();
					if (d == 0) {
						return 0;
					} else if (d > 0) {
						return 1;
					} else {
						return -1;
					}
				}
			});*/
			for (WeiboEntity entity : allWeibo.subList(0, maxSVMWeibo)) {
				weiboWrapperList.add(new WeiboEntityWrapper(entity));
			}
		}
		
		/*TimeLineOfWeibos timeline = new TimeLineOfWeibos(allWeibo);
		timeline.analysistimeline();
		weibotimeline = timeline.getTimeLineResult();*/

		/**
		 * 5.选取源头微博
		 */
		// 尽量选取最大类中最早的原创微博
		keyurllist = new ArrayList<String>();
		int maxClusterSize = 0;
		int maxClusterIdx = 0;
		for (int i = 0; i < clusters.size(); ++i) {
			if (clusters.get(i).size() > maxClusterSize) {
				maxClusterSize = clusters.get(i).size();
				maxClusterIdx = i;
			}
		}

		for (int i = 0; i < clusters.get(maxClusterIdx).size(); ++i) {
			if (!clusters.get(maxClusterIdx).get(i).isOrigin())
				continue;
			int c = Integer.parseInt(clusters.get(maxClusterIdx).get(i)
					.getForword());
			if (c >= 100 && c <= 4000) {
				keyurllist.add(clusters.get(maxClusterIdx).get(i).getUrl());
			}
		}

		// 如果还未找到关键微博，则在所有微博中选择，在小于100转发中找最大转发的，在大于4000转发中找最小转发的
		if (keyurllist.size() < 1) {
			ArrayList<WeiboEntity> remainAllWeibo = new ArrayList<WeiboEntity>();
			for (ArrayList<WeiboEntity> cluster : clusters) {
				for (WeiboEntity we : cluster) {
					if (!we.isOrigin())
						continue;
					remainAllWeibo.add(we);
					int c = Integer.parseInt(we.getForword());
					if (c >= 100 && c <= 4000) {
						keyurllist.add(we.getUrl());
					}
				}
			}
			if (keyurllist.size() == 0 && remainAllWeibo.size() > 0) {
				Collections.sort(remainAllWeibo, new Comparator<WeiboEntity>() {
					public int compare(WeiboEntity w0, WeiboEntity w1) {
						int j = (int) (Integer.parseInt((w0.getForword())) - Integer
								.parseInt(w1.getForword()));
						return j;
					}
				});
				int min = Integer.parseInt(remainAllWeibo.get(0).getForword());
				int max = Integer.parseInt(remainAllWeibo.get(
						remainAllWeibo.size() - 1).getForword());

				if (min > 4000) {
					for (int i = 0; i < remainAllWeibo.size(); i++) {
						if (!remainAllWeibo.get(i).isOrigin())
							continue;
						int forward = 0;
						try {
							forward = Integer.parseInt(remainAllWeibo.get(i).getForword());
						} catch (Exception e){
							System.err.println("[EventService]forward count wrong format:" + remainAllWeibo.get(i).getForword());
						}
						if (forward > 8000){ // 限制关键微博的最大转发量
							break;
						}
						keyurllist.add(remainAllWeibo.get(i).getUrl());
					}
				} else if (max < 100) {
					for (int i = remainAllWeibo.size() - 1; i >= 0; i--) {
						if (!remainAllWeibo.get(i).isOrigin())
							continue;
						keyurllist.add(remainAllWeibo.get(i).getUrl());
					}
				}
			}
		}

		
//		partynamelist = new ArrayList<String>();
		//目前已经不使用参与人物了
//		for (int i = 0; i < peo.size(); i++) {
//			if (partynamelist.size() >= 3) {
//				break;
//			}
//			//过滤掉@的微博用户
//			if (peo.get(i).charAt(0) != '@') {
//				partynamelist.add(peo.get(i));
//			}
//		}


		/**
		 * 6.计算词云和情感值
		 */
		
		HashMap<String, Integer> wordcloud = KeyWords.getEventKeyword(clusters,
				50);
		
		//计算词云情感值，获得正负情感词
		SentimentAnalysis sa = new SentimentAnalysis();
		eventemovalue = sa.getEmotionFromWordIntegerMap(wordcloud);
		eventemo = new String[3];
		/*
		 * if(credit>0){ eventemo[0] = "" + eventemovalue; }else{ eventemo[0]=""
		 * +(80+credit*(-100)); }
		 */
		List<String> eventpos = sa.getPosWordcloud();
		String pstr = "";
		for (int i = 0; i < eventpos.size(); i++) {
			if (i >= 5)
				break;
			pstr = pstr + " " + eventpos.get(i);
		}
		eventemo[1] = pstr;
		List<String> eventneg = sa.getNegWordcloud();
		String nstr = "";
		for (int i = 0; i < eventneg.size(); i++) {
			if (i >= 5)
				break;
			nstr = nstr + " " + eventneg.get(i);
		}
		eventemo[2] = nstr;

		//词云json
		HashMap<String, Integer> emoWordCloud = KeyWords.getEmotionalKeywords(clusters);
		wordCloudJson = GenerateWordCloudJSON.generateJson(emoWordCloud);

		/**
		 * 7.计算时间轴，生成时间轴json文件
		 */
		// timeline每天最多显示5个类
		ArrayList<ArrayList<WeiboEntity>> timelineClusters = new ArrayList<ArrayList<WeiboEntity>>();
		ArrayList<ArrayList<WeiboEntity>> tmpClusters = new ArrayList<ArrayList<WeiboEntity>>();
		int clusterSize = clusters.size();
		if (clusterSize > maxTimelineCluster) { // 如果微博类过多，则限制一下；每天留maxTimelineCluster个类
			Calendar cal = Calendar.getInstance();
			// 获取最早日期的天数
			String currDayStr = clusters.get(0).get(0).getTime();
			cal.setTimeInMillis(Long.parseLong(currDayStr));
			int currDay = cal.get(Calendar.DAY_OF_YEAR);
			for (ArrayList<WeiboEntity> cluster : clusters) {
				String clusterTimeStr = cluster.get(0).getTime();
				cal.setTimeInMillis(Long.parseLong(clusterTimeStr));
				int clusterTimeDay = cal.get(Calendar.DAY_OF_YEAR);
				if (clusterTimeDay != currDay || clusters.get(clusterSize - 1).equals(cluster)) {
					if (tmpClusters.size() > maxTimelineCluster) { //同一天的微博类太多,留下最大的maxTimelineCluster个类
						ArrayList<ArrayList<WeiboEntity>> tmp = new ArrayList<ArrayList<WeiboEntity>>();
						tmp.addAll(tmpClusters);
						Collections.sort(tmp, new Comparator<ArrayList<WeiboEntity>>() {
							public int compare(ArrayList<WeiboEntity> o1,
									ArrayList<WeiboEntity> o2) {
								return o2.size() - o1.size();
							}
						});
						timelineClusters.addAll(tmp.subList(0, maxTimelineCluster));
					} else {
						timelineClusters.addAll(tmpClusters);
					}
					tmpClusters.clear();
					currDay = clusterTimeDay;
				}
				tmpClusters.add(cluster);// 将同天的微博类聚到一起
			}
		} else {
			timelineClusters.addAll(clusters);
		}

		// WARNING : timeline maybe change timelineClusters
		jsonfile = FileIO.writeJSON(new GenerateTimelineJSON().geneAll(
				timelineClusters, keyword));
		
		/**
		 * 8.生成详情页json文件
		 */
		eventjson = FileIO.writeJSON(new GenerateEventJSON().geneAll(clusters,
				keyword));
		if (eventjson.equals("")) {
			return "json file null";
		}

		/*======================================*/
		timeMessage[2] = String
				.valueOf((System.currentTimeMillis() - begin) / 1000);// 其它时间
		/*======================================*/
		
		return "success";
	}
	/**
	 * 修改聚类策略
	 * @author yazi
	 * 
	 * */
	public ArrayList<ArrayList<WeiboEntity>> Executecluster(ArrayList<WeiboEntity> allWeibo,String keyword){	
		/**
		 * 1.按照时间排序
		 */
		Collections.sort(allWeibo, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				long j = 0;
				try {
					j = Long.parseLong(w0.getTime())
							- Long.parseLong(w1.getTime());
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				if (j == 0)
					return 0;
				else if (j > 0)
					return 1;
				else
					return -1;
			}
		});
		
		/**
		 * 2.对采集到的微博进行分词
		 */
		for (WeiboEntity we : allWeibo) {
			String content = we.getContent();
			ArrayList<WordNode> segs = WeiboEntityProcessor
					.getSegments(content);
			we.setSegs(segs);
		}
		WeiboEntityProcessor.setHotrate(allWeibo);
		
		String[] clueKeyWords=keyword.split(" ");
		//聚类前先过滤不相关的微博
		ArrayList<WeiboEntity> allWeibo_filter=new ArrayList<WeiboEntity>();
		for(WeiboEntity we : allWeibo)
		{
			if(we.getContent().contains(clueKeyWords[0])&&we.getContent().contains(clueKeyWords[1]))
			{
				allWeibo_filter.add(we);
			}
		}
//		ArrayList<WeiboEntity> allWeibo_filter=allWeibo;
		ArrayList<ArrayList<WeiboEntity>> clusters = cluster(allWeibo_filter);
		if (clusters.size() == 0) {
//			System.out.println("聚类个数为0");
			return null;
		}
		
		/**
		 * 4.1. 获取事件profile--关键词
		 */
		TFDFModel tfdfModel = new TFDFModel(clusters);
		String topWords = tfdfModel.getTopWordStr();
		
		//根据事件提取的关键词，判断是否需要二次过滤
		String[] topKeywords = topWords.split(" ");
		if (allWeibo_filter.size() > twiceFilterWeiboCount && topKeywords.length > 2) {
			ArrayList<WeiboEntity> tmpList = new ArrayList<WeiboEntity>();
			tmpList.addAll(allWeibo_filter);
			
			allWeibo_filter.clear();
			for (WeiboEntity entity : tmpList) {
				if (entity.getContent().contains(topKeywords[0]) && entity.getContent().contains(topKeywords[1])) {
					allWeibo_filter.add(entity);
				}
			}
			
			//System.out.println("二次过滤后微博总数：" + allWeibo.size());
			
			//重新获取聚类结果和事件关键词
			clusters = cluster(allWeibo_filter);
			if (clusters.size() == 0) {
				return null;
			}
		}

		//聚类后判断各个类是否包含至少两个最重要的关键词
//		ArrayList<ArrayList<WeiboEntity>> clusters_filter=new ArrayList<ArrayList<WeiboEntity>>();
//		for(int i=0;i<clusters.size();i++)
//		{
//			ArrayList<ArrayList<WeiboEntity>> tempcluster=new ArrayList<ArrayList<WeiboEntity>>();
//			tempcluster.add(clusters.get(i));
//			
//			TFDFModel temptfdfModel = new TFDFModel(tempcluster);
//			String temptopWords = tfdfModel.getTopWordStr();
//			if(temptopWords.contains(clueKeyWords[0])&&temptopWords.contains(clueKeyWords[1]))
//			{
//				clusters_filter.add(clusters.get(i));
//			}
//		}
//		return clusters_filter;
		return clusters;
	}

	public ArrayList<ArrayList<WeiboEntity>> cluster(
			ArrayList<WeiboEntity> allWeibo) {	
		ArrayList<ArrayList<WeiboEntity>> clusters = new Cluster()
				.singlePassCluster(allWeibo, minSim);

		// 按时间排序
		for (ArrayList<WeiboEntity> wel : clusters) {
			Collections.sort(wel, new Comparator<WeiboEntity>() {
				public int compare(WeiboEntity w0, WeiboEntity w1) {
					long j = Long.parseLong(w0.getTime())
							- Long.parseLong(w1.getTime());
					if (j > 0)
						return 1;
					else if (j < 0)
						return -1;
					else
						return 0;
				}
			});
		}

		Collections.sort(clusters, new Comparator<ArrayList<WeiboEntity>>() {
			public int compare(ArrayList<WeiboEntity> o1,
					ArrayList<WeiboEntity> o2) {
				long j = Long.parseLong(o1.get(0).getTime())
						- Long.parseLong(o2.get(0).getTime());
				if (j > 0)
					return 1;
				else if (j < 0)
					return -1;
				else
					return 0;
			}
		});
		
		if (clusters.size() > minClusterLimit) {
			// 去掉热度总量很小的类
			ArrayList<ArrayList<WeiboEntity>> tArrayList = new ArrayList<ArrayList<WeiboEntity>>();
			for (ArrayList<WeiboEntity> we : clusters) {
				int allcount = 0;
				for (WeiboEntity w : we) {
					allcount += Integer.parseInt(w.getForword())
							+ Integer.parseInt(w.getComment());
				}
				// System.out.println(allcount);
				if (we.size() > isoWeiboLimit || allcount >= this.minSumRate)
					tArrayList.add(we);
			}
			
			// 最少留minClusterLimit个类
			if (tArrayList.size() < minClusterLimit) {
				// 计算类的热度
				int[] clusterHot = new int[clusters.size()];
				for (int i = 0; i < clusters.size(); i++) {
					ArrayList<WeiboEntity> cluster = clusters.get(i);
					if (tArrayList.contains(cluster)) {
						clusterHot[i] = -1;
						continue;
					}
					int hot = 0;
					for (WeiboEntity w : cluster) {
						hot += Integer.parseInt(w.getForword())
								+ Integer.parseInt(w.getComment());
					}
					clusterHot[i] = hot;
				}
				// 添加最热的类
				int need = minClusterLimit - tArrayList.size();
				while(need > 0) {
					int maxIdx = -1;
					int max = -1;
					for (int i = 0; i < clusterHot.length; i++) {
						if (clusterHot[i] > max) {
							max = clusterHot[i];
							maxIdx = i;
						}
					}
					tArrayList.add(clusters.get(maxIdx));
					need--;
					clusterHot[maxIdx] = -1;
				}
			}
			
			
			
			//System.out.println("删除类似孤立点：" + (clusters.size() - tArrayList.size()) + "个");
			Collections.sort(tArrayList, new Comparator<ArrayList<WeiboEntity>>() {
				public int compare(ArrayList<WeiboEntity> cluster0, ArrayList<WeiboEntity> cluster1) {
					long j = Long.parseLong(cluster0.get(0).getTime())
							- Long.parseLong(cluster1.get(0).getTime());
					if (j > 0)
						return 1;
					else if (j < 0)
						return -1;
					else
						return 0;
				}
			});
			clusters = tArrayList;
		}
		
		// 移除热度小于minIsoRate的微博，但保证不会因此使每个类的微博数量少于maxIncluster
		for (ArrayList<WeiboEntity> we : clusters) {
			if (we.size() > maxIncluster) {
				Collections.sort(we, new Comparator<WeiboEntity>() {
					public int compare(WeiboEntity w0, WeiboEntity w1) {
						int hotrate0 = Integer.parseInt(w0.getForword())
								+ Integer.parseInt(w0.getComment());
						int hotrate1 = Integer.parseInt(w1.getForword())
								+ Integer.parseInt(w1.getComment());
						return hotrate1 - hotrate0;
					}
				});
				for (int i = we.size() - 1; i >= 0; i--) {
					int forward = Integer.parseInt(we.get(i).getForword());
					int comment = Integer.parseInt(we.get(i).getComment());
					int hotrate = forward + comment; // 微博热度指标
					// System.out.println("hot:" + hotrate);
					
					if (we.size() <= maxIncluster) {
						break;
					}
				}
				Collections.sort(we, new Comparator<WeiboEntity>() {
					public int compare(WeiboEntity w0, WeiboEntity w1) {
						long j = Long.parseLong(w0.getTime())
								- Long.parseLong(w1.getTime());
						if (j > 0)
							return 1;
						else if (j < 0)
							return -1;
						else
							return 0;
					}
				});
			}
		}

//		for (int i = 0; i < clusters.size(); i++) {
//			ArrayList<WeiboEntity> wel = clusters.get(i);
//			System.out.println("第" + i + "个类,共有" + wel.size() + "条微博");
//		}
		return clusters;
	}
	
	public ArrayList<WeiboEntity> getSourceUsers() {
		return sourceUsers;
	}

	public void setSourceUsers(ArrayList<WeiboEntity> sourceUsers) {
		this.sourceUsers = sourceUsers;
	}

	public ArrayList<WeiboEntity> getKeyUsers() {
		return keyUsers;
	}

	public void setKeyUsers(ArrayList<WeiboEntity> keyUsers) {
		this.keyUsers = keyUsers;
	}

	public ArrayList<String> getExternalURL() {
		return externalURL;
	}

	public void setExternalURL(ArrayList<String> externalURL) {
		this.externalURL = externalURL;
	}

	public ArrayList<String[]> getImglist() {
		return imglist;
	}

	public void setImglist(ArrayList<String[]> imglist) {
		this.imglist = imglist;
	}

	public ArrayList<WeiboEntity> getFilteredWeibos() {
		return filteredWeibos;
	}
	
	public ArrayList<WeiboEntityWrapper> getWeiboWrapperList() {
		return weiboWrapperList;
	}

	public String[] getProfile() {
		return profile;
	}

	public String[] getEventemo() {
		return eventemo;
	}

	public ArrayList<String> getKeyurllist() {
		return keyurllist;
	}

	public double getEventemovalue() {
		return eventemovalue;
	}

	public String getJsonfile() {
		return jsonfile;
	}

	public String getEventjson() {
		return eventjson;
	}

//	public ArrayList<String> getPartynamelist() {
//		return partynamelist;
//	}

	/*public List<String[]> getWeibotimeline() {
		return weibotimeline;
	}
*/
	public ArrayList<ArrayList<WeiboEntity>> getClusters() {
		return clusters;
	}

	// added by lsj
	public String getRunTimeMessage() {
		String r = "";
		for (String s : timeMessage)
			r += s + "\t";
		return r.trim();
	}
	

	public JSONArray getWordCloudJson() {
		return wordCloudJson;
	}

	public void setMinweibo(int minweibo) {
		this.minweibo = minweibo;
	}

	public double getKeyValue() {
		return keyValue;
	}

	public double getSpeed() {
		return speed;
	}
	
	public static void resetKeyValue() {
		DBCollection event = ICTMongoClient.getCollection("NewsCertify", "event");
		DBCollection result = ICTMongoClient.getCollection("NewsCertify", "result");
		ArrayList<WeiboEntity> weiboList = new ArrayList<WeiboEntity>();
		try {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DATE, 5);
			cal.set(Calendar.HOUR_OF_DAY, 12);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			System.out.println(cal.getTime());
			long june5 = cal.getTimeInMillis();
			DBCursor cursor = result.find(new BasicDBObject("keyValue", new BasicDBObject("$exists", true))
				.append("timestamp", new BasicDBObject("$lt", june5)), new BasicDBObject("id", 1).append("timestamp", 1));
			System.out.println(cursor.count());
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String id = obj.get("id").toString();
				timestamp = (Long)obj.get("timestamp");
				System.out.println("id:" + id);
				DBObject eventObj = event.findOne(new BasicDBObject("id", obj.get("id")), new BasicDBObject("weibo", 1));
				
				weiboList.clear();
				int allForwardCount = 0;
				BasicDBList weibo = (BasicDBList)eventObj.get("weibo");
				for (Object wb : weibo) {
					WeiboEntity we = new WeiboEntity();
					BasicDBObject w = (BasicDBObject)wb;
					String forward = w.getString("forward");
					String fan = w.getString("userFollowCount");
					String time = w.getString("time");
					we.setForword(forward);
					allForwardCount += Integer.parseInt(forward);
					//we.setUserFanCount(fan);
					we.setTime(time);
					weiboList.add(we);
				}
				int hotForwardCountThreshold = (int) (allForwardCount / weibo.size());
				if (weiboList == null || weiboList.size() == 0) {
					continue;
				}
				Collections.sort(weiboList, new Comparator<WeiboEntity>() {
					public int compare(WeiboEntity w0, WeiboEntity w1) {
						double j = 0;
						try {
							double forward1 = Double.parseDouble(w1.getForword());
							//double fan1 = (Double.parseDouble(w1.getUserFanCount())+1);
							double current = (double)timestamp;
							// 微博发布时间到当前时间， 对即时线索更有利
							double interval1 = current - Double.parseDouble(w1.getTime()) + 1;
							
							double forward0 = Double.parseDouble(w0.getForword());
							//double fan0 = (Double.parseDouble(w0.getUserFanCount())+1);
							double interval0 = current - Double.parseDouble(w0.getTime()) + 1;
							//j = forward1 / fan1 / interval1 - forward0 / fan0 / interval0;
						} catch(Exception e) {
							System.err.println("[EventService] parse error");
							e.printStackTrace();
						}
						
						if (j > 0)
							return 1;
						else if (j < 0)
							return -1;
						else
							return 0;
					}
				});

				// 计算线索重要程度，（转发/粉丝/发布间隔） 最大的5个值平均值
				int keyValueSize = 0;
				double keyValueSum = 0;
				StringBuilder sb = new StringBuilder();
				double current = (double)timestamp;
				for (WeiboEntity kwb : weiboList) {
					double forward = Double.parseDouble(kwb.getForword());
					if (keyValueSize < 5 && forward > hotForwardCountThreshold) {
						//double fan = (Double.parseDouble(kwb.getUserFanCount())+1);
						double interval = current - Double.parseDouble(kwb.getTime()) + 1;
						//double ratio = Math.log(forward / fan + 1) / interval * 3600 * 1000;
						//keyValueSum += ratio;
						//sb.append(ratio + "|" + kwb.getTime() + "|" + kwb.getName() + " ");
						keyValueSize += 1;
					} else {
						break;
					}
				}
				System.out.println(keyValueSize);
				if (keyValueSize == 0) {
					WeiboEntity kwb = weiboList.get(0);
					double forward = Double.parseDouble(kwb.getForword());
					//double fan = (Double.parseDouble(kwb.getUserFanCount())+1);
					double interval = current - Double.parseDouble(kwb.getTime()) + 1;
					//double ratio = Math.log(forward / fan + 1) / interval * 3600 * 1000;
					//keyValueSum += ratio;
					//sb.append(ratio + " ");
					keyValueSize += 1;
				}
				double keyValue = keyValueSum / keyValueSize;
				System.out.println("[EventService] keyValue:" + keyValue + "(" + sb.toString() + ")");
				result.update(new BasicDBObject("id", obj.get("id")), new BasicDBObject("$set", new BasicDBObject("keyValue", keyValue)));
			}
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
		}
	}
	
	public static void resetSpeedValue() {
		DBCollection event = ICTMongoClient.getCollection("NewsCertify", "event");
		DBCollection result = ICTMongoClient.getCollection("NewsCertify", "result");
		ArrayList<Long> timeList = new ArrayList<Long>();
		double speed = 0;
		try {
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.DATE, 5);
			cal.set(Calendar.HOUR_OF_DAY, 12);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			//System.out.println(cal.getTime());
			long june5 = cal.getTimeInMillis();
			DBCursor cursor = result.find(new BasicDBObject("speed", new BasicDBObject("$exists", false))
				/*.append("timestamp", new BasicDBObject("$lt", june5))*/, new BasicDBObject("id", 1).append("timestamp", 1));
			System.out.println(cursor.count());
			
			BufferedReader br = new BufferedReader(new FileReader("speed_ids"));
			HashSet<String> ids = new HashSet<String>();
			while (br.ready()) {
				String id = br.readLine().trim().split(":")[0];
				ids.add(id);
			}
			br.close();
			
			BufferedWriter bw = new BufferedWriter(new FileWriter("speed_ids2"));
			int count = 0;
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				String id = obj.get("id").toString();
				if (ids.contains(id)) {
					continue;
				}
				timestamp = (Long)obj.get("timestamp");
				//System.out.println("id:" + id);
				
				DBObject eventObj = event.findOne(new BasicDBObject("id", obj.get("id")), new BasicDBObject("weibo", 1));
				
				timeList.clear();
				BasicDBList weibo = (BasicDBList)eventObj.get("weibo");
				for (Object wb : weibo) {
					BasicDBObject w = (BasicDBObject)wb;
					String time = w.getString("time");
					timeList.add(Long.parseLong(time));
				}
				int weiboCount = weibo.size();
				
				Collections.sort(timeList);

				if (weiboCount > 1) {
					try {
						long firstWeiboTime = timeList.get(0);
						long lastWeiboTime = timeList.get(weiboCount - 1);
						speed = (double)weiboCount / (lastWeiboTime - firstWeiboTime) * 1000 * 3600;
						//System.out.println("[EventService] firstWeibo" + TimeConvert.getStringTime(firstWeiboTime) + "speed:" + speed);
					} catch (NumberFormatException e) {
						System.err.println("[EventService] bad first weibo time");
					}
				} else {
					speed = 0;
				}
				
				//System.out.println("[EventService] Speed:" + speed);
				bw.write(id + ":" + speed + "\n");
				bw.flush();
				count++;
				if (count % 500 == 0) {
					System.out.println(count);
				}
				result.update(new BasicDBObject("id", obj.get("id")), new BasicDBObject("$set", new BasicDBObject("speed", speed)));
			}
			bw.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws IOException {
		EventService es=new EventService();
		PhraseGenerator pg = new PhraseGenerator();
		DBCollection eventcoll = ICTMongoClient.getCollection("NewsCertify", ParamUtil.EVENT_TABLE);
		DBCollection coll = ICTMongoClient.getCollection("NewsCertify", ParamUtil.RESULT_TABLE);
		DBCollection cluecoll = ICTMongoClient.getCollection("NewsCertify", ParamUtil.NEWSCLUE_TABLE);
		DBCursor cursor = coll.find();
		int i=0;
		while(cursor.hasNext())
		{
			DBObject obj = cursor.next();
			if(obj.containsField("hidden")&&obj.get("hidden").equals("true"));
			else
			{
				String clue_id=(String)obj.get("clue_id");
				DBCursor clueCursor=eventcoll.find(new BasicDBObject(new BasicDBObject("clue_id", ""+clue_id)))
						.sort(new BasicDBObject("timestamp", -1)).limit(1);//按时间取最新
				if(clueCursor.count()>0)
				{
					DBObject clueObj = clueCursor.next();
					i++;
					if(i>30)break;
					if(clueObj.containsField("keywords")&&clueObj.containsField("weibo"))
					{
						//聚类
						BasicDBList weibolist = (BasicDBList)clueObj.get("weibo");
						ArrayList<WeiboEntity> weList = WeiboEntity.convert(weibolist);
						String keyword=(String)clueObj.get("keywords");
						ArrayList<ArrayList<WeiboEntity>> clusters=es.Executecluster(weList, keyword);
						if(clusters!=null)
						{
							System.out.println(clue_id);
							System.out.println(pg.generateTitle(clusters, keyword));
//							pg.generateForAll(clusters, keyword);
//							JSONObject eventjson =new GenerateEventJSON().geneAll(clusters,keyword);
//							//更新
//							System.out.println(clue_id);
//							String path;
//							if(obj.containsField("eventjson"))
//							{
//								path="/home/ictmcg/server/apache-tomcat-6.0.41/webapps/NewsCertify-WebOffline/json/"+obj.get("eventjson");
////								path="D://newspace//test//"+obj.get("eventjson");
//							}
//							else
//							{
//								String eventjsonName=FileIO.writeJSON(eventjson);
//								path="/home/ictmcg/server/apache-tomcat-6.0.41/webapps/NewsCertify-WebOffline/json/"+eventjsonName;
////								path="D://newspace//test//"+eventjsonName;
//								obj.put("eventjson", eventjsonName);
//								coll.save(obj);
//								
//							}
//							File outfile = new File(path);
//							try {
//								BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
//										new FileOutputStream(outfile), "utf-8"));			
//								writer.write(eventjson.toString());
//								writer.flush();
//								writer.close();
//							} catch (IOException e1) {
//								e1.printStackTrace();
//							}
						}
						else System.out.println("该线索聚类个数为0："+clue_id);
					}
					else System.out.println("该线索缺keyword/weibo字段,无法聚类:"+clue_id);
				}
				else System.out.println("该线索在event库里找不到:"+clue_id);
			}

		}
		
//		String[] cluelist=new String[]{
//				"69c57ff35a13f4f211d880c5e705e62a",
//				"c8cbfa78802197f124bda26a331577e8",
//				"9cf7371491f3bcb316a3d581176bc40d",
//				"ec226f78e5cb5b25022f63b1362f3000",
//				"4b29ea7fb403cebdb88589aa5a35e273",
//				"31505a31926a830d1b5f0a8ce7e94738",
//				"d41ce23f8be7dd93a7d70a4edb1781a5",
//				"2420b55e08e440b1e33b61151ab2155e",
//				"ecc743fc8b2695a4ad1a8e84d1e42e5a",
//				"199a6453e49e5added93b160fd762405"};
//		for(String id:cluelist)
//		{
//			FileWriter fw=new FileWriter(new File("D:\\newspace\\clusterResult_"+id+".txt"));
//			DBCursor clueCursor=coll.find(new BasicDBObject(new BasicDBObject("clue_id", ""+id))).sort(new BasicDBObject("timestamp", -1)).limit(1);
//			DBObject clueObj = clueCursor.next();
//			BasicDBList weibolist = (BasicDBList)clueObj.get("weibo");
//			ArrayList<WeiboEntity> weList = WeiboEntity.convert(weibolist);
//			String keyword=(String)clueObj.get("keywords");
//			ArrayList<ArrayList<WeiboEntity>> clusters=es.Executecluster(weList, keyword);
//			pg.generateForAll(clusters, keyword);
//			fw.write("["+keyword+"]\n");
//			System.out.println(FileIO.writeJSON(new GenerateEventJSON().geneAll(clusters,keyword)));
//			for(ArrayList<WeiboEntity> c:clusters)
//			{
//				fw.write(c.get(0).getClasstitle()+"\n");
//				for(WeiboEntity w:c)
//				{
//					fw.write(w.getContent()+"\n");
//				}
//				fw.write("**********************\n");
//			}
//			fw.close();
//		}
		
	}
}
