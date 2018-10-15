package com.ict.mcg.gather.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.WordNode;
import com.mongodb.BasicDBObject;

public class WeiboEntityV2 implements Serializable {

	/**
	 * @author GC 存放单微博评论与转发的实体
	 */
	private static final long serialVersionUID = 3164936142246549477L;
	private String Mid;
	private String url; // 微博url
	private String userurl;// 发布者url
	private String username;//发布者名称
	private String content;//发布内容
	private String time; // 发布时间
	private String forword; // 转发量
	private boolean isComment;//是否评论
	private boolean isForward;//是否转发
	private String originalWid;//原始微博id
	private Map<String, Double> faces; //微博表情,以及其对应的位置 by gc
	private int userCertify;
	
	private ArrayList<WordNode> segs; //分词结果
	private double hotrate; //一条微博在一组微博中的热度
	
	private String praise;//点赞数
	
	private String userId;
	private String userGender;
	private String userFollowCount;
	private String userFanCount;
	private String userWeiboCount;
	private String userLocation;
	private String userDescription;
	
	public WeiboEntityV2(){
		this.isComment = false;
		this.isForward = false;
		this.forword = "0";
		this.praise = "0";
//		this.userDescription = "";
//		this.userLocation = "";
//		this.faces = new HashMap<String,Double>();
		
	}
	
	public void setSegs(){
		Partition p = new Partition();
		ArrayList<WordNode> s = p.participleWithFilter(content, 1);
		this.segs = s;
	}
	
	public void setSegs(ArrayList<WordNode> segs){
		this.segs = segs;
	}
	
	public ArrayList<WordNode> getSegs(){
		return segs;
	}
	
	public String getMid() {
		return Mid;
	}
	public void setMid(String mid) {
		Mid = mid;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getUserurl() {
		return userurl;
	}
	public void setUserurl(String userurl) {
		this.userurl = userurl;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getForword() {
		return forword;
	}
	public void setForword(String forword) {
		this.forword = forword;
	}
	public boolean isComment() {
		return isComment;
	}
	public void setComment(boolean isComment) {
		this.isComment = isComment;
	}
	public boolean isForward() {
		return isForward;
	}
	public void setForward(boolean isForward) {
		this.isForward = isForward;
	}
	public String getOriginalWid() {
		return originalWid;
	}
	public void setOriginalWid(String originalWid) {
		this.originalWid = originalWid;
	}
	public Map<String, Double> getFaces() {
		return faces;
	}
	public void setFaces(Map<String, Double> faces) {
		this.faces = faces;
	}
	public int getUserCertify() {
		return userCertify;
	}
	public void setUserCertify(int userCertify) {
		this.userCertify = userCertify;
	}
	public String getPraise() {
		return praise;
	}
	public void setPraise(String praise) {
		this.praise = praise;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getUserGender() {
		return userGender;
	}
	public void setUserGender(String userGender) {
		this.userGender = userGender;
	}
	public String getUserFollowCount() {
		return userFollowCount;
	}
	public void setUserFollowCount(String userFollowCount) {
		this.userFollowCount = userFollowCount;
	}
	public String getUserFanCount() {
		return userFanCount;
	}
	public void setUserFanCount(String userFanCount) {
		this.userFanCount = userFanCount;
	}
	public String getUserWeiboCount() {
		return userWeiboCount;
	}
	public void setUserWeiboCount(String userWeiboCount) {
		this.userWeiboCount = userWeiboCount;
	}
	public String getUserLocation() {
		return userLocation;
	}
	public void setUserLocation(String userLocation) {
		this.userLocation = userLocation;
	}
	public String getUserDescription() {
		return userDescription;
	}
	public void setUserDescription(String userDescription) {
		this.userDescription = userDescription;
	}
	public double getHotrate() {
		return hotrate;
	}

	public void setHotrate(double hotrate) {
		this.hotrate = hotrate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	
	public BasicDBObject dbObject(){
		BasicDBObject props = new BasicDBObject("mid",Mid).append("url", url)
				.append("userurl", userurl).append("username", username).append("content", content)
				.append("time", time).append("forward", forword).append("isComment",isComment)
				.append("isForward", isForward).append("originalWid", originalWid).append("faces", faces)
				.append("userCertify", userCertify).append("praise", praise).append("userId", userId)
				.append("userGender", userGender).append("userFollowCount", userFollowCount).append("userLocation", userLocation)
				.append("userFanCount", userFanCount).append("userWeiboCount", userWeiboCount).append("userDescription", userDescription);
		return props;
	}
}
