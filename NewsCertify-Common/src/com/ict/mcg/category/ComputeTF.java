package com.ict.mcg.category;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import java.util.Set;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.ICTAnalyzer;

/**
 * TF与DF计算
 * @param args
 */

public class ComputeTF {
	ArrayList<ArrayList<HashMap<String,Float>>> list=new ArrayList<ArrayList<HashMap<String,Float>>>();
	ArrayList<HashMap<String,Float>> keywordlist=new ArrayList<HashMap<String,Float>>();
	ArrayList<Float> totalweight=new ArrayList<Float>();
	ArrayList<Integer>	wordcount=new ArrayList<Integer>();
	ArrayList<Integer> weibocount=new ArrayList<Integer>();
	String readpath;
	String savepath;
	String keywordsavepath;
	String keywordsavepath2;
	String geshi;
	int K;
	Logger log = Logger.getRootLogger();
	
	public static String[] getTopKeywords(List<WeiboEntity> weiboList, int k) {
		List<List<String>> tokenList = tokenize(weiboList);
		return getTopTfIdf(tokenList, k);
	}
	
	public static List<List<String>> tokenize(List<WeiboEntity> weiboList) {
		List<List<String>> tokenList = new ArrayList<List<String>>();
		
		for(WeiboEntity w : weiboList){
			ArrayList<String> singlewords = ICTAnalyzer.analyzeParagraph(w.getContent(), 1);
			List<String> tokens = new ArrayList<String>();
			
		    for(String st : singlewords){
				if(st.contains("/")){
				    String[]s=st.split("/");
				    if (s.length < 2) {
				    	continue;
				    }
					
    				if(s[1].indexOf("n")==0||s[1].indexOf("s")==0||s[1].equals("vn")||s[1].equals("vx")||s[1].equals("vi")||
    						s[1].equals("an")||s[1].indexOf("z")==0||s[1].indexOf("y")==0) {
    					if(!s[0].contains("@")&&s[0].length()>1) {
    						tokens.add(s[0]);
    					}
					
					}
				}
		    }
		    
		    tokenList.add(tokens);
		}
		
		return tokenList;
	}
	
