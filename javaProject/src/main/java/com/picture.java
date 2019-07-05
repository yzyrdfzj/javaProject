package com;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class picture {
	
	public static void main(String[] args) {
        try {
        	//读取图片转换为字节
            File file = new File("D:\\Desktop\\picture\\微信图片_20190529163413.jpg");
            //readAllBytes适用于将所有字节读取到字节数组中比较方便的简单情况。不适合在大文件中读取
            byte[] fileByte = Files.readAllBytes(file.toPath());
            
            //读取字节输出到xx
            FileOutputStream fout = new FileOutputStream("D:\\Desktop\\123.jpg");
            fout.write(fileByte);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
   
        pictureToByte();
	 }
	
	public static byte[] pictureToByte() {
		byte[] bytes = null;
	     try {
	    	 FileInputStream fin = new FileInputStream(new File("D:\\Desktop\\picture\\370103.jpg"));
		     //可能溢出,简单起见就不考虑太多,如果太大就要另外想办法，比如一次传入固定长度byte[]
	    	 bytes = new byte[fin.available()];//fin.available() 字节长度
	    	 fin.read(bytes);
		     //将文件内容写入字节数组，提供测试的case
			 fin.read(bytes);
			 
			 //输出
	         FileOutputStream fout = new FileOutputStream("D:\\Desktop\\000.jpg");
	         //写入
	         fout.write(bytes);
	         
	         //写入
//	         int ch=fin.read();
//	            while(ch!=-1){
//	            	fout.write(ch);
//	                ch=fin.read();
//	            }
	         
			 fin.close();
			 fout.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	     return bytes;
	}
}
