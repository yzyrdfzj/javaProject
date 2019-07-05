package com;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class aaaa {
	public static void main(String[] args) {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sim = new SimpleDateFormat("yyyyMMdd");
		c.setTime(new Date());
		c.add(Calendar.DATE, -(30));
		Date beforeDayTime = c.getTime();
		String beforeDay = sim.format(beforeDayTime);
		System.out.println(beforeDay);
		
//		int j = 10;
//		String paths = "/record/disk" + j + "/TYNVRVideo/" + beforeDay;
//		System.out.println(paths);
		
		File files = new File("H:/record/disk01/TYNVRVideo/");
		File[] file = files.listFiles();
		System.out.println(file.length);
		int[] pathLast = new int[file.length];
		for (int i = 0; i < file.length; i++) {
            //获取子文件路径  需要获取最后面八位比较大小选出最小的
			pathLast[i] = Integer.parseInt(file[i].getAbsolutePath().substring(28));
            System.out.println(pathLast[i]);
		}
		int minPathLast = 999999999;
		//获取日期最小的文件的日期
			for (int k = 0; k < pathLast.length; k++) {
				if(minPathLast>pathLast[k]){
					minPathLast = pathLast[k];
				}
		}
		System.out.println(minPathLast);
		
	}

	
	
}
