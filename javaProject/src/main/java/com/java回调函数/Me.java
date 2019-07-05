package com.java回调函数;

public class Me implements CallBack{
	Shop b = new Shop();
	
	/*
	 * (non-Javadoc)
	 * @see CallBack#slove()
	 * 响应回调函数
	 */
	public void slove() {
		System.out.println("问题解决了");
	}
	
	
	/*
	 * 登记回调函数
	 */
	public void askQuestion(){
		System.out.println("我现在有一个问题");
		/*
		 * 自己去做其它事
		 */
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				System.out.println("我去做别的事情");
			}
		}).start();
		/*
		 * 向商店寻求解决问题
		 * 调用Shop的方法
		 */
		this.b.call(this);
	}
	/*
	 * test
	 */
	 public static void main(String[] args)  {
		Me a = new Me();
		a.askQuestion();
		
		/**
		 大致流程： 测试时，我的方法中调用Shop的方法，Shop调用CallBack的方法，我实现了 CallBack的方法即响应CallBack
		 * */
	}
}
