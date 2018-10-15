package com.ict.mcg.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShortUrlExpander {
	
	public static String convertShortUrlToLong(String shortUrl) throws IOException {
		URL url = new URL(shortUrl);
		HttpURLConnection httpconn = (HttpURLConnection) (url.openConnection());
		httpconn.setReadTimeout(5000);
		httpconn.setConnectTimeout(5000);
		httpconn.setRequestMethod("GET");  
		httpconn.setInstanceFollowRedirects(false);
		httpconn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpconn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		httpconn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8");
		httpconn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36");
		httpconn.connect();
		
		return httpconn.getHeaderField("Location");
	}
	
	public static void main(String[] args) {
		String url = "http://t.cn/R5Ps33Q";
		String content = "ahaha";
		Pattern pattern = Pattern
				.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
		Matcher matcher = pattern.matcher(content);
		if (matcher.find()) {
			url = matcher.group().trim();
			System.out.println(url);
		}
//		try {
//			System.out.println(convertShortUrlToLong(url));
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

}
