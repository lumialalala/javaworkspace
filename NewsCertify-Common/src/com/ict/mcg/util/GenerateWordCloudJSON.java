package com.ict.mcg.util;

import java.util.HashMap;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class GenerateWordCloudJSON {

	/**
	 * 生成词云的json格式串
	 * @param wordCloud
	 * @return
	 */
	public static JSONArray generateJson(HashMap<String, Integer> wordCloud) {
		JSONArray jsonArray = new JSONArray();
		
		for (String word:wordCloud.keySet()) {
			JSONObject obj = new JSONObject();
			obj.element("text", word);
			obj.element("size", wordCloud.get(word));
			jsonArray.add(obj);
		}
		
		return jsonArray;
		
	}
	
	
	//wordCloud value可能为Double
	
	public static JSONArray generateJsonForDouble(HashMap<String, Double> wordCloud) {
		JSONArray jsonArray = new JSONArray();
		
		for (String word:wordCloud.keySet()) {
			JSONObject obj = new JSONObject();
			obj.element("text", word);
			obj.element("size", wordCloud.get(word));
			jsonArray.add(obj);
		}
		
		return jsonArray;
		
	}
}
