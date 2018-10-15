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
 * 生成timeline的json格式文件
 * 
 * @author JZW
 * 
 */
public class GenerateTimelineJSON {
	private int minRate = 500;
	int min = 5, max = 50; // 事件最大、最小重要性
	private String[] iconlist1 = { "circle_blue.png", "circle_green.png",
			"circle_orange.png", "circle_purple.png", "circle_red.png",
			"circle_yellow.png" };

	/**
	 * 从一条微博构建一个事件
	 * 
	 * @param we
	 * @return
	 */
	private JSONObject geneEvent(WeiboEntity we, String icon, String id) {
		JSONObject event = new JSONObject();

		String stime = TimeConvert.getStringTime(Long.parseLong(we.getTime()));
		event.element("startdate", stime);
		event.element("enddate", "");
		event.element("date_display", "hour");

		StringBuilder sb = new StringBuilder();
		String name = we.getName();
		String content = we.getContent();
		String comment = we.getComment();
		String forword = we.getForword();
		String userurl = we.getUserurl();
		userurl = userurl.replaceAll("/////", "");
		String time = TimeConvert.getStringTime(Long.parseLong(we.getTime()));

		sb.append("@<a target='_blank' href='" + userurl + "'>" + name
				+ "</a>:");
		sb.append(content + "<br/>");
		sb.append(time);
		sb.append(" 转 发（" + forword + "）" + " 评 论（" + comment + "）<br/><br/>");
		sb.append("<iframe class='iframeStyle' frameborder='0' src='"
				+ we.getUrl() + "'>");
		event.element("title", name);
		event.element("description", sb.toString());
		event.element("icon", icon);
		int importance = (int) (we.getHotrate() * (max - min) + min);
		event.element("importance", importance);
		event.element("video", " ");
		event.element("id", id);
		//在timeline中加入“是否辟谣”信息
		event.element("isPiyao", we.isPiyao());

		return event;
	}

	/**
	 * 从一组微博构建包含所有微博信息的一个隐藏事件
	 * 
	 * @param welist
	 * @param id
	 * @return
	 */
	private JSONObject geneHiddenEvent(ArrayList<WeiboEntity> welist, String id) {
		JSONObject event = new JSONObject();

		String stime = TimeConvert.getStringTime(Long.parseLong(welist.get(0)
				.getTime()));
		event.element("startdate", stime);
		event.element("enddate", "");
		event.element("date_display", "hour");
		event.element("css_class", "hiddenevent");

		// 构建所有事件的描述
		StringBuilder sb = new StringBuilder();
		for (WeiboEntity we : welist) {
			String name = we.getName();
			String content = we.getContent();
			String comment = we.getComment();
			String forword = we.getForword();
			String userurl = we.getUserurl();
			userurl = userurl.replaceAll("/////", "");
			String time = TimeConvert.getStringTime(Long
					.parseLong(we.getTime()));

			sb.append("@<a target='_blank' href='" + userurl + "'>" + name
					+ "</a>:");
			sb.append(content + "<br/>");
			sb.append(time);
			sb.append(" 转 发（" + forword + "）" + " 评 论（" + comment
					+ "）<br/><br/>");
		}
		// 构建搜有事件图片
		String imglist = "";
		HashSet<String> sing = new HashSet<String>();
		for (WeiboEntity we : welist) {
			ArrayList<String> piclist = we.getPiclist();
			if (piclist == null || piclist.size() < 0)
				continue;
			for (String s : piclist) {
				if (sing.contains(s))
					continue;
				sing.add(s);
				imglist = imglist + "<img src='" + s + "'>";
			}
		}
		String description = sb.toString();
		if (!imglist.equals("")) {
			description = description + imglist;
		}
		event.element("title", "相关微博及图片");
		event.element("description", description);
		int importance = 10;
		event.element("importance", importance);
		event.element("video", " ");
		event.element("id", id);

		return event;
	}

	/**
	 * 从一组微博构建一组子事件（icon都相同）
	 * 
	 * @param welist
	 * @return
	 */
	private JSONArray geneEventlist(ArrayList<WeiboEntity> welist, String icon,
			String id) {
		JSONArray eventlist = new JSONArray();
		for (int i = 0; i < welist.size(); i++) {
			String eventid = id + "_" + i;
			JSONObject event = geneEvent(welist.get(i), icon, eventid);
			eventlist.add(event);
		}

		JSONObject hidden = this.geneHiddenEvent(welist, id + "_hidden");
		eventlist.add(hidden);
		return eventlist;
	}

	/**
	 * 从一组孤立微博构建子事件
	 * 
	 * @param welist
	 * @param iconlist
	 * @param id
	 * @return
	 */

