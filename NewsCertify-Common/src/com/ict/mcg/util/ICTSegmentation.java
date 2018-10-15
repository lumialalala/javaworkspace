package com.ict.mcg.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.ict.mcg.processs.ICTAnalyzer;

/**
 * 
  * Class Name ：ICTSegmentation
  * Class Description ：
  * Creation Time ：2014-1-16 上午11:02:17
  *
 */
public class ICTSegmentation {
	public final int POS_YES = 1;	//返回结果含有词性分类 
	public final int POS_NO = 0;		 //返回结果不含词性分类
	

	public ArrayList<String> splitStrings(String str, int postTag){
		
		return ICTAnalyzer.analyzeParagraph(str, postTag);
	}
	/**
	 * remove duplicate elements
	 * @param arlList
	 */
	 public void removeDuplicate(ArrayList<String> arlList)
	  {
	   HashSet<String> h = new HashSet<String>(arlList);
	   arlList.clear();
	   arlList.addAll(h);
	  }
	 
	 /**
	  * remove word by word's attribute,  only save words that is nouns and verbs
	  * @param wordList
	  * @return
	  */
	 public  ArrayList<String> removeByWordAttribute(ArrayList<String> wordList) {
		 ArrayList<String> removedWord = new ArrayList<String>();
		 
		for (String string : wordList) {
			if (string!=null&&string.length()!=0) {
				String[] tmpAry = string.split("\\/");
				if (tmpAry.length<=1) {
					continue;
				}
				String attri = tmpAry[1];
				if (tmpAry[0].length()<=1) {
					continue;
				}
				if (attri==null||attri.length()==0) {
					continue;
				}
				if (attri.charAt(0)=='n'||attri.charAt(0)=='v'||attri.charAt(0)=='x') {			
					if (!attri.equals("ns")) {	
						removedWord.add(tmpAry[0].toLowerCase());
					}
					
				}
			}
		}
		return removedWord;
	}
	 /**
	  * sort hashmap , get sorted list from hashmap
	  * @param tmplist
	  * @return
	  */
	 public List<Map.Entry<String, Integer>> getSortList(HashMap<String, Integer> tmplist){
			Set<Map.Entry<String, Integer>> set = tmplist.entrySet();
			List<Map.Entry<String, Integer>> sortList = new ArrayList<Map.Entry<String, Integer>>(set);  
			Collections.sort(sortList, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
					return (o2.getValue() - o1.getValue()); 
				}
			}); 
			return sortList;
		}
	 /**
	  * test main function
	  * @param argc
	  */
	public static void main(String[] argc) {
		ICTSegmentation segmentation = new ICTSegmentation();
//		String str = "#腿粗的人智商高#智商好高啊你！是吧！爱迪小婊砸";
		String str = "对于中国庞大的职业教育力量来说也是微不足道，荣兰祥抛出国外势力通过“黑”蓝翔来对于中国庞大的职业教育力量来说也是微不足道，荣兰祥抛出国外势力通过“黑”蓝翔来";
		ArrayList<String> strings = segmentation.splitStrings(str, 1);
		for (String string : strings) {
			System.out.println(string);
		}
		System.out.println("#################1");
		segmentation.removeDuplicate(strings);
		for (String string : strings) {
			System.out.println(string);
		}
		System.out.println("#################2");
		strings = segmentation.removeByWordAttribute(strings);
		segmentation.removeDuplicate(strings);
		for (String string : strings) {
			System.out.println(string);
		}
		
		ArrayList<Integer> att = new ArrayList<Integer>();
		att.add(2);
		att.add(3);
		att.add(3);
		int color1 =230;
		
		System.out.println(Integer.toHexString(color1));
	}
	public List<String> saveWordHasAttribute(List<String> wordList) {
		 ArrayList<String> savedWord = new ArrayList<String>();
		 for (String string : wordList) {
				if (string!=null&&string.length()!=0) {
					String[] tmpAry = string.split("\\/");
					if (tmpAry.length<=1) {
						continue;
					}
					String attri = tmpAry[1];
					if (tmpAry[0].length()<=1) {
						continue;
					}
					if (attri==null||attri.length()==0) {
						continue;
					}
					if (attri.charAt(0)== 'm') {						
						savedWord.add(tmpAry[0].toLowerCase());
					}					
				}
			}
		 return savedWord;
		}	 
	public List<String> saveEmailOrUrl(List<String> wordList) {
		ArrayList<String> savedWord = new ArrayList<String>();
		for (String string : wordList) {
			if (string!=null&&string.length()!=0) {
				String[] tmpAry = string.split("\\/");
				if (tmpAry.length<=1) {
					continue;
				}
				String attri = tmpAry[tmpAry.length-1];
				if (tmpAry[0].length()<=1) {
					continue;
				}
				if (attri==null||attri.length()==0) {
					continue;
				}
				if (attri.equals("url")) {						
					savedWord.add(string.substring(0, string.length()-4));
				}else if(attri.equals("email")){
					savedWord.add(string.substring(0, string.length()-6));
				}				
			}
		}
		return savedWord;
	}
	/**
	 * get tokenize word list without remove word
	 * @param weiboContentList
	 * @return
	 */
	public List<List<String>> getSentencesList(List<String> weiboContentList){
		List<List<String>> sentencesList = new ArrayList<List<String>>();	
		for (String oneWeibo : weiboContentList) {
			List<String> oneSentence = splitStrings(oneWeibo, this.POS_NO);
			sentencesList.add(oneSentence);
		}
		return sentencesList;
	} 
	public HashMap<String, Integer> filterWords(List<Map.Entry<String, Integer>> wordList) {
		HashMap<String, Integer> removedWordCloud = new HashMap<String, Integer>();
		// 1. remove word that has @ ,and add word max to 60
		Iterator<Entry<String, Integer>> iter = wordList.iterator();
		int cnt = 0;
		while(iter.hasNext()){
			Map.Entry<String, Integer> wordValue = iter.next();
			String word = wordValue.getKey();
			//2.remove unreadable code
			if (word.contains("�")) {
				continue;
			}
			if (!word.contains("@")) {				
				int value = wordValue.getValue();
				removedWordCloud.put(word, value);
				if (++cnt>60) {
					break;
				}				
			}
		}
		
		return removedWordCloud;
	}
}

