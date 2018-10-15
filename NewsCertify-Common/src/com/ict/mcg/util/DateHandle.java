package com.ict.mcg.util;
 
import java.text.ParsePosition;  
import java.text.SimpleDateFormat;   
import java.util.Calendar;
import java.util.Date;  
import org.apache.commons.httpclient.util.DateUtil; 
import org.apache.commons.logging.Log;  
import org.apache.commons.logging.LogFactory;  

/**
 * structure for handle date
 * @author senochow
 *
 */
public class DateHandle {
	private static Log log = LogFactory.getLog(DateUtil.class);  
	private static Calendar cale = Calendar.getInstance();  
    public static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
    public static SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");  
    public static SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
	
    /**  
     * Access to the server's current date and time，return in string format：yyyy-MM-dd HH:mm:ss 
     */  
    public static String getDateTime(){  
        try{  
            return sdf2.format(cale.getTime());  
        } catch(Exception e){  
            log.debug("DateUtil.getDateTime():" + e.getMessage());  
            return "";  
        }  
    }  
    /**  
     * Access to the server's current date and time，return in string format：yyyy-MM-dd
     */  
    public static String getDate(){  
        try{  
            return sdf.format(cale.getTime());  
        } catch(Exception e){  
            log.debug("DateUtil.getDate():" + e.getMessage());  
            return "";  
        }  
    }  
  
    /**  
     * Access to the server's current date and time，return in string format：HH:mm:ss 
     */  
    public static String getTime(){  
        String temp = "";  
        try{  
            temp += sdf1.format(cale.getTime());  
            return temp;  
        } catch(Exception e){  
            log.debug("DateUtil.getTime():" + e.getMessage());  
            return "";  
        }  
    }  
	 /**  
     * Compute the number of days between two dates ,  
     * date1 is later than date2 ,
     * DateFormate: yyyy-MM-dd
     */  
    public static int getMargin(String date1,String date2){  
        int margin;  
        try{  
            ParsePosition pos = new ParsePosition(0);  
            ParsePosition pos1 = new ParsePosition(0);  
            Date dt1 = sdf.parse(date1,pos);  
            Date dt2 = sdf.parse(date2,pos1);  
            long l = dt1.getTime() - dt2.getTime();  
            margin = (int)(l / (24 * 60 * 60 * 1000));  
            return margin;  
        } catch(Exception e){  
            log.debug("DateUtil.getMargin():" + e.toString());  
            return 0;  
        }  
    }  
    /**  
     * Returns the number of days in a given month in the system year 
     * @return 指定月的总天数  
     */  
    @SuppressWarnings("deprecation")  
    public static String getMonthLastDay(int month)  
    {  
        Date date=new Date();  
        int[][] day={{0,30,28,31,30,31,30,31,31,30,31,30,31},  
                        {0,31,29,31,30,31,30,31,31,30,31,30,31}};     
        int year=date.getYear()+1900;  
        if(year%4==0 && year%100!=0 || year%400==0)   
        {  
            return day[1][month]+"";  
        }  
        else  
        {  
            return day[0][month]+"";  
        }  
    }  
      
    /**  
     * Returns the number of days in the specified month in the specified year  
     * @return 指定月的总天数  
     */  
    public static String getMonthLastDay(int year,int month)  
    {  
        int[][] day={{0,30,28,31,30,31,30,31,31,30,31,30,31},  
                        {0,31,29,31,30,31,30,31,31,30,31,30,31}};  
        if(year%4==0 && year%100!=0 || year%400==0)   
        {  
            return day[1][month]+"";  
        }  
        else  
        {  
            return day[0][month]+"";  
        }  
    }  
//	/**
//	 * @param args main function for test
//	 */
//	public static void main(String[] args) {
//		// TODO Auto-generated method stub
//		System.out.println(getDateTime());
//	}

}
