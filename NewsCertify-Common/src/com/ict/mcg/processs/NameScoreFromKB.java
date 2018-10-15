package com.ict.mcg.processs;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.HashSet;

import com.ict.mcg.util.FileIO;

/*
 * author lsj
 * 利用知识库的先验知识来处理人名异常
 */
public class NameScoreFromKB {
	private static boolean IS_INIT_NAMESCORE = false;
	private static HashMap<String, Double> nameScoreMap = new HashMap<String, Double>();
	private static HashSet<String> stopname = new HashSet<String>();
	private static String nameScoreFile = FileIO.getFilePath() + "name_weight_kb.txt";
	private static String stopnameFile = FileIO.getFilePath() + "stopname.txt";
	
	//打成jar包中的resources
	private static String nameScoreFile_resource = FileIO.getResourcePath() + "name_weight_kb.txt";
	private static String stopnameFile_resource = FileIO.getResourcePath() + "stopname.txt";
	
	public static void init(String nameScoreFile, String stopnameFile) {
		NameScoreFromKB.nameScoreFile = nameScoreFile;
		NameScoreFromKB.stopnameFile = stopnameFile;
		nameScoreFile_resource = "";
		stopnameFile_resource = "";
		initNameScore();
	}

	// 引入知识库人名权重
	public static HashMap<String, Integer> resetNameScore(HashMap<String, Integer> locmap) {
		try {
			initNameScore();
			HashMap<String, Integer> hash = new HashMap<String, Integer>();
			Integer score;
			for (String word : locmap.keySet()) {
				if(stopname.contains(word))
					continue;
				
				if (nameScoreMap.containsKey(word)) {
					score = (int) (locmap.get(word) * nameScoreMap.get(word));
					if (score == 0)
						continue;
					hash.put(word, score);
				} else
					hash.put(word, locmap.get(word));
			}
			return hash;
		} catch (Exception e) {
			System.out.println("error: resetNameSocre 方法出错");
			return locmap;
		}
	}

	private static void initNameScore() {
		if (IS_INIT_NAMESCORE)
			return;
		try {
			InputStream is = NameScoreFromKB.class.getResourceAsStream(nameScoreFile_resource);
			if (null == is) {
				is = new FileInputStream(nameScoreFile);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line, word;
			Double score;
			String[] str;
			while ((line = reader.readLine()) != null) {
				str = line.split("\t");
				word = str[0];
				score = Double.parseDouble(str[3]);
				if (score == 1)
					continue;
				nameScoreMap.put(word, score);
			}
			reader.close();
			is.close();
			
			is = NameScoreFromKB.class.getResourceAsStream(stopnameFile_resource);
			if (null == is) {
				is = new FileInputStream(stopnameFile);
			}
			reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
			while ((line = reader.readLine()) != null) {
				stopname.add(line.trim());
			}
			reader.close();
			IS_INIT_NAMESCORE = true;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws IOException {
		for (int i = 0; i < 10; i++) {
			HashMap<String, Integer> map = new HashMap<String, Integer>();
			map.put("周三", 100);
			map.put("习近平", 10);
			map.put("王增", 10);
			map.put("吕绍杰", 10);
			map = resetNameScore(map);
			for (String name : map.keySet()) {
				System.out.println(name + "\t" + map.get(name));
			}
			System.out.println("***********************");
		}
	}
}
