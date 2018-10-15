package com.ict.mcg.veryfication.veryfy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ict.mcg.gather.entity.WeiboEntityWrapper;//warn
import com.ict.mcg.processs.Node;


public class Implication {

	/**
	 * Compute the centroid word vector for sub-event, event
	 * 
	 * @param wel
	 * @return
	 */
	public static ArrayList<Node> computeCentoid(ArrayList<WeiboEntityWrapper> wel) {
		ArrayList<Node> centroid = new ArrayList<Node>();

		HashMap<Integer, Double> wmap = new HashMap<Integer, Double>();

		for (WeiboEntityWrapper we : wel) {
			ArrayList<Node> nl = we.getText();
			for (Node n : nl) {
				int idx = n.getIndex();
				double w = n.getWeight();
				if (wmap.containsKey(idx)) {
					wmap.put(idx, wmap.get(idx) + w);
				} else {
					wmap.put(idx,w);
				}
			}
		}

		List<Map.Entry<Integer, Double>> list = new ArrayList<Map.Entry<Integer, Double>>(
				wmap.entrySet());

		Collections.sort(list, new Comparator<Map.Entry<Integer, Double>>() {
			public int compare(Map.Entry<Integer, Double> o1,
					Map.Entry<Integer, Double> o2) {
				return (o1.getKey().compareTo(o2.getKey()));
			}
		});
		for (int i = 0; i < list.size(); i++) {
			Node n = new Node();
			n.setIndex(list.get(i).getKey());
			n.setWeight(list.get(i).getValue());
			centroid.add(n);
		}

		return centroid;
	}

	/**
	 * Compute messsage-message implication
	 * 
	 * @param we1
	 * @param we2
	 * @return
	 */
	public static double computeMMImp(WeiboEntityWrapper we1, WeiboEntityWrapper we2) {
		if (we1.getSentiment() * we2.getSentiment() < 0)
			return 0;

		ArrayList<Node> l1 = we1.getText();
		ArrayList<Node> l2 = we2.getText();

		int n1 = l1.size();
		int n2 = l2.size();

		if (n1 * n2 == 0)
			return 0;

		int i = 0, j = 0;

		double cor = 0;

		while (i < n1 && j < n2) {
			int idx1 = l1.get(i).getIndex();
			int idx2 = l2.get(j).getIndex();
			if (idx1 == idx2) {
				cor++;
				i++;
				j++;
			} else if (idx1 < idx2) {
				i++;
			} else {
				j++;
			}
		}

		int min = n1 < n2 ? n1 : n2;

		return cor / min;
	}

	/**
	 * compute sub-event to sub-event implication
	 * 
	 * @param c1
	 * @param c2
	 * @return
	 */
	public static double computeSSImplication(ArrayList<Node> c1,
			ArrayList<Node> c2) {
		double imp = 0;

		int n1 = c1.size();
		int n2 = c2.size();

		if (n1 * n2 == 0)
			return 0;

		int i = 0, j = 0;

		double cor = 0;

		while (i < n1 && j < n2) {
			int idx1 = c1.get(i).getIndex();
			int idx2 = c2.get(j).getIndex();

			if (idx1 == idx2) {
				cor += c1.get(i).getWeight() * c2.get(j).getWeight();
				i++;
				j++;
			} else if (idx1 < idx2) {
				i++;
			} else {
				j++;
			}
		}

		double norm1 = 0, norm2 = 0;
		for (int k = 0; k < n1; k++) {
			norm1 += c1.get(k).getWeight() * c1.get(k).getWeight();
		}
		norm1 = Math.sqrt(norm1);
		for (int k = 0; k < n2; k++) {
			norm2 += c2.get(k).getWeight() * c2.get(k).getWeight();
		}
		norm2 = Math.sqrt(norm2);

		imp = cor / (norm1 * norm2);

		return imp;
	}

	/**
	 * compute message to sub-event implication
	 * 
	 * @param we
	 * @param maxprop
	 * @param l
	 * @return
	 */
	public static double computeMSImplication(WeiboEntityWrapper we,
			ArrayList<Node> centroid, double maxprop, double lab) {
		double rel = computeSSImplication(we.getText(), centroid);
		double imp = (we.getPropagation() + 1) / (maxprop + 1);
		double result = lab * rel + (1 - lab) * imp;
		return result;

	}

	/**
	 * compute sub-event to event implication
	 * 
	 * @param subevent
	 * @param event
	 * @param prop
	 * @param maxprop
	 * @return
	 */

	public static double computeSEImplication(ArrayList<Node> subevent,
			ArrayList<Node> event, double prop, double maxprop, double lab) {
		double rel = computeSSImplication(subevent, event);
		double imp = (prop + 1) / (maxprop + 1);
		double result = lab * rel + (1 - lab) * imp;
		return result;
	}
}
