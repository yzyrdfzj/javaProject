package com.JNA;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Callback;
import com.JNA.DllCallBackTest.JnaCallBackDll.SCBack_Impl;
public class DllCallBackTest {

	public interface JnaCallBackDll extends Library {
		 
		public static JnaCallBackDll instance = (JnaCallBackDll) Native.loadLibrary("src/main/resources/Dll2.dll",JnaCallBackDll.class);
 
		int dllFunction(String host, int port, Callback callback);
 
		public interface SCBack extends Callback {
			public void MessageHandle(String name, int length);
		}
 
		public static class SCBack_Impl implements SCBack {
			public void MessageHandle(String name, int length) {
				// TODO Auto-generated method stub
				System.out.println("回调成功！");
				// 此处添加需要的数据处理操作
			}
		}
	}
 
	public static void main(String[] args) {
 
		DllCallBackTest.JnaCallBackDll.SCBack callback = new SCBack_Impl();
 
		JnaCallBackDll.instance.dllFunction("127.0.0.1", 1234, callback);
	}
	
	
}
