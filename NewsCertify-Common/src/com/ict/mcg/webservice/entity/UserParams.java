package com.ict.mcg.webservice.entity;

/**
 * 已弃用 2017.10.10
 * 原用于用户账户采集服务的参数传递
 */
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="userParam")
@XmlAccessorType(XmlAccessType.FIELD)
public class UserParams implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9110915836288991685L;
	@XmlElement(name="userIds")
	private ArrayList<String> userIdList;
	@XmlElement(name="userNames")
	private ArrayList<String> userNameList;
	@XmlElement(name="userPartys")
	private ArrayList<String> partyNameList;
	
	
	public UserParams() {
		super();
	}
	public UserParams(ArrayList<String> userIdList, ArrayList<String> userNameList, ArrayList<String> partyNameList) {
		super();
		this.userIdList = userIdList;
		this.userNameList = userNameList;
		this.partyNameList = partyNameList;
	}
	public ArrayList<String> getUserIdList() {
		return userIdList;
	}
	public ArrayList<String> getUserNameList() {
		return userNameList;
	}
	public ArrayList<String> getPartyNameList() {
		return partyNameList;
	}
}
