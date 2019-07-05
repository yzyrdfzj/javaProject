package com.udpsend;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

//udp传输： 接收
/**步骤：---
* 1、建立udp socket，设置接收端口
* 2、预先创建数据存放的位置，封装
* 3、使用receive阻塞式接收
* 4、关闭资源
* */
public class UDPrecv {

    public static void main(String[] args) throws Exception{
        /**1、建立udp socket，设置接收端口*/
        
        DatagramSocket recv = new DatagramSocket(3333);

        /**2、预先创建数据存放的位置，封装*/
        byte [] bbuf = new byte [1024];
         DatagramPacket dp = new DatagramPacket(bbuf,bbuf.length);
        
         System.out.println("接受端口监听");
         while(true){
        	 /**3、使用receive阻塞式接收*/
             recv.receive(dp);
             System.out.println("ip::"+dp.getAddress().getHostAddress()+"\nport::"+dp.getPort()+"\ndata::"+new String(dp.getData()));
             try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
             
         }
        
         /**4、关闭资源*/
        // recv.close();
    }
}
