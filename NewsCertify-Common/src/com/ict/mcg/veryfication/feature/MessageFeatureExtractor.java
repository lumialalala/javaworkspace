/**
 * 
 */
package com.ict.mcg.veryfication.feature;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.NamedEntity;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.SentimentAnalysis;
import com.ict.mcg.processs.WordNode;

/**
 * @author JZW 获取一条微博信息的特征
 */
public class MessageFeatureExtractor {
	private WeiboEntity we;
	private String content = "";
	private ArrayList<WordNode> segwords = new ArrayList<WordNode>();
	public static Partition p = new Partition("./file/dic");
	
	public MessageFeatureExtractor(WeiboEntity we) {
		this.we = we;
		this.content = we.getContent();
	}

	/**
	 * 去除并返回文本中包含的链接列表
	 * 
	 * @param content
	 * @return
	 */
	private ArrayList<String> matchUrl() {
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> result = new ArrayList<String>();

		pattern = Pattern
				.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
		matcher = pattern.matcher(content);

		String labeltext = "";
		while (matcher.find()) {
			labeltext = matcher.group();
			result.add(labeltext);
		}

		content = matcher.replaceAll("");
		return result;
	}

	/**
	 * 去除并返回传入文本中包含的@列表
	 * 
	 * @param content
	 * @return
	 */
	private ArrayList<String> matchAt() {
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> result = new ArrayList<String>();

		pattern = Pattern.compile("@[^\\.^\\,^:^;^!^\\?^\\s^#^@^。^，^：^；^！^？]+");
		matcher = pattern.matcher(content);

		String labeltext = "";
		while (matcher.find()) {
			labeltext = matcher.group();
			result.add(labeltext);
		}
		content = matcher.replaceAll("");
		return result;
	}

	/**
	 * 去除并返回传入文本中包含的话题（##）列表
	 * 
	 * @param content
	 * @return
	 */
	private ArrayList<String> matchHashtag() {
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> result = new ArrayList<String>();

		pattern = Pattern.compile("#([^\\#|.]+)#");
		matcher = pattern.matcher(content);

		String labeltext = "";
		while (matcher.find()) {
			labeltext = matcher.group();
			result.add(labeltext);
		}
		content = matcher.replaceAll("");
		return result;
	}

	/**
	 * 返回传入文本中包含的问号数 0:没有，1：1个，2:多个
	 * 
	 * @param content
	 * @return
	 */
	private int matchQu() {
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> result = new ArrayList<String>();

		pattern = Pattern.compile("\\?|？");
		matcher = pattern.matcher(content);

		String labeltext = "";
		while (matcher.find()) {
			labeltext = matcher.group();
			result.add(labeltext);
		}
		if (result.size() == 0) {
			return 0;
		} else if (result.size() == 1) {
			return 1;
		} else
			return 2;
	}

	/**
	 * 返回传入文本中包含的感叹号数 0:没有，1：1个，2:多个
	 * 
	 * @param content
	 * @return
	 */
	private int matchEx() {
		Pattern pattern;
		Matcher matcher;
		ArrayList<String> result = new ArrayList<String>();

		pattern = Pattern.compile("！|!");
		matcher = pattern.matcher(content);

		String labeltext = "";
		while (matcher.find()) {
			labeltext = matcher.group();
			result.add(labeltext);
		}
		if (result.size() == 0) {
			return 0;
		} else if (result.size() == 1) {
			return 1;
		} else
			return 2;
	}

	/**
	 * 将纯文本内容分词、命名实体识别
	 * 
	 * @return
	 */
	private void segWord() {
//		String file = "./file/dic";
//		Partition p = new Partition(file);
		// 分词
//		ArrayList<WordNode> al = p.participleAndMerge(content);
		//过滤单字
		ArrayList<WordNode> al = p.participleAndMergeExcludeSingleWord(content);
		NamedEntity ne = new NamedEntity();
		ne.setProps(al);
		segwords = al;
	}

	public ArrayList<WordNode> getSegWords() {
		return segwords;
	}
	
	/**
	 * 返回词条数目（包含中英文），字数（英文单词算一个字）
	 * 
	 * @return
	 */
	private int[] countWords() {
		int wcnt = segwords.size();
		int count = 0;
		for (WordNode w : segwords) {
			if (w.getPos().charAt(0) == 'x') {
				count++;
			} else {
				count += w.getWord().length();
			}
		}
		int[] r = { wcnt, count };
		return r;
	}

	/**
	 * 统计三个人称出现的次数
	 * 
	 * @return
	 */
	private int[] countPe() {
		int i = 0, j = 0, k = 0;
		for (WordNode w : segwords) {
			String s = w.getWord().substring(0, 1);

			if (s.equals("我")) {
				i++;
			} else if (s.equals("你")) {
				j++;
			} else if (s.equals("他") || s.equals("她") || s.equals("它")) {
				k++;
			}
		}
		int r[] = { i, j, k };
		return r;
	}

