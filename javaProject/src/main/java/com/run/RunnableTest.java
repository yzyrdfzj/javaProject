package com.run;

public class RunnableTest implements Runnable {

	static Runnable r1;
	static Runnable r2;

	@Override
	public void run() {

		cc();
	}

	public static void cc() {
		 
		r1 = new Runnable() {
//		 new Thread(new Runnable() {
			@Override
			public void run() {
		for (int i = 0; i < 100; i++) {
			System.out.println(i);
		}
				
			}
		}/*).start()*/;
		 
		 r2 = new Runnable() {
//		 new Thread(new Runnable() {
			@Override
			public void run() {
				System.out.println("一");
				System.out.println("二");
				System.out.println("三");
				System.out.println("四");
				System.out.println("五");
				System.out.println("六");
				System.out.println("七");
				System.out.println("八");
				System.out.println("九");
				System.out.println("十");
				System.out.println("十一");
				System.out.println("十二");
				System.out.println("十三");
				System.out.println("十四");
				System.out.println("十五");
				System.out.println("十六");
				System.out.println("十七");
				System.out.println("十八");
				System.out.println("十九");
				System.out.println("二十");
				System.out.println("二十一");
				System.out.println("二十二");
				System.out.println("二十三");
				System.out.println("二十四");
				System.out.println("二十五");
				System.out.println("二十六");
				System.out.println("二十七");
				System.out.println("二十八");
				System.out.println("二十九");
				System.out.println("三十");
			}

		}/*.start()*/;
	}

	public static void main(String[] args) {//方法内部 r1 = new Runnable() {} 须先调用方法，再启动线程
		RunnableTest r = new RunnableTest();
		 cc();
		new Thread(r1).start();
		new Thread(r2).start();
		
	}

}
