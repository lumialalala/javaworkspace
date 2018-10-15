package com.ict.mcg.util;

import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Pattern;


public class DataControl {
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
		System.out.print(getRatioStrFromVal(a));
		String s = "南方都市报深圳官方微博。24小时报料热线电话：0755-82121212；24小时在线QQ：800075175。 ";
		ICTSegmentation segmentation = new ICTSegmentation();
		List<String> wordList = segmentation.splitStrings(s, segmentation.POS_YES);
		String att1 = "m";List<String> attList = new ArrayList<String>();attList.add(att1);
		wordList = segmentation.saveWordHasAttribute(wordList);
		System.out.print(patternPhoneNumber(s));
		
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
 * @param 手机号码
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
}
