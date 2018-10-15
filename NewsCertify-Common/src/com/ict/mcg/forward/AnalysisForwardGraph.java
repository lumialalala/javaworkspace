package com.ict.mcg.forward;
/**
 * @author WuBo
 */
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;

import com.ict.mcg.util.RunTime;


public class AnalysisForwardGraph implements Runnable{
	private List<String[]> mListForwardRelation;
	private List<String[]> mListUserInfo;
	private String mAuthorWmid;
	private String[] mAuthorInfo;
	private String mInputWmid;
	private String mInputName;
	private String mFilePath;
	private CountDownLatch mCountDown;
	public AnalysisForwardGraph(String authorwmid, String[] authorinfo, String inputwmid, String inputname, List<String[]> forwardrelation, List<String[]> userinfo, String filepath, CountDownLatch latch)
	{
		mListForwardRelation = Collections.synchronizedList(new ArrayList<String[]>());
		mListForwardRelation = forwardrelation;
		mListUserInfo = Collections.synchronizedList(new ArrayList<String[]>());
		mListUserInfo = userinfo;
		mAuthorWmid = authorwmid;
		mAuthorInfo = authorinfo;
		mInputWmid = inputwmid;
		mInputName = inputname;
		mFilePath = filepath;
		this.mCountDown = latch;
	}

