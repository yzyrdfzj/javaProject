package com;

import java.util.Random;

public class Randomss {
	public static void main(String[] args) {
		Random ran = new Random();
		int min = 30;
		int max = 50;
		for (int i = 0; i < 500000; i++) {
			int num = ran.nextInt(99999)+0;//随机出来的数最大为括号中的+外面的，最小为外面的
			System.out.println(num);
		}
		
	}
}
