package com.ict.mcg.service;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.ict.mcg.gather.entity.AbstractUserInfo;
import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.gather.entity.WeiboUserInfo;
import com.ict.mcg.people.NoneWeiboUser;
import com.ict.mcg.people.PeopleInfoProcessor;
import com.ict.mcg.processs.WeiboEntityProcessor;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.util.ParamUtil;
import com.ict.mcg.util.RedisUtil;
import com.ict.mcg.util.SerializeUtil;
import com.ict.mcg.util.TimeConvert;
import com.ict.mcg.webservice.client.ServiceClient;
import com.ict.mcg.webservice.service.GatherWebService;

import net.sf.json.JSONObject;

public class PeopleService implements Callable<String>{

	private static int topLimit = 5;
	private int peoplenum = 10;
	private ArrayList<String[]> userinfo;
	private List<WeiboUserInfo> wUserInfo = new ArrayList<WeiboUserInfo>();
	private Map<String, List<AbstractUserInfo>> user2followeeMap = new HashMap<String, List<AbstractUserInfo>>();
	private Map<String, List<WeiboEntity>> userHistoryWeiboMap = new HashMap<String, List<WeiboEntity>>();
	private List<JSONObject> sourceUserFolloweeInfoList = new ArrayList<JSONObject>();
	private String[] useremo = new String[5];
	private String[] peoemo = new String[5];
	private String[] timeMessage;
	private int useremoValue;
	private int peoemoValue;
	private int certifyValue;
	private String startDate;
	private ArrayList<WeiboEntity> filteredWeibos;
	private String userInfoMap = "userInfoMap";
	public static String USER_FOLLOW_LIST_MAP = "userFollowListMap";
	public static String USER_WEIBO_LIST_MAP = "userWeiboListMap";
	private String tmpUserSearchMap = "tmpUserSearchMap";
	private String tmpPartyMap = "tmpPartyMap";
	private Logger log = Logger.getLogger(PeopleService.class);
//	private ArrayList<String> participantNameList;
	private ArrayList<String []> keyUsers = new ArrayList<String []>();
	private ArrayList<String []> sourceUsers = new ArrayList<String []>();
	private ArrayList<String []> piyaoUsers = new ArrayList<String []>();
	
	private int is_online=0;//0 离线搜索，1在线搜索
	
	public void set_online(){
		is_online=1;
	}
	
	
	public PeopleService(ArrayList<WeiboEntity> keyUsers,ArrayList<WeiboEntity> sourceUsers, ArrayList<WeiboEntity> piyaoUsers) {
		for(WeiboEntity ku: keyUsers){
			String []info = new String[2];
			info[0] = ku.getUserId();
			info[1] = ku.getName();
			this.keyUsers.add(info);
		}
		
		for(WeiboEntity su: sourceUsers){
			String []info = new String[2];
			info[0] = su.getUserId();
			info[1] = su.getName();
			this.sourceUsers.add(info);
		}

		for(WeiboEntity pu: piyaoUsers){
			String []info = new String[2];
			info[0] = pu.getUserId();
			info[1] = pu.getName();
			this.piyaoUsers.add(info);
		}
//		this.participantNameList = participantNameList;
		this.startDate = "";
	}
	
	public PeopleService(ArrayList<String[]> keyUsers, ArrayList<String[]> sourceUsers){
		this.keyUsers = keyUsers;
		this.sourceUsers = sourceUsers;
//		this.participantNameList = new ArrayList<String>();
		this.startDate = "";
	}
	
	public void setFilteredWeibo(String startDate, ArrayList<WeiboEntity> filteredWeibos) {
		this.startDate = startDate;
		this.filteredWeibos = filteredWeibos;
	}
	// added by lsj
	public String getRunTimeMessage(){
		String r = "";
		for(String s : timeMessage)
			r+=s+"\t";
		return r.trim();
	}

