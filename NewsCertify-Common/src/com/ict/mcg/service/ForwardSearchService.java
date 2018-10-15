package com.ict.mcg.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.logging.Level;

import org.apache.log4j.Logger;

import com.ict.mcg.util.RedisUtil;
import com.ict.mcg.util.TimeConvert;
import com.ict.mcg.webservice.client.ServiceClient;
import com.ict.mcg.webservice.service.GatherWebService;

public class ForwardSearchService implements Callable<String> {

	private long startDate;
	public List<String[]> getPeakLine() {
		return peakLine;
	}

	private long endDate;
	private String keywords;
	private String searchNumMap="searchNumMap";
	private List<String[]> weiboTimeLine = new ArrayList<String[]>();
	private List<String[]> reducedWeiboTimeLine = new ArrayList<String[]>();
	private List<String[]> peakLine = new ArrayList<String[]>();
	private Logger log = Logger.getLogger(ForwardSearchService.class);
	private Map<Integer, Double> baseMap = new HashMap<Integer, Double>();
	
	private int is_online=0;//0 离线搜索，1在线搜索
	
	public void set_online(){
		is_online=1;
	}
	
	public ForwardSearchService(String keywords, long startDate, long endDate) {
		this.startDate = startDate;
		this.endDate = endDate;
		this.keywords = keywords;
	}
	
	public List<String[]> getWeiboTimeLine() {
		return  weiboTimeLine;
	}
	
	public List<String[]> getReducedWeiboTimeLine() {
		return reducedWeiboTimeLine;
	}

	public String call() throws Exception {
		List<String> expandDateList = new ArrayList<String>();
		int span = (int) ((endDate - startDate) / (24 * 3600000) + 1);
		//规则1，如果跨度大于等于4天，则不需要扩展
		if (span >= 4) {
		//规则2，如果跨度包含当天，则只扩展前一天
		} else if (System.currentTimeMillis() - endDate < 24*60*60*1000) {
			expandDateList.add(TimeConvert.getStringDate((startDate-24*60*60*1000)));
		} else {
			expandDateList.add(TimeConvert.getStringDate((startDate-24*60*60*1000)));
			expandDateList.add(TimeConvert.getStringDate((endDate+24*60*60*1000)));
		}
		
		String[] timeLineUnit = null; 
		for (int i = 0; i < span; ++i) {
			String date = TimeConvert.getStringDate(startDate+i*24*3600000);
			int weiboCount = 0;
			byte[] data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
									(keywords + "\t" + date + "-0" + ":" + date + "-8").getBytes("UTF-8"));
			if (data != null) {
				weiboCount += Integer.parseInt(new String(data, "UTF-8"));
			}
			timeLineUnit = new String[2];
			timeLineUnit[0] = date+" 00-08";
			timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
			weiboTimeLine.add(timeLineUnit);
			
			weiboCount = 0;
			data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
					(keywords + "\t" + date + "-9" + ":" + date + "-12").getBytes("UTF-8"));
			if (data != null) {
				weiboCount += Integer.parseInt(new String(data, "UTF-8"));
			}
			timeLineUnit = new String[2];
			timeLineUnit[0] = date+" 09-12";
			timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
			weiboTimeLine.add(timeLineUnit);
			
			weiboCount = 0;
			data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
					(keywords + "\t" + date + "-13" + ":" + date + "-15").getBytes("UTF-8"));
			if (data != null) {
				weiboCount += Integer.parseInt(new String(data, "UTF-8"));
			}
			timeLineUnit = new String[2];
			timeLineUnit[0] = date+" 13-15";
			timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
			weiboTimeLine.add(timeLineUnit);
					
			weiboCount = 0;
			data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
					(keywords + "\t" + date + "-16" + ":" + date + "-18").getBytes("UTF-8"));
			if (data != null) {
				weiboCount += Integer.parseInt(new String(data, "UTF-8"));
			}
			timeLineUnit = new String[2];
			timeLineUnit[0] = date+" 16-18";
			timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
			weiboTimeLine.add(timeLineUnit);
			
			weiboCount = 0;
			data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
					(keywords + "\t" + date + "-19" + ":" + date + "-21").getBytes("UTF-8"));
			if (data != null) {
				weiboCount += Integer.parseInt(new String(data, "UTF-8"));
			}
			timeLineUnit = new String[2];
			timeLineUnit[0] = date+" 19-21";
			timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
			weiboTimeLine.add(timeLineUnit);
			
			weiboCount = 0;
			data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
					(keywords + "\t" + date + "-22" + ":" + date + "-23").getBytes("UTF-8"));
			if (data != null) {
				weiboCount += Integer.parseInt(new String(data, "UTF-8"));
			}
			timeLineUnit = new String[2];
			timeLineUnit[0] = date+" 22-23";
			timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
			weiboTimeLine.add(timeLineUnit);
