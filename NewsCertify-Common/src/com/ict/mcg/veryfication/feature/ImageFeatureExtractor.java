package com.ict.mcg.veryfication.feature;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.imageio.ImageIO;

import com.ict.mcg.gather.entity.WeiboEntity;

public class ImageFeatureExtractor {

	private ArrayList<WeiboEntity> welist;
	private int concurrentLimit = 15;
	private String imgPath = "";
	private String gistPath = "";
	
	public ImageFeatureExtractor(ArrayList<WeiboEntity> welist) {
		this.welist = welist;
	}
	
	public List<String> extractJPEGImageUrl() {
		Set<String> urlSet = new HashSet<String>();
		for (WeiboEntity we:welist) {
			for (String picUrl:we.getPiclist()) {
				if (picUrl.endsWith(".jpg")) {
					urlSet.add(picUrl);
				}
			}
		}
		
		return new ArrayList<String>(urlSet);
	}
	
	public List<String> downloadImgAndExtractGistByBatch(List<String> urlList, String imgDir, String gistDir) {
		String url = null;
		String picName = null;
		String gistName = null;
		List<String> gistList = new ArrayList<String>();
		
		ExecutorService pool = Executors.newCachedThreadPool();
		List<Future<Integer>> fuList = new ArrayList<Future<Integer>>();
		
		for (int i = 0; i < urlList.size(); i = i+concurrentLimit) {
			int concurrentCnt = Math.min(concurrentLimit, urlList.size()-i);
			
			List<String> imgList = new ArrayList<String>();
			CountDownLatch countDown = new CountDownLatch(concurrentCnt);
			for (int j = 0; j < concurrentCnt; ++j) {
				url = urlList.get(i+j);
				picName = imgDir.concat(url.substring(urlList.lastIndexOf("/")+1));
				gistName = picName.substring(0, picName.lastIndexOf(".")).concat(".gist");
				
				if (!new File(gistDir.concat(gistName)).exists()) {
					new Thread(new PicDownloader(url, picName, countDown)).start();
					imgList.add(picName);
					gistList.add(gistName);
				}
			}
			
			try {
				countDown.await();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			fuList.add(pool.submit(new GistExtractor(imgDir,"",imgList,gistDir,"")));
		}
		
		for (Future<Integer> fu:fuList) {
			try {
				int exVal = fu.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		return gistList;
		
	}
	
	/*public List<String> clearNoisyImage(String dir) {
		List<String> imgList = new ArrayList<String>();
		
		File dirFile = new File(dir);
		if (dirFile.isDirectory()) {
			String[] files = dirFile.list();
			
			for (String file:files) {
				File picFile = new File(dir.concat(file));
			    try {
					BufferedImage sourceImg =ImageIO.read(new FileInputStream(picFile));
					//去掉方形的图片
					if (sourceImg.getWidth() == sourceImg.getHeight()) {
						//需要删除么？
						//picFile.delete();
					} else {
						imgList.add(file);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}
		
		return imgList;
	}*/
	
	public void extractGistFeature() {
		List<String> urlList = extractJPEGImageUrl();
		List<String> gistList = downloadImgAndExtractGistByBatch(urlList, imgPath, gistPath);
		
		
	}
}
