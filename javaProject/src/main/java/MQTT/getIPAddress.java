package MQTT;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class getIPAddress {
	public static byte byMagneticStatus[] = new byte[32];
	
	
	
    public static void main(String[] args) {
    	try {
    		InetAddress address = getLocalHostIp();
    		System.err.println(address);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
    
    /**
     * 获取本机所有IP地址
     * @return
     * @throws SocketException
     */
    public static InetAddress getLocalHostIp() throws SocketException{
        Enumeration<?> allNetInterfaces=NetworkInterface.getNetworkInterfaces();
        InetAddress ip=null;
        while(allNetInterfaces.hasMoreElements()){
            NetworkInterface netInterface=(NetworkInterface) allNetInterfaces.nextElement();
            //System.out.println(netInterface.getName());
            Enumeration<?> addresses=netInterface.getInetAddresses();
            while(addresses.hasMoreElements()){
                ip=(InetAddress) addresses.nextElement();
                if(ip!=null && ip instanceof Inet4Address){
                    System.out.println("本机的ip="+ip.getHostAddress());
                    break;
                }
            }
        }
        return ip;
    }
    
}