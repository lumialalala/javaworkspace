package com.ict.mcg.gather.entity;

import java.io.Serializable;

import com.ict.mcg.util.DataControl;

/**
 * weibo user info structure
 * @author senochow
 *
 */
public class WeiboUserInfo extends AbstractUserInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1604670480108995942L;
	public int followCount;
	public int fansCount;
	public int weiboCnt;
	public String approveInfo;
	public String tag;
	public String edu;
	public String career;	
	public String topLocation;
	public String friendInterestInfo;
	public String contactInfo;
	public double approveRatio;
	private String emtionIndexStr;
	public double anomalyValue;
	public double influenceValue;
	public String registerDate;
	public int trustInfo;
	public String avatarUrl; //用户头像图片URL
	
	public WeiboUserInfo () {
		followCount = 0;
		fansCount = 0;
		weiboCnt=0;
		approveInfo = "";
		tag = "";
		edu = "";
		career = "";
		approveRatio = 0.0;
		topLocation = "";
		anomalyValue = 0.0;
		friendInterestInfo = "";
		contactInfo ="";
		influenceValue = 0.0;
		registerDate = "";
		trustInfo = 0;
		avatarUrl = "";
	}
	
	public void printInfo() {
		System.out.println(name+" "+id+" "+gender+" "+approve+location+" "+description+" "+tag+" "+" "+followCount+" "+fansCount+" "+weiboCnt+" "+edu+" "+career);
		System.out.println(topLocation+" "+approveInfo);
		System.out.println(contactInfo+getAnomalyString()+" "+getInfluenceRatio()+" "+getEmotionIndexStr());
	}
	
	public String getApproveRatioString () {
		return DataControl.getRatioStrFromVal(this.approveRatio);
	}

	public double getUserInfluence(double weiboPerDay){
		double[] wc = {4,5,3}; 
		double[] w = wc;
		
		if (weiboPerDay>20) {
			weiboPerDay = 20.0;
		}
		if (this.followCount==0||this.fansCount==0) {
			followCount = 100;
			fansCount = 10000;
			weiboCnt = 300;
		}
		double activeVal = w[0]*computeLogVal(this.followCount)+w[1]*computeLogVal(this.fansCount)+w[2]*weiboPerDay;
		activeVal = DataControl.getThreeNumberDouble(activeVal);
		if (activeVal>100) {
			activeVal = 100;
		}	
		return activeVal;
	}
	
	private double computeLogVal(int mval){
		double val = Math.log10((double)(mval+10));
		return val;
	}
	
	public double getAnomalyValue(double attendanceRate){
		double anomaly = 0.0;
		double[] wc = {0.1454,0.1395};
		double[] wa = {0.1172,0.2684};
		double[] w = wc;
		if (this.approve.equals("普通用户")) {
			w = wc;
		}else {
			w = wa;
		}
		anomaly = 1-anomalyWeight(w)*attendanceRate;
		anomaly = DataControl.getThreeNumberDouble(anomaly);
		if (anomaly<0) {
			anomaly = 0.0;
		}else if(anomaly>1){
			anomaly = 1.0;
		}
		
		return anomaly;
		
	}
	private double anomalyWeight(double[] w){
		double mweight = 0.0;
		//some case: followCnt and fansCount = 0
		if (fansCount==0||followCount==0) {
			return 1;
		}
		mweight = (w[0]*computeLogVal(fansCount))/(w[1]*computeLogVal(followCount));
		return mweight;
	}
	public String getAnomalyString(){
		String anomlyStr = "无异常";
		double val = anomalyValue;
		val = val*100;
        double val1 = DataControl.keepNDecimals(val, 3);
        int indexVal = (int)val1/10;
        if (indexVal==0) {
			return anomlyStr;
		}else {
			anomlyStr = String.valueOf(indexVal);
			return anomlyStr;
		}
	}
	/**
	 * 0~100
	 * @return
	 */
	public String getInfluenceRatio(){
		String influStr = "";
		int influIndex = (int)influenceValue/10;
		influStr = String.valueOf(influIndex);
		return influStr;
	}
	public void setEmotionIndex(String emotion) {
		this.emtionIndexStr = emotion;	
		
	}
	public String getEmotionIndexStr() {
		return emtionIndexStr;	
	}
}