	private static String[] getTopTfIdf(List<List<String>> tokenList, int k) {
//		int totalword=0;
//		for (List<String> tokens:tokenList) {
//			totalword += tokens.size();
//		}
		
		Map<String, Float> tfMap = new HashMap<String, Float>(1000);
		Map<String, Float> dfMap = new HashMap<String, Float>(1000);
		for (List<String> tokens:tokenList) {
			Set<String> filterSet = new HashSet<String>();
			for (String token:tokens) {
				if (tfMap.containsKey(token)) {
					tfMap.put(token, tfMap.get(token)+1);
				} else {
					tfMap.put(token, 1.0f);
				}
				
				if (!filterSet.contains(token)) {
					if (dfMap.containsKey(token)) {
						dfMap.put(token, tfMap.get(token)+1);
					} else {
						dfMap.put(token, 1.0f);
					}
					filterSet.add(token);
				}
			}
		}
		
		for (String key : tfMap.keySet()) {
			tfMap.put(key, (float) (tfMap.get(key) * Math.log(tokenList.size() / dfMap.get(key))));
		}
		
		List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(tfMap.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>(){

			public int compare(Entry<String, Float> o1, Entry<String, Float> o2) {
				if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		
		String[] keywords = new String[k];
		for (int i = 0; i < k; ++i) {
			if (i < entryList.size()) {
				keywords[i] = entryList.get(i).getKey();
			} else {
				keywords[i] = entryList.get(entryList.size()-1).getKey();
			}
		}
		
		return keywords;
		
	}
	
	
	
	
	
	 //计算TF-IDF
	public void gettfidf() throws IOException{	
		 Set<String> stopList = new HashSet<String>();
//		 BufferedReader brr = new BufferedReader(new InputStreamReader(new FileInputStream("D:\\user\\zjq\\dataset\\category\\stopword2.txt"), "UTF-8"));
//		 String word = null;
//		 while ((word = brr.readLine()) != null) {
//		     stopList.add(word);
//		 }
//		 brr.close();
			
		 ArrayList<String> readpathlist=ReadFileName.readfile(savepath);
		 for(int i=0;i<readpathlist.size();i++){//i表示第几个文件
			 wordcount.clear();
			String completesavepath=savepath+"\\"+readpathlist.get(i);
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 			BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 			String line;
 			int ii=0;//第ii条微博
 			int kk=0;//第kk条微博
 			
 			//得到某类的微博条数，各微博词数，微博词汇表
 			int totalword=0;
 	 		ArrayList<String> temparray=new ArrayList<String>();
 	 		while((line=br.readLine())!=null){
 	 			int t=0;
 	 	 		 while(line.length()!=0 && !stopList.contains(line)){
 	 	 		     ++t;
 	 	 		    temparray.add(line);
 	 	 			line=br.readLine();
 	 	 		 }
 	 	 		 ++kk;
 	 	 		 wordcount.add(t);
 	 	 	}
 	 		weibocount.add(kk);
 	 		br.close();
 	 		
 	 	    //计算该类总词数
 	 		for(int j=0;j<weibocount.get(i);j++){
 	 			totalword+=wordcount.get(j);
 	 		}
 	 		
 	 		//初始化list
 	 		ArrayList<HashMap<String,Float>> temparr= new ArrayList<HashMap<String,Float>>();
 	 		for(int m=0;m<weibocount.get(i);m++){
 	 			HashMap<String,Float> temphash=new HashMap<String,Float>();
 	 				temparr.add(temphash);	
 	 		}
 	 		list.add(i,temparr);
 	 		temparr=null;
 	 		
 	 		//放词进list
 	 		while((line=br2.readLine())!=null){
 	 	 		 while(line.length()!=0 && !stopList.contains(line)){
 	 	 			 try{
 	 	 					list.get(i).get(ii).put(line,(float)0);
 	 	 			        line=br2.readLine();
 	 	 			 } catch(Exception e) {
 	 	 				 log.error(line, e);
 	 	 				 System.exit(1);
 	 	 			 }
 	 	 			
 	 	 		 }
 	 	 		 ++ii;
 	 		}
 	 	 	br2.close();
 	 	 		
 	 	 	//计算tf
 	 		ArrayList<String> tempa=new ArrayList<String>();
 	 		tempa.addAll(temparray);
 	 		HashMap<String,Float> te= new HashMap<String,Float>();
 			for(String str: temparray){
				int ct=0;
				while (tempa.remove(str))
				{
					++ct;
					te.put(str,(float)ct);
				}
 			}
 			
 			//计算tf-idf
 			for(String linee : temparray){
 	 			int times=0;
 	 			 for(int j=0;j<wordcount.size();j++){
 	 				if(list.get(i).get(j).containsKey(linee)){
 	 					++times;
 	 				}
 	 			}
 	 			for(int j=0;j<wordcount.size();j++){
 	 				try {
 	 				if(list.get(i).get(j).containsKey(linee)){
 	 				float temp=te.get(linee);
 	 				temp*=Math.log((float)wordcount.size()/times);				
 	 				list.get(i).get(j).put(linee,temp);
 	 				}
 	 				} catch (Exception e) {
 	 					log.error(linee, e);
 	 					System.exit(1);
 	 				}
 	 			 }
 	 		 }

	 		ArrayList<String> keyword =new ArrayList<String>();
	 		ArrayList<Float> weight=new ArrayList<Float>();
	 		HashMap<String,Float> keyweight=new HashMap<String,Float>();
	 		keyword=getKkeywords(weibocount.get(i),(ArrayList<HashMap<String,Float>>)list.get(i),temparray, weight);
	 		for(int n=0;n<keyword.size();n++){
	 			keyweight.put(keyword.get(n),weight.get(n));
	 		}
	 		keywordlist.add(keyweight);
	 		float allweight=0;
	 		for(int p=0;p<K;p++){
	 			allweight+=weight.get(p);
	 		}
	 		totalweight.add(allweight);
			BufferedWriter bw = new BufferedWriter(new FileWriter(keywordsavepath,true));
			bw.write(readpathlist.get(i).substring(0,readpathlist.get(i).indexOf(".")));
			bw.write("\n");
			for(int n=0;n<keyword.size();n++){
				String linee=keyword.get(n);
				bw.write(linee+"\t");
			}
			bw.write("\n");
			bw.close();
	 		temparray.clear();
		 }
	 }
	//提取前k个关键词  
	 ArrayList<String> getKkeywords(int count,ArrayList<HashMap<String,Float>> Ilist,ArrayList<String> temparray,ArrayList<Float> weight)
	 {
		 ArrayList<String> keyword =new ArrayList<String>();
	     HashMap<String,Float> hm= new HashMap<String,Float>();
	     for(int j=0;j<count;j++){
	    	 hm.putAll(Ilist.get(j));
	     }
	     for(int m=0;m<K;m++)
	     {
	    	 String max=temparray.get(0);
	    	 float maxvalue=0;
		     for (int k=0;k<temparray.size();k++)
		     {
		    	String wordtemp =temparray.get(k);
		    	if(hm.get(max)<hm.get(wordtemp))
		    	{
		    		max=wordtemp;
		    		maxvalue=hm.get(wordtemp);
		    	}
		     }
		     keyword.add(max);
		     weight.add(maxvalue);
		     hm.put(max, (float)-1.0);
	     }
	     hm.clear();
		return keyword;
	 }
	 //计算tf
	 public void getfrequency() throws IOException{	
		 ArrayList<String> readpathlist=ReadFileName.readfile(savepath);
		 for(int i=0;i<readpathlist.size();i++){
			String completesavepath=savepath+"\\"+readpathlist.get(i);
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 			String line;
 			//得到某类的微博词汇表
 	 		ArrayList<String> temparray=new ArrayList<String>();
 	 		
 	 		while((line=br.readLine())!=null){
 	 	 		 while(line.length()!=0){
 	 	 		    temparray.add(line);
 	 	 			line=br.readLine();
 	 	 		 }
 	 	 		 }

 	 		br.close();
 	 		
 	 		ArrayList<Integer> count=new ArrayList<Integer>();
 	 		ArrayList<String> tempa=new ArrayList<String>();
 	 		tempa.addAll(temparray);
 			for(String str: temparray){
				int ct=0;
				while (tempa.remove(str))
				{
					++ct;
			}
				count.add(ct);
		}
	
		for(int k=0;k<K;k++)
		{
			int max=count.get(0);
			int position=0;
			for(int j=0;j<count.size();j++)
			{
	
				if(max<count.get(j))
				{
					max=count.get(j);
					position=j;
				}
			}
			count.set(position, 0);
		}
		 }
		 
	 }
	 //计算df
	 public void getdf() throws IOException{	
		 ArrayList<String> readpathlist=ReadFileName.readfile(savepath);
		 for(int i=0;i<readpathlist.size();i++){
			String completesavepath=savepath+"\\"+readpathlist.get(i);
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 			BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 
 			String line;
 			int ii=0;//第ii条微博
 			int kk=0;//第kk条微博
 			//得到某类的微博条数，各微博词数，微博词汇表
 	 		ArrayList<String> temparray=new ArrayList<String>();

 	 		while((line=br.readLine())!=null){
 	 			int t=0;
 	 	 		 while(line.length()!=0){
 	 	 		     ++t;
 	 	 		    temparray.add(line);
 	 	 			line=br.readLine();
 	 	 		 }
 	 	 		 wordcount.add(t);
 	 	 		 ++kk;	 	 		
 	 	 		 }
 	 		weibocount.add(kk);
 	 		br.close();
 	 		
 	 		ArrayList<HashMap<String,Float>> temparr= new ArrayList<HashMap<String,Float>>();
 	 		for(int m=0;m<weibocount.get(i);m++){
 	 			HashMap<String,Float> temphash=new HashMap<String,Float>();
 	 				temparr.add(temphash);	
 	 		}
 	 		list.add(i,temparr);
 	 		temparr=null;
 	 		
 	 		//放词进list
 	 		while((line=br2.readLine())!=null){
 	 	 		 while(line.length()!=0){
 	 	 			 try{
 	 	 					list.get(i).get(ii).put(line,(float)0);
 	 	 			        line=br2.readLine();
 	 	 			 } catch(Exception e) {
 	 	 				 log.error(line, e);
 	 	 				 System.exit(1);
 	 	 			 }
 	 	 			
 	 	 		 }
 	 	 		 ++ii;
 	 			 }
 	 	 		br2.close();
 	 	 		
 			//计算df
 		for(String linee : temparray){
 	 			int times=0;
 	 			 for(int j=0;j<wordcount.size();j++){
 	 				if(list.get(i).get(j).containsKey(linee)){
 	 					++times;
 	 				}
 	 				}
 	 			for(int j=0;j<wordcount.size();j++){
 	 				try {
 	 				if(list.get(i).get(j).containsKey(linee)){
 	 				float temp=times;			
 	 				list.get(i).get(j).put(linee,temp);
 	 				}
 	 				} catch (Exception e) {
 	 					log.error(linee, e);
 	 					System.exit(1);
 	 				}
 	 			 }
 	 		 }

 		ArrayList<String> keyword =new ArrayList<String>();
 		ArrayList<Float> weight=new ArrayList<Float>();
 		HashMap<String,Float> keyweight=new HashMap<String,Float>();
 		keyword=getKkeywords(weibocount.get(i),(ArrayList<HashMap<String,Float>>)list.get(i),temparray, weight);
 		for(int n=0;n<keyword.size();n++){
 			keyweight.put(keyword.get(n),weight.get(n));
 		}
 		keywordlist.add(keyweight);
 		float allweight=0;
 		for(int p=0;p<K;p++){
 			allweight+=weight.get(p);
 		}
 		totalweight.add(allweight);
		BufferedWriter bw = new BufferedWriter(new FileWriter(keywordsavepath,true));
		bw.write(readpathlist.get(i).substring(0,readpathlist.get(i).indexOf(".")));
		bw.write("\n");
		for(int n=0;n<keyword.size();n++){
			String linee=keyword.get(n);
			bw.write(linee+"\t");
		}
		bw.write("\n");
		bw.close();
 		temparray.clear();
		 }
	 }
	 
	 //计算TF-DF
	 public void gettfdf() throws IOException{
		 ArrayList<String> readpathlist=ReadFileName.readfile(savepath);
		 for(int i=0;i<readpathlist.size();i++){
			String completesavepath=savepath+"\\"+readpathlist.get(i);
			//String completesavepath=savepath;
 			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 			BufferedReader br2 = new BufferedReader(new InputStreamReader(new FileInputStream(completesavepath)));
 			String line;
 			int ii=0;//第ii条微博
 			int kk=0;//第kk条微博
 			//得到某类的微博条数，各微博词数，微博词汇表
 	 		ArrayList<String> temparray=new ArrayList<String>();

 	 		while((line=br.readLine())!=null){
 	 			int t=0;
 	 	 		 while(line.length()!=0){
 	 	 		    ++t;
 	 	 		    temparray.add(line);
 	 	 			line=br.readLine();
 	 	 		 }
 	 	 		 ++kk;
 	 	 		 wordcount.add(t);
 	 	 		 
 	 	 		 }
 	 		weibocount.add(kk);
 	 		br.close();

 	 		ArrayList<HashMap<String,Float>> temparr= new ArrayList<HashMap<String,Float>>();
 	 		for(int m=0;m<weibocount.get(i);m++){
 	 			HashMap<String,Float> temphash=new HashMap<String,Float>();
// 	 				temphash.put("a", -1);
 	 				temparr.add(temphash);	
 	 		}
 	 		list.add(i,temparr);
 	 		temparr=null;
 	 		//放词进list
 	 		while((line=br2.readLine())!=null){
 	 	 		 while(line.length()!=0){
 	 	 			 try{
 	 	 					list.get(i).get(ii).put(line,(float)0);
 	 	 			        line=br2.readLine();
 	 	 			 } catch(Exception e) {
 	 	 				 log.error(line, e);
 	 	 				 System.exit(1);
 	 	 			 }
 	 	 			
 	 	 		 }
 	 	 		 ++ii;
 	 			 }
 	 	 		br2.close();
 	 	 		
 	 	 		//计算tf
 	 		ArrayList<String> tempa=new ArrayList<String>();
 	 		tempa.addAll(temparray);
 	 		HashMap<String,Float> te= new HashMap<String,Float>();
 			for(String str: temparray){
				int ct=0;
				while (tempa.remove(str))
				{
					++ct;
					te.put(str,(float)ct);
				}
		}
 			//计算tf-df
 		for(String linee : temparray){
 	 			int times=0;
 	 			 for(int j=0;j<wordcount.size();j++){
 	 				if(list.get(i).get(j).containsKey(linee)){
 	 					++times;
 	 				}
 	 				}
 	 			for(int j=0;j<wordcount.size();j++){
 	 				try {
 	 				if(list.get(i).get(j).containsKey(linee)){
 	 				float temp=te.get(linee);
 	 				temp*=times;				
 	 				list.get(i).get(j).put(linee,temp);
 	 				}
 	 				} catch (Exception e) {
 	 					log.error(linee, e);
 	 					System.exit(1);
 	 				}
 	 			 }
 	 		 }

 		ArrayList<String> keyword =new ArrayList<String>();
 		ArrayList<Float> weight=new ArrayList<Float>();
 		HashMap<String,Float> keyweight=new HashMap<String,Float>();
 		keyword=getKkeywords(weibocount.get(i),(ArrayList<HashMap<String,Float>>)list.get(i),temparray, weight);
 		for(int n=0;n<keyword.size();n++){
 			keyweight.put(keyword.get(n),weight.get(n));
 		}
 		keywordlist.add(keyweight);
 		float allweight=0;
 		for(int p=0;p<K;p++){
 			allweight+=weight.get(p);
 		}
 		totalweight.add(allweight);
		BufferedWriter bw = new BufferedWriter(new FileWriter(keywordsavepath,true));
		bw.write(readpathlist.get(i).substring(0,readpathlist.get(i).indexOf(".")));
		bw.write("\n");
		for(int n=0;n<keyword.size();n++){
			String linee=keyword.get(n);
			bw.write(linee+"\t");
		}
		bw.write("\n");
		bw.close();
 		temparray.clear();
		 }
	 }
	 

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ComputeTF example= new ComputeTF();
		ArrayList<Integer>	wordcount=new ArrayList<Integer>();
		ArrayList<Integer> weibocount=new ArrayList<Integer>();
		
		example.readpath="E:\\社会补充";
		example.savepath="E:\\社会补充words";
		example.keywordsavepath="E:\\test\\keyword\\keyword.txt";
		example.geshi="utf-8";
	//	example.keywordsavepath2="E:\\keyword\\按tf-idf2.txt";
		example.K=80;
	//	example.dividwords();
	//	example.gettfidf();
	//	example.getfrequency();
	//	example.getdf();
	//	example.gettfdf();
	}

}

class ReadFileName {
	 /**
	  * 获取某个文件夹下的所有文件名
	  */
   public static ArrayList<String> readfile(String filepath){
   	ArrayList<String> filelist = new ArrayList<String>() ;
                   File file = new File(filepath);
                   if (!file.isDirectory()) {
                	   Logger log = Logger.getRootLogger();
                       log.warn("文件夹不存在");

                   } else if (file.isDirectory()) {
                           String[] fileset =file.list();
                           for(int i=0;i<fileset.length;i++)
                           {
                           filelist.add(fileset[i]);	
                           }
                           }
            return filelist;
                   }            
   }

