/**
 * 
 */
package com.ict.mcg.data;

import java.util.ArrayList;

/**
 * @author JZW
 *新闻线索实体类
 */
public class SourceEntity {
	private ArrayList<String> keywords;
	private String mid;
	private String url;
	private int reposts_count;
	private int comments_count;
	private String text;
	private String time;
	
	public SourceEntity(ArrayList<String> keywords, String mid, String url,
			int repostsCount, int commentsCount, String text, String time) {
		super();
		this.keywords = keywords;
		this.mid = mid;
		this.url = url;
		reposts_count = repostsCount;
		comments_count = commentsCount;
		this.text = text;
		this.time = time;
	}

	public ArrayList<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(ArrayList<String> keywords) {
		this.keywords = keywords;
	}

	public String getMid() {
		return mid;
	}

	public void setMid(String mid) {
		this.mid = mid;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getReposts_count() {
		return reposts_count;
	}

	public void setReposts_count(int repostsCount) {
		reposts_count = repostsCount;
	}

	public int getComments_count() {
		return comments_count;
	}

	public void setComments_count(int commentsCount) {
		comments_count = commentsCount;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

}
