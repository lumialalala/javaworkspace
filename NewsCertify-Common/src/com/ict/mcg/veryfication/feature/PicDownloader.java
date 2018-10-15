package com.ict.mcg.veryfication.feature;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.CountDownLatch;

/**
 * 使用URLConnection下载文件或图片并保存到本地。
 * 
 */
public class PicDownloader implements Runnable{
	private String url;
	private String fileName;
	private CountDownLatch countDownLatch;
	
	public PicDownloader(String url, String fileName, CountDownLatch countDownLatch) {
		this.url = url;
		this.fileName = fileName;
		this.countDownLatch = countDownLatch;
	}

	public void run() {
		
		File file = new File(fileName);
		//已经下载过，则不需要再次下载
		if (file.exists()) {
			this.countDownLatch.countDown();
			return;
		}
		
		URL u = null;
		InputStream is = null;
		OutputStream os = null;
		URLConnection con = null;
		
		try {
			u = new URL(url);
			con = u.openConnection();
			con.setConnectTimeout(1000*10);
			con.setReadTimeout(1000*20);
			
			is = con.getInputStream();
			// 1K的数据缓冲
			byte[] bs = new byte[1024];
			// 读取到的数据长度
			int len;
			// 输出的文件流
			os = new FileOutputStream(file);
			// 开始读取
			while ((len = is.read(bs)) != -1) {
				os.write(bs, 0, len);
			}
			os.close();
			is.close();
		} catch (Exception e) {
//			e.printStackTrace();
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
//					e.printStackTrace();
				}
			}
		}
		
		this.countDownLatch.countDown();
	}
}
