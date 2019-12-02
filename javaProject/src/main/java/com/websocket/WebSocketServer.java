package com.websocket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.CopyOnWriteArraySet;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;



@ServerEndpoint("/websocket/{sid}")
@Component
public class WebSocketServer {

	private static Logger log = LoggerFactory.getLogger(WebSocketServer.class);
	private static int onlineCount = 0;
	private static CopyOnWriteArraySet<WebSocketServer> webSocketSet = new CopyOnWriteArraySet<WebSocketServer>();

	private Session session;
	private String sid="";
	/**
	 * 连接建立成功调用的方法*/
	@OnOpen
	public void onOpen(Session session,@PathParam("sid") String sid) {
		this.session = session;
		webSocketSet.add(this);     //加入set中
		addOnlineCount();           //在线数加1
		log.info("有新窗口开始监听:"+sid+",当前在线人数为" + getOnlineCount());
		this.sid=sid;
		try {
			sendTextMessage("连接成功");
		} catch (IOException e) {
			log.error("websocket IO异常");
		}
	}

	/**
	 * 连接关闭调用的方法
	 */
	@OnClose
	public void onClose() {
		webSocketSet.remove(this);  //从set中删除
		subOnlineCount();           //在线数减1
		log.info("有一连接关闭！当前在线人数为" + getOnlineCount());
	}

	/**
	 * 收到客户端消息后调用的方法
	 *
	 * @param message 客户端发送过来的消息*/
	@OnMessage
	public void onMessage(String message, Session session) {
		log.info("收到来自窗口"+sid+"的信息:"+message);
		//群发消息
		for (WebSocketServer item : webSocketSet) {
			try {
				item.sendTextMessage(message);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param session
	 * @param error
	 */
	@OnError
	public void onError(Session session, Throwable error) {
		log.error("发生错误");
		error.printStackTrace();
	}
	/**
	 * 实现服务器主动推送
	 */
	public void sendTextMessage(String message) throws IOException {
		this.session.getBasicRemote().sendText(message);
	}
	public void sendBinaryMessage(byte[] message) throws IOException {
		this.session.getBasicRemote().sendBinary(ByteBuffer.wrap(message));
	}

	public static void sendBinaryMsg(byte[] message){
		for (WebSocketServer item : webSocketSet) {
			synchronized (item) 
			{
				try {
					item.sendBinaryMessage(message);
				} catch (IOException e) {
					continue;
				}
			}
		}
	}

	
	public static void sendBinaryMsg(byte[] message,@PathParam("sid") String sid){
		for (WebSocketServer item : webSocketSet) {
			synchronized (item) 
			{
				try {
					if(sid==null) {
						item.sendBinaryMessage(message);
					}else if(item.sid.equals(sid)){
						item.sendBinaryMessage(message);
					}
				} catch (IOException e) {
					continue;
				}
			}
		}
	}

	/**
	 * 群发自定义消息
	 * */
	public static void sendTextMsg(String message,@PathParam("sid") String sid) throws IOException {
		for (WebSocketServer item : webSocketSet) {
			try {
				if(sid==null) {
					item.sendTextMessage(message);
				}else if(item.sid.equals(sid)){
					item.sendTextMessage(message);
				}
			} catch (IOException e) {
				continue;
			}
		}
	}

	public static synchronized int getOnlineCount() {
		return onlineCount;
	}

	public static synchronized void addOnlineCount() {
		WebSocketServer.onlineCount++;
	}

	public static synchronized void subOnlineCount() {
		WebSocketServer.onlineCount--;
	}
	
}