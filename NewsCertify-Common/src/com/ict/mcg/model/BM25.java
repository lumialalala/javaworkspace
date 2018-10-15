package com.ict.mcg.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.ict.mcg.util.FileIO;


public class BM25 {
	private static double N = 5200000.0;
	private final static double k1 = 2.0;
	private final static double b = 0.75;
	private final static double Lave = 544;
	private static Map<String, Integer> dfMap = new HashMap<String, Integer>();

	private static String df_file = FileIO.getFilePath() + "ne_df.txt";
	private static String df_file_resource = FileIO.getResourcePath() + "ne_df.txt";
	
	private static boolean isinitial = false;
	
	public static void init(String filePath) throws IOException {
		dfMap.clear();
		df_file = filePath;
		df_file_resource = "";
		loadFile();
	}
	
	public static void loadFile() throws IOException {
		InputStream is = BM25.class.getResourceAsStream(df_file_resource);
		if (null == is) {
			is = new FileInputStream(df_file);
		}
		BufferedReader breader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));
		String line = null;
		while ((line = breader.readLine()) != null) {
			line.trim();
			String[] kv = line.split("\t");
			try{
				dfMap.put(kv[0], Integer.parseInt(kv[1]));
			} catch(Exception e) {
				System.out.println(kv[0]);
			}
		}
		breader.close();
		isinitial = true;
	}
	
//	public static BM25 getInstance(String filePath) {
//		if (instance == null) {
//			try {
//				instance = new BM25(filePath);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//		return instance;
//	}
	
	// TODO: check BM25 formula
	public static double getNEWeight(String word, double tf, double ld) {
		if (!isinitial) {
			try {
				loadFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		double df = 0.0;
		if (dfMap.containsKey(word)) {
			df = dfMap.get(word);
			return Math.log((N+1.0)/(df+1.0)) * ((k1+1)*tf/(tf + (k1*(1-b + b*(Lave/ld)))));
		} else {
			return Math.log((N+1.0)/(df+1.0)) * ((k1+1)*tf/(tf + (k1*(1-b + b*(Lave/ld)))));
		} 
		
	}
	public static double getWeight(String word, double tf, double ld) {
		if (!isinitial) {
			try {
				loadFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		double df = 0.0;
		if (dfMap.containsKey(word)) {
			df = dfMap.get(word);
			return Math.log((N+1.0)/(df+1.0)) * ((k1+1)*tf/(tf + (k1*(1-b + b*(Lave/ld)))));
		} else {
			return 0;
		} 
		
	}
}
