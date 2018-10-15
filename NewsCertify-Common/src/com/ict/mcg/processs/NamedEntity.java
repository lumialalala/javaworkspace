/**
 * 
 */
package com.ict.mcg.processs;

import java.util.ArrayList;

/**
 * 命名实体分类
 * 
 * @author JZW
 * 
 */
public class NamedEntity {
	public static final int PERSON = 1;
	public static final int ORGANIZATION = 2;
	public static final int REGION = 3;
	public static final int GENERAL = 4;

	/**
	 * 获取pos对应的命名实体属性
	 * 
	 * @param pos
	 * @return
	 */
	public static int getProps(String pos) {
		if (pos.equals("nr") || pos.equals("nrj") || pos.equals("nrf")) {
			return PERSON;
		} else if (pos.equals("nt")) {
			return ORGANIZATION;
		} else if (pos.equals("ns") || pos.equals("nsf")) {
			return REGION;
		} else {
			return GENERAL;
		}
	}

	public void setProps(ArrayList<WordNode> allWord) {
		for (WordNode w : allWord) {
			String pos = w.getPos();
			int props = getProps(pos);
			w.setProps(props);			
		}
	}
}
