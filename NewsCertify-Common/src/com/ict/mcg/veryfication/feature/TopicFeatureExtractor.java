package com.ict.mcg.veryfication.feature;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ict.mcg.gather.entity.WeiboEntity;


public class TopicFeatureExtractor {
	private static Logger log = Logger.getRootLogger();
	
	public static boolean isFromThirdpart(String content) {
		String[] pattern = {"网友称", "爆料称", "据说", "消息称", "援引知情人士", "爆料", "网传", "据称"};
		for (String pat:pattern) {
			if (content.contains(pat)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isClarity(String content) {
		String[] pattern = {"谣言", "假的", "失实", "虚假", "辟谣", "传谣"};
		for (String pat:pattern) {
			if (content.contains(pat)) {
				return true;
			}
		}
		
		return false;
	}
	
	public double[] extractOneTopic(ArrayList<WeiboEntity> wel, int label) {

		// 针对每条微博提取特征
		int clarityCnt = 0;
		int fromThirdPartCnt = 0;
		ArrayList<MessageFeature> mflist = new ArrayList<MessageFeature>();
		ArrayList<String> usernamelist = new ArrayList<String>();
		HashMap<String, Integer> nameset = new HashMap<String, Integer>();
		for (int i = 0; i < wel.size(); i++) {
			MessageFeatureExtractor mfe = new MessageFeatureExtractor(
					wel.get(i));
			
			try {
				MessageFeature mf = mfe.extract();
				mflist.add(mf);
			} catch (Exception e) {
				log.warn(wel.get(i).getMid() + ":" + e.getMessage(), e);
			}
			String username = wel.get(i).getName();
			if (!nameset.containsKey(username)) {
				nameset.put(username, 1);
				usernamelist.add(username);
			} else {
				int v = nameset.get(username);
				nameset.put(username, v + 1);
			}
			
			if (isClarity(wel.get(i).getContent())) {
				clarityCnt++;
			}
			if (isFromThirdpart(wel.get(i).getContent())) {
				fromThirdPartCnt++;
			}
		}

		/*
		 * 微博信息计数器 共21项，分别记录： 转发评论量,非零转发评论量,赞数量,问号、叹号、多问号、多叹号、三个人称、字数、词数、
		 * url、@、#、三类命名实体、情感打分,正面情感、负面情感, img, 多img
		 */
		int[] count = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0};
		// 统计最大评论转发量
		int max = 0;

		int allImg = 0; // img总数

		/*
		 * 统计出现的url、@、#、三类命名实体
		 */
		HashMap<String, Integer> urlmap = new HashMap<String, Integer>();
		HashMap<String, Integer> atmap = new HashMap<String, Integer>();
		HashMap<String, Integer> hashtagmap = new HashMap<String, Integer>();
		HashMap<String, Integer> peomap = new HashMap<String, Integer>();
		HashMap<String, Integer> locmap = new HashMap<String, Integer>();
		HashMap<String, Integer> orgmap = new HashMap<String, Integer>();

		// 统计出现的img
		HashMap<String, Integer> imgmap = new HashMap<String, Integer>();

		for (MessageFeature mf : mflist) {
			int forword = mf.getForword();
			int comment = mf.getComment();
			int praise = mf.getPraise();
			ArrayList<String> urllist = mf.getUrllist();
			ArrayList<String> atlist = mf.getAtlist();
			ArrayList<String> hashtaglist = mf.getHashtaglist();
			ArrayList<ArrayList<String>> nameentitylist = mf
					.getNameentitylist();
			int qcount = mf.getQcount(); // question mark count;
			int ecount = mf.getEcount(); // exclaim mark count
			int[] wcount = mf.getWcount(); // word count, charactor count 2
			int[] pecount = mf.getPecount(); // three personality count 3
			int[] emocount = mf.getEmocount(); // emotional count 3 pos, neg,
			// score

			List<String> imglist = mf.getImgList();

			// 开始计数
			int pcnt = forword + comment;
			if (pcnt > max)
				max = pcnt;
			count[0] += pcnt;
			if (pcnt > 0) {
				count[1] += 1;
			}
			if (qcount > 1) {
				count[3] += 1;
			} else if (qcount > 0) {
				count[2] += 1;
			}
			if (ecount > 1) {
				count[5] += 1;
			} else if (ecount > 0) {
				count[4] += 1;
			}
			if (pecount[0] > 0) {
				count[6] += 1;
			}
			if (pecount[1] > 0) {
				count[7] += 1;
			}
			if (pecount[2] > 0) {
				count[8] += 1;
			}
			count[9] += wcount[0];
			count[10] += wcount[1];
			if (urllist.size() > 0) {
				count[11] += 1;
			}
			if (atlist.size() > 0) {
				count[12] += 1;
			}
			if (hashtaglist.size() > 0) {
				count[13] += 1;
			}
			if (nameentitylist.get(0).size() > 0) {
				count[14] += 1;
			}
			if (nameentitylist.get(1).size() > 0) {
				count[15] += 1;
			}
			if (nameentitylist.get(2).size() > 0) {
				count[16] += 1;
			}
			count[17] += emocount[2];
			if (emocount[2] > 0) {
				count[18] += 1;
			} else if (emocount[2] < 0) {
				count[19] += 1;
			}

			if (imglist != null) {
				if (imglist.size() > 1) {
					count[21] += 1;
					count[20] +=1;
				} else if (imglist.size() > 0) {
					count[20] += 1;
				}
			}
			
			count[22] += praise;

			/*
			 * 统计出现的url、@、#、三类命名实体
			 */

			for (String s : urllist) {
				if (urlmap.containsKey(s)) {
					int v = urlmap.get(s);
					urlmap.put(s, v + 1);
				} else {
					urlmap.put(s, 1);
				}
			}
			for (String s : atlist) {
				if (atmap.containsKey(s)) {
					int v = atmap.get(s);
					atmap.put(s, v + 1);
				} else {
					atmap.put(s, 1);
				}
			}
			for (String s : hashtaglist) {
				if (hashtagmap.containsKey(s)) {
					int v = hashtagmap.get(s);
					hashtagmap.put(s, v + 1);
				} else {
					hashtagmap.put(s, 1);
				}
			}
			for (String s : nameentitylist.get(0)) {
				if (peomap.containsKey(s)) {
					int v = peomap.get(s);
					peomap.put(s, v + 1);
				} else {
					peomap.put(s, 1);
				}
			}
			for (String s : nameentitylist.get(1)) {
				if (orgmap.containsKey(s)) {
					int v = orgmap.get(s);
					orgmap.put(s, v + 1);
				} else {
					orgmap.put(s, 1);
				}
			}
			for (String s : nameentitylist.get(2)) {
				if (locmap.containsKey(s)) {
					int v = locmap.get(s);
					locmap.put(s, v + 1);
				} else {
					locmap.put(s, 1);
				}
			}
			// 统计img

			if(imglist!=null){
				for (String s : imglist) {
					if (imgmap.containsKey(s)) {
						int v = imgmap.get(s);
						imgmap.put(s, v + 1);
					} else {
						imgmap.put(s, 1);
					}
				}
				allImg += imglist.size();
			}
		}

		// 计算微博内容与传播相关的各类特征
		int msgcount = wel.size();// 微博总数
//		int avgProp = count[0] / msgcount; // 平均转发评论量
//		int avgNZProp = 0; // 平均非0转发评论量
//		if (count[1] > 0)
//			avgNZProp = count[0] / count[1];
		int avgPraise = count[22] / msgcount; //平均每条微博获赞的数量
		double rateEM = (double) count[4] / msgcount; // 单个叹号的比例
		double rateEMs = (double) count[5] / msgcount; // 多个叹号的比例
		double rateQM = (double) count[2] / msgcount; // 单个问号的比例
		double rateQMs = (double) count[3] / msgcount; // 多个问号的比例
//		double avgWord = (double) count[9] / msgcount; // 平均词数
//		double avgChar = (double) count[10] / msgcount; // 平均字数
//		double rateUrl = (double) count[11] / msgcount; // url比例
//		double rateAt = (double) count[12] / msgcount;// at比例
//		double ratePeo = (double) count[14] / msgcount; // 人名比例
//		double rateNeg = (double) count[19] / msgcount; // 负面比例

		double rateImg = (double) count[20] / msgcount; // 图片比例
		double rateImgs = (double) count[21] / msgcount; // 多图片比例
		double rateImgs2 = 0;
		if (count[20] > 0) {
			rateImgs2 = (double) count[21] / count[20]; // 多图片占所有图片比例
		}

		int disAuth = nameset.size();
		double rateAuth = (double) this.getLargsetValueforMap(nameset)
				/ msgcount; // 最热门作者比例
//		int disUrl = urlmap.size(); // 不同url数量
		int disAt = atmap.size(); // 不同at数量
//		int disHashtag = hashtagmap.size(); // 不同hashtag数量
//		double rateHotpeo = (double) this.getLargsetValueforMap(peomap)
//				/ msgcount; // 最热门people比例
		int disLoc = locmap.size(); // 不同location数量

		/*
		 * 获取user特征
		 */
		ArrayList<WeiboEntity> uflist = new ArrayList<WeiboEntity>();
		Set<String> userSet = new HashSet<String>();
		
		for (WeiboEntity we : wel) {
			if (!userSet.contains(we.getName())) {
				uflist.add(we);
				userSet.add(we.getName());
			}
		}
		/*
		 * 用户信息计数器 共11项，分别记录： 已发微博数,粉丝数,关注数、认证数、描述、性别、联系信息、 教育信息，地理信息，标签，职业
		 */
		
		int[] userinfocount = { 0, 0, 0, 0, 0};
		int[] userfandiscrete = {0,0,0,0,0,0};

		for (WeiboEntity uf : uflist) {
			try {
				userinfocount[0] += Integer.parseInt(uf.getUserWeiboCount());
			} catch (Exception e) {
				System.out.println("TopicFeatureExtractor 310L:" + e.getMessage());
			}
			try {
				userinfocount[1] += Integer.parseInt(uf.getUserFanCount());
			} catch (Exception e) {
				System.out.println("TopicFeatureExtractor 315L:" + e.getMessage());
			}
			try {
				userinfocount[2] += Integer.parseInt(uf.getUserFollowCount());
			} catch (Exception e) {
				System.out.println("TopicFeatureExtractor 320L:" + e.getMessage());
			}
			if (uf.getUserCertify() == 1) {
				userinfocount[3] += 1;
			}
			if (uf.getUserCertify() == 2) {
				userinfocount[4] += 1;
			}
			
			int fancout = 0;
			try {
				fancout = Integer.parseInt(uf.getUserFanCount());
			} catch (Exception e) {
				System.out.println("TopicFeatureExtractor 333L:" + e.getMessage());
			}
			if (fancout < 100) {
				userfandiscrete[0]++;
			} else if (fancout < 1000) {
				userfandiscrete[1]++;
			} else if (fancout < 10000) {
				userfandiscrete[2]++;
			} else if (fancout < 100000) {
				userfandiscrete[3]++;
			} else if (fancout < 1000000) {
				userfandiscrete[4]++;
			} else {
				userfandiscrete[5]++;
			}
		}
		// 计算微博用户相关的各类特征
		int usercount = uflist.size();// 不同用户数（不包括没抓取到信息的用户）
//		int avgWecnt = userinfocount[0] / usercount; // 平均已发微博数
		int avgFacnt = userinfocount[1] / usercount; // 平均粉丝数
		double ratePerApp = (double) userinfocount[3] / usercount; // 个人认证比例
		
//		double avgImg = (double)allImg / msgcount;
		double rateDisImg = 0.0;
		if (allImg > 0) {
			int disImg = imgmap.size();// 不同img数量
			rateDisImg = (double) disImg / allImg;// 不同img比例
		}
		
		double clarityRate = (double) clarityCnt / msgcount;
		double thirdPartyRate = (double) fromThirdPartCnt / msgcount;
		double fan1000Rate = (double) userfandiscrete[1] / usercount;
		
		int originOrgCnt = 0;
		for (int j = 0; j < 10 && j < wel.size(); ++j) {
			if (wel.get(j).getUserCertify() == 2 && wel.get(j).isOrigin()) {
				if (isClarity(wel.get(j).getContent()) || isFromThirdpart(wel.get(j).getContent())) {
				} else {
					originOrgCnt++;
				}
			}
		}
		
		double[] features = new double[19]; 
		features[0] = avgPraise;
		features[1] = rateQM;
		features[2] = rateQMs;
		features[3] = rateEM;
		features[4] = rateEMs;
		features[5] = disAuth;
		features[6] = rateAuth;
		features[7] = disAt;
		features[8] = disLoc;
		features[9] = avgFacnt;
		features[10] = ratePerApp;
		features[11] = rateImg;
		features[12] = rateImgs;
		features[13] = rateImgs2;
		features[14] = rateDisImg;
		features[15] = clarityRate;
		features[16] = thirdPartyRate;
		features[17] = fan1000Rate;
		features[18] = originOrgCnt;
		
		return features;
		
	}
	
	public String extractOneTopic(ArrayList<WeiboEntity> wel, int label,
			String category, String filepath) {
		
		if (wel.size() == 0) {
			return "";
		}

		// 针对每条微博提取特征
		ArrayList<MessageFeature> mflist = new ArrayList<MessageFeature>();
		ArrayList<String> usernamelist = new ArrayList<String>();
		HashMap<String, Integer> nameset = new HashMap<String, Integer>();
		for (int i = 0; i < wel.size(); i++) {
			MessageFeatureExtractor mfe = new MessageFeatureExtractor(
					wel.get(i));
			
			try {
				MessageFeature mf = mfe.extract();
				mflist.add(mf);
			} catch (Exception e) {
				log.warn(wel.get(i).getMid() + ":" + e.getMessage(), e);
			}
			String username = wel.get(i).getName();
			if (!nameset.containsKey(username)) {
				nameset.put(username, 1);
				usernamelist.add(username);
			} else {
				int v = nameset.get(username);
				nameset.put(username, v + 1);
			}
		}

		/*
		 * 微博信息计数器 共21项，分别记录： 转发评论量,非零转发评论量,赞数量,问号、叹号、多问号、多叹号、三个人称、字数、词数、
		 * url、@、#、三类命名实体、情感打分,正面情感、负面情感, img, 多img
		 */
		int[] count = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0};
		// 统计最大评论转发量
		int max = 0;

		int allImg = 0; // img总数

		/*
		 * 统计出现的url、@、#、三类命名实体
		 */
		HashMap<String, Integer> urlmap = new HashMap<String, Integer>();
		HashMap<String, Integer> atmap = new HashMap<String, Integer>();
		HashMap<String, Integer> hashtagmap = new HashMap<String, Integer>();
		HashMap<String, Integer> peomap = new HashMap<String, Integer>();
		HashMap<String, Integer> locmap = new HashMap<String, Integer>();
		HashMap<String, Integer> orgmap = new HashMap<String, Integer>();

		// 统计出现的img
		HashMap<String, Integer> imgmap = new HashMap<String, Integer>();

		for (MessageFeature mf : mflist) {
			int forword = mf.getForword();
			int comment = mf.getComment();
			int praise = mf.getPraise();
			ArrayList<String> urllist = mf.getUrllist();
			ArrayList<String> atlist = mf.getAtlist();
			ArrayList<String> hashtaglist = mf.getHashtaglist();
			ArrayList<ArrayList<String>> nameentitylist = mf
					.getNameentitylist();
			int qcount = mf.getQcount(); // question mark count;
			int ecount = mf.getEcount(); // exclaim mark count
			int[] wcount = mf.getWcount(); // word count, charactor count 2
			int[] pecount = mf.getPecount(); // three personality count 3
			int[] emocount = mf.getEmocount(); // emotional count 3 pos, neg,
			// score

			List<String> imglist = mf.getImgList();

			// 开始计数
			int pcnt = forword + comment;
			if (pcnt > max)
				max = pcnt;
			count[0] += pcnt;
			if (pcnt > 0) {
				count[1] += 1;
			}
			if (qcount > 1) {
				count[3] += 1;
			} else if (qcount > 0) {
				count[2] += 1;
			}
			if (ecount > 1) {
				count[5] += 1;
			} else if (ecount > 0) {
				count[4] += 1;
			}
			if (pecount[0] > 0) {
				count[6] += 1;
			}
			if (pecount[1] > 0) {
				count[7] += 1;
			}
			if (pecount[2] > 0) {
				count[8] += 1;
			}
			count[9] += wcount[0];
			count[10] += wcount[1];
			if (urllist.size() > 0) {
				count[11] += 1;
			}
			if (atlist.size() > 0) {
				count[12] += 1;
			}
			if (hashtaglist.size() > 0) {
				count[13] += 1;
			}
			if (nameentitylist.get(0).size() > 0) {
				count[14] += 1;
			}
			if (nameentitylist.get(1).size() > 0) {
				count[15] += 1;
			}
			if (nameentitylist.get(2).size() > 0) {
				count[16] += 1;
			}
			count[17] += emocount[2];
			if (emocount[2] > 0) {
				count[18] += 1;
			} else if (emocount[2] < 0) {
				count[19] += 1;
			}

			if (imglist != null) {
				if (imglist.size() > 1) {
					count[21] += 1;
					count[20] +=1;
				} else if (imglist.size() > 0) {
					count[20] += 1;
				}
			}
			
			count[22] += praise;

			/*
			 * 统计出现的url、@、#、三类命名实体
			 */

			for (String s : urllist) {
				if (urlmap.containsKey(s)) {
					int v = urlmap.get(s);
					urlmap.put(s, v + 1);
				} else {
					urlmap.put(s, 1);
				}
			}
			for (String s : atlist) {
				if (atmap.containsKey(s)) {
					int v = atmap.get(s);
					atmap.put(s, v + 1);
				} else {
					atmap.put(s, 1);
				}
			}
			for (String s : hashtaglist) {
				if (hashtagmap.containsKey(s)) {
					int v = hashtagmap.get(s);
					hashtagmap.put(s, v + 1);
				} else {
					hashtagmap.put(s, 1);
				}
			}
			for (String s : nameentitylist.get(0)) {
				if (peomap.containsKey(s)) {
					int v = peomap.get(s);
					peomap.put(s, v + 1);
				} else {
					peomap.put(s, 1);
				}
			}
			for (String s : nameentitylist.get(1)) {
				if (orgmap.containsKey(s)) {
					int v = orgmap.get(s);
					orgmap.put(s, v + 1);
				} else {
					orgmap.put(s, 1);
				}
			}
			for (String s : nameentitylist.get(2)) {
				if (locmap.containsKey(s)) {
					int v = locmap.get(s);
					locmap.put(s, v + 1);
				} else {
					locmap.put(s, 1);
				}
			}
			// 统计img

			for (String s : imglist) {
				if (imgmap.containsKey(s)) {
					int v = imgmap.get(s);
					imgmap.put(s, v + 1);
				} else {
					imgmap.put(s, 1);
				}
			}
			allImg += imglist.size();
		}

		// 计算微博内容与传播相关的各类特征
		int msgcount = wel.size();// 微博总数
		int avgProp = count[0] / msgcount; // 平均转发评论量
		int avgNZProp = 0; // 平均非0转发评论量
		if (count[1] > 0)
			avgNZProp = count[0] / count[1];
		int avgPraise = count[22] / msgcount; //平均每条微博获赞的数量
		double rateQM = (double) count[2] / msgcount; // 单个问号的比例
		double rateQMs = (double) count[3] / msgcount; // 多个问号的比例
		double avgWord = (double) count[9] / msgcount; // 平均词数
		double avgChar = (double) count[10] / msgcount; // 平均字数
		double rateUrl = (double) count[11] / msgcount; // url比例
		double rateAt = (double) count[12] / msgcount;// at比例
		double ratePeo = (double) count[14] / msgcount; // 人名比例
		double rateNeg = (double) count[19] / msgcount; // 负面比例

		double rateImgs = (double) count[21] / msgcount; // 多图片比例
		double rateImgs2 = 0;
		if (count[20] > 0) {
			rateImgs2 = (double) count[21] / count[20]; // 多图片占所有图片比例
		}

		int disAuth = nameset.size();
		double rateAuth = (double) this.getLargsetValueforMap(nameset)
				/ msgcount; // 最热门作者比例
		int disUrl = urlmap.size(); // 不同url数量
		int disAt = atmap.size(); // 不同at数量
		int disHashtag = hashtagmap.size(); // 不同hashtag数量
		double rateHotpeo = (double) this.getLargsetValueforMap(peomap)
				/ msgcount; // 最热门people比例
		int disLoc = locmap.size(); // 不同location数量

		/*
		 * 获取user特征
		 */
		ArrayList<WeiboEntity> uflist = new ArrayList<WeiboEntity>();
		Set<String> userSet = new HashSet<String>();
		
		for (WeiboEntity we : wel) {
			if (!userSet.contains(we.getName())) {
				uflist.add(we);
				userSet.add(we.getName());
			}
		}
		/*
		 * 用户信息计数器 共11项，分别记录： 已发微博数,粉丝数,关注数、认证数、描述、性别、联系信息、 教育信息，地理信息，标签，职业
		 */
		int[] userinfocount = { 0, 0, 0, 0, 0};

		for (WeiboEntity uf : uflist) {
			userinfocount[0] += Integer.parseInt(uf.getUserWeiboCount());
			userinfocount[1] += Integer.parseInt(uf.getUserFanCount());
			userinfocount[2] += Integer.parseInt(uf.getUserFollowCount());
			if (uf.getUserCertify() == 1) {
				userinfocount[3] += 1;
			}
			if (uf.getUserCertify() == 2) {
				userinfocount[4] += 1;
			}
		}
		// 计算微博用户相关的各类特征
		int usercount = uflist.size();// 不同用户数（不包括没抓取到信息的用户）
		int avgWecnt = userinfocount[0] / usercount; // 平均已发微博数
		int avgFacnt = userinfocount[1] / usercount; // 平均粉丝数
		double ratePerApp = (double) userinfocount[3] / usercount; // 个人认证比例
		
		double avgImg = (double)allImg / msgcount;

		
		String[] cats = category.split(",");
		
		StringBuffer buffer = new StringBuffer();
		buffer.append(label);
		buffer.append(" 1:" + normalize(msgcount, 1116, 12));
		buffer.append(" 2:" + normalize(avgProp, 3425, 0));
		buffer.append(" 3:" + normalize(avgNZProp, 21721,2));
		buffer.append(" 4:" + normalize(avgPraise, 7771, 0));
		buffer.append(" 5:" + normalize(rateQM, 0.84, 0));
		buffer.append(" 6:" + normalize(rateQMs, 0.95, 0));
		buffer.append(" 7:" + normalize(avgWord, 45.23, 3.95));
		buffer.append(" 8:" + normalize(avgChar, 98.86, 8.05));
		buffer.append(" 9:" + normalize(rateUrl, 0.97, 0));
		buffer.append(" 10:" + normalize(rateAt, 0.96, 0));
		buffer.append(" 11:" + normalize(ratePeo, 1, 0));
		buffer.append(" 12:" + normalize(rateNeg, 1, 0));
		buffer.append(" 13:" + normalize(disAuth, 1058, 12));
		buffer.append(" 14:" + normalize(rateAuth, 0.55, 0));
		buffer.append(" 15:" + normalize(disUrl, 473, 0));
		buffer.append(" 16:" + normalize(disAt, 614, 0));
		buffer.append(" 17:" + normalize(disHashtag, 134, 0));
		buffer.append(" 18:" + normalize(rateHotpeo, 2.6, 0));
		buffer.append(" 19:" + normalize(disLoc, 125, 0));
		buffer.append(" 20:" + normalize(avgWecnt, 258922, 726));
		buffer.append(" 21:" + normalize(avgFacnt, 3588566, 2891));
		buffer.append(" 22:" + normalize(ratePerApp, 0.7, 0));
		buffer.append(" 23:" + normalize(rateImgs, 0.95, 0));
		buffer.append(" 24:" + normalize(rateImgs2, 1, 0));
		buffer.append(" 25:" + cats[0]);
		buffer.append(" 26:" + cats[1]);
		buffer.append(" 27:" + cats[2]);
		buffer.append(" 28:" + cats[3]);
		buffer.append(" 29:" + cats[4]);
		buffer.append(" 30:" + cats[5]);
		buffer.append(" 31:" + cats[6]);
		buffer.append(" 32:" + cats[7]);
		buffer.append(" 33:" + cats[8]);
		buffer.append(" 34:" + cats[9]);
		buffer.append(" 35:" + normalize(allImg, 1575, 0));
		buffer.append(" 36:" + normalize(avgImg, 22.582, 0));
		
		/*String result = label + " 1:"
				+ normalize(msgcount, 1116,12) + " 2:"
				+ normalize(avgProp, 3425, 0) + " 3:"
				+ normalize(avgNZProp, 21721,2) + " 4:"
				+ normalize(avgPraise, 7771, 0) + " 5:"
				+ normalize(rateQM, 0.84, 0) + " 6:"
				+ normalize(rateQMs, 0.95, 0) + " 7:"
				+ normalize(avgWord, 45.23, 3.95) + " 8:"
				+ normalize(avgChar, 98.86, 8.05) + " 9:"
				+ normalize(rateUrl, 0.97, 0) + " 10:"
				+ normalize(rateAt, 0.96, 0) + " 11:"
				+ normalize(ratePeo, 1, 0) + " 12:"
				+ normalize(rateNeg, 1, 0) + " 13:" 
				+ normalize(disAuth, 1058, 12) + " 14:"
				+ normalize(rateAuth, 0.55, 0) + " 15:"
				+ normalize(disUrl, 473, 0) + " 16:"
				+ normalize(disAt, 614, 0) + " 17:"
				+ normalize(disHashtag, 134, 0) + " 18:"
				+ normalize(rateHotpeo, 2.6, 0) + " 19:"
				+ normalize(disLoc, 125, 0) + " 20:"
				+ normalize(avgWecnt, 258922, 726) + " 21:"
				+ normalize(avgFacnt, 3588566, 2891) + " 22:"
				+ normalize(ratePerApp, 0.7, 0) + " 23:"
				+ normalize(rateImgs, 0.95, 0) + " 24:"
				+ normalize(rateImgs2, 1, 0) + " 25:"
				+ cats[0] + " 26:"
				+ cats[1] + " 27:"
				+ cats[2] + " 28:"
				+ cats[3] + " 29:"
				+ cats[4] + " 30:"
				+ cats[5] + " 31:"
				+ cats[6] + " 32:"
				+ cats[7] + " 33:"
				+ cats[8] + " 34:"
				+ cats[9] + " 35:"
				+ normalize(allImg, 1575, 0) + " 36:"
				+ normalize(avgImg, 22.582, 0);*/
		File outfile = new File(filepath);
		try {
			Writer writer = new FileWriter(outfile);
			writer.write(buffer.toString() + "\r\n");
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return buffer.toString();
	}
	
	private String normalize(double value, double max, double min) {
		double d = (value - min) / (max - min);
		if (d > 1)
			d = 1;
		if (d < 0)
			d = 0;
		String result = String.format("%.6f", d);
		return result;
	}

	/**
	 * 从map中选出值最大的元素
	 * 
	 * @param map
	 * @return
	 */
	private int getLargsetValueforMap(HashMap<String, Integer> map) {
		if (map.size() < 1)
			return 0;
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				map.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue() - o1.getValue());
			}
		});
		return list.get(0).getValue();

	}

	public Map<String, String> getCategoryFeatures(String categoryFile) throws IOException {
		Map<String, String> catMap = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(categoryFile), "UTF-8"));
		if (reader.ready()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] kv = line.split("\t");
				String value = "";
				int cat = Integer.parseInt(kv[1]);
				for (int i = 0; i < 10; ++i) {
					if (cat == i) {
						value += "1,";
					} else {
						value += "0,";
					}
				}
				value = value.substring(0, value.length()-1);
				catMap.put(kv[0], value);
			}
		}
		
		reader.close();
		return catMap;
	}
	
	public Map<String, String> getCategoryAndPicFeatures(String categoryFile) throws IOException {
		Map<String, String> catMap = new HashMap<String, String>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(categoryFile), "UTF-8"));
		if (reader.ready()) {
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] kv = line.split("\t");
				String value = "";
				int cat = Integer.parseInt(kv[1].substring(0,kv[1].indexOf(",")));
				for (int i = 0; i < 10; ++i) {
					if (cat == i) {
						value += "1,";
					} else {
						value += "0,";
					}
				}
//				value = value.substring(0, value.length()-1);
				value = value + kv[1].substring(kv[1].indexOf(",")+1);
				catMap.put(kv[0], value);
			}
		}
		
		reader.close();
		return catMap;
	}

}
