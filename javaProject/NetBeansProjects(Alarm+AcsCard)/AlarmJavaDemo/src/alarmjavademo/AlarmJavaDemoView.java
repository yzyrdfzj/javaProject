/*
 * AlarmJavaDemoView.java
 */

package alarmjavademo;

import alarmjavademo.HCNetSDK.TimePointParam;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import java.text.DateFormat;
import java.util.Date;
import java.text.SimpleDateFormat;
import javax.swing.table.DefaultTableModel;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * The application's main frame.
 */
public class AlarmJavaDemoView extends FrameView {

    static HCNetSDK hCNetSDK = HCNetSDK.INSTANCE;
    HCNetSDK.NET_DVR_DEVICEINFO_V30 m_strDeviceInfo;//设备信息
    String m_sDeviceIP;//已登录设备的IP地址

    NativeLong lUserID;//用户句柄
    NativeLong lAlarmHandle;//报警布防句柄
    NativeLong lListenHandle;//报警监听句柄

    FMSGCallBack fMSFCallBack;//报警回调函数实现
    FMSGCallBack_V31 fMSFCallBack_V31;//报警回调函数实现

    FGPSDataCallback fGpsCallBack;//GPS信息查询回调函数实现
    FRemoteCfgCallBackCardGet fRemoteCfgCallBackCardGet;
    FRemoteCfgCallBackCardSet fRemoteCfgCallBackCardSet;
    FRemoteCfgCallBackFaceGet fRemoteCfgCallBackFaceGet;
    FRemoteCfgCallBackFaceSet fRemoteCfgCallBackFaceSet;
    FRemoteCfgCallBackFaceCapture fRemoteCfgCallBackFaceCapture;
    FRemoteCfgCallBackFingerPrint fRemoteCfgCallBackFingerPrint;

    public AlarmJavaDemoView(SingleFrameApplication app) {
        super(app);

        initComponents();

        lUserID = new NativeLong(-1);
        lAlarmHandle = new NativeLong(-1);
        lListenHandle = new NativeLong(-1);
        fMSFCallBack = null;
        fGpsCallBack = null;

        fRemoteCfgCallBackCardGet = null;
        fRemoteCfgCallBackCardSet = null;
        fRemoteCfgCallBackFaceGet = null;
        fRemoteCfgCallBackFaceSet = null;

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);

