package com.ict.mcg.forward;
/**
 * @author WuBo
 */
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ict.mcg.util.RunTime;


public class AnalysisWordCloud {

//	private List<String> mListWord;


	
	/*public AnalysisWordCloud( List<String[]> forwardrelation, CountDownLatch latch)
	{

		mListForwardRelation = Collections.synchronizedList(new ArrayList<String[]>());
		mListForwardRelation = forwardrelation;
		mListWord = Collections.synchronizedList(new LinkedList<String>());
		setmArrResult(new JSONArray());
		this.mCountDown = latch;
	}*/

//	public void run()
	public JSONArray calculateWordCloud(List<String[]> mListForwardRelation)
	{
		JSONArray mArrResult = new JSONArray();
		List<String> mListWord = new LinkedList<String>();
		RunTime time = new RunTime("WordCloud Time");
		time.GetStartTime();
		if(mListForwardRelation==null||mListForwardRelation.size()==0)
		{
			System.out.println(">>>>>" + "没有获得可以显示的WordCloud结果。" );
			return null;
		}
		WeiboTextAnalysis analysis = new WeiboTextAnalysis();
//		List<Integer> ListForwardCount = Collections.synchronizedList(new ArrayList<Integer>());
		Map<String, Integer> wordfrequencymap = new HashMap<String, Integer>();
		for(int index = 0;index<mListForwardRelation.size();index++)
		{
			try {
				//分词模块
				List<String> res = analysis.SimpleAnalysis(mListForwardRelation.get(index)[4]);
				if(res!=null && res.size()!=0){
					//去重，计算的词频为df
					HashSet<String> WordSet =new HashSet<String>(res);
					for (String str:WordSet) {
						if (!wordfrequencymap.containsKey(str)) {
							wordfrequencymap.put(str, 1);
						} else {
							wordfrequencymap.put(str, wordfrequencymap.get(str) + 1);
						}
					}
//						HashSet<String> WordSet =new HashSet<String>(res);
//						mListWord.addAll(WordSet);
////						System.out.println("分词前: "+mListForwardRelation.get(index)[4]+"\n"+"分词后: "+WordSet);
//						for(int i=0;i<WordSet.size();i++)
//							ListForwardCount.add(Integer.valueOf(mListForwardRelation.get(index)[6]));
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		if(wordfrequencymap==null || wordfrequencymap.size()==0){
			JSONObject obj = new JSONObject();
			obj.put("text", "无");
			obj.put("weight", 3);
//			if(i<linktopn){
//				obj.put("link", "www.baidu.com");//添加链接
//			}
			mArrResult.add(obj);
			System.out.println("没有需要显示的词");
			return mArrResult;
		}
//		System.out.println("ListForwardCount: "+ListForwardCount);

		//计算词频
//		Map<String, Integer> wordfrequencymap = new HashMap<String, Integer>();
//		wordfrequencymap = getWordFrequencyList(mListWord,ListForwardCount);
		
		//排序
		List<Map.Entry<String, Integer>> MapList = new ArrayList<Map.Entry<String, Integer>>(wordfrequencymap.entrySet());
		Collections.sort(MapList, new Comparator<Map.Entry<String, Integer>>(){
			public int compare(Map.Entry<String,Integer > o1, Map.Entry<String, Integer> o2){
				return (o2.getValue() - o1.getValue());
			}
		});
//		System.out.println("word and frequency sorted:"+MapList);
		
		//记录前35个词的结果
		int maxcount=35;
		if(MapList.size()<maxcount)
			maxcount=MapList.size();
		wordfrequencymap.clear();
		for(int i=0;i<maxcount;i++){ 
			wordfrequencymap.put(MapList.get(i).getKey(), MapList.get(i).getValue()); 
		}  
	        
	
		//Rank方法
		//value值序列去重
		HashSet<Integer> FeqValueSet =new HashSet<Integer>(wordfrequencymap.values());
//		List<Integer> mListFeqValue = Collections.synchronizedList(new ArrayList<Integer>());
		List<Integer> mListFeqValue = new ArrayList<Integer>();
		mListFeqValue.addAll(FeqValueSet);
		
		int n=10;
		if(mListFeqValue.size()==0)
		{
			JSONObject obj = new JSONObject();
			obj.put("text", "无");
			obj.put("weight", 3);
			mArrResult.add(obj);
			System.out.println("没有高质量的词");
		}
		else{
			if(mListFeqValue.size()<n)
				n=mListFeqValue.size();
			int k = mListFeqValue.size()/n;
			Integer maxFeqValue = Collections.max(mListFeqValue);
//			System.out.println(" k: "+ k);
			int linktopn=5;
			if (maxcount<linktopn)
				linktopn=maxcount;
			for(int i=0;i<maxcount;i++){  
				Entry<String,Integer > element = MapList.get(i);
				DecimalFormat df = new DecimalFormat("0.0");
				int feqindex=mListFeqValue.indexOf(element.getValue());
//				System.out.print("feqlevel: "+feqindex+", ");
				//排名归一化
//				String weight = df.format((feqindex*0.1/mListFeqValue.size())*6+4);
				
				//排名分n份
//				String weight = df.format((feqindex/k+1)*1.0);
				
				//取log
				String weight = df.format(3+7*logEx(2,element.getValue()+1)/logEx(2,maxFeqValue+1));
				
				//直接
//				String weight = df.format(element.getValue());
				
//				System.out.print(element.getKey()+"="+weight);

//				System.out.print("词云分词结果："+element.getKey()+"="+element.getValue()+" "+maxFeqValue + " "+ logEx(2,element.getValue()+1)+", ");
				
				JSONObject obj = new JSONObject();
				obj.put("text", element.getKey());
				obj.put("weight", weight);
//				if(i<linktopn){
//					obj.put("link", "www.baidu.com");//添加链接
//				}
				mArrResult.add(obj);
			}  		
//			System.out.println("word weight: "+mArrResult);
		}
		time.GetEndTime();
		double s = time.ComputeRunTime();
		System.out.println("分析之词云模块 总时间：" + s + "秒");

		return mArrResult;
	}
	
	public Map<String, Integer> getWordFrequencyList(List<String> WordList, List<Integer> ForwardCountList) {
		Map<String, Integer> WordsFreqList = new HashMap<String, Integer>();		
		for(int index=0;index<WordList.size();index++) {
			String word=WordList.get(index);
			if(!WordsFreqList.containsKey(word))
			{
				int feq=Collections.frequency(WordList, word);
				int ffcount=ForwardCountList.get(index);//所在微博的二次转发数
				WordsFreqList.put(word, (int) (feq));
//				WordsFreqList.put(word, (int) (index));
			}
		}
		return WordsFreqList;
	}
	

	public double logEx(double base, double value) {
		return Math.log(value)/Math.log(base);
	}

//	public static Map<String, Integer> getWordsListwithFrequent(List<String> strList) {
//		System.out.println("aaaaaa: "+strList);
//		Map<String, Integer> wordsListFreq = new HashMap<String, Integer>();
//		Iterator<String> str_it = strList.iterator();
//		while (str_it.hasNext()) {
//			addWord(wordsListFreq, str_it.next().toString());
//		}
//		System.out.println("aaa: "+wordsListFreq);
//		return wordsListFreq;
//	}
//	
//	public static void addWord(Map<String, Integer> map, String word) {
//		if (word.length() > 0) {
//			if (map.containsKey(word)) {
//				Integer value = map.get(word);
//				value += 1;
//				map.put(word, new Integer(value));
//			} else {
//				map.put(word, new Integer(1));
//			}
//		}
//	}

	
//	public static void main(String[] args){
//
//		String [] str={"鹿哥", "肠胃", "哇塞", "揭阳", "身体", "揭阳","机场","机场"};
//		List<String> strList =Collections.synchronizedList(new ArrayList<String>());;
//		for(int i=0;i<str.length;i++)
//			strList.add(str[i]);
//		System.out.println(getWordsListwithFrequent(strList) );
//	}
}

		
		
