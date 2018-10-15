/**
 * Old ICTAnalyzer, Now use Ansj instead
 */
package com.ict.mcg.processs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.ict.mcg.util.DateHandle;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

import com.ict.mcg.util.DataHandle;

public class ICTAnalyzer {
	public static final int POS_YES = 1;
	public static final int POS_NO = 0;

	public static ArrayList<String> analyzeParagraph(String str, int postTag) {
		String commonStr = DataHandle.replaceCommonStr(str);
		ArrayList<String> rst = new ArrayList<String>();
		HashSet<String> hash = new HashSet<String>();
		List<Term> parse = ToAnalysis.parse(commonStr).getTerms();

		if(postTag == 1){
			for(Term t : parse)
				rst.add(t.toString());
		}
		else if(postTag == 0){
			for(Term t: parse){
				if(!rst.contains(t.getName().trim()))
					rst.add(t.getName().trim());
			}
		}
		return rst;
	}
	
	public static void main(String[] args){
		String str = "丁家宜，人们曾经无比熟悉的国产护肤品牌，诞生于1995年。 但2010年，丁家宜以24亿元“嫁入”全球最大香水公司、法国美容集团科蒂这个豪门后，却如同“小护士”嫁入欧莱雅般，每况愈下。2014年6月，科蒂宣布停止销售丁家宜系列产品。 但现在，丁家宜又要回来了。近日，多位知情人士透露，丁家宜创始人庄文阳已在今年初完成了对丁家宜品牌和工厂的全部回购，所用总金额不到一亿元，只相当于当年出售价格的零头。目前，新品牌的样品图已有流出，9月将上市膏霜新品并启动招商。 缘起 丁家宜教授的帝爱牌洗面奶 最开始，丁家宜是一个人的名字。 1939年，丁家宜出生于江苏沭阳，在7个兄弟姐妹里排老三。1949年，丁家宜的父亲随部队解放扬州，丁家宜从此在扬州长大，1958年从名校扬州中学考入北京农业大学植物生理学专业，曾发表我国第一篇有关“植物昼夜周期性研究”的报告。毕业后，他被分配到四川原子核应用技术研究所，“主要研究原子弹爆炸后对植物的辐射效应”。 文革中，丁家宜也受到冲击，被安排回到江苏南京，进入中国药科大学生药学研究室。上世纪70年代，他到长白山考察，看到当地洗参人不论男女老幼，一双手都特别白皙水嫩，与当地其他人风吹日晒下粗糙老化的皮肤形成强烈反差。丁家宜展开研究，终于发现原来是人参中含有多种促使人体“抗氧化”和清除“自由基”的缘故。 此后，丁家宜深入研究人参属植物生物技术，系统地完成了人参细胞培养，并萌生了将之运用到生活领域的念头。 但当时，药品、保健品、食品等领域对新技术的进入要求都很严格，一般是国外没有类似产品，国内都无法报批。不得已，丁家宜只能转而朝化妆品方向努力，研制出了洗面奶。 1987年，丁家宜以“人参活性细胞的培养方法”获国家专利，成为我国生物美白第一人。 1993年，在当时下海创业潮的激励下，中国药科大学神农生物技术公司成立，主打“帝爱牌”洗面奶。丁家宜介绍，这个产品当时只有几个系列，主要是面对学生市场，价格很便宜极受欢迎，年产值三四百万。但由于公司里都是学校的科研人员，基本不懂营销和管理，产品虽然卖得好，但很多经销商欠钱不还，导致陷入“卖得越多，亏得越多”的怪圈。 合作 台商庄文阳借来20万美元 正当丁家宜为企业未来发愁之际，台商庄文阳出现了。 庄文阳来自台湾苗栗一个四代务农家庭，本是制药厂业务员，月薪35000元（新台币）。内地改革开放后，他再也不甘心打工，一心想到大陆闯荡。很快，他到南京创业做化学原料贸易，但因竞争者蜂拥而至，一年之内25％的高利润就暴跌到5％以下，只好忍痛关门。痛定思痛，他决定不再碰低门坎的贸易业务。这时，他听说南京药科大学教授丁家宜从人参中萃取出了美白成分，当即意识到这是个天大的商机，立刻登门拜访。 庄文阳来自生活水平更高的台湾，熟知爱美女性的市场潜力，加上出身制药公司，与丁家宜一见如故，很快谈定了合作。庄文阳虽然是化妆品的门外汉，但据他观察，当时大陆的国产护肤品牌，不重视质量管理，更不懂得市场营销，他决心以产品研发与品牌打开市场。 1995年，34岁的庄文阳借来20万美元，和中国药科大学合作成立南京珈侬生化有限公司，庄负责采购和销售，丁家宜负责科研。 然而，珈侬公司销售帝爱系列化妆品的效果还是不理想";
		ArrayList<String> arr = analyzeParagraph(str, 1);
		for(String s : arr){
			System.out.println(s);
		}
	}
}