	/**
	 * 统计命名实体
	 */
	private ArrayList<ArrayList<String>> getNamedEntity() {
		ArrayList<String> peolist = new ArrayList<String>();
		ArrayList<String> orglist = new ArrayList<String>();
		ArrayList<String> loclist = new ArrayList<String>();

		for (WordNode w : segwords) {
			if (w.getProps() == NamedEntity.PERSON) {
				peolist.add(w.getWord());
			} else if (w.getProps() == NamedEntity.ORGANIZATION) {
				orglist.add(w.getWord());
			} else if (w.getProps() == NamedEntity.REGION) {
				loclist.add(w.getWord());
			}
		}

		ArrayList<ArrayList<String>> result = new ArrayList<ArrayList<String>>();
		result.add(peolist);
		result.add(orglist);
		result.add(loclist);
		return result;
	}

	/**
	 * 计算文本内容的情感值及正负情感词个数
	 * 
	 * @return
	 */
	public int[] computeEmotion() {
		ArrayList<String> l = new ArrayList<String>();
		for (WordNode wn : this.segwords) {
			l.add(wn.getWord());
		}
		SentimentAnalysis sa = new SentimentAnalysis();
		double d = sa.getEmotionFromSentence(l);
		int score = (int) (d * 100);
		int pos = sa.getPosWordcloud().size();
		int neg = sa.getNegWordcloud().size();

		int r[] = { pos, neg, score };
		return r;
	}

	/*public MessageFeature extract() {
		int forword = Integer.parseInt(this.we.getForword());
		int comment = Integer.parseInt(this.we.getComment());
		ArrayList<String> urllist = this.matchUrl();
		ArrayList<String> atlist = this.matchAt();
		ArrayList<String> hashtaglist = this.matchHashtag();

		int qcount = this.matchQu();
		int ecount = this.matchEx();
		this.segWord();
		int[] wcnt = this.countWords();
		int[] pecnt = this.countPe();
		int[] emocnt = this.computeEmotion();
		ArrayList<ArrayList<String>> nameentitylist = this.getNamedEntity();

		MessageFeature mf = new MessageFeature(forword, comment, urllist,
				atlist, hashtaglist, nameentitylist, qcount, ecount, wcnt,
				pecnt, emocnt);
		return mf;
	}*/
	
	//new update 2015/1/12
	public MessageFeature extract() {
		int forword = Integer.parseInt(this.we.getForword());
		int comment = Integer.parseInt(this.we.getComment());
		ArrayList<String> urllist = this.matchUrl();
		ArrayList<String> atlist = this.matchAt();
		ArrayList<String> hashtaglist = this.matchHashtag();

		int qcount = this.matchQu();
		int ecount = this.matchEx();
		this.segWord();
		int[] wcnt = this.countWords();
		int[] pecnt = this.countPe();
		int[] emocnt = this.computeEmotion();
		ArrayList<ArrayList<String>> nameentitylist = this.getNamedEntity();
		
		int praise = Integer.parseInt(this.we.getPraise());
		
		int hasimg = 0, multiimgs = 0, countimgs = 0;
		ArrayList<String> imagelist = getImglist();
		if(imagelist!=null) countimgs = imagelist.size();
		if (countimgs > 0) {
			hasimg = 1;
			if (countimgs > 1) {
				multiimgs = 1;
			}
		}
		int[] imgfeature = {hasimg, multiimgs, countimgs};
		int platformType = getPlatformType(we);
		
		int origin = 0;
		if (we.isOrigin()) {
			origin = 1;
		}
		
		MessageFeature mf = new MessageFeature(forword, comment,praise,
				urllist, atlist, hashtaglist, nameentitylist, qcount, ecount,
				wcnt, pecnt, emocnt, imgfeature, platformType, origin, getImglist());
		return mf;
	}
	
	/**
	 * 返回图片列表
	 * @return
	 */
	public ArrayList<String> getImglist(){
		return this.we.getPiclist();
	}
	
	private int getPlatformType(WeiboEntity we) {
		int platformType = 1;
		String[] mobilePhrase = {"手机", "客户端", "平板", "iPad", "iPhone", "Android", "vivo", "联想", "华为", "小米", "酷派", "中兴", "OPPO", "金立", "三星", "索尼", "TCL", "红米", "魅族", "一加", "锤子", "诺基亚"};
//		System.out.println(we.getPlatform());
		if (we.getSourcePlatform().length() > 0) {
			for (String ph : mobilePhrase) {		
				if (we.getSourcePlatform().contains(ph)) {
					platformType = 0;
					break;
				}
			}
		}
//		System.out.println(platformType);
		return platformType;
	}
}
