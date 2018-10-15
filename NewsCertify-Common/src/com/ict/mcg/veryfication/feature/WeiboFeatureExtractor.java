/**
 * 
 */
package com.ict.mcg.veryfication.feature;
import java.util.ArrayList;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.WordNode;
/**
 * 获取一条微博内容方面的特征
 * 
 * @author JZW
 * 
 */
public class WeiboFeatureExtractor {
	/**
	 * 获取一条微博的内容特征与用户特征
	 * 
	 * @param we
	 *            微博
	 * @param label
	 *            主题标注
	 * @return 特征结果
	 */
	
	public static double[][] LDA_MAX_MIN= {{0.028,0.481}, {0.028,0.457}, {0.028,0.354}, {0.029,0.418}, {0.029,0.463}, {0.028,0.351}, {0.028,0.409}, {0.029,0.453}, {0.029,0.475}, {0.028,0.419}, {0.028,0.463}, {0.029,0.481}, {0.029,0.487}, {0.029,0.431}, {0.029,0.442}, {0.029,0.414}, {0.028,0.4}, {0.029,0.378}}; 
	public String extractOneWeibo(WeiboEntity we, String label, ArrayList<ArrayList<WordNode>> allSegWords) {
		if (we == null)
			return "";
		// 提取內容特征
		MessageFeatureExtractor mfe = new MessageFeatureExtractor(we);
		MessageFeature mf = mfe.extract();
		// 要放在extract()调用后
		allSegWords.add(mfe.getSegWords());

		// 输出特征结果
		/*String result = label + " 1:"
				+ normalize(mf.getComment(), 9.081, 0, true) + " 2:"
				+ normalize(mf.getEcount(), 2, 0, false) + " 3:"
				+ normalize(mf.getForword(), 10.285, 0, true) + " 4:"
				+ normalize(mf.getQcount(), 2, 0, false) + " 5:"
				+ normalize(mf.getAtlist().size(), 11, 0, false) + " 6:"
				+ normalize(mf.getEmocount()[0], 14, 0, false) + " 7:"
				+ normalize(mf.getEmocount()[1], 12, 0, false) + " 8:"
				+ normalize(mf.getEmocount()[2], 155, -186, false) + " 9:"
				+ normalize(mf.getHashtaglist().size(), 21, 0, false) + " 10:"
				+ normalize(mf.getNameentitylist().get(0).size(), 9, 0, false)
				+ " 11:"
				+ normalize(mf.getNameentitylist().get(1).size(), 6, 0, false)
				+ " 12:"
				+ normalize(mf.getNameentitylist().get(2).size(), 26, 0, false)
				+ " 13:" + normalize(mf.getPecount()[0], 6, 0, false) + " 14:"
				+ normalize(mf.getPecount()[1], 6, 0, false) + " 15:"
				+ normalize(mf.getPecount()[2], 6, 0, false) + " 16:"
				+ normalize(mf.getUrllist().size(), 8, 0, false) + " 17:"
				+ normalize(mf.getWcount()[0], 57, 0, false) + " 18:"
				+ normalize(mf.getWcount()[1], 132, 0, false) + " 19:"
				+ normalize(mf.getImgFeature()[0], 1, 0, false) + " 20:"
				+ normalize(mf.getImgFeature()[1], 1, 0, false) + " 21:"
				+ normalize(mf.getImgFeature()[2], 1, 0, false) + " 22:"
				+ normalize(mf.getPlatformType(), 1, 0, false) + " 23:"
				+ normalize(mf.getPraise(), 1, 0, false);*/
		
		
		/**
		 * 修改特征维数后，需要修改getAllLDAFeature()中lda特征的起始维数位
		 */
		String result = label + " 1:"
				+ normalize(mf.getComment(), 10.236, 0, true) + " 2:"
				+ normalize(mf.getEcount(), 2, 0, false) + " 3:"
				+ normalize(mf.getForword(), 11.687, 0, true) + " 4:"
				+ normalize(mf.getQcount(), 2, 0, false) + " 5:"
				+ normalize(mf.getAtlist().size(), 22, 0, false) + " 6:"
				+ normalize(mf.getEmocount()[0], 14, 0, false) + " 7:"
				+ normalize(mf.getEmocount()[1], 13, 0, false) + " 8:"
				+ normalize(mf.getEmocount()[2], 155, -240, false) + " 9:"
				+ normalize(mf.getHashtaglist().size(), 16, 0, false) + " 10:"
				+ normalize(mf.getNameentitylist().get(0).size(), 13, 0, false)
				+ " 11:"
				+ normalize(mf.getNameentitylist().get(1).size(), 5, 0, false)
				+ " 12:"
				+ normalize(mf.getNameentitylist().get(2).size(), 15, 0, false)
				+ " 13:" + normalize(mf.getPecount()[0], 5, 0, false) + " 14:"
				+ normalize(mf.getPecount()[1], 6, 0, false) + " 15:"
				+ normalize(mf.getPecount()[2], 5, 0, false) + " 16:"
				+ normalize(mf.getUrllist().size(), 5, 0, false) + " 17:"
				+ normalize(mf.getWcount()[0], 58, 1, false) + " 18:"
				+ normalize(mf.getWcount()[1], 143, 1, false) + " 19:"
				+ normalize(mf.getImgFeature()[0], 1, 0, false) + " 20:"
				+ normalize(mf.getImgFeature()[1], 1, 0, false) + " 21:"
				+ normalize(mf.getImgFeature()[2], 2.303, 0, true) + " 22:"
				+ normalize(mf.getPlatformType(), 1, 0, false) + " 23:"
				+ normalize(mf.getPraise(), 10.722, 0, true) + " 24:"
				+ normalize(mf.getOrigin(), 1, 0, false);
//		String result = label + " 1:"
//				+ mf.getComment() + " 2:"
//				+ mf.getEcount() + " 3:"
//				+ mf.getForword() + " 4:"
//				+ mf.getQcount()+ " 5:"
//				+ mf.getAtlist().size() + " 6:"
//				+ mf.getEmocount()[0] + " 7:"
//				+ mf.getEmocount()[1] + " 8:"
//				+ mf.getEmocount()[2] + " 9:"
//				+ mf.getHashtaglist().size() + " 10:"
//				+ mf.getNameentitylist().get(0).size()
//				+ " 11:"
//				+ mf.getNameentitylist().get(1).size()
//				+ " 12:"
//				+ mf.getNameentitylist().get(2).size()
//				+ " 13:" + mf.getPecount()[0] + " 14:"
//				+ mf.getPecount()[1] + " 15:"
//				+ mf.getPecount()[2] + " 16:"
//				+ mf.getUrllist().size() + " 17:"
//				+ mf.getWcount()[0] + " 18:"
//				+ mf.getWcount()[1] + " 19:"
//				+ mf.getImgFeature()[0] + " 20:"
//				+ mf.getImgFeature()[1] + " 21:"
//				+ mf.getImgFeature()[2] + " 22:"
//				+ mf.getPlatformType() + " 23:"
//				+ mf.getPraise() + " 24:"
//				+ mf.getOrigin();

		return result;
	}

	private String normalize(double value, double max, double min, boolean log) {
		if (log)
			value = Math.log(value + 1);
		double d = (value - min) / (max - min);
		if (d > 1)
			d = 1;
		if (d < 0)
			d = 0;
		String result = String.format("%.6f", d);
		return result;
	}

}
