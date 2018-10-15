package com.ict.mcg.forward;
/*
 * 
 * author: ICT wubo v1.8
 * date:2012.11.12
 * 
 * */ 


import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ict.mcg.processs.ICTAnalyzer;


public class WeiboTextAnalysis {  

	public List<String> Analysis(String text) throws IOException {  

		List<String> wordlist = new LinkedList<String>();


		if(text == null)
		{
			System.out.println("Can't get text");
			return null;
		}


		/*
		 * analysis weibotext data
		 * */


		String weibotext = text;
		Pattern pattern ;
		Matcher matcher ;
		String result = "";


		/*
		 * <>message
		 * */
		pattern = Pattern.compile("<([^>]*)>");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String labeltext = "";     
		while(matcher.find()){
			labeltext = matcher.group();
			result += " " + labeltext;
		} 
		//write at message in the data file
		weibotext = matcher.replaceAll("");


		/*
		 * link message
		 * */

		//transfer link
		weibotext = weibotext.replaceAll("\\\\/", "/");
		pattern = Pattern.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
		matcher = pattern.matcher(weibotext);



		result = "";
		String linktext = "";     


		while(matcher.find()){
			linktext = matcher.group();
			result += " " + linktext;
		} 
		//write link message in the data file
		weibotext = matcher.replaceAll("");


		/*
		 * @message
		 * */
		pattern = Pattern.compile("@[^\\.^\\,^:^;^!^\\?^\\s^#^@^。^，^：^；^！^？]+");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String attext = "";     
		while(matcher.find()){
			attext = matcher.group();
			result += " " + attext;
		} 
		//write at message in the data file
		weibotext = matcher.replaceAll("");


		/*
		 * #message
		 * */
		pattern = Pattern.compile("#([^\\#|.]+)#");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String topictext = "";     
		while(matcher.find()){
			topictext = matcher.group();
			result += " " + topictext;
		}  
		//write topic message in the data file
		weibotext = matcher.replaceAll("");


		/*
		 * ICTCLAS
		 * */

		//		weibotext=weibotext.replace("时候", "");
		//		weibotext=weibotext.replace("朋友", "");
		//		weibotext=weibotext.replace("问题", "");		
		//		weibotext=weibotext.replace("喜欢", "");		
		//		weibotext=weibotext.replace("感觉", "");	
		//		weibotext=weibotext.replace("中国", "");	
		//		weibotext=weibotext.replace("世界", "");	
		//weibotext=weibotext.replace("?,", "");	
		//weibotext=weibotext.replace("??", "");	
		//weibotext=weibotext.replace("?.", "");	
		//weibotext=weibotext.replace("?!", "");	
		//weibotext=weibotext.replace("??", "");	


		ArrayList<String> list = ICTAnalyzer.analyzeParagraph(weibotext, ICTAnalyzer.POS_YES);
		ArrayList<String> rst = new ArrayList<String>();
		for(String str: list){
			int index = str.indexOf("/");
			if(index > 0){
				String pos = str.substring(index+1, str.length());
				if(isKeep(pos)){
					rst.add(str);
				}
			}
		}

		for(int i=0;i<rst.size();i++){  
			String word = rst.get(i);
			word=word.substring(0, word.indexOf("/"));
			if((word.length()<=1&&word.matches("[0-9\\u4e00-\\u9fa5]*"))||word.matches("[0-9]*"))
				continue;
			if(word.length()<=3&&word.matches("[a-zA-Z]|[a-z0-9A-Z]*"))
				continue;
			String regEx = "[\u4e00-\u9fa5]";   
			Pattern pat = Pattern.compile(regEx); 
			Matcher matcher1 = pat.matcher(word);     
			if (matcher1.find())    {    
				//if(word.getBytes().length==word.length())

				result += " " + word;
				wordlist.add(word);
//				System.out.println(word);
			}
		}


		/*
		 * IK
		 * */


		//result = "";
		//StringReader sr=new StringReader(weibotext);  
		//IKSegmenter ik=new IKSegmenter(sr, true);  
		//Lexeme lex=null;


		/*	while((lex=ik.next())!=null){  
			String word = lex.getLexemeText();
			if((word.length()<=1&&word.matches("[0-9\\u4e00-\\u9fa5]*"))||word.matches("[0-9]*"))
				continue;
			if(word.length()<=3&&word.matches("[a-zA-Z]|[a-z0-9A-Z]*"))
				continue;
			result += " " + word;
			wordlist.add(word);
		} 
		 */				
//		System.out.println("wordlist: "+wordlist);
		//	System.out.println("wordlist size:　"+wordlist.size());
		
		if(result != ""){
			return wordlist;
		}else{
			return null;
		}
	}

