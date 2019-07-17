package com.run;

public class RunnableTest implements Runnable {
	
	static Runnable r1;
	static Runnable r2;

	@Override
	public void run() {
		
		cc();
	}
	
	public static void cc() {
		 
//		r1 = new Runnable() {
		 new Thread(new Runnable() {
			@Override
			public void run() {
		for (int i = 0; i < 100; i++) {
			System.out.println(i);
			try {
				new Thread().sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
				
			}
		}).start();
		 
//		 r2 = new Runnable() {
		 new Thread(new Runnable() {
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
			}

		}).start();
	}
	
	public static void main(String[] args) {
		RunnableTest r = new RunnableTest();
		cc();
//		new Thread(r2).start();
//		new Thread(r1).start();
	}

	

}

