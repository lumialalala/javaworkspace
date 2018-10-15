/**
 * 
 */
package com.ict.mcg.data;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ict.mcg.processs.NamedEntity;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.PhraseGenerator;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.util.ParamUtil;
import com.ict.mcg.util.TimeConvert;
import com.trs.cis4.tsi.client.service.HotEventInWeiboService;
import com.trs.cis4.tsi.client.service.impl.HotEventInWeiboServiceImpl;
import com.trs.dh.api.sdk.ApiSdk;

/**
 * @author JZW TRS提供的数据接口
 */
public class TRSInterface {
	// private static String keyid = "293";
	private static String keyid = null;
	private static int limit = 100;
	private static int minsize = 10;
	private static int days = 3;
	private static String rootPath = null;
	/**
	 * 获得原始数据
	 * 
	 * @param time
	 *            时间
	 * @param limit
	 *            数据量限制
	 * @param keyid
	 *            keyid
	 * @return
	 */
	public static String getInput(String time, int limit, String keyid) {
		HotEventInWeiboService hotEventInWeiboService = new HotEventInWeiboServiceImpl();
		String json = null;
		try{
			json = hotEventInWeiboService.listHotEventInWeiboBeanInJSON(time, limit, keyid);
		}
		catch(NullPointerException ex){
			return null;
		}
		return json;
	}

	/**
	 * 使用默认配置获取原始数据
	 * 
	 * @param time
	 *            时间
	 * @return
	 */
	public static String getInput(String time) {
		return getInput(time, limit, keyid);
	}

	public static String fromObjToParam(JSONObject obj) {
		int MAX_KEYWORDS = 4;
		JSONObject news = new JSONObject();
		// 1.生成标题
		String title = "";
		PhraseGenerator pg = new PhraseGenerator();
		ArrayList<WordNode> phrases = pg.generate(obj.getString("text"));
		for (int i = 0; i < phrases.size(); i++) {
			String p = phrases.get(i).getWord();
			if (p.length() > 4) {
				title = p;
				break;
			}
		}
		String keywords = "";
		// 选择关键词：命名实体优先
		Partition p = new Partition();
		int count = 1;

		// keywordArray可能remove()元素，需要拷贝一份
		JSONArray keywordArray = new JSONArray();
		keywordArray.addAll(obj.getJSONArray("keywords"));
		for (int i = 0; i < keywordArray.size(); i++) {
			if (count > MAX_KEYWORDS)
				break;
			String s = keywordArray.getJSONObject(i).getString("word");
			ArrayList<WordNode> al = p.participleAndMerge(s);
			if (al.size() > 0) {
				NamedEntity ne = new NamedEntity();
				ne.setProps(al);
				boolean flag = true;
				for (int j = 0; j < al.size(); j++) {
					if (al.get(j).getProps() >= 4) {
						flag = false;
						break;
					}
				}
				if (flag) {
					keywords = keywords + " " + s;
					keywordArray.remove(i);
					i--;
					count++;
				}
			}
		}
		for (int i = 0; i < keywordArray.size(); i++) {
			if (count > MAX_KEYWORDS)
				break;
			keywords = keywords + " " + keywordArray.getJSONObject(i).getString("word");
			count++;
		}

		if (title.length() < 1)
			title = keywords;
		news.element("title", title);
		String time = obj.getString("time");
		long now = TimeConvert.getNow();
		long t = TimeConvert.convertString(time);
		String start = TimeConvert.getStringDate(t - 24 * 3600 * 1000);// 提前一天搜索
		String end = start;
		long m = now - t;
		if (m > days * 24 * 3600 * 1000) {
			// 超过4天
			end = TimeConvert.getStringDate(t + (days - 1) * 24 * 3600 * 1000); // 最多days天
		} else {
			end = TimeConvert.getStringDate(now);
		}
		
		keywords = keywords.trim().replace(" ", "%20");
		String param = "k=" + keywords + "&s=" + start + "&e=" + end;

		return param;
	}
	/**
	 * 从原始json数据中解析出信息
	 * 
	 * @param json
	 * @return
	 */
	public static ArrayList<SourceEntity> getInputSource(String json) {
		if(json == null)
			return null;
		ArrayList<SourceEntity> result = new ArrayList<SourceEntity>();
		JSONArray newslist = JSONArray.fromObject(json);

		for (int i = 0; i < newslist.size(); i++) {
			JSONObject news = newslist.getJSONObject(i);
			JSONArray kw = news.getJSONArray("keywords");
			ArrayList<String> keywords = new ArrayList<String>();
			for (int j = 0; j < kw.size(); j++)
				keywords.add(kw.getJSONObject(j).getString("word"));
			String mid = news.getString("mid");
			String url = news.getString("url");
			int reposts_count = news.getInt("reposts_count");
			int comments_count = news.getInt("comments_count");
			String text = news.getString("text");
			String time = news.getString("time");

			SourceEntity se = new SourceEntity(keywords, mid, url, reposts_count, comments_count, text, time);
			result.add(se);
		}
		return result;
	}

