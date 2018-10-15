package com.ict.mcg.gather.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ict.mcg.processs.WordNode;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

/**
 * @author JZW 表示一条微博相关信息的实体类
 */
public class WeiboEntity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7359235840510638097L;
	private String weiboId;
	private String url; // 微博url
	private String userId;// 发布者id
	private String content;
	private String time; // 发布时间
	private String forword; // 转发量
	private String comment; // 评论量
	private String praise;
	private boolean isOrigin;//是否原创
	private ArrayList<WordNode>  segs; //微博分词结果
	private ArrayList<String> piclist; //微博配图列表
	private Map<String,Double> faces; //微博表情
	
	private String sourcePlatform;
	private boolean hasVideo;
	

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}


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

	public Map<String, Double> getFaces() {
		return faces;
	}

	public void setFaces(Map<String, Double> faces) {
		this.faces = faces;
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

	public WeiboEntity(String mid, String url, String praise,
			String time, String forword, String comment, String content) {
		this.weiboId = mid;
		this.url = url;
		this.time = time;
		this.forword = forword;
		this.comment = comment;
		this.praise = praise;
		this.content = content;
	}



	public WeiboEntity() {
	}

	public String getMid() {
		return weiboId;
	}

	public void setMid(String mid) {
		weiboId = mid;
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
		if(forword  == null || forword.equals(""))
			return "0";
		return forword;
	}
	
	public String getOriginForword(){
		return forword;
	}
	
	public void setForword(String forword) {
		this.forword = forword;
	}

	public String getComment() {
		if(comment == null || comment.equals(""))
			return "0";
		return comment;
	}
	
	public String getOriginComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public ArrayList<String> getPiclist() {
		return piclist;
	}

	public void setPiclist(ArrayList<String> piclist) {
		this.piclist = piclist;
	}

	public boolean isContainsVideo() {
		return hasVideo;
	}

	public void setContainsVideo(boolean containsVideo) {
		this.hasVideo = containsVideo;
	}

	@Override
	public String toString() {
		String result = weiboId + "|"  + url + "|"  
				+ time + "|" + forword + "|" + comment + "|" + hasVideo +"|" + "\r\n" + content
				+ "\r\n";
		if(faces!=null&&this.faces.size()>0){
			for(Entry<String,Double> s:faces.entrySet())
				System.out.println(s.getKey());
		}
		if(piclist!=null&&this.piclist.size()>0){
			for(String s:piclist)
				System.out.println(s);
		}
		return result;
	}
	
	public BasicDBObject dbOjbect() {
		BasicDBObject weibo = new BasicDBObject("mid", weiboId).append("url", url)
				.append("content", content).append("time", time)
				.append("forward", forword).append("comment", comment)
				.append("sourcePlatform", sourcePlatform).append("praise", praise);
		
		return weibo;
	}
	
	public BasicDBObject dbOjbect4postRecord() {
		long now = System.currentTimeMillis();
		
		BasicDBObject forwardObj = new BasicDBObject().append("forward", forword).append("time", now);
		BasicDBList forwardList = new BasicDBList();
		forwardList.add(forwardObj);
		
		BasicDBObject commentObj = new BasicDBObject().append("comment", comment).append("time", now);
		BasicDBList commentList = new BasicDBList();
		commentList.add(commentObj);
		
		BasicDBObject praiseObj = new BasicDBObject().append("praise", praise).append("time", now);
		BasicDBList praiseList = new BasicDBList();
		praiseList.add(praiseObj);
		
		BasicDBObject weibo = new BasicDBObject("mid", weiboId).append("url", url)
				.append("content", content).append("time", time)
				.append("forwards", forwardList).append("comments", commentList)
				.append("isOrigin", isOrigin)
				.append("piclist", piclist)
				.append("sourcePlatform", sourcePlatform).append("praises", praiseList)
				.append("userId", userId).append("faces", faces);
		return weibo;
	}
	
	/**
	 * 从List<WeiboEntity>转换为BasicDBList
	 * @param weList
	 * @return 
	 */
	public static BasicDBList toDBList(List<WeiboEntity> weList) {
		BasicDBList weiboList = new BasicDBList();
		for (WeiboEntity we : weList) {
			weiboList.add(we.dbOjbect());
		}
		return weiboList;
	}
	
	// From @com.ict.mcg.service.EventService 20171213
	public static WeiboEntity convert(BasicDBObject obj) {
		WeiboEntity we = new WeiboEntity();
		we.setMid(obj.getString("mid"));
		we.setUrl(obj.getString("url"));
		
		we.setContent(obj.getString("content"));
		we.setTime(obj.getString("time"));
		we.setForword(obj.getString("forward"));
		if (we.getOriginForword() == null||we.getOriginForword().equals("")) {
			we.setForword(obj.getString("forword")); // 谁拼错，谁知道
		}
		we.setComment(obj.getString("comment"));
		if (we.getOriginComment() == null||we.getOriginComment().equals("")) {
			we.setComment(obj.getString("commment")); // 谁拼错，谁知道
		}

		we.setOrigin(obj.getBoolean("isOrigin"));
		we.setPiclist((ArrayList<String>)obj.get("piclist"));
		
		we.setSourcePlatform(obj.getString("sourcePlatform"));
		we.setPraise(obj.getString("praise"));
		we.setUserId(obj.getString("userId"));
		
		we.setFaces((Map<String,Double>)obj.get("faces")); // by gc
		
		we.setContainsVideo(obj.getBoolean("containsVideo"));
		we.setContainsVideo(obj.getBoolean("containsUrl"));
		
		return we;
	}
	
	public static ArrayList<WeiboEntity> convert(BasicDBList objList) {
		ArrayList<WeiboEntity> weList = new ArrayList<WeiboEntity>();
		for (Object o:objList) { 
			WeiboEntity we = convert((BasicDBObject)o);
			weList.add(we);
		}
		return weList;
	}
}
