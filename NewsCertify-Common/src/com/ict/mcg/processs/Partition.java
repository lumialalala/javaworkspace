package com.ict.mcg.processs;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;

import com.ict.mcg.util.FileIO;

public class Partition {
	public static HashSet<String> dicset = new HashSet<String>();

	private static String wikiFile = FileIO.getFilePath()+"dic";
	
	private static String wikiFile_resource = FileIO.getResourcePath()+"dic";
	
	public Partition(String filewiki) {
		wikiFile = filewiki;
		wikiFile_resource="";
		if (dicset.size() == 0)
			loadItems();
	}
	
	public Partition(){
		if (dicset.size() == 0)
			loadItems();
	}
	
	/**
	 * 先分词，再合并
	 * 
	 * @param content
	 */
	public ArrayList<WordNode> participleAndMerge(String content) {

		ArrayList<WordNode> rstlist = new ArrayList<WordNode>();
		if (content.length() > 0) {
			// 开始分词
			ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
			ArrayList<WordNode> words = new ArrayList<WordNode>();

			//2014-11-2,zjq
			int size = list.size();
			for (int i = 0; i < size; ++i) {
				String w = list.get(i);

//				int index = w.indexOf("/");
				int index = w.lastIndexOf("/");

				if (index > 0) {
					//过滤符号
					String pos = w.substring(index + 1, w.length());
					if((pos.charAt(0)=='x')||(pos.charAt(0)=='w'))
						continue;
					
					if (pos.equals("nr1") && i < size-1) {
						String nextToken = list.get(i+1);
						int nextIndex = nextToken.lastIndexOf("/");
						if (nextToken.substring(nextIndex + 1, nextToken.length()).equals("n")) {
							WordNode node = new WordNode();
							node.setWord(w.substring(0, index)+nextToken.substring(0,nextIndex));
							node.setPos("nr");
							words.add(node);
						}
					}
					
					WordNode node = new WordNode();
					node.setWord(w.substring(0, index));
					node.setPos(pos);
					words.add(node);
				}

			}

			// 用wiki 数据库归并
			rstlist = wikiDBMerge(words);
		}
		
		return rstlist;
	}
	
	
	//tag = 0 不去重，不处理
	public ArrayList<WordNode> participleWithFilter(String content, int postTag){
		ArrayList<WordNode> rstlist = new ArrayList<WordNode>();
		ArrayList<String> temp = new ArrayList<String>();
		
		//获取转发的最后一层文本
		if(content.contains("//@")){
			content = content.substring(0,content.indexOf("//@"));
		}
		content = content.replace("转发微博", "");
		content = content.replaceAll("@(.*?) ", "");
		content = content.replaceAll("[\\s;#@,.&!?：；，。！？'\\\"%\\[\\]\\(\\)\\（\\）\\:\\<\\>\\《\\》“”‘’~_——-、【】-]","");
		content = content.replaceAll("\\～","");
		//去除连续的字母
		content = content.replaceAll("[a-zA-Z]{7,}","");
		//去除连续的数字
		content = content.replaceAll("[0-9]{5,}","");
		content = content.replaceAll("o网页链接","");
		content = content.replaceAll("网页链接","");
		content = content.replaceAll("...展开全文c","");
		content = content.replaceAll("展开全文c","");
		content = content.replaceAll("nbsp", "");
		
		if(content.length()>0){
			ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
			if(postTag == 0){
				for(String info:list){
					String []split = info.split("/");
					if(split.length>1){
						WordNode node = new WordNode();
						node.setWord(split[0].trim());
						node.setPos(split[1]);
						rstlist.add(node);
					}
				}
			}
			else if(postTag == 1){
			for(String info: list){
				String []split =  info.split("/");
				if(split.length>1){
					String pos = split[1];
					String word = split[0];
					
					if(pos!=null&&pos.length()>0){
							if(pos.equals("en")) continue;
							if(pos.equals("m")) continue;
							if(pos.equals("w")) continue;
							if(pos.equals("ul")) continue;
							if(pos.equals("uj")) continue;
							if(pos.equals("p")) continue;
							if(pos.equals("r")) continue;
							if(pos.equals("f")) continue;
							if(pos.equals("b")) continue;
							if(pos.equals("y")) continue;
							if(pos.equals("d")) continue;
							if(temp.contains(word.trim())) continue;
							WordNode node = new WordNode();
							node.setPos(pos);
							node.setWord(word);
							rstlist.add(node);
						}
					}
				}
			}
			
		}
		
		return  rstlist;
	}
	
