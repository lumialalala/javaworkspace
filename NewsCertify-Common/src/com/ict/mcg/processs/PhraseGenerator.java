/**
 * 
 */
package com.ict.mcg.processs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.model.ExtractTitle;
import com.ict.mcg.service.CosineSimilarity;
import com.ict.mcg.util.FileIO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;

/**
 * @author JZW 从一段文本中提取出短语
 */
public class PhraseGenerator {
	public static HashSet<String> dicset = new HashSet<String>();

	private int MaxLength = 20;// 最长短语为20字

	private static String wikiFile = FileIO.getFilePath()+"dic";
	
	private static String wikiFile_resource = FileIO.getResourcePath()+"dic";

	public PhraseGenerator(String filewiki) {
		wikiFile = filewiki;
		wikiFile_resource = "";
		if (dicset.size() == 0)
			loadItems();
	}

	public PhraseGenerator() {
		if (dicset.size() == 0)
			loadItems();
	}


	/**
	 * 将wiki数据load到HashMap中
	 */
	private void loadItems() {
		try {
			InputStream is = this.getClass().getResourceAsStream(wikiFile_resource);
			if (is == null) {
				is = new FileInputStream(wikiFile);
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(
					is, "utf-8"));

			String line = "";

			while ((line = br.readLine()) != null) {
				dicset.add(line);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	// 根据数据库词条，按最大匹配原则做词条归并
	private ArrayList<WordNode> wordsMerge(ArrayList<WordNode> nodelist,
			HashSet<String> map) {

		ArrayList<WordNode> tags = new ArrayList<WordNode>();

		int length = nodelist.size();

		int index_word = 0; // wordList的索引
		while (index_word < length) {
			// 实现分词处理
			int position = 0;
			position = TagCheck(nodelist, index_word, tags, map);
			index_word = position + 2;

		}

		return tags;
	}

	private int TagCheck(ArrayList<WordNode> nodes, int startIndex,
			ArrayList<WordNode> tags, HashSet<String> map) {
		int position = 0;

		int size = 7;
		long[] ids = { -1, -1, -1, -1, -1, -1, -1 };
		String[] titles = { "", "", "", "", "", "", "" };

		titles[0] = nodes.get(startIndex).getWord();
		int max = 0;

		// 取出用于匹配的候选集
		for (int j = 1; j < size; j++) {
			if (startIndex + j < nodes.size()) {
				titles[j] = titles[j - 1]; // + bArray.get(startIndex+j);

				titles[j] += nodes.get(startIndex + j).getWord();
				max = j;
			}
		}

		// 最大匹配原则
		for (int j = max; j >= 0; j--) {
			if (map.contains(titles[j])) {
				ids[j] = 1; // 取出ID
				break;
			}
		}

		WordNode result = new WordNode();
		for (int j = max; j >= 0; j--) {
			if (ids[j] != -1) {

				// result.setIDInDB(ids[j]);
				result.setWord(titles[j]);
				if (j > 0) {
					result.setPos("nm"); // 多个词合并成一个词
				} else if (j == 0) {
					result.setPos(nodes.get(startIndex).getPos());
					result.setWeight(nodes.get(startIndex).getWeight());
				}
				tags.add(result);

				position = j;

				return position + startIndex;
			}
		}

		if (ids[0] == -1) {
			tags.add(nodes.get(startIndex));
		}

		return position + startIndex;
	}

	public ArrayList<WordNode> wikiDBMerge(ArrayList<WordNode> wordlist) {
		ArrayList<WordNode> wikiTag = new ArrayList<WordNode>();

		wikiTag = wordsMerge(wordlist, dicset);

		return wikiTag;
	}

	/**
	 * 先分词，再合并
	 * 
	 * @param content
	 */
	public ArrayList<ArrayList<WordNode>> partAndMerge(String content) {

		ArrayList<ArrayList<WordNode>> rstlist = new ArrayList<ArrayList<WordNode>>();
		// 统计词频
		HashMap<String, Integer> wordmap = new HashMap<String, Integer>();
		if (content.length() > 0) {
			// 加入初始句子
			rstlist.add(new ArrayList<WordNode>());

			// 开始分词
			ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
			ArrayList<WordNode> sen = rstlist.get(rstlist.size() - 1); // 获取最后一个空句子

//			System.out.println(list);
			
			int quoteLock = -1;
			for (int idx = 0; idx < list.size(); idx++) {
				String w = list.get(idx);
				int index = w.lastIndexOf("/");

				if (index > 0) {
					// 按符号划分
					String pos = w.substring(index + 1, w.length());
					String word = w.substring(0, index);
					if ((pos.equals("session"))
							|| (pos.charAt(0) == 'x' && !pos.equals("x"))
							|| (pos.charAt(0) == 'w' && !word.equals("\"")
									&& !word.equals("'") && !word.equals("“")
									&& !word.equals("”") && !word.equals("-"))) {
						// 用wiki 数据库归并当前句子
						if (word.equals("【")) {
							quoteLock = idx;
						}
						sen = wikiDBMerge(sen);
						// 加入新句子
						rstlist.add(new ArrayList<WordNode>());
						sen = rstlist.get(rstlist.size() - 1);
						continue;
					}
					// 处理@人的情况
					if (w.charAt(0) == '@') {
						pos = "@";
					}

					WordNode node = new WordNode();

					node.setWord(word);
					node.setPos(pos);
					// 基于pos设置权重
					node.setWeight(getWeight(pos));
					if (idx == quoteLock + 1) { // '【'之后的词
						node.setWeight(2.5);
						quoteLock = -1;
					}
					sen.add(node);
					if (wordmap.containsKey(word)) {
						int i = wordmap.get(word);
						wordmap.put(word, i + 1);
					} else {
						wordmap.put(word, 1);
					}
				}
			}
		}
		// 基于词频加设权值
		for (ArrayList<WordNode> wnl : rstlist) {
			for (WordNode wn : wnl) {
				if (wn.getWeight() >= 0.6)
					wn.setWeight(wn.getWeight() + 0.5
							* Math.sqrt(wordmap.get(wn.getWord())));
			}
		}
		return rstlist;
	}

	/**
	 * 基于pos设置词权重
	 */
	private double getWeight(String pos) {
		if (pos.contains("ude"))// 单独处理"的地得"
			return 0.55;
		char f = pos.charAt(0);
		double r = 0;
		switch (f) {
		case 'w':
			r=0;
			break;
		case 'n':
			r = 1;
			break;
		case 'v':
			r = 0.7;
			break;
		case 'a':
		case 'r':
			r = 0.6;
			break;
		case 'u':
			r = 0.4;
			break;
		default:
			r = 0.5;
		}
		return r;
	}

	private String filter(String str) {
		// 匹配url
		String urlpattern = "[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?";
		String content = str.replaceAll(urlpattern, "");
		return content;
	}

	public ArrayList<WordNode> combinePhrase(ArrayList<WordNode> sen) {
		double minweight = 0.5;// 组合词阈值
		ArrayList<WordNode> result = new ArrayList<WordNode>();

		for (int i = 0; i < sen.size(); i++) {
			ArrayList<WordNode> p = new ArrayList<WordNode>();
			// 对每个词以起始词开始组合
			WordNode first = sen.get(i);
			double weight = first.getWeight();
			if (weight <= 0.6)
				continue;

			String word = first.getWord();
			for (int j = i + 1; j < sen.size(); j++) {
				WordNode last = sen.get(j);
				double w = last.getWeight();
				String wo = last.getWord();
				// 计算组合短语的权重
				// double add = 0.15;
				double add = Math.sqrt(j - i) * 0.1;
				if (((j - i) <= 6) && ((j - i) >= 4)) {
					add = (j - i) * 0.06;
				}

				double newweight = (weight + w) / 2 * (1 + add); // 组合词加权
				if (newweight < minweight || word.length() > MaxLength) {
					break;
				} else {
					weight = newweight;
					word = word + wo;
					WordNode wn = new WordNode();
					wn.setWord(word);
					wn.setWeight(weight);
					p.add(wn);
					// result.add(wn);
				}
			}
			// 保留最长短语
			if (p.size() > 0) {
				result.add(p.get(p.size() - 1));
			}
		}

		int flag[] = new int[result.size()];// 标记是否重复需删除
		for (int i = 0; i < flag.length; i++) {
			flag[i] = 0;
		}

		for (int i = 0; i < result.size(); i++) {
			// System.out.println(result.get(i).getWord() + ":" +
			// result.get(i).getWeight());
			for (int j = 0; j < i; j++) {
				if (result.get(j).getWord().contains(result.get(i).getWord())) {
					flag[i] = 1;
					break;
				}
			}
		}
		ArrayList<WordNode> re = new ArrayList<WordNode>();
		for (int i = 0; i < flag.length; i++) {
			if (flag[i] == 0)
				re.add(result.get(i));
		}
		return re;
	}

	/**
	 * 计算两个短语相似度
	 */
	private double getSim(String w1, String w2) {
		double result = 0;
		Partition p = new Partition();
		ArrayList<WordNode> list1 = p.part(w1);
		ArrayList<WordNode> list2 = p.part(w2);
		int corr = 0;
		for (WordNode wn1 : list1) {
			for (WordNode wn2 : list2) {
				if (wn2.getWord().equals(wn1.getWord()))
					corr++;
			}
		}
		int min = list1.size() > list2.size() ? list2.size() : list1.size();
		result = (double) corr / min;
		return result;
	}

	/**
	 * 针对一个类，生成类中文本的标题
	 * @param text 类中的文本
	 * @param wordFreq 文本所在类的词频
	 * @return 文本生成的短语
	 */
	public ArrayList<WordNode> generate(String text, HashMap<String, Integer> wordFreq) {
		ArrayList<WordNode> result = new ArrayList<WordNode>();
		// 0. 先做过滤
		text = this.filter(text);
		// 1. 分词、合词、设置权重、按标点划分句子
		ArrayList<ArrayList<WordNode>> sentences = this.partAndMerge(text);

		// 通过类的词频加权
		for (ArrayList<WordNode> sentence : sentences) {
			for (WordNode wn : sentence) {
				if (!wordFreq.containsKey(wn.getWord())) {
					continue;
				}
				if (wn.getWeight() >= 0)
					wn.setWeight(wn.getWeight() + 0.5
							* Math.sqrt(wordFreq.get(wn.getWord())));
			}
		}
		// 2. 对每个句子分别合并短语
		for (int i = 0; i < sentences.size(); i++) {
			ArrayList<WordNode> p = this.combinePhrase(sentences.get(i));
			if (i == 1) {
				// 对第一句话加权
				for (WordNode wn : p) {
					Pattern pattern = Pattern.compile("【.*?" + Pattern.quote(wn.getWord()) + ".*?】");
					Matcher matcher = pattern.matcher(text);
					if (matcher.find()) {
						wn.setWeight(wn.getWeight() * 3);
					} else {
						wn.setWeight(wn.getWeight() * 1.5);
					}

				}
			}

			result.addAll(p);
		}
		// for(WordNode wn:result)
		// System.out.println(wn.getWord()+"\t"+wn.getWeight());
		return result;
	}
	/**
	 * 短语生成入口，传入一段文本，输出结果
	 */
	public ArrayList<WordNode> generate(String text) {
		ArrayList<WordNode> result = new ArrayList<WordNode>();
		// 0. 先做过滤
		text = this.filter(text);
		// 1. 分词、合词、设置权重、按标点划分句子
		ArrayList<ArrayList<WordNode>> sentences = this.partAndMerge(text);
		// 2. 对每个句子分别合并短语
		// //组合形成第一句
		// String first = "";
		// ArrayList<WordNode> sen = sentences.get(1);
		// double weight = 0.0;
		// for(WordNode wn:sen){
		// first+=wn.getWord();
		// weight=(weight+wn.getWeight())/2*1.2;
		// }
		// WordNode wn = new WordNode();
		// wn.setWeight(weight);
		// wn.setWord(first);
		// result.add(wn);
		for (int i = 0; i < sentences.size(); i++) {
			ArrayList<WordNode> p = this.combinePhrase(sentences.get(i));
			if (i == 1) {
				// 对第一句话加权
				for (WordNode wn : p) {
					Pattern pattern = Pattern.compile("【.*?" + wn.getWord()
							+ ".*?】");
					Matcher matcher = pattern.matcher(text);
					if (matcher.find()) {
						wn.setWeight(wn.getWeight() * 3);
					} else {
						wn.setWeight(wn.getWeight() * 1.5);
					}

				}
			}

			result.addAll(p);
		}
		// for(WordNode wn:result)
		// System.out.println(wn.getWord()+"\t"+wn.getWeight());
		return result;
	}

	/**
	 * 获取整个类的词频统计
	 * @param clusters
	 * @return 词频统计
	 */
	public HashMap<String, Integer> getWordFrequency(ArrayList<ArrayList<WeiboEntity>> clusters) {
		HashMap<String, Integer> wordFrequency = new HashMap<String, Integer>();
		for (ArrayList<WeiboEntity> cluster : clusters) {
			for (WeiboEntity weibo : cluster) {
				String content = weibo.getContent();
				// 开始分词
				ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
				for (String pair : list) {
					if (!pair.contains("/")) {
						continue;
					}
					int idx = pair.lastIndexOf("/");
					if (pair.substring(idx + 1).contains("w")) { //标点符号不计算词频
						continue;
					}
					String word = pair.substring(0, idx);
					Integer freq = 0;
					if (wordFrequency.containsKey(word)) {
						freq = wordFrequency.get(word);
					}
					freq++;
					wordFrequency.put(word, freq);
				}
			}
		}
		
		return wordFrequency;
	}
	
	/***
	 * 新版提取标题算法
	 * 
	 * @author 雅滋宝宝
	 * */
	public static String generateTitle(ArrayList<ArrayList<WeiboEntity>> clusters, String keyword)
	{			
		String keySentence=null;
		String titletemp = null;
		String title;
		String content;
		int count=1;
		Pattern p=Pattern.compile("【(.*?)】");
		ArrayList<String> titlelist=new ArrayList<String>();
		for(ArrayList<WeiboEntity> weibos:clusters)
		{
			//先找天然标题【】
			StringBuilder builder = new StringBuilder() ;
			HashMap<String,Integer> titlecount=new HashMap<String,Integer>();
			for(WeiboEntity wn : weibos)
			{
				content=wn.getContent();
				if(content!=null)builder.append(content);
				
				Matcher m=p.matcher(content);
				if(m.find())
				{  
					String text=content.substring(m.start()+1, m.end()-1);
					//不用硬匹配，使用余弦相似度来匹配
					boolean flag = true; 
					for (String titlecount_temp : titlecount.keySet()) {  
						if (!titlecount_temp.equals("")&&!text.equals("")){
							if (CosineSimilarity.getSimilarity(titlecount_temp, text)>0.75){
					    	count=titlecount.get(titlecount_temp)+1;
					    	titlecount.put(titlecount_temp, count) ;
					    	flag = false;
					    	break;
					    }
						}
					}  
					//if(titlecount.containsKey(text))count=titlecount.get(text)+1;
					if(flag){
						count=1;
						titlecount.put(content.substring(m.start()+1, m.end()-1), count) ;
					}
				}
			}
			Boolean flag = true; 
			if(titlecount.size()>0)
			{
				int maxcount=0;
				for(String s : titlecount.keySet())
				{
					if(titlecount.get(s)>maxcount)
					{
						titletemp=s;
						maxcount=titlecount.get(s);
					}
					if(titlecount.get(s)==maxcount && s.length()>titletemp.length())
					{
						titletemp=s;
					}
				}
				if(titletemp.length()>5){   //标题的长度必须大于5
					flag=false;
					titlelist.add(titletemp);
				}
//				System.out.println("0步过滤:"+titletemp);
			}

			//执行textrank+新标题提取算法
			if(flag) 
			{ 
				String text=builder.toString();
				keySentence=ExtractTitle.extractKeySentence(text);//从文本提取关键句
//				System.out.println("关键句："+keySentence);
				if(keySentence!=null)
				{
					titletemp=ExtractTitle.extractTitle(keySentence);//从关键句提取标题
//					System.out.println("标题："+titletemp);
					if(titletemp!=null)titlelist.add(titletemp);
				}
			}
		}
		
		// 将每条微博与其类标题关联
		for (int i = 0; i < clusters.size(); i++) {
			String s = titlelist.get(i);
			if(s!=null)
			{
				ArrayList<WeiboEntity> wel = clusters.get(i);
				for (WeiboEntity we : wel)
					we.setClasstitle(s);
			}
		}
		
		//记录最大类
		int maxClusterIdx = -1;
		int maxClusterNum = 0;
		for (int j = 0; j < clusters.size(); j++) {
			ArrayList<WeiboEntity> cluster = clusters.get(j);
			if (cluster.size() > maxClusterNum) {
				maxClusterNum = cluster.size();
				maxClusterIdx = j;
			}
		}
		
		// 选择最早的契合搜索词的类标题
		Cluster cl = new Cluster();
		ArrayList<WordNode> c1 = WeiboEntityProcessor.getSegments(keyword);
		for (int i = 0; i < titlelist.size(); i++) {
			String s = titlelist.get(i);
			ArrayList<WordNode> c2 = WeiboEntityProcessor.getSegments(s);
			if (s.length() > 1 && cl.match(c1, c2) >= 1 && clusters.get(i).size() * 4 > maxClusterNum) {
				title = s;
				return title;
			}
		}
				
		// 选择最早微博大类的标题，大于最大类的一半
		for (int i = 0; i < titlelist.size(); i++) {
			String s = titlelist.get(i);
			if (s.length() > 1 && clusters.get(i).size() * 2 > maxClusterNum) {
				title = s;
				return title;
			}
		}

		// 选择最大微博类的标题
		title = titlelist.get(maxClusterIdx);
		return title;
	}
	/**
	 * 从事件聚类结果生成每个事件的关键短语(已弃用)
	 * 
	 * @param clusters
	 * @return 整个事件的标题
	 */
	public String generateForAll(ArrayList<ArrayList<WeiboEntity>> clusters, String keyword) {
		String title = "";
		
		// 获取微博类的词频
		HashMap<String, Integer> wordFreq = getWordFrequency(clusters);
				
		ArrayList<String> result = new ArrayList<String>();
		ArrayList<ArrayList<WordNode>> all = new ArrayList<ArrayList<WordNode>>();
		// 每个聚类分别生成短语

		int maxClusterIdx = -1;
		int maxClusterNum = 0;
		for (int j = 0; j < clusters.size(); j++) {
			ArrayList<WeiboEntity> cluster = clusters.get(j);
			if (cluster.size() > maxClusterNum) {
				maxClusterNum = cluster.size();
				maxClusterIdx = j;
			}
			HashMap<String, Double> map = new HashMap<String, Double>();
			ArrayList<WordNode> wordlist = new ArrayList<WordNode>();
			for (int i = 0; i < cluster.size(); i++) {
				if (i >= 10) // 最多只分析10条微博
					break;
				WeiboEntity we = cluster.get(i);
				// 每条微博分别生成短语
				ArrayList<WordNode> r = this.generate(we.getContent(), wordFreq);
				for (WordNode wn : r) {
					if (map.containsKey(wn.getWord())) {
						double w = map.get(wn.getWord());
						w = w > wn.getWeight() ? w * 1.015
								: wn.getWeight() * 1.05;
						map.put(wn.getWord(), w);
					} else {
						map.put(wn.getWord(), wn.getWeight());
						wordlist.add(wn);
					}
//					System.out.println(wn.getWord() + ":" + wn.getWeight());
				}
//				System.out.println();
			}
			for (WordNode wn : wordlist) {
				wn.setWeight(map.get(wn.getWord()));
			}

			// 排序
			Collections.sort(wordlist, new Comparator<WordNode>() {
				public int compare(WordNode o1, WordNode o2) {
					double d = o2.getWeight() - o1.getWeight();
					if (d > 0)
						return 1;
					else if (d < 0)
						return -1;
					else
						return 0;
				}
			});
//			System.out.println("cluster:" + j);
//			for (WordNode word : wordlist) {
//				System.out.println(word.getWord() + ":" + word.getWeight());
//			}
			all.add(wordlist);

		}

		// 为每个类选出代表短语
		for (ArrayList<WordNode> wnl : all) {
			boolean add = false;
			if (result.size() < 1) {
				for (WordNode wn : wnl) {
					if (wn.getWord().length() > 3) {
						title = wn.getWord();
						result.add(wn.getWord());
						add = true;
						break;
					}
				}
			} else {

				for (WordNode wn : wnl) {
					String word = wn.getWord();
					if (word.length() < 4)
						continue;
					double sim = 0;
					for (String s : result) {
						double d = this.getSim(word, s);
						if (sim < d)
							sim = d;
					}
					if (sim <= 0.5) {
						result.add(word);
						add = true;
						break;
					}
				}
			}
			if (!add)
				result.add("");

		}

//		for (String s : result)
//			System.out.println(s);
		
		// 将每条微博与其类标题关联
		for (int i = 0; i < clusters.size(); i++) {
			String s = result.get(i);
			ArrayList<WeiboEntity> wel = clusters.get(i);
			for (WeiboEntity we : wel)
				we.setClasstitle(s);
		}

		// 选择最早的契合搜索词的类标题
		Cluster cl = new Cluster();
		ArrayList<WordNode> c1 = WeiboEntityProcessor.getSegments(keyword);
		for (int i = 0; i < result.size(); i++) {
			String s = result.get(i);
			ArrayList<WordNode> c2 = WeiboEntityProcessor.getSegments(s);
			if (s.length() > 1 && cl.match(c1, c2) >= 1 && clusters.get(i).size() * 4 > maxClusterNum) {
				title = s;
				return title;
			}
		}
				
		// 选择最早微博大类的标题，大于最大类的一半
		for (int i = 0; i < result.size(); i++) {
			String s = result.get(i);
			if (s.length() > 1 && clusters.get(i).size() * 2 > maxClusterNum) {
				title = s;
				return title;
			}
		}

		// 选择最大微博类的标题
		title = result.get(maxClusterIdx);
		// System.out.println(title);

		return title;
	}

	public static void main(String[] args) {
		
		
		
			/*
			try{
				if((boolean) total_content.get("hidden")==true){
					System.out.println("continue2");
					continue;
				}
			}catch(Exception e){
				ArrayList<WeiboEntity> Weibo_list=new ArrayList<WeiboEntity>();
				WeiboEntity we=new WeiboEntity();
				we.setContent(content);
				Weibo_list.add(we);
				ArrayList<ArrayList<WeiboEntity>> clusters_test=new ArrayList<ArrayList<WeiboEntity>>();
				clusters_test.add(Weibo_list);
				
				KeyWordComputer kwc = new KeyWordComputer(5);
				//String title = "维基解密否认斯诺登接受委内瑞拉庇护";
				//String content = "有俄罗斯国会议员，9号在社交网站推特表示，美国中情局前雇员斯诺登，已经接受委内瑞拉的庇护，不过推文在发布几分钟后随即删除。俄罗斯当局拒绝发表评论，而一直协助斯诺登的维基解密否认他将投靠委内瑞拉。　　俄罗斯国会国际事务委员会主席普什科夫，在个人推特率先披露斯诺登已接受委内瑞拉的庇护建议，令外界以为斯诺登的动向终于有新进展。　　不过推文在几分钟内旋即被删除，普什科夫澄清他是看到俄罗斯国营电视台的新闻才这样说，而电视台已经作出否认，称普什科夫是误解了新闻内容。　　委内瑞拉驻莫斯科大使馆、俄罗斯总统府发言人、以及外交部都拒绝发表评论。而维基解密就否认斯诺登已正式接受委内瑞拉的庇护，说会在适当时间公布有关决定。　　斯诺登相信目前还在莫斯科谢列梅捷沃机场，已滞留两个多星期。他早前向约20个国家提交庇护申请，委内瑞拉、尼加拉瓜和玻利维亚，先后表示答应，不过斯诺登还没作出决定。　　而另一场外交风波，玻利维亚总统莫拉莱斯的专机上星期被欧洲多国以怀疑斯诺登在机上为由拒绝过境事件，涉事国家之一的西班牙突然转口风，外长马加略]号表示愿意就任何误解致歉，但强调当时当局没有关闭领空或不许专机降落。";
				Collection<Keyword> keyword = kwc.computeArticleTfidf(content);
				String str_temp = "";
				for (Keyword result_temp : keyword) {
					str_temp = str_temp + result_temp.getName() + " ";
				}
				System.out.println(str_temp);
			
				String title= generateTitle(clusters_test,str_temp);
				
				ArrayList profile = new ArrayList();
				profile = (ArrayList) total_content.get("profile");
				profile.set(0,title);
				System.out.println(profile.get(0));
				table.update(new BasicDBObject("_id", total_content.get("_id")), new BasicDBObject("$set", new BasicDBObject("profile", profile))); 
			}
			}
		
		
		/*
		String file = "./WebRoot/file/dic";
		PhraseGenerator p = new PhraseGenerator(file);
		
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415088479985.json";
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415088671740.json";
		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415089004232.json";
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415086605800.json";
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415069089291.json";
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415071994598.json";
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415080582518.json";
//		String jsonFile = "C:\\Users\\Shirley\\Desktop\\1415081773268.json";
		
		String jsonStr = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(jsonFile), "UTF-8"));
			String line = "";
			while ((line = reader.readLine()) != null) {
				jsonStr += line;
			}
			reader.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		
		ArrayList<ArrayList<WeiboEntity>> allClusters = new ArrayList<ArrayList<WeiboEntity>>();
		JSONObject obj = JSONObject.fromObject(jsonStr);
		String keyword = obj.getString("title");
		JSONArray clusters = obj.getJSONArray("eventlist");
		for (int i = 0; i < clusters.size(); i++) {
			JSONObject cluster = clusters.getJSONObject(i);
			System.out.println(cluster.get("title"));
			JSONArray weibos = cluster.getJSONArray("weibos");
			ArrayList<WeiboEntity> weiboOfCluster = new ArrayList<WeiboEntity>();
			for (int j = 0; j < weibos.size(); j++) {
				JSONObject weibo = weibos.getJSONObject(j);
				WeiboEntity wb = new WeiboEntity();
				wb.setName(weibo.getString("name"));
				String content = weibo.getString("text");
				wb.setContent(content);
				ArrayList<WordNode> segs = WeiboEntityProcessor
						.getSegments(content);
				wb.setSegs(segs);
				weiboOfCluster.add(wb);
			}
			allClusters.add(weiboOfCluster);
		}
		System.out.println();
		System.out.println(p.generateForAll(allClusters, keyword));
		
		String str11 = "【所谓“深圳最美女孩”被曝是炒作】跪地给残疾乞丐喂饭，深圳一90后女孩“文芳”爆红网络。@南方都市报 记者求证得知，整个行为是某商业展的炒作，拍照现场不在欢乐海岸而是在东门。东门新园路出口报刊亭老板见证了照片拍摄过程：“女孩喂了几口饭，一瘦小男子来回拍了几张照片，然后一起走了”";
		String str10 = "【深圳90后女孩当街给残疾乞丐喂饭 感动路人】一名满头白发的老人眼巴巴地盯着快餐店里的盒饭，被一名过路的女孩瞅见了，女孩当即买来盒饭，并单膝跪地将饭一口一口喂进老人嘴里。女孩叫文芳，湖南新化人，出生于1991年，由于家境并不宽裕，即将大学毕业的她便来到了深圳打工。";
		String str9 = "【李坚柔为中国夺得索契冬奥会首金 】半决赛主力队友意外折戟，决赛中起跑落后，一切都没能阻挡27岁的李坚柔在短道速滑女子500米比赛中夺冠。决赛中，三位选手摔倒，李坚柔未受影响，一路向前，率先冲过终点，为中国代表团赢来索契冬奥会的首金，同时为中国队实现了该项目“四连冠”。";
		String str = "【内地生赴港要求普通话授课 引发骂战】香港城市大学一课程的内地生选读“以粤语授课”的班，却要求老师改用普通话，引发香港学生不满对骂。老师只能讲一句粤語，再用普通话翻译一次。内地生：“我们花这么多钱来到这里，却这样安排！”，香港生：“嚟香港预咗要听广东话啦！”";
		String str1 = "【《大话西游》20年后重新上映！】时隔20年，周星驰经典电影《大话西游》系列将于10月24日登陆全国各大影院。公映的《月光宝盒》、《大圣娶亲》版本是从1994年内地公映的胶片拷贝转制的数字版。网友：曾有一部经典电影摆在我面前，我却没有买票…这次，你会选择去影院重温经典吗？By央视";
		String str4 = "对于这样的全裸图片新闻，\"广西女子与朋友打赌，只为了赢的iphone6而街头裸奔\"， 一般第一天都不打马赛克，第二天就打了，机会，很重要，我已经保存全套不打的，谁要？ http://t.cn/R7waJ4u (分享自 @内涵段子)";
		String str3 = "【央行：还清首套房贷再贷算首套 最低首付三成打七折】央行和银监会昨发通知，拥有1套住房并已结清相应购房贷款的家庭，为改善居住条件再次申请贷款购买普通商品住房，银行业金融机构执行首套房贷款政策。此外，银行业金融机构可向符合政策条件的非本地居民发放住房贷款。网图http://t.cn/Rhu7Ams";
		String str5 = "【女子为赢iPhone6 深夜当街裸奔三点尽露】广西崇左市龙州县昨夜突现一年轻女子，全身赤裸三点尽露在马路裸奔。据称该女子在“朋友圈”跟人打赌，赤条条绕城一圈便送她一部iPhone6，结果她真的褪去所有衣服尽情裸奔。图：http://t.cn/R77HzXW 网友：赢了手机锻炼了身体、众人饱了眼福，一举三得啊！";
		String str2 = "神经病！ 【女子街头裸奔 为iPhone6打赌】 http://t.cn/R77CVRA @新浪新闻客户端";
		String str6 = "10月18日零时，Smartisan T1 4G版将正式开售，我们在天猫开设的“锤子科技官方旗舰店”（http://t.cn/RvRYHfl）也将同时开业。祝大卖...";
		String str7 = "这世上人渣太多，光教孩子以人为善是不够的 //9岁孩子登门道歉 遭殴打后被逼下跪一小时新闻频道__中国青年网";
		String str8 = "【大事不妙！Adobe公司要关了】今天上午，一位叫@龙猫巴斯 在微博上曝光Adobe中国区全体解散。CSDN记者向Adobe中国求证，Adobe中国回复称，他们将马上给出官方回复，请稍等片刻。至今仍未有消息。";
		String str12 = "【为缓解北京雾霾天 廊坊出临时限行新规】记者从廊坊市有关部门获悉，为缓解北京雾霾天气，紧邻的廊坊市紧急出台新规，临时对廊坊进行尾号限行措施的交通管控。 时间为2014年10月8日至2014年10月11日，该措施具体何时结束，将视北京天气状况决定。（记者 辰光）";
		String str13 = "【廊坊为缓解北京雾霾天气出临时限行新规----今天买的科林环保明天有戏了";
		String str14 = "从银滩回北京是10月4号，银滩天空瓦蓝瓦蓝的，汽车越往前开，天气越糟糕。到了河北地界就看见天气不光是阴，还有雾霾。女儿到北京后，开始哮喘。昨晚喘得很厉害。真不知道如何是好。继续待在山东不回来，女儿的工作怎么办？回来就犯病，身体怎么办，生病也不能不上班。北京的雾霾天气何时才能治理好！";
		String str15 = "河北廊坊为缓解北京雾霾天气紧急实行汽车限行 （分享自 @网易新闻）京津冀应该同时限行，因为都连着。 http://t.cn/RhkfQrj ";
		String str16 = "【西安：“雨水”节气迎降雪 雾霾天气有缓解但效果不大】2月18日是“雨水”节气，这表示雪花纷飞，冷气浸骨的天气将渐渐消失，而春风拂面，冰雪融化，湿润的空气、温和的阳光和潇潇细雨的日子正向我们走来。雨水节气一般从2月18日或19日开始，到3月4日或5日结束。";
		
		*/
//		ArrayList<WordNode> results = p.generate(str16);
//		for (WordNode result : results) {
//			System.out.println(result.getWord() + ":" + result.getWeight());
//		}
	}
}
