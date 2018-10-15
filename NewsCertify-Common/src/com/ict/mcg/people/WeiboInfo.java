package com.ict.mcg.people;

public class WeiboInfo {
	public String name;
	public String url; // 微博url
	public String userurl;// 发布者url
	public String time; // 发布时间
	public String forword; // 转发量
	public String comment; // 评论量
	public double hotrate;
	
	public String userFanCount;
	public double keyRatio;
	
	public WeiboInfo(){
		
	}
	/**
	 * 
	 * @return
	 */
	public double getHorate(){
		double mcomment = Double.parseDouble(this.comment);
		double mforword = Double.parseDouble(this.forword);
		hotrate = Math.pow(mforword, 1.0/3)+Math.pow(mcomment, 1.0/2);
		return hotrate;
	}
}
