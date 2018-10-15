package com.ict.mcg.util;
import java.util.Collection;
import java.util.List;

import org.ansj.app.keyword.KeyWordComputer;
import org.ansj.app.keyword.Keyword;

import com.hankcs.hanlp.HanLP;

public class Summary {

	public static String getSummaryKeyword(String title, String content) {
		return getSummaryKeyword(title, content, 5);
	}
	
	/**
	 * 提取文章关键词，首先摘要，从摘要中提关键词
	 * @param title 标题
	 * @param content 正文
	 * @param max 最多关键词个数
	 * @return 关键词，以空格隔开
	 */
	public static String getSummaryKeyword(String title, String content, int max) {
		// 从原文中找7句主题句出来
		String Summary_Content = getSummary(content, 7);
		String keywords = getKeywords(title, Summary_Content, max);
		
		return keywords;
	}
	
	public static String getKeywords(String title, String content, int max) {
		KeyWordComputer kwc = new KeyWordComputer(max);
		Collection<Keyword> result = kwc.computeArticleTfidf(title, content);

		String str_temp = "";
		for (Keyword result_temp : result) {
			str_temp = str_temp + result_temp.getName() + " ";
		}
		
		return str_temp;
	}
	
	/**
	 * 文本摘要
	 * @param content 内容文本
	 * @param max 最多句子数
	 * @return 摘要，多句之间以空格连接
	 */
	public static String getSummary(String content, int max) {
		List<String> sentenceList = HanLP.extractSummary(content, 7);

		String Summary_Content = "";
		for (String result_temp : sentenceList) {
			Summary_Content = Summary_Content + result_temp + " ";
		}
		
		return Summary_Content;
	}
}