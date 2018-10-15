package com.ict.mcg.webservice.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.jws.soap.SOAPBinding.Style;


@WebService
@SOAPBinding(style=Style.RPC)
public interface GatherWebService {
	String userInfoCrawl(String query, String type); // 用户信息
	String searchWeibo(String query);
	String forwardCrawl(String url);
	String userCrawl(String query);
	String expandSearchWeibo(String query);
	String monitorSearchWeibo(String keywords, int pageCnt);
	String searchWeiboOffline(String query);
	String searchWeiboForExperiment(String query);
	String searchWeiboForCrawl(String query);//雅滋姐采集，by gc
	String singleWeiboForward(String query);  //单微博转发采集
	String crawlSingleWeibo(String url);
	String refuteWeibo(String string);
}
