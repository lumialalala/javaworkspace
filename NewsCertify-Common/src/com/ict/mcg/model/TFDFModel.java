package com.ict.mcg.model;
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

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.WordNode;


/**
 * 
 * ClassName: TFDFModel
 * Function: 
 * date: 2014-5-5 上午09:50:54 
 * @author senochow
 * 修改人：senochow   
 * 修改时间：2014-5-5 上午09:50:54   
 * 修改备注：
 * 修改人：zjq   
 * 修改时间：2014-10-10 下午14:36:54   
 * 修改备注：选取top5关键词时忽略太长的词
 */
public class TFDFModel {

	private List<List<String>> wordsList = new ArrayList<List<String>>(); 
	private String topWordStr = "";
	private List<Double> weiboWeight = new ArrayList<Double>();
	public TFDFModel(ArrayList<ArrayList<WeiboEntity>> clusters){
		this.wordsList = processWeiboEntity(clusters);
		List<Map.Entry<String, Double>> sortedWords = new ArrayList<Entry<String,Double>>();
		sortedWords = tfdfModel(wordsList);
		int keyWordCount = 5;
		for (int i = 0; i < keyWordCount && i < sortedWords.size(); i++) {
			//不要太长的词
			if (sortedWords.get(i).getKey().length() < 10) {
				if (i==0) {
					topWordStr += sortedWords.get(i).getKey();
				}else {
					topWordStr += " "+sortedWords.get(i).getKey();
				}
			} else {
				keyWordCount++ ;
			}
		}

	}
	/**
	 * processing weibo entity data to word list
	 * @param clusters
	 * @return
	 */
	private List<List<String>> processWeiboEntity(ArrayList<ArrayList<WeiboEntity>>clusters){
		List<List<String>> allWord = new ArrayList<List<String>>();
		for (ArrayList<WeiboEntity> oneCluster : clusters) {
			
			for (WeiboEntity oneEntity : oneCluster) {
				ArrayList<WordNode> nodes = oneEntity.getSegs();
				List<String> oneWordList = new ArrayList<String>();
				for (WordNode w : nodes) {
					String str = w.getWord();
					String pos = w.getPos();
					if (pos.length() < 1)
						continue;
					if (str.contains("@")) {
						continue;
					}
					String first = pos.substring(0, 1);
					if (first.equals("n") || first.equals("s") || first.equals("v")
							|| first.equals("a")) {					
						// 保留名词、处所词、动词、形容词
						oneWordList.add(str);
					}
				}
				allWord.add(oneWordList);
				double weight = oneEntity.getHotrate();
				this.weiboWeight.add(weight);
			}
		}				
		return allWord;
	}
	/**
	 * 
	 * @param wordsList
	 * @return
	 */
	private List<Map.Entry<String, Double>> tfdfModel(List<List<String>> wordsList){
		HashMap<String, Integer> wordsTf = new HashMap<String, Integer>();
		HashMap<String, Integer> wordsDf = new HashMap<String, Integer>();
		//1. get tf value and df value
		int i = 0;
		for (List<String> oneWeiboWords : wordsList) {
			int weight = (int)Math.log10(this.weiboWeight.get(i)*10000+1);
//			int weight = 1;
			// 1.1 get tf value
			for (String word : oneWeiboWords) {
				if (wordsTf.containsKey(word)) {
					int val = wordsTf.get(word);
					wordsTf.put(word, val+weight);
				}else {
					wordsTf.put(word, weight);
				}
			}
			// 1.2. get df value 
			HashSet<String> wordSet = new HashSet<String>(oneWeiboWords);
			
			for(String word : wordSet){
				if(wordsDf.containsKey(word)){
					int val = wordsDf.get(word);
					wordsDf.put(word, val+weight);
				}else {			
					wordsDf.put(word, weight);
				}
			}
			i++;
		}
	// 2. get tf-mf value
	HashMap<String, Double> wordsMap = new HashMap<String, Double>();
	Iterator<Entry<String,Integer>> iter = wordsTf.entrySet().iterator();
	while(iter.hasNext()){
		Map.Entry<String, Integer> wordMap = iter.next();
		String word = wordMap.getKey();
		int tfVal = wordMap.getValue();
		int dfVal = wordsDf.get(word);
		
		double mfVal = Math.log((double)(dfVal+1));
		double tfmfVal = tfVal*mfVal*word.length();
		wordsMap.put(word, tfmfVal);
	}
	List<Map.Entry<String, Double>> sortedWords  = getSortList(wordsMap);
	return sortedWords;
	
}
/**
 * sort hashmap , get sorted list from hashmap
 * @param tmplist
 * @return
 */
 public List<Map.Entry<String, Double>> getSortList(HashMap<String, Double> tmplist){
		Set<Map.Entry<String, Double>> set = tmplist.entrySet();
		List<Map.Entry<String, Double>> sortList = new ArrayList<Map.Entry<String, Double>>(  
               set);  
		Collections.sort(sortList, new Comparator<Map.Entry<String, Double>>() {   
		    public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2) {      
		    	if ((o2.getValue() - o1.getValue())>0)  
		            return 1;  
		          else if((o2.getValue() - o1.getValue())==0)  
		            return 0;  
		          else   
		            return -1;  
		        
		    }
		}); 
		return sortList;

 }
	public String getTopWordStr() {
		return topWordStr;
	}
}


