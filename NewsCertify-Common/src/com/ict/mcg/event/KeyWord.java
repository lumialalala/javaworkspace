package com.ict.mcg.event;
import java.util.ArrayList;
import java.util.Set;


/**
 * @author WuBo 表示关键词的实体类
 */
public class KeyWord {
	private String word; // 关键词文本
	private float weight; // 权重
	private int type;// 类型
	
	public static final int POS = 1;
	public static final int NEG = -1;
	public static final int NEU = 0;
	
	public KeyWord(String word, float weight, int type) {
		this.word = word;
		this.weight = weight;
		this.type = type;
	}
	
	
	public void setWord(String word) {
		this.word = word;
	}
	public String getWord() {
		return word;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public float getWeight() {
		return weight;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getType() {
		return type;
	}
	
	@Override
	public String toString() {
		String result = word + "|" + weight + "|" + type 
				+ "\r\n";
		return result;
	}
	
	/**
	 * 根据词集合初始化词表（数组）
	 */
	public static KeyWord[] initialKeyWordArr(Set<String> wordset){
		KeyWord[] keywordarr = new KeyWord[wordset.size()];
		int i = 0;
		for(String word:wordset){
			KeyWord keyword = new KeyWord(word, 1, 0);
			keywordarr[i] = keyword;
			i++;
		}
		return keywordarr;
	}
	
	/**
	 * 根据词集合初始化词表（链表）
	 */
	public static ArrayList<KeyWord> initialKeyWordList(Set<String> wordset){
		ArrayList<KeyWord> keywordarr = new ArrayList<KeyWord>();
		for(String word:wordset){
			KeyWord keyword = new KeyWord(word, 1, 0);
			keywordarr.add(keyword);
		}
		return keywordarr;
	}

	
	
}