	private JSONArray geneIsoEventlist(ArrayList<WeiboEntity> welist,
			String[] iconlist, String id) {
		JSONArray eventlist = new JSONArray();
		for (int i = 0; i < welist.size(); i++) {
			String eventid = id + "_" + i;
			String icon = iconlist[(i + 5) % iconlist.length];
			JSONObject event = geneEvent(welist.get(i), icon, eventid);
			eventlist.add(event);
		}
		return eventlist;
	}

	/**
	 * 从一组微博构建一条timeline
	 * 
	 * @param welist
	 * @param focus_date
	 * @param id
	 * @return
	 */
	private JSONObject geneTimeline(ArrayList<WeiboEntity> welist,
			String focus_date, String id, String bottom, String icon,
			String title) {
		JSONObject timeline = new JSONObject();
		timeline.element("focus_date", focus_date);
		timeline.element("title", title);
		timeline.element("id", id);
		timeline.element("initial_zoom", 8);
		timeline.element("bottom", bottom);
		timeline.element("inverted", "true");
		JSONArray eventlist = geneEventlist(welist, icon, id);
		timeline.element("events", eventlist);

		return timeline;
	}

	/**
	 * 从一组孤立微博构建一条timeline
	 * 
	 * @param welist
	 * @param focus_date
	 * @param id
	 * @return
	 */
	private JSONObject geneIsoTimeline(ArrayList<WeiboEntity> welist,
			String focus_date, String id, String bottom, String[] iconlist) {
		JSONObject timeline = new JSONObject();
		timeline.element("focus_date", focus_date);
		timeline.element("title", "");
		timeline.element("id", id);
		timeline.element("initial_zoom", 8);
		timeline.element("bottom", bottom);
		timeline.element("inverted", "true");
		JSONArray eventlist = geneIsoEventlist(welist, iconlist, id);
		timeline.element("events", eventlist);

		return timeline;
	}

	/**
	 * 生成完整json格式
	 * 
	 * @return
	 */
	public JSONObject geneAll(ArrayList<ArrayList<WeiboEntity>> clusters,
			String title) {
		JSONObject all = new JSONObject();

		all.element("presentation", "MCG ICT");
		all.element("title", title);
		all.element("initial_zoom", 8);
		//选取中间的微博时间做为关注时间
//		int cindex = clusters.size()/2;
//		int windex = clusters.get(cindex).size()/2;
//		String focus_date = TimeConvert.getStringTime(Long.parseLong(clusters
//				.get(cindex).get(windex).getTime()));
		
		//选取最早微博时间作为焦点时间
		String focus_date = TimeConvert.getStringTime(Long.parseLong(clusters.get(0).get(0).getTime()));
		all.element("focus_date", focus_date);

		int timelinecount = 0;
		ArrayList<WeiboEntity> isowel = new ArrayList<WeiboEntity>();
		// remove isoline for single weibo cluster
		/*for (int i = 0; i < clusters.size(); i++) {
			ArrayList<WeiboEntity> wel = clusters.get(i);
			if (wel.size() <= 1) {
				int hot = Integer.parseInt(wel.get(0).getForword())
						+ Integer.parseInt(wel.get(0).getComment());
				if (hot < minRate) {
					isowel.addAll(wel);
					clusters.remove(i);
					i--;
				}
			} else {
				timelinecount++;
			}
		}*/
		
		timelinecount = clusters.size();
		
		String idlist = "[";
		for (int i = 0; i < timelinecount; i++) {
			idlist += "'line" + i + "'";
			if (i != timelinecount - 1)
				idlist += ",";
		}
		// 加入孤立事件的时间线
		// if (isowel.size() > 0){
		if (timelinecount > 0)
			idlist += ",";
		idlist += "'isoline'";
		// }
		idlist += "]";
		all.element("initial_timelines", idlist);
		JSONArray timelinelist = new JSONArray();
		int bottom = 150;
		for (int i = 0; i < clusters.size(); i++) {
			ArrayList<WeiboEntity> wel = clusters.get(i);
			String id = "line" + i;
			String icon = this.iconlist1[i % iconlist1.length];
			String t = wel.get(0).getClasstitle();
			timelinelist.add(geneTimeline(wel, focus_date, id, "" + bottom,
					icon, t));

			bottom += 50;
			if (bottom > 250)
				bottom = 125;
		}
		String isoid = "isoline";
		timelinelist.add(geneIsoTimeline(isowel, focus_date, isoid,
				"" + bottom, iconlist1));
		// }

		all.element("timelines", timelinelist);
		return all;
	}
}
