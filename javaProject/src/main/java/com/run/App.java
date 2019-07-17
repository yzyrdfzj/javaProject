package com.run;


/**
 * Hello world!
 *
 */
public class App 
{
	public static void main( String[] args ){

		RunnableTest ra = new RunnableTest();
		RunnableTest2 ra2 = new RunnableTest2();
//		ra.run();
//		ra2.run();
		
//		ra.cc();
		new Thread(ra2).start();
		new Thread(ra).start();
//		new Thread(ra.r1).start(); 
//		new Thread(ra.r2).start();
    }
}
