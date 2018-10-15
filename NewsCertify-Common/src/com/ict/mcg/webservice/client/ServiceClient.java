package com.ict.mcg.webservice.client;


import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;


import com.ict.mcg.util.ParamUtil;
import com.ict.mcg.webservice.service.GatherWebService;

public class ServiceClient {
	//ParamUtil.WEB_SERVICE_ADDRESS[idx]
	//其中idx为0表示离线分析的采集服务器，1对应的是在线分析的采集服务器
	
//	private static int index = 0;
	private static JaxWsProxyFactoryBean factory;
	private static JaxWsProxyFactoryBean getFactoryInstance(int idx) {
		
		if (factory == null) {
			factory = new JaxWsProxyFactoryBean();
			factory.setServiceClass(GatherWebService.class);
			//目前只有一个地址
			factory.setAddress(ParamUtil.WEB_SERVICE_ADDRESS[idx] + "/gatherWebService");//位置
		}
		
		return factory;
	}
	
	public static GatherWebService getServiceInstance(){
		int index = 0;
		if (Thread.currentThread().getName().contains("http")) {
			index = 1;
		}
		return getServiceInstance(index);
	}
	
	public static GatherWebService getServiceInstance(int idx) {
		System.out.println("NAME:" + Thread.currentThread().getName() + " index:" + idx);
		System.out.println(ParamUtil.WEB_SERVICE_ADDRESS[idx] + "/gatherWebService");
		if(idx>=ParamUtil.WEB_SERVICE_ADDRESS.length) idx = ParamUtil.WEB_SERVICE_ADDRESS.length-1;
		JaxWsProxyFactoryBean factory = getFactoryInstance(idx);
		
		factory.setAddress(ParamUtil.WEB_SERVICE_ADDRESS[idx] + "/gatherWebService");
//		index = (index+1) % ParamUtil.WEB_SERVICE_ADDRESS.length;
		
		GatherWebService service = (GatherWebService)factory.create();
		
		Client proxy = ClientProxy.getClient(service);
	    HTTPConduit conduit = (HTTPConduit) proxy.getConduit();
	    HTTPClientPolicy policy = new HTTPClientPolicy();
	    // time updated on June 1st,2018
	    // 采集器需要更久的容忍时间
	    policy.setConnectionTimeout(50000); //连接超时时间
	    policy.setReceiveTimeout(900000);//请求超时时间.
	    conduit.setClient(policy);
	    return service;
	}
	
	public static void main(String[] args) {
		getServiceInstance();
		
	}
	
}
