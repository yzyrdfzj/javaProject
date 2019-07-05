package com.udpsend;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

//udp传输：
/**步骤：---
 * 1、建立udp socket 接收和发送点
 * 2、提供数据，打包
 * 3、使用send发送
 * 4、关闭资源
 * */
public class UDPsend {
	//UDP提供的是无连接的、不可靠的数据传送方式，是一种尽力而为的数据交付服务
	 public static void main(String[] args) throws SocketException, UnknownHostException {
	        /** 1、建立udp socket端点 */
	        
	        DatagramSocket send = new DatagramSocket();
	        
	        /** 2、提供数据，封装打包  ---DatagramPacket(byte[] buf, int length, InetAddress address, int port)  */
	        
	        byte[] bs = "正在使用UDP发送--我是数据! ".getBytes(); 
	        DatagramPacket dp = new DatagramPacket(bs, bs.length, InetAddress.getByName("192.168.0.92"), 3333);

	        while(true) {
	        	 /** 3、使用send发送 */
		        try {
		        	send.send(dp);
		        	System.out.println("发送数据 "+System.currentTimeMillis());
		        } catch (IOException e) {
		            System.out.println("发送失败： ");
		            e.printStackTrace();
		        }
	        	
		        try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
	       
	        
	        /** 4、关闭资源 */
//	        send.close();
	        
	    }

}
