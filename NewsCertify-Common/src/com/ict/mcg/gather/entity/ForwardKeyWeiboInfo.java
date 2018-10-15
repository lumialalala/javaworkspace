package com.ict.mcg.gather.entity;

import java.io.Serializable;

public class ForwardKeyWeiboInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7862469967488271682L;
	//weibo info
	private String wId;
	private String wForwardCount;
	private String wCommentCount;
	private String wTime;
	private String wFrom;
	private String wContent;
	
	//author info
	private String aId;
	private String aName;
	private String aAprove;
	private String aGender;
	private String aFollowCount;
	private String aFanCount;
	private String aWeiboCount;
	private String aLocation;
	private String aDescription;
	private String aLogoUrl;
	
	private String inputWid;
	private String inputAname;
	
	private boolean isRepostPublic = true;
	
	public boolean isRepostPublic() {
		return isRepostPublic;
	}

	public void setRepostPublic(boolean isRepostPublic) {
		this.isRepostPublic = isRepostPublic;
	}

	public ForwardKeyWeiboInfo() {
		super();
	}
	
	public ForwardKeyWeiboInfo(String wId, String wForwardCount,
			String wCommentCount, String wTime, String wFrom, String wContent,
			String aId, String aName, String aAprove, String aGender,
			String aFollowCount, String aFanCount, String aWeiboCount, String aLocation,
			String aDescription, String aLogoUrl) {
		super();
		this.wId = wId;
		this.wForwardCount = wForwardCount;
		this.wCommentCount = wCommentCount;
		this.wTime = wTime;
		this.wFrom = wFrom;
		this.wContent = wContent;
		this.aId = aId;
		this.aName = aName;
		this.aAprove = aAprove;
		this.aGender = aGender;
		this.aFollowCount = aFollowCount;
		this.aFanCount = aFanCount;
		this.aWeiboCount = aWeiboCount;
		this.aLocation = aLocation;
		this.aDescription = aDescription;
		this.aLogoUrl = aLogoUrl;
	}
	public String getwId() {
		return wId;
	}
	public void setwId(String wId) {
		this.wId = wId;
	}
	public String getwForwardCount() {
		return wForwardCount;
	}
	public void setwForwardCount(String wForwardCount) {
		this.wForwardCount = wForwardCount;
	}
	public String getwCommentCount() {
		return wCommentCount;
	}
	public void setwCommentCount(String wCommentCount) {
		this.wCommentCount = wCommentCount;
	}
	public String getwTime() {
		return wTime;
	}
	public void setwTime(String wTime) {
		this.wTime = wTime;
	}
	public String getwFrom() {
		return wFrom;
	}
	public void setwFrom(String wFrom) {
		this.wFrom = wFrom;
	}
	public String getwContent() {
		return wContent;
	}
	public void setwContent(String wContent) {
		this.wContent = wContent;
	}
	public String getaId() {
		return aId;
	}
	public void setaId(String aId) {
		this.aId = aId;
	}
	public String getaName() {
		return aName;
	}
	public void setaName(String aName) {
		this.aName = aName;
	}
	public String getaAprove() {
		return aAprove;
	}
	public void setaAprove(String aAprove) {
		this.aAprove = aAprove;
	}
	public String getaFollowCount() {
		return aFollowCount;
	}
	public void setaFollowCount(String aFollowCount) {
		this.aFollowCount = aFollowCount;
	}
	public String getaFanCount() {
		return aFanCount;
	}
	public void setaFanCount(String aFanCount) {
		this.aFanCount = aFanCount;
	}
	public String getaWeiboCount() {
		return aWeiboCount;
	}
	public void setaWeiboCount(String aWeiboCount) {
		this.aWeiboCount = aWeiboCount;
	}
	public String getaLocation() {
		return aLocation;
	}
	public void setaLocation(String aLocation) {
		this.aLocation = aLocation;
	}
	public String getaDescription() {
		return aDescription;
	}
	public void setaDescription(String aDescription) {
		this.aDescription = aDescription;
	}
	public String getaLogoUrl() {
		return aLogoUrl;
	}
	public void setaLogoUrl(String aLogoUrl) {
		this.aLogoUrl = aLogoUrl;
	}
	public String getInputWid() {
		return inputWid;
	}
	public void setInputWid(String inputWid) {
		this.inputWid = inputWid;
	}
	public String getInputAname() {
		return inputAname;
	}
	public void setInputAname(String inputAname) {
		this.inputAname = inputAname;
	}
	public String getaGender() {
		return aGender;
	}
	public void setaGender(String aGender) {
		this.aGender = aGender;
	}
}
