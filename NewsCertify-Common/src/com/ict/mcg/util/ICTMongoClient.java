package com.ict.mcg.util;

import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class ICTMongoClient {

	private static MongoClient mongoClient = null;
	private static MongoClient mongoClientWithPwd = null;
	
	private static MongoClient initMongo() {
		if (mongoClient == null) {
			try {
//				mongoClient = new MongoClient("210.56.193.244", 27017);
//				mongoClient = new MongoClient("10.25.0.231", 27017);
//				mongoClient = new MongoClient("127.0.0.1", 27017);
				mongoClient = new MongoClient("139.129.217.181", 27017);
			} catch (UnknownHostException e) {
				System.out.println("mongoClient:" + mongoClient);
				e.printStackTrace();
			}
		}
		return mongoClient;
	}
	
	private static MongoClient initMongoWithPwd() {
		//连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址  
        //ServerAddress()两个参数分别为 服务器地址 和 端口  
        ServerAddress serverAddress;
		try {
			serverAddress = new ServerAddress("w4.aishiyao.com",80);
			List<ServerAddress> addrs = new ArrayList<ServerAddress>();  
	        addrs.add(serverAddress);  
	          
	        //MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码  
	        MongoCredential credential = MongoCredential.createScramSha1Credential("ictmcg", "admin", "ictsearch".toCharArray());  
	        List<MongoCredential> credentials = new ArrayList<MongoCredential>();  
	        credentials.add(credential);  
	          
	        //通过连接认证获取MongoDB连接  
	        mongoClientWithPwd = new MongoClient(addrs,credentials);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		
		return mongoClientWithPwd;
	}
	
	public static DB getDatabase(String dbname) {
		mongoClient = initMongo();
		DB db = null;
		if (mongoClient != null) {
			db = mongoClient.getDB(dbname);
		}
		return db;
	}

	public static DBCollection getCollection(String dbname, String collectname) {
		DB db = getDatabase(dbname);
		DBCollection coll = null;
		if (db != null) {
			coll = db.getCollection(collectname);
		}
		return coll;
	}
	
	public static DB getDatabaseWithPwd(String dbname) {
		mongoClientWithPwd = initMongoWithPwd();
//		System.out.println(mongoClientWithPwd);
		DB db = null;
		if (mongoClientWithPwd != null) {
			db = mongoClientWithPwd.getDB(dbname);
		}
//		System.out.println(db);
		return db;
	}

	public static DBCollection getCollectionWithPwd(String dbname, String collectname) {
		DB db = getDatabaseWithPwd(dbname);
		DBCollection coll = null;
		if (db != null) {
			coll = db.getCollection(collectname);
		}
		return coll;
	}
	
	
	/**通用的函数，用于将mongo的时间数据转换为时间串*/
	private static SimpleDateFormat sdfCommon = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static String getDateString(Object o){
		if(o==null) return "";
		if(o instanceof Date){
			return sdfCommon.format((Date)o);
		}
		if(o instanceof Long){
			Date t = new Date((long)o);
			return sdfCommon.format(t);
		}
		return "";
	}
	
	
	public static String getString(DBObject obj, String name){
		if(obj==null) return null;
		Object o = obj.get(name);
		if(o==null) return null;
		if(!(o instanceof String)) return null;
		return (String)o;
	}
	
	public static long getLong(DBObject obj, String name){
		if(obj==null) return -1;
		Object o = obj.get(name);
		if(o==null) return -1;
		if(!(o instanceof Long)) return -1;
		return (long)o;
	}
	
	public static String getDateString(DBObject obj, String name){
		long v = getLong(obj, name);
		if(v==-1) return null;
		return getDateString(v);
	}
	
	public static void main(String[] args) {
		DBCollection collect = ICTMongoClient.getCollection("NewsCertify", "result");
		try {
			BasicDBObject query = new BasicDBObject("emoVal",3).append("profile", new BasicDBObject("$exists", true));
			BasicDBObject sortBy = new BasicDBObject("start", 1);
			DBCursor cursor = collect.find(query).sort(sortBy).limit(1);
			int i = 0;
			while (cursor.hasNext()) {
				DBObject obj = cursor.next();
				System.out.println(obj.toString());
				System.out.println(getDateString(obj, "timestamp"));
				BasicDBList profile = (BasicDBList)obj.get("profile");
				String[] p = new String[profile.size()];
//				for (Object p2 : profile) {
//					System.out.println(p2);
//				}
//				p = profile.toArray(p);
//				for (String pp : p) {
//					System.out.println(pp);
//				}
				System.out.println(p.length);
				i++;
			}
			System.out.println(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
