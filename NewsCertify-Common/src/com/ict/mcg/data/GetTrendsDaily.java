package com.ict.mcg.data;

import java.util.ArrayList;
import java.util.List;

import weibo4j.Trend;
import weibo4j.model.Trends;
import weibo4j.model.WeiboException;
import weibo4j.util.WeiboConfig;

public class GetTrendsDaily {
	public static List<String> getCurrDayTrends() {
		List<String> dayTrends = new ArrayList<String>();
		String access_token = WeiboConfig.getValue("access_token");
		//System.out.println(access_token);
		Trend tm = new Trend(access_token);
		try {
			List<Trends> trends = tm.getTrendsDaily();
			for(Trends ts : trends){
				System.out.println(ts.toString());
				//System.out.println(ts.getTrends().length);
				for (weibo4j.model.Trend t : ts.getTrends()) {
					//System.out.println(t.getQuery());
					dayTrends.add(t.getQuery());
				}
			}
		} catch (WeiboException e) {
			e.printStackTrace();
		}
		return dayTrends;
	}
}


