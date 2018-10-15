package com.ict.mcg.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ict.mcg.gather.entity.WeiboUserInfo;

public class UserService {
	
	private JSONArray genderDistri = new JSONArray(); //性别分布，分别为男，女的比例
	private JSONArray certifyDistri = new JSONArray();  //认证类型分布，分别为未认证，个人认证和机构认证的比例
	private JSONArray locationDistri = new JSONArray();  //用户的位置分布
	private JSONArray fansDistri = new JSONArray();  //用户粉丝数量分布
	
	private List<WeiboUserInfo> usersInfo;
	private final int[] fansDistriNodes = {0, 10, 50, 100, 500, 1000, 2000, 5000, 10000, 100000};
	private final String[] provinceArray = {"其他","海外","辽宁","吉林","黑龙江","河北","山西","陕西","山东","安徽","江苏","浙江","河南","湖北","湖南","江西","台湾","福建","云南","海南","四川","贵州","广东","甘肃","青海","西藏","新疆","广西","内蒙古","宁夏","北京","天津","上海","重庆"};
	public int execute(List<WeiboUserInfo> users){
		
		if(users==null || users.size() == 0)
			return 0;
		
		usersInfo = new ArrayList<WeiboUserInfo>(users);
		
		//获得性别分布
		genderAnalyze();
		
		//获得用户认证分布
		certifyAnalyze();
		
		//获得用户粉丝分布
		fansDistriAnalyze();
		
		//获得用户位置分布
		locationDistriAnalyze();
		
		return 0;
	}
	
	public JSONArray genderAnalyze(){
		int maleCnt = 0;
		
		for(WeiboUserInfo sg : usersInfo){
			if (sg == null) continue;
			if (sg.gender == null) {
				continue;
			}
			if(sg.gender.equals("male")||sg.gender.equals("男")){
				maleCnt += 1;
			}
		}
		
		int maleRatio = maleCnt;
		int femaleRatio = usersInfo.size() - maleRatio;
		
		JSONObject joMale = new JSONObject();
		joMale.put("name", "男");
		joMale.put("value", maleRatio);
		JSONObject joFemale = new JSONObject();
		joFemale.put("name", "女");
		joFemale.put("value", femaleRatio);
		genderDistri.add(joMale);
		genderDistri.add(joFemale);
		
		return genderDistri;
	}
	
	
	public JSONArray certifyAnalyze(){
		int type0Cnt = 0; //普通用户
		int type1Cnt = 0;  //个人认证
		int type2Cnt = 0;	//机构认证
		
		for(WeiboUserInfo sg: usersInfo){
			if(sg.approve.equals("普通用户") || sg.approve.equals("0"))
				type0Cnt += 1;
			else if(sg.approve.equals("微博达人") || sg.approve.equals("新浪个人认证") || sg.approve.equals("1"))
				type1Cnt += 1;
			else if(sg.approve.equals("新浪机构认证")|| sg.approve.equals("2"))
				type2Cnt +=1;
		}
		
		JSONObject jo1 = new JSONObject();
		jo1.put("name", "未认证用户");
		jo1.put("value", type0Cnt);
		JSONObject jo2 = new JSONObject();
		jo2.put("name", "个人认证");
		jo2.put("value", type1Cnt);
		JSONObject jo3 = new JSONObject();
		jo3.put("name", "机构认证");
		jo3.put("value", type2Cnt);
		certifyDistri.add(jo1);
		certifyDistri.add(jo2);
		certifyDistri.add(jo3);
		
		return certifyDistri;
	}
	
	public JSONArray fansDistriAnalyze(){
		
//		fansDistri = new LinkedHashMap<String, Integer>();
		//按照粉丝数量进行升序排序
		Collections.sort(usersInfo, new Comparator<WeiboUserInfo>(){
			public int compare(WeiboUserInfo w0, WeiboUserInfo w1){
				int k = 0;
				k = w0.fansCount - w1.fansCount;
				return k;
			}
		});
//		for(WeiboUserInfo user: usersInfo){
//			System.out.println(user.fansCount);
//		}
		int curCnt = 0;
		int nodeflag = 0;
		for(int i = 0; i < usersInfo.size(); ){
			WeiboUserInfo sg = usersInfo.get(i); 
			if(nodeflag == fansDistriNodes.length-1){
				curCnt += 1;
				i++;
				
			}else if(sg.fansCount <= fansDistriNodes[nodeflag+1]){
				curCnt += 1;
				i++;
				
			}else{
				String fansInteval = ""+fansDistriNodes[nodeflag]+":"+fansDistriNodes[nodeflag+1];
				JSONObject jo1 = new JSONObject();
				jo1.put("name", fansInteval);
				jo1.put("value", curCnt);
				fansDistri.add(jo1);
				curCnt = 0;
				nodeflag += 1;
			}
		}
		
		String fansInteval;
		if(nodeflag == fansDistriNodes.length-1){
			fansInteval = ""+fansDistriNodes[nodeflag]+"以上";
		}else{
			fansInteval = ""+fansDistriNodes[nodeflag]+":"+fansDistriNodes[nodeflag+1];
		}
		
		JSONObject jo2 = new JSONObject();
		jo2.put("name", fansInteval);
		jo2.put("value", curCnt);
		fansDistri.add(jo2);
//		nodeflag += 1;
		
		for(int i = nodeflag+1 ; i< fansDistriNodes.length; i++){
			if(i == fansDistriNodes.length-1){
				fansInteval = ""+fansDistriNodes[i]+"以上";
			}else{
				fansInteval = ""+fansDistriNodes[i]+":"+fansDistriNodes[i+1];
			}
			JSONObject jo = new JSONObject();
			jo.put("name", fansInteval);
			jo.put("value", 0);
			fansDistri.add(jo);
		}
		return fansDistri;
	}
	
	public JSONArray locationDistriAnalyze(){
		
//		locationDistri = new LinkedHashMap<String, Integer>();
		
		int []provinceCnt = new int[provinceArray.length];
		
		for(WeiboUserInfo sg: usersInfo){
			if(sg.location == null || sg.location == "")
				continue;
			int i = 0;
			for(i = 0; i < provinceArray.length; i++){
				if(sg.location.contains(provinceArray[i]))
					break; 
			}
			if(i != provinceArray.length){
				provinceCnt[i] +=1 ;
			}
		}
		
		for(int i = 0; i < provinceArray.length; i++){
			JSONObject jo = new JSONObject();
			jo.put("name", provinceArray[i]);
			jo.put("value", provinceCnt[i]);
			locationDistri.add(jo);
		}
		
		return locationDistri;
	}

	public JSONArray getGenderDistri() {
		return genderDistri;
	}

	public void setGenderDistri(JSONArray genderDistri) {
		this.genderDistri = genderDistri;
	}

	public JSONArray getCertifyDistri() {
		return certifyDistri;
	}

	public void setCertifyDistri(JSONArray certifyDistri) {
		this.certifyDistri = certifyDistri;
	}

	public JSONArray getLocationDistri() {
		return locationDistri;
	}

	public void setLocationDistri(JSONArray locationDistri) {
		this.locationDistri = locationDistri;
	}

	public JSONArray getFansDistri() {
		return fansDistri;
	}

	public void setFansDistri(JSONArray fansDistri) {
		this.fansDistri = fansDistri;
	}
}
