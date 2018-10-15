package com.ict.mcg.event;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import com.ict.mcg.util.FileIO;



/**
 * 文本的情感倾向
 * 
 * @author WuBo
 * 
 */
public class EmotionAnalyze {
	private static HashSet<String> positive = new HashSet<String>();
	private static HashSet<String> negtive = new HashSet<String>();

	public static final int POS = 1;
	public static final int NEG = -1;
	public static final int NEU = 0;
	
	private ArrayList<String> poswords = new ArrayList<String>();
	private ArrayList<String> negwords = new ArrayList<String>();
	private static String posFile = FileIO.getFilePath() + "positive.txt";
	private static String negFile = FileIO.getFilePath() + "negtive.txt";
	
	private static String posFile_resource = FileIO.getResourcePath() + "positive.txt";
	private static String negFile_resource = FileIO.getResourcePath() + "negtive.txt";
	private static boolean isinitial = false;
	
	public static void init(String posFile, String negFile) {
		EmotionAnalyze.posFile = posFile;
		EmotionAnalyze.negFile = negFile;
		loadFile();
	}
	/**
	 * 从文件载入正负词表
	 */
	private static void loadFile() {
		if (positive.size() == 0) {
			try {
				InputStream is = EmotionAnalyze.class.getResourceAsStream(posFile_resource);
				if (null == is) {
					is = new FileInputStream(posFile);
				}
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"));
				String line = "";
				while ((line = reader.readLine()) != null) {
					positive.add(line);
				}
				reader.close();
			} catch (IOException e) {
				System.out.println("load positive.txt file failed!");
				e.printStackTrace();
			}
		}
		if (negtive.size() == 0) {
			try {
				InputStream is = EmotionAnalyze.class.getResourceAsStream(negFile_resource);
				if (null == is) {
					is = new FileInputStream(negFile);
				}
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, "utf-8"));
				String line = "";
				while ((line = reader.readLine()) != null) {
					negtive.add(line);
				}
				reader.close();
			} catch (IOException e) {
				System.out.println("load negtive.txt file failed!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * 获得词的情感标签
	 */
	public static int getEmotion(String word) {
		if (!isinitial) {
			loadFile();
		}
		if (positive.contains(word))
			return POS;
		else if (negtive.contains(word))
			return NEG;
		else
			return NEU;
	}

	/**
	 * 设置词表情感标签
	 */
	private  ArrayList<KeyWord> setWordsEmotion(ArrayList<KeyWord> wordlist) {
		// 设置词表情感标签
		for (KeyWord keyword : wordlist) {
//			System.out.println(keyword.getWord() + "："
//					+ EmotionAnalyze.getEmotion(keyword.getWord()));
			int res = EmotionAnalyze.getEmotion(keyword.getWord());
			if (res == POS) {
				keyword.setType(POS);
				this.poswords.add(keyword.getWord());
			} else if (res == NEG) {
				keyword.setType(NEG);
				this.negwords.add(keyword.getWord());
			} else if (res == NEU) {
				keyword.setType(NEU);
			}
		}
		return wordlist;
	}

	/**
	 * 根据词表计算情感值(无权重)
	 */
	public static int computeEV0(ArrayList<KeyWord> wordlist) {
		// 计算关键词集合中的情感值
		int pos = 0;
		int neg = 0;
		int neu = 0;
		for (KeyWord keyword : wordlist) {
			int res = keyword.getType();
			if (res == POS) {
				pos++;
			} else if (res == NEG) {
				neg++;
			} else if (res == NEU) {
				neu++;
			}
		}
		if ((pos + neg) == 0) {
			return 0;
		} else{
			int t = (int) ((pos - neg*1.1) * 100 / (pos + neg));
			if(t>100) t = 100;
			if(t<-100) t = -100;
			return t;
		}
	}

	public int computeEmotion(Set<String> words) {
		ArrayList<KeyWord> keywords = KeyWord.initialKeyWordList(words);
		keywords = setWordsEmotion(keywords);
		int emotion = computeEV0(keywords);
		return emotion;
	}
	
	public ArrayList<String> getPos(){
		return this.poswords;
	}
	
	public ArrayList<String> getNeg(){
		return this.negwords;
	}

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		System.out.println(EmotionAnalyze.getEmotion("赞扬"));
		

	}

}