        boolean initSuc = hCNetSDK.NET_DVR_Init();
        if (initSuc != true)
        {
                 JOptionPane.showMessageDialog(null, "初始化失败");
        }
    }

    public class FGPSDataCallback implements HCNetSDK.fGPSDataCallback
    {
        public void invoke(NativeLong nHandle, int dwState, Pointer lpBuffer, int dwBufLen, Pointer pUser)
        {
        }
    }

    public void AlarmDataHandle(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
    {
            String sAlarmType = new String();
            DefaultTableModel alarmTableModel = ((DefaultTableModel) jTableAlarm.getModel());//获取表格模型
            String[] newRow = new String[3];
            //报警时间
            Date today = new Date();
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            String[] sIP = new String[2];

            sAlarmType = new String("lCommand=") + lCommand.intValue();
            //lCommand是传的报警类型
            switch (lCommand.intValue())
            {     
                case HCNetSDK.COMM_ALARM_V30:///宏定义0x4000   9000报警信息主动上传
                    HCNetSDK.NET_DVR_ALARMINFO_V30 strAlarmInfoV30 = new HCNetSDK.NET_DVR_ALARMINFO_V30();
                    strAlarmInfoV30.write();
                    Pointer pInfoV30 = strAlarmInfoV30.getPointer();
                    pInfoV30.write(0, pAlarmInfo.getByteArray(0, strAlarmInfoV30.size()), 0, strAlarmInfoV30.size());
                    strAlarmInfoV30.read();
                    switch (strAlarmInfoV30.dwAlarmType)
                    {
                        case 0:
                            sAlarmType = sAlarmType + new String("：信号量报警") + "，"+ "报警输入口：" + (strAlarmInfoV30.dwAlarmInputNumber+1);
                            break;
                        case 1:
                            sAlarmType = sAlarmType + new String("：硬盘满");
                            break;
                        case 2:
                            sAlarmType = sAlarmType + new String("：信号丢失");
                            break;
                        case 3:
                            sAlarmType = sAlarmType + new String("：移动侦测") + "，"+ "报警通道：";
                             for (int i=0; i<64; i++)
                             {
                                if (strAlarmInfoV30.byChannel[i] == 1)
                                {
                                   sAlarmType=sAlarmType + "ch"+(i+1)+" ";
                               }
                            }
                            break;
                        case 4:
                            sAlarmType = sAlarmType + new String("：硬盘未格式化");
                            break;
                        case 5:
                            sAlarmType = sAlarmType + new String("：读写硬盘出错");
                            break;
                        case 6:
                            sAlarmType = sAlarmType + new String("：遮挡报警");
                            break;
                        case 7:
                            sAlarmType = sAlarmType + new String("：制式不匹配");
                            break;
                        case 8:
                            sAlarmType = sAlarmType + new String("：非法访问");
                            break;
                    }
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);
                    break;
                case HCNetSDK.COMM_ALARM_RULE:
                    HCNetSDK.NET_VCA_RULE_ALARM strVcaAlarm = new HCNetSDK.NET_VCA_RULE_ALARM();
                    strVcaAlarm.write();
                    Pointer pVcaInfo = strVcaAlarm.getPointer();
                    pVcaInfo.write(0, pAlarmInfo.getByteArray(0, strVcaAlarm.size()), 0, strVcaAlarm.size());
                    strVcaAlarm.read();

                    switch (strVcaAlarm.struRuleInfo.wEventTypeEx)
                    {
                        case 1:
                            sAlarmType = sAlarmType + new String("：穿越警戒面") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                        case 2:
                            sAlarmType = sAlarmType + new String("：目标进入区域") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                        case 3:
                            sAlarmType = sAlarmType + new String("：目标离开区域") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                        default:
                            sAlarmType = sAlarmType + new String("：其他行为分析报警") + "，" +
                                    "_wPort:" + strVcaAlarm.struDevInfo.wPort +
                                    "_byChannel:" + strVcaAlarm.struDevInfo.byChannel +
                                    "_byIvmsChannel:" +  strVcaAlarm.struDevInfo.byIvmsChannel +
                                    "_Dev IP：" + new String(strVcaAlarm.struDevInfo.struDevIP.sIpV4);
                            break;
                    }
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);

                    if(strVcaAlarm.dwPicDataLen>0)
                    {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            fout = new FileOutputStream(newName+"_VCA.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strVcaAlarm.pImage.getPointer().getByteBuffer(offset, strVcaAlarm.dwPicDataLen);
                            byte [] bytes = new byte[strVcaAlarm.dwPicDataLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        }catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_UPLOAD_PLATE_RESULT:
                    HCNetSDK.NET_DVR_PLATE_RESULT strPlateResult = new HCNetSDK.NET_DVR_PLATE_RESULT();
                    strPlateResult.write();
                    Pointer pPlateInfo = strPlateResult.getPointer();
                    pPlateInfo.write(0, pAlarmInfo.getByteArray(0, strPlateResult.size()), 0, strPlateResult.size());
                    strPlateResult.read();
                    try {
                        String srt3=new String(strPlateResult.struPlateInfo.sLicense,"GBK");
                        sAlarmType = sAlarmType + "：交通抓拍上传，车牌："+ srt3;
                    }
                     catch (UnsupportedEncodingException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                     }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);

                    if(strPlateResult.dwPicLen>0)
                    {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                            fout = new FileOutputStream(newName+"_PlateResult.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = strPlateResult.pBuffer1.getByteBuffer(offset, strPlateResult.dwPicLen);
                            byte [] bytes = new byte[strPlateResult.dwPicLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;
                case HCNetSDK.COMM_ITS_PLATE_RESULT:
                    HCNetSDK.NET_ITS_PLATE_RESULT strItsPlateResult = new HCNetSDK.NET_ITS_PLATE_RESULT();
                    strItsPlateResult.write();
                    Pointer pItsPlateInfo = strItsPlateResult.getPointer();
                    pItsPlateInfo.write(0, pAlarmInfo.getByteArray(0, strItsPlateResult.size()), 0, strItsPlateResult.size());
                    strItsPlateResult.read();
                    try {
                        String srt3=new String(strItsPlateResult.struPlateInfo.sLicense,"GBK");
                        sAlarmType = sAlarmType + ",车辆类型："+strItsPlateResult.byVehicleType + ",交通抓拍上传，车牌："+ srt3;
                    }
                     catch (UnsupportedEncodingException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                     }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);

                    for(int i=0;i<strItsPlateResult.dwPicNum;i++)
                    {
                        if(strItsPlateResult.struPicInfo[i].dwDataLen>0)
                        {
                             SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                             String newName = sf.format(new Date());
                             FileOutputStream fout;
                             try {
                                 String filename = newName+"_ITSPlateResult_type"+strItsPlateResult.struPicInfo[i].byType+".jpg";
                                 fout = new FileOutputStream(filename);
                                 //将字节写入文件
                                 long offset = 0;
                                 ByteBuffer buffers = strItsPlateResult.struPicInfo[i].pBuffer.getByteBuffer(offset, strItsPlateResult.struPicInfo[i].dwDataLen);
                                 byte [] bytes = new byte[strItsPlateResult.struPicInfo[i].dwDataLen];
                                 buffers.rewind();
                                 buffers.get(bytes);
                                 fout.write(bytes);
                                 fout.close();
                             } catch (FileNotFoundException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }
                        }
                    }
                    break;
                case HCNetSDK.COMM_ALARM_PDC:
                    HCNetSDK.NET_DVR_PDC_ALRAM_INFO strPDCResult = new HCNetSDK.NET_DVR_PDC_ALRAM_INFO();
                    strPDCResult.write();
                    Pointer pPDCInfo = strPDCResult.getPointer();
                    pPDCInfo.write(0, pAlarmInfo.getByteArray(0, strPDCResult.size()), 0, strPDCResult.size());
                    strPDCResult.read();

                    sAlarmType = sAlarmType + "：客流量统计，进入人数："+ strPDCResult.dwEnterNum + "，离开人数：" + strPDCResult.dwLeaveNum;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(strPDCResult.struDevInfo.struDevIP.sIpV4).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);
                    break;

                case HCNetSDK.COMM_ITS_PARK_VEHICLE:
                    HCNetSDK.NET_ITS_PARK_VEHICLE strItsParkVehicle = new HCNetSDK.NET_ITS_PARK_VEHICLE();
                    strItsParkVehicle.write();
                    Pointer pItsParkVehicle = strItsParkVehicle.getPointer();
                    pItsParkVehicle.write(0, pAlarmInfo.getByteArray(0, strItsParkVehicle.size()), 0, strItsParkVehicle.size());
                    strItsParkVehicle.read();
                    try {
                        String srtParkingNo=new String(strItsParkVehicle.byParkingNo).trim(); //车位编号
                        String srtPlate=new String(strItsParkVehicle.struPlateInfo.sLicense,"GBK").trim(); //车牌号码
                        sAlarmType = sAlarmType + ",停产场数据,车位编号："+ srtParkingNo + ",车位状态："
                                + strItsParkVehicle.byLocationStatus+ ",车牌："+ srtPlate;
                    }
                     catch (UnsupportedEncodingException e1) {
                         // TODO Auto-generated catch block
                         e1.printStackTrace();
                     } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
                     }

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);

                    for(int i=0;i<strItsParkVehicle.dwPicNum;i++)
                    {
                        if(strItsParkVehicle.struPicInfo[i].dwDataLen>0)
                        {
                             SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                             String newName = sf.format(new Date());
                             FileOutputStream fout;
                             try {
                                 String filename = newName+"_ITSPark_type"+strItsParkVehicle.struPicInfo[i].byType+".jpg";
                                 fout = new FileOutputStream(filename);
                                 //将字节写入文件
                                 long offset = 0;
                                 ByteBuffer buffers = strItsParkVehicle.struPicInfo[i].pBuffer.getByteBuffer(offset, strItsParkVehicle.struPicInfo[i].dwDataLen);
                                 byte [] bytes = new byte[strItsParkVehicle.struPicInfo[i].dwDataLen];
                                 buffers.rewind();
                                 buffers.get(bytes);
                                 fout.write(bytes);
                                 fout.close();
                             } catch (FileNotFoundException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                             }
                        }
                    }
                    break;
                case HCNetSDK.COMM_ALARM_ACS: //门禁主机报警信息
                    HCNetSDK.NET_DVR_ACS_ALARM_INFO strACSInfo = new HCNetSDK.NET_DVR_ACS_ALARM_INFO();
                    strACSInfo.write();
                    Pointer pACSInfo = strACSInfo.getPointer();
                    pACSInfo.write(0, pAlarmInfo.getByteArray(0, strACSInfo.size()), 0, strACSInfo.size());
                    strACSInfo.read();

                    sAlarmType = sAlarmType + "：门禁主机报警信息，卡号："+  new String(strACSInfo.struAcsEventInfo.byCardNo).trim() + "，卡类型：" +
                            strACSInfo.struAcsEventInfo.byCardType + "，报警主类型：" + strACSInfo.dwMajor + "，报警次类型：" + strACSInfo.dwMinor;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);

                    if(strACSInfo.dwPicDataLen>0)
                    {
                        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                        String newName = sf.format(new Date());
                        FileOutputStream fout;
                        try {
                                 String filename = newName+"_ACS_card_"+ new String(strACSInfo.struAcsEventInfo.byCardNo).trim()+".jpg";
                                 fout = new FileOutputStream(filename);
                                 //将字节写入文件
                                 long offset = 0;
                                 ByteBuffer buffers = strACSInfo.pPicData.getByteBuffer(offset, strACSInfo.dwPicDataLen);
                                 byte [] bytes = new byte[strACSInfo.dwPicDataLen];
                                 buffers.rewind();
                                 buffers.get(bytes);
                                 fout.write(bytes);
                                 fout.close();
                        } catch (FileNotFoundException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                        } catch (IOException e) {
                                 // TODO Auto-generated catch block
                                 e.printStackTrace();
                        }
                   }
                    break;
                case HCNetSDK.COMM_ID_INFO_ALARM: //身份证信息
                    HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM strIDCardInfo = new HCNetSDK.NET_DVR_ID_CARD_INFO_ALARM();
                    strIDCardInfo.write();
                    Pointer pIDCardInfo = strIDCardInfo.getPointer();
                    pIDCardInfo.write(0, pAlarmInfo.getByteArray(0, strIDCardInfo.size()), 0, strIDCardInfo.size());
                    strIDCardInfo.read();

                    sAlarmType = sAlarmType + "：门禁身份证刷卡信息，身份证号码："+  new String(strIDCardInfo.struIDCardCfg.byIDNum).trim() + "，姓名：" +
                            new String(strIDCardInfo.struIDCardCfg.byName).trim() + "，报警主类型：" + strIDCardInfo.dwMajor + "，报警次类型：" + strIDCardInfo.dwMinor;

                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);
                    break;
                default:
                    newRow[0] = dateFormat.format(today);
                    //报警类型
                    newRow[1] = sAlarmType;
                    //报警设备IP地址
                    sIP = new String(pAlarmer.sDeviceIP).split("\0", 2);
                    newRow[2] = sIP[0];
                    alarmTableModel.insertRow(0, newRow);
                    break;
            }
    }

    public class FMSGCallBack_V31 implements HCNetSDK.FMSGCallBack_V31
    {
        public boolean invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
        {
            AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
            return true;
        }

    }
    public class FMSGCallBack implements HCNetSDK.FMSGCallBack
    {
        //报警信息回调函数

        public void invoke(NativeLong lCommand, HCNetSDK.NET_DVR_ALARMER pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser)
        {
            AlarmDataHandle(lCommand, pAlarmer, pAlarmInfo, dwBufLen, pUser);
        }
     }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = AlarmJavaDemoApp.getApplication().getMainFrame();
            aboutBox = new AlarmJavaDemoAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        AlarmJavaDemoApp.getApplication().show(aboutBox);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jTextFieldIPAddress = new javax.swing.JTextField();
        jLabelIPAddress = new javax.swing.JLabel();
        jLabelUserName = new javax.swing.JLabel();
        jTextFieldUserName = new javax.swing.JTextField();
        jPasswordFieldPassword = new javax.swing.JPasswordField();
        jLabelPassWord = new javax.swing.JLabel();
        jLabelPortNumber = new javax.swing.JLabel();
        jTextFieldPortNumber = new javax.swing.JTextField();
        jButtonLogin = new javax.swing.JButton();
        jLogOut = new javax.swing.JButton();
        jBtnAlarm = new javax.swing.JButton();
        jBtnCloseAlarm = new javax.swing.JButton();
        jScrollPanelAlarmList = new javax.swing.JScrollPane();
        jTableAlarm = new javax.swing.JTable();
        jButtonListen = new javax.swing.JButton();
        jTextFieldListenIP = new javax.swing.JTextField();
        jLabelIPAddress1 = new javax.swing.JLabel();
        jTextFieldListenPort = new javax.swing.JTextField();
        jLabelPortNumber1 = new javax.swing.JLabel();
        jButtonStopListen = new javax.swing.JButton();
        jButtonTest = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        javax.swing.JButton jBtnGetFaceCfg = new javax.swing.JButton();
        jBtnSetFaceCfg = new javax.swing.JButton();
        jBtnDelFace = new javax.swing.JButton();
        jBtnPlanCfg = new javax.swing.JButton();
        jBtnDoorCfg = new javax.swing.JButton();
        jBtnCaptureFace = new javax.swing.JButton();
        jBtnFingerPrint = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();

        mainPanel.setName("mainPanel"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(alarmjavademo.AlarmJavaDemoApp.class).getContext().getResourceMap(AlarmJavaDemoView.class);
        jTextFieldIPAddress.setText(resourceMap.getString("jTextFieldIPAddress.text")); // NOI18N
        jTextFieldIPAddress.setName("jTextFieldIPAddress"); // NOI18N

        jLabelIPAddress.setText(resourceMap.getString("jLabelIPAddress.text")); // NOI18N
        jLabelIPAddress.setName("jLabelIPAddress"); // NOI18N

        jLabelUserName.setText(resourceMap.getString("jLabelUserName.text")); // NOI18N
        jLabelUserName.setName("jLabelUserName"); // NOI18N

        jTextFieldUserName.setText(resourceMap.getString("jTextFieldUserName.text")); // NOI18N
        jTextFieldUserName.setName("jTextFieldUserName"); // NOI18N

        jPasswordFieldPassword.setText(resourceMap.getString("jPasswordFieldPassword.text")); // NOI18N
        jPasswordFieldPassword.setName("jPasswordFieldPassword"); // NOI18N

        jLabelPassWord.setText(resourceMap.getString("jLabelPassWord.text")); // NOI18N
        jLabelPassWord.setName("jLabelPassWord"); // NOI18N

        jLabelPortNumber.setText(resourceMap.getString("jLabelPortNumber.text")); // NOI18N
        jLabelPortNumber.setName("jLabelPortNumber"); // NOI18N

        jTextFieldPortNumber.setText(resourceMap.getString("jTextFieldPortNumber.text")); // NOI18N
        jTextFieldPortNumber.setName("jTextFieldPortNumber"); // NOI18N

        jButtonLogin.setText(resourceMap.getString("jButtonLogin.text")); // NOI18N
        jButtonLogin.setName("jButtonLogin"); // NOI18N
        jButtonLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonLoginActionPerformed(evt);
            }
        });

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(alarmjavademo.AlarmJavaDemoApp.class).getContext().getActionMap(AlarmJavaDemoView.class, this);
        jLogOut.setAction(actionMap.get("Logout")); // NOI18N
        jLogOut.setText(resourceMap.getString("jLogOut.text")); // NOI18N
        jLogOut.setName("jLogOut"); // NOI18N

        jBtnAlarm.setAction(actionMap.get("SetupAlarmChan")); // NOI18N
        jBtnAlarm.setText(resourceMap.getString("jBtnAlarm.text")); // NOI18N
        jBtnAlarm.setName("jBtnAlarm"); // NOI18N

        jBtnCloseAlarm.setAction(actionMap.get("CloseAlarmChan")); // NOI18N
        jBtnCloseAlarm.setText(resourceMap.getString("jBtnCloseAlarm.text")); // NOI18N
        jBtnCloseAlarm.setName("jBtnCloseAlarm"); // NOI18N

        jScrollPanelAlarmList.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jScrollPanelAlarmList.setName("jScrollPanelAlarmList"); // NOI18N

        jTableAlarm.setModel(initialTableModel());
        jTableAlarm.setName("jTableAlarm"); // NOI18N
        jScrollPanelAlarmList.setViewportView(jTableAlarm);

        jButtonListen.setAction(actionMap.get("StartAlarmListen")); // NOI18N
        jButtonListen.setText(resourceMap.getString("jButtonListen.text")); // NOI18N
        jButtonListen.setName("jButtonListen"); // NOI18N

        jTextFieldListenIP.setText(resourceMap.getString("jTextFieldListenIP.text")); // NOI18N
        jTextFieldListenIP.setName("jTextFieldListenIP"); // NOI18N

        jLabelIPAddress1.setText(resourceMap.getString("jLabelIPAddress1.text")); // NOI18N
        jLabelIPAddress1.setName("jLabelIPAddress1"); // NOI18N

        jTextFieldListenPort.setText(resourceMap.getString("jTextFieldListenPort.text")); // NOI18N
        jTextFieldListenPort.setName("jTextFieldListenPort"); // NOI18N

        jLabelPortNumber1.setText(resourceMap.getString("jLabelPortNumber1.text")); // NOI18N
        jLabelPortNumber1.setName("jLabelPortNumber1"); // NOI18N

        jButtonStopListen.setAction(actionMap.get("StopAlarmListen")); // NOI18N
        jButtonStopListen.setText(resourceMap.getString("jButtonStopListen.text")); // NOI18N
        jButtonStopListen.setName("jButtonStopListen"); // NOI18N

        jButtonTest.setAction(actionMap.get("OneTest")); // NOI18N
        jButtonTest.setText(resourceMap.getString("jButtonTest.text")); // NOI18N
        jButtonTest.setActionCommand(resourceMap.getString("jButtonTest.actionCommand")); // NOI18N
        jButtonTest.setName("jButtonTest"); // NOI18N

        jButton1.setAction(actionMap.get("GetCardInfo")); // NOI18N
        jButton1.setText(resourceMap.getString("jBtnGetCard.text")); // NOI18N
        jButton1.setName("jBtnGetCard"); // NOI18N

        jButton2.setAction(actionMap.get("SetCardInfo")); // NOI18N
        jButton2.setText(resourceMap.getString("jBtnSetCard.text")); // NOI18N
        jButton2.setName("jBtnSetCard"); // NOI18N

        jBtnGetFaceCfg.setAction(actionMap.get("jBtnGetFaceCfg")); // NOI18N
        jBtnGetFaceCfg.setText(resourceMap.getString("jBtnGetFaceCfg.text")); // NOI18N
        jBtnGetFaceCfg.setActionCommand(resourceMap.getString("jBtnGetFaceCfg.actionCommand")); // NOI18N
        jBtnGetFaceCfg.setName("jBtnGetFaceCfg"); // NOI18N

        jBtnSetFaceCfg.setAction(actionMap.get("jBtnSetFaceCfg")); // NOI18N
        jBtnSetFaceCfg.setText(resourceMap.getString("jBtnSetFaceCfg.text")); // NOI18N
        jBtnSetFaceCfg.setActionCommand(resourceMap.getString("jBtnSetFaceCfg.actionCommand")); // NOI18N
        jBtnSetFaceCfg.setFocusCycleRoot(true);
        jBtnSetFaceCfg.setName("jBtnSetFaceCfg"); // NOI18N

        jBtnDelFace.setAction(actionMap.get("jBtnDelFace")); // NOI18N
        jBtnDelFace.setText(resourceMap.getString("jBtnDelFace.text")); // NOI18N
        jBtnDelFace.setName("jBtnDelFace"); // NOI18N

        jBtnPlanCfg.setAction(actionMap.get("jBtnPlanCfg")); // NOI18N
        jBtnPlanCfg.setText(resourceMap.getString("jBtnPlanCfg.text")); // NOI18N
        jBtnPlanCfg.setName("jBtnPlanCfg"); // NOI18N

        jBtnDoorCfg.setAction(actionMap.get("jBtnDoorCfg")); // NOI18N
        jBtnDoorCfg.setText(resourceMap.getString("jBtnDoorCfg.text")); // NOI18N
        jBtnDoorCfg.setName("jBtnDoorCfg"); // NOI18N

        jBtnCaptureFace.setAction(actionMap.get("jBtnCaptureFace")); // NOI18N
        jBtnCaptureFace.setText(resourceMap.getString("jBtnCaptureFace.text")); // NOI18N
        jBtnCaptureFace.setName("jBtnCaptureFace"); // NOI18N

        jBtnFingerPrint.setAction(actionMap.get("jBtnFingerPrint")); // NOI18N
        jBtnFingerPrint.setText(resourceMap.getString("jBtnFingerPrint.text")); // NOI18N
        jBtnFingerPrint.setName("jBtnFingerPrint"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabelPortNumber)
                                .addGap(26, 26, 26)
                                .addComponent(jTextFieldPortNumber))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                                .addComponent(jLabelIPAddress)
                                .addGap(14, 14, 14)
                                .addComponent(jTextFieldIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(31, 31, 31)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabelUserName)
                            .addComponent(jLabelPassWord))
                        .addGap(14, 14, 14)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jPasswordFieldPassword)
                            .addComponent(jTextFieldUserName, javax.swing.GroupLayout.DEFAULT_SIZE, 96, Short.MAX_VALUE))
                        .addGap(48, 48, 48)
                        .addComponent(jButtonLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(30, 30, 30)
                        .addComponent(jLogOut, javax.swing.GroupLayout.PREFERRED_SIZE, 79, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(43, 43, 43)
                        .addComponent(jButtonTest))
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jScrollPanelAlarmList, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, mainPanelLayout.createSequentialGroup()
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addComponent(jBtnAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jBtnCloseAlarm, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addComponent(jButton1)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButton2)))
                            .addGap(28, 28, 28)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addComponent(jLabelIPAddress1)
                                    .addGap(12, 12, 12)
                                    .addComponent(jTextFieldListenIP, javax.swing.GroupLayout.PREFERRED_SIZE, 119, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(18, 18, 18)
                                    .addComponent(jLabelPortNumber1)
                                    .addGap(10, 10, 10)
                                    .addComponent(jTextFieldListenPort, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addComponent(jBtnGetFaceCfg)
                                    .addGap(26, 26, 26)
                                    .addComponent(jBtnSetFaceCfg)
                                    .addGap(26, 26, 26)
                                    .addComponent(jBtnDelFace)))
                            .addGap(22, 22, 22)
                            .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(mainPanelLayout.createSequentialGroup()
                                    .addComponent(jButtonListen)
                                    .addGap(18, 18, 18)
                                    .addComponent(jButtonStopListen))
                                .addComponent(jBtnPlanCfg))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addComponent(jBtnDoorCfg)
                        .addGap(18, 18, 18)
                        .addComponent(jBtnCaptureFace)
                        .addGap(18, 18, 18)
                        .addComponent(jBtnFingerPrint)))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelIPAddress)
                                    .addComponent(jTextFieldIPAddress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabelPortNumber)
                                    .addComponent(jTextFieldPortNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(mainPanelLayout.createSequentialGroup()
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabelUserName)
                                    .addComponent(jTextFieldUserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                    .addComponent(jLabelPassWord)
                                    .addComponent(jPasswordFieldPassword, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                    .addGroup(mainPanelLayout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButtonLogin)
                            .addComponent(jLogOut)
                            .addComponent(jButtonTest))))
                .addGap(18, 18, 18)
                .addComponent(jScrollPanelAlarmList, javax.swing.GroupLayout.PREFERRED_SIZE, 214, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jBtnAlarm)
                        .addComponent(jBtnCloseAlarm))
                    .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabelIPAddress1)
                        .addComponent(jTextFieldListenIP, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jLabelPortNumber1)
                        .addComponent(jTextFieldListenPort, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(jButtonListen)
                        .addComponent(jButtonStopListen)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton2)
                    .addComponent(jBtnGetFaceCfg)
                    .addComponent(jBtnSetFaceCfg)
                    .addComponent(jBtnDelFace)
                    .addComponent(jBtnPlanCfg))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jBtnDoorCfg)
                    .addComponent(jBtnCaptureFace)
                    .addComponent(jBtnFingerPrint))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jBtnAlarm.getAccessibleContext().setAccessibleName(resourceMap.getString("jBtnAlarm.AccessibleContext.accessibleName")); // NOI18N

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        exitMenuItem.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                exitMenuItemMouseClicked(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 823, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 803, Short.MAX_VALUE)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel))
                .addGap(3, 3, 3))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonLoginActionPerformed

        //注册之前先注销已注册的用户,预览情况下不可注销     
        if (lUserID.longValue() > -1) {
            //先注销
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = new NativeLong(-1);
        }

        //注册
        m_sDeviceIP = jTextFieldIPAddress.getText();//设备ip地址
        m_strDeviceInfo = new HCNetSDK.NET_DVR_DEVICEINFO_V30();
        int iPort = Integer.parseInt(jTextFieldPortNumber.getText());
        lUserID = hCNetSDK.NET_DVR_Login_V30(m_sDeviceIP,
                (short) iPort, jTextFieldUserName.getText(), new String(jPasswordFieldPassword.getPassword()), m_strDeviceInfo);

        long userID = lUserID.longValue();
        if (userID == -1) {
            JOptionPane.showMessageDialog(null, "注册失败");
        } else {
            JOptionPane.showMessageDialog(null, "注册成功");
        }
}//GEN-LAST:event_jButtonLoginActionPerformed

    private void exitMenuItemMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_exitMenuItemMouseClicked
        // TODO add your handling code here:
        if (lAlarmHandle.intValue() > -1)
        {
            hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle);
            lAlarmHandle = new NativeLong(-1);
        }
        if (lUserID.longValue() > -1) {
            //先注销
            hCNetSDK.NET_DVR_Logout(lUserID);
            lUserID = new NativeLong(-1);
        }
        hCNetSDK.NET_DVR_Cleanup();
    }//GEN-LAST:event_exitMenuItemMouseClicked

    @Action
    public void Logout() {
        //报警撤防
        if (lAlarmHandle.intValue() > -1)
        {
            if(!hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle))
            {
                 JOptionPane.showMessageDialog(null, "撤防失败");
            }
            else
            {
                lAlarmHandle = new NativeLong(-1);
            }
        }

        //注销
        if (lUserID.longValue() > -1) {            
            if(hCNetSDK.NET_DVR_Logout(lUserID))
            {
                JOptionPane.showMessageDialog(null, "注销成功");
                lUserID = new NativeLong(-1);
            }
        }
    }

    @Action
    public void SetupAlarmChan() {
        if (lUserID.intValue() == -1)
        {
            JOptionPane.showMessageDialog(null, "请先注册");
            return;
        }
         if (lAlarmHandle.intValue() < 0)//尚未布防,需要布防
         {
                if (fMSFCallBack_V31 == null)
                {
                    fMSFCallBack_V31 = new FMSGCallBack_V31();
                    Pointer pUser = null;
                    if (!hCNetSDK.NET_DVR_SetDVRMessageCallBack_V31(fMSFCallBack_V31, pUser))
                    {
                        System.out.println("设置回调函数失败!");
                    }
                }
                HCNetSDK.NET_DVR_SETUPALARM_PARAM m_strAlarmInfo = new HCNetSDK.NET_DVR_SETUPALARM_PARAM();
                m_strAlarmInfo.dwSize=m_strAlarmInfo.size();
                m_strAlarmInfo.byLevel=1;
                m_strAlarmInfo.byAlarmInfoType=1;
                m_strAlarmInfo.write();
                lAlarmHandle = hCNetSDK.NET_DVR_SetupAlarmChan_V41(lUserID, m_strAlarmInfo);
                if (lAlarmHandle.intValue() == -1)
                {
                    JOptionPane.showMessageDialog(null, "布防失败");
                }
                else
                {
                    JOptionPane.showMessageDialog(null, "布防成功");
                }
          }
    }

    @Action
    public void CloseAlarmChan() {
        //报警撤防
        if (lAlarmHandle.intValue() > -1)
        {
            if(hCNetSDK.NET_DVR_CloseAlarmChan_V30(lAlarmHandle))
            {
                JOptionPane.showMessageDialog(null, "撤防成功");
                lAlarmHandle = new NativeLong(-1);
            }
        }
    }

     /*************************************************
    函数:      initialTableModel
    函数描述:	初始化报警信息列表,写入列名称
     *************************************************/
    public DefaultTableModel initialTableModel()
    {
        String tabeTile[];
        tabeTile = new String[]{"时间", "报警信息", "设备信息" };
        DefaultTableModel alarmTableModel = new DefaultTableModel(tabeTile, 0);
        return alarmTableModel;
    }

    @Action
    public void StartAlarmListen() {
        String m_sListenIP = jTextFieldListenIP.getText();//设备ip地址
        int iListenPort = Integer.parseInt(jTextFieldListenPort.getText());
        Pointer pUser = null;

        if (fMSFCallBack == null)
        {
             fMSFCallBack = new FMSGCallBack();
        }
        lListenHandle = hCNetSDK.NET_DVR_StartListen_V30(m_sListenIP, (short)iListenPort,fMSFCallBack, pUser);
        if(lListenHandle.intValue() < 0)
        {
            JOptionPane.showMessageDialog(null, "启动监听失败");
        }
        else
        {
             JOptionPane.showMessageDialog(null, "启动监听成功");
        }
    }

    @Action
    public void StopAlarmListen() {
        if(lListenHandle.intValue() < 0)
        {
            return;
        }

        if(!hCNetSDK.NET_DVR_StopListen_V30(lListenHandle))
        {
            JOptionPane.showMessageDialog(null, "停止监听失败");
        }
        else
        {
             JOptionPane.showMessageDialog(null, "停止监听成功");
        }
    }

    @Action
    public void OneTest() {
        
        HCNetSDK.NET_DVR_SNAPCFG struSnapCfg = new HCNetSDK.NET_DVR_SNAPCFG();
        struSnapCfg.dwSize=struSnapCfg.size();
        struSnapCfg.bySnapTimes =1;
        struSnapCfg.wSnapWaitTime =1000;
        struSnapCfg.write();

        if (false == hCNetSDK.NET_DVR_ContinuousShoot(lUserID, struSnapCfg))
	{
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "网络触发失败，错误号：" + iErr);
            return;
        }

        /*
        HCNetSDK.NET_DVR_CHANNEL_GROUP mstruChanGroup = new HCNetSDK.NET_DVR_CHANNEL_GROUP();
        mstruChanGroup.dwSize = mstruChanGroup.size();
        mstruChanGroup.dwChannel = 1;

        HCNetSDK.NET_VCA_TRAVERSE_PLANE_DETECTION mstruTraverseCfg = new HCNetSDK.NET_VCA_TRAVERSE_PLANE_DETECTION();	

	IntByReference pInt = new IntByReference(0);
        Pointer lpStatusList = pInt.getPointer();

	mstruChanGroup.write();
	mstruTraverseCfg.write();

        Pointer lpCond = mstruChanGroup.getPointer();
        Pointer lpInbuferCfg = mstruTraverseCfg.getPointer();
        
	if (false == hCNetSDK.NET_DVR_GetDeviceConfig(lUserID, HCNetSDK.NET_DVR_GET_TRAVERSE_PLANE_DETECTION, 1, lpCond, mstruChanGroup.size(), lpStatusList, lpInbuferCfg, mstruTraverseCfg.size()))
	{
            int iErr = hCNetSDK.NET_DVR_GetLastError();
            return;
        }
        mstruTraverseCfg.read();
        int dwMaxRelRecordChanNum = mstruTraverseCfg.dwMaxRelRecordChanNum;
         * */
    }

     public class FRemoteCfgCallBackCardGet implements HCNetSDK.FRemoteConfigCallback
    {
        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData)
        {
            HCNetSDK.MY_USER_DATA m_userData = new HCNetSDK.MY_USER_DATA();
            m_userData.write();
            Pointer pUserVData = m_userData.getPointer();
            pUserVData.write(0, pUserData.getByteArray(0, m_userData.size()), 0, m_userData.size());
            m_userData.read();

            System.out.println("长连接回调获取数据,NET_SDK_CALLBACK_TYPE_STATUS:" + dwType);
	    switch (dwType){
                case 0: //NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.REMOTECONFIGSTATUS_CARD struCfgStatus  = new HCNetSDK.REMOTECONFIGSTATUS_CARD();
                    struCfgStatus.write();
                    Pointer pCfgStatus = struCfgStatus.getPointer();
                    pCfgStatus.write(0, lpBuffer.getByteArray(0, struCfgStatus.size()), 0,struCfgStatus.size());
                    struCfgStatus.read();

                    int iStatus = 0;
                    for(int i=0;i<4;i++)
                    {
                        int ioffset = i*8;
                        int iByte = struCfgStatus.byStatus[i]&0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus){
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            System.out.println("查询卡参数成功,dwStatus:" + iStatus);
                            break;
                        case 1001:
                            System.out.println("正在查询卡参数中,dwStatus:" + iStatus);
                            break;
                        case 1002:
                            int iErrorCode = 0;
                            for(int i=0;i<4;i++)
                            {
                                int ioffset = i*8;
                                int iByte = struCfgStatus.byErrorCode[i]&0xff;
                                iErrorCode = iErrorCode + (iByte << ioffset);
                            }
                            System.out.println("查询卡参数失败, dwStatus:" + iStatus + "错误号:" + iErrorCode);
                            break;
                    }
                    break;
		case 2: //NET_SDK_CALLBACK_TYPE_DATA
			HCNetSDK.NET_DVR_CARD_CFG_V50 m_struCardInfo = new HCNetSDK.NET_DVR_CARD_CFG_V50();
			m_struCardInfo.write();
			Pointer pInfoV30 = m_struCardInfo.getPointer();
			pInfoV30.write(0, lpBuffer.getByteArray(0, m_struCardInfo.size()), 0,m_struCardInfo.size());
			m_struCardInfo.read();

                        String str = new String(m_struCardInfo.byCardNo);


                        try {
                            String srtName=new String(m_struCardInfo.byName,"GBK").trim(); //姓名
                            System.out.println("查询到的卡号,getCardNo:" + str + "姓名:" + srtName);
                        }
                        catch (UnsupportedEncodingException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
			break;
		default:
			break;
		}
        }
    }

    @Action
    public void GetCardInfo()
    {
        int iErr = 0;
        HCNetSDK.NET_DVR_CARD_CFG_COND m_struCardInputParam = new HCNetSDK.NET_DVR_CARD_CFG_COND();
	m_struCardInputParam.dwSize = m_struCardInputParam.size();
	m_struCardInputParam.dwCardNum = 0xffffffff; //查找全部
	m_struCardInputParam.byCheckCardNo = 1;

	Pointer lpInBuffer = m_struCardInputParam.getPointer();
        fRemoteCfgCallBackCardGet = new FRemoteCfgCallBackCardGet();
        m_struCardInputParam.write();

        HCNetSDK.MY_USER_DATA userData = new HCNetSDK.MY_USER_DATA();
        userData.dwSize = userData.size();
        userData.byteData = "1234567".getBytes();
        Pointer pUserData = userData.getPointer();
        userData.write();

	NativeLong lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_GET_CARD_CFG_V50, lpInBuffer, m_struCardInputParam.size(),fRemoteCfgCallBackCardGet, pUserData);
        if (lHandle.intValue() < 0)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "建立长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "建立获取卡参数长连接成功!");

     /*	//查找指定卡号
      HCNetSDK.NET_DVR_CARD_CFG_SEND_DATA m_struCardSendInputParam = new HCNetSDK.NET_DVR_CARD_CFG_SEND_DATA();
	m_struCardSendInputParam.read();
	m_struCardSendInputParam.dwSize = m_struCardSendInputParam.size();
	m_struCardSendInputParam.byCardNo = "111010".getBytes();
	m_struCardSendInputParam.byRes = "0".getBytes();
        
        Pointer pSendBuf = m_struCardSendInputParam.getPointer();
        m_struCardSendInputParam.write();

        if(!hCNetSDK.NET_DVR_SendRemoteConfig(lHandle, 0x3, pSendBuf, m_struCardSendInputParam.size()))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "ENUM_ACS_SEND_DATA失败，错误号：" + iErr);
            return;
        }*/

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        if(!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "断开长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "断开长连接成功!");
    }

     public class FRemoteCfgCallBackCardSet implements HCNetSDK.FRemoteConfigCallback
    {
        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData)
        {
            System.out.println("长连接回调获取数据,NET_SDK_CALLBACK_TYPE_STATUS:" + dwType);
            switch (dwType){
		case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.REMOTECONFIGSTATUS_CARD struCardStatus = new HCNetSDK.REMOTECONFIGSTATUS_CARD();
                    struCardStatus.write();
                    Pointer pInfoV30 = struCardStatus.getPointer();
                    pInfoV30.write(0, lpBuffer.getByteArray(0, struCardStatus.size()), 0,struCardStatus.size());
                    struCardStatus.read();
                    
                    int iStatus = 0;
                    for(int i=0;i<4;i++)
                    {
                        int ioffset = i*8;
                        int iByte = struCardStatus.byStatus[i]&0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus){
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            System.out.println("下发卡参数成功,dwStatus:" + iStatus);
                            break;
                        case 1001:
                            System.out.println("正在下发卡参数中,dwStatus:" + iStatus);
                            break;
                        case 1002:
                            int iErrorCode = 0;
                            for(int i=0;i<4;i++)
                            {
                                int ioffset = i*8;
                                int iByte = struCardStatus.byErrorCode[i]&0xff;
                                iErrorCode = iErrorCode + (iByte << ioffset);
                            }
                            System.out.println("下发卡参数失败, dwStatus:" + iStatus + "错误号:" + iErrorCode);
                            break;
                    }
                    break;
		default:
			break;
		}
        }
    }

    @Action
    public void SetCardInfo() throws UnsupportedEncodingException {
        int iErr = 0;
        
        //设置卡参数	
	HCNetSDK.NET_DVR_CARD_CFG_COND m_struCardInputParamSet = new HCNetSDK.NET_DVR_CARD_CFG_COND();
	m_struCardInputParamSet.read();
	m_struCardInputParamSet.dwSize = m_struCardInputParamSet.size();
	m_struCardInputParamSet.dwCardNum = 1;
	m_struCardInputParamSet.byCheckCardNo = 1;
        
	Pointer lpInBuffer = m_struCardInputParamSet.getPointer();
        m_struCardInputParamSet.write();

	Pointer pUserData = null;
        fRemoteCfgCallBackCardSet = new FRemoteCfgCallBackCardSet();

	NativeLong lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_SET_CARD_CFG_V50, lpInBuffer, m_struCardInputParamSet.size(), fRemoteCfgCallBackCardSet, pUserData);
	if (lHandle.intValue() < 0)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "建立长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "建立设置卡参数长连接成功!");

        HCNetSDK.NET_DVR_CARD_CFG_V50 struCardInfo = new HCNetSDK.NET_DVR_CARD_CFG_V50(); //卡参数
        struCardInfo.read();
        struCardInfo.dwSize = struCardInfo.size();
        struCardInfo.dwModifyParamType = 0x00000001 + 0x00000002 + 0x00000004 + 0x00000008 +
                0x00000010 + 0x00000020 + 0x00000080 + 0x00000100 + 0x00000200 + 0x00000400 + 0x00000800;
        /***
         * #define CARD_PARAM_CARD_VALID       0x00000001  //卡是否有效参数
         * #define CARD_PARAM_VALID            0x00000002  //有效期参数
         * #define CARD_PARAM_CARD_TYPE        0x00000004  //卡类型参数
         * #define CARD_PARAM_DOOR_RIGHT       0x00000008  //门权限参数
         * #define CARD_PARAM_LEADER_CARD      0x00000010  //首卡参数
         * #define CARD_PARAM_SWIPE_NUM        0x00000020  //最大刷卡次数参数
         * #define CARD_PARAM_GROUP            0x00000040  //所属群组参数
         * #define CARD_PARAM_PASSWORD         0x00000080  //卡密码参数
         * #define CARD_PARAM_RIGHT_PLAN       0x00000100  //卡权限计划参数
         * #define CARD_PARAM_SWIPED_NUM       0x00000200  //已刷卡次数
         * #define CARD_PARAM_EMPLOYEE_NO      0x00000400  //工号
         * #define CARD_PARAM_NAME             0x00000800  //姓名
         */

        String strCardNo = "666111078";
        for (int i = 0; i < HCNetSDK.ACS_CARD_NO_LEN; i++)
        {
            struCardInfo.byCardNo[i] = 0;
        }
        for (int i = 0; i <  strCardNo.length(); i++)
        {
            struCardInfo.byCardNo[i] = strCardNo.getBytes()[i];
        }


        struCardInfo.byCardValid = 1;
        struCardInfo.byCardType =1;
        struCardInfo.byLeaderCard = 0;
        struCardInfo.byDoorRight[0]  = 1; //门1有权限
        struCardInfo.wCardRightPlan[0].wRightPlan[0] = 1; //门1关联卡参数计划模板1
        
        //卡有效期
        struCardInfo.struValid.byEnable = 1;
        struCardInfo.struValid.struBeginTime.wYear = 2017;
        struCardInfo.struValid.struBeginTime.byMonth = 12;
        struCardInfo.struValid.struBeginTime.byDay = 1;
        struCardInfo.struValid.struBeginTime.byHour = 0;
        struCardInfo.struValid.struBeginTime.byMinute = 0;
        struCardInfo.struValid.struBeginTime.bySecond = 0;
        struCardInfo.struValid.struEndTime.wYear = 2018;
        struCardInfo.struValid.struEndTime.byMonth = 12;
        struCardInfo.struValid.struEndTime.byDay = 1;
        struCardInfo.struValid.struEndTime.byHour = 0;
        struCardInfo.struValid.struEndTime.byMinute = 0;
        struCardInfo.struValid.struEndTime.bySecond = 0;

        struCardInfo.dwMaxSwipeTime = 0; //无次数限制
        struCardInfo.dwSwipeTime = 0;
        struCardInfo.byCardPassword = "12346".getBytes();
        struCardInfo.dwEmployeeNo = 22;

        byte[] strCardName = "测试".getBytes("GBK");
        for (int i = 0; i < HCNetSDK.NAME_LEN; i++)
        {
            struCardInfo.byName[i] = 0;
        }
        for (int i = 0; i <  strCardName.length; i++)
        {
             struCardInfo.byName[i] = strCardName[i];
        }

        struCardInfo.write();
        Pointer pSendBufSet = struCardInfo.getPointer();     

        if(!hCNetSDK.NET_DVR_SendRemoteConfig(lHandle, 0x3, pSendBufSet, struCardInfo.size()))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "ENUM_ACS_SEND_DATA失败，错误号：" + iErr);
            return;
        }

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        if(!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "断开长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "断开长连接成功!");
    }

    public class FRemoteCfgCallBackFaceGet implements HCNetSDK.FRemoteConfigCallback
    {
        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData)
        {
            System.out.println("长连接回调获取数据,NET_SDK_CALLBACK_TYPE_STATUS:" + dwType);
	    switch (dwType){
                case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.REMOTECONFIGSTATUS_CARD struCfgStatus  = new HCNetSDK.REMOTECONFIGSTATUS_CARD();
                    struCfgStatus.write();
                    Pointer pCfgStatus = struCfgStatus.getPointer();
                    pCfgStatus.write(0, lpBuffer.getByteArray(0, struCfgStatus.size()), 0,struCfgStatus.size());
                    struCfgStatus.read();

                    int iStatus = 0;
                    for(int i=0;i<4;i++)
                    {
                        int ioffset = i*8;
                        int iByte = struCfgStatus.byStatus[i]&0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus){
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            System.out.println("查询人脸参数成功,dwStatus:" + iStatus);
                            break;
                        case 1001:
                            System.out.println("正在查询人脸参数中,dwStatus:" + iStatus);
                            break;
                        case 1002:
                            int iErrorCode = 0;
                            for(int i=0;i<4;i++)
                            {
                                int ioffset = i*8;
                                int iByte = struCfgStatus.byErrorCode[i]&0xff;
                                iErrorCode = iErrorCode + (iByte << ioffset);
                            }
                            System.out.println("查询人脸参数失败, dwStatus:" + iStatus + "错误号:" + iErrorCode);
                            break;
                    }
                    break;
		case 2: //NET_SDK_CALLBACK_TYPE_DATA
			HCNetSDK.NET_DVR_FACE_PARAM_CFG m_struFaceInfo = new HCNetSDK.NET_DVR_FACE_PARAM_CFG();
			m_struFaceInfo.write();
			Pointer pInfoV30 = m_struFaceInfo.getPointer();
			pInfoV30.write(0, lpBuffer.getByteArray(0, m_struFaceInfo.size()), 0,m_struFaceInfo.size());
			m_struFaceInfo.read();
			String str = new String(m_struFaceInfo.byCardNo).trim();
			System.out.println("查询到人脸数据关联的卡号,getCardNo:" + str + ",人脸数据类型:" + m_struFaceInfo.byFaceDataType);
                        if(m_struFaceInfo.dwFaceLen >0)
                        {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                            fout = new FileOutputStream(newName +"_Card[" + str + "]_ACSFaceCfg.jpg");
                            //将字节写入文件
                            long offset = 0;
                            ByteBuffer buffers = m_struFaceInfo.pFaceBuffer.getByteBuffer(offset, m_struFaceInfo.dwFaceLen);
                            byte [] bytes = new byte[m_struFaceInfo.dwFaceLen];
                            buffers.rewind();
                            buffers.get(bytes);
                            fout.write(bytes);
                            fout.close();
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
			break;
		default:
			break;
		}
        }
    }

    @Action
    public void jBtnGetFaceCfg() {
        int iErr = 0;
        HCNetSDK.NET_DVR_FACE_PARAM_COND m_struFaceInputParam = new HCNetSDK.NET_DVR_FACE_PARAM_COND();
	m_struFaceInputParam.dwSize = m_struFaceInputParam.size();
	m_struFaceInputParam.byCardNo = "111011".getBytes(); //人脸关联的卡号
	m_struFaceInputParam.byEnableCardReader[0]  = 1;
        m_struFaceInputParam.dwFaceNum = 1;
        m_struFaceInputParam.byFaceID = 1;
        m_struFaceInputParam.write();

	Pointer lpInBuffer = m_struFaceInputParam.getPointer();
	Pointer pUserData = null;
        fRemoteCfgCallBackFaceGet = new FRemoteCfgCallBackFaceGet();

	NativeLong lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_GET_FACE_PARAM_CFG, lpInBuffer, m_struFaceInputParam.size(),fRemoteCfgCallBackFaceGet, pUserData);
        if (lHandle.intValue() < 0)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "建立长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "建立获取卡参数长连接成功!");

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        if(!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "断开长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "断开长连接成功!");
    }

    public class FRemoteCfgCallBackFaceSet implements HCNetSDK.FRemoteConfigCallback
    {
        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData)
        {
            System.out.println("长连接回调获取数据,NET_SDK_CALLBACK_TYPE_STATUS:" + dwType);
            switch (dwType){
		case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.REMOTECONFIGSTATUS_CARD struCfgStatus  = new HCNetSDK.REMOTECONFIGSTATUS_CARD();
                    struCfgStatus.write();
                    Pointer pCfgStatus = struCfgStatus.getPointer();
                    pCfgStatus.write(0, lpBuffer.getByteArray(0, struCfgStatus.size()), 0,struCfgStatus.size());
                    struCfgStatus.read();

                    int iStatus = 0;
                    for(int i=0;i<4;i++)
                    {
                        int ioffset = i*8;
                        int iByte = struCfgStatus.byStatus[i]&0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus){
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            System.out.println("下发人脸参数成功,dwStatus:" + iStatus);
                            break;
                        case 1001:
                            System.out.println("正在下发人脸参数中,dwStatus:" + iStatus);
                            break;
                        case 1002:
                            int iErrorCode = 0;
                            for(int i=0;i<4;i++)
                            {
                                int ioffset = i*8;
                                int iByte = struCfgStatus.byErrorCode[i]&0xff;
                                iErrorCode = iErrorCode + (iByte << ioffset);
                            }
                            System.out.println("下发人脸参数失败, dwStatus:" + iStatus + "错误号:" + iErrorCode);
                            break;
                    }
                    break;
                case 2:// 获取状态数据
			HCNetSDK.NET_DVR_FACE_PARAM_STATUS  m_struFaceStatus = new HCNetSDK.NET_DVR_FACE_PARAM_STATUS();
			m_struFaceStatus.write();
			Pointer pStatusInfo = m_struFaceStatus.getPointer();
			pStatusInfo.write(0, lpBuffer.getByteArray(0, m_struFaceStatus.size()), 0,m_struFaceStatus.size());
			m_struFaceStatus.read();
                        String str = new String(m_struFaceStatus.byCardNo).trim();
			System.out.println("下发人脸数据关联的卡号:" + str + ",人脸读卡器状态:" + 
                                m_struFaceStatus.byCardReaderRecvStatus[0] + ",错误描述:" + new String(m_struFaceStatus.byErrorMsg).trim());
		default:
			break;
		}
        }
    }    

    @Action
    public void jBtnSetFaceCfg() {
        int iErr = 0;
        //设置人脸参数
	HCNetSDK.NET_DVR_FACE_PARAM_COND m_struFaceSetParam = new HCNetSDK.NET_DVR_FACE_PARAM_COND();
	m_struFaceSetParam.dwSize = m_struFaceSetParam.size();
	m_struFaceSetParam.byCardNo = "111011".getBytes(); //人脸关联的卡号
	m_struFaceSetParam.byEnableCardReader[0]  = 1;
        m_struFaceSetParam.dwFaceNum = 1;
        m_struFaceSetParam.byFaceID = 1;
        m_struFaceSetParam.write();

	Pointer lpInBuffer = m_struFaceSetParam.getPointer();

	Pointer pUserData = null;
        fRemoteCfgCallBackFaceSet = new FRemoteCfgCallBackFaceSet();

	NativeLong lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_SET_FACE_PARAM_CFG, lpInBuffer, m_struFaceSetParam.size(), fRemoteCfgCallBackFaceSet, pUserData);
	if (lHandle.intValue() < 0)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "建立长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "建立设置卡参数长连接成功!");

        HCNetSDK.NET_DVR_FACE_PARAM_CFG struFaceInfo = new HCNetSDK.NET_DVR_FACE_PARAM_CFG(); //卡参数
        struFaceInfo.read();
        struFaceInfo.dwSize = struFaceInfo.size();
        struFaceInfo.byCardNo = "111011".getBytes();
        struFaceInfo.byEnableCardReader[0] = 1; //需要下发人脸的读卡器，按数组表示，每位数组表示一个读卡器，数组取值：0-不下发该读卡器，1-下发到该读卡器
        struFaceInfo.byFaceID  =1; //人脸ID编号，有效取值范围：1~2
        struFaceInfo.byFaceDataType  = 1; //人脸数据类型：0- 模板（默认），1- 图片

        /*****************************************
         * 从本地文件里面读取JPEG图片二进制数据
         *****************************************/
        FileInputStream picfile = null;
        int picdataLength = 0;
        try{
                 picfile = new FileInputStream(new File(System.getProperty("user.dir") + "\\face.jpg"));
        }
        catch(FileNotFoundException e)
        {
        	 e.printStackTrace();
        }

        try{
        	picdataLength = picfile.available();
        }
        catch(IOException e1)
        {
        	e1.printStackTrace();
        }
         if(picdataLength < 0)
        {
        	System.out.println("input file dataSize < 0");
        	return;
        }

        HCNetSDK.BYTE_ARRAY ptrpicByte = new HCNetSDK.BYTE_ARRAY(picdataLength);
        try {
        	picfile.read(ptrpicByte.byValue);
        } catch (IOException e2) {
            e2.printStackTrace();
        }
        ptrpicByte.write();
        /**************************/
        
        struFaceInfo.dwFaceLen  = picdataLength;
        struFaceInfo.pFaceBuffer  = ptrpicByte.getPointer();

        struFaceInfo.write();
        Pointer pSendBufSet = struFaceInfo.getPointer();

        //ENUM_ACS_INTELLIGENT_IDENTITY_DATA = 9,  //智能身份识别终端数据类型，下发人脸图片数据
        if(!hCNetSDK.NET_DVR_SendRemoteConfig(lHandle, 0x9, pSendBufSet, struFaceInfo.size()))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "NET_DVR_SendRemoteConfig失败，错误号：" + iErr);
            return;
        }

	try {
		Thread.sleep(5000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        if(!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "断开长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "断开长连接成功!");
    }

    @Action
    public void jBtnDelFace() {
        
        int iErr = 0;
        //删除人脸数据
	HCNetSDK.NET_DVR_FACE_PARAM_CTRL m_struFaceDel = new HCNetSDK.NET_DVR_FACE_PARAM_CTRL();
	m_struFaceDel.dwSize = m_struFaceDel.size();
	m_struFaceDel.byMode = 0; //删除方式：0- 按卡号方式删除，1- 按读卡器删除

        m_struFaceDel.struProcessMode.setType(HCNetSDK.NET_DVR_FACE_PARAM_BYCARD.class);
        m_struFaceDel.struProcessMode.struByCard.byCardNo = "111011".getBytes();//需要删除人脸关联的卡号
        m_struFaceDel.struProcessMode.struByCard.byEnableCardReader[0] = 1; //读卡器
        m_struFaceDel.struProcessMode.struByCard.byFaceID[0] = 1; //人脸ID
        m_struFaceDel.write();

	Pointer lpInBuffer = m_struFaceDel.getPointer();

	boolean lRemoteCtrl = hCNetSDK.NET_DVR_RemoteControl(lUserID, HCNetSDK.NET_DVR_DEL_FACE_PARAM_CFG, lpInBuffer, m_struFaceDel.size());
	if (!lRemoteCtrl)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "NET_DVR_DEL_FACE_PARAM_CFG删除人脸图片失败，错误号：" + iErr);
        }
        else{
            JOptionPane.showMessageDialog(null, "NET_DVR_DEL_FACE_PARAM_CFG成功!");
        }
    }

    @Action
    public void jBtnPlanCfg(){
        int iErr = 0;

        HCNetSDK.NET_DVR_WEEK_PLAN_COND struWeekPlanCond = new HCNetSDK.NET_DVR_WEEK_PLAN_COND();
        struWeekPlanCond.dwSize = struWeekPlanCond.size();
        struWeekPlanCond.dwWeekPlanNumber  = 1;
        struWeekPlanCond.wLocalControllerID = 0;

        HCNetSDK.NET_DVR_WEEK_PLAN_CFG struWeekPlanCfg = new HCNetSDK.NET_DVR_WEEK_PLAN_CFG();

	IntByReference pInt = new IntByReference(0);
        Pointer lpStatusList = pInt.getPointer();

	struWeekPlanCond.write();
	struWeekPlanCfg.write();

        Pointer lpCond = struWeekPlanCond.getPointer();
        Pointer lpInbuferCfg = struWeekPlanCfg.getPointer();

	if (false == hCNetSDK.NET_DVR_GetDeviceConfig(lUserID, HCNetSDK.NET_DVR_GET_CARD_RIGHT_WEEK_PLAN_V50, 1, lpCond, struWeekPlanCond.size(), lpStatusList, lpInbuferCfg, struWeekPlanCfg.size()))
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "NET_DVR_GET_CARD_RIGHT_WEEK_PLAN_V50失败，错误号：" + iErr);
            return;
        }
        struWeekPlanCfg.read();

        for(int i=0;i<7;i++)
        {
            for(int j=0;j<8;j++)
            {
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].byEnable = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].struTimeSegment.struBeginTime.byHour = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].struTimeSegment.struBeginTime.byMinute = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].struTimeSegment.struBeginTime.bySecond = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].struTimeSegment.struEndTime.byHour = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].struTimeSegment.struEndTime.byMinute = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[j].struTimeSegment.struEndTime.bySecond = 0;
            }
        }

        for(int i=0;i<7;i++)
        {
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].byEnable = 1;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].struTimeSegment.struBeginTime.byHour = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].struTimeSegment.struBeginTime.byMinute = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].struTimeSegment.struBeginTime.bySecond = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].struTimeSegment.struEndTime.byHour = 5;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].struTimeSegment.struEndTime.byMinute = 59;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[0].struTimeSegment.struEndTime.bySecond = 59;

            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].byEnable = 1;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].struTimeSegment.struBeginTime.byHour = 6;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].struTimeSegment.struBeginTime.byMinute = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].struTimeSegment.struBeginTime.bySecond = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].struTimeSegment.struEndTime.byHour = 12;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].struTimeSegment.struEndTime.byMinute = 59;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[1].struTimeSegment.struEndTime.bySecond = 59;

            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].byEnable = 1;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].struTimeSegment.struBeginTime.byHour = 13;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].struTimeSegment.struBeginTime.byMinute = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].struTimeSegment.struBeginTime.bySecond = 0;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].struTimeSegment.struEndTime.byHour = 23;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].struTimeSegment.struEndTime.byMinute = 59;
            struWeekPlanCfg.struPlanCfg[i].struPlanCfgDay[2].struTimeSegment.struEndTime.bySecond = 59;
        }
        struWeekPlanCfg.write();

        if (false == hCNetSDK.NET_DVR_SetDeviceConfig(lUserID, HCNetSDK.NET_DVR_SET_CARD_RIGHT_WEEK_PLAN_V50, 1, lpCond, struWeekPlanCond.size(), lpStatusList, lpInbuferCfg, struWeekPlanCfg.size()))
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "NET_DVR_SET_CARD_RIGHT_WEEK_PLAN_V50失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "NET_DVR_SET_CARD_RIGHT_WEEK_PLAN_V50成功！");
    }

    @Action
    public void jBtnDoorCfg() throws UnsupportedEncodingException {
        int iErr = 0;

        HCNetSDK.NET_DVR_DOOR_CFG struDoorCfg = new HCNetSDK.NET_DVR_DOOR_CFG();
	struDoorCfg.write();

        IntByReference iBytesReturned = new IntByReference(0);
	if (false == hCNetSDK.NET_DVR_GetDVRConfig(lUserID, HCNetSDK.NET_DVR_GET_DOOR_CFG, new NativeLong(1), struDoorCfg.getPointer(), struDoorCfg.size(), iBytesReturned))
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "NET_DVR_GET_DOOR_CFG失败，错误号：" + iErr);
            return;
        }
        struDoorCfg.read();
        JOptionPane.showMessageDialog(null, "NET_DVR_GET_DOOR_CFG成功！门1名称为：" + new String(struDoorCfg.byDoorName,"GBK"));

        byte[] strDoorName = "门1_test".getBytes("GBK");
        for (int i = 0; i < HCNetSDK.DOOR_NAME_LEN; i++)
        {
            struDoorCfg.byDoorName[i] = 0;
        }
        for (int i = 0; i <  strDoorName.length; i++)
        {
            struDoorCfg.byDoorName[i] = strDoorName[i];
        }

        struDoorCfg.write();

        if (false == hCNetSDK.NET_DVR_SetDVRConfig(lUserID, HCNetSDK.NET_DVR_SET_DOOR_CFG, new NativeLong(1), struDoorCfg.getPointer(), struDoorCfg.size()))
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "NET_DVR_SET_DOOR_CFG失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "NET_DVR_SET_DOOR_CFG成功！");
    }

    public class FRemoteCfgCallBackFaceCapture implements HCNetSDK.FRemoteConfigCallback
    {
        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData)
        {
             System.out.println("长连接回调获取数据,NET_SDK_CALLBACK_TYPE_STATUS:" + dwType);
            switch (dwType)
            {
		case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.REMOTECONFIGSTATUS_CARD struCfgStatus  = new HCNetSDK.REMOTECONFIGSTATUS_CARD();
                    struCfgStatus.write();
                    Pointer pCfgStatus = struCfgStatus.getPointer();
                    pCfgStatus.write(0, lpBuffer.getByteArray(0, struCfgStatus.size()), 0,struCfgStatus.size());
                    struCfgStatus.read();

                    int iStatus = 0;
                    for(int i=0;i<4;i++)
                    {
                        int ioffset = i*8;
                        int iByte = struCfgStatus.byStatus[i]&0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus){
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            System.out.println("采集人脸信息成功,dwStatus:" + iStatus);
                            break;
                        case 1001:
                            System.out.println("正在采集人脸信息中,dwStatus:" + iStatus);
                            break;
                        case 1002:
                            System.out.println("采集人脸信息失败, dwStatus:" + iStatus);
                            break;
                    }
                    break;
                case 2:// 获取状态数据
			HCNetSDK.NET_DVR_CAPTURE_FACE_CFG struFaceCfg = new HCNetSDK.NET_DVR_CAPTURE_FACE_CFG();
			struFaceCfg.write();
			Pointer pStatusInfo = struFaceCfg.getPointer();
			pStatusInfo.write(0, lpBuffer.getByteArray(0, struFaceCfg.size()), 0, struFaceCfg.size());
			struFaceCfg.read();
			System.out.println("采集进度:" + struFaceCfg.byCaptureProgress + ",人脸图片数据大小:" + struFaceCfg.dwFacePicSize);
                        if((struFaceCfg.byCaptureProgress==100)&&(struFaceCfg.dwFacePicSize>0))
                        {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                fout = new FileOutputStream(newName+"_CaptureFacePic.jpg");
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = struFaceCfg.pFacePicBuffer.getByteBuffer(offset, struFaceCfg.dwFacePicSize);
                                byte [] bytes = new byte[struFaceCfg.dwFacePicSize];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            }catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
		default:
			break;
            }
        }
     }

    @Action
    public void jBtnCaptureFace() {
        int iErr = 0;
        HCNetSDK.NET_DVR_CAPTURE_FACE_COND struCaptureFaceCond = new HCNetSDK.NET_DVR_CAPTURE_FACE_COND();
	struCaptureFaceCond.dwSize = struCaptureFaceCond.size();
        struCaptureFaceCond.write();

	Pointer lpInBuffer = struCaptureFaceCond.getPointer();
        fRemoteCfgCallBackFaceCapture = new FRemoteCfgCallBackFaceCapture();

	NativeLong lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_CAPTURE_FACE_INFO, lpInBuffer, struCaptureFaceCond.size(),fRemoteCfgCallBackFaceCapture, null);
        if (lHandle.intValue() < 0)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "建立长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "建立采集人脸信息长连接成功!");

	try {
		Thread.sleep(9000);
                //这里是写死的等待时间，实际开发需要自己监听FRemoteCfgCallBackFaceCapture回调函数里面返回状态，
                //如果返回成功或者失败则调用NET_DVR_StopRemoteConfig断开长连接
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        if(!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "断开长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "断开长连接成功!");
        
    }

    public class FRemoteCfgCallBackFingerPrint implements HCNetSDK.FRemoteConfigCallback
    {
        public void invoke(int dwType, Pointer lpBuffer, int dwBufLen, Pointer pUserData)
        {
             System.out.println("长连接回调获取数据,NET_SDK_CALLBACK_TYPE_STATUS:" + dwType);
            switch (dwType)
            {
		case 0:// NET_SDK_CALLBACK_TYPE_STATUS
                    HCNetSDK.REMOTECONFIGSTATUS_CARD struCfgStatus  = new HCNetSDK.REMOTECONFIGSTATUS_CARD();
                    struCfgStatus.write();
                    Pointer pCfgStatus = struCfgStatus.getPointer();
                    pCfgStatus.write(0, lpBuffer.getByteArray(0, struCfgStatus.size()), 0,struCfgStatus.size());
                    struCfgStatus.read();

                    int iStatus = 0;
                    for(int i=0;i<4;i++)
                    {
                        int ioffset = i*8;
                        int iByte = struCfgStatus.byStatus[i]&0xff;
                        iStatus = iStatus + (iByte << ioffset);
                    }

                    switch (iStatus){
                        case 1000:// NET_SDK_CALLBACK_STATUS_SUCCESS
                            System.out.println("采集指纹信息成功,dwStatus:" + iStatus);
                            break;
                        case 1001:
                            System.out.println("正在采集指纹信息中,dwStatus:" + iStatus);
                            break;
                        case 1002:
                            System.out.println("采集指纹信息失败, dwStatus:" + iStatus);
                            break;
                    }
                    break;
                case 2:// 获取状态数据
			HCNetSDK.NET_DVR_CAPTURE_FINGERPRINT_CFG struFingerPrintCfg = new HCNetSDK.NET_DVR_CAPTURE_FINGERPRINT_CFG();
			struFingerPrintCfg.write();
			Pointer pStatusInfo = struFingerPrintCfg.getPointer();
			pStatusInfo.write(0, lpBuffer.getByteArray(0, struFingerPrintCfg.size()), 0, struFingerPrintCfg.size());
			struFingerPrintCfg.read();
			System.out.println("指纹编号:" + struFingerPrintCfg.byFingerNo + ",指纹质量:" + struFingerPrintCfg.byFingerPrintQuality );
                        if((struFingerPrintCfg.byFingerData != null)&&(struFingerPrintCfg.dwFingerPrintDataSize >0)) //指纹数据
                        {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                fout = new FileOutputStream(newName+"_FingerPrint.data");
                                //将字节写入文件
                                fout.write(struFingerPrintCfg.byFingerData);
                                fout.close();
                            }catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        if((struFingerPrintCfg.pFingerPrintPicBuffer != null)&&(struFingerPrintCfg.dwFingerPrintPicSize >0)) //指纹图片
                        {
                            SimpleDateFormat sf = new SimpleDateFormat("yyyyMMddHHmmss");
                            String newName = sf.format(new Date());
                            FileOutputStream fout;
                            try {
                                fout = new FileOutputStream(newName+"_CaptureFacePic.jpg");
                                //将字节写入文件
                                long offset = 0;
                                ByteBuffer buffers = struFingerPrintCfg.pFingerPrintPicBuffer.getByteBuffer(offset, struFingerPrintCfg.dwFingerPrintPicSize);
                                byte [] bytes = new byte[struFingerPrintCfg.dwFingerPrintPicSize];
                                buffers.rewind();
                                buffers.get(bytes);
                                fout.write(bytes);
                                fout.close();
                            }catch (FileNotFoundException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                        }
                        break;
		default:
			break;
            }
        }
     }

    @Action
    public void jBtnFingerPrint() {
        int iErr = 0;
        HCNetSDK.NET_DVR_CAPTURE_FINGERPRINT_COND struFingerCond = new HCNetSDK.NET_DVR_CAPTURE_FINGERPRINT_COND();
	struFingerCond.dwSize = struFingerCond.size();
        struFingerCond.byFingerNo = 1; //手指编号，取值范围：1~10
        struFingerCond.write();

	Pointer lpInBuffer = struFingerCond.getPointer();
        fRemoteCfgCallBackFingerPrint = new FRemoteCfgCallBackFingerPrint();

	NativeLong lHandle = hCNetSDK.NET_DVR_StartRemoteConfig(lUserID, HCNetSDK.NET_DVR_CAPTURE_FINGERPRINT_INFO, lpInBuffer, struFingerCond.size(),fRemoteCfgCallBackFingerPrint, null);
        if (lHandle.intValue() < 0)
	{
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "建立长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "建立采集人脸信息长连接成功!");

	try {
		Thread.sleep(9000);
                //这里是写死的等待时间，实际开发需要自己监听FRemoteCfgCallBackFingerPrint回调函数里面返回状态，
                //如果返回成功或者失败则调用NET_DVR_StopRemoteConfig断开长连接
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}

        if(!hCNetSDK.NET_DVR_StopRemoteConfig(lHandle))
        {
            iErr = hCNetSDK.NET_DVR_GetLastError();
            JOptionPane.showMessageDialog(null, "断开长连接失败，错误号：" + iErr);
            return;
        }
        JOptionPane.showMessageDialog(null, "断开长连接成功!");
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jBtnAlarm;
    private javax.swing.JButton jBtnCaptureFace;
    private javax.swing.JButton jBtnCloseAlarm;
    private javax.swing.JButton jBtnDelFace;
    private javax.swing.JButton jBtnDoorCfg;
    private javax.swing.JButton jBtnFingerPrint;
    private javax.swing.JButton jBtnPlanCfg;
    private javax.swing.JButton jBtnSetFaceCfg;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButtonListen;
    private javax.swing.JButton jButtonLogin;
    private javax.swing.JButton jButtonStopListen;
    private javax.swing.JButton jButtonTest;
    private javax.swing.JLabel jLabelIPAddress;
    private javax.swing.JLabel jLabelIPAddress1;
    private javax.swing.JLabel jLabelPassWord;
    private javax.swing.JLabel jLabelPortNumber;
    private javax.swing.JLabel jLabelPortNumber1;
    private javax.swing.JLabel jLabelUserName;
    private javax.swing.JButton jLogOut;
    private javax.swing.JPasswordField jPasswordFieldPassword;
    private javax.swing.JScrollPane jScrollPanelAlarmList;
    private javax.swing.JTable jTableAlarm;
    private javax.swing.JTextField jTextFieldIPAddress;
    private javax.swing.JTextField jTextFieldListenIP;
    private javax.swing.JTextField jTextFieldListenPort;
    private javax.swing.JTextField jTextFieldPortNumber;
    private javax.swing.JTextField jTextFieldUserName;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
}


