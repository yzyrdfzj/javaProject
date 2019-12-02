package com.websocket;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Test {

	@Autowired
	private static WebSocketServer webSocketServer;
	
	public void sendMessage() throws IOException{
		//webscoket 在线测试工具网站http://ws.douqq.com/
		webSocketServer.sendTextMessage("我点你妈的");
		
	}
}
