package com.ict.mcg.category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import com.ansj.vec.Word2VEC;
import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.util.FileIO;

public class Word2VectorClassifier {
	private static Word2VectorClassifier instance = null; 
	private static String vectorFile = null;
	private static String categoryKeywordsFile = null;
	private Word2VEC w1 = null;
	private int K = 120;
	private int Klei = 10;
	private String[][] key= new String[Klei][K];
	
	private Word2VectorClassifier() {
		
	}
	
	public static void init(String vectorFile, String categoryKeywordsFile) {
		Word2VectorClassifier.vectorFile = vectorFile;
		Word2VectorClassifier.categoryKeywordsFile = categoryKeywordsFile;
	}
	
	public static Word2VectorClassifier getInstance() {
		if (null == instance) {
			if (null == vectorFile) {
				vectorFile = FileIO.getFilePath()+"vectors.bin";
			}
			if (null == categoryKeywordsFile) {
				categoryKeywordsFile = FileIO.getFilePath()+"keyword120.txt";
			}
			instance = new Word2VectorClassifier();
		}
		return instance;
	}
	
	public void init() {
		if (w1 == null) {
			w1 = new Word2VEC() ;
			try {
				w1.loadGoogleModel(vectorFile) ;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(categoryKeywordsFile), "UTF-8"));
//				BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("./file/" + "keyword120.txt")));
				for(int i=0;i<Klei;i++){
		        	br.readLine();
		        	
		    	    String line=br.readLine();
			        for(int j=0;j<K;j++){
			            int index=line.indexOf("\t");
			            String keyword=line.substring(0, index);//第i类的某个关键词
			            line=line.substring(index+1);
			            key[i][j]=keyword;
			        }
		        }
		        br.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public String classify(List<WeiboEntity> weiboList) {
		if (w1 == null) {
			init();
		}
		String[] keywords = ComputeTF.getTopKeywords(weiboList, K);
		double[][] allsimilarity =new double[Klei][K];
        for(int m=0;m<Klei;m++){
        	
        	String word = null;
        	for(int n=0;n<K;n++){//取一个关键词
                word = keywords[n];
                double[] tempsim=new double[K];
                for(int p=0;p<K;p++){//和一个类里所有关键词比较
                	if(w1.getWordVector(word)==null||w1.getWordVector(key[m][p])==null) {
                		continue;
                	} else {
                		tempsim[p] = w1.similarity(word,key[m][p]);
                	}
                }
                double max=tempsim[0];
                for(int q=0;q<K;q++){
                	if(max < tempsim[q]) {
                		max = tempsim[q];
                	}
                }
                allsimilarity[m][n] = max;
            }
        }
        
        double[] finalsim=new double[Klei];
        for(int s=0;s<Klei;s++){
        	for(int t=0;t<K;t++){
        		finalsim[s]+=allsimilarity[s][t];
        	}
        }
        int position=0;
        double maxsim=finalsim[0];
        for(int w=0;w<Klei;w++){
        	if(maxsim<finalsim[w]) {
        		maxsim=finalsim[w];
        		position=w;
        	}
        }
        
        String assort=null;
	    switch(position) {
            case 0:assort="体育";break;
            case 1:assort="健康";break;
            case 2:assort="军事";break;
            case 3:assort="国际";break;
            case 4:assort="娱乐";break;
            case 5:assort="影音";break;
            case 6:assort="时政";break;
            case 7:assort="社会";break;
            case 8:assort="科技";break;
            case 9:assort="财经";break;
	    }
        
        String value = "";
		for (int i = 0; i < 10; ++i) {
			if (position == i) {
				value += "1,";
			} else {
				value += "0,";
			}
		}
		
		return value.substring(0, value.length()-1);
//		return assort;
	}
	
    public static void main(String[] args) throws IOException {
    	Word2VectorClassifier classifier = Word2VectorClassifier.getInstance();
    	classifier.init();
    	String weiboDir = "D:\\user\\zjq\\dataset\\SW-SYSTEM\\newLabeled\\nonrumor_weibos\\";
    	File dir = new File(weiboDir);
    	for (String file:dir.list()) {
    		List<WeiboEntity> weList = FileIO.readWeibo(weiboDir+file, "UTF-8");
    		System.out.print(file.substring(0, file.length()-4)+"\t");
    		System.out.println(classifier.classify(weList));
    	}
    	
    }
}


