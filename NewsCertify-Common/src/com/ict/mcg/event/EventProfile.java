/**
 * 
 */
package com.ict.mcg.event;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.ict.mcg.gather.entity.WeiboEntity;
import com.ict.mcg.processs.ICTAnalyzer;
import com.ict.mcg.processs.NameScoreFromKB;
import com.ict.mcg.processs.NamedEntity;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.util.ShortUrlExpander;
import com.ict.mcg.util.TimeConvert;


/**
 * 分析获取事件profile
 * 
 * @author JZW
 * 
 */
public class EventProfile {
	private ArrayList<WeiboEntity> allWeibo;
	private int peopleLimit = 5; 
	Logger log = Logger.getLogger(this.getClass());

	public EventProfile(ArrayList<WeiboEntity> allWeibo) {
		this.allWeibo = allWeibo;
	}

	public String[] getTime() {

		String[] time = new String[2];
		// 按时间排序微博
		Collections.sort(allWeibo, new Comparator<WeiboEntity>() {
			public int compare(WeiboEntity w0, WeiboEntity w1) {
				// 按时间排序
				long j = Long.parseLong(w0.getTime()) - Long.parseLong(w1
						.getTime());
				if (j == 0)
					return 0;
				else if(j > 0) return 1;
				else return -1;
			}
		});
		time[0] = TimeConvert.getStringTime(Long.parseLong(allWeibo.get(0)
				.getTime()));
		time[1] = TimeConvert.getStringTime(Long.parseLong(allWeibo.get(
				allWeibo.size() - 1).getTime()));

		// System.out.println(time[0] + "	" + time[1]);
		return time;
	}

	/**
	 * 按照属性提取关键词
	 * 
	 * @param props
	 *            命名实体属性值
	 * @return
	 */
	public ArrayList<String> getKeyWordsByProps(int props) {
		ArrayList<String> result = new ArrayList<String>();
		if (this.allWeibo == null || this.allWeibo.size() < 1)
			return result;
		// 设置分词属性
		for (WeiboEntity we : allWeibo) {
			ArrayList<WordNode> wlist = we.getSegs();
			for (WordNode wn : wlist) {
				wn.setProps(NamedEntity.getProps(wn.getPos()));
			}
		}

		HashMap<String, Integer> locmap = new HashMap<String, Integer>();
		HashMap<String, Integer> atmap = new HashMap<String, Integer>();
		for (WeiboEntity we : allWeibo) {
			ArrayList<WordNode> wlist = we.getSegs();
			HashSet<String> wordlist = new HashSet<String>();// 统计df
			for (WordNode wn : wlist) {
				if (wn.getProps() == props) {
					String w = wn.getWord();
					if (wordlist.contains(w))
						continue;
					wordlist.add(w);
					
					if (props == NamedEntity.PERSON && w.charAt(0) == '@') {
//						w = w.substring(1);
						if (atmap.containsKey(w)) {
							atmap.put(w, atmap.get(w) + 1);
						} else {
							atmap.put(w, 1);
						}
						
					} else {
						if (locmap.containsKey(w)) {
							locmap.put(w, locmap.get(w) + 1);
						} else {
							locmap.put(w, 1);
						}
					}
				}
			}
		}

		// 利用知识库的先验知识来处理人名异常，如'周一'
		if (props == NamedEntity.PERSON)
			locmap = NameScoreFromKB.resetNameScore(locmap);
		
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				locmap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});

		for (int i = 0; i < list.size(); i++) {
			// System.out.println(list.get(i).getKey()+":"+list.get(i).getValue());
			int count = list.get(i).getValue();
			if (count <= 1)
				break;
			
			//2014-11-02新添加规则,当出现两个词，其中一个词是另一个的前缀，认定为同一个词，选tf最高的
			boolean isAdded = false;
			for (String s : result) {
				if (s.startsWith(list.get(i).getKey()) || 
						list.get(i).getKey().startsWith(s)) {
					isAdded = true;
					break;
				}
			}
			if (!isAdded) {
				result.add(list.get(i).getKey());
			}
		}
		
