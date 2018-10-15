package com.ict.mcg.veryfication.veryfy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.ict.mcg.gather.entity.WeiboEntityWrapper;
import com.ict.mcg.processs.Cluster;
import com.ict.mcg.processs.Node;
import com.ict.mcg.processs.Partition;
import com.ict.mcg.processs.SentimentAnalysis;
import com.ict.mcg.processs.WordNode;


/**
 * contruct a three layer credibility network then propagate
 * 
 * 
 */
public class NewsCP {
	// entities
	private double[] messages; // message vector
	private double[] subevents; // subevent vector
	private double event; // event
	private double[] m0; // initial message vector
	private double[] s0; // initial subevent vector
	private double e0; // initial event

	// relations (after regulization)
	private double[][] MatrixF; // relations between messages
	private double[][] MatrixH; // relations between subevents
	private double[][] MatrixG; // relations between messages and subevents
	private double[] MatrixP; // relations between subevents and event

	// parameters
	private double Pf = 0.3; // parameter control messages
	private double Ph = 0.06; // parameter control subevents
	private double Pg = 0.06; // parameter control message-subevent
	private double Pp = 0.5; // paratmeter control subevent-event

	double minSim = 0.6; // the treshhold of cluster algrithm

	private double Plambda = 0.5; // parameter for computing implicaitons

	// content
	HashMap<String, Double> allCreditMap; // the classification result of all
	// messages;
	private HashMap<Integer, WeiboEntityWrapper> allweibomap; // weibo map : index -

	// weibo
	
	
	private double ita = 0.1;

