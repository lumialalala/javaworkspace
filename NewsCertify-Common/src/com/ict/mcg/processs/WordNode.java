package com.ict.mcg.processs;


public class WordNode {
	private String word = "";	
	private String pos = "";  //word的词性(通过分词工具获得)
	private int props = 0; //标记命名实体属性值
	private double weight = 0;
	
	public double getWeight(){
		return weight;
	}
	
	public void setWeight(double w){
		this.weight = w;
	}	

	public int getProps() {
		return props;
	}

	public void setProps(int props) {
		this.props = props;
	}

	public WordNode(String word, String pos) {
		this.word=word;
		this.pos = pos;
	}

	public WordNode() {
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}
}
