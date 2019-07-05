package com;

import java.util.Random;

public class Randoms {
	public static void main(String[] args) {
		Random ran = new Random();
		int a = ran.nextInt();
//		System.out.println(a);
		
		int aa = 100; // 1100100
		int bb = aa >>> 5;  //转换为二进制数后，右移 x 位，左加右减
//		System.out.println(bb);
		
		
		String str = "asdfghjknbterg";
		char ab = str.charAt(5);//取指定下标的字符
		System.out.println(ab);
		
		
	}
	
	public static String aa() {
		String js = "jintaintainqibucuo";
		return js;
	}

}