	/**
	 * 从一条信息源获取请求属性：关键词、时间范围、标题
	 * 
	 * @param se
	 * @return
	 */
	public static JSONObject getAttr(SourceEntity se) {
		int MAX_KEYWORDS = 3;
		JSONObject news = new JSONObject();
		// 1.生成标题
		String title = "";
		PhraseGenerator pg = new PhraseGenerator();
		ArrayList<WordNode> phrases = pg.generate(se.getText());
		for (int i = 0; i < phrases.size(); i++) {
			String p = phrases.get(i).getWord();
			if (p.length() > 4) {
				title = p;
				break;
			}
		}
		String keywords = "";
		// 选择关键词：命名实体优先
		Partition p = new Partition();
		int count = 1;

		for (int i = 0; i < se.getKeywords().size(); i++) {
			if (count > MAX_KEYWORDS)
				break;
			String s = se.getKeywords().get(i);
			ArrayList<WordNode> al = p.participleAndMerge(s);
			if (al.size() > 0) {
				NamedEntity ne = new NamedEntity();
				ne.setProps(al);
				boolean flag = true;
				for (int j = 0; j < al.size(); j++) {
					if (al.get(j).getProps() >= 4) {
						flag = false;
						break;
					}
				}
				if (flag) {
					keywords = keywords + " " + s;
					se.getKeywords().remove(i);
					i--;
					count++;
				}
			}
		}
		for (int i = 0; i < se.getKeywords().size(); i++) {
			if (count > MAX_KEYWORDS)
				break;
			keywords = keywords + " " + se.getKeywords().get(i);
			count++;
		}

		if (title.length() < 1)
			title = keywords;
		news.element("title", title);
		String time = se.getTime();
		long now = TimeConvert.getNow();
		long t = TimeConvert.convertString(time);
		String start = TimeConvert.getStringDate(t - 24 * 3600 * 1000);// 提前一天搜索
		String end = start;
		long m = now - t;
		if (m > days * 24 * 3600 * 1000) {
			// 超过4天
			end = TimeConvert.getStringDate(t + (days - 1) * 24 * 3600 * 1000); // 最多days天
		} else {
			end = TimeConvert.getStringDate(now);
		}
		keywords = keywords.trim().replace(" ", "%20");
		String url = "k=" + keywords + "&s=" + start + "&e=" + end;
		news.element("key", keywords);
		news.element("begin", start);
		news.element("end", end);
		news.element("url", url);
		return news;
	}

	/**
	 * 从一条信息源获取请求信息需要的参数
	 * 
	 * @param se
	 * @return
	 */
	public static String[] getUrl(SourceEntity se) {
		String keywords = "";
		// 选择关键词：命名实体优先
		Partition p = new Partition();
		int count = 0;

		for (int i = 0; i < se.getKeywords().size(); i++) {
			if (count > 1)
				break;
			String s = se.getKeywords().get(i);
			ArrayList<WordNode> al = p.participleAndMerge(s);
			if (al.size() > 0) {
				NamedEntity ne = new NamedEntity();
				ne.setProps(al);
				boolean flag = true;
				for (int j = 0; j < al.size(); j++) {
					if (al.get(j).getProps() >= 4) {
						flag = false;
						break;
					}
				}
				if (flag) {
					keywords = keywords + " " + s;
					se.getKeywords().remove(i);
					i--;
					count++;
				}
			}
		}
		for (int i = 0; i < se.getKeywords().size(); i++) {
			if (count > 1)
				break;
			keywords = keywords + " " + se.getKeywords().get(i);
			count++;
		}

		// 计算时间范围
		String time = se.getTime();
		long now = TimeConvert.getNow();
		long t = TimeConvert.convertString(time);
		String start = TimeConvert.getStringDate(t - 24 * 3600 * 1000);// 提前一天搜索
		String end = start;
		long m = now - t;
		if (m > 5 * 24 * 3600 * 1000) {
			// 超过5天
			end = TimeConvert.getStringDate(t + 5 * 24 * 3600 * 1000); // 最多5天
		} else {
			end = TimeConvert.getStringDate(now);
		}

		String[] url = new String[3];
		url[0] = keywords;
		url[1] = start;
		url[2] = end;
		return url;
	}