//		if (props == NamedEntity.PERSON && result.size() < peopleLimit) {
		//添加@的用户,用作选取时间profile中的关键用户
		if (props == NamedEntity.PERSON && atmap.size() > 0) {
			list = new ArrayList<Map.Entry<String, Integer>>(
					atmap.entrySet());

			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1,
						Map.Entry<String, Integer> o2) {
					return (o2.getValue().compareTo(o1.getValue()));
				}
			});
			
			int size = peopleLimit < list.size()?peopleLimit:list.size();
			
			//@次数达到一定限制才会入选
			int atCountLimit = 20;
			for (int i = 0; i < size; ++i) {
				if (list.get(i).getValue() >= atCountLimit) {
					result.add(list.get(i).getKey());
				} else {
					break;
				}
			}
			
		}
		
		return result;
		// 针对人物处理含有@的人,前面已经处理含有@的人
//		if (props == NamedEntity.PERSON) {
//			ArrayList<String> res = new ArrayList<String>();
//			for (String s : result) {
//				if (s.charAt(0) == '@')
//					continue;
//				res.add(s);
//			}
//			return res;
//		} else {
//			return result;
//		}
	}

	public void getProfile() {
		String[] time = this.getTime();
		NamedEntity ne = new NamedEntity();
		for (WeiboEntity we : allWeibo) {
			ne.setProps(we.getSegs());
		}
		ArrayList<String> loc = this.getKeyWordsByProps(NamedEntity.REGION);
		ArrayList<String> org = this
				.getKeyWordsByProps(NamedEntity.ORGANIZATION);
		ArrayList<String> peo = this.getKeyWordsByProps(NamedEntity.PERSON);
	}
	
	/**
	 * 2014-11-4,zjq,通过微博内容中包含的url链接，获取外部信息源实体名称
	 * @return 检测到的外部信息源
	 */
	public ArrayList<String> getExternalSource() {
		String content = "";
		Pattern pattern = null;
		Matcher matcher = null;
		Map<String, Integer> urlCountMap = new HashMap<String, Integer>();
		Map<String, List<String>> urlSourceMap = new HashMap<String, List<String>>();
		
		//[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?
		for (WeiboEntity entity : allWeibo) {
			content = entity.getContent();
			
			//匹配内容中的url窗口
			pattern = Pattern
					.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
			matcher = pattern.matcher(content);

			String url = "";
			int prefixStart = 0;
			int prefixEnd = 0;
			int suffixStart = 0;
			int suffixEnd = 0;
			String windows = "";
			
			if (matcher.find()) {
				url = matcher.group().trim();
				prefixEnd = matcher.start();
				suffixStart = matcher.end();
				
				prefixStart = prefixEnd > 15?prefixEnd - 15:0;
				suffixEnd = suffixStart + 15;
				windows = content.substring(prefixStart, prefixEnd)+" ";
				if (content.length() <= suffixEnd) {
					windows += content.substring(suffixStart);
				} else {
					windows += content.substring(suffixStart, suffixEnd);
				}
				
				//对窗口内的内容分词处理，并对窗口内括号括起来的部分着重处理
				ArrayList<String> list = ICTAnalyzer.analyzeParagraph(windows, 1);
				List<String> sourceList = new ArrayList<String>();
				int size = list.size();
				int kzIndex = -1;
				int kyIndex = -1;
				for (int i = 0; i < size; ++i) {
					String token = list.get(i);
					int index = token.lastIndexOf("/");
					if (index == -1) {
						continue;
					}
					String pos = token.substring(index+1, token.length());
					String w = token.substring(0, index);

					if (NamedEntity.getProps(pos) == NamedEntity.ORGANIZATION) {
						sourceList.add(w);
					} else if (pos.equals("wkz")) {
						kzIndex = i;
					} else if (pos.equals("wky")) {
						kyIndex = i;
					}
				}

				List<String> kh = new ArrayList<String>();
				if (kzIndex != -1 && kyIndex != -1 && kyIndex > kzIndex) {
					kh = list.subList(kzIndex, kyIndex);
				} else if (kzIndex != -1){
					if (kzIndex < list.size()-1) {
						kh = list.subList(kzIndex+1, list.size());
					}
				} else if (kyIndex != -1) {
					if (kyIndex > 0) {
						kh = list.subList(0, kyIndex);
					}
				}
				
				if (kh.size() > 0) {
					ArrayList<WordNode> words = new ArrayList<WordNode>();
					for (int i = 0; i < kh.size(); ++i) {
						String w = kh.get(i);

						int index = w.lastIndexOf("/");
						if (index > 0) {
							//过滤符号
							String pos = w.substring(index + 1, w.length());
							if((pos.charAt(0)=='x')||(pos.charAt(0)=='w'))
								continue;
							
							if (pos.equals("nr1") && i < kh.size()-1) {
								String nextToken = kh.get(i+1);
								int nextIndex = nextToken.lastIndexOf("/");
								if (nextToken.substring(nextIndex + 1, nextToken.length()).equals("n")) {
									WordNode node = new WordNode();
									node.setWord(w.substring(0, index)+nextToken.substring(0,nextIndex));
									node.setPos("nr");
									words.add(node);
								}
							}
							
							WordNode node = new WordNode();
							node.setWord(w.substring(0, index));
							node.setPos(pos);
							words.add(node);
						}

					}

					// 用wiki 数据库归并
					words = new Partition().wikiDBMerge(words);
					
					for (WordNode wn : words) {
						int props = NamedEntity.getProps(wn.getPos());
						if (props == NamedEntity.PERSON || props == NamedEntity.ORGANIZATION || wn.getPos().equals("nm")) {
							if (wn.getWord().charAt(0) != '@') {
								sourceList.add(wn.getWord());
							}
						}
					}
				}
				
				if (urlCountMap.containsKey(url)) {
					urlCountMap.put(url, urlCountMap.get(url)+1);
					
					sourceList.addAll(urlSourceMap.get(url));
					urlSourceMap.put(url, sourceList);
				} else {
					urlCountMap.put(url, 1);
					urlSourceMap.put(url, sourceList);
				}
				
			}
		}
		
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				urlCountMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});
		
