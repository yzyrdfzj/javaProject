package com.DB;

public class Test {
	public static void main(String[] args) {
        for (int i = 1; i <=10; i++) {//10*100一千万
              new MyThread().start();//用线程时间至少快一倍
            }
    }
}