//			keywords + "\t" + date + "-9" + ":" + date + "-12";
//			keywords + "\t" + date + "-13" + ":" + date + "-14";
//			keywords + "\t" + date + "-15" + ":" + date + "-18";
//			keywords + "\t" + date + "-19" + ":" + date + "-21";
//			keywords + "\t" + date + "-22" + ":" + date + "-23";
//			String key = keywords+"\t"+date+"-0"
					
		}
		
		if (expandDateList.size() > 0) {
			String query = keywords+"\t";
			for (String expandDate:expandDateList) {
				query += expandDate+":";
			}
			GatherWebService service = ServiceClient.getServiceInstance(is_online);
			String returnFlag = service.expandSearchWeibo(query);
			
			if (returnFlag.contains("success")) {
				for (String expandDate:expandDateList) {
					int weiboCount = 0;
					String key = keywords+"\t"+expandDate+"-0" +":"+expandDate+"-8";
					byte[] data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
												key.getBytes("UTF-8"));
					if (data != null) {
						weiboCount += Integer.parseInt(new String(data, "UTF-8"));
					} else {
						
					}
					
					timeLineUnit = new String[2];
					timeLineUnit[0] = expandDate+" 00-08";
					timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
					weiboTimeLine.add(timeLineUnit);
					
					weiboCount = 0;
					key = keywords+"\t"+expandDate+"-9" +":"+expandDate+"-12";
					data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
												key.getBytes("UTF-8"));
					if (data != null) {
						weiboCount += Integer.parseInt(new String(data, "UTF-8"));
					}
					
					timeLineUnit = new String[2];
					timeLineUnit[0] = expandDate+" 09-12";
					timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
					weiboTimeLine.add(timeLineUnit);
					
					weiboCount = 0;
					key = keywords+"\t"+expandDate+"-13" +":"+expandDate+"-15";
					data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
												key.getBytes("UTF-8"));
					if (data != null) {
						weiboCount += Integer.parseInt(new String(data, "UTF-8"));
					}
					
					timeLineUnit = new String[2];
					timeLineUnit[0] = expandDate+" 13-15";
					timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
					weiboTimeLine.add(timeLineUnit);
					
					weiboCount = 0;
					key = keywords+"\t"+expandDate+"-16" +":"+expandDate+"-18";
					data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
												key.getBytes("UTF-8"));
					if (data != null) {
						weiboCount += Integer.parseInt(new String(data, "UTF-8"));
					}
					timeLineUnit = new String[2];
					timeLineUnit[0] = expandDate+" 16-18";
					timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
					weiboTimeLine.add(timeLineUnit);
					
					weiboCount = 0;
					key = keywords+"\t"+expandDate+"-19" +":"+expandDate+"-21";
					data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
												key.getBytes("UTF-8"));
					if (data != null) {
						weiboCount += Integer.parseInt(new String(data, "UTF-8"));
					}
					timeLineUnit = new String[2];
					timeLineUnit[0] = expandDate+" 19-21";
					timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
					weiboTimeLine.add(timeLineUnit);
					
					weiboCount = 0;
					key = keywords+"\t"+expandDate+"-22" +":"+expandDate+"-23";
					data = RedisUtil.hget(searchNumMap.getBytes("UTF-8"), 
												key.getBytes("UTF-8"));
					if (data != null) {
						weiboCount += Integer.parseInt(new String(data, "UTF-8"));
					}
					timeLineUnit = new String[2];
					timeLineUnit[0] = expandDate+" 22-23";
					timeLineUnit[1] = ""+(weiboCount>=0?weiboCount:0);
					weiboTimeLine.add(timeLineUnit);
				}
				
			} else {
				log.error("analyze time line(expandSearchWeibo) error:" + returnFlag);
				System.out.println("analyze time line error");
			}
			
		}
		
		Collections.sort(weiboTimeLine, new Comparator<String[]>(){
						public int compare(String[] o1, String[] o2) {
							String[] date1 = o1[0].split(" ");
							String[] date2 = o2[0].split(" ");
							long d1 = TimeConvert.convertString(date1[0]);
							long d2 = TimeConvert.convertString(date2[0]);
							if (d1 > d2) {return 1;}
							else if (d1 < d2) {return -1;}
							else {
								Integer h1 = Integer.parseInt(date1[1].substring(date1[1].lastIndexOf('-') + 1));
								Integer h2 = Integer.parseInt(date2[1].substring(date2[1].lastIndexOf('-') + 1));
								return h1.compareTo(h2);
							}
						}});
		
		
