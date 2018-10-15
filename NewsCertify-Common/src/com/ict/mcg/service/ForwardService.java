package com.ict.mcg.service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

import org.apache.log4j.Logger;

import com.ict.mcg.event.EmotionAnalyze;
import com.ict.mcg.forward.AnalysisForwardGraph;
import com.ict.mcg.forward.AnalysisWordCloud;
import com.ict.mcg.gather.entity.ForwardKeyWeiboInfo;
import com.ict.mcg.gather.entity.ForwardRelationEntity;
import com.ict.mcg.util.EncodeUtil;
import com.ict.mcg.util.FileIO;
import com.ict.mcg.util.ParamUtil;
import com.ict.mcg.util.RedisUtil;
import com.ict.mcg.util.SerializeUtil;
import com.ict.mcg.webservice.client.ServiceClient;
import com.ict.mcg.webservice.service.GatherWebService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ForwardService implements Callable<String>{
	private int MAXERROR = 3;
	private String gexfname;
	private String weibourl;
	private String[] weiboemo;
	private String[] weibouserinfo;
	private String[] timeMessage;
	private int weiboemovalue;
	private ArrayList<String> keyurllist;
	private ForwardRelationEntity forwardRelationEntity; 
	private Logger log = Logger.getLogger(ForwardService.class);
	private Logger logger = Logger.getLogger(this.getClass());
	private String filePath;
	private int is_online=0;//0 离线搜索，1在线搜索
	
	public void set_online(){
		is_online=1;
	}
	public ForwardRelationEntity getForwardRelationEntity() {
		return forwardRelationEntity;
	}
	
	public ForwardService(ArrayList<String> keyurllist, String filePath) {
		this.keyurllist = keyurllist;
		this.filePath = filePath;
	}
	public String getGexfname() {
		return gexfname;
	}

	public String getWeibourl() {
		return weibourl;
	}

	public String[] getWeiboemo() {
		return weiboemo;
	}

	public int getWeiboemovalue() {
		return weiboemovalue;
	}

	public String[] getWeibouserinfo() {
		return weibouserinfo;
	}

	//added by lsj
	public String getRunTimeMessage(){
		String r = "";
		for(String s : timeMessage)
			r+=s+"\t";
		return r.trim();
	}	
	
	public String call() {
		timeMessage = new String[3];
		long begin = System.currentTimeMillis();
		for (int i = 0; i < keyurllist.size(); i++) {
			if (i > MAXERROR - 1) {
				return "error";
			}
				
			String WebPath = FileIO.getFilePath();
			WebPath = WebPath.substring(0, WebPath.length() - 5);
			// ony use for test
			// String keyurl = "http://weibo.com/1314608344/zpc5k57EA";
			String keyUrl = keyurllist.get(i);
//			MainWeiboForwardAnalysis wfa = new MainWeiboForwardAnalysis();
			if (keyUrl != null && keyUrl.length() > 0){

				String forwardId = EncodeUtil.MD5(keyUrl);
				//判断是否传播网络已爬取并在有效内(通过gexf文件的修改时间判断)
				boolean isValid = false;
				
				String webPath = filePath;
				webPath = webPath.substring(0, webPath.length() - 5);
				String gexfFilePath = webPath + "gexf/wuboForward_"+getFileName(keyUrl)+".gexf";
				gexfname = gexfFilePath;
				File file = new File(gexfFilePath);
				
				if (file.exists()) {
					long lastModifiedTime = file.lastModified();
					long timeGap = System.currentTimeMillis() - lastModifiedTime;
					
					//判断有效期，暂时设为2小时
					if (timeGap < 1000*60*ParamUtil.FORWARD_VALIDTIME) {
						isValid = true;
					} /*else if (timeGap > 15*24*60*60*1000) {
						isValid = true;
					}*/
				}
				
				long mid=0;
				if (!isValid) {
					//调用web service
					//todo
					GatherWebService service = ServiceClient.getServiceInstance(is_online);
					String returnFlag = service.forwardCrawl(keyUrl);
					mid = System.currentTimeMillis();
					timeMessage[0] = String.valueOf((mid - begin) /1000);
					
					if (returnFlag.equals("none")) {
						log.warn("关键微博转发列表不可见，url:" + keyUrl);
						weibourl = keyUrl;
						timeMessage[1] ="0:0";
						timeMessage[2] = String.valueOf((System.currentTimeMillis() - mid)/1000);
						return "none";
					} else if (!returnFlag.equals("success")) {
						log.warn("forwardCrawl error:" + returnFlag);
						continue;
					}
				}
				
				ForwardRelationEntity forwardRelation = null;
				ForwardKeyWeiboInfo keyWeiboInfo = null;
				byte[] data = null;
				
				try {
					logger.info("FORWARD_RESULT_MAP:" + ParamUtil.FORWARD_RESULT_MAP);
					logger.info("FORWARD_ID:" + forwardId);
					data = RedisUtil.hget(ParamUtil.FORWARD_RESULT_MAP.getBytes("UTF-8"), forwardId.getBytes("UTF-8"));
					if (data != null) {
						forwardRelation = (ForwardRelationEntity)SerializeUtil.unserialize(data);
					}
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				System.out.println("---------FORWARD:" + forwardRelation.getForwardList().size()+":"+forwardRelation.getUserInfoList().size());
				timeMessage[1] = forwardRelation.getForwardList().size()+":"+forwardRelation.getUserInfoList().size();
				forwardRelationEntity = forwardRelation;
				
				keyWeiboInfo = forwardRelation.getKeyWeiboInfo();
				weibouserinfo = new String[]{keyWeiboInfo.getaName(), keyWeiboInfo.getaLogoUrl(), keyWeiboInfo.getaGender(),
						keyWeiboInfo.getaLocation(), keyWeiboInfo.getaFollowCount(), keyWeiboInfo.getaFanCount(),
						keyWeiboInfo.getaWeiboCount(), keyWeiboInfo.getaDescription(), keyWeiboInfo.getwContent(),
						keyWeiboInfo.getwForwardCount(), keyWeiboInfo.getwCommentCount(), keyWeiboInfo.getwTime()};
				
				if (!isValid) {
					String[] authorinfo = new String[]{keyWeiboInfo.getaId(), keyWeiboInfo.getaName(), keyWeiboInfo.getaAprove(),
							keyWeiboInfo.getaGender(), keyWeiboInfo.getaFollowCount(), keyWeiboInfo.getaFanCount(), 
							keyWeiboInfo.getaWeiboCount(), keyWeiboInfo.getaLocation(), keyWeiboInfo.getaDescription(), 
							keyWeiboInfo.getwId()};
	
					// 预先生成传播图
					CountDownLatch analysislatch = new CountDownLatch(1);
					AnalysisForwardGraph graphanalysis = new AnalysisForwardGraph(keyWeiboInfo.getwId(), authorinfo, 
							keyWeiboInfo.getInputWid(), keyWeiboInfo.getInputAname(), forwardRelation.getForwardList(), 
							forwardRelation.getUserInfoList(), gexfFilePath, analysislatch);
					Thread graphtread=new Thread(graphanalysis);
					graphtread.start();
				}
				
				// 获得关键词集合的情感值
				JSONArray wc = new AnalysisWordCloud().calculateWordCloud(forwardRelation.getForwardList());
				Set<String> weibowordcloud = new HashSet<String>();
				if (wc != null) {
					for (int j = 0; j < wc.size(); j++) {
						JSONObject jo = (JSONObject) wc.get(j);
						String t = jo.getString("text");
						weibowordcloud.add(t);
					}
				}
				EmotionAnalyze ea = new EmotionAnalyze();
				weiboemo = new String[3];
				weiboemovalue = -1 * ea.computeEmotion(weibowordcloud);
				weiboemo[0] = "" + weiboemovalue;
				ArrayList<String> weibopos = ea.getPos();
				String wpstr = "";
				for (int j = 0; j < weibopos.size(); j++) {
					if (j >= 5)
						break;
					wpstr = wpstr + " " + weibopos.get(j);
				}
				weiboemo[1] = wpstr;
				ArrayList<String> weiboneg = ea.getNeg();
				String wnstr = "";
				for (int j = 0; j < weiboneg.size(); j++) {
					if (j >= 5)
						break;
					wnstr = wnstr + " " + weiboneg.get(j);
				}
				weiboemo[2] = wnstr;

				weibourl = keyUrl;

				timeMessage[2] = String.valueOf((System.currentTimeMillis() - mid)/1000);
				return "success";
			}
		}
		
		return "没有形成有规模的传播";
	}
	
	public static String getFileName(String url) {
        String filename = "";
        
        // 从路径中获取
        if (filename == null || "".equals(filename)) {
        	if (url.contains("?")) {
        		url = url.substring(0, url.indexOf("?"));
        	}
            filename = url.substring(url.lastIndexOf("/") + 1);
            url = url.replaceAll("/"+filename, "");
            filename = url.substring(url.lastIndexOf("/") + 1)+"-"+filename;
        }
        return filename;
    }
	
	public static void writeGexf(String keyUrl, String gexfFilePath) {
		ForwardRelationEntity forwardRelation = null;
		ForwardKeyWeiboInfo keyWeiboInfo = null;
		
		String forwardId = EncodeUtil.MD5(keyUrl);
		
		byte[] data = null;
		try {
			data = RedisUtil.hget(ParamUtil.FORWARD_RESULT_MAP.getBytes("UTF-8"), forwardId.getBytes("UTF-8"));
			if (data != null) {
				forwardRelation = (ForwardRelationEntity)SerializeUtil.unserialize(data);
			}
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}

		if (forwardRelation == null) {
			System.out.println("forward null");
			return;
		}
		
		System.out.println("---------FORWARD:" + forwardRelation.getForwardList().size()+":"+forwardRelation.getUserInfoList().size());
		
		keyWeiboInfo = forwardRelation.getKeyWeiboInfo();

		String[] authorinfo = new String[]{keyWeiboInfo.getaId(), keyWeiboInfo.getaName(), keyWeiboInfo.getaAprove(),
				keyWeiboInfo.getaGender(), keyWeiboInfo.getaFollowCount(), keyWeiboInfo.getaFanCount(), 
				keyWeiboInfo.getaWeiboCount(), keyWeiboInfo.getaLocation(), keyWeiboInfo.getaDescription(), 
				keyWeiboInfo.getwId()};

		// 预先生成传播图
		CountDownLatch analysislatch = new CountDownLatch(1);
		AnalysisForwardGraph graphanalysis = new AnalysisForwardGraph(keyWeiboInfo.getwId(), authorinfo, 
				keyWeiboInfo.getInputWid(), keyWeiboInfo.getInputAname(), forwardRelation.getForwardList(), 
				forwardRelation.getUserInfoList(), gexfFilePath, analysislatch);
		Thread graphtread=new Thread(graphanalysis);
		graphtread.start();
	}
	
	public static void main(String[] args) {
		String keyUrl = "http://weibo.com/1699432410/FrZjj9hsi";
		
		String filePath = "D:\\project\\newscertify\\source\\newsverify.com\\NewsCertify-WebOffline\\FrZjj9hsi.gexf";
		
//		ArrayList<String> urllist = new ArrayList<String>();
//		urllist.add(keyUrl);
//		ForwardService fs = new ForwardService(urllist,filePath);
//		if (urllist.size() >= 1) {
//			String returnFlag = fs.call();
//			System.out.println(returnFlag);
//		}
        
		GatherWebService service = ServiceClient.getServiceInstance();
		String returnFlag = service.forwardCrawl(keyUrl);
		System.out.println(returnFlag);
		        
//		writeGexf("//weibo.com/1784473157/FEKnaAP3C", "D:\\project\\newscertify\\source\\newsverify.com\\NewsCertify-WebOffline\\FEKnaAP3C.gexf");
//		System.out.println(ForwardService.getFileName("http://weibo.com/1644114654/FENHYws8S"));
	}
}
