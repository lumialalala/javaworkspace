package com.ict.mcg.people;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ict.mcg.gather.entity.WeiboEntity;



public class SelectUsers {
	private ArrayList<ArrayList<WeiboEntity>> weiboClusters;
	private int userNumber;
	private List<String> urls = new ArrayList<String>();
	private WeiboInfo earlistUser = new WeiboInfo();
	private List<WeiboInfo> everyClusterTopThree = new ArrayList<WeiboInfo>();
	private List<String> selectUserName = new ArrayList<String>();
	
	private int weiboTotalForward = 0;
	private int weiboTotalCount = 0;
	private int weiboAverageForward = 0;
	private static final int FORWARD_LIMIT = 100;
	
	/**
	 * Select users from clusters
	 * @param clusters
	 * @param n number of user to select
	 */
	public SelectUsers(ArrayList<ArrayList<WeiboEntity>> clusters,int n){
		this.weiboClusters = clusters;
		this.userNumber = n; 
	}
	
	/**
	 * get important top userNumer count value
	 * @return : WeiboInfo list
	 */
	public List<WeiboInfo> getImportantUser (){
		List<WeiboInfo> selectedUsers = new ArrayList<WeiboInfo>();
		if (this.weiboClusters==null||this.weiboClusters.size()==0) {
			return null;
		}	
		//1.  sort by date ,get earliest weibo user ,add it at last
		this.earlistUser = getEarliestWeiboUser();
		
		//2.	select top users in each cluster
		weiboAverageForward = getWeiboAverageForward();
		for (ArrayList<WeiboEntity> oneCluster : this.weiboClusters) {
			WeiboInfo oneWeiboInfo = getTopUserInOneCluster(oneCluster);
			selectedUsers.add(oneWeiboInfo);
		}
		List<WeiboInfo> resUsers = removeRepeateUser(selectedUsers);
		//3.	processing num < 5;
		if (resUsers.size()<5) {
			List<WeiboInfo> allTopInfo = sortAllWeiboInfo(everyClusterTopThree);
			for (WeiboInfo weiboInfo : allTopInfo) {
				if (!this.selectUserName.contains(weiboInfo.name) && !weiboInfo.name.equals(earlistUser.name)) {
					this.selectUserName.add(weiboInfo.name);
					resUsers.add(weiboInfo);
				}
				if (resUsers.size()>=5) {
					break;
				}
			}
		}
		//4. return data , set earliest user in the first place
		List<WeiboInfo> selUsers = new ArrayList<WeiboInfo>();
		selUsers.add(this.earlistUser);
		for (WeiboInfo weiboInfo : resUsers) {
			if (!weiboInfo.name.equals(this.earlistUser.name)) {
				selUsers.add(weiboInfo);
			}
		}
		getMostRelatedUrl(selUsers);		
		return selUsers;
	}
	
	private int getWeiboAverageForward() {
		for (ArrayList<WeiboEntity> c : weiboClusters) {
			for (WeiboEntity w : c) {
				int forward = 0;
				try {
					forward = Integer.parseInt(w.getForword());
				} catch (Exception e) {
					e.printStackTrace();
				}
				weiboTotalForward += forward;
			}
			weiboTotalCount += c.size();
		}
		
		return weiboTotalForward / weiboTotalCount;
	}
	
