/**
 * 
 */
package com.ict.mcg.util;

import java.util.ArrayList;
import java.util.HashSet;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ict.mcg.gather.entity.WeiboEntity;

/**
 * 生成timeline的json格式文件,用户单独的展示页面
 * 
 * @author JZW
 * 
 */
public class GenerateEventJSON {

	/**
	 * 从一条微博构建一个对象
	 * 
	 * @param we
	 * @return
	 */
	private JSONObject geneWeibo(WeiboEntity we) {
		JSONObject weibo = new JSONObject();

		weibo.element("name", we.getName());
		String content = we.getContent();
		String time = TimeConvert.getStringTime(Long.parseLong(we.getTime()));
		String forward = we.getForword();
		String comment = we.getComment();
		String text = content + "</br>" + time + " 转发（" + forward + "）  评论（"
				+ comment + "）";
		weibo.element("text", text);
		
		weibo.element("url", we.getUrl());
		
		weibo.element("isPiyao", we.isPiyao());

		return weibo;
	}

	/**
	 * 从一组微博构建包含所有微博信息的一个事件
	 * 
	 * @param welist
	 * @param id
	 * @return
	 */
	private JSONObject geneEvent(ArrayList<WeiboEntity> welist, String title) {
		JSONObject event = new JSONObject();
		String stime = TimeConvert.getStringTime(Long.parseLong(welist.get(0)
				.getTime()));

		event.element("title", title);
		event.element("time", stime);
		JSONArray weibolist = new JSONArray();
		JSONArray imglist = new JSONArray();
		HashSet<String> sing = new HashSet<String>();
		for (int i = 0; i < welist.size(); i++) {
			JSONObject weibo = geneWeibo(welist.get(i));
			weibolist.add(weibo);
			ArrayList<String> piclist = welist.get(i).getPiclist();
			if (piclist == null || piclist.size() < 0)
				continue;
			for (String s : piclist) {
				if (sing.contains(s))
					continue;
				sing.add(s);
				JSONObject img = new JSONObject();
				img.element("img", s);
				String t = TimeConvert.getStringTime(Long.parseLong(welist.get(i)
						.getTime()));
				img.element("time", t);
				imglist.add(img);
			}
		}
		event.element("weibos", weibolist);
		event.element("imglist", imglist);

		return event;
	}

	/**
	 * 生成完整json格式
	 * 
	 * @return
	 */
	public JSONObject geneAll(ArrayList<ArrayList<WeiboEntity>> clusters,
			String title) {

		JSONObject all = new JSONObject();

		all.element("title", title);
		JSONArray eventlist = new JSONArray();

		for (int i = 0; i < clusters.size(); i++) {
			ArrayList<WeiboEntity> wel = clusters.get(i);
			String t = wel.get(0).getClasstitle();
			eventlist.add(geneEvent(wel, t));
		}

		all.element("eventlist", eventlist);
		return all;
	}
}
