/**
 * 
 */
package com.ict.mcg.processs;

import java.util.ArrayList;
import java.nio.charset.Charset;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.gather.entity.WeiboEntityWrapper;
import com.csvreader.CsvReader;
/**
 * 提供相似度、聚类等算法
 * @author JZW
 *
 */
public class Cluster {
	/**
	 * 将微博单遍聚类
	 * 
	 * @param allWeibo
	 *            所有微博
	 * @param minSim
	 *            最小相似度
	 * @return
	 */
	public ArrayList<ArrayList<WeiboEntity>> singlePassCluster(
			ArrayList<WeiboEntity> allWeibo, double minSim) {
		ArrayList<ArrayList<WeiboEntity>> clusters = new ArrayList<ArrayList<WeiboEntity>>();
		if(allWeibo.size()==0)
			return clusters;

		// 1.随机选择一个元素进行初始化形成一个类
		//2013-11-27 改为选取第一个元素作为初始类
		int rand = 0;
		ArrayList<WeiboEntity> inicluster = new ArrayList<WeiboEntity>();
		inicluster.add(allWeibo.get(rand));
		clusters.add(inicluster);

		// 2.遍历剩余每一个元素进行聚类
		for (int k = 1; k < allWeibo.size(); ++k) {
			WeiboEntity we = allWeibo.get(k);
			// 3. 计算该元素与已有聚类的相似度
			double sim = 0;
			int cindex = -1;
			for (int i = 0; i < clusters.size(); i++) {
				ArrayList<WeiboEntity> c = clusters.get(i);
				double s = match(c, we);
				if (s >= minSim && s > sim) {
					sim = s;
					cindex = i;
				}
			}
			if (cindex >= 0) {
				// 3.1 将该元素指定给最相近的类
				clusters.get(cindex).add(we);
			} else {
				// 3.2 形成新的类
				ArrayList<WeiboEntity> newcluster = new ArrayList<WeiboEntity>();
				newcluster.add(we);
				clusters.add(newcluster);
			}
		}
		
		//4.对较小的类(小于等于2个)再次聚类
		//4.1 分开较小聚类和已经聚好的大类
		ArrayList<WeiboEntity> rest = new ArrayList<WeiboEntity>();
		ArrayList<ArrayList<WeiboEntity>> newclusters = new ArrayList<ArrayList<WeiboEntity>>();
		double newMinSim = minSim-0.01;//降低最小相似度限制
		for(ArrayList<WeiboEntity> wel:clusters){
			if(wel.size()<=2)
				rest.addAll(wel);
			else
				newclusters.add(wel);
		}
		for (WeiboEntity we : rest) {
			// 4.2. 计算该元素与已有聚类的相似度
			double sim = 0;
			int cindex = -1;
			for (int i = 0; i < newclusters.size(); i++) {
				ArrayList<WeiboEntity> c = newclusters.get(i);
				double s = match(c, we);
				if (s >= newMinSim && s > sim) {
					sim = s;
					cindex = i;
				}
			}
			if (cindex >= 0) {
				newclusters.get(cindex).add(we);
			} else {
				ArrayList<WeiboEntity> newcluster = new ArrayList<WeiboEntity>();
				newcluster.add(we);
				newclusters.add(newcluster);
			}
		}
		
		return newclusters;
	}
	
	/**
	 * 计算一条微博与一组微博的相似度
	 * 
	 * @param c
	 * @param we
	 * @return
	 */
	private double match(ArrayList<WeiboEntity> c, WeiboEntity we) {
		// 采用平均相似度（采用最大/最小相似度相比，哪种方法更好呢？）
		double sum = 0;
		for (WeiboEntity w : c) {
			sum += this.match(w.getSegs(), we.getSegs());
		}
		return sum / c.size();
	}
	
	/**
	 * 计算一条微博与一组微博的相似度
	 * 
	 * @param c
	 * @param we
	 * @return
	 */
	private double matchVTwo(ArrayList<WeiboEntityWrapper> c, WeiboEntityWrapper we) {
		// 采用平均相似度（采用最大/最小相似度相比，哪种方法更好呢？）
		double sum = 0;
		for (WeiboEntityWrapper w : c) {
			sum += this.match(w.getSegs(), we.getSegs());
		}
		return sum / c.size();
	}
	
	/**
	 * 计算两组关键词匹配度
	 * 
	 * @param list1
	 * @param list2
	 * @return
	 */
	public double match(ArrayList<WordNode> list1, ArrayList<WordNode> list2) {
		int corr = 0;
		for (WordNode w1 : list1) {
			for(WordNode w2 :list2){
				if(w2.getWord().equals(w1.getWord()))
					corr++;
			}
		}
		return (2 * (double) corr) / (list1.size() + list2.size());
	}
	