	/**
	 * 将新闻线索写入文件
	 * 
	 * @param newslist
	 * @param filePath
	 */
	public static void writeJson(JSONArray newslist, String filePath) {
		
		File outfile = new File(filePath);
		// 如果文件已存在则先读入原文件
		JSONArray old = new JSONArray();
		if (outfile.exists()) {
			try {
				HashSet<String> urls = new HashSet<String>();
				for (int i = 0; i < newslist.size(); i++) {
					String url = newslist.getJSONObject(i).getString("url");
					urls.add(url);

				}

				BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(outfile), "utf-8"));
				String content = "";
				String s = "";
				while ((s = reader.readLine()) != null) {
					content += s;
				}
				reader.close();
				if (content.trim().startsWith("[")) {

					old = JSONArray.fromObject(content);
					for (int i = 0; i < old.size(); i++) {
						JSONObject jo = old.getJSONObject(i);
						if (urls.contains(jo.getString("url"))) {
							old.remove(i);
							i--;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		JSONArray all = new JSONArray();
		all.addAll(newslist);
		all.addAll(old);

		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "utf-8"));
			writer.write(all.toString() + "\n");
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/*public static void getNews(String time, int num) {
		ArrayList<SourceEntity> sel = getInputSource(getInput(time));
		JSONArray newslist = new JSONArray();
		for (int i = 0; i < sel.size(); i++) {
			if (i >= num)
				break;
			newslist.add(getAttr(sel.get(i)));
		}
		writeJson(newslist, "newslist_test100.json");
	}*/
	
	/*public static JSONArray getNews(String time, int num, String fileName) {
		ArrayList<SourceEntity> sel = getInputSource(getInput(time, num, keyid));
		if (sel == null) {
			return null;
		}
		JSONArray newslist = new JSONArray();
		for (int i = 0; i < sel.size(); i++) {
			if (i >= num)
				break;
			newslist.add(getAttr(sel.get(i)));
		}
		if (fileName != null && fileName.length() > 0) {
			writeJson(newslist, fileName);
		}
		return newslist;
	}*/
	
	

	/**
	 * 请求url
	 * 
	 * @param urlStr
	 */
	public static void requestUrl(String urlStr) {
		HttpURLConnection url_con = null;
		try {
			String prefix = "http://localhost:8080";
			URL url = new URL(prefix + urlStr);
			url_con = (HttpURLConnection) url.openConnection();
			url_con.setRequestMethod("POST");
			url_con.setDoOutput(true);
			url_con.setConnectTimeout(60000);
			url_con.setReadTimeout(60000);
			url_con.getOutputStream().flush();
			url_con.getOutputStream().close();
			url_con.getInputStream();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (url_con != null)
				url_con.disconnect();
		}
	}


	public static int getMonthDayCount(int year, int month) {
		switch(month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:return 31;
			case 4:
			case 6:
			case 9:
			case 11:return 30;
			case 2:if (year%4==0 && (year%400==0 || year%100 != 0)){return 29;}else {return 28;}
		}
		return 0;
	}
	public static void main(String[] args) {
		String jsonData;
		String appId = "qAL3c53947Rk8nZ9", appKey = "SBxNYF7153h00X482ZC5ZGXb27";
		String host = "http://d.trs.com.cn/api";
		try {
			ApiSdk apiSdk = new ApiSdk(appId, appKey); // 创建一个ApiSdk对象
			Map map = new HashMap(); // 参数集合
			//map.put("weixinid", "rmrbwx\r\nzhanhao668\r\ncctvnewscenter"); // 实际参数
			jsonData = apiSdk.send(host + "/account/get_userinfo", map); // 接口调用并获取返回值
			System.out.println(jsonData);
		} catch (Exception e) {
			e.printStackTrace();
		}
//		String time = "2018-03-22";
//		System.out.println(TRSInterface.getInput(time));

		// String json =
		// "[{'comments_count':55863,'keywords':[{'word':'东莞扫黄'}],'mid':'3681542180326705','reposts_count':33008,'text':'华晨宇yu：我知道如何笑得像糖，出手像枪！赞(51415)|转发(33008)|收藏|评论(55863)2月24日16:45来自iPhone客户端','time':'2014-02-24 16:45','url':''}]";
		// testInput(json);
		//getNews(time, 50);

		/*for(int i = 1; i < 30; i++){
			time = "2013-8-" + i;
			getNews(time, 100);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("~~~" + time);
		}
		//getNews(time, 100);
		System.out.println();*/
//		String time = "";
//		for(int year = 2014; year >= 2012; --year) {
//			for (int i = 1; i <= 12; ++i) {
//				int count = getMonthDayCount(year, i);
//				for (int j = 1; j <= count; ++j) {
//					time = year+"-"+i+"-"+j;
//					try {
//						getNews(time, 50, "newslist_"+year+"-"+i+"_"+j+".json");
//					} catch (Exception e) {
//						e.printStackTrace();
//						continue;
//					}
//				}
//			}
//		}
	}
}
