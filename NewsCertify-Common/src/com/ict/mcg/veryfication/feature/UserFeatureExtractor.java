package com.ict.mcg.veryfication.feature;

import java.util.ArrayList;

import com.ict.mcg.gather.entity.WeiboEntity;

public class UserFeatureExtractor {
	public double[] userFeatureExtract(ArrayList<WeiboEntity> weList){
		double[] feat = new double[29];
		if(weList==null || weList.size() == 0)
			return feat;
		int weiboCnt = weList.size();
		feat[0] = weiboCnt;
		double cert0 = 0, cert1 = 0, cert2 = 0, hasDesCnt = 0, fanCnt = 0, fanLt1k = 0, fanGt1k = 0, fanGt1w = 0;
		double fanGt10w = 0, fanGt100w = 0, fanGt1000w =0, folCnt = 0, folLt100 = 0, folGt100 = 0, folGt500 = 0;
		double folGt1000 = 0, maleCnt = 0, hasLoc = 0, locBeij = 0, locSH = 0, locHW = 0, locSic = 0,locEls = 0;
		double weiboCount = 0, wbLt500 = 0, wbGt500 = 0, wbGt1000 = 0;
		for(WeiboEntity we: weList){
			if(we.getUserCertify() == 0)
				cert0 ++;
			else if(we.getUserCertify() == 1)
				cert1 ++;
			else if(we.getUserCertify() == 2)
				cert2 ++;
			if(we.getUserDescription() != null)
				hasDesCnt ++;
			try{
				int fan = Integer.parseInt(we.getUserFanCount());
				fanCnt += fan;
				if(fan > 10000000)
					fanGt1000w ++;
				else if(fan > 1000000)
					fanGt100w ++;
				else if(fan > 100000)
					fanGt10w ++;
				else if(fan > 10000)
					fanGt1w ++;
				else if(fan > 1000)
					fanGt1k ++;
				else
					fanLt1k ++;
				int follow = Integer.parseInt(we.getUserFollowCount());
				folCnt += follow;
				if(follow > 1000)
					folGt1000 ++;
				else if(follow > 500)
					folGt500 ++;
				else if(follow > 100)
					folGt100 ++;
				else 
					folLt100 ++;
				int wbct = Integer.parseInt(we.getUserWeiboCount());
				weiboCount += wbct;
				if(wbct > 1000)
					wbGt1000 += 1;
				else if(wbct > 500)
					wbGt500 += 1;
				else
					wbLt500 +=1;
				
			}catch(Exception e){
				System.out.println("UserFeatureExtractor fan/follow/weibo error:" + e.getMessage());
			}
			try{
				if(we.getUserGender().equals("male"))
					maleCnt ++;
			}catch(Exception e){
				System.out.println("UserFeatureExtractor usergender error:" + e.getMessage());
			}
			try{
				String location = we.getUserLocation();
				if(location !=  null && location.length() != 0){
					hasLoc ++;
					if(location.contains("北京"))
						locBeij ++;
					else if(location.contains("上海"))
						locSH ++;
					else if(location.contains("四川"))
						locSic ++;
					else if(location.contains("海外"))
						locHW ++;
					else 
						locEls ++;
				}
			}catch(Exception e){
				System.out.println("UserFeatureExtractor uselocation error:" + e.getMessage());
			}
		}
		
//		System.out.println(weiboCnt + " " + cert0 + " " +cert1 + " " +cert2);
		feat[1] = cert0/weiboCnt;
		feat[2] = cert1/weiboCnt;
		feat[3] = cert2/weiboCnt;
//		System.out.println(weiboCnt + " " + feat[1] + " " +feat[2] + " " +feat[3]);
		feat[4] = hasDesCnt*1.0/weiboCnt;
		feat[5] = fanCnt*1.0/weiboCnt;
		feat[6] = fanGt1000w*1.0/weiboCnt;
		feat[7] = fanGt100w*1.0/weiboCnt;
		feat[8] = fanGt10w*1.0/weiboCnt;
		feat[9] = fanGt1w*1.0/weiboCnt;
		feat[10] = fanGt1k*1.0/weiboCnt;
		feat[11] = fanLt1k*1.0/weiboCnt;
		feat[12] = folCnt*1.0/weiboCnt;
		feat[13] = folGt1000*1.0/weiboCnt;
		feat[14] = folGt500*1.0/weiboCnt;
		feat[15] = folGt100*1.0/weiboCnt;
		feat[16] = folLt100*1.0/weiboCnt;
		feat[17] = maleCnt*1.0/weiboCnt;
		feat[18] = hasLoc*1.0/weiboCnt;
		feat[19] = locBeij*1.0/weiboCnt;
		feat[20] = locSH*1.0/weiboCnt;
		feat[21] = locHW*1.0/weiboCnt;
		feat[22] = locSic*1.0/weiboCnt;
		feat[23] = locEls*1.0/weiboCnt;
		if(locHW > 0)
			feat[24] = 1;
		else 
			feat[24] = 0;
		feat[25] = weiboCount*1.0/weiboCnt;
		feat[26] = wbGt1000*1.0/weiboCnt;
		feat[27] = wbGt500*1.0/weiboCnt;
		feat[28] = wbLt500*1.0/weiboCnt;
		return feat;
	}

}
