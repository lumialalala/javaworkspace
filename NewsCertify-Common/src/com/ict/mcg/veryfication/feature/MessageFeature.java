/**
 * 
 */
package com.ict.mcg.veryfication.feature;
import java.util.ArrayList;
import java.util.List;

/**
 * @author JZW 微博message特征实体类
 */
public class MessageFeature {
	private int forword;
	private int comment;
	private int praise;
	private int origin;
	private ArrayList<String> urllist;
	private ArrayList<String> atlist;
	private ArrayList<String> hashtaglist;
	private ArrayList<ArrayList<String>> nameentitylist;
	private int qcount; // question mark count;
	private int ecount; // exclaim mark count
	private int[] wcount; // word count, charactor count 2
	private int[] pecount; // three personality count 3
	private int[] emocount; // emotional count 3 pos, neg, score

	private int[] imgfeature; // image:hasimg, multiimg, countimg
	private int platformType; // 0 no info, 1 mobile, 2 webpage
	private List<String> imgList;
	
	public MessageFeature(int forword, int comment,int praise, ArrayList<String> urllist,
			ArrayList<String> atlist, ArrayList<String> hashtaglist,
			ArrayList<ArrayList<String>> nameentitylist, int qcount,
			int ecount, int[] wcount, int[] pecount, int[] emocount, int[] imgfeature, int platformType, int origin) {
		super();
		this.forword = forword;
		this.comment = comment;
		this.urllist = urllist;
		this.atlist = atlist;
		this.hashtaglist = hashtaglist;
		this.nameentitylist = nameentitylist;
		this.qcount = qcount;
		this.ecount = ecount;
		this.wcount = wcount;
		this.pecount = pecount;
		this.emocount = emocount;
		this.imgfeature = imgfeature;
		this.platformType = platformType;
		this.praise = praise;
		this.origin = origin;
	}
	
	public MessageFeature(int forword, int comment,int praise, ArrayList<String> urllist,
			ArrayList<String> atlist, ArrayList<String> hashtaglist,
			ArrayList<ArrayList<String>> nameentitylist, int qcount,
			int ecount, int[] wcount, int[] pecount, int[] emocount, int[] imgfeature, int platformType, int origin, 
			List<String> imgList) {
		super();
		this.forword = forword;
		this.comment = comment;
		this.urllist = urllist;
		this.atlist = atlist;
		this.hashtaglist = hashtaglist;
		this.nameentitylist = nameentitylist;
		this.qcount = qcount;
		this.ecount = ecount;
		this.wcount = wcount;
		this.pecount = pecount;
		this.emocount = emocount;
		this.imgfeature = imgfeature;
		this.platformType = platformType;
		this.praise = praise;
		this.origin = origin;
		this.imgList = imgList;
	}
	
	public List<String> getImgList() {
		return imgList;
	}

	public void setImgList(List<String> imgList) {
		this.imgList = imgList;
	}

	public int getPraise() {
		return praise;
	}

	public void setPraise(int praise) {
		this.praise = praise;
	}
	
	public int getOrigin() {
		return origin;
	}

	public void setOrigin(int origin) {
		this.origin = origin;
	}

	public int getForword() {
		return forword;
	}

	public void setForword(int forword) {
		this.forword = forword;
	}

	public int getComment() {
		return comment;
	}

	public void setComment(int comment) {
		this.comment = comment;
	}

	public ArrayList<String> getUrllist() {
		return urllist;
	}

	public void setUrllist(ArrayList<String> urllist) {
		this.urllist = urllist;
	}

	public ArrayList<String> getAtlist() {
		return atlist;
	}

	public void setAtlist(ArrayList<String> atlist) {
		this.atlist = atlist;
	}

	public ArrayList<String> getHashtaglist() {
		return hashtaglist;
	}

	public void setHashtaglist(ArrayList<String> hashtaglist) {
		this.hashtaglist = hashtaglist;
	}

	public ArrayList<ArrayList<String>> getNameentitylist() {
		return nameentitylist;
	}

	public void setNameentitylist(ArrayList<ArrayList<String>> nameentitylist) {
		this.nameentitylist = nameentitylist;
	}

	public int getQcount() {
		return qcount;
	}

	public void setQcount(int qcount) {
		this.qcount = qcount;
	}

	public int getEcount() {
		return ecount;
	}

	public void setEcount(int ecount) {
		this.ecount = ecount;
	}

	public int[] getWcount() {
		return wcount;
	}

	public void setWcount(int[] wcount) {
		this.wcount = wcount;
	}

	public int[] getPecount() {
		return pecount;
	}

	public void setPecount(int[] pecount) {
		this.pecount = pecount;
	}

	public int[] getEmocount() {
		return emocount;
	}

	public void setEmocount(int[] emocount) {
		this.emocount = emocount;
	}

	public int[] getImgFeature() {
		return imgfeature;
	}

	public void setImgFeature(int[] imgfeature) {
		this.imgfeature = imgfeature;
	}

	public int getPlatformType() {
		return platformType;
	}

	public void setPlatformType(int platformType) {
		this.platformType = platformType;
	}
}
