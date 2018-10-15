package com.ict.mcg.processs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import com.ict.mcg.gather.entity.WeiboEntity;

public class WeiboEntityProcessor {

	// 分词结果如果为0，直接用原微博内容作为分词结果
	public static ArrayList<WordNode> getSegments(String content){
		ArrayList<WordNode> result = new ArrayList<WordNode>();
		if (content == null || content.equals(""))
			return result;
		// 匹配英文、数字
		String enpattern = "[a-z0-9A-Z_:：-]+";

		Partition p = new Partition();
		result = p.part(content);
		for (int i = 0; i < result.size(); i++) {
			String w = result.get(i).getWord();
			String n = w.replaceAll(enpattern, "");
			if (n.length() < 1) {
				result.remove(i);
				i--;
			}
		}
		if (result.size() == 0) {
			result.add(new WordNode(content, "self"));
		}
		// System.out.println(result);
		return result;
	}

	/**
	 *  过滤规则1 ： 根据微博url 去除重复微博
	 *  过滤规则2 ： 根据新采集微博的关键词和原微博关键词  去除不相关微博
	 * @param allweibo
	 * @param keyword
	 * @return
	 */
	public static ArrayList<WeiboEntity> weiboFilter(ArrayList<WeiboEntity> allweibo,
			String keyword) {
		ArrayList<WeiboEntity> result = new ArrayList<WeiboEntity>();
		if (allweibo == null || allweibo.size() < 1)
			return result;
		HashSet<String> midlist = new HashSet<String>();
		Cluster cl = new Cluster();
		ArrayList<WordNode> keywords = getSegments(keyword);
		
		for (WeiboEntity w : allweibo) {
			String url = w.getUrl();
			//  根据微博url去重复微博
			if (midlist.contains(url)) {
				continue;
			} else {
				midlist.add(url);
			}

			ArrayList<WordNode> segs = w.getSegs();
			
			// 去除不相关微博
			ArrayList<WordNode> list = new ArrayList<WordNode>();
			list.addAll(segs);
			//list.add(new WordNode(w.getName(), "nr")); // 加入作者名字,防止漏掉当事人的微博
			// 过滤掉 : 新采集的微博和原先关键词的匹配程度低 || 当原微博类关键词只有一个（分词结果的size为1），如果采集到的微博不包含这个关键词，则过滤
			if (cl.match(keywords, list) == 0 || ("self".equals(keywords.get(0).getPos()) && !w.getContent().contains(keywords.get(0).getWord())))
				continue;

			result.add(w);
		}

		return result;
	}
	
	public static void setHotrate(ArrayList<WeiboEntity> allWeibo) {
		//ArrayList<WeiboEntity> result = new ArrayList<WeiboEntity>();
		int max = 0;
		for (WeiboEntity we : allWeibo) {
			int hot = Integer.parseInt(we.getForword())
					+ Integer.parseInt(we.getComment());
			if (max < hot)
				max = hot;
		}

		for (WeiboEntity we : allWeibo) {
			int hot = Integer.parseInt(we.getForword())
					+ Integer.parseInt(we.getComment());
			double hotrate = Math.sqrt(hot) / Math.sqrt(max);
			//we.setHotrate(hotrate);
			//result.add(we);
		}
//		return result;
	}
	
	//不用了
	/*private static ArrayList<WordNode> getKeywords(String content) {
		ArrayList<WordNode> result = new ArrayList<WordNode>();

		StringTokenizer st = new StringTokenizer(content);
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			result.add(new WordNode(s, "n"));
		}
		if (result.size() > 1)
			return result;

		// 匹配英文、数字
		String enpattern = "[a-z0-9A-Z_:：-]+";

		Partition p = new Partition();
		result = p.part(content);
		for (int i = 0; i < result.size(); i++) {
			String w = result.get(i).getWord();
			String n = w.replaceAll(enpattern, "");
			if (n.length() < 1) {
				result.remove(i);
				i--;
			}
		}
		if (result.size() == 0) {
			result.add(new WordNode(content, "n"));
		}
		// System.out.println(result);
		return result;
	}*/
}
