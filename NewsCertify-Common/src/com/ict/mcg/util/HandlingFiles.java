package com.ict.mcg.util;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
/**
 * 
  * Class Name ：HandlingFiles
  * Class Description ：
  * Creation Time ：2014-1-16 上午11:01:40
  *
 */
public class HandlingFiles {
	// 验证字符串是否为正确路径名的正则表达式
		private static String matches = "[A-Za-z]:\\\\[^:?\"><*]*";
		// 通过 sPath.matches(matches) 方法的返回值判断是否正确
		// sPath 为路径字符串
		boolean flag = false;
		File file;

		public boolean DeleteFolder(String deletePath) {// 根据路径删除指定的目录或文件，无论存在与否
			flag = false;
			if (deletePath.matches(matches)) {
				file = new File(deletePath);
				if (!file.exists()) {// 判断目录或文件是否存在
					return flag; // 不存在返回 false
				} else {

					if (file.isFile()) {// 判断是否为文件
						return deleteFile(deletePath);// 为文件时调用删除文件方法
					} else {
						return deleteDirectory(deletePath);// 为目录时调用删除目录方法
					}
				}
			} else {
				System.out.println("要传入正确路径！");
				return false;
			}
		}

		public boolean deleteFile(String filePath) {// 删除单个文件
			flag = false;
			file = new File(filePath);
			if (file.isFile() && file.exists()) {// 路径为文件且不为空则进行删除
				file.delete();// 文件删除
				flag = true;
			}
			return flag;
		}

		public boolean deleteDirectory(String dirPath) {// 删除目录（文件夹）以及目录下的文件
			// 如果sPath不以文件分隔符结尾，自动添加文件分隔符
			if (!dirPath.endsWith(File.separator)) {
				dirPath = dirPath + File.separator;
			}
			File dirFile = new File(dirPath);
			// 如果dir对应的文件不存在，或者不是一个目录，则退出
			if (!dirFile.exists() || !dirFile.isDirectory()) {
				return false;
			}
			flag = true;
			File[] files = dirFile.listFiles();// 获得传入路径下的所有文件
			for (int i = 0; i < files.length; i++) {// 循环遍历删除文件夹下的所有文件(包括子目录)
				if (files[i].isFile()) {// 删除子文件
					flag = deleteFile(files[i].getAbsolutePath());
//					System.out.println(files[i].getAbsolutePath() + " 删除成功");
					if (!flag)
						break;// 如果删除失败，则跳出
				} else {// 运用递归，删除子目录
					flag = deleteDirectory(files[i].getAbsolutePath());
					if (!flag)
						break;// 如果删除失败，则跳出
				}
			}
			if (!flag)
				return false;
			if (dirFile.delete()) {// 删除当前目录
				return true;
			} else {
				return false;
			}
		}

		// 创建单个文件
		public static boolean createFile(String filePath) {
			File file = new File(filePath);
			if (file.exists()) {// 判断文件是否存在
				System.out.println("File exists" + filePath);
				return false;
			}
			if (filePath.endsWith(File.separator)) {// 判断文件是否为目录
				System.out.println("File can't be dirctory");
				return false;
			}
			if (!file.getParentFile().exists()) {// 判断目标文件所在的目录是否存在
				// 如果目标文件所在的文件夹不存在，则创建父文件夹
				System.out.println("目标文件所在目录不存在，准备创建它！");
				if (!file.getParentFile().mkdirs()) {// 判断创建目录是否成功
					System.out.println("创建目标文件所在的目录失败！");
					return false;
				}
			}
			try {
				if (file.createNewFile()) {// 创建目标文件
					System.out.println("创建文件成功:" + filePath);
					return true;
				} else {
					System.out.println("创建文件失败！");
					return false;
				}
			} catch (IOException e) {// 捕获异常
				e.printStackTrace();
				System.out.println("创建文件失败！" + e.getMessage());
				return false;
			}
		}

