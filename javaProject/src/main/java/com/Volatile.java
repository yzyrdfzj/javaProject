package com;

public class Volatile {
	//volatile:可见性 当当线程1对t进行了加1操作并把数据写回到主存之后，线程2就会知道它自己工作空间内的t已经被修改了，当它要执行加1操作之后，就会去主存中读取。这样，两边的数据就能一致了
	//有序性：虚拟机会保证这个变量之前的代码一定会比它先执行，而之后的代码一定会比它慢执行
	
// 什么时候volatile可以保证线程安全
//	1. 对变量的写入操作不依赖变量的当前值，或者你能确保只有单个线程更新变量的值。
//	2. 该变量没有包含在具有其他变量的不变式中。
	 public static volatile int t = 0;

	    public static void main(String[] args){

	        Thread[] threads = new Thread[10];
	        for(int i = 0; i < 10; i++){
	            //每个线程对t进行1000次加1的操作
	        	
	        	threads[i]  = new Thread(new Runnable(){
	                @Override
	                public void run(){
	                    for(int j = 0; j < 1000; j++){
	                        t = t + 1;
	                    }
	                }
	            });
	            threads[i].start();
	        }

	        //等待所有累加线程都结束
	        while(Thread.activeCount() > 1){
	            Thread.yield();
	        }

	        //打印t的值
	        System.out.println(t);//并不会等于10000
	    }

}
