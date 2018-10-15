package com.ict.mcg.util;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLResult;
import org.dom4j.io.XMLWriter;

public class XmlUtil {

	static class StringUtil {
		public static boolean strIsNullOrEmpty(String str) {
			return (str == null || str.length() == 0);
		}
	}
	/***************************************************************************
	 * 通过xml节点获取下一级节点元素值，并进行空值处理
	 * 
	 * @param ele
	 *            节点
	 * @param tag
	 *            标签
	 * @param nullVal
	 *            空值处理
	 * @return
	 */
	public static String getXmlChildElementTextByElementTag(Element ele, String tag, String nullVal) {
		Element eleChild = ele.element(tag);
		if (null == eleChild) {
			return nullVal;
		} else {
			String eleText = eleChild.getTextTrim();
			// return eleText == null ? nullVal : eleText;
			return StringUtil.strIsNullOrEmpty(eleText) ? nullVal : eleText;
		}
	}

	/***************************************************************************
	 * 通过xml节点获取当前节点元素值，并进行空值处理
	 * 
	 * @param ele
	 *            节点
	 * @param tag
	 *            标签
	 * @param nullVal
	 *            空值处理
	 * @return
	 */
	public static String getXmlElementTextByElementTag(Element ele, String tag, String nullVal) {
		if (null == ele) {
			return nullVal;
		} else {
			String eleText = ele.getTextTrim();
			// return eleText == null ? nullVal : eleText;
			return StringUtil.strIsNullOrEmpty(eleText) ? nullVal : eleText;
		}
	}

	/***************************************************************************
	 * 通过xml节点获取属性值，并进行空值处理
	 * 
	 * @param ele
	 *            节点
	 * @param attrName
	 *            属性名
	 * @param nullVal
	 *            空值处理
	 * @return
	 */
	public static String getXmlAttributeValueByElement(Element ele, String attrName, String nullVal) {
		String attrVal = ele.attributeValue(attrName);
		return StringUtil.strIsNullOrEmpty(attrVal) ? nullVal : attrVal;
	}

	/***************************************************************************
	 * string2Document 将字符串转为Document
	 * 
	 * @return
	 * @param s
	 *            xml格式的字符串
	 */
	public static Document string2Document(String s) {
		Document doc = null;
		try {
			doc = DocumentHelper.parseText(s);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return doc;
	}

	/***************************************************************************
	 * 过虑xml的无效字符。<p/>
	 * <ol>
	 * <li>0x00 - 0x08</li>
	 * <li>0x0b - 0x0c</li>
	 * <li>0x0e - 0x1f</li>
	 * </ol>
	 * 
	 * @author chenlb 2008-11-7 下午04:27:48
	 */
	public static String xmlfilter(String xmlStr) {
		StringBuilder sb = new StringBuilder();
		char[] chs = xmlStr.toCharArray();
		// System.out.println("filter before=" +chs.length);
		for (char ch : chs) {
			if ((ch >= 0x00 && ch <= 0x08) || (ch >= 0x0b && ch <= 0x0c) || (ch >= 0x0e && ch <= 0x1f)) {
				// eat...
			} else {
				sb.append(ch);
			}
		}
		// System.out.println("filter after=" +sb.length());
		return sb.toString();
	}

	/***************************************************************************
	 * 
	 * @param str
	 * @return
	 */
	public static String xmlStrFilter(String str) {
		String regex = "[\\x00-\\x08\\x0b-\\x0c\\x0e-\\x1f]";
		if (str != null) {
			str = str.replaceAll(regex, "");
		}
		return str;
	}

	/***************************************************************************
	 * 
	 * @param str
	 * @return
	 */
	public static String xmlStrParser(String str) {
		String regex = "<[^<]*/>";
		if (str != null) {
			str = str.replaceAll(regex, "");
		}
		return str;
	}

	/***************************************************************************
	 * 
	 * @param str
	 * @return
	 */
	public static String xmlInvalidParser(String str) {
		String regex = "&#\\d*;";
		if (str != null) {
			str = str.replaceAll(regex, "");
		}
		return str;
	}

	/***************************************************************************
	 * 使用DOM4J 将Document对象保存到xml文件中
	 * 
	 * @param doc
	 * @param xmlFilePath
	 * @param encoding
	 */
	public static void writeXmlFileByDom4J(Document doc, String xmlFilePath, String encoding) {
		BufferedOutputStream bos = null;
		XMLWriter writer = null;
		try {
			OutputFormat format = OutputFormat.createPrettyPrint();
			format.setEncoding(encoding);
			format.setIndent(true);
			bos = new BufferedOutputStream(new FileOutputStream(xmlFilePath));
			writer = new XMLResult(bos , format).getXMLWriter();
			if (doc != null) {
				writer.write(doc);
			}
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			try {
				if(null!=writer){
					writer.close();
					writer = null;
				}
				if(null!=bos){
					bos.close();
					bos = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {

	}

}