//		for (int i = 0; i < list.size(); ++i) {
//			System.out.println(list.get(i).getKey() + ":" + list.get(i).getValue());
//		}
		
//		int externalSourceFrequenceLimit = allWeibo.size() / 15;
		int externalSourceFrequenceLimit = 10;
		if (list.size() > 0 && list.get(0).getValue() < externalSourceFrequenceLimit && list.get(0).getValue() > 1) {
			externalSourceFrequenceLimit = list.get(0).getValue();
		}
		
		Map<String, Integer> sourceMap = new HashMap<String, Integer>();
		for (int i = 0; i < list.size(); ++i) {
			if (list.get(i).getValue() >= externalSourceFrequenceLimit) {
				
				List<String> sourceList = urlSourceMap.get(list.get(i).getKey());
				for (String s:sourceList) {
					if (!s.trim().isEmpty()) {
						if (sourceMap.containsKey(s)) {
							sourceMap.put(s, sourceMap.get(s));
						} else {
							sourceMap.put(s, 1);
						}
					}
				}
			} else {
				break;
			}
		}
		
		list = new ArrayList<Map.Entry<String, Integer>>(sourceMap.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});
		
		ArrayList<String> result = new ArrayList<String>();
		int externalSourceCountLimit = 10;
		
		for (int i = 0; i < list.size(); ++i) {
			if (i < externalSourceCountLimit) {
				result.add(list.get(i).getKey());
			} else {
				break;
			}
		}
		
		return result;
	}
	
	/**
	 * 选取外部url
	 * @return
	 */
	public ArrayList<String> getExternalUrl() {
		int externalUrlCount = 3;
		ArrayList<String> externalUrls = new ArrayList<String>();
		String content = "";
		Pattern pattern = null;
		Matcher matcher = null;
		Map<String, Integer> urlCountMap = new HashMap<String, Integer>();
		Map<String, Integer> urlForwardCountMap = new HashMap<String, Integer>();
		
		//[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?
		for (WeiboEntity entity : allWeibo) {
			content = entity.getContent();
			
			//匹配内容中的url窗口
			pattern = Pattern
					.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
			matcher = pattern.matcher(content);

			String url = "";
			
			while (matcher.find()) {
				url = matcher.group().trim();
				if (url.equals("http://")) {
					log.warn(content);
					continue;
				}
				if (urlCountMap.containsKey(url)) {
					urlCountMap.put(url, urlCountMap.get(url)+1);
				} else {
					urlCountMap.put(url, 1);
				}
				
				if (urlForwardCountMap.containsKey(url)) {
					urlForwardCountMap.put(url, urlForwardCountMap.get(url)+1+Integer.parseInt(entity.getForword()));
				} else {
					urlForwardCountMap.put(url, 1+Integer.parseInt(entity.getForword()));
				}
			}
		}
		
		List<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(
				urlCountMap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o2.getValue().compareTo(o1.getValue()));
			}
		});
		
		int externalSourceFrequenceLimit = 5;
		//url出现次数多于阈值
		if (list.size() > 0 && list.get(0).getValue() >= externalSourceFrequenceLimit) {
			for (int i = 0; i < list.size(); ++i) {
				if (list.get(i).getValue() >= externalSourceFrequenceLimit && externalUrls.size() < externalUrlCount) {
					String completeUrl = list.get(i).getKey();
					try {
						completeUrl = ShortUrlExpander.convertShortUrlToLong(list.get(i).getKey());
					} catch (IOException e) {
						log.warn("short2long fail: " + completeUrl, e);
					}
					
					try {
						if (isValidExternalUrl(completeUrl)) {
							externalUrls.add(completeUrl);
						} 
					} catch (IOException e) {
						log.warn("isvalid fail: " + completeUrl, e);
					}
					//去掉已审核的url
					urlForwardCountMap.remove(list.get(i).getKey());
					
				} else {
					break;
				}
			} 
		} 
		
		int externalSourceForwardLimit = 50;
		//根据微博转发数从大到小取
		if (externalUrls.size() < externalUrlCount) {
			list = new ArrayList<Map.Entry<String, Integer>>(urlForwardCountMap.entrySet());
			Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
				public int compare(Map.Entry<String, Integer> o1,
						Map.Entry<String, Integer> o2) {
					return (o2.getValue().compareTo(o1.getValue()));
				}
			});
			
			for (int i = 0; i < list.size(); ++i) {
				if (list.get(i).getValue() > externalSourceForwardLimit && externalUrls.size() < externalUrlCount) {
					String completeUrl = list.get(i).getKey();
					try {
						completeUrl = ShortUrlExpander.convertShortUrlToLong(list.get(i).getKey());
					} catch (IOException e) {
						log.warn("short2long fail: " + completeUrl, e);
					}
					
					try {
						if (isValidExternalUrl(completeUrl)) {
							externalUrls.add(completeUrl);
						}
					} catch (IOException e) {
						log.warn("valid fail: " + completeUrl, e);
					}
				} else {
					break;
				}
			}
		}
		
		return externalUrls;
	}
	
	public boolean isValidExternalUrl(String url) throws IOException {
		//去掉http://协议头
		String noProtocalUrl = url.substring(7);
		if (noProtocalUrl.startsWith("s.weibo") || noProtocalUrl.startsWith("weibo") ||
				noProtocalUrl.endsWith("com") || noProtocalUrl.endsWith("com/") ||
				noProtocalUrl.endsWith("app") || noProtocalUrl.endsWith("app/")) {
			return false;
		}
		
		//取顶级域名
		if (noProtocalUrl.contains("/")) {
			noProtocalUrl = noProtocalUrl.substring(0,noProtocalUrl.indexOf("/"));
		}
		//去掉购物网站
		if (noProtocalUrl.contains("taobao") || noProtocalUrl.contains("tmall") ||
				noProtocalUrl.contains("jd.com") || noProtocalUrl.contains("sjzhushou.com") ||
				noProtocalUrl.contains("dangdang.com") || noProtocalUrl.contains("yhd.com") ||
				noProtocalUrl.contains("1mall.com") || noProtocalUrl.contains("amazon")) {
			return false;
		}
		
		URL u = new URL(url);
		HttpURLConnection httpconn = (HttpURLConnection) (u.openConnection());
		httpconn.setReadTimeout(5000);
		httpconn.setConnectTimeout(5000);
		httpconn.setRequestMethod("GET");  
		httpconn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpconn.setRequestProperty("Accept-Encoding", "gzip, deflate");
		httpconn.setRequestProperty("Accept-Language", "zh-cn,zh;q=0.8");
		httpconn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.93 Safari/537.36");
		httpconn.connect();
		
		//去掉不能打开的
		if (httpconn.getResponseCode() == 200) {
			return true;
		} else {
			return false;
		}
	}
}
