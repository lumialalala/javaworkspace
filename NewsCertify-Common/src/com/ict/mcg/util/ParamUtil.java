package com.ict.mcg.util;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ParamUtil {

//	public static final String REDIS_ADDRESS = "122.115.50.63";
	public static String[] REDIS_ADDRESS = {"139.196.175.194"};
//	public static String[] REDIS_ADDRESS = {"10.25.0.235"}; //本地
//	public static String[] REDIS_ADDRESS = {"139.129.217.181"};  //aliyun linux 181
//	public static String[] REDIS_ADDRESS = {"139.196.202.163"};  //aliyun windows 150
//	public static String[] REDIS_ADDRESS = {"139.129.215.148"};  //aliyun linux 148
	public static int REDIS_PORT = 22121;
//	public static int REDIS_PORT = 6379;
	
	public static String ZOOKEEPER_PATHPORT = "139.196.175.194:2181";
//	public static String ZOOKEEPER_PATHPORT = "122.115.50.63:2181";
//	public static final String ZOOKEEPER_PATHPORT = "202.85.222.108:2181";
	public static String GATHER_PATH = "/weibo_gather/gathers";
    public static String EXTRACTTITLE_PATH = "/home/ictmcg/ExtractTitle/" ;
	
	public static String SEARCH_RESULT_MAP = "searchResult";
	public static String FORWARD_RESULT_MAP = "forwardResultMap";
	public static String USER_RESULT_MAP = "userResultMap";
	public static String TIMESTAMP_MAP = "timestampMap";
	public static String QUERY_HOT_MAP = "queryHot";
	public static String QUERY_URL_MAP = "queryUrl";
	public static String QUERY_TIME_MAP = "queryTime";
	public static String USER_PART_INFO_MAP = "partUserInfoMap";
	public static String USER_FULL_INFO_MAP = "allUserInfoMap";
	public static String SINGLEWEIBO_FORWARD_MAP = "singleweiboForwardResultMap";
	public static String SINGLEWEIBO_FORWRAD_RESULT = "singleweiboForwardResult"; 
	public static String EVENT_TABLE = "event_ready";
	public static String RESULT_TABLE = "result_ready";
	public static String NEWSCLUE_TABLE = "newsclues_ready";
	
//	public static String[] WEB_SERVICE_ADDRESS = {"http://122.115.50.67:9999"};
//	public static String[] WEB_SERVICE_ADDRESS = {"http://127.0.0.1:9999"};
//	public static String[] WEB_SERVICE_ADDRESS = {"http://122.115.50.63:9999"};
	public static String[] WEB_SERVICE_ADDRESS = {"http://139.196.175.194:9999","http://139.196.175.195:9999"};
//	public static String[] WEB_SERVICE_ADDRESS = {"http://202.85.222.108:9999"};
	
	
	public static long FORWARD_VALIDTIME = 2*60;
	public static long USER_VALIDTIME = 2*24*60;
	
	public static int HOT_QUERY_COUNT_UPPER = 50;
	public static int HOT_QUERY_COUNT_FLOOR = 15;
	public static int HOT_QUERY_SHOW_COUNT = 10;
	public static int HOT_QUERY_REFRESH_INTERVAL = 30;
	public static int TRS_CLUE_REFRESH_INTERVAL = 1440;
	public static int TRS_CLUE_REFRESH_TIMECLOCK = 5;
	
	
	public static void config(String filePath) {
		// TODO Auto-generated constructor stub
		InputStream in;
		Properties config = new Properties();
		try {
			in = new BufferedInputStream (new FileInputStream(filePath));
			config.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		REDIS_ADDRESS = config.getProperty("REDIS_ADDRESS").split(",");
		REDIS_PORT = Integer.parseInt(config.getProperty("REDIS_PORT"));
		ZOOKEEPER_PATHPORT = config.getProperty("ZOOKEEPER_PATHPORT");
		GATHER_PATH = config.getProperty("GATHER_PATH");
        EXTRACTTITLE_PATH = config.getProperty("EXTRACTTITLE_PATH") ;

		SEARCH_RESULT_MAP = config.getProperty("SEARCH_RESULT_MAP");
		FORWARD_RESULT_MAP = config.getProperty("FORWARD_RESULT_MAP");
		USER_RESULT_MAP = config.getProperty("USER_RESULT_MAP");
		TIMESTAMP_MAP = config.getProperty("TIMESTAMP_MAP");
		QUERY_HOT_MAP = config.getProperty("QUERY_HOT_MAP");
		QUERY_URL_MAP = config.getProperty("QUERY_URL_MAP");
		QUERY_TIME_MAP = config.getProperty("QUERY_TIME_MAP");
		
		WEB_SERVICE_ADDRESS = config.getProperty("WEB_SERVICE_ADDRESS").split(",");
		
		FORWARD_VALIDTIME = Integer.parseInt(config.getProperty("FORWARD_VALIDTIME"));
		USER_VALIDTIME = Integer.parseInt(config.getProperty("USER_VALIDTIME"));
		HOT_QUERY_COUNT_UPPER = Integer.parseInt(config.getProperty("HOT_QUERY_COUNT_UPPER"));
		HOT_QUERY_COUNT_FLOOR = Integer.parseInt(config.getProperty("HOT_QUERY_COUNT_FLOOR"));
		HOT_QUERY_SHOW_COUNT = Integer.parseInt(config.getProperty("HOT_QUERY_SHOW_COUNT"));
		HOT_QUERY_REFRESH_INTERVAL = Integer.parseInt(config.getProperty("HOT_QUERY_REFRESH_INTERVAL"));
		TRS_CLUE_REFRESH_INTERVAL = Integer.parseInt(config.getProperty("TRS_CLUE_REFRESH_INTERVAL"));
		TRS_CLUE_REFRESH_TIMECLOCK = Integer.parseInt(config.getProperty("TRS_CLUE_REFRESH_TIMECLOCK"));
	}
}
