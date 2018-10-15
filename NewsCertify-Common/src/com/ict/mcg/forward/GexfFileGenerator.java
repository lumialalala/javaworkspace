package com.ict.mcg.forward;
/**
 * @author WuBo
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.List;

public class GexfFileGenerator {
	
	private String mPath;
	private double id;
	private String label;
	private String attvalues[];
	private double size;
	private int color[];
	private double pos[];
	private OutputStreamWriter mWriter;
	
	public GexfFileGenerator(String path) throws UnsupportedEncodingException, FileNotFoundException {
		mPath = path;
		mWriter = new OutputStreamWriter(new FileOutputStream(mPath),"UTF-8");
		attvalues = new String[4];
		color = new int[3];
		pos = new double[3];
	}

	public boolean input(double id, String label, String attvalues[],
			double sizeValue, int colorValue[], double positions[]) {
		if (attvalues == null || colorValue == null
				|| colorValue.length != 3 || positions == null
				|| positions.length != 3) {
			
			return false;
		}

		this.id = id;
		this.label = label;
		this.attvalues = attvalues.clone();
		this.size = sizeValue;
		this.color = colorValue.clone();
		this.pos = positions.clone();
		return true;
	}

	public boolean outputHead() {
		try {
			mWriter.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + "\n");
			mWriter.write("<gexf xmlns:viz=\"http:///www.gexf.net/1.1draft/viz\" version=\"1.1\" xmlns=\"http://www.gexf.net/1.1draft\">"
					+ "\n");
			mWriter.write("<meta lastmodifieddate=\"2010-05-29+01:27\">" + "\n");
			mWriter.write("<Creator>WuBo@ictcas</Creator>" + "\n");
			mWriter.write("<Title>Les Misérables, the characters coappearance weighted graph</Title>"
					+ "\n");
			mWriter.write("</meta>" + "\n");
			mWriter.write("<graph defaultedgetype=\"directed\" idtype=\"string\" type=\"static\">"
					+ "\n");
			mWriter.write("<attributes class=\"node\" mode=\"static\">" + "\n");
			mWriter.write("<attribute id=\"uid\" title=\"Uid\" type=\"string\"/>" 
					+ "\n");
			mWriter.write("<attribute id=\"fanc\" title=\"Fanc\" type=\"string\"/>"
					+ "\n");
			mWriter.write("<attribute id=\"loc\" title=\"Loc\" type=\"string\"/>"
					+ "\n");
			mWriter.write("<attribute id=\"des\" title=\"Des\" type=\"string\"/>"
					+ "\n");
			mWriter.write("</attributes>" + "\n");

			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean outputN(List<String> labelList,	List<String[]> attList, 
			List<double[]> sizeList, List<int[]> colorList,
			List<double[]> posList) {
		try {
			System.out.println("\nNodeListSize:" +labelList.size() );
			outputHead();
			mWriter.write("<nodes count=\"" + labelList.size() + "\">" + "\n");
			int i = 0;
			while (i != labelList.size()) {
				input((double) i, labelList.get(i), attList.get(i), sizeList.get(i)[0],
						colorList.get(i), posList.get(i));
				if (!outputNode())
					return false;
				i++;
			}
			mWriter.write("</nodes>" + "\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean outputE(List<double[]> edgeList) {
		try {
	
			mWriter.write("<edges count=\"" + edgeList.size() + "\">" + "\n");
			int count = 0;
			while (count != edgeList.size()) {
				if (outputEdge(count, edgeList.get(count))) {
					count++;
				}else return false;
			}
			mWriter.write("</edges>" + "\n");
			mWriter.write("</graph>" + "\n");
			mWriter.write("</gexf>" + "\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public boolean outputNode() {
		try {
			mWriter.write("<node id=\"" + id + "\" label=\"" + label + "\">"
					+ "\n");
			outputAttvalues();
			outputViz();
			mWriter.write("</node>" + "\n");

			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean closeWriter(){
		try {
			mWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return true;
	}
	
	private boolean outputAttvalues() {
		try {
			mWriter.write("<attvalues>" + "\n");
			mWriter.write("<attvalue for=\"uid\" value=\"" + attvalues[0]
					+ "\"/>" + "\n");
			mWriter.write("<attvalue for=\"fanc\" value=\"" + attvalues[1]
					+ "\"/>" + "\n");
			mWriter.write("<attvalue for=\"loc\" value=\"" + attvalues[2]
			        + "\"/>" + "\n");
			mWriter.write("<attvalue for=\"des\" value=\"" + attvalues[3]
			        + "\"/>" + "\n");
			mWriter.write("</attvalues>" + "\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean outputViz() {
		try {
			mWriter.write("<viz:size value=\"" + size + "\"/>" + "\n");
			mWriter.write("<viz:color b=\"" + color[0] + "\" g=\"" + color[1]
					+ "\" r=\"" + color[2] + "\"/>" + "\n");
			mWriter.write("<viz:position x=\"" + pos[0] + "\" y=\"" + pos[1]
					+ "\" z=\"" + pos[2] + "\"/>" + "\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	private boolean outputEdge(int id, double[] edge) {
		if (edge == null || edge.length != 3) {
			return false;
		}
		try {
			mWriter.write("<edge id=\"" + id + "\" source=\"" + edge[0]
					+ "\" target=\"" + edge[1]);
			if (edge[2] > 0) {
				mWriter.write("\" weight=\"" + edge[2] + "\"");
			}
			mWriter.write("\"/>" + "\n");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	



}
