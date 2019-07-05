package com.javaProject;

public class RunnableTest implements Runnable {
	static Runnable ra;
	
	int count = 1000;
	String a = "5";
//	public void printLn(String a) {
//		long start = System.currentTimeMillis();
//		while (count > 0) {
//			System.out.println("#### " + Thread.currentThread().getName() + " : " + count--);
//		}
//		long end = System.currentTimeMillis();
//		System.out.println("#### " + Thread.currentThread().getName() + " : run over." + (start-end));
//		a = this.a;
//	}
	
	public void printLn() {
		ra = new Runnable() {
			public void run() {
				while (count > 0) {
					System.out.println("#### " + Thread.currentThread().getName() + " : " + count--);
				}
				
			}
		};
	}
	
	public void printLn2() {
		ra = new Runnable() {
			public void run() {
				while (count > 0) {
					System.out.println("****" + Thread.currentThread().getName() + " : " + count--);
				}
				
			}
		};
	}

	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
	
//	public static void main(String[] args) {
//		RunnableTest tir = new RunnableTest();
//        	new Thread(tir).start();
//        	new Thread(ra).start();
//        	
//	}
	

}
