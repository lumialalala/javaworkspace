package com.ict.mcg.forward;

/**
 * @author WuBo
 */

import java.io.File;
import java.io.IOException;

import org.gephi.graph.api.DirectedGraph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.api.ExportController;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.force.StepDisplacement;
import org.gephi.layout.plugin.force.yifanHu.YifanHuLayout;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.openide.util.Lookup;

import com.ict.mcg.util.RunTime;

public class GraphComputeWuBo {

	public void transfer(String path) {
		RunTime transfertime = new RunTime("gexf transfer 初始化过程");
		transfertime.GetStartTime();
		//Init a project - and therefore a workspace
		ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
		pc.newProject();
		Workspace workspace = pc.getCurrentWorkspace();
		//Get controllers and models
		ImportController importController = Lookup.getDefault().lookup(ImportController.class);
		transfertime.GetEndTime();
		double a = transfertime.ComputeRunTime();

		transfertime = new RunTime("gexf 文件载入");
		transfertime.GetStartTime();
		Container container;
		try {
			File file = new File(path);
			container = importController.importFile(file);
			// container.getLoader().setEdgeDefault(EdgeDefault.DIRECTED);   //Force DIRECTED
			container.setAllowAutoNode(false);  //Don't create missing nodes

		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

		//Append imported data to GraphAPI
		importController.process(container, new DefaultProcessor(), workspace);
		//See if graph is well imported
		GraphModel graphModel = Lookup.getDefault().lookup(GraphController.class).getModel();
		DirectedGraph graph = graphModel.getDirectedGraph();
		int nodecount = graph.getNodeCount();
		int edgescount = graph.getEdgeCount();
//		System.out.println("GexfTrans Nodes: " + nodecount);
//		System.out.println("GexfTrans Edges: " + edgescount);
		transfertime.GetEndTime();
		double b = transfertime.ComputeRunTime();

		transfertime = new RunTime("gexf 节点布局计算");
		transfertime.GetStartTime();
		//Run YifanHuLayout for 100 passes - The layout always takes the current visible view
		YifanHuLayout layout = new YifanHuLayout(null, new StepDisplacement(1f));
		layout.setGraphModel(graphModel);
		layout.initAlgo();
		layout.resetPropertiesValues();
		layout.setOptimalDistance(100f);
		int maxstep=2600;
		if(nodecount>maxstep*1.2)
			maxstep= (int) (nodecount*1.2) ;
		for (int i = 0; i < maxstep && layout.canAlgo(); i++) {
			layout.goAlgo();
		}
		layout.endAlgo();
		transfertime.GetEndTime();
		double c = transfertime.ComputeRunTime();

		try {
			transfertime = new RunTime("gexf 文件更新");
			transfertime.GetStartTime();
			//Export full graph
			ExportController ec = Lookup.getDefault().lookup(ExportController.class);
			ec.exportFile(new File(path));
//			ec.exportFile(new File(path.substring(0, path.lastIndexOf("_")) + ".gexf"));//改
			transfertime.GetEndTime();
			double d = transfertime.ComputeRunTime();
//			System.out.println("Gexf文件生成时间，初始化时间：" + a + "秒，gexf文件载入:" + b + "秒，图的节点布局计算:" + c + "秒，gexf文件更新:"+ d + "秒。" );
		} catch (IOException ex) {
			System.out.println("Gexf");
			ex.getMessage();
			ex.printStackTrace();
			return;
		}


	}
}
