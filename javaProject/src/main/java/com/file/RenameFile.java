package com.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RenameFile {
	static DateTimeFormatter dtf = DateTimeFormatter.ofPattern("YMMddHHmmss");
	
	
	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		delOldFile(null, null,10);
//		dealFile();
//		creatFile();
	}

	/**文件超过大小新写一个文件*/
	@SuppressWarnings("resource")
	public static void dealFile(){
//		String path = "D:\\Desktop\\";
//		String name = "yans.txt";
		File file = new File("D:\\Desktop\\yans.txt");
		try {
			if (!file.exists()) {
				file.createNewFile(); 
			}
			// file.renameTo(new File("D:\\Desktop\\song.txt"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		LocalDateTime date = LocalDateTime.now();
		String d = date.format(dtf);

		File f = new File("D:\\Desktop\\song" + d + ".txt");
		FileChannel inputChannel = null;// nio 输入流
		FileChannel outputChannel = null;// nio 输出流
		try {
			inputChannel = new FileInputStream(file).getChannel();
			outputChannel = new FileOutputStream(f).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());// 复制文件
			inputChannel.close();
			outputChannel.close();// 通道不关闭，文件进行删除等操作
			file.delete();
			file.createNewFile();
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

	/**文件数量超过counts个删除最早的一个*/
	@SuppressWarnings("null")
	public static void delOldFile(String path, String name, int nums) {
		path = "D:\\Desktop\\";
		name = "yans.txt";
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
						boolean b = delFile.delete();
						System.out.println(b);
					}
				}
			}
		}
	}

	public static void creatFile() {
		for (int i = 10; i < 25; i++) {
			File file = new File("D:\\Desktop\\" + "yans201906" + i + "183025.txt");
				try {
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}

	}

}