		/**
		 * Create directory
		 * @param destDirName
		 * @param mode 1 to override, 0 for not
		 * @return
		 */
		public static boolean createDir(String destDirName,int mode) {
			File dir = new File(destDirName);
			if (dir.exists()) {// 判断目录是否存在	
				if (mode == 1) {
					HandlingFiles handlingFiles = new HandlingFiles();
					handlingFiles.deleteDirectory(destDirName);
					dir = new File(destDirName);
				}else {
					System.out.println("Create Dirctory Failed!! Directory already exists!");
					return false;
				}
				
			}
			if (!destDirName.endsWith(File.separator)) {// 结尾是否以"/"结束
				destDirName = destDirName + File.separator;
			}
			if (dir.mkdirs()) {// 创建目标目录
				System.out.println("Create dirctory successfully !!" + destDirName);
				return true;
			} else {
				System.out.println("Create Dirctory Failed！");
				return false;
			}
		}
		/**
		 * Determine whether there is a directory
		 * @param dirName
		 * @return
		 */
		public static boolean isDirExist(String dirName ) {
			File dir = new File(dirName);
			if (dir.exists()) {
				return true;
			}else {
				return false;
			}
		}
		// 创建临时文件
		public static String createTempFile(String prefix, String suffix,
				String dirName) {
			File tempFile = null;
			if (dirName == null) {// 目录如果为空
				try {
					tempFile = File.createTempFile(prefix, suffix);// 在默认文件夹下创建临时文件
					return tempFile.getCanonicalPath();// 返回临时文件的路径
				} catch (IOException e) {// 捕获异常
					e.printStackTrace();
					System.out.println("创建临时文件失败：" + e.getMessage());
					return null;
				}
			} else {
				// 指定目录存在
				File dir = new File(dirName);// 创建目录
				if (!dir.exists()) {
					// 如果目录不存在则创建目录
					if (HandlingFiles.createDir(dirName,0)) {
						System.out.println("创建临时文件失败，不能创建临时文件所在的目录！");
						return null;
					}
				}
				try {
					tempFile = File.createTempFile(prefix, suffix, dir);// 在指定目录下创建临时文件
					return tempFile.getCanonicalPath();// 返回临时文件的路径
				} catch (IOException e) {// 捕获异常
					e.printStackTrace();
					System.out.println("创建临时文件失败!" + e.getMessage());
					return null;
				}
			}
		}
/**
 * write line in the end :append model
 * @param path
 * @param content
 */
		public static void writeInEnd(String path, String content) {
			try {
				File f = new File(path);
				if (!f.exists()) {
					createFile(path);
				}
				BufferedWriter output = new BufferedWriter(new FileWriter(f,true));
				String newStr = new String(content.getBytes(), "UTF-8"); 
				output.write(newStr);
				output.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		/**
		 * 修改第一行的数据
		 * @param fpath
		 * @param replaceStr
		 */
		public static void replaceTxtByStr(String fpath,String replaceStr) {
	        String temp = "";
	        try {
	            File file = new File(fpath);
	            FileInputStream fis = new FileInputStream(file);
	            InputStreamReader isr = new InputStreamReader(fis);
	            BufferedReader br = new BufferedReader(isr);
	            StringBuffer buf = new StringBuffer();        
	            br.readLine();
	            // 将内容插入
	            buf = buf.append(replaceStr);
	            
	           int i = 0;
	            // 保存该行后面的内容
	            while ((temp = br.readLine()) != null) {
	            	i =1;
	                buf = buf.append(System.getProperty("line.separator"));
	                buf = buf.append(temp);
	            }
	            if (i ==0) {
					buf = buf.append(System.getProperty("line.separator"));
				}
	            br.close();
	            FileOutputStream fos = new FileOutputStream(file);
	            PrintWriter pw = new PrintWriter(fos);
	            pw.write(buf.toString().toCharArray());
	            pw.flush();
	            pw.close();
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	    }
		
//		public static void main(String[] args) throws IOException {
//			
//			int count = 2432543;
//			replaceTxtByStr("D:/pageCnt.txt", String.valueOf(count));
//			String visitDetectionPath = "D:/visitDetect";
//			if (!isDirExist(visitDetectionPath)) {
//				createDir(visitDetectionPath, 0);
//			}
//			String totalVisitPath = visitDetectionPath+File.separator+"totalVisit.txt";
//			String visitRecordPath = visitDetectionPath+File.separator+"visitRecord.txt";
//			createFile(totalVisitPath);		
//			createFile(visitRecordPath);
//			
//			
//		}
}
