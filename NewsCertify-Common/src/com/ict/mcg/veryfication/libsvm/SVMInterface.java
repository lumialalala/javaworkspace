package com.ict.mcg.veryfication.libsvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class SVMInterface {
	public String predictEvent(String datafile, ArrayList<String> idlist,
			String[] models, String savepath) {
		ExecutorService pool = Executors.newFixedThreadPool(models.length);
		List<Future<String>> fuList = new ArrayList<Future<String>>();
		for (int i = 0; i < models.length; i++) {
			String outfile = savepath + "/result/part_" + i + ".predict";
			String[] parg = new String[5];
			parg[0] = "-b";
			parg[1] = "1";
			parg[2] = datafile;
			parg[3] = savepath + "/" + models[i];
			parg[4] = outfile;
			fuList.add(pool.submit(new svm_predict(parg)));
//			predict(datafile, savepath + "/" + models[i], outfile);
		}
		
		for (int i = 0; i < fuList.size(); ++i) {
			if (fuList.get(i) != null) {
				try {
					fuList.get(i).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		pool.shutdownNow();
		// combine result
		String allcredit = savepath + "/result/" + System.currentTimeMillis()
				+ ".result";

		ArrayList<Float> prelist = new ArrayList<Float>();
		for (int j = 0; j < models.length; j++) {
			String predictfile = savepath + "/result/part_" + j + ".predict";
			ArrayList<Float> resultlist = readResult(predictfile);
			for (int k = 0; k < resultlist.size(); k++) {
				Float s = resultlist.get(k);
				if (prelist.size() < resultlist.size()) {
					prelist.add(s);
				} else {
					prelist.set(k, s + prelist.get(k));
				}
			}
		}
		for (int k = 0; k < prelist.size(); k++) {
			prelist.set(k, prelist.get(k) / models.length);
		}

		try {
			Writer writer = new FileWriter(allcredit, true);
			for (int j = 0; j < idlist.size(); j++) {

				writer.write(idlist.get(j) + " " + prelist.get(j) + "\r\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// delete temporary files
		for (int j = 0; j < models.length; j++) {
			String predictfile = savepath + "/result/part_" + j + ".predict";
			File f = new File(predictfile);
			if (f.exists()) {
				f.delete();
			}
		}
		return allcredit;
	}

	public float predictEvent(String datafile, String[] models, String savepath) {
		ExecutorService pool = Executors.newFixedThreadPool(models.length);
		List<Future<String>> fuList = new ArrayList<Future<String>>();
		for (int i = 0; i < models.length; i++) {
			String outfile = savepath + "/result/part_" + i + ".predict";
			String[] parg = new String[5];
			parg[0] = "-b";
			parg[1] = "1";
			parg[2] = datafile;
			parg[3] = savepath + "/" + models[i];
			parg[4] = outfile;
			fuList.add(pool.submit(new svm_predict(parg)));
//			predict(datafile, savepath + "/" + models[i], outfile);
		}
		
		for (int i = 0; i < fuList.size(); ++i) {
			if (fuList.get(i) != null) {
				try {
					fuList.get(i).get();
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			}
		}
		pool.shutdownNow();

		ArrayList<Float> prelist = new ArrayList<Float>();
		for (int j = 0; j < models.length; j++) {
			String predictfile = savepath + "/result/part_" + j + ".predict";
			ArrayList<Float> resultlist = readResult(predictfile);
			for (int k = 0; k < resultlist.size(); k++) {
				Float s = resultlist.get(k);
				if (prelist.size() < resultlist.size()) {
					prelist.add(s);
				} else {
					prelist.set(k, s + prelist.get(k));
				}
			}
		}
		for (int k = 0; k < prelist.size(); k++) {
			prelist.set(k, prelist.get(k) / models.length);
		}

		// delete temporary files
		for (int j = 0; j < models.length; j++) {
			String predictfile = savepath + "/result/part_" + j + ".predict";
			File f = new File(predictfile);
			if (f.exists()) {
				f.delete();
			}
		}
		return prelist.get(0);
	}
	

	/**
	 * training a svm model with gamma and cost and b =1
	 * 
	 * @param g
	 *            gamma
	 * @param c
	 *            cost
	 * @param traindata
	 * @param modelpath
	 */
	private static void trainModel(double g, double c, String traindata,
			String modelpath) {

		String[] arg = new String[10];

		arg[0] = "-b";
		arg[1] = "1";
		arg[2] = "-g";
		arg[3] = "" + g;
		arg[4] = "-c";
		arg[5] = "" + c;
		arg[6] = "-h";
		arg[7] = "" + 0;

		arg[8] = traindata;
		arg[9] = modelpath;

		try {
			svm_train.main(arg);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * predict values in a file
	 * 
	 * @param testdata
	 * @param model
	 * @param outfile
	 */
	private static ArrayList<Float> readResult(String predictfile) {
		try {

			// read idfile
			ArrayList<Float> list = new ArrayList<Float>();
			BufferedReader reader = new BufferedReader(new FileReader(
					predictfile));
			String line1 = null;
			
			line1 = reader.readLine();
			int labels[] = new int[2];
			String[] strs = line1.split(" ");
			labels[0] = Integer.parseInt(strs[1]);
			labels[1] = Integer.parseInt(strs[2]);
			
			while ((line1 = reader.readLine()) != null) {
				String[] ar = line1.split(" ");
				float[] pros = new float[3];
				pros[0] = Float.parseFloat(ar[0]); 
				pros[labels[0]+1] = Float.parseFloat(ar[1]); 
				pros[labels[1]+1] = Float.parseFloat(ar[2]); 
				
				list.add(pros[2]);
			}
			reader.close();
			return list;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
