package com.JNA2;

import com.sun.jna.Callback;
import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.NativeLongByReference;


public interface HKInterface extends Library{

	HKInterface INSTANCE = (HKInterface) Native.loadLibrary("F:\\CH-HCNetSDKV6.0.2.35_build20190411_Win64\\CH-HCNetSDKV6.0.2.35_build20190411_Win64\\库文件\\HCNetSDK.dll", HKInterface.class);//加载库文件
	
	public static final int NET_DVR_DEV_ADDRESS_MAX_LEN = 129;
	public static final int NET_DVR_LOGIN_USERNAME_MAX_LEN = 64;
	public static final int NET_DVR_LOGIN_PASSWD_MAX_LEN = 64;
	public static final int MAX_CARD_READER_NUM = 64;
	public static final int SERIALNO_LEN = 48;   //序列号长度
	public static final int MAX_CASE_SENSOR_NUM = 8;
	public static final int MAX_ALARMHOST_ALARMIN_NUM = 512;
	public static final int MAX_ALARMHOST_ALARMOUT_NUM = 512;
	public static final int ACS_CARD_NO_LEN = 32;
	public static final int CARD_PASSWORD_LEN = 8;
	public static final int MAX_CARD_RIGHT_PLAN_NUM = 4;
	public static final int MAX_GROUP_NUM = 128;
	public static final int NAME_LEN = 128;
	public static final int MAX_LOCK_CODE_LEN = 8;
	public static final int MAX_DOOR_CODE_LEN = 8;
	public static final int MACADDR_LEN = 16;
	public static final int MAX_NAMELEN = 16;
	public static final int MAX_CARD_READER_NUM_512 = 512;
	public static final int MAX_DOOR_NUM_256 = 256;
	/** 最大门数量 */
	public static final int MAX_DOOR_NUM = 32;
	/**dwCommand 门禁主机报警信息*/
	public static final int COMM_ALARM_ACS = 0x5002;
	
		/**sdk初始化*/
		 boolean NET_DVR_Init();
		/**获取错误值*/
		 int NET_DVR_GetLastError();
		 /**获取错误信息*/
		 String NET_DVR_GetErrorMsg(NativeLongByReference pErrorNo );
		 /**登录*/
		 NativeLong NET_DVR_Login_V40(Pointer pLoginInfo,Pointer lpDeviceInfo);
		 
		 /**
		  * @param fMessageCallBack 注册报警回调函数
		  * @param pUser 用户数据
		  */
		 boolean NET_DVR_SetDVRMessageCallBack_V31(MSGCallBack_V31 fMessageCallBack, Pointer pUser);
		 
		 
		 
		    /**
		     * 注册报警回调函数
		     * @author dell
		     */
	public static interface MSGCallBack_V31 extends Callback{
	    public boolean invoke(NativeLong lCommand, NET_DVR_ALARMER pAlarmer, String pAlarmInfo, int dwBufLen, Pointer pUser);
				 
	}
		    
		    
		    /**
		     * 注册报警回调函数的实现 
		     * @author dell
		     */
	public static class MSGCallBackV31 implements MSGCallBack_V31 {

		private String cardID = "";
		public boolean invoke(NativeLong lCommand, NET_DVR_ALARMER pAlarmer, String pAlarmInfo, int dwBufLen, Pointer pUser) {
			System.out.println("进入回调函数");
				return true;
		}
	}
		    
		    

		 
		 /**登录注册回调函数
		   * @author dell
		   */
		 public static interface FLoginResultCallBack extends Callback{
		    	public int invoke(NativeLong lUserID,int dwResult,Pointer lpDeviceinfo,Pointer pUser);
		    }
/**-----------------------------------------------------------------------------------------------------------------------*/
		 
		 /**NET_DVR_ALARMER:报警设备信息*/
		    public class NET_DVR_ALARMER extends Structure{
		    	public byte byUserIDValid;
		    	public byte bySerialValid;
		    	public byte byVersionValid;
		    	public byte byDeviceNameValid;
		    	public byte byMacAddrValid;
		    	public byte byLinkPortValid;
		    	public byte byDeviceIPValid;
		    	public byte bySocketIPValid;
		    	public NativeLong lUserID;
		    	public byte sSerialNumber[] = new byte[SERIALNO_LEN];
		    	public int dwDeviceVersion;
		    	public byte sDeviceName[] = new byte[NAME_LEN];
		    	public byte byMacAddr[] = new byte[MACADDR_LEN];
			    public short wLinkPort;
			    public byte sDeviceIP[] = new byte[128];
			    public byte sSocketIP[] = new byte[128];
			    public byte byIpProtocol;
			    public byte byRes2[] = new byte[11];
		    }
		 
		 
		 public class NET_DVR_USER_LOGIN_INFO extends Structure{
				public byte                    sDeviceAddress[] = new byte[NET_DVR_DEV_ADDRESS_MAX_LEN];
				public byte                    byUseTransport;
				public short                   wPort;
				public byte                    sUserName[] = new byte[NET_DVR_LOGIN_USERNAME_MAX_LEN];
				public byte                    sPassword[] = new byte[NET_DVR_LOGIN_PASSWD_MAX_LEN];
				public FLoginResultCallBack    cbLoginResult;
				public Pointer                 pUser;
				public boolean                 bUseAsynLogin;
				public byte                    byProxyType;
				public byte                    byUseUTCTime;
				public byte                    byLoginMode;
				public byte                    byHttps;
				public NativeLong              iProxyID;
				public byte                    byRes3[] = new byte[120];

			}
		 
		 public static class NET_DVR_DEVICEINFO_V40 extends Structure {
//		 		public NET_DVR_DEVICEINFO_V30    struDeviceV30;//设备参数
				public byte                      bySupportLock;///设备是否支持锁定功能，bySupportLock 为 1 时，dwSurplusLockTime 和 byRetryLoginTime
				public byte                      byRetryLoginTime;///剩余可尝试登陆的次数，用户名、密码错误时，此参数有效
				public byte                      byPasswordLevel;///密码安全等级：0- 无效，1- 默认密码，2- 有效密码，3- 风险较高的密码
				public byte                      byProxyType;
				public int                       dwSurplusLockTime;///剩余锁定时间
				public byte                      byCharEncodeType;
				public byte                      bySupportDev5;
				public byte                      byLoginMode;
				public byte                      byRes2[] = new byte[253];
		 }
}














