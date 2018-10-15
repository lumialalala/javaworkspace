package com.ict.mcg.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ict.mcg.processs.ICTAnalyzer;
import com.ict.mcg.processs.PhraseGenerator;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.util.ParamUtil;

/**
 * @author pick
 * 2018-1-17
 * 标题提取
 * 此处使用TextRank4ZH作为标题提取的工具
 * TextRank4ZH使用python3实现，需要linux安装好TextRank4ZH供调用
 * java代码使用java自带的runtime方式类似于命令行的形式调用TextRank4ZH
 */
public class ExtractTitle {
	/**
	 * @param list 包含需要提取标题的多条微博，多条微博都是同一条线索的
	 * @return 提取后的标题
	 */
	public static String extractTitle(String content)
	{
		//优先找天然标题【】
		Pattern p=Pattern.compile("【(.*?)】");
		Matcher m=p.matcher(content);
		if(m.find())
		{
//			System.out.println("0步过滤");
			
			return removeSymbol(content.substring(m.start()+1, m.end()-1));
		}
		
		//优先找天然标题《》
		 p=Pattern.compile("《(.*?)》");
		 m=p.matcher(content);
		if(m.find())
		{
//			System.out.println("0步过滤");
			return removeSymbol(content.substring(m.start()+1, m.end()-1));
		}
		
		//首先过滤无用字符
		String urlpattern = "[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?";
		content = content.replaceAll(urlpattern, "");//去url
		content = content.replaceAll("@.*?[ $]", "");//去@
		content =content.replaceAll("#(.*?)#", "");//去#
		content = content.replaceAll("[a-zA-Z]{7,}","");//去连续字母
		content = content.replaceAll("[0-9]{5,}","");//去连续数字
		content = content.replace("转发微博", "");
		content = content.replaceAll("o网页链接","");
		content = content.replaceAll("网页链接","");
		content = content.replaceAll("...展开全文c","");
		content = content.replaceAll("展开全文c","");
		content = content.replaceAll("nbsp", "");
//		System.out.println(content);
		if(content.length()<30)
		{
//			System.out.println("1步过滤:"+content);
			return removeSymbol(content);
		}
		//再过滤无用词性
		ArrayList<String> words = ICTAnalyzer.analyzeParagraph(content, 1);
		HashMap<String, Integer> words_filter=new HashMap<String, Integer>();
		ArrayList<String> wordslist=new ArrayList<String>();
		ArrayList<String> sentence_by_comma=new ArrayList<String>();//按逗号分句
		String temp="";
		int count;
		for(int i=0;i<words.size();i++)
		{
			String s=words.get(i);
			int index = s.lastIndexOf("/");
			if(index>0)
			{
				String pos=s.substring(index+1);
				String word=s.substring(0,index);
				if(words_filter.containsKey(word))count=words_filter.get(word)+1;
				else count=1;
				words_filter.put(word, count);
				wordslist.add(word);
				if(word.equals(",")||word.equals("，")||word.equals("～")||word.equals("~"))
				{
					sentence_by_comma.add(temp);
					temp="";
				}
				else 
				{
					temp+=word;
				}
			}
		}

		sentence_by_comma.add(temp);
		
//		ArrayList<String> sentence_copy=new ArrayList<String>();
//		sentence_copy.addAll(sentence_by_comma);
//		//最后按长度过滤短句
//		Collections.sort(sentence_by_comma, new Comparator<String>() {
//			public int compare(String s0, String s1) {
//				double d = s1.length()-s0.length();
//				if (d == 0) {
//					return 0;
//				} else if (d > 0) {
//					return 1;
//				} else {
//					return -1;
//				}
//			}
//		});

		String tempres=sentence_by_comma.get(0)+",";
		int templen=tempres.length();
		for(int i=1;i<sentence_by_comma.size();i++)
		{
			String s=sentence_by_comma.get(i);
			if(templen+s.length()>30)break;
			tempres=tempres+s+",";
			templen+=s.length();
		}
		tempres=tempres.substring(0,tempres.length()-1);
		if(tempres.length()<30)
		{
//			System.out.println("2步过滤："+tempres);
			return removeSymbol(tempres);
		}
		else
		{
			PhraseGenerator pg=new PhraseGenerator();
			ArrayList<WordNode> wordns=pg.generate(tempres,words_filter);
			String maxres="";
			boolean find=false;
			String finalres="";
			for(WordNode wn : wordns)
			{
				String word=wn.getWord();
				if(word.length()>=4)
				{
					finalres=word;
					find=true;
					break;
				}
				else
				{
					if(word.length()>maxres.length())maxres=word;
				}
			}
			if(find==true)
			{
				finalres = finalres.replaceAll("[\\pP‘'“”]", "");
//				System.out.println("3步过滤："+finalres);
				return removeSymbol(finalres);
			}
			else 
			{
				maxres = maxres.replaceAll("[\\pP‘'“”]", "");
//				System.out.println("3步过滤："+maxres);
				return removeSymbol(maxres);
			}
			
		}
}
	public static String extractKeySentence(String text) {
		StringBuilder ansbuilder = new StringBuilder(""); ;
		
//		System.out.println("text:"+text);
		try {
			// 从配置文件获取标题提取python相关文件的位置
			String path = ParamUtil.EXTRACTTITLE_PATH ;
			
			// 写入文件
			File infile = new File(path+"title.txt") ;
			Writer writer = new FileWriter(infile, false) ;
			writer.append(text) ;
			writer.flush();
			writer.close();
			
			// 调用执行，并等待结果产生,process会一直等待python执行结束
			String[] args1 = new String[] { "python", path + "ExtractTitle.py" };
			Process pr = Runtime.getRuntime().exec(args1);
			pr.waitFor();
			
			// 从文件里获取结果
			File outfile = new File(path+"title-ans.txt") ;
			BufferedReader reader = new BufferedReader(new FileReader(outfile)) ;
			String line = "" ;
			while((line = reader.readLine())!=null) {
				ansbuilder.append(line) ;
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ansbuilder.toString();
	}

	
	public static String removeSymbol(String s) {
		Set<String> string_set = new HashSet<String>();
		string_set.add("~");
		string_set.add("`");
		string_set.add("=");
		string_set.add("+");
		string_set.add("|");
		string_set.add("<");
		string_set.add(">");
		string_set.add("￥");
		string_set.add("$");
		string_set.add("^");
		string_set.add("……");
		string_set.add("…");
		if (s.length()<1) {                       //防止报错
			return s;
		}
		String s_front=s.substring(0, 1);         //处理句首
		s_front=s_front.replaceAll("\\p{P}" , "");//去除所有标点符号
		if(string_set.contains(s_front)){         //去除其他符号
			s_front="";
		}
		s=s_front+s.substring(1);

		if (s.length()<1) {
			return s;
		}
		String s_end=s.substring(s.length()-1);
		s_end=s_end.replaceAll("\\p{P}" , "");
		if(string_set.contains(s_end)){
			s_end="";
		}
		s=s.substring(0,s.length()-1)+s_end;
		
		//System.out.println(s);
		return s;
	}
	
	public static void main(String args[])
	{
		String content="近期收到了很多小伙伴的私信，问我旅行青蛙会不会死，是因为有一张被肢解的呱呱图片出来，这篇文章在空间里转发量过千，发文者称采访游戏开发者，然而图片上的人和开发者没任何关系，而是岩井俊二，日本的作家，而这张被肢解的照片也是人为所p，不知道造谣者是何居心？";
		System.out.print(ExtractTitle.extractTitle(content));
	}
}
