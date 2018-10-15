package com.ict.mcg.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class DataHandle {
	
	final static String url_pa = "(http://|ftp://|https://|www){0,1}[^\u4e00-\u9fa5\\s]*?\\.(com|net|cn|me|tw|fr)[^\u4e00-\u9fa5\\s]*";
	final static String[] PHONENUMBER_PREFIX = { "130", "131", "132", "145", "155", "156", "185", "186", "134", "135", "136", "137", "138",
		"139", "147", "150", "151", "152", "157", "158", "159", "182", "183", "187", "188", "133", "153", "189", "180" };
	public static double getThreeNumberDouble(double val) {		
        double val1 = keepNDecimals(val, 3);	
		return val1;
	}
	
	public static String getRatioStrFromVal(double val) {
		String ratioStr = "";
		val = val*100;
        double val1 = keepNDecimals(val, 3);
		ratioStr = Double.toString(val1).concat("%");
		return ratioStr;
	}
	public static void main(String[] args)  {
		double a = 0.654324452;
		a = getThreeNumberDouble(a);
		String str = "asfdas撒撒旦按时转发微博，、、@转发微博";
		System.out.print(textClean(str));
	}

    /**
     * 匹配手机号码
     * <p>
     * 新联通</br>
     * 	 （中国联通+中国网通）手机号码开头数字 130,131,132,145,155,156,185,186</br>
     * 新移动</br>
     * 　（中国移动+中国铁通）手机号码开头数字</br>
     * 134,135,136,137,138,139,147,150,151,152,157,158,159,182,183,187,188</br>
     * 新电信</br>
     * 　（中国电信+中国卫通）手机号码开头数字 133,153,189,180</br>
     * </p>
     * @param number
     * @return	参数为null和不合法时返回false，否则返回true
     */
    public static boolean patternPhoneNumber(String number) {
        int len = PHONENUMBER_PREFIX.length;
        if (number != null) {
            for (int i = 0; i < len; i++) {
                Pattern p = Pattern.compile(PHONENUMBER_PREFIX[i] + "\\d{8}");
                if (p.matcher(number).matches()) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * Keep n decimal places
     * @param val
     * @param k
     * @return
     */
    public static double keepNDecimals(double val, int k) {
        BigDecimal bg = new BigDecimal(val);
        double val1 = bg.setScale(k, BigDecimal.ROUND_HALF_UP).doubleValue();
        return val1;
    }

    public static String filter_url(String content) {
        String str = content.replaceFirst(url_pa, "");
        return str;
    }
    /**
     * weibo text cleaning
     * @param text
     * @return
     */
    public static String textClean(String text) {
        if(text == null){
            System.out.println("Can't get text");
            return null;
        }
        String weibotext = text;
        //remove reposted info by other users
    //	int repostIndex = weibotext.indexOf("//<a href");
    //	if (repostIndex>=0) {
    //		weibotext = weibotext.substring(0,repostIndex);
    //	}
    //	if (weibotext.length()==0) {
    //		return "";
    //	}

        String result = "";
        Pattern pattern = Pattern.compile("<([^>]*)>");
        Matcher matcher = pattern.matcher(weibotext);
        String labeltext = "";
        while(matcher.find()){
            labeltext = matcher.group();
            result += " " + labeltext;
        }

        //write at message in the data file
        weibotext = matcher.replaceAll("");
        //transfer link
        weibotext = weibotext.replaceAll("\\\\/", "/");
        pattern = Pattern.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
        matcher = pattern.matcher(weibotext);

        result = "";
        String linktext = "";
        while(matcher.find()){
            linktext = matcher.group();
            result += " " + linktext;
        }
        //write link message in the data file
        weibotext = matcher.replaceAll("");

		/*
		 * @message
		 * */
		pattern = Pattern.compile("@[^\\.^\\,^:^;^!^\\?^\\s^#^@^。^，^：^；^！^？]+");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String attext = "";     
		while(matcher.find()){
			attext = matcher.group();
			result += " " + attext;
		} 
		//write at message in the data file
		weibotext = matcher.replaceAll("");
	//	System.out.println(weibotext);

		/*
		 * #message
		 * */
		pattern = Pattern.compile("#([^\\#|.]+)#");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String topictext = "";     
		while(matcher.find()){
			topictext = matcher.group();
			result += " " + topictext;
		}  
		//write topic message in the data file
		weibotext = matcher.replaceAll("");
		weibotext=weibotext.replace("时候", "");
		weibotext=weibotext.replace("朋友", "");
		weibotext=weibotext.replace("问题", "");		
		weibotext=weibotext.replace("喜欢", "");		
		weibotext=weibotext.replace("感觉", "");	
		weibotext=weibotext.replace("中国", "");	
		weibotext=weibotext.replace("世界", "");	
		weibotext = weibotext.replace("转发微博", "");
		if (weibotext.equals("转发微博 ")) {
			weibotext = "";
		}
		return weibotext;
	}

    public static String replaceCommonStr(String content) {
        content = content.replaceAll("转发微博", "");
        content = content.replaceAll("o网页链接","");
        content = content.replaceAll("网页链接","");
        content = content.replaceAll("...展开全文c","");
        content = content.replaceAll("展开全文c","");
        content = content.replaceAll("&nbsp;", "");

        return content;
    }

    public static List<Map.Entry<String, Double>> getSortList(Map<String, Double> tmplist){
        Set<Map.Entry<String, Double>> set = tmplist.entrySet();
        List<Map.Entry<String, Double>> sortList = new ArrayList<Map.Entry<String, Double>>(
               set);
        Collections.sort(sortList, new Comparator<Map.Entry<String, Double>>() {
                                       public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {
                if ((o2.getValue() - o1.getValue())>0)
                    return 1;
                  else if((o2.getValue() - o1.getValue())==0)
                    return 0;
                  else
                    return -1;

        }
        });
        return sortList;

    }
    public static List<Map.Entry<String, Integer>> getSortList_int(Map<String, Integer> tmplist){
        Set<Map.Entry<String, Integer>> set = tmplist.entrySet();
        List<Map.Entry<String, Integer>> sortList = new ArrayList<Map.Entry<String, Integer>>(
               set);
        Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
                                       public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                if ((o2.getValue() - o1.getValue())>0)
                    return 1;
                  else if((o2.getValue() - o1.getValue())==0)
                    return 0;
                  else
                    return -1;

        }
        });
        return sortList;

    }

    public static Map<Integer, String> sortMapByKey(Map<Integer, String> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }
        Map<Integer, String> sortMap = new TreeMap<Integer, String>();
        sortMap.putAll(map);
        return sortMap;
    }

}
