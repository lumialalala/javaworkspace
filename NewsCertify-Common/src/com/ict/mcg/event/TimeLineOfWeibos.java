package com.ict.mcg.event;
/**
 * @author WuBo
 */
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.util.TimeConvert;



public class TimeLineOfWeibos  {


	private ArrayList<WeiboEntity> mWeiboList;
	private List<String[]> TimeLineResult;

	public TimeLineOfWeibos(ArrayList<WeiboEntity> weibolist)
	{

		mWeiboList = new ArrayList<WeiboEntity>();
		mWeiboList = weibolist;
		TimeLineResult = Collections.synchronizedList(new ArrayList<String[]>());
	}
	public class sortClass implements Comparator{  
		public int compare(Object arg0,Object arg1){ 
			int flag=0;
			Date date0 = (Date)arg0;
			Date date1 = (Date)arg1;
			flag = date0.compareTo(date1);
			return flag;  
		}  
	}

	public boolean analysistimeline()
	{
		//		RunTime time = new RunTime("TimeLine Process");		time.GetStartTime();
		if(mWeiboList==null||mWeiboList.size()==0)
		{
			String message = "前台传入的事件微博为空，无法生成TimeLine";
			Logger log = Logger.getLogger(this.getClass());
			log.error(message);
			return false;
		}


		List<String> mListNodeTime = Collections.synchronizedList(new ArrayList<String>());
		List<Integer> mListCount = Collections.synchronizedList(new ArrayList<Integer>());
		try {

			//时间排序
			Collections.sort(mWeiboList, new Comparator<WeiboEntity>() {
				public int compare(WeiboEntity w0, WeiboEntity w1) {
					long j = Long.parseLong(w0.getTime()) - Long
							.parseLong(w1.getTime());
					if (j == 0) {
						return 0;
					} else if (j > 0) {
						return 1;
					} else {
						return -1;
					}
				}
			});

			//时间格式转换，判断是否在同一个bin
			List<Date> ListWeiboTime = Collections.synchronizedList(new ArrayList<Date>());
			SimpleDateFormat dateformatsecond = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			for(int index = 0; index<mWeiboList.size(); index++)
			{
				//				System.out.println("时间::::"+mListForwardRelation.get(index)[5]);
				String time = TimeConvert.getStringTime(Long.parseLong(mWeiboList.get(index).getTime()));
				Date weibotime = dateformatsecond.parse(time);
				ListWeiboTime.add(weibotime);//forward time
			}
			//时间列表排序
			sortClass sort = new sortClass();
			Collections.sort(ListWeiboTime, sort);


			//将每次时间都对应到相应的bin
			String previousnode = null ;
			SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");//Date to String
			int i = 0;
			for(int index = 0; index<ListWeiboTime.size(); index++){

				Date date = ListWeiboTime.get(index);
				Date thisnode = changeNodeTime(date);//按照bin的生成规则改变时间显示
				String thisnodestr = dateformat.format(thisnode);

				if(previousnode!=null)
				{
					//System.out.println("thisnode: "+thisnodestr+";previousnode=" + previousnode+ "index= " + i+ " \n" );
					if(!thisnodestr.equals(previousnode))
					{
						mListCount.add(i);
						mListNodeTime.add(thisnodestr);
						previousnode = thisnodestr;
						i=0;
						i++;	
					}
					else
					{
						i++;
					}
				}
				else 
				{
					previousnode = thisnodestr;
					mListNodeTime.add(thisnodestr);
					i++;
				}
			}
			mListCount.add(i);

			//存入结果集合
			//				System.out.println("time list:"+ mListNodeTime);
			//				System.out.println("timecount:"+ mListCount);
			//				System.out.println("time list size:" + mListNodeTime.size() + "  size:" + mListCount.size());
			System.out.print("时间线：");
			for(int index = 0; index <mListNodeTime.size();index++)
			{
				//System.out.println("time:" + mListNodeTime.get(index) + "  count:" + mListCount.get(index));
				String[] result = new String[2];
				result[0] = mListNodeTime.get(index);
				result[1] = String.valueOf(mListCount.get(index));
				TimeLineResult.add(result);
				System.out.print(TimeLineResult.get(index)[0]+"="+TimeLineResult.get(index)[1]+" ,");
			}System.out.println("");

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(TimeLineResult.size()>0)
			return true;
		else
			return false;

	}

	public Date changeNodeTime(Date date) {
		int bin = 60;
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		calendar.set(Calendar.MINUTE, (int)(calendar.get(Calendar.MINUTE)/bin)*bin);
		date = calendar.getTime();
		return date;
	}

	public int getSeconds(Date startdate, Date enddate) {
		long time = enddate.getTime() - startdate.getTime();
		int second = new Long(time / 1000).intValue();
		return second;
	}

	public void TimeLineResult(List<String[]> TimeLineResult) {
		this.TimeLineResult = TimeLineResult;
	}

	public List<String[]> getTimeLineResult() {
		return TimeLineResult;
	}


}
