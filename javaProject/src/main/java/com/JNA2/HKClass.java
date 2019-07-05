package com.JNA2;

import com.sun.jna.NativeLong;


public class HKClass {
	static HKInterface hCNetSDK = HKInterface.INSTANCE; 
	static NativeLong userId;
	
	/**初始化*/
	public static void init() {
		boolean b = hCNetSDK.NET_DVR_Init();// 初始化
		if (b) {
			System.out.println("初始化成功");
		} else {
			int numbeFalse = hCNetSDK.NET_DVR_GetLastError();
			String a = hCNetSDK.NET_DVR_GetErrorMsg(null);
			System.out.println("初始化失败，错误代码：" + numbeFalse + "，错误信息：" + a);
		}
	}
	
	/**
	 * 登录
	 * @return NativeLong id
	 */
	public static NativeLong LoginDevice(String ip,String user,String password, int port) {
		HKInterface.NET_DVR_USER_LOGIN_INFO struLoginInfo = new HKInterface.NET_DVR_USER_LOGIN_INFO();//登录信息结构体
		HKInterface.NET_DVR_DEVICEINFO_V40  struDeviceInfo = new HKInterface.NET_DVR_DEVICEINFO_V40();//设备信息结构体
		for (int i = 0; i < ip.length(); i++) {
			struLoginInfo.sDeviceAddress[i] = (byte) ip.charAt(i);
		}
		
		for (int i = 0; i < user.length(); i++) {
			struLoginInfo.sUserName[i] = (byte) user.charAt(i);
		}
		
		for (int i = 0; i < password.length(); i++) {
			struLoginInfo.sPassword[i] = (byte) password.charAt(i);
		}
		struLoginInfo.wPort = (short)port;
		struLoginInfo.write();
		NativeLong id = hCNetSDK.NET_DVR_Login_V40(struLoginInfo.getPointer(), struDeviceInfo.getPointer());// getPointer()返回对象指针
		if ("-1".equals(String.valueOf(id).trim())) {
			int numbeFalse = hCNetSDK.NET_DVR_GetLastError();
			String a = hCNetSDK.NET_DVR_GetErrorMsg(null);
			System.out.println("登录失败,错误代码：" + numbeFalse + ",错误信息" + a);
		} else {
			System.out.println("登录成功 ");
		}
		userId = id;
	return id;
	}
	
	
	/**
	 * 注册报警回调函数
	 * @return
	 */
	public static boolean setDVRMessageCallBack_V31() {
		HKInterface.MSGCallBackV31 msgCallBackV31 = new HKInterface.MSGCallBackV31();
		boolean bool = hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(msgCallBackV31, null);
		if (bool) {
			System.out.println("报警成功");
		} else {
			int numbeFalse = hCNetSDK.NET_DVR_GetLastError();
			String a = hCNetSDK.NET_DVR_GetErrorMsg(null);
			System.out.println("报警失败,错误代码：" + numbeFalse + "，错误信息：" + a);
		}
//		TeamWayInterface.NET_DVR_ALARMER pAlarmer = new TeamWayInterface.NET_DVR_ALARMER();
//		TeamWayInterface.NET_DVR_ACS_ALARM_INFO pAlarmInfo = new TeamWayInterface.NET_DVR_ACS_ALARM_INFO();
//		pAlarmer.read();
//		pAlarmInfo.read();
//		String a = "";
//		msgCallBackV31.invoke(COMM_ALARM_ACS, pAlarmer, a, pAlarmer.size(), null);
		return bool;
	}
	 
	
	public static void main(String[] args) {
		init();
		LoginDevice("192.168.12.39","admin","TY26811438",8000);
		setDVRMessageCallBack_V31();
	}
}



















