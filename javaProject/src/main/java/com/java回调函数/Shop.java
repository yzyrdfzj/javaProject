package com.java回调函数;

public class Shop {
	/*
	 * 回调函数
	 */
	public void call(CallBack a){
		/*
		 * b help a solve the priblem
		 */
		System.out.println("商店帮助我解决问题");
		/*
		 * call back
		 */
		a.slove();
		
	}
}
