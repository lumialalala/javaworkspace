package com.ict.mcg.model;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.log4j.Logger;

import com.ict.mcg.processs.ICTAnalyzer;
import com.ict.mcg.processs.NamedEntity;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.WordNode;
import com.ict.mcg.util.FileIO;

public class ShortTextKeywords {
	private static Set<String> stopSet = null;
	private static String stop_word = FileIO.getFilePath() + "stop_words.txt";
	private static String stop_word_resource = FileIO.getResourcePath() + "stop_words.txt";
	
	private static boolean isinitial = false;
	
	public static void init(String stopWordFile, String DFFile) {
		stop_word = stopWordFile;
		stop_word_resource = "";
		
		try {
			BM25.init(DFFile);
			ShortTextKeywords.loadStopWords();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private static void loadStopWords() throws IOException {
		stopSet = new HashSet<String>();
		InputStream is = ShortTextKeywords.class.getResourceAsStream(stop_word_resource);
		if (null == is) {
			is = new FileInputStream(stop_word);
		}
		BufferedReader breader = new BufferedReader(
				new InputStreamReader(is, "UTF-8"));
		String line = null;
		while ((line = breader.readLine()) != null) {
			line.trim();
			stopSet.add(line);
		}
		breader.close();
		
		isinitial = true;
	}
	
	public static List<String> extract(String content) {
		if (!isinitial) {
			try {
				loadStopWords();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		content = content.replaceAll("@[^\\s;,.!?；，。！？:：]{0,10}", " ");
		Partition p = new Partition();
		ArrayList<WordNode> tokens = new ArrayList<WordNode>();
		if (content.length() > 0) {
			// 开始分词
			ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
			ArrayList<WordNode> words = new ArrayList<WordNode>();

			int size = list.size();
			for (int i = 0; i < size; ++i) {
				String w = list.get(i);

				int index = w.lastIndexOf("/");

				if (index > 0) {
					//过滤符号
					String pos = w.substring(index + 1, w.length());
					if((pos =="xe")||(pos =="xs") || (pos =="xm") || (pos =="xu") ||(pos.charAt(0)=='w'))
						continue;
					
					if (pos.equals("nr1") && i < size-1) {
						String nextToken = list.get(i+1);
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
			tokens = p.wikiDBMerge(words);
		}
		ArrayList<WordNode> result = new ArrayList<WordNode>();
		for (WordNode wn : tokens) {
			if (wn.getWord().length() < 2) {
				continue;
			}
			String pos = wn.getPos();
			if (pos.charAt(0) == 'n') {
				wn.setWeight(getWeight(pos));
				result.add(wn);
			} else if (pos.charAt(0) == 'x') {
				wn.setWeight(getWeight(pos));
				result.add(wn);
			}
		}
		
		Map<String, Double> neMap = new HashMap<String, Double>();
		Map<String, Double> keywordMap = new HashMap<String, Double>();
		for (WordNode wn : result) {
			if (stopSet.contains(wn.getWord())) {
				continue;
			}
			if (NamedEntity.getProps(wn.getPos()) == NamedEntity.PERSON ||
					NamedEntity.getProps(wn.getPos()) == NamedEntity.ORGANIZATION ||
					NamedEntity.getProps(wn.getPos()) == NamedEntity.REGION ) {
				if (neMap.containsKey(wn.getWord())) {
					neMap.put(wn.getWord(), neMap.get(wn.getWord())+wn.getWeight());
				} else {
					neMap.put(wn.getWord(), wn.getWeight());
				}
			} else {
				if (keywordMap.containsKey(wn.getWord())) {
					keywordMap.put(wn.getWord(), keywordMap.get(wn.getWord()) + wn.getWeight());
				} else {
					keywordMap.put(wn.getWord(), wn.getWeight());
				}
			}
		}
		
		
		List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(neMap.entrySet());
		for (Entry<String, Double> entry:list) {
			entry.setValue(Math.log(entry.getKey().length()+2) * BM25.getNEWeight(entry.getKey(), entry.getValue(), content.length()));
		}
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		
		Set<String> addSet = new HashSet<String>();
		List<String> resultList = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer();
		int count = 0;
		for (Entry<String, Double> entry:list) {
			if (entry.getValue() > 1.0 && count < 15) {
				buffer.append(entry.getKey()+",");
				count++;
				addSet.add(entry.getKey());
			} else {
				break;
			}
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length()-1);
		}
		resultList.add(buffer.toString());
		
		list = new ArrayList<Entry<String, Double>>(keywordMap.entrySet());
		for (Entry<String, Double> entry:list) {
			entry.setValue(Math.log(entry.getKey().length()+2) * BM25.getWeight(entry.getKey(), entry.getValue(), content.length()));
		}
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		
		count = 0;
		buffer = new StringBuffer();
		for (Entry<String, Double> entry:list) {
			if (addSet.contains(entry.getKey())) {
				continue;
			}
			if (entry.getValue() > 2.0 && count < 15) {
				buffer.append(entry.getKey()+",");
				count++;
			} else {
				break;
			}
		}
		if (buffer.length() > 0) {
			buffer.deleteCharAt(buffer.length()-1);
		}
		resultList.add(buffer.toString());
		
		return resultList;
	}
	
	public static List<String> extractForNewsClue(String content) {
		if (stopSet == null) {
			try {
				loadStopWords();
			} catch (IOException e) {
				Logger log = Logger.getRootLogger();
				log.warn(e);
			}
		}
		//content = content.replace("@", ",");
		content = content.replaceAll("@[^\\s;,.!?；，。！？:：@]{0,10}", "");
		Partition p = new Partition();
		ArrayList<WordNode> tokens = new ArrayList<WordNode>();
		if (content.length() > 0) {
			// 开始分词
			ArrayList<String> list = ICTAnalyzer.analyzeParagraph(content, 1);
			ArrayList<WordNode> words = new ArrayList<WordNode>();

			int size = list.size();
			for (int i = 0; i < size; ++i) {
				String w = list.get(i);

				int index = w.lastIndexOf("/");

				if (index > 0) {
					//过滤符号
					String pos = w.substring(index + 1, w.length());
					if((pos =="xe")||(pos =="xs") || (pos =="xm") || (pos =="xu") ||(pos.charAt(0)=='w'))
						continue;
					
					if (pos.equals("nr1") && i < size-1) {
						String nextToken = list.get(i+1);
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
			tokens = p.wikiDBMerge(words);
		}
		ArrayList<WordNode> result = new ArrayList<WordNode>();
		for (WordNode wn : tokens) {
			if (wn.getWord().length() < 2) {
				continue;
			}
			String pos = wn.getPos();
			if (pos.charAt(0) == 'n') {
				wn.setWeight(getWeight(pos));
				result.add(wn);
			} else if (pos.charAt(0) == 'x') {
				wn.setWeight(getWeight(pos));
				result.add(wn);
			}
		}
		
		Map<String, Double> neMap = new HashMap<String, Double>();
		Map<String, Double> keywordMap = new HashMap<String, Double>();
		for (WordNode wn : result) {
			if (stopSet.contains(wn.getWord())) {
				continue;
			}
			if (NamedEntity.getProps(wn.getPos()) == NamedEntity.PERSON ||
					NamedEntity.getProps(wn.getPos()) == NamedEntity.ORGANIZATION ||
					NamedEntity.getProps(wn.getPos()) == NamedEntity.REGION ) {
				if (neMap.containsKey(wn.getWord())) {
					neMap.put(wn.getWord(), neMap.get(wn.getWord())+wn.getWeight());
				} else {
					neMap.put(wn.getWord(), wn.getWeight());
				}
			} else {
				if (keywordMap.containsKey(wn.getWord())) {
					keywordMap.put(wn.getWord(), keywordMap.get(wn.getWord()) + wn.getWeight());
				} else {
					keywordMap.put(wn.getWord(), wn.getWeight());
				}
			}
		}
		
		
		List<Entry<String, Double>> list = new ArrayList<Entry<String, Double>>(neMap.entrySet());
		for (Entry<String, Double> entry:list) {
			entry.setValue(Math.log(entry.getKey().length()+2) * BM25.getWeight(entry.getKey(), entry.getValue(), content.length()));
		}
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		
		List<Entry<String, Double>> list2 = new ArrayList<Entry<String, Double>>(keywordMap.entrySet());
		for (Entry<String, Double> entry:list2) {
			entry.setValue(Math.log(entry.getKey().length()+2) * BM25.getWeight(entry.getKey(), entry.getValue(), content.length()));
		}
		Collections.sort(list2, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		if (list2.size() > 0) {
			if (list.size() > 4) {
				list = list.subList(0, 4);
			}
			if (list2.size() > 4) {
				list2 = list2.subList(0, 4);
			} 
			list.addAll(list2);
			
		}
		
		Collections.sort(list, new Comparator<Entry<String, Double>>() {
			public int compare(Entry<String, Double> o1,
					Entry<String, Double> o2) {
				if (o1.getValue() < o2.getValue()) {
					return 1;
				} else {
					return -1;
				}
			}
			
		});
		
		Set<String> addSet = new HashSet<String>();
		List<String> resultList = new ArrayList<String>();
		for (Entry<String, Double> entry:list) {
			if (!addSet.contains(entry.getKey())) {
				resultList.add(entry.getKey());
				addSet.add(entry.getKey());
			}
		}
		
		List<String> selected = new ArrayList<String>();
		for (String keyword:resultList) {
			boolean add = true;
			for (int i = 0; i < selected.size(); ++i) {
				String word = selected.get(i);
				if (word.contains(keyword)) {
					add = false;
					break;
				} else if (keyword.contains(word)) {
					selected.set(i, keyword);
					add = false;
					break;
				}
			}
			if (add) {
				selected.add(keyword);
			}
			if (selected.size() == 4) {
				break;
			}
		}
		
		return selected;
	}
	
	private static double getWeight(String pos) {
		if (pos.charAt(0) == 'x') {
			return 1.0;
		} else if (pos.startsWith("nr")) {
			return 1.0;
		} else if (pos.startsWith("nt")) {
			return 1.0;
		} else if (pos.startsWith("nz")) {
			return 1.0;
		} else if (pos.startsWith("nm")) {
			return 1.0;
		} else if (pos.equals("ns")) {
			return 1.0;
		} else if (pos.equals("nsf")) {
			return 0.7;
		} else if (pos.startsWith("nz")) {
			return 0.6;
		} else {
			return 1.0;
		}
	}

	public static void main(String[] args) {
		
//		String content = "“新视野号”项目首席科学家斯特恩指出,该探测器有一万分之一的概率，会因撞上冥王星周边的碎石残骸而功败垂成。“新视野号”订于美国东部时间下午4时20向地球发送信号报平安，但要近5小时后，科学家才会收到，因此在“新视野号”与冥王星近距离接触后大约13小时，美国宇航局才会宣布“新视野号”是否安然度过此次的高速邂逅。";
//		String content = "上半年第三产业增长8.4%，第三产业占国内生产总值（GDP）的比例占49.5%，上升到历史新高，目前第三产业排在第一位。从产业上看有上有下，下降的行业集中在高能耗、高物耗、高污染领域；上升的产业有高科技、中高端制造业及民生产业。新能源汽车销售增加二点四倍，计算机增加1.1倍，工业机器人销售增加87.5%，智能电视51.7%，服务业投资占全部投资56.3%";
//		String content = "今天清晨，一睁眼打开社交网络的人几乎被“优衣库”、“优衣库视频”、“优衣库试衣间”等词刷频，这些已登上微博热搜榜的近似关键词热传起因是一段不雅视频，在背景音显示地点优衣库北京三里屯店的试衣间内，一对男女正在做羞羞的事";
//		String content = "丁家宜，人们曾经无比熟悉的国产护肤品牌，诞生于1995年。 但2010年，丁家宜以24亿元“嫁入”全球最大香水公司、法国美容集团科蒂这个豪门后，却如同“小护士”嫁入欧莱雅般，每况愈下。2014年6月，科蒂宣布停止销售丁家宜系列产品。 但现在，丁家宜又要回来了。近日，多位知情人士透露，丁家宜创始人庄文阳已在今年初完成了对丁家宜品牌和工厂的全部回购，所用总金额不到一亿元，只相当于当年出售价格的零头。目前，新品牌的样品图已有流出，9月将上市膏霜新品并启动招商。 缘起 丁家宜教授的帝爱牌洗面奶 最开始，丁家宜是一个人的名字。 1939年，丁家宜出生于江苏沭阳，在7个兄弟姐妹里排老三。1949年，丁家宜的父亲随部队解放扬州，丁家宜从此在扬州长大，1958年从名校扬州中学考入北京农业大学植物生理学专业，曾发表我国第一篇有关“植物昼夜周期性研究”的报告。毕业后，他被分配到四川原子核应用技术研究所，“主要研究原子弹爆炸后对植物的辐射效应”。 文革中，丁家宜也受到冲击，被安排回到江苏南京，进入中国药科大学生药学研究室。上世纪70年代，他到长白山考察，看到当地洗参人不论男女老幼，一双手都特别白皙水嫩，与当地其他人风吹日晒下粗糙老化的皮肤形成强烈反差。丁家宜展开研究，终于发现原来是人参中含有多种促使人体“抗氧化”和清除“自由基”的缘故。 此后，丁家宜深入研究人参属植物生物技术，系统地完成了人参细胞培养，并萌生了将之运用到生活领域的念头。 但当时，药品、保健品、食品等领域对新技术的进入要求都很严格，一般是国外没有类似产品，国内都无法报批。不得已，丁家宜只能转而朝化妆品方向努力，研制出了洗面奶。 1987年，丁家宜以“人参活性细胞的培养方法”获国家专利，成为我国生物美白第一人。 1993年，在当时下海创业潮的激励下，中国药科大学神农生物技术公司成立，主打“帝爱牌”洗面奶。丁家宜介绍，这个产品当时只有几个系列，主要是面对学生市场，价格很便宜极受欢迎，年产值三四百万。但由于公司里都是学校的科研人员，基本不懂营销和管理，产品虽然卖得好，但很多经销商欠钱不还，导致陷入“卖得越多，亏得越多”的怪圈。 合作 台商庄文阳借来20万美元 正当丁家宜为企业未来发愁之际，台商庄文阳出现了。 庄文阳来自台湾苗栗一个四代务农家庭，本是制药厂业务员，月薪35000元（新台币）。内地改革开放后，他再也不甘心打工，一心想到大陆闯荡。很快，他到南京创业做化学原料贸易，但因竞争者蜂拥而至，一年之内25％的高利润就暴跌到5％以下，只好忍痛关门。痛定思痛，他决定不再碰低门坎的贸易业务。这时，他听说南京药科大学教授丁家宜从人参中萃取出了美白成分，当即意识到这是个天大的商机，立刻登门拜访。 庄文阳来自生活水平更高的台湾，熟知爱美女性的市场潜力，加上出身制药公司，与丁家宜一见如故，很快谈定了合作。庄文阳虽然是化妆品的门外汉，但据他观察，当时大陆的国产护肤品牌，不重视质量管理，更不懂得市场营销，他决心以产品研发与品牌打开市场。 1995年，34岁的庄文阳借来20万美元，和中国药科大学合作成立南京珈侬生化有限公司，庄负责采购和销售，丁家宜负责科研。 然而，珈侬公司销售帝爱系列化妆品的效果还是不理想";
//		String content = "有了新品牌，庄文阳开始强力拓展市场。在华北，他采取直营，华南则用经销代理。他派出三名南京大学毕业生担任丁家宜产品的华南地区经销商，直接到广东湛江开疆辟土。明亮摩登的专柜，琅琅上口的“面容一洗白”口号，再加上销售小姐强力宣传“一洗就白”的使用前后对照，产品开始热卖。“一支洗面奶6元出货，在市场卖到18元还缺货，所有经销商都抱着现金排队买货”，庄文阳创业第一年就赚了5000多万。 然而，越来越多的经销商意识到丁家宜产品的两倍利润空间，他们开始在各省之间窜货，把别省的货拿到浙江低价倒货，造成华东数省的丁家宜产品价格混乱，守规矩的经销商苦不堪言。最后庄文阳不得不花300多万，以17元的价格买回产品，仓库都堆满了，市场价格才算稳住。经过此役，他开始严控终端售价。 2000年，联合利华、宝洁等外资巨头相继入华，丁家宜产品顿时面临巨大压力。“过去赚机会财，现在要赚管理财”，庄文阳当即从东森购物、康师傅等名企找来商品营销与生产管理高手分析，认为外资将主攻城市市场，丁家宜则可以“上山下乡”。为此，他启动了下乡“11工程”，要攻下中国四万个乡镇中的一万个，每个乡镇贡献1000元业绩。 先是扩充产品线，丁家宜产品本来没有洗发水，但考虑到下乡抢市场，也开始推出洗发精、面膜、男性护肤产品，产品线从十数种拓展到一百多种品项。 其次是建起6个中转仓库，让一年8000万瓶产品的物流网络更通达，同时招募6000多位女销售扎根一线。当时全中国40000个开放式卖场，丁家宜就涵盖九成，连青海格尔木周边的小村落，都可以看到粉红色的丁家宜专柜——“中国内需市场的肉在农村，骨头在城市”，庄文阳说。每周日，他还会亲赴各省销售点突击检查，被称为“假日杀手”。";
//		String content = "今早看到谢霆锋和王菲复合的新闻真是有点惊讶，好了，不带感情色彩的科普下谢霆锋王菲张柏芝这十几年来的爱恨纠葛，一切从15年前说起";
//		String content = "关键词:  中国银行  国家队  管理层  中石油  概念 Ooo猪仔仔ooO 说:高转送龙头是春兴精工，次新是坚强的光力科技，暴跌时略略试了一点次新股和举牌概念，手上也拿着一点国家队中国银行，早上追高任何概念的暂时都吃套了，昨晚看好了一个军工想想还是算了，这种行情还真是坑你没商量，吃你不留骨，太可怕了";
//		String content = "山西省委书记王儒林再下严令关键词:  办公室  办公  王儒林  领导  干部潮平海顺 说:发表了博文《王儒林强调在办公室办公的现实意义》作为一个市委书记，作风懈怠，萎靡不振，经常不按时上班，晚来早走，或者半天上班、半天休息，我行我素，不听招呼，不守规矩，不守纪律，这是坚决不能允许的，对市委http://t.cn/RLMTDAV2015-7-20 14:59:20 转发（0）  评论（0）";
//		String content = "关键词:  令计划  开除  司法机关  党籍  公职 hfzhangyongoxi_49170a 说:【刘铁男严重违纪违法被双开】发改委原副主任刘铁男被处以开除党籍和行政开除处分；其违法违纪所得被收缴，涉嫌犯罪问题将移送司法机关依法处理。此前其曾被媒体人实名举报贪腐。2015-7-20 12:1:29 转发（0）  评论（0）";
		String content = "#王思聪范冰冰骂战##王思聪 操碎了心# 王思聪的两个爆料回复，你们擦亮眼睛看清楚点。为什么爆个料鄙视个人也会被炮轰，觉得他嚣张？所以他不爆料，把你们都蒙在鼓里就是好事了？别说他靠爹，是你们冲着他的身份去关注人家的。他起初红就是因为被称扒皮王，用他知道的真相给你们科普，还要被骂 ";
		List<String> re = extractForNewsClue(content);
		String result = "{'namedEntity':'"+re.get(0)+"','keyword':'"+re.get(1)+"'}";
		System.out.println(re);
		
		
	}
}
