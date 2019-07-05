package com.javaProject;

public class RunnableTest2 implements Runnable {
	
	int count = 1000;
	public void run() {
		long start = System.currentTimeMillis();
		while (count > 0) {
			System.out.println("$$$$ " + Thread.currentThread().getName() + " : " + count--);
		}
		long end = System.currentTimeMillis();
		System.out.println("$$$$ " + Thread.currentThread().getName() + " : run over." + (start-end));
	}
	
//	public void printLn() {
//		ra = new Runnable() {
//			public void run() {
//				while (count > 0) {
//					System.out.println("$$$$ " + Thread.currentThread().getName() + " : " + count--);
//				}
//			}
//		};
//		
//	}

	
	


}
