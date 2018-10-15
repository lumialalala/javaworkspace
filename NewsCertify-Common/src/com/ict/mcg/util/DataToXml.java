package com.ict.mcg.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.FileUtils;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class DataToXml {

	public static String xmlpath;

	public static HashMap<String, File> tmpPath = new HashMap<String, File>();
	public static HashMap<String, Long> tmpTime = new HashMap<String, Long>();
	
	static {
		try {
		Properties prop = new Properties();
//		InputStream in = Object.class.getResourceAsStream("xml_conf.properties");
		InputStream in = DataToXml.class.getClassLoader().getResourceAsStream("xml_conf.properties");
//		InputStream in = new FileInputStream("xml_conf.properties");
		
			prop.load(in);
			xmlpath = prop.getProperty("xmlpath").trim();
			System.out.println("配置路径："+xmlpath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 将传播图数据文件拷贝到相应目录
	 * @param gexfPath
	 */
	public static void copyGexf(String gexfPath, String filename) {
		
		File f = new File(gexfPath);
		File cf = new File(xmlpath + filename);
		// 文件存在未过期
		if (f.exists() && (System.currentTimeMillis() - f.lastModified()) < 2*60*60*1000) {
			try {
				FileUtils.copyFile(f, cf);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (!tmpPath.containsKey(filename)) {
				tmpPath.put(filename, f);
				tmpTime.put(filename, System.currentTimeMillis());
			}
			
			TimerTask task = new TimerTask() {
		    	@Override
				public void run() {
					// retry to copy file
		    		ArrayList<String> keys = new ArrayList<String>();
		    		for (String filename : tmpPath.keySet()) {
		    			File cf = new File(xmlpath + filename);
		    			File f = tmpPath.get(filename);
		    			Long t = tmpTime.get(filename);
			    		if ((System.currentTimeMillis() - t) > 1 * 60 * 1000 && f.exists() && (System.currentTimeMillis() - f.lastModified()) < 2*60*60*1000) {
			    			try {
			    				FileUtils.copyFile(f, cf);
			    			} catch (IOException e) {
			    				e.printStackTrace();
			    			}
			    			keys.add(filename);
			    		}
		    		}
		    		for (String key : keys) {
		    			tmpPath.remove(key);
		    			tmpTime.remove(key);
		    		}
				}
			};
			// 1分钟后拷贝
			new Timer().schedule(task, 1 * 60 * 1000);
		}
	}
	
	/***************************************************************************
	 * 单条微博信息写入xml
	 * 
	 * @param weibo
	 * @param outputFileName
	 */
	public static void weibo2Xml(String[] weibo, String outputFileName) {
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF8");
		Document doc = null;
		Element root;
		try {
			doc = DocumentHelper.createDocument();
			root = doc.addElement("data");
			root.addAttribute("version", "1");

			Element e = DocumentHelper.createElement("weibo");
			e.addElement("url").addCDATA(null == weibo[0] ? "" : XmlUtil.xmlStrFilter(weibo[0]));
			e.addElement("keyword").addCDATA(null == weibo[1] ? "" : XmlUtil.xmlStrFilter(weibo[1]));
			e.addElement("name").addCDATA(null == weibo[2] ? "" : XmlUtil.xmlStrFilter(weibo[2]));
			e.addElement("content").addCDATA(null == weibo[3] ? "" : XmlUtil.xmlStrFilter(weibo[3]));
			e.addElement("forward").addCDATA(null == weibo[4] ? "" : XmlUtil.xmlStrFilter(weibo[4]));
			e.addElement("comment").addCDATA(null == weibo[5] ? "" : XmlUtil.xmlStrFilter(weibo[5]));
			e.addElement("image").addCDATA(null == weibo[6] ? "" : XmlUtil.xmlStrFilter(weibo[6]));
			e.addElement("time").addCDATA(null == weibo[7] ? "" : XmlUtil.xmlStrFilter(weibo[7]));
			e.addElement("index").addCDATA(null == weibo[8] ? "" : XmlUtil.xmlStrFilter(weibo[8]));

			root.add(e);

		} catch (Exception e) {
			e.printStackTrace();
		}
		XmlUtil.writeXmlFileByDom4J(doc, xmlpath + outputFileName, "UTF8");
	}
	
	/***************************************************************************
	 * 微博信息写入xml
	 * 
	 * @param weibos
	 * @param outputFileName
	 */
	public static void weibos2Xml(List<String[]> weibos, String outputFileName) {
		// File file = new File(outputFileName);
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF8");
		Document doc = null;
		Element root;
		try {
			doc = DocumentHelper.createDocument();
			root = doc.addElement("data");
			root.addAttribute("version", "1");

			for (String[] st : weibos) {
				Element e = DocumentHelper.createElement("weibo");
				e.addElement("url").addCDATA(null == st[0] ? "" : XmlUtil.xmlStrFilter(st[0]));
				e.addElement("keyword").addCDATA(null == st[1] ? "" : XmlUtil.xmlStrFilter(st[1]));
				e.addElement("name").addCDATA(null == st[2] ? "" : XmlUtil.xmlStrFilter(st[2]));
				e.addElement("content").addCDATA(null == st[3] ? "" : XmlUtil.xmlStrFilter(st[3]));
				e.addElement("forward").addCDATA(null == st[4] ? "" : XmlUtil.xmlStrFilter(st[4]));
				e.addElement("comment").addCDATA(null == st[5] ? "" : XmlUtil.xmlStrFilter(st[5]));
				e.addElement("image").addCDATA(null == st[6] ? "" : XmlUtil.xmlStrFilter(st[6]));
				e.addElement("time").addCDATA(null == st[7] ? "" : XmlUtil.xmlStrFilter(st[7]));
				e.addElement("index").addCDATA(null == st[8] ? "" : XmlUtil.xmlStrFilter(st[8]));
				// e.addElement("sex").addCDATA(null == st[2] ? "" :
				// XmlUtil.xmlStrFilter(st[2]));
				// e.addElement("place").addCDATA(null == st[3] ? "" :
				// XmlUtil.xmlStrFilter(st[3]));
				// e.addElement("guanzhu").addCDATA(null == st[4] ? "" :
				// XmlUtil.xmlStrFilter(st[4]));
				// e.addElement("fans").addCDATA(null == st[5] ? "" :
				// XmlUtil.xmlStrFilter(st[5]));
				// e.addElement("wcnt").addCDATA(null == st[6] ? "" :
				// XmlUtil.xmlStrFilter(st[6]));
				// e.addElement("desc").addCDATA(null == st[7] ? "" :
				// XmlUtil.xmlStrFilter(st[7]));

				root.add(e);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		XmlUtil.writeXmlFileByDom4J(doc, xmlpath + outputFileName, "UTF8");
	}

	/***************************************************************************
	 * 关键用户写入xml
	 * 
	 * @param userdataList
	 * @param outputFileName
	 */
	public static void users2Xml(List<String[]> userdataList, String outputFileName) {
		// File file = new File(outputFileName);
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF8");
		Document doc = null;
		Element root;
		try {
			doc = DocumentHelper.createDocument();
			root = doc.addElement("data");
			root.addAttribute("version", "1");

			for (String[] st : userdataList) {

				Element e = DocumentHelper.createElement("user");
				e.addElement("index").addCDATA(null == st[0] ? "" : XmlUtil.xmlStrFilter(st[0]));// ////////////////////////
				e.addElement("name").addCDATA(null == st[1] ? "" : XmlUtil.xmlStrFilter(st[1]));
				e.addElement("fans").addCDATA(null == st[2] ? "" : XmlUtil.xmlStrFilter(st[2]));
				e.addElement("type").addCDATA(null == st[3] ? "" : XmlUtil.xmlStrFilter(st[3]));
				e.addElement("wcnt").addCDATA(null == st[4] ? "" : XmlUtil.xmlStrFilter(st[4]));
				e.addElement("forward").addCDATA(null == st[5] ? "" : XmlUtil.xmlStrFilter(st[5]));
				e.addElement("uid").addCDATA(null == st[6] ? "" : XmlUtil.xmlStrFilter(st[6]));

				root.add(e);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		XmlUtil.writeXmlFileByDom4J(doc, xmlpath + outputFileName, "UTF8");
	}

	/***************************************************************************
	 * 时间曲线写入xml
	 * 
	 * @param baseList
	 * @param timeList
	 * @param outputFileName
	 */
	public static void times2Xml(List<String[]> baseList, List<String[]> timeList, String outputFileName) {
		// File file = new File(outputFileName);
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF8");
		Document doc = null;
		Element root;
		try {
			doc = DocumentHelper.createDocument();
			root = doc.addElement("data");
			root.addAttribute("version", "1");

			for (String[] st : baseList) {
				Element e = DocumentHelper.createElement("base");
				e.addElement("name").addCDATA(null == st[0] ? "" : XmlUtil.xmlStrFilter(st[0]));
				e.addElement("date").addCDATA(null == st[1] ? "" : XmlUtil.xmlStrFilter(st[1]));

				root.add(e);
			}
			for (String[] st : timeList) {
				Element e = DocumentHelper.createElement("time");
				e.addElement("date").addCDATA(null == st[0] ? "" : XmlUtil.xmlStrFilter(st[0]));
				e.addElement("cnt").addCDATA(null == st[1] ? "" : XmlUtil.xmlStrFilter(st[1]));

				root.add(e);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		XmlUtil.writeXmlFileByDom4J(doc, xmlpath + outputFileName, "UTF8");
	}

	/***************************************************************************
	 * 地区图写入xml
	 * 
	 * @param mapList
	 * @param outputFileName
	 */
	public static void maps2Xml(List<String[]> mapList, String outputFileName) {
		// File file = new File(outputFileName);
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF8");
		Document doc = null;
		Element root;
		try {
			doc = DocumentHelper.createDocument();
			root = doc.addElement("data");
			root.addAttribute("version", "1");

			for (String[] st : mapList) {

				Element e = DocumentHelper.createElement("map");
				e.addElement("name").addCDATA(null == st[0] ? "" : XmlUtil.xmlStrFilter(st[0]));
				e.addElement("value").addCDATA(null == st[1] ? "" : XmlUtil.xmlStrFilter(st[1]));
				e.addElement("rate").addCDATA(null == st[2] ? "" : XmlUtil.xmlStrFilter(st[2]));

				root.add(e);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		XmlUtil.writeXmlFileByDom4J(doc, xmlpath + outputFileName, "UTF8");
	}
	
	/***************************************************************************
	 * 地区图写入xml
	 * 
	 * @param mapArr
	 * @param outputFileName
	 */
	public static void mapsJson2Xml(JSONArray mapJsonArr, String outputFileName) {
		// File file = new File(outputFileName);
		SAXReader reader = new SAXReader();
		reader.setEncoding("UTF8");
		Document doc = null;
		Element root;
		try {
			doc = DocumentHelper.createDocument();
			root = doc.addElement("data");
			root.addAttribute("version", "1");

			for (int i=0;i<mapJsonArr.size();i++) {
				JSONObject obj=mapJsonArr.getJSONObject(i);
				Element e = DocumentHelper.createElement("map");
				e.addElement("name").addCDATA(null == obj.getString("name") ? "" : XmlUtil.xmlStrFilter(obj.getString("name")));
				e.addElement("value").addCDATA(null == obj.getString("value") ? "" : XmlUtil.xmlStrFilter(obj.getString("value")));
				e.addElement("rate").addCDATA(null == obj.getString("rate") ? "" : XmlUtil.xmlStrFilter(obj.getString("rate")));

				root.add(e);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		XmlUtil.writeXmlFileByDom4J(doc, xmlpath + outputFileName, "UTF8");
	}


	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String xmlfile = xmlpath;
		List<String[]> weibos = new ArrayList<String[]>();
		String[] s1 = { "http://weibo.com/2097722823/ApW8Q6dm0?mod=weibotime", "keyword", "我是真梁言", "早安2014!早安北京我的家!都硬硬朗朗乐乐呵呵的哈!", "43", "131", "http://tp4.sinaimg.cn/2097722823/180/5598322770/1", "2014-01-01 06:20", "1" };
		weibos.add(s1);
		weibos2Xml(weibos, xmlfile + "a.xml");

		List<String[]> users = new ArrayList<String[]>();
		String[] u = { "aaaa", "先农坛的打火机", "64", "普通用户", "1", "5", "1440668804" };
		users.add(u);
		users2Xml(users, xmlfile + "b.xml");

		List<String[]> baseList = new ArrayList<String[]>();
		String[] b = { "BJ国安NB-奇奇", "2014-01-01 06:30" };
		baseList.add(b);
		List<String[]> timeList = new ArrayList<String[]>();
		String[] t = { "2014-01-01 06:20", "1" };
		timeList.add(t);
		times2Xml(baseList, timeList, xmlfile + "c.xml");

		List<String[]> mapList = new ArrayList<String[]>();
		String[] m = { "北京", "28", "90.32" };
		mapList.add(m);
		maps2Xml(mapList, xmlfile + "d.xml");
	}
}
