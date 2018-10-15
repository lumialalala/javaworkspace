package com.ict.mcg.veryfication.feature;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

public class GistExtractor implements Callable<Integer> {

	private String imgPath = "";
	private List<String> imgList;
	private String gistFile = "";
	private String tmpPath = "";
	private String imgNameFile = "";
	private String gistCalculatorPath = "";
	
	public GistExtractor(String imgPath, String tmpPath, List<String> imgList, String gistFile, String imgNameFile) {
		// TODO Auto-generated constructor stub
		this.imgPath = imgPath;
		this.tmpPath = tmpPath;
		this.imgList = imgList;
		this.gistFile = gistFile;
		this.imgNameFile = imgNameFile;
	}
	
	public Integer call() throws Exception {
		List<String> cleanImgList = clearNoisyImage();
		generateImgNameFile(cleanImgList, imgNameFile);
		
		String[] cmd = new String[5];
		cmd[0] = gistCalculatorPath;
		cmd[1] = imgNameFile;
		cmd[2] = imgPath;
		cmd[3] = tmpPath;
		cmd[4] = gistFile;
		
		Process pcs = Runtime.getRuntime().exec(cmd);
		InputStreamReader ir = new InputStreamReader(pcs.getInputStream());
		LineNumberReader input = new LineNumberReader(ir);
		String line = null;    
		while ((line = input.readLine()) != null){    
			System.out.println(line);
		}    
		if(null != input){    
			input.close();    
		}

		if(null != ir){    
			ir.close();    
		}
		int extValue = pcs.waitFor();
		
		
		return extValue;
	}
	
	private List<String> clearNoisyImage() {
		List<String> cleanImgList = new ArrayList<String>();
		
		File dirFile = new File(imgPath);
		if (dirFile.isDirectory()) {
			for (String file:imgList) {
				File picFile = new File(imgPath.concat(file));
			    try {
					BufferedImage sourceImg =ImageIO.read(new FileInputStream(picFile));
					//去掉方形的图片
					if (sourceImg.getWidth() == sourceImg.getHeight()) {
						//需要删除么？
						//picFile.delete();
					} else {
						cleanImgList.add(file);
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}
		}
		
		return cleanImgList;
	}
	
	private void generateImgNameFile(List<String> cleanImgList, String file) throws IOException {
		FileWriter writer = new FileWriter(file);
		for (String img:cleanImgList) {
			writer.write(img.concat("\n"));
		}
		writer.close();
	}

}
