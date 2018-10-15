/**
 * 
 */
package com.ict.mcg.util;

import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * @author WuBo 
 */
public class TimeConvert {

	/**
	 * 获取当前时间long型表达
	 *
	 * @return
	 */
	public static int getNowHours() {
		Calendar now = Calendar.getInstance();
//		Date nowd = now.getTime();
		return now.get(Calendar.HOUR_OF_DAY);
	}

	/**
	 * 获取当前时间long型表达
	 * 
	 * @return
	 */
	public static long getNow() {
		Calendar now = Calendar.getInstance();
		long l = now.getTimeInMillis();
		return l;
	}

	/**
	 * 将long型表达的时间转为Date型
	 * 
	 * @param l 
	 * @return
	 */
	public static Date getDate(long l) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);
		Date d = c.getTime();
		return d;
	}
	/**
	 * 将long型表达的时间转为String
	 */
	@SuppressWarnings("deprecation")
	public static String getStringDate(long l){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);
		Date d = c.getTime();
		int y = d.getYear()+1900;
		int m = d.getMonth()+1;
		int da = d.getDate();
		return (""+y+"-"+m+"-"+da);
	}
	
	/**
	 * 将long型表达的时间转为String
	 */
	@SuppressWarnings("deprecation")
	public static String getStringTime(long l){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(l);
		Date d = c.getTime();
		int y = d.getYear()+1900;
		int m = d.getMonth()+1;
		int da = d.getDate();
		int h = d.getHours();
		int min = d.getMinutes();
		int s = d.getSeconds();
		return (""+y+"-"+m+"-"+da+" "+h+":"+min+":"+s);
	}
	
	/**
	 * 将string类型的时间转换为long型
	 * 
	 */
	public static long convertString(String time){		
		StringTokenizer st = new StringTokenizer(time,"- :");
		Calendar c = Calendar.getInstance();
		c.clear();
		c.set(Integer.parseInt(st.nextToken()), Integer.parseInt(st.nextToken())-1, Integer.parseInt(st.nextToken()));
		if(st.hasMoreTokens())
			c.set(Calendar.HOUR_OF_DAY,(Integer.parseInt(st.nextToken())));
		if(st.hasMoreTokens())
			c.set(Calendar.MINUTE,(Integer.parseInt(st.nextToken())));
		if(st.hasMoreTokens())
			c.set(Calendar.SECOND,(Integer.parseInt(st.nextToken())));		
		
		long result = c.getTimeInMillis();
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		System.out.println(getStringTime( 1427762209409l));
//		System.out.println(getNow());
//		System.out.println(Double.parseDouble("20.6"));
//		String time = "2013 11 4";
//		convertString(time);
		String timestr = "2016-11-09";
		System.out.println(TimeConvert.getNowHours());
		System.out.println(TimeConvert.convertString(timestr));
		System.out.println(getStringTime(1476925282000l));
		Date end = TimeConvert.getDate(1476925282000l + 10 *60  * 60 * 1000);
		System.out.println(getStringTime(1476925282000l + 10 * 60 * 60 * 1000));
		int end_hour = end.getHours();
		String endhours = String.valueOf(end_hour);
		if (end_hour < 10) {
			endhours = "0" + endhours;
		}
		System.out.println(endhours);
	}
}