	public List<String> SimpleAnalysis(String text) throws IOException {  

		List<String> wordlist = new LinkedList<String>();


		if(text == null)
		{
			System.out.println("Can't get text");
			return null;
		}


		/*
		 * analysis weibotext data
		 * */


		String weibotext = text;
		Pattern pattern ;
		Matcher matcher ;
		String result = "";


		/*
		 * @message
		 * */
//		pattern = Pattern.compile("@[^\\.^\\,^:^;^!^\\?^\\s^#^@^。^，^：^；^！^？]+");
//		matcher = pattern.matcher(weibotext); 
//
//		result = "";
//		String attext = "";     
//		while(matcher.find()){
//			attext = matcher.group();
//			result += " " + attext;
//			wordlist.add(attext);
//		} 
//		//write at message in the data file
//		weibotext = matcher.replaceAll("");


		/*
		 * #message
		 * */
		pattern = Pattern.compile("#([^\\#|.]+)#");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String topictext = "";     
		while(matcher.find()){
			topictext = matcher.group();
			result += " " + topictext;
			wordlist.add(topictext);
		}  
		//write topic message in the data file
		weibotext = matcher.replaceAll("");


		/*
		 * ICTCLAS
		 * */

		//		weibotext=weibotext.replace("时候", "");
		//		weibotext=weibotext.replace("朋友", "");
		//		weibotext=weibotext.replace("问题", "");		
		//		weibotext=weibotext.replace("喜欢", "");		
		//		weibotext=weibotext.replace("感觉", "");	
		//		weibotext=weibotext.replace("中国", "");	
		//		weibotext=weibotext.replace("世界", "");	
		//weibotext=weibotext.replace("?,", "");	
		//weibotext=weibotext.replace("??", "");	
		//weibotext=weibotext.replace("?.", "");	
		//weibotext=weibotext.replace("?!", "");	
		//weibotext=weibotext.replace("??", "");	


		ArrayList<String> list = ICTAnalyzer.analyzeParagraph(weibotext, ICTAnalyzer.POS_YES);
		ArrayList<String> rst = new ArrayList<String>();
		for(String str: list){
			int index = str.indexOf("/");
			if(index > 0){
				String pos = str.substring(index+1, str.length());
				if(isKeep(pos)){
					rst.add(str);
				}
			}
		}

		for(int i=0;i<rst.size();i++){  
			String word = rst.get(i);
			word=word.substring(0, word.indexOf("/"));
			if((word.length()<=1&&word.matches("[0-9\\u4e00-\\u9fa5]*"))||word.matches("[0-9]*"))
				continue;
			if(word.length()<=3&&word.matches("[a-zA-Z]|[a-z0-9A-Z]*"))
				continue;
			String regEx = "[\u4e00-\u9fa5]";   
			Pattern pat = Pattern.compile(regEx); 
			Matcher matcher1 = pat.matcher(word);     
			if (matcher1.find())    {    
				//if(word.getBytes().length==word.length())

				result += " " + word;
				wordlist.add(word);
//				System.out.println(word);
			}
		}

		if(wordlist.size() >0){
			return wordlist;
		}else{
			return null;
		}
	}

	
	private boolean isKeep(String pos) {
		boolean keep = false;
		if (pos.length() > 0) {
			if (pos.charAt(0) == 'n' || pos.charAt(0) == 'N'|| pos.charAt(0) == 'a') {
				keep = true;
			} else if (pos.equalsIgnoreCase("vn")) {
				keep = true;
			} 
//			else if (pos.equalsIgnoreCase("vi")) {
//				keep = true;
//			}
		}

		return keep;
	}

	public String Filter(String text) throws IOException {  

		if(text == null)
		{
			System.out.println("Can't get text");
			return null;
		}


		/*
		 * analysis weibotext data
		 * */


		String weibotext = text;
		Pattern pattern ;
		Matcher matcher ;
		String result = "";


		/*
		 * <>message
		 * */
		pattern = Pattern.compile("<([^>]*)>");
		matcher = pattern.matcher(weibotext); 

		result = "";
		String labeltext = "";     
		while(matcher.find()){
			labeltext = matcher.group();
			result += " " + labeltext;
		} 
		//write at message in the data file

		weibotext = matcher.replaceAll("");


		/*
		 * link message
		 * */

		//transfer link
		weibotext = weibotext.replaceAll("\\\\/", "/");
		pattern = Pattern.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
		matcher = pattern.matcher(weibotext);



		result = "";
		String linktext = "";     


		while(matcher.find()){
			linktext = matcher.group();
			result += " " + linktext;
		} 
		//write link message in the data file

		weibotext = matcher.replaceAll("");

		if(weibotext != ""){
			return weibotext;
		}else{
			return null;
		}


	}
}  
