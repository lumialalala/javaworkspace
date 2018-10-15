package com.ict.mcg.people;

import java.util.ArrayList;
import java.util.List;


public class NoneWeiboUser {
	public String name;
	double emotionValue = 0.0;
	private List<String> posWordcloud = new ArrayList<String>();
	private List<String> negWordcloud = new ArrayList<String>();
	
	public NoneWeiboUser() {
		// TODO Auto-generated constructor stub
		this.name = "";
	}
	
	public void setEmotionValue(double val){
		if (val>=1) {
			val = 1;
		}else if (val<=-1) {
			val = -1;
		}
		val = val*100;
		this.emotionValue = val;
	}
	public double getEmotionValue(){
		return this.emotionValue;
	}
	public void setPosWordcloud(List<String> posWords){
		this.posWordcloud = posWords;
	}
	public void setNegWordcloud(List<String> negWords){
		this.negWordcloud = negWords;
	}
	public List<String> getPosWordcloud(){
		return this.posWordcloud;
	}
	public List<String> getNegWordcloud(){
		return this.negWordcloud;
	}
}