	public ArrayList<ArrayList<WeiboEntityWrapper>> singlePassClusterVTwo(
			ArrayList<WeiboEntityWrapper> allWeibo, double minSim) {
		ArrayList<ArrayList<WeiboEntityWrapper>> clusters = new ArrayList<ArrayList<WeiboEntityWrapper>>();
		if(allWeibo.size()==0)
			return clusters;

		// 1.随机选择一个元素进行初始化形成一个类
		//2013-11-27 改为选取第一个元素作为初始类
//		int count = allWeibo.size();
//		int rand = (int) (Math.random() * count);
		int rand = 0;
		ArrayList<WeiboEntityWrapper> inicluster = new ArrayList<WeiboEntityWrapper>();
		inicluster.add(allWeibo.get(rand));
		clusters.add(inicluster);

		// 2.遍历剩余每一个元素进行聚类
		allWeibo.remove(rand);
		for (WeiboEntityWrapper we : allWeibo) {
			// 3. 计算该元素与已有聚类的相似度
			double sim = 0;
			int cindex = -1;
			System.out.println("clusters.size():");
			System.out.println(clusters.size());
			for (int i = 0; i < clusters.size(); i++) {
				ArrayList<WeiboEntityWrapper> c = clusters.get(i);
				double s = matchVTwo(c, we);
				if (s >= minSim && s > sim) {
					sim = s;
					cindex = i;
				}
			}
			if (cindex >= 0) {
				// 3.1 将该元素指定给最相近的类
				clusters.get(cindex).add(we);
			} else {
				// 3.2 形成新的类
				ArrayList<WeiboEntityWrapper> newcluster = new ArrayList<WeiboEntityWrapper>();
				newcluster.add(we);
				clusters.add(newcluster);
			}
		}
		
		//4.对较小的类(小于等于2个)再次聚类
		//4.1 分开较小聚类和已经聚好的大类
		ArrayList<WeiboEntityWrapper> rest = new ArrayList<WeiboEntityWrapper>();
		ArrayList<ArrayList<WeiboEntityWrapper>> newclusters = new ArrayList<ArrayList<WeiboEntityWrapper>>();
		double newMinSim = minSim-0.01;//降低最小相似度限制
		for(ArrayList<WeiboEntityWrapper> wel:clusters){
			if(wel.size()<=2)
				rest.addAll(wel);
			else
				newclusters.add(wel);
		}
		for (WeiboEntityWrapper we : rest) {
			// 4.2. 计算该元素与已有聚类的相似度
			double sim = 0;
			int cindex = -1;
			for (int i = 0; i < newclusters.size(); i++) {
				ArrayList<WeiboEntityWrapper> c = newclusters.get(i);
				double s = matchVTwo(c, we);
				if (s >= newMinSim && s > sim) {
					sim = s;
					cindex = i;
				}
			}
			if (cindex >= 0) {
				newclusters.get(cindex).add(we);
			} else {
				ArrayList<WeiboEntityWrapper> newcluster = new ArrayList<WeiboEntityWrapper>();
				newcluster.add(we);
				newclusters.add(newcluster);
			}
		}
		
		return newclusters;
	}
	public static ArrayList<WeiboEntity> readCsvFile(String filePath){
		ArrayList<WeiboEntity> allWeibo = new ArrayList<WeiboEntity>();
		try {
            ArrayList<String[]> csvList = new ArrayList<String[]>(); 
            CsvReader reader = new CsvReader(filePath,',',Charset.forName("UTF-8"));
  //          reader.readHeaders(); //跳过表头,不跳可以注释掉

            while(reader.readRecord()){
                csvList.add(reader.getValues()); //按行读取，并把每一行的数据添加到list集合
            }
            reader.close();
            System.out.println("读取的行数："+csvList.size());
            	
            for(int row=0;row<csvList.size();row++){
            	String mid = csvList.get(row)[1];
            	String url = csvList.get(row)[2];
				String time = csvList.get(row)[7];
				String forword = csvList.get(row)[4];
				String comment = csvList.get(row)[5];
				String content = csvList.get(row)[10];
				String praise = csvList.get(row)[6];
				//System.out.println(content);
                WeiboEntity temp = new WeiboEntity(mid, url, praise, time, forword, comment, content);
                allWeibo.add(temp);
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allWeibo;
	}

	public static void main(String [] args) throws IOException {
		ArrayList<WeiboEntity> allWeibo = new ArrayList<WeiboEntity>();
		// 读入文件并添加到allweibo中
		String file_path = "D:/pythonworkspace/weibo/data/file_news.csv";
		allWeibo = readCsvFile(file_path);		
		for (WeiboEntity we : allWeibo) {
			String content = we.getContent();
			ArrayList<WordNode> segs = WeiboEntityProcessor.getSegments(content);
			
			we.setSegs(segs);
		}
		/*
		ArrayList<WordNode> segs = WeiboEntityProcessor
				.getSegments("#时评#【用最严筑牢食品药品安全防线】8月16日中共中央政治局常务委员会召开会议听取关于>吉林长春长生公司问题疫苗案件调查及有关问责情况的汇报并对相关责任人进行了严肃问责问责层次之高、力度之大让人民群众\n" + 
						"看到了党中央维护食品药品安全、保障人民群众切身利益的坚定决心");
		for(WordNode seg: segs) {
			System.out.print(seg.getWord() + seg.getPos() + " ");
		}*/
		ArrayList<ArrayList<WeiboEntity>> clusters = new Cluster().singlePassCluster(allWeibo, 0.3);
		File f =new File("D:/pythonworkspace/weibo/data/news_cluster.csv");
		PrintWriter output = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f),"UTF-8")));

		output.write("content_id,content,cluster_index\n");	
		String clusters_size = Integer.toString(clusters.size());
		System.out.println("聚类的数目："+ clusters_size);
		for(int row=0;row<clusters.size();row++){
			System.out.println(Integer.toString(row));
			for (int index = 0; index< clusters.get(row).size();index++) {
				String mid = clusters.get(row).get(index).getMid();
				//String url = clusters.get(row).get(index).getUrl();
				//String praise = clusters.get(row).get(index).getPraise();
				//String time = clusters.get(row).get(index).getTime();
				//String forward = clusters.get(row).get(index).getForword();
				//String comment = clusters.get(row).get(index).getForword();
				
				String content = clusters.get(row).get(index).getContent();
				//System.out.println(content);
				String cluster_index = Integer.toString(row);
				output.write(mid+','+content+','+cluster_index+'\n');
			}
        }
		//output.flush();
		output.close();
		System.out.println("aaaa");
	}
}