//		System.out.println("weiboTimeLine size : "+weiboTimeLine.size());
//		for (String[] units:weiboTimeLine) {
//			System.out.println(units[0] + ":" + units[1]+" | ");
//		}
		
		int flag = -1;
		for (int i = 0; i < weiboTimeLine.size(); ++i) {
			if (Integer.parseInt(weiboTimeLine.get(i)[1]) < 1) {
				flag = i;
			} else {
				break;
			}
		}
		
		if (flag > 0) {
			weiboTimeLine = weiboTimeLine.subList(flag, weiboTimeLine.size());
		}
		
		flag = -1;
		for (int i = 0; i < weiboTimeLine.size(); ++i) {
			if (Integer.parseInt(weiboTimeLine.get(i)[1]) > 0) {
				flag = i;
			}
		}
		
		if (flag == -1) {
			weiboTimeLine.clear();
		} else if (flag < weiboTimeLine.size() - 1) {
			weiboTimeLine = weiboTimeLine.subList(0, flag+1);
		}
		
//		System.out.println("weiboTimeLine size : "+weiboTimeLine.size());
//		for (String[] units:weiboTimeLine) {
//			System.out.println(units[0] + ":" + units[1]+" | ");
//		}
		
		if (weiboTimeLine.size() > 0) {
//			reducedWeiboTimeLine = reduce(weiboTimeLine);
			reducedWeiboTimeLine = weiboTimeLine;
			peakLine = detection(reducedWeiboTimeLine);
		} else {
			System.out.println("传播模式所有时间段的微博量都为0");
		}
		//test
