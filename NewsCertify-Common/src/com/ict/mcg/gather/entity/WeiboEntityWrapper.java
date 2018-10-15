package com.ict.mcg.gather.entity;

import java.util.ArrayList;

import com.ict.mcg.processs.Node;
import com.ict.mcg.processs.WordNode;

public class WeiboEntityWrapper {
	private String Mid;
	private String url; // 微博url
	private String userurl;// 发布者url
	private String name;
	private String content;
	private String time; // 发布时间
	private String forword; // 转发量
	private String comment; // 评论量
	private double hotrate; // 一条微博在一组微博中的热度比值
	private double propagation; // 一条微博传播比例
	private boolean isOrigin;//是否原创
	private ArrayList<WordNode>  segs; //微博分词结果
	private ArrayList<String> piclist; //微博配图列表
	
	private ArrayList<Node> text; //word vector
	private double sentiment; //sentiment value;
	
	private String classtitle=""; //该条微博所在类的标题
	
	private String sourcePlatform;
	private String praise;

	public String getSourcePlatform() {
		return sourcePlatform;
	}

	public void setSourcePlatform(String sourcePlatform) {
		this.sourcePlatform = sourcePlatform;
	}

	public String getPraise() {
		return praise;
	}

	public void setPraise(String praise) {
		this.praise = praise;
	}

	public boolean isOrigin() {
		return isOrigin;
	}

	public void setOrigin(boolean isOrigin) {
		this.isOrigin = isOrigin;
	}

	public ArrayList<WordNode> getSegs() {
		return segs;
	}

	public void setSegs(ArrayList<WordNode> segs) {
		this.segs = segs;
	}

	/*public WeiboEntityWrapper(String mid, String url, String userurl, String name,
			String time, String forword, String comment, String content) {
		this.Mid = mid;
		this.userurl = userurl;
		this.name = name;
		this.url = url;
		this.time = time;
		this.forword = forword;
		this.comment = comment;
		this.content = content;
	}*/
	
	public WeiboEntityWrapper(WeiboEntity we) {
		this.Mid = we.getMid();
		//this.userurl = we.getUserurl();
		//this.name = we.getName();
		this.url = we.getUrl();
		this.time = we.getTime();
		this.forword = we.getForword();
		this.comment = we.getComment();
		this.content = we.getContent();
		//this.hotrate = we.getHotrate();
		this.isOrigin = we.isOrigin();
		this.piclist = we.getPiclist();
		this.segs = we.getSegs();
		//this.classtitle = we.getClasstitle();
		this.sourcePlatform = we.getSourcePlatform();
		this.praise = we.getPraise();
	}

	public String getUserurl() {
		return userurl;
	}

	public void setUserurl(String userurl) {
		this.userurl = userurl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public WeiboEntityWrapper() {
	}

	public String getMid() {
		return Mid;
	}

	public void setMid(String mid) {
		Mid = mid;
	}

	public String getUrl() {
		if (url.contains("?")) {
			url = url.substring(0, url.indexOf("?"));
		}
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTime() {
		if(time.equals(""))
			return "0";
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public String getForword() {
		if(forword.equals(""))
			return "0";
		return forword;
	}

	public void setForword(String forword) {
		this.forword = forword;
	}

	public String getComment() {
		if(comment.equals(""))
			return "0";
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setHotrate(double hotrate) {
		this.hotrate = hotrate;
	}

	public double getHotrate() {
		return this.hotrate;
	}

	public ArrayList<String> getPiclist() {
		return piclist;
	}

	public void setPiclist(ArrayList<String> piclist) {
		this.piclist = piclist;
	}
	
	public String getClasstitle(){
		return this.classtitle;
	}
	
	public void setClasstitle(String classtitle){
		this.classtitle = classtitle;
	}	
	
	public double getPropagation() {
		return propagation;
	}

	public void setPropagation(double propagation) {
		this.propagation = propagation;
	}

	public ArrayList<Node> getText() {
		return text;
	}

	public void setText(ArrayList<Node> text) {
		this.text = text;
	}

	public double getSentiment() {
		return sentiment;
	}

	public void setSentiment(double sentiment) {
		this.sentiment = sentiment;
	}

	
	@Override
	public String toString() {
		String result = Mid + "|" + name + "|" + url + "|" + userurl + "|"
				+ time + "|" + forword + "|" + comment + "\r\n" + content
				+ "\r\n";
		if(piclist!=null&&this.piclist.size()>0){
			for(String s:piclist)
				System.out.println(s);
		}
		return result;
	}

}
