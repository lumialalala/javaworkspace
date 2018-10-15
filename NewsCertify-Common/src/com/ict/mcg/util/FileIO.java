package com.ict.mcg.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.json.JSONObject;

import com.ict.mcg.gather.entity.WeiboEntity;


/**
 * IO operation of file
 * 
 * @author JZW
 * 
 */
public class FileIO {
	/**
	 * 获取文件读写的目录
	 * @return
	 */
	public static String getFilePath(){
		return "file/";
	}
	public static String getResourcePath(){
		return "/resources/";
	}
	/**
	 * 获取SVM训练目录
	 * @return
	 */
	public static String getSVMPath(){
		return "file/svm/";
	}
	
	
	public static String getLDAPath(){
		return "file/lda/";
	}
	
	public static void writeWeibo(String filepath, ArrayList<WeiboEntity> wel) {
		if (wel == null || wel.size() == 0)
			return;
		File outfile = new File(filepath);
		try {
			Writer writer = new FileWriter(outfile, true);
			for (WeiboEntity we : wel) {
				writer.append(we.toString());
			}
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static ArrayList<WeiboEntity> readWeibo(String file) {
		ArrayList<WeiboEntity> weibolist = new ArrayList<WeiboEntity>();
		File infile = new File(file);
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(infile));
			String line = "";
			while ((line = reader.readLine()) != null) {
				StringTokenizer tk = new StringTokenizer(line, "|");
				String mid = tk.nextToken();
				String name = tk.nextToken();
				String url = tk.nextToken();
				String userurl = tk.nextToken();
				String longtime = tk.nextToken();
				String forward = tk.nextToken();
				String comment = tk.nextToken();

				String content = reader.readLine();

				WeiboEntity w = new WeiboEntity(mid, url, userurl, name,
						longtime, forward, comment, content);
				weibolist.add(w);
			}
			reader.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return weibolist;
	}

	public static void writerForward(List<String[]> forwardlist, String filepath) {
		File outfile = new File(filepath);
		if (forwardlist == null || forwardlist.size() == 0)
			return;
		try {
			Writer writer = new FileWriter(outfile, true);
			for (String[] str : forwardlist) {
				String content = "";
				for (int i = 0; i < 7; i++) {
					if (i == 4) {
						content = str[i];
						continue;
					}
					writer.append(str[i]);
					if (i != 6)
						writer.append("|");
				}
				writer.append("\r\n" + content + "\r\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
	
	public static String writeJSON(JSONObject json){
		long now = System.currentTimeMillis();
		String filename = "" + now + "_" + json.size() + ".json";

		String u = URLDecoder.decode(Thread.currentThread()
				.getContextClassLoader().getResource("").toString());
		u = u.substring(6);
		String path = u.substring(0, u.length() - 16) + "json/" + filename; // 拼接gexf文件地址
		String os = System.getProperty("os.name");
		if(os.contains("win")||os.contains("Win")){
			;
		}else{
			path = "/"+path;
		}
		
		File outfile = new File(path);
		try {
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(outfile), "utf-8"));			
			writer.write(json.toString());
			writer.flush();
			writer.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return filename;		
	}

	public static String getKV(String key, String value) {
		value = value.replaceAll("\"", "”");
		String s = "\"" + key + "\"" + ":" + "\"" + value + "\"" + "\r\n";
		return s;
	}

	public static ArrayList<String> getStopWords() {
		ArrayList<String> stopWords = new ArrayList<String>();
		String stopWordsFile = getLDAPath().concat("stopWords.txt");
		try {
			BufferedReader reader = new BufferedReader(new FileReader(stopWordsFile));
			String line = "";
			while ((line = reader.readLine()) != null) {
				stopWords.add(line.trim());
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stopWords;
	}
	
	public static ArrayList<WeiboEntity> readWeibo(String weiboFile, String CharacterEncoding) {
		ArrayList<WeiboEntity> weibolist = new ArrayList<WeiboEntity>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(weiboFile), CharacterEncoding));
			while (br.ready()) {
				String line = br.readLine();
				StringTokenizer tk = new StringTokenizer(line, "|");
				if (tk.countTokens() < 15) {					
					continue;
				} else {
					String mid = tk.nextToken();
					String name = tk.nextToken();
					String url = tk.nextToken();
//					String userurl = "";
					String userurl = tk.nextToken();
					String longtime = tk.nextToken();
					String isOrigin = tk.nextToken();
					String forward = tk.nextToken();
					String comment = tk.nextToken();
					
					String praise = tk.nextToken();
					String uid = tk.nextToken();
					String userCertify = tk.nextToken();
					String userFanCount = tk.nextToken();
					String userFollowCount = tk.nextToken();
					String userWeiboCount = tk.nextToken();
					String platform = tk.nextToken();
					
					String content = null;
					ArrayList<String> imglist = new ArrayList<String>();
					if (br.ready()) {
						String urllist = br.readLine();
						
						StringTokenizer tk2 = new StringTokenizer(urllist, "|");
						while(tk2.hasMoreTokens()){
							imglist.add(tk2.nextToken());
						}
						if (imglist.size() > 0 && imglist.get(imglist.size()-1).equals("null")) {
							imglist.remove(imglist.size()-1);
						}
						content = br.readLine();
					}
					WeiboEntity w = new WeiboEntity(mid, url, userurl, name,
							longtime, forward, comment, content);
					w.setPraise(praise);
					w.setSourcePlatform(platform);
					w.setOrigin(Boolean.parseBoolean(isOrigin));
					w.setUserCertify(Integer.parseInt(userCertify));
					w.setUserFanCount(userFanCount);
					w.setUserFollowCount(userFollowCount);
					w.setUserWeiboCount(userWeiboCount);
					w.setPiclist(imglist);
					w.setUserId(uid);
					weibolist.add(w);
				}
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return weibolist;
	}
	
	public static void main(String args[]) {
		// readWeibo("孙杨无证驾驶");
		// generateJson("孙杨无证驾驶", "孙杨无证驾驶.red2");
//		ArrayList<WeiboEntity> we = readWeibo("F:/WeiboProject/RumorData/为中暑清洁工撑伞_2013-08-01-2013-08-02.txt");
//		System.out.println("##################################");
//		for(WeiboEntity w:we){
//			String c = w.getContent();			
//			System.out.println(c);
//		}
		System.out.println(FileIO.getFilePath());
	}

	

}