	private WeiboInfo getEarliestWeiboUser() {
		WeiboInfo weiboInfo = new WeiboInfo();
		ArrayList<WeiboEntity> earlyClustrs = new ArrayList<WeiboEntity>();
		for (ArrayList<WeiboEntity> wel : this.weiboClusters) {
			Collections.sort(wel, new Comparator<WeiboEntity>() {
				public int compare(WeiboEntity w0, WeiboEntity w1) {
					long j = 0;
					try {
						j = Long.parseLong(w0.getTime())
								- Long.parseLong(w1.getTime());
					} catch(Exception e) {
						e.printStackTrace();
					}
					if (j > 0) {
						return 1;
					} else if (j < 0){
						return -1;
					} else {
						return 0;
					}
				}
			});
			if (wel!=null&&wel.size()!=0) {
				earlyClustrs.add(wel.get(0));
			}
		}
		Collections.sort(earlyClustrs, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				long j = 0;
				try {
					j = Long.parseLong(w0.getTime())
							- Long.parseLong(w1.getTime());
				} catch(Exception e) {
					e.printStackTrace();
				}
				if (j > 0) {
					return 1;
				} else if (j < 0){
					return -1;
				} else {
					return 0;
				}
			}
		});
		if (earlyClustrs!=null&&earlyClustrs.size()!=0) {
			
			WeiboEntity weiboEntity = earlyClustrs.get(0);
			weiboInfo.name = weiboEntity.getName();
			weiboInfo.url = weiboEntity.getUrl();
			weiboInfo.userurl = weiboEntity.getUserurl();
			weiboInfo.time = weiboEntity.getTime();
			weiboInfo.comment = weiboEntity.getComment();
			weiboInfo.forword = weiboEntity.getForword();
			weiboInfo.hotrate = weiboInfo.getHorate();
		}
		return weiboInfo;
	}
	

	/**
	 * get entire sorted weiboinfo, and remove repeated weibo user 
	 * @param weiboInfos
	 * @return
	 */
	private List<WeiboInfo> sortAllWeiboInfo(List<WeiboInfo> weiboInfos) {
		// TODO Auto-generated method stub
		List<WeiboInfo> allWeiboInfos = new ArrayList<WeiboInfo>();
		HashMap<String, WeiboInfo> allHashMap = new HashMap<String, WeiboInfo>();
		for (WeiboInfo weiboInfo : weiboInfos) {
			String nameStr = weiboInfo.name;
			if (!allHashMap.containsKey(nameStr)) {
				allHashMap.put(nameStr, weiboInfo);
			}	
		}
		List<Map.Entry<String, WeiboInfo>> list = getSortedWeiboInfo(allHashMap);
		for (int i = 0; i < list.size(); i++) {
			allWeiboInfos.add(list.get(i).getValue());
		}
		return allWeiboInfos;
	}
	/**
	 * remove repeated user in different cluster, and return the sorted list
	 * @param selectedUsers
	 * @return
	 */
	private List<WeiboInfo> removeRepeateUser(List<WeiboInfo> selectedUsers) {
		// TODO Auto-generated method stub
		HashMap<String, WeiboInfo> reWeiboInfo = new HashMap<String, WeiboInfo>();
		List<WeiboInfo> finalWeiboInfo = new ArrayList<WeiboInfo>();
		for (WeiboInfo weiboInfo : selectedUsers) {
			String name = weiboInfo.name;
			if (!reWeiboInfo.containsKey(name)) {
				reWeiboInfo.put(name, weiboInfo);
			}
		}
		// sort and return top num
		List<Map.Entry<String, WeiboInfo>> list = getSortedWeiboInfo(reWeiboInfo);
		int num;
		if (list.size()>this.userNumber) {
			num = this.userNumber;
		}else {
			num = list.size();
		}
		for (int i = 0; i < num; i++) {
			finalWeiboInfo.add(list.get(i).getValue());
			this.selectUserName.add(list.get(i).getValue().name);
		}
		return finalWeiboInfo;
	}
	/**
	 * get top user in one cluster, and save the top 3 user in one cluster
	 * @param oneCluster
	 * @return
	 */
	private WeiboInfo getTopUserInOneCluster(ArrayList<WeiboEntity> oneCluster) {		
		HashMap<String, WeiboInfo> oneClusterWeibo = new HashMap<String, WeiboInfo>();
		// 1.	avoid two same user in one cluster
		for (WeiboEntity weiboEntity : oneCluster) {
			String name = weiboEntity.getName();
			double keyRatio = 0;
			try {
				int forward = Integer.parseInt(weiboEntity.getForword());
				int limit = weiboAverageForward > FORWARD_LIMIT ? FORWARD_LIMIT : weiboAverageForward;
				if (forward >= limit) {
					keyRatio = (double)forward / Integer.parseInt(weiboEntity.getUserFanCount());	
				}
			} catch (Exception e) {
				System.out.println("[SelectUsers]forwardcount:" + weiboEntity.getForword());
				System.out.println("[SelectUsers]fancount:" + weiboEntity.getUserFanCount());
//				e.printStackTrace();
			}
			
			if (oneClusterWeibo.containsKey(name)) {
				WeiboInfo weiboInfo = oneClusterWeibo.get(name);
				int com = Integer.parseInt(weiboInfo.comment)+Integer.parseInt(weiboEntity.getComment());
				int forw = Integer.parseInt(weiboInfo.forword)+Integer.parseInt(weiboEntity.getForword());
				weiboInfo.comment = ""+com;
				weiboInfo.forword = ""+forw;
				weiboInfo.hotrate = weiboInfo.getHorate();
				
				keyRatio += weiboInfo.keyRatio;
				weiboInfo.keyRatio = keyRatio;
				oneClusterWeibo.put(weiboInfo.name, weiboInfo);
			}else {
				WeiboInfo weiboInfo = new WeiboInfo();
				weiboInfo.name = weiboEntity.getName();
				weiboInfo.url = weiboEntity.getUrl();
				weiboInfo.userurl = weiboEntity.getUserurl();
				weiboInfo.time = weiboEntity.getTime();
				weiboInfo.comment = weiboEntity.getComment();
				weiboInfo.forword = weiboEntity.getForword();
				weiboInfo.hotrate = weiboInfo.getHorate();
				
				weiboInfo.keyRatio = keyRatio;
				oneClusterWeibo.put(weiboInfo.name, weiboInfo);
			}
		}
		//2.	sort weibo hashmap
		List<Map.Entry<String, WeiboInfo>> list = getSortedWeiboInfo(oneClusterWeibo);
		WeiboInfo resWeibo = list.get(0).getValue();	
		//3.	save top 3 weibo in one cluster
		int mnum = 3;
		if (list.size()<mnum) {
			mnum = list.size();
		}
		for (int i = 0; i < mnum; i++) {
			this.everyClusterTopThree.add(list.get(i).getValue());
		}
		return resWeibo;
	}
	
	/**
	 * sort hashmap
	 * @param allWeiboInfo
	 * @return
	 */
	private List<Map.Entry<String, WeiboInfo>> getSortedWeiboInfo(HashMap<String, WeiboInfo> allWeiboInfo){
		List<Map.Entry<String, WeiboInfo>> list = new ArrayList<Map.Entry<String, WeiboInfo>>(
				allWeiboInfo.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, WeiboInfo>>() {
			public int compare(Map.Entry<String, WeiboInfo> o1,
					Map.Entry<String, WeiboInfo> o2) {
				double m = (o2.getValue().keyRatio)-(o1.getValue().keyRatio);
				if(m==0) return 0;
				else if(m>0) return 1;
				else return -1;
			}
		});
		return list;
	}
	/**
	 * get most influenced weibo url
	 * @param allWeiboInfos
	 */
	private void getMostRelatedUrl(List<WeiboInfo> allWeiboInfos){
		if (allWeiboInfos ==null||allWeiboInfos.size() ==0) {
			return;
		}
		if(allWeiboInfos.size()<3){
			for (int i = 0; i < allWeiboInfos.size(); i++) {
				this.urls.add(allWeiboInfos.get(i).url);
			}
		}else {
			for (int i = 0; i < 3; i++) {
				this.urls.add(allWeiboInfos.get(i).url);
			}
		}	
	}
	public List<String> getSelectUrls(){
		return this.urls;
	}
}