	public ArrayList<WordNode> participleAndMergeExcludeSingleWord(String content) {

		ArrayList<WordNode> rstlist = new ArrayList<WordNode>();
		if (content.length() > 0) {
			// 开始分词
			ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
			ArrayList<WordNode> words = new ArrayList<WordNode>();

			//2014-11-2,zjq
			int size = list.size();
			for (int i = 0; i < size; ++i) {
				String w = list.get(i);

//				int index = w.indexOf("/");
				int index = w.lastIndexOf("/");

				if (index > 0) {
					//过滤符号
					String pos = w.substring(index + 1, w.length());
					if((pos.charAt(0)=='x')||(pos.charAt(0)=='w'))
						continue;
					
					if (pos.equals("nr1") && i < size-1) {
						String nextToken = list.get(i+1);
						int nextIndex = nextToken.lastIndexOf("/");
						if (nextToken.substring(nextIndex + 1, nextToken.length()).equals("n")) {
							WordNode node = new WordNode();
							node.setWord(w.substring(0, index)+nextToken.substring(0,nextIndex));
							node.setPos("nr");
							words.add(node);
						}
					}
					
					WordNode node = new WordNode();
					node.setWord(w.substring(0, index));
					node.setPos(pos);
					words.add(node);
				}

			}

			// 用wiki 数据库归并
			rstlist = wikiDBMerge(words);
		}
		
		//过滤单字
		ArrayList<WordNode> result = new ArrayList<WordNode>();
		for(WordNode wn: rstlist){
			if(wn.getWord().length()<2){
				continue;
			}
			result.add(wn);
		}

		return result;
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
			BufferedReader br = new BufferedReader(new InputStreamReader(is, "utf-8"));

			String line = "";

			while ((line = br.readLine()) != null) {
				dicset.add(line);
			}

			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public ArrayList<WordNode> wikiDBMerge(ArrayList<WordNode> wordlist) {
		ArrayList<WordNode> wikiTag = new ArrayList<WordNode>();

		wikiTag = wordsMerge(wordlist, dicset);

		return wikiTag;
	}

	// 根据数据库词条，按最大匹配原则做词条归并
	private ArrayList<WordNode> wordsMerge(ArrayList<WordNode> nodelist,
			HashSet<String> map){

		ArrayList<WordNode> tags = new ArrayList<WordNode>();

		int length = nodelist.size();

		int index_word = 0; // wordList的索引
		while (index_word < length) {
			// 实现分词处理
			int position = 0;
			position = TagCheck(nodelist, index_word, tags, map);
			index_word = position + 1;

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

//				result.setIDInDB(ids[j]);
				result.setWord(titles[j]);
				if (j > 0) {
					result.setPos("nm"); // 多个词合并成一个词
				} else if (j == 0) {
					result.setPos(nodes.get(startIndex).getPos());
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
	/**
	 * 分词接口，传入文本输出分词结果
	 * @param str
	 * @return
	 */
	public ArrayList<WordNode> part(String str){
		//先做过滤
	    str = this.filter(str);
		ArrayList<WordNode> result=new ArrayList<WordNode>();
		ArrayList<WordNode> al = this.participleAndMerge(str);
		for (WordNode w : al) {
			if (w.getWord().length() >= 2)
				result.add(w);
		}
		return result;
	}

	private String filter(String str) {
		// 匹配url
		String urlpattern = "[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?";
		String content = str.replaceAll(urlpattern, "");
		return content;
	}

	public static void main(String[] args) {
		String str = "【深圳90后女孩当街给残疾乞丐喂饭 感动路人】一名满头白发的老人眼巴巴地盯着快餐店里的盒饭，被一名过路的女孩瞅见了，女孩当即买来盒饭，并单膝跪地将饭一口一口喂进老人嘴里。女孩叫文芳，湖南新化人，出生于1991年，由于家境并不宽裕，即将大学毕业的她便来到了深圳打工。";
//		String str = "乌克兰";
		String file = "./WebRoot/file/dic";
		Partition p = new Partition();
		ArrayList<WordNode> al = p.participleAndMerge(str);
		NamedEntity ne = new NamedEntity();
		ne.setProps(al);
		for (WordNode w : al) {
			//if (w.getWord().length() >= 2)
				System.out.println(w.getWord() + ":" + w.getPos()+"-"+w.getProps());
		}
	}
}
