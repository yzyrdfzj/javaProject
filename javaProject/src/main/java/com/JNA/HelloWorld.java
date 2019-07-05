package com.JNA;

import com.sun.jna.Callback;
import com.sun.jna.Library;

import com.sun.jna.Native;

import com.sun.jna.Platform;

public class HelloWorld {
	
	 public interface CLibrary extends Library {
		 	/**
		 	 * Native.loadLibrary()加载C++库文件
		 	 * 第 一个参数是动态链接库dll/so的名称，但不带.dll或.so这样的后缀，这符合JNI的规范，因为带了后缀名就不可以跨操作系统平台了
		 	 * 第二个参数是本接口的Class类型。JNA通过这个Class类型，根据指定的.dll/.so文件，动态创建接口的实例。该实例由JNA通过反射自动生成。
		 	 * printf函数在Windows平台下所在的dll库名称是msvcrt，Linux下的so库名称是c。
		 	 */
		 public static CLibrary INSTANCE = (CLibrary)Native.loadLibrary((Platform.isWindows() ? "msvcrt" : "c"),CLibrary.class);

	        void printf(String format, Object... args);
	        
	       
	        int printf(String name, int port, Callback callback);
	        
	        public interface SCBack extends Callback {
				public void MessageHandle(String name, int length);
			}
	 
			public static class SCBack_Impl implements SCBack {
				public void MessageHandle(String name, int length) {
					//添加操作
					System.out.println("回调成功");
					
				}
			}
	        

	    }

	    public static void main(String[] args) {

	        CLibrary.INSTANCE.printf("Hello, World/n");

	        for (int i=0;i < args.length;i++) {
	            CLibrary.INSTANCE.printf("Argument %d: %s/n", i, args[i]);
	        }
	        
	        HelloWorld.CLibrary.SCBack caca = new HelloWorld.CLibrary.SCBack_Impl();
	        
	        CLibrary.INSTANCE.printf("aa", 12306, caca);
	        
	    }

}