//		System.out.println("reducedWeiboTimeLine size : "+reducedWeiboTimeLine.size());
//		for (String[] units:reducedWeiboTimeLine) {
//			System.out.println(units[0] + ":" + units[1]+" | ");
//		}
		System.out.println("analyze time line success");
		return "success";
	}
	
	public List<String[]> detection(List<String[]> weiboTimeLine) {
		List<String[]> peak = new ArrayList<String[]>();
		try {
			int count = weiboTimeLine.size();
			List<Double> valueList = new ArrayList<Double>();
			for (String[] term : weiboTimeLine) {
				valueList.add(Double.parseDouble(term[1]));
			}
			double medium = medium(valueList);
			double std = std(valueList);
			double line = medium + std;

			double cur = 0;
			for (int i = 0; i < count; i++) {
//				cur = Integer.parseInt(weiboTimeLine.get(i)[1]);
				cur = Double.parseDouble(weiboTimeLine.get(i)[1]);
				if (cur > line) {	
					peak.add(weiboTimeLine.get(i));
				} else {
					peak.add(new String[]{weiboTimeLine.get(i)[0], null});
				}
			}

			return peak;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Peak detection error.");
			return peak;
		}
	}
	
	public List<String[]> reduce(List<String[]> weiboTimeLine){
		//init the baseline
		double w = 0.5;
		initBase(baseMap);
		
		//List to Map
		double value = 0;
		Map<String, Double> map = new HashMap<String, Double>();
		for(String[] term : weiboTimeLine){
			value = Double.parseDouble(term[1]);
			map.put(term[0], value);
		}
		
		//nomarlize
		double max = Collections.max(map.values());
		double min = Collections.min(map.values());
		Map<String, Double> normalMap = new HashMap<String, Double>();
		if (max != min) {
			for(String k : map.keySet()){
				normalMap.put(k, (map.get(k) - min)/(max - min));
			}
		} else {
			for(String k : map.keySet()){
				normalMap.put(k, 1.0);
			}
		}
		
		//reduce
		List<String[]> timeLine = new ArrayList<String[]>();
		for(String[] term : weiboTimeLine){
			int hour = Integer.parseInt(term[0].substring(term[0].lastIndexOf('-') + 1)) - 1;
			
			if(!baseMap.containsKey(hour))
				;
			else{
				String[] unit = {term[0], "" + (normalMap.get(term[0]) - w * baseMap.get(hour))};
				timeLine.add(unit);
			}
		}
		
		
		return timeLine;
	}
	
	public int oddPeakNum(List<String[]> weiboTimeLine){
		int sum = 0;
		for(String[] term : weiboTimeLine){
			int hour = Integer.parseInt(term[0].substring(term[0].lastIndexOf('-') + 1)) - 1;
			if(hour == 2 || hour == 6)
				sum++;
		}
		return sum;
	}

	double medium(List<Double> input){
		Collections.sort(input);
		int size = input.size();
		if(size % 2 == 0)
			return input.get(size / 2);
		else
			return input.get((size - 1)/2);
	}
	
	double std(List<Double> input){
		double sum = 0;
		for(double v : input)
			sum += v;
		double avg = sum / input.size();
		sum = 0;
		for(double v : input)
			sum += Math.pow(v - avg, 2);
		double std = Math.sqrt(sum / input.size());
		return std;
	}
	
	void initBase(Map<Integer, Double> baseMap){

		baseMap.put(2, 0.177569871630357);
		baseMap.put(6, 0.0);
		baseMap.put(10, 0.760499476192274);
		baseMap.put(14, 0.869952233498471);
		baseMap.put(18, 0.882773989098361);
		baseMap.put(22, 1.0);
	}
	
	public static void main(String[] args) throws NumberFormatException, IOException {
		List<String[]> input = new ArrayList<String[]>();
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream("C:/Users/shaoj_000/Documents/Visual Studio 2012/Projects/Course/TrendView/bin/Debug/old.txt"),
				"utf-8"));
		String line;
		String[] str, timeStr;
		while((line = reader.readLine())!=null){
			str = line.split("\t");
			String[] unit = new String[2];
			timeStr = str[0].split("-");
			int hour = Integer.parseInt( str[0].substring(str[0].lastIndexOf('-') + 1));
			unit[0] = timeStr[1] + "-" + timeStr[2] + " " + (hour==0?20:(hour -4)) + "-" + hour;
			unit[1] = str[1];
			input.add(unit);
		}
		reader.close();
		
		
		ForwardSearchService fss = new ForwardSearchService("", 0, 0);
		List<String[]> output = fss.reduce(input);
		for(String[] term : output){
			System.out.println(term[0] + "\t" + term[1]);
		}

	}
	
	public List<String[]> detection_old(List<String[]> weiboTimeLine) {
		List<String[]> peak = new ArrayList<String[]>();
		try {
			int count = weiboTimeLine.size();
			double w = 0.5;
			int sum = 0;
			for (String[] term : weiboTimeLine) {
				sum += Integer.parseInt(term[1]);
			}
			double average = sum / count;
			sum = 0;
			for (String[] term : weiboTimeLine) {
				sum += Math.abs(Integer.parseInt(term[1]) - average);
			}
			double std = sum / count;
			double line = average + w * std;

			int pre = 0, cur = 0, suf = 0;
			for (int i = 0; i < count; i++) {
				if (i == 0) {
					pre = 0;
					suf = Integer.parseInt(weiboTimeLine.get(i + 1)[1]);
				} else if (i == count - 1) {
					pre = Integer.parseInt(weiboTimeLine.get(i - 1)[1]);
					suf = 0;
				}else{
					suf = Integer.parseInt(weiboTimeLine.get(i + 1)[1]);
					pre = Integer.parseInt(weiboTimeLine.get(i - 1)[1]);
				}
				cur = Integer.parseInt(weiboTimeLine.get(i)[1]);
				if (cur > line && cur > pre && cur >= suf) {
				//if (cur > line) {	
					peak.add(weiboTimeLine.get(i));
				} else {
					peak.add(new String[]{weiboTimeLine.get(i)[0], null});
				}
			}

			return peak;
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Peak detection error.");
			return peak;
		}
	}


}
