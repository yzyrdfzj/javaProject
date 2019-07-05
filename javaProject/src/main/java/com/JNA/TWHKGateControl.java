package com.JNA;

import com.sun.jna.NativeLong;

public class TWHKGateControl {
	
	private String loginName;
	private String pwd;
	private String ip;
	private short port;
	
	
	public TWHKGateControl(String loginName, String pwd, String ip, short port) {
		this.loginName = loginName;
		this.pwd = pwd;
		this.ip = ip;
		this.port = port;
		
	}
	
	
	public String getLoginName() {
		return loginName;
	}
	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}
	public String getPwd() {
		return pwd;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public short getPort() {
		return port;
	}
	public void setPort(short port) {
		this.port = port;
	}


	
	
	/**加载啊库文件*/
	static TWHKGateControlInterface twgc = TWHKGateControlInterface.INSTANCE; ///加载库文件
	
	
	/**
	 * 初始化
	 * @return
	 */
	public static void init () {
		boolean b = twgc.NET_DVR_Init();//初始化
		twgc.NET_DVR_SetLogToFile(true, null, false);//启用日志文件接口
		if(b) {
			System.out.println("初始化成功");
		} else {
			System.out.println("初始化失败");
		}
		
	}
	
	
	private static NativeLong LoginDevice(Device deviceInfo){
		
		return null;
	}

}
