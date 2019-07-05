package com.JNA;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface TWHKGateControlInterface extends Library{
	
	static TWHKGateControlInterface INSTANCE = 
			(TWHKGateControlInterface) Native.loadLibrary("hcnetsdk",TWHKGateControlInterface.class);//加载库文件

	/**初始化*/
	boolean NET_DVR_Init();
	boolean NET_DVR_SetLogToFile(boolean bLogEnable , String  strLogDir, boolean bAutoDel );

}