	public void run()
	{
		RunTime time = new RunTime("Graph Process");
		time.GetStartTime();
		if(mListForwardRelation==null||mListForwardRelation.size()==0)
		{
			System.out.println(">>>>>" + "没有获得可以显示的AnalysisForwardGraph 结果。" );
			mCountDown.countDown();
			return;
		}

		//按照下一层转发排序
		Comparator<String[]> comparator = new Comparator<String[]>(){

			public int compare(String[] o1, String[] o2) {
				// TODO Auto-generated method stub
				int column0=6;
				int column1=9;
				//按照column列排序
				if(!o1[column0].equals(o2[column0])){
					return Integer.valueOf(o2[column0])-Integer.valueOf(o1[column0]);
				}
				else if(!o1[column1].equals(o2[column1])){
					return Integer.valueOf(o2[column1])-Integer.valueOf(o1[column1]);
				}
				return 0;
			}
		};
		Collections.sort(mListForwardRelation,comparator);
		
		List<String> WmidList = new ArrayList<String>();
		WmidList.add(mAuthorWmid);

		List<String> labelList = new ArrayList<String>();
		List<String[]> attList = new ArrayList<String[]>();
		List<double[]> sizeList = new ArrayList<double[]>();
		List<int[]> colorList = new ArrayList<int[]>();
		List<double[]> posList = new ArrayList<double[]>();



		labelList.add(mAuthorInfo[1]);//name
//		String[] rootatt = { mAuthorInfo[0], mAuthorInfo[5], mAuthorInfo[7], mAuthorInfo[8]  };	//uid, fanc, description
		String[] rootatt = {" "," "," "," "};
		attList.add(rootatt);
//		System.out.print("findex "+mAuthorInfo[5]+" ");
		double[] rootsize = {4} ;
		sizeList.add(rootsize);
		int[] rootcolor = {190,125,200};//根节点 紫色
		colorList.add(rootcolor);
		double[] rootpos = {(double) (100 - 200*Math.random()),(double) (100 - 200*Math.random()),0};
		posList.add(rootpos);
		
		Random ran = new Random();
		int hindnum=0;
		boolean filt = false;int per = 8;
		List<String> srclist = Collections.synchronizedList(new ArrayList<String>());
		for(int index = 0;index<mListForwardRelation.size();index++){
			if(Integer.valueOf(mListForwardRelation.get(index)[6])>1000){
				srclist.add(mListForwardRelation.get(index)[3]);
				filt = true;
				per = 4;
			}
		}
		if(mListForwardRelation.size()>3000){
			filt = true;
			per=3;
		}
		
		//fuid list 生成 用于信息
		if (mListUserInfo != null && mListUserInfo.size() > 0) {
			List<String> fuidlist = new ArrayList<String>();
			for(String[] infolist:mListUserInfo){
				fuidlist.add(infolist[0]);
			}
		}
		
		for(int index = 0;index<mListForwardRelation.size();index++)
		{
			String relations[] = mListForwardRelation.get(index);

			WmidList.add(relations[3]);
			int n=1;
			if(filt&&Integer.valueOf(relations[6])==0&& (srclist.contains(relations[0])||mAuthorWmid.equals(relations[0]))){
				int randomnum = ran.nextInt(per);
//				System.out.println("随机数 "+randomnum);
				if (randomnum==0) {
					hindnum++;
					continue;
				}
			}
			
			String username = relations[2];
			while(labelList.contains(username))
			{
				username=relations[2]+"|"+n;
				n++;
			}
			labelList.add(username);
			
			String[] att ={" "," "," "," "};
//			if(Integer.valueOf(relations[6])>0){
//			int findex=fuidlist.indexOf(relations[1]);
//			
//			att[0] = mListUserInfo.get(findex)[0];//uid
//			att[1] =  mListUserInfo.get(findex)[5];	//fanc
//			att[2] =  mListUserInfo.get(findex)[7];	//loc
//			att[3] =  mListUserInfo.get(findex)[8];	//des
//			}
			
			attList.add(att);
			
			String fwmid = relations[3];
			int ffcount = Integer.valueOf(relations[6]);
			//double[] size = {Integer.valueOf(mListForwardRelation.get(index)[6])*20/roottwice};

			if(fwmid.equals(mInputWmid))//输入节点 
			{
				double[] inputsize = {4} ;
				sizeList.add(inputsize);
				int[] inputcolor = {250,128,10};
				colorList.add(inputcolor);
				double[] pos = {(double) (100 ),(double) (100 ),0};
				posList.add(pos);
			}
			else 
			{
				if(ffcount>=5&&index<10)//非叶关键节点
				{
					double[] size = {3.5} ;
					sizeList.add(size);
					int[] color = {240,240,150};
					colorList.add(color);
				}
				else//其他节点
				{
					double[] size = {2} ;
					sizeList.add(size);
					int[] color = {73,214,214};
					colorList.add(color);
				}

				double[] pos = {(double) (100*Math.random()),(double) (100*Math.random()),0};
				posList.add(pos);
			}
		}
		if(hindnum>50)
			System.out.print("隐藏显示的首层叶节点 num "+hindnum+"  ");

//		System.out.println("label list len "+labelList.size());
//		System.out.println("wmid list len "+WmidList.size());
//		Set<String> labelset = new HashSet<String>(labelList); 
//		Set<String> wmidset = new HashSet<String>(WmidList); 
//		System.out.println("label set len "+labelset.size());
//		System.out.println("wmid set "+wmidset.size());

		//		Display.displayArray(labelList,"lablelist");

		List<double[]> edgeList = new ArrayList<double[]>();
		for(int index=0;index<mListForwardRelation.size();index++){
			double[] edge = {(double)WmidList.indexOf(mListForwardRelation.get(index)[0]),(double)WmidList.indexOf(mListForwardRelation.get(index)[3]),0};
			edgeList.add(edge);
		}



		String filepath=mFilePath;
//		System.out.print("Graph filepath："+filepath);
		
		RunTime generatetime = new RunTime("GexfGenerate Process");
		generatetime.GetStartTime();
		try {

			GexfFileGenerator generator;
			generator = new GexfFileGenerator(filepath);
			generator.outputN(labelList, attList, sizeList, colorList, posList);//输出node
			generator.outputE(edgeList);//输出edge
			generator.closeWriter();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		generatetime.GetEndTime();
		double a = generatetime.ComputeRunTime();

		RunTime transfertime = new RunTime("GexfTransfer Process");
		transfertime.GetStartTime();
		GraphComputeWuBo gexftrans = new GraphComputeWuBo();
		gexftrans.transfer(filepath);
		transfertime.GetEndTime();
		double b = transfertime.ComputeRunTime();
		
		time.GetEndTime();
		double s = time.ComputeRunTime();
//		System.out.println("分析之转发图模块 总时间：" + s + "秒，gexf文件生成时间:" + a + "秒，gexf文件转换时间:" + b + "秒。" );
		mCountDown.countDown();
	}

	
}


