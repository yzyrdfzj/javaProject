package com.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Nickwong on 31/07/2018. 根据1-8楼的建议，优化了代码
 */
public class ReadWriteTxtForLog {
	private String path;// 路径
	private String name;// 文件名
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YMMddHHmmss");

	/**
	 * 
	 * @param path
	 *            路径
	 * @param name
	 *            文件名
	 */
	public ReadWriteTxtForLog(String path, String name) {
		this.path = path;
		this.name = name;
	}

	/**
	 * 读入TXT文件
	 */
	public void readFile() {
		String pathname = "input.txt"; // 绝对路径或相对路径都可以，写入文件时演示相对路径,读取以上路径的input.txt文件
		// 防止文件建立或读取失败，用catch捕捉错误并打印，也可以throw;
		// 不关闭文件会导致资源的泄露，读写文件都同理
		// Java7的try-with-resources可以优雅关闭文件，异常时自动关闭文件；详细解读https://stackoverflow.com/a/12665271
		try (
				FileReader reader = new FileReader(pathname); 
				BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
		) {
			String line;
			// 网友推荐更加简洁的写法
			while ((line = br.readLine()) != null) {
				// 一次读入一行数据
				System.out.println(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 覆盖方式写入TXT文件
	 */
	public void writeFile() {
		try {
			File writeName = new File("C:\\writeError\\test.txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
			writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
			try (FileWriter writer = new FileWriter(writeName); BufferedWriter out = new BufferedWriter(writer)) {
				out.write("我会写入文件啦1\r\n"); // \r\n即为换行
				out.write("我会写入文件啦2\r\n"); // \r\n即为换行
				out.flush(); // 把缓存区内容压入文件
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 追加方式写入TXT文件
	 */
	public void writeFile2(String log) {
		FileWriter fw = null;
		if (name == null || "".equals(name.trim())) {
			name = "log.txt";
		}

		if (path != null && !"".equals(path.trim())) {
			String str = path.substring(0, 1);
			if ("C".equals(str) || "c".equals(str)) {
				path = "C:\\Logs";
			}
		} else {
			path = "C:\\Logs";
		}

		File file = new File(path + "\\" + name);
		if (file.length() > 20000000) {// 20兆 00000
			try {
				dealFile(path, name);//复制文件
				writeFile2(log);//回调重新建立文件写入当次log
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		} else {
			try {
				File fileParent = file.getParentFile();
				if (!fileParent.exists()) {// 判断其父级是否存在
					fileParent.mkdirs();
				}
				// 如果文件存在，则追加内容；如果文件不存在，则创建文件
				fw = new FileWriter(file, true);
				delOldFile(path, name,10);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
			PrintWriter pw = new PrintWriter(fw);
			pw.println(log);
			pw.flush();
			try {
				fw.flush();// 清空缓冲区的数据流
				pw.close();
				fw.close();
			} catch (IOException e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	/** 文件超过大小，保留原文件数据，新建文件 */
	@SuppressWarnings("resource")
	public void dealFile(String path, String name) {
		File file = new File(path + "\\" + name);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			// file.renameTo(new File("D:\\Desktop\\song.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		LocalDateTime d = LocalDateTime.now();
		String date = dtf.format(d);
		String prefixName = name.substring(0, name.lastIndexOf("."));// 去掉后缀的文件名
		String lastName = name.substring(name.lastIndexOf("."));// 文件名后缀 .xxx
		File f = new File(path + "\\" + prefixName + date + lastName);

		FileChannel inputChannel = null;// nio 输入流
		FileChannel outputChannel = null;// nio 输出流
		try {
			inputChannel = new FileInputStream(file).getChannel();
			outputChannel = new FileOutputStream(f).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());// 复制文件
			inputChannel.close();
			outputChannel.close();// 不关闭通道 对文件的删除等操作无法进行
			file.delete();
//			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				inputChannel.close();
				outputChannel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/** 日志文件超过counts个则自动删除一个 */
	@SuppressWarnings("null")
	public static void delOldFile(String path, String name, int nums) throws Exception{
		String prefixName = name.substring(0, name.lastIndexOf("."));
		String lastName = name.substring(name.lastIndexOf("."));
		
		File file = new File(path);
		if (file.isDirectory()) {
			String[] f = file.list();
			String []str = new String[f.length+1];//时间
			int count = 0;
			int strIndex = 0;
			for (String filename : f) {//filename 目录下所有文件名
				if (filename.startsWith(prefixName)) {
					String regEx="[^0-9]";   
					Pattern p = Pattern.compile(regEx);   
					Matcher m = p.matcher(filename); 
					String date = m.replaceAll("").trim();
					str[count] = date;
					count++;
					if (count > nums) {
						strIndex ++;
						File delFile = new File(path  + "\\" + prefixName + str[strIndex] + lastName);
						delFile.delete();
					}
				}
			}
		}
	}
	
	public static void main(String args[]) {
		ReadWriteTxtForLog rw = new ReadWriteTxtForLog("","");
			 rw.writeFile2("kkkkkkkkkkkkkkkk");
		
		}


}
