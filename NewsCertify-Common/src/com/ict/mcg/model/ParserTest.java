package com.ict.mcg.model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.ICTAnalyzer;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.WeiboEntityProcessor;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.service.EventService;
import com.ict.mcg.util.FileIO;
import com.ict.mcg.util.ICTMongoClient;
import com.ict.mcg.util.ParamUtil;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

import net.sf.json.JSONArray;

public class ParserTest {
	private static Set<String> stopSet = null;
	private static String stop_word = FileIO.getFilePath() + "stop_words.txt";
	private static String stop_word_resource = FileIO.getResourcePath() + "stop_words.txt";

	public static void main(String[] args) throws IOException {
		DBCollection eventColl = ICTMongoClient.getCollection("NewsCertify", "event_ready");
		DBCollection resColl = ICTMongoClient.getCollection("NewsCertify", "result_ready");
		
		BasicDBObject query = new BasicDBObject();
		query.append("from", "weibo_search");
		query.append("update", true);
		DBCursor cursor = eventColl.find(query);
		
		JSONArray jArray = new JSONArray();
		
		while (cursor.hasNext()) {
			DBObject e = cursor.next();
			String id = e.get("clue_id").toString();
			String key_event = e.get("keywords").toString();
			BasicDBObject query1 = new BasicDBObject();
			query1.append("clue_id", id);
			query1.append("hidden", new BasicDBObject("$ne", true));
			DBObject res = resColl.findOne(query1);
			if (res == null) {
				continue;
			}
			BasicDBList profile = (BasicDBList) res.get("profile");
			String key_result = profile.get(1).toString();
			
			Map<String, String> map = new HashMap<String, String>();
			map.put("id", id);
			map.put("key_event", key_event);
			map.put("key_result", key_result);
			jArray.add(map);
			
			File outfile = new File("F:\\BTM\\input\\"+id);
			BufferedWriter writer1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "utf-8"));
			
			BasicDBList weibolist = (BasicDBList)e.get("weibo");
			ArrayList<WeiboEntity> weList = WeiboEntity.convert(weibolist);
			for (WeiboEntity we : weList) {
				String content = we.getContent();
				ArrayList<WordNode> segs = WeiboEntityProcessor.getSegments(content);
				for(WordNode wn : segs) {
					String pos=wn.getPos();
					if(pos.startsWith("u")||pos.startsWith("c")||pos.startsWith("p")
							||pos.startsWith("d")||pos.startsWith("q")
							||pos.startsWith("r")||pos.startsWith("b")||pos.startsWith("f")) {
						continue;
					}
					writer1.write(wn.getWord() + " ");
					writer1.flush();
				}
				writer1.write("\n");
				writer1.flush();
			}
			writer1.close();
		}
		
		File records = new File("F:\\BTM\\input\\records_" + System.currentTimeMillis() + ".json");
		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(records), "utf-8"));
		writer.write(jArray.toString());
		writer.flush();
		writer.close();
	
	}
//		String id="541b7fdd0be084265f2c0807b521e4a0";
////		String id="e4d0aad3cec7ac6c623906145bbab70b";
//		File outfile = new File("D:\\newspace\\test\\clue2.txt");
//		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outfile), "utf-8"));
//		DBCollection cluecoll = ICTMongoClient.getCollection("NewsCertify", ParamUtil.NEWS_CLUE_TABLE);
//		DBObject obj= cluecoll.findOne(new BasicDBObject("id", id));
//		String content = (String)obj.get("key_weibo");
//		ArrayList<WordNode> segs = WeiboEntityProcessor.getSegments(content);
//		for(WordNode wn : segs){
//			String pos=wn.getPos();
//			if(pos.startsWith("n")){
//				writer.write(wn.getWord()+" ");
//				writer.flush();
//			}
//		}
//		writer.write("\n");
//		writer.flush();
//		writer.close();
//	}
	
}