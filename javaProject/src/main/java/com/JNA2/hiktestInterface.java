package com.JNA2;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;


public interface hiktestInterface extends Library{
	public int NET_DVR_DEV_ADDRESS_MAX_LEN = 129;
	public int NET_DVR_LOGIN_USERNAME_MAX_LEN = 64;
	public int NET_DVR_LOGIN_PASSWD_MAX_LEN = 64;
	public int SERIALNO_LEN = 48;
	public int MAX_DOOR_NUM = 32;
	public int MAX_CASE_SENSOR_NUM = 8;
	public int MAX_CARD_READER_NUM = 64;
	public int MAX_ALARMHOST_ALARMOUT_NUM = 512;
	public int MAX_ALARMHOST_ALARMIN_NUM = 512;
	hiktestInterface INSTANCE = (hiktestInterface) Native.loadLibrary("F:\\CH-HCNetSDKV6.0.2.35_build20190411_Win64\\CH-HCNetSDKV6.0.2.35_build20190411_Win64\\库文件\\HCNetSDK.dll", hiktestInterface.class);
	
	boolean NET_DVR_Init();
	NativeLong NET_DVR_Login_V40(Pointer pLoginInfo,Pointer lpDeviceInfo);
	boolean NET_DVR_GetDVRConfig(NativeLong lUserID,int dwCommand,NativeLong lChannel,Pointer lpOutBuffer,int dwOutBufferSize,IntByReference lpBytesReturned);
	int NET_DVR_GetLastError();
	
	public class NET_DVR_DEVICEINFO_V30 extends Structure{
		public byte     sSerialNumber[] = new byte[SERIALNO_LEN];
		public byte     byAlarmInPortNum;
		public byte     byAlarmOutPortNum;
		public byte     byDiskNum;
		public byte     byDVRType;
		public byte     byChanNum;
		public byte     byStartChan;
		public byte     byAudioChanNum;
		public byte     byIPChanNum;
		public byte     byZeroChanNum;
		public byte     byMainProto;
		public byte     bySubProto;
		public byte     bySupport;
		public byte     bySupport1;
		public byte     bySupport2;
		public short      wDevType;
		public byte     bySupport3;
		public byte     byMultiStreamProto;
		public byte     byStartDChan;
		public byte     byStartDTalkChan;
		public byte     byHighDChanNum;
		public byte     bySupport4;
		public byte     byLanguageType;
		public byte     byVoiceInChanNum;
		public byte     byStartVoiceInChanNo;
		public byte     byRes3[] = new byte[2];
		public byte     byMirrorChanNum;
		public short      wStartMirrorChanNo;
		public byte     byRes2[] = new byte[2];

	}
	
	public static interface fLoginResultCallBack extends Callback{
    	public void invoke(NativeLong lUserID,int dwResult,Pointer lpDeviceInfo,Pointer pUser);
    	
    }	
	
	
	public class NET_DVR_USER_LOGIN_INFO extends Structure{
		public byte                    sDeviceAddress[] = new byte[NET_DVR_DEV_ADDRESS_MAX_LEN];
		public byte                    byUseTransport;
		public short                   wPort;
		public byte                    sUserName[] = new byte[NET_DVR_LOGIN_USERNAME_MAX_LEN];
		public byte                    sPassword[] = new byte[NET_DVR_LOGIN_PASSWD_MAX_LEN];
		public fLoginResultCallBack    cbLoginResult;
		public Pointer                 pUser;
		public boolean                 bUseAsynLogin;
		public byte                    byProxyType;
		public byte                    byUseUTCTime;
		public byte                    byLoginMode;
		public byte                    byHttps;
		public NativeLong              iProxyID;
		public byte                    byRes3[] = new byte[120];

	}
	
	public class NET_DVR_DEVICEINFO_V40 extends Structure{
		public NET_DVR_DEVICEINFO_V30    struDeviceV30;
		public byte                      bySupportLock;
		public byte                      byRetryLoginTime;
		public byte                      byPasswordLevel;
		public byte                      byProxyType;
		public int                       dwSurplusLockTime;
		public byte                      byCharEncodeType;
		public byte                      bySupportDev5;
		public byte                      byLoginMode;
		public byte                      byRes2[] = new byte[253];
	}
	
	public class NET_DVR_ACS_WORK_STATUS extends Structure{
		public int    dwSize;
		public byte     byDoorLockStatus[] = new byte[MAX_DOOR_NUM];
		public byte     byDoorStatus[] = new byte[MAX_DOOR_NUM];
		public byte     byMagneticStatus[] = new byte[MAX_DOOR_NUM];
		public byte     byCaseStatus[] = new byte[MAX_CASE_SENSOR_NUM];
		public short    wBatteryVoltage;
		public byte     byBatteryLowVoltage;
		public byte     byPowerSupplyStatus;
		public byte     byMultiDoorInterlockStatus;
		public byte     byAntiSneakStatus;
		public byte     byHostAntiDismantleStatus;
		public byte     byIndicatorLightStatus;
		public byte     byCardReaderOnlineStatus[] = new byte[MAX_CARD_READER_NUM];
		public byte     byCardReaderAntiDismantleStatus[] = new byte[MAX_CARD_READER_NUM];
		public byte     byCardReaderVerifyMode[] = new byte[MAX_CARD_READER_NUM];
		public byte     bySetupAlarmStatus[] = new byte[MAX_ALARMHOST_ALARMIN_NUM];
		public byte     byAlarmInStatus[] = new byte[MAX_ALARMHOST_ALARMIN_NUM];
		public byte     byAlarmOutStatus[] = new byte[MAX_ALARMHOST_ALARMOUT_NUM];
		public int      dwCardNum;
		public byte     byRes2[] = new byte[32];

	}
	
}

