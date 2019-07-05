package com.JNA2;

import com.sun.jna.NativeLong;
import com.sun.jna.ptr.IntByReference;

import com.JNA2.hiktestInterface.NET_DVR_ACS_WORK_STATUS;
import com.JNA2.hiktestInterface.NET_DVR_DEVICEINFO_V40;
import com.JNA2.hiktestInterface.NET_DVR_USER_LOGIN_INFO;

public class hiktest {
	public static void main(String[] args) {
		boolean ret = hiktestInterface.INSTANCE.NET_DVR_Init();
		if(ret==true) {
			NET_DVR_USER_LOGIN_INFO loginInfo = new NET_DVR_USER_LOGIN_INFO();
			NET_DVR_DEVICEINFO_V40 deviceInfo = new NET_DVR_DEVICEINFO_V40();
			String ip = "192.168.12.39";
			String user = "admin";
			String password = "TY26811438";
//			loginInfo.sDeviceAddress = ip.getBytes();
//			loginInfo.sUserName = user.getBytes();
//			loginInfo.sPassword = password.getBytes();
			
			for (int i = 0; i < ip.length(); i++) {
				loginInfo.sDeviceAddress[i] = (byte) ip.charAt(i);
			}
			
			for (int i = 0; i < user.length(); i++) {
				loginInfo.sUserName[i] = (byte) user.charAt(i);
			}
			
			for (int i = 0; i < password.length(); i++) {
				loginInfo.sPassword[i] = (byte) password.charAt(i);
			}
			
			loginInfo.wPort = 8000;
			loginInfo.write();
			
			NativeLong userId = hiktestInterface.INSTANCE.NET_DVR_Login_V40(loginInfo.getPointer(), deviceInfo.getPointer());
			if(userId.longValue()>=0) {
				NET_DVR_ACS_WORK_STATUS status = new NET_DVR_ACS_WORK_STATUS();
				IntByReference len = new IntByReference();
				NativeLong channel = new NativeLong(-1);
				boolean result = hiktestInterface.INSTANCE.NET_DVR_GetDVRConfig(userId, 2123, channel, status.getPointer(), status.size(), len);
				status.read();
				if(result==true) {
					System.out.println("status:" + status.byDoorStatus[0]);
				}
				else
				{
					System.out.println("error:" + hiktestInterface.INSTANCE.NET_DVR_GetLastError());
				}
			}
			else {
				System.out.println("login error:"+hiktestInterface.INSTANCE.NET_DVR_GetLastError());
			}
		}
	}
}