	public int getCertifyValue() {
		int certifyCnt = 0;
		int cnt = 1;
		long start = 0;
		try {
			start = TimeConvert.convertString(startDate);
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
		
		ArrayList<String> usrnames = new ArrayList<String>();
		for (WeiboEntity we : filteredWeibos) {
			long wt = 0;
			try {
				wt = Long.parseLong(we.getTime());
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
			long interval = wt - start;
			interval = interval > 0 ? interval : -interval;
			long interDay = TimeUnit.DAYS.convert(interval, TimeUnit.MILLISECONDS);
			if (interDay > 7) {
				continue;
			}
			String uname = we.getName();
			if (usrnames.contains(uname)) {
				continue;
			} else {
				usrnames.add(uname);
			}
			
			int certify = we.getUserCertify();
			if (certify == 2) { // 机构认证用户
				certifyCnt++;
			}
			cnt++;
			if (cnt > topLimit) {
				break;
			}
		}
		if (cnt <= topLimit && certifyCnt > 0) {
			certifyCnt = certifyCnt * topLimit / (cnt - 1);
		}
		certifyValue = 0;
		useremoValue = 0;
		useremo[1] = "0";
		if (certifyCnt == 5) {
			certifyValue = -3;
			useremoValue = -90;
			// FIXME: just useremoValue is ok
			useremo[1] = "" + useremoValue;
		} else if (certifyCnt == 4) {
			certifyValue = -2;
			useremoValue = -60;
			useremo[1] = "" + useremoValue;
		} else if (certifyCnt == 3) {
			certifyValue = -1;
			useremoValue = -30;
			useremo[1] = "" + useremoValue;
		}
		return certifyValue;
	}

	public void setCertifyValue(int certifyValue) {
		this.certifyValue = certifyValue;
	}

	public String call() {
		timeMessage = new String[3];
		long begin = System.currentTimeMillis();
		System.out.println(startDate);
//		SelectUsers selectUsers = new SelectUsers(clusters, 7);
//		List<WeiboInfo> rankedResult = selectUsers.getImportantUser();
//		
//		ArrayList<String> userUrlList = new ArrayList<String>();
//		ArrayList<String> userNameList = new ArrayList<String>();
//		if (rankedResult != null) {
//			for (int i = 0; i < rankedResult.size(); i++) {
//				if (i >= this.peoplenum)
//					break;
//				userUrlList.add(rankedResult.get(i).userurl);
//				userNameList.add(rankedResult.get(i).name);
//			}
//		}
//		
//		if (userUrlList.size() == 0) {
//			return "none";
//		}
//		ArrayList<String> userIdList = getIdFromUrl(userUrlList);
		
		if (keyUsers.size() + sourceUsers.size() == 0) {
			return "none";
		}
		ArrayList<String> userIdList = new ArrayList<String>();
		ArrayList<String> userNameList = new ArrayList<String>();
		HashSet<String> keyUserIds = new HashSet<String>();
		HashSet<String> sourceUserIds = new HashSet<String>();
		int peoples = 0;
		for (String[] ku : keyUsers) {
			peoples++;
			if (peoples > peoplenum) break;
			userIdList.add(ku[0]);
			userNameList.add(ku[1]);
			keyUserIds.add(ku[0]);
		}
		for (String[] su : sourceUsers) {
			if (!userIdList.contains(su[0])) {
				peoples++;
				if (peoples > peoplenum * 2) break;
				userIdList.add(su[0]);
				userNameList.add(su[1]);
				sourceUserIds.add(su[0]);
			}
		}
		
		for (String[] pu : piyaoUsers) {
			if (!userIdList.contains(pu[0])) {
				peoples++;
				if (peoples > peoplenum * 3) break;
				userIdList.add(pu[0]);
				userNameList.add(pu[1]);
			}
		}
		
		
		//只选取缓存中没有的或者已经过了有效期的人物进行爬取
		List<String> toCrawlIdList = new ArrayList<String>();
		List<String> toCrawlNameList = new ArrayList<String>();
//		for (String userId:userIdList) {
		for (int i = 0; i < userIdList.size(); ++i) {
			try {
				if (RedisUtil.hexists(ParamUtil.TIMESTAMP_MAP.getBytes("UTF-8"), userIdList.get(i).getBytes("UTF-8"))) {
				
					byte[] time = RedisUtil.hget(ParamUtil.TIMESTAMP_MAP.getBytes("UTF-8"), userIdList.get(i).getBytes("UTF-8"));
					if (time != null) {
						long lastVersionTime = (Long) SerializeUtil.unserialize(time);
						//判断有效期，暂时设为2天
						if (System.currentTimeMillis() - lastVersionTime < ParamUtil.USER_VALIDTIME * 60 * 1000) {
							continue;
						}
					}
				}
				
				toCrawlIdList.add(userIdList.get(i));
				toCrawlNameList.add(userNameList.get(i));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		//调用web service 获取userinfo
		//todo userUrlList,userNameList
		GatherWebService service = ServiceClient.getServiceInstance(is_online);
		
		//拼接任务
		String nodeData = "";
//		int userNum = userIdList.size();
		int userNum = toCrawlIdList.size();//改，2014-10-13
		for (int i = 0; i < userNum; i++) {
//			nodeData += "keyuser_" + userIdList.get(i) + "_" + userNameList.get(i) + "\t";
			nodeData += "keyuser_" + toCrawlIdList.get(i) + "_" + toCrawlNameList.get(i) + "\t";//改，2014-10-13
		}
		
//		for (String pn : participantNameList) {
//			nodeData += "party_" + pn + "\t";
//		}
		
		nodeData = nodeData.trim();
		
		long mid = 0;
		if (!nodeData.isEmpty()) { // 有用户任务要采集
			String returnFlag = "error";
			try {
				returnFlag = service.userCrawl(nodeData);
			} catch (Exception e) {
				returnFlag += "[" + nodeData + "]";
			}
			System.out.println("people service returnFlag is : " + returnFlag);
			mid = System.currentTimeMillis();
			timeMessage[0] = String.valueOf((mid - begin)/1000);
//			timeMessage[1] = userNum + ":" + participantNameList.size();
			if (!returnFlag.equals("success")) {
				log.warn("userCrawl error:" + returnFlag);
				return "error";
			}
		}
		
		for (String userId : userIdList) {
			try {
				byte[] data = RedisUtil.hget(userInfoMap.getBytes("UTF-8"), userId.getBytes("UTF-8"));
				if (data != null) {
					wUserInfo.add((WeiboUserInfo)SerializeUtil.unserialize(data));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			
			
			//新加，为了用户信息的缓存利用       2014-10-13
			if (toCrawlIdList.contains(userId)) {
				try {
					RedisUtil.hset(ParamUtil.TIMESTAMP_MAP.getBytes("UTF-8"), userId.getBytes("UTF-8"), 
							SerializeUtil.serialize((Long)System.currentTimeMillis()));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			}
			// 获取用户关注列表
			try {
				byte[] data = RedisUtil.hget(USER_FOLLOW_LIST_MAP.getBytes("UTF-8"), userId.getBytes("UTF-8"));
				if (data != null) {
					user2followeeMap.put(userId, (List<AbstractUserInfo>)SerializeUtil.unserialize(data));
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			// 获取用户微博列表
			try {
				byte[] data = RedisUtil.hget(USER_WEIBO_LIST_MAP.getBytes("UTF-8"), userId.getBytes("UTF-8"));
				if (data != null) {
					List<String[]> userHistoryWeiboPropList = (List<String[]>)SerializeUtil.unserialize(data);
					List<WeiboEntity> userHistoryWeiboList = new ArrayList<WeiboEntity>();
					for (String[] uswp : userHistoryWeiboPropList) {
						userHistoryWeiboList.add(entityFromWeiboPropArr(uswp));
					}
					userHistoryWeiboMap.put(userId, userHistoryWeiboList);
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

		// TODO: 最多获取50个关注用户的信息
		int MAX_FOLLOWEE_COUNT = 50;
		if (!sourceUsers.isEmpty()) {
			StringBuilder sourceFolloweeIds = new StringBuilder();
			List<AbstractUserInfo> sourceUserFolloweeList = user2followeeMap.get(sourceUsers.get(0));
			if (sourceUserFolloweeList != null) {
				// 组装用户信息搜索query
				int followeeSize = 0;
				for (AbstractUserInfo followee : sourceUserFolloweeList) {
					sourceFolloweeIds.append(followee.id);
					sourceFolloweeIds.append(",");
					if ((followeeSize++) >= MAX_FOLLOWEE_COUNT) {
						break;
					}
				}
				if (sourceFolloweeIds.length() > 0) {
					sourceFolloweeIds.deleteCharAt(sourceFolloweeIds.length() - 1);
				}
				String query = sourceFolloweeIds.toString();
				try {
					String returnFlag = service.userInfoCrawl(query, "0"); // 获取全部属性
					log.warn("source user's followee return:" + returnFlag);
					if (!returnFlag.equals("overtime")) {
						// 获取用户信息结果
						for (AbstractUserInfo followee : sourceUserFolloweeList) {
							try {
								byte[] data = RedisUtil.hget(ParamUtil.USER_FULL_INFO_MAP.getBytes("UTF-8"), followee.id.getBytes("UTF-8"));
								if (data != null) {
									String retJsonStr = new String(data);
									if (!retJsonStr.isEmpty()) {
										JSONObject userInfo = JSONObject.fromObject(retJsonStr);
										sourceUserFolloweeInfoList.add(userInfo);
									}
								} else {
									log.warn("user info data is null:" + returnFlag + "-" + query);
								}
							} catch (UnsupportedEncodingException e) {
								log.warn(e);
							}
						}
					}
				} catch (Exception e) {
					log.warn(e);
				}
			}
		}
		
		// 计算参与用户舆情
		//调用 web service 获取参与用户搜索结果userNameList
		PeopleInfoProcessor peopleInfoProcessor = new PeopleInfoProcessor();
		ArrayList<WeiboEntity> weiboList = null;
		List<NoneWeiboUser> participantUsers = new ArrayList<NoneWeiboUser>();
		NoneWeiboUser participantUser = new NoneWeiboUser();
		for (String userName : userNameList) {
			try {
				byte[] data = RedisUtil.hget(tmpUserSearchMap.getBytes("UTF-8"), userName.getBytes("UTF-8"));
				if (data != null) {
					weiboList = (ArrayList<WeiboEntity>)SerializeUtil.unserialize(data);
					for (WeiboEntity we : weiboList) {
						String content = we.getContent();
						ArrayList<WordNode> segs = WeiboEntityProcessor.getSegments(content);
						we.setSegs(segs);
					}
//					weiboList = WeiboEntityProcessor.weiboFilter(weiboList, userName);
					WeiboEntityProcessor.setHotrate(weiboList);
					NoneWeiboUser user = peopleInfoProcessor.getNoneWeiboUser(weiboList, userName);
					if (user != null) {
						participantUsers.add(user);
					}
				}
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("participantUser size is " + participantUsers.size());
		if (participantUsers==null||participantUsers.size()==0) {
			for (WeiboUserInfo userInfo : wUserInfo) {
				userInfo.setEmotionIndex("正常");
			}
		}else {
			participantUser = getLowEmotionParticipant(participantUsers);
			for (WeiboUserInfo userInfo : wUserInfo) {
				boolean hasName = false;
				for (NoneWeiboUser noneWeiboUser : participantUsers) {
					if (userInfo.name.equals(noneWeiboUser.name)) {
						userInfo.setEmotionIndex(String.valueOf((int)noneWeiboUser.getEmotionValue()));
						hasName = true;
						break;
					}
				}
				if (!hasName) {
					userInfo.setEmotionIndex("正常");
				}
			}		
		}

		System.out.println("wUserInfo size : " + wUserInfo.size());
		userinfo = new ArrayList<String[]>();
		
		int anomaly = 0;
		for (WeiboUserInfo b : wUserInfo) {
			String info[] = new String[15];

			info[0] = checkNull(b.name);

			if (info[0].equals("")) // 过滤为空的用户
				continue;
			if (b.approveInfo != null)
				info[1] = b.approveInfo;
			else
				info[1] = checkNull(b.approve);
			if (info[1].length() == 0) {
				info[1] = "普通用户";
			}
			info[2] = checkNull(b.career);
			info[3] = checkNull(b.edu);
			info[4] = checkNull(b.location);
			info[5] = checkNull(b.contactInfo);
			String udes = checkNull(b.description);
			if (udes.length() > 10)
				udes = udes.substring(0, 10) + "...";
			info[6] = udes;
			info[7] = "" + b.fansCount;
			
			info[8] = "";
			if (b.topLocation != null && !b.topLocation.isEmpty()) {
				info[8] = b.topLocation;
			}

			info[9] = b.getAnomalyString();
			info[10] = b.getInfluenceRatio();
			info[11] = "http://weibo.com/" + b.id;
			info[12] = "" + b.followCount;
			info[13] = "" + b.weiboCnt;
			if (sourceUserIds.contains(b.id)) {
				info[14] = "信息源";
			} else if (keyUserIds.contains(b.id)) {
				info[14] = "关键传播者";
			} else {
				
			}
			
			userinfo.add(info);
			
			int anomalyVal = 0;
			if (info[9].equals("无异常")) {
				anomalyVal = 0;
			} else {
				anomalyVal = Integer.parseInt(info[9]);
			}
			anomaly += anomalyVal;
		}
		
		System.out.println("userinfo size : " + userinfo.size());
		
		
		useremo[0] = participantUser.name;
//		useremoValue = -1 * (int) participantUser.getEmotionValue();
//		useremo[1] = "" + useremoValue;
		ArrayList<String> pos = (ArrayList<String>) participantUser.getPosWordcloud();
		ArrayList<String> neg = (ArrayList<String>) participantUser.getNegWordcloud();
		String posstr = "", negstr = "";
		for (int i = 0; i < pos.size(); i++) {
			if (i >= 5)
				break;
			posstr = posstr + " " + pos.get(i);
		}

		for (int i = 0; i < neg.size(); i++) {
			if (i >= 5)
				break;
			negstr = negstr + " " + neg.get(i);
		}
		useremo[2] = posstr;
		useremo[3] = negstr;
		
		//调用web service，查询当事人信息
		/*
		List<NoneWeiboUser> noneweibouser = new ArrayList<NoneWeiboUser>();
//		System.out.println("participantName size : " + participantNameList.size());
//		for (String name : participantNameList) {
//			try {
//				byte[] data = RedisUtil.hget(tmpPartyMap.getBytes("UTF-8"), name.getBytes("UTF-8"));
//				if (data != null) {
//					weiboList = (ArrayList<WeiboEntity>)SerializeUtil.unserialize(data);
//					for (WeiboEntity we : weiboList) {
//						String content = we.getContent();
//						ArrayList<WordNode> segs = WeiboEntityProcessor.getSegments(content);
//						we.setSegs(segs);
//					}
////					weiboList = WeiboEntityProcessor.weiboFilter(weiboList, name);
//					WeiboEntityProcessor.setHotrate(weiboList);
//					NoneWeiboUser user = peopleInfoProcessor.getNoneWeiboUser(weiboList, name);
//					if (user != null) {
//						noneweibouser.add(user);
//					}
////					noneweibouser.add(peopleInfoProcessor.getNoneWeiboUser(weiboList, name));
//				}
//			} catch (UnsupportedEncodingException e) {
//				e.printStackTrace();
//			}
//		}
		// 选出舆情最差者
		int min = 100, index = -1;
		for (int i = 0; i < noneweibouser.size(); i++) {
			int emo = (int) noneweibouser.get(i).getEmotionValue();

			if (emo <= min) {
				index = i;
				min = emo;
			}
		}
		if (index >= 0) {
			peoemo = new String[5];
			NoneWeiboUser nwu = noneweibouser.get(index);
			peoemo[0] = nwu.name;
//			peoemoValue = -1 * (int) nwu.getEmotionValue();
			
			pos = (ArrayList<String>) nwu.getPosWordcloud();
			neg = (ArrayList<String>) nwu.getNegWordcloud();
			posstr = ""; negstr = "";
			for (int i = 0; i < pos.size(); i++) {
				if (i >= 5)
					break;
				posstr = posstr + " " + pos.get(i);
			}

			for (int i = 0; i < neg.size(); i++) {
				if (i >= 5)
					break;
				negstr = negstr + " " + neg.get(i);
			}
			peoemo[2] = posstr;
			peoemo[3] = negstr;
			peoemo[4] = "peo";
		} else {
			// 取得第二舆情最差用户信息填充当事人舆情信息
			participantUsers.remove(participantUser);
			NoneWeiboUser participantUser2 = getLowEmotionParticipant(participantUsers);
			participantUsers.add(participantUser);
			
			// 构造当事人舆情信息
			if (participantUser2 != null) {
				peoemo = new String[5];
				peoemo[0] = participantUser2.name;
				peoemo[1] = "" + (-1 * (int) participantUser2.getEmotionValue());
				pos = (ArrayList<String>) participantUser2.getPosWordcloud();
				neg = (ArrayList<String>) participantUser2.getNegWordcloud();
				posstr = "";
				negstr = "";
				for (int i = 0; i < pos.size(); i++) {
					if (i >= 5)
						break;
					posstr = posstr + " " + pos.get(i);
				}
	
				for (int i = 0; i < neg.size(); i++) {
					if (i >= 5)
						break;
					negstr = negstr + " " + neg.get(i);
				}
				peoemo[2] = posstr;
				peoemo[3] = negstr;
				peoemo[4] = "user";
			}
		}
		*/
		if(startDate != ""){
			getCertifyValue();
		}
		peoemoValue = (anomaly * 5) / wUserInfo.size();
		peoemo[1] = "" + peoemoValue;
		
		timeMessage[2] = String.valueOf((System.currentTimeMillis() - mid)/1000);
		return "success";
	}
	/**
	 * 从微博属性数组重组WeiboEntity对象
	 * @param arr arr[0]-arr[14]分别是微博的属性
	 * @return WeiboEntity对象（当然仅有15个属性）
	 */
	public static WeiboEntity entityFromWeiboPropArr(String[] arr) {
		if (arr == null || arr.length < 15) {
			return null;
		}
		WeiboEntity we = new WeiboEntity();
		we.setMid(arr[0]);
		we.setContent(arr[1]);
		we.setUrl(arr[2]);
		we.setForword(arr[3]);
		we.setComment(arr[4]);
		we.setPraise(arr[5]);
		we.setFaces(str2map(arr[6]));
		we.setTime(arr[7]);
		we.setSourcePlatform(arr[8]);
		we.setWeiboLocation(arr[9]);
		we.setPiclist(new ArrayList<String>(str2list(arr[10])));
		we.setContainsVideo(Boolean.valueOf(arr[11]));
		we.setContainsUrl(Boolean.valueOf(arr[12]));
		// TODO: mentions
		//data[13] = auw.getMentions(dhtml).toString();
		we.setOrigin(Boolean.valueOf(arr[14]));
		return we;
	}
	
	/**
	 * 从List.toString()方法返回的字符串，重建List<String>对象
	 * @param str toString()返回的字符串
	 * @return List<String>对象
	 */
	public static List<String> str2list(String str) {
		List<String> list = new ArrayList<String>();
		if (str.length() < 3) {
			return list;
		}
		str = str.substring(1, str.length() - 1);
		String[] arr = str.split(", ");
		for (String astr : arr) {
			list.add(astr);
		}
		return list;
	}
	/**
	 * 从Map.toString()方法返回的字符串，重建Map<String, Double)对象
	 * @param str toString()返回的字符串
	 * @return Map<String, Double)对象
	 */
	public static Map<String, Double> str2map(String str) {
		Map<String, Double> map = new HashMap<String, Double>();
		if (str.length() < 5) {
			return map;
		}
		str = str.substring(1, str.length() - 1);
		String[] pairArr = str.split(", ");
		for (String pairStr : pairArr) {
			String[] k2v = pairStr.split("=");
			if (k2v.length < 2) {
				continue;
			}
			map.put(k2v[0], Double.valueOf(k2v[1]));
		}
		return map;
	}
			
	public static void main(String[] args) {
		GatherWebService service = ServiceClient.getServiceInstance();
		
		//拼接任务
		String nodeData = "keyuser_2306590210_sg90";
		
		String returnFlag = "error";
		try {
//			returnFlag = service.monitorSearchWeibo("鹿晗", 1);
			returnFlag = service.userCrawl(nodeData);
		}catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println(returnFlag);
	}
	
	private NoneWeiboUser getLowEmotionParticipant(
			List<NoneWeiboUser> participantUsers) {
		double min = 200;int index = -1;
		for (int i = 0; i < participantUsers.size(); i++) {
			if (participantUsers.get(i).getEmotionValue()<min) {
				min =participantUsers.get(i).getEmotionValue();
				index = i;
			}
		}
		if (index > -1) {
			NoneWeiboUser lowestOne = participantUsers.get(index);
			return lowestOne;
		} else {
			return null;
		}
	}
	
	private ArrayList<String> getIdFromUrl(List<String> userUrlList){
		ArrayList<String> mIdList = new ArrayList<String>();
		for (String userUrl : userUrlList) {
			if (userUrl!=null&&userUrl.length()!=0) {
				String[] urlAry = userUrl.split("\\/");
				mIdList.add(urlAry[urlAry.length-1]);
			}
		}
		return mIdList;
	}
		//哈哈哈哈上官老师惊不惊喜 by zyz
	private String checkNull(String s) {
		if (s != null)
			return s;
		else
			return "";
	}

	public int getPeoplenum() {
		return peoplenum;
	}

	public ArrayList<String[]> getUserinfo() {
		return userinfo;
	}

	public List<WeiboUserInfo> getwUserInfo() {
		return wUserInfo;
	}

	public String[] getUseremo() {
		return useremo;
	}

	public String[] getPeoemo() {
		return peoemo;
	}

	public int getUseremoValue() {
		return useremoValue;
	}

	public int getPeoemoValue() {
		return peoemoValue;
	}

	public Map<String, List<AbstractUserInfo>> getUser2followeeMap() {
		return user2followeeMap;
	}

	public Map<String, List<WeiboEntity>> getUserHistoryWeiboMap() {
		return userHistoryWeiboMap;
	}

	public List<JSONObject> getSourceUserFolloweeInfoList() {
		return sourceUserFolloweeInfoList;
	}
}
