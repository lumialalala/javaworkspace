/**
 * 
 */
package com.ict.mcg.event;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.WordNode;

/**
 * 提取关键词
 * 
 * @author JZW
 * 
 */
public class KeyWords {
	
	public static HashMap<String, Integer> getEventKeyword(ArrayList<ArrayList<WeiboEntity>> clusters,int k){
		HashMap<String, Integer> wordmap = new HashMap<String, Integer>();
		if(clusters.size()==0)
			return wordmap;
		ArrayList<HashMap<String,Double>> each = getEachKeywords(clusters,k/clusters.size()+10);
		for(HashMap<String,Double> h:each){
			for (Iterator it = h.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String word = (String) entry.getKey();
				double count = (Double) entry.getValue();
				if(wordmap.containsKey(word)){
					int d = wordmap.get(word);
					wordmap.remove(word);
					wordmap.put(word, (int)(d+count)/2);
				}else{
					wordmap.put(word, (int)count);
				}
			}
		}
		
//		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
//				wordmap.entrySet());
//
//		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
//			public int compare(Map.Entry<String, Integer> o1,
//					Map.Entry<String, Integer> o2) {
//				return (o2.getValue().compareTo(o1.getValue()));
//			}
//		});
		
		
		return wordmap;
	}

	public static HashMap<String, Integer> getEventKeyword(
			ArrayList<ArrayList<WeiboEntity>> clusters) {
		HashMap<String, Integer> wordmap = new HashMap<String, Integer>();
		int size = 0;
		for (ArrayList<WeiboEntity> cluster : clusters) {
			HashMap<String, Integer> clusterresult = getClusterKeyword(cluster);
			size += cluster.size();
			for (Iterator it = clusterresult.entrySet().iterator(); it
					.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String w = (String) entry.getKey();
				int c = (Integer) entry.getValue();
				if (wordmap.containsKey(w)) {
					int i = wordmap.get(w);
					i += c;
					wordmap.remove(w);
					wordmap.put(w, i);
				} else {
					wordmap.put(w, c);
				}
			}
		}
		size = size * 14 / 15; // 过滤出现超过2/3的词
		for (Iterator it = wordmap.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String word = (String) entry.getKey();
			int count = (Integer) entry.getValue();
			if (count >= size) {// 过滤最高频词
				it.remove();
			}
		}

		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				wordmap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});

		HashMap<String, Integer> sortmap = new HashMap<String, Integer>();

		int max = 50;
		for (int i = 0; i < list.size(); i++) {
			if (i >= max)
				break;
			sortmap.put(list.get(i).getKey(), list.get(i).getValue());
		}
		return sortmap;
	}

	private static HashMap<String, Integer> getClusterKeyword(
			ArrayList<WeiboEntity> cluster) {
		HashMap<String, Integer> wordset = new HashMap<String, Integer>();
		for (WeiboEntity we : cluster) {
			ArrayList<WordNode> segs = we.getSegs();
			if (segs == null) {
				Partition p = new Partition();
				String content = we.getContent();
				segs = p.part(content);
				if (segs.size() == 0) {
					segs.add(new WordNode(content, "n"));
				}
			}
			HashSet<String> cont = new HashSet<String>();// 每条微博中重复出现的词只统计一次，DF
			for (WordNode w : segs) {
				String str = w.getWord();
				String pos = w.getPos();
				if (pos.length() < 1)
					continue;
				String first = pos.substring(0, 1);
				if (first.equals("n") || first.equals("s") || first.equals("v")
						|| first.equals("a")) {
					// 保留名词、处所词、动词、形容词
					if (cont.contains(str))
						continue;
					cont.add(str);
					if (wordset.containsKey(str)) {
						int i = wordset.get(str);
						i++;
						wordset.remove(str);
						wordset.put(str, i);
					} else {
						wordset.put(str, 1);
					}
				}
			}
		}

		int size = cluster.size();
		for (Iterator it = wordset.entrySet().iterator(); it.hasNext();) {
			Map.Entry entry = (Map.Entry) it.next();
			String word = (String) entry.getKey();
			int count = (Integer) entry.getValue();
			if (size > 1 && count >= size) {
				// 过滤最高频词
				it.remove();
			}
		}

		return wordset;
	}
	
	/**
	 * 获取每个事件各自的关键词
	 * 
	 * @return
	 */
	public static ArrayList<HashMap<String ,Double>> getEachKeywords(
			ArrayList<ArrayList<WeiboEntity>> clusters, int n) {
		ArrayList<HashMap<String,Double>> result = new ArrayList<HashMap<String,Double>>();
		// 计算所有微博中词的tf
		ArrayList<WeiboEntity> all = new ArrayList<WeiboEntity>();
		for (ArrayList<WeiboEntity> wel : clusters) {
			all.addAll(wel);
		}
		HashMap<String,Double> alltf = getKeywordsByTF(all);

		for (int i = 0; i < clusters.size(); i++) {
			HashMap<String, Double> tf = getKeywordsByTF(clusters.get(i));
			HashMap<String, Double> wordweight = new HashMap<String, Double>();
			for (Iterator it = tf.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String word = (String) entry.getKey();
				double t = (Double) entry.getValue();
				
				double r = alltf.get(word) - t; 
				double w ;
				if(r == 0) w = 0;
				else w = t / Math.sqrt(r);
				wordweight.put(word, w);
			}

			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(
					wordweight.entrySet());

			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
				public int compare(Map.Entry<String, Double> o1,
						Map.Entry<String, Double> o2) {
					return (o2.getValue().compareTo(o1.getValue()));
				}
			});
			HashMap<String,Double> keywords = new HashMap<String,Double>();

			for (int j = 0; j < list.size(); j++) {
				if (j >= n)
					break;
				keywords.put(list.get(j).getKey(),list.get(j).getValue());
			}
			result.add(keywords);
		}
		return result;
	}

	/**
	 * 获取每个事件各自的关键词
	 * 
	 * @return
	 */
	public static ArrayList<ArrayList<String>> getSeperateKeywords(
			ArrayList<ArrayList<WeiboEntity>> clusters, int n) {
		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		// 计算所有微博中词的tf
		ArrayList<WeiboEntity> all = new ArrayList<WeiboEntity>();
		for (ArrayList<WeiboEntity> wel : clusters) {
			all.addAll(wel);
		}
		HashMap<String,Double> alltf = getKeywordsByTF(all);

		for (int i = 0; i < clusters.size(); i++) {
			HashMap<String, Double> tf = getKeywordsByTF(clusters.get(i));
			HashMap<String, Double> wordweight = new HashMap<String, Double>();
			for (Iterator it = tf.entrySet().iterator(); it.hasNext();) {
				Map.Entry entry = (Map.Entry) it.next();
				String word = (String) entry.getKey();
				double t = (Double) entry.getValue();
				
				double r = alltf.get(word) - t; 
				double w ;
				if(r == 0) w = 0;
				else w = t / Math.sqrt(r);
				wordweight.put(word, w);
			}

			List<Map.Entry<String, Double>> list = new ArrayList<Map.Entry<String, Double>>(
					wordweight.entrySet());

			Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
				public int compare(Map.Entry<String, Double> o1,
						Map.Entry<String, Double> o2) {
					return (o2.getValue().compareTo(o1.getValue()));
				}
			});
			ArrayList<String> keywords = new ArrayList<String>();

			for (int j = 0; j < list.size(); j++) {
				if (j >= n)
					break;
				keywords.add(list.get(j).getKey());
			}
			result.add(keywords);
		}
		return result;
	}

	/**
	 * 统计一组微博加权的tf
	 * 
	 * @param wel
	 * @return
	 */
	private static HashMap<String, Double> getKeywordsByTF(ArrayList<WeiboEntity> wel) {
		HashMap<String, Double> wordtf = new HashMap<String, Double>();
		for (WeiboEntity we : wel) {
			ArrayList<WordNode> segs = we.getSegs();
			double hotrate = we.getHotrate();
			double weight = Math.log10(hotrate * 10000+1); // 权值
			if (segs == null) {
				Partition p = new Partition();
				String content = we.getContent();
				segs = p.part(content);
				if (segs.size() == 0) {
					segs.add(new WordNode(content, "n"));
				}
			}
//			System.out.println();
			for (WordNode w : segs) {
				String str = w.getWord();
				String pos = w.getPos();
//				System.out.print(str+":"+pos);
				if (pos.length() < 1)
					continue;
				String first = pos.substring(0, 1);
				if (first.equals("n") || first.equals("s") || first.equals("v")
						|| first.equals("a")) {
					// 保留名词、处所词、动词、形容词
					if (wordtf.containsKey(str)) {
						double i = wordtf.get(str);
						i += weight;
						wordtf.remove(str);
						wordtf.put(str, i);
					} else {
						wordtf.put(str, weight);
					}

				}

			}

		}
		return wordtf;
	}
	
	public static HashMap<String, Integer> getEmotionalKeywords(ArrayList<ArrayList<WeiboEntity>> clusters) {
		HashMap<String, Integer> wordtf = new HashMap<String, Integer>();
		for (ArrayList<WeiboEntity> wel:clusters) {
			for (WeiboEntity we : wel) {
				ArrayList<WordNode> segs = we.getSegs();
				if (segs == null) {
					Partition p = new Partition();
					String content = we.getContent();
					segs = p.part(content);
					if (segs.size() == 0) {
						segs.add(new WordNode(content, "n"));
					}
				}
				for (WordNode w : segs) {
					String str = w.getWord();
					
					if (EmotionAnalyze.getEmotion(str) == EmotionAnalyze.POS || 
							EmotionAnalyze.getEmotion(str) == EmotionAnalyze.NEG) {
						if (wordtf.containsKey(str)) {
							wordtf.put(str, wordtf.get(str)+1);
						} else {
							wordtf.put(str, 1);
						}
					}
				}
			}
		}
		
		return wordtf;
	}
}
