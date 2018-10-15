package com.ict.mcg.gather.entity;

import java.io.Serializable;
import java.util.List;


public class ForwardRelationEntity implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1206937330266296524L;
	private List<String[]> forwardList;
	private List<String[]> userInfoList;
	private ForwardKeyWeiboInfo keyWeiboInfo;
	public List<String[]> getForwardList() {
		return forwardList;
	}
	public void setForwardList(List<String[]> forwardList) {
		this.forwardList = forwardList;
	}
	public List<String[]> getUserInfoList() {
		return userInfoList;
	}
	public void setUserInfoList(List<String[]> userInfoList) {
		this.userInfoList = userInfoList;
	}
	
	public ForwardKeyWeiboInfo getKeyWeiboInfo() {
		return keyWeiboInfo;
	}
	public void setKeyWeiboInfo(ForwardKeyWeiboInfo keyWeiboInfo) {
		this.keyWeiboInfo = keyWeiboInfo;
	}
	public ForwardRelationEntity(List<String[]> forwardList,
			List<String[]> userInfoList, ForwardKeyWeiboInfo keyWeiboInfo) {
		super();
		this.forwardList = forwardList;
		this.userInfoList = userInfoList;
		this.keyWeiboInfo = keyWeiboInfo;
	}
}