	/**
	 * loading classification result of all messages
	 * 
	 * @param allCreditFile
	 */
	private void loadAllCreditMap(String allCreditFile) {
		allCreditMap = new HashMap<String, Double>();
		File infile = new File(allCreditFile);
		BufferedReader reader;
		double d = 0.5;
		try {
			reader = new BufferedReader(new FileReader(infile));
			String line = "";
			while ((line = reader.readLine()) != null) {
				String[] ary = line.split(" ");
				String id = ary[0];
				double cre = (Double.parseDouble(ary[1]));
				cre = (cre - d);
				if (cre < 0) {
					cre = cre / d;
				} else {
					cre = cre / (1 - d);
				}
				if (allCreditMap.containsKey(id)) {
					double dup = allCreditMap.get(id);
					if (cre <= dup)
						continue;
				}
				allCreditMap.put(id, cre);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * initial graph
	 * 
	 * @param allCreditFile
	 * @param eventWeibofile
	 */
	private void initialGraph(String allCreditFile,
			ArrayList<WeiboEntityWrapper> welist) {

		if (allCreditMap == null)
			loadAllCreditMap(allCreditFile);
		// 1. load all weibo of the event and preprocessing
		ArrayList<WeiboEntityWrapper> allweibo = welist;
		// filter out wrong weibos
		ArrayList<WeiboEntityWrapper> weibofilter = new ArrayList<WeiboEntityWrapper>();
		for (WeiboEntityWrapper we : allweibo) {
			if (allCreditMap.containsKey(we.getMid())) {
				weibofilter.add(we);
			}
		}
		allweibo = weibofilter;
		// 处理文本并分词
		String file = "./file/dic";
		Partition p = new Partition(file);

		// for (WeiboEntity we : allweibo) {
		// contentProcess(we, p);
		// }

		for (WeiboEntityWrapper we : allweibo) {
			contentProcess(we, p);
		}

		// 2. clustering to get subevent
		int mcount = allweibo.size();//gai by zjq
		Cluster c = new Cluster();

		ArrayList<ArrayList<WeiboEntityWrapper>> clusters = c.singlePassClusterVTwo(
				allweibo, minSim);

		//new add.calculate creditMap using aging strengthen theory
//		Map<String, Double> agingCreditMap = agingStrengthen(allweibo);
				
		// 3. using cluster result to initialize message-subevent matrix and
		// message vector
		int scount = clusters.size();

		double[][] WMS = new double[mcount][scount]; // message-subevent matrix
		// (m*s)
		for (int i = 0; i < mcount; i++) {
			for (int j = 0; j < scount; j++) {
				WMS[i][j] = 0;
			}
		}
		allweibomap = new HashMap<Integer, WeiboEntityWrapper>();
		messages = new double[mcount];
		int messageindex = 0;
		for (int i = 0; i < clusters.size(); i++) {
			for (WeiboEntityWrapper we : clusters.get(i)) {
				String id = we.getMid();
				allweibomap.put(messageindex, we);


				double credit = 0.0;
				credit = allCreditMap.get(id);
				
				messages[messageindex] = credit;
				WMS[messageindex][i] = 1;
				messageindex++;
			}
		}
		m0 = new double[mcount];
		for (int i = 0; i < mcount; i++) {
			m0[i] = messages[i];
		}

		// 4. initialize the credit of event and subevent
		subevents = new double[clusters.size()];
		s0 = new double[clusters.size()];

		for (int i = 0; i < scount; i++) {
			double d = 0, count = 0;
			for (int j = 0; j < mcount; j++) {
				if (WMS[j][i] == 0)
					continue;
				d += messages[j];
				count++;
			}
			if (count > 0) {
				s0[i] = subevents[i] = d / count;
			}
		}

		event = 0;

		for (double d : subevents) {
			event += d;
		}
		event /= subevents.length;
		e0 = event;

		// 5. preprocess before computing implications
		ArrayList<ArrayList<Node>> subCentroid = new ArrayList<ArrayList<Node>>(); // centroids
		// for
		// sub-events
		ArrayList<Node> eventCentroid = new ArrayList<Node>(); // centroid for
		// event
		for (int i = 0; i < clusters.size(); i++) {
			subCentroid.add(Implication.computeCentoid(clusters.get(i)));
		}
		eventCentroid = Implication.computeCentoid(allweibo);

		double[] submaxprop = new double[clusters.size()];
		double[] suballprop = new double[clusters.size()];
		double eventmaxprop = 0;
		for (int i = 0; i < clusters.size(); i++) {
			double sum = 0, max = 0;
			for (WeiboEntityWrapper we : clusters.get(i)) {
				double prop = Double.parseDouble(we.getForword()
						+ Double.parseDouble(we.getComment()));
				we.setPropagation(prop);
				sum += prop;
				if (max < prop)
					max = prop;
			}
			submaxprop[i] = max;
			suballprop[i] = sum;
			if (eventmaxprop < max)
				eventmaxprop = max;
		}

		// 5. compute implications between messages and normalize
		double[][] WMM = new double[mcount][mcount]; // message-message matrix

		for (int k = 0; k < mcount; k++) {
			WeiboEntityWrapper weA = allweibomap.get(k);
			for (int j = 0; j <= k; j++) {
				if (k == j)
					WMM[k][j] = 0;
				WeiboEntityWrapper weB = allweibomap.get(j);
				double A2B = Implication.computeMMImp(weA, weB);
				WMM[k][j] = WMM[j][k] = A2B; // similarity should be symmetric
			}
		}
		// normalize
		MatrixF = new double[mcount][mcount];
		double[] DiaF = new double[mcount]; // 归一化对角阵
		for (int i = 0; i < mcount; i++) {
			double sum = 0;
			for (int j = 0; j < mcount; j++) {
				sum += WMM[i][j];
			}
			if (sum != 0)
				sum = 1.0 / Math.sqrt(sum);
			DiaF[i] = sum;
		}
		for (int i = 0; i < mcount; i++) {
			for (int j = 0; j < mcount; j++) {
				double w = WMM[i][j];
				w = w * DiaF[i] * DiaF[j]; // 行、列归一化
				MatrixF[i][j] = w;
			}
		}

		// 6. compute implications between subevents and normalize
		double[][] WSS = new double[scount][scount];

		for (int i = 0; i < scount; i++) {
			ArrayList<Node> subA = subCentroid.get(i);
			for (int j = 0; j <= i; j++) {
				if (j == i)
					WSS[i][j] = 0;
				ArrayList<Node> subB = subCentroid.get(j);
				double impA2B = Implication.computeSSImplication(subA, subB);
				WSS[i][j] = WSS[j][i] = impA2B;
			}
		}
		// normlize
		MatrixH = new double[scount][scount];
		double[] DiaH = new double[scount]; // 归一化对角阵
		for (int i = 0; i < scount; i++) {
			double sum = 0;
			for (int j = 0; j < scount; j++) {
				sum += WSS[i][j];
			}
			if (sum != 0)
				sum = 1.0 / Math.sqrt(sum);
			DiaH[i] = sum;
		}
		for (int i = 0; i < scount; i++) {
			for (int j = 0; j < scount; j++) {
				double w = WSS[i][j];
				w = w * DiaF[i] * DiaF[j]; // 行、列归一化
				MatrixH[i][j] = w;
			}
		}

		// 7. compute implications of message-subevent and normalize
		for (int i = 0; i < mcount; i++) {
			WeiboEntityWrapper weA = allweibomap.get(i);
			for (int j = 0; j < scount; j++) {
				if (WMS[i][j] == 0)
					continue;
				ArrayList<Node> subkey = subCentroid.get(j);
				double impA2E = Implication.computeMSImplication(weA, subkey,
						submaxprop[j], Plambda);
				WMS[i][j] = impA2E;
			}
		}
		// normalize
		MatrixG = new double[mcount][scount];
		double[] DiaGM = new double[mcount];
		double[] DiaGS = new double[scount];
		for (int i = 0; i < mcount; i++) {
			double sum = 0;
			for (int j = 0; j < scount; j++) {
				sum += WMS[i][j];
			}
			if (sum != 0)
				sum = 1.0 / Math.sqrt(sum);
			DiaGM[i] = sum;
		}
		for (int i = 0; i < scount; i++) {
			double sum = 0;
			for (int j = 0; j < mcount; j++) {
				sum += WMS[j][i];
			}

			if (sum != 0)
				sum = 1.0 / Math.sqrt(sum);

			DiaGS[i] = sum;
		}
		for (int i = 0; i < mcount; i++) {
			for (int j = 0; j < scount; j++) {
				double w = WMS[i][j];
				w = w * DiaGM[i] * DiaGS[j]; // 行、列归一化
				MatrixG[i][j] = w;
			}
		}

		// 8. compute implications of subevent-event and normalize
		double[] WSE = new double[scount];
		for (int i = 0; i < scount; i++) {
			ArrayList<Node> subevent = subCentroid.get(i);
			WSE[i] = Implication.computeSEImplication(subevent, eventCentroid,
					suballprop[i], eventmaxprop, Plambda);
		}
		// normalize
		MatrixP = new double[scount];
		double sum = 0;
		for (int i = 0; i < scount; i++) {
			sum += WSE[i];
		}
		if (sum != 0)
			sum = 1.0 / Math.sqrt(sum);
		for (int i = 0; i < scount; i++) {
			double d = WSE[i];
			if (d != 0)
				d = d * sum / Math.sqrt(d);
			MatrixP[i] = d;
		}
	}

	private double[] updateMessage() {
		int n = messages.length;
		double[] newMessages = new double[n];
		// compute other evidences' implication
		double[] mess = new double[n];
		for (int i = 0; i < n; i++) {
			double d = 0;
			for (int j = 0; j < n; j++) {
				d += MatrixF[i][j] * messages[j];
			}
			mess[i] = d;
		}
		// compute subevent's impliction
		double[] sub = new double[n];
		int m = subevents.length;
		for (int i = 0; i < n; i++) {
			double d = 0;
			for (int j = 0; j < m; j++) {
				d += MatrixG[i][j] * subevents[j];
			}
			sub[i] = d;
		}
		// combine
		for (int i = 0; i < n; i++) {
			newMessages[i] = messages[i] - ita*(messages[i] - (Pf * mess[i] + Pg * sub[i] + (1 - Pf - Pg) * m0[i]));
		}

		return newMessages;
	}

	private double[] updateSubevent() {
		int n = subevents.length;
		double[] newSubevents = new double[n];
		// compute other subevents' implication
		double[] sub = new double[n];
		for (int i = 0; i < n; i++) {
			double d = 0;
			for (int j = 0; j < n; j++) {
				d += MatrixH[i][j] * subevents[j];
			}
			sub[i] = d;
		}
		// compute message's impliction
		double[] mess = new double[n];
		int m = messages.length;
		for (int i = 0; i < n; i++) {
			double d = 0;
			for (int j = 0; j < m; j++) {
				d += MatrixG[j][i] * messages[i];
			}
			mess[i] = d;
		}
		// compute event's impliction
		double[] eve = new double[n];
		for (int i = 0; i < n; i++) {
			eve[i] = MatrixP[i] * event;
		}
		// combine
		for (int i = 0; i < n; i++) {
			newSubevents[i] = subevents[i] - ita*(subevents[i] - (Ph * sub[i] + Pg * mess[i] + Pp * eve[i]
					+ (1 - Ph - Pg - Pp) * s0[i]));
		}

		return newSubevents;
	}

	private double updateEvent() {
		double newEvent = 0;
		double sub = 0;
		for (int i = 0; i < subevents.length; i++) {
			sub += MatrixP[i] * subevents[i];
		}

		newEvent = event - ita*(event - (Pp * sub + (1 - Pp) * e0));

		return newEvent;
	}

	public void update(int maxIter) {
		int iter = 0;
		boolean iscontinue = true;

		if (maxIter <= 0)
			return;
		while (iscontinue) {
			if (iter >= maxIter)
				break;
			double[] newMessages = updateMessage();
			double[] newSubevents = updateSubevent();
			double newEvent = updateEvent();

			double tol = 0;
			tol = Math.abs(newEvent - event);

			messages = newMessages;
			subevents = newSubevents;
			event = newEvent;

			iter++;

			if (iter > 1 && tol < 0.0001)
				break;
		}

	}

//	public double run(String allCreditFile, ArrayList<WeiboEntityWrapper> welist, int i) {
//		this.initialGraph(allCreditFile, welist);
//		this.update(i);
//		return this.event;
//	}
	
	public double run(String allCreditFile, ArrayList<WeiboEntityWrapper> welist, int i) {
		if (allCreditMap == null) {
			loadAllCreditMap(allCreditFile);
		}
		
		ArrayList<WeiboEntityWrapper> weibofilter = new ArrayList<WeiboEntityWrapper>();
		for (WeiboEntityWrapper we : welist) {
			if (allCreditMap.containsKey(we.getMid())) {
				weibofilter.add(we);
			}
		}
		welist = weibofilter;
		double credit = 0.0;
		for (WeiboEntityWrapper we : welist) {
			credit += allCreditMap.get(we.getMid());
		}
		credit = credit / welist.size();
		
		return credit;
		
	}

	/**
	 * 处理文本并分词
	 * 
	 * @param we
	 */
	private void contentProcess(WeiboEntityWrapper we, Partition p) {
		String content = we.getContent();
		// 1.replace url
		Pattern pattern;
		Matcher matcher;

		pattern = Pattern
				.compile("[http]{4}\\:\\/\\/[a-z]*(\\.[a-zA-Z]*)*(\\/([a-zA-Z]|[0-9])*)*\\s?");
		matcher = pattern.matcher(content);
		content = matcher.replaceAll("");
		// 2. match at
		pattern = Pattern.compile("@[^\\.^\\,^:^;^!^\\?^\\s^#^@^。^，^：^；^！^？]+");
		matcher = pattern.matcher(content);
		content = matcher.replaceAll("");

		// 3. part word
		ArrayList<WordNode> al = p.participleAndMerge(content);
		we.setSegs(al);
		ArrayList<String> l = new ArrayList<String>();
		for (WordNode wn : al) {
			l.add(wn.getWord());
		}
		SentimentAnalysis sa = new SentimentAnalysis();
		double d = sa.getEmotionFromSentence(l);
		we.setSentiment(d);

		ArrayList<Node> nl = new ArrayList<Node>();
		HashMap<Integer, Integer> wmap = new HashMap<Integer, Integer>();

		int index = 0;
		for (WordNode w : al) {
			String s = w.getWord();
			int idx = -1;
			HashMap<String, Integer> vocabulary = new HashMap<String, Integer>();
			if (vocabulary.containsKey(s)) {
				idx = vocabulary.get(s);
			} else {
				vocabulary.put(w.getWord(), index);
				idx = index;
				index++;
			}

			if (wmap.containsKey(idx)) {
				wmap.put(idx, wmap.get(idx) + 1);
			} else {
				wmap.put(idx, 1);
			}
		}

		List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer, Integer>>(
				wmap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
			public int compare(Map.Entry<Integer, Integer> o1,
					Map.Entry<Integer, Integer> o2) {
				return (o1.getKey().compareTo(o2.getKey()));
			}
		});
		for (int i = 0; i < list.size(); i++) {
			Node n = new Node();
			n.setIndex(list.get(i).getKey());
			n.setWeight(list.get(i).getValue());
			nl.add(n);
		}

		we.setText(nl);

	}

	public void setParameters(double[] p, double minsim) {
		this.Pf = p[0];
		this.Pg = p[1];
		this.Ph = p[2];
		this.Pp = p[3];
		this.minSim = minsim;
	}
	
	public double alphaFun(double x, double mid, double b1, double b2) {
		double a2 = b2 / (1-mid)*(1-mid);
		double a1 = b1 / mid*mid;

		if (x <= mid) {
			return a1 * (x-mid)*(x-mid);
		} else {
			return a2 * (x-mid)*(x-mid);
		}
		
	}
}
