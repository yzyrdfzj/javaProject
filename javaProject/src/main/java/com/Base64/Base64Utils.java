package com.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

//import com.steward.utils.StringUtil;
 
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
 
@SuppressWarnings("restriction")
public class Base64Utils {
 
	public static void main(String[] args) throws Exception {
		
		FileInputStream fileInputStream = new FileInputStream("D:\\Desktop\\base64.txt");
		byte[] buf = new byte[1024];  
		int length = 0;
		//循环读取文件内容，输入流中将最多buf.length个字节的数据读入一个buf数组中,返回类型是读取到的字节数。
		//当文件读取到结尾时返回 -1,循环结束。
		String s = "";
		while((length = fileInputStream.read(buf)) != -1){
			String a = new String(buf,0,length);
			s= s + a;
		}
		System.out.println(s);
		//最后记得，关闭流
		fileInputStream.close();
		
		String strs = "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";
		//本地图片地址
		String url = "D:\\Desktop\\picture\\7bc2d5628535e5dd1277c3eb7bc6a7efce1b62af.jpg";
		//在线图片地址
//		String string = "http://bpic.588ku.com//element_origin_min_pic/17/03/03/7bf4480888f35addcf2ce942701c728a.jpg";
		
//		String str = Base64Utils.ImageToBase64ByLocal(url);//本地图片转换成base64字符串
//		System.out.println(str);
		
//		String ste = Base64Utils.ImageToBase64ByOnline(string);//在线图片转换成base64字符串
		
		
//		Base64Utils.Base64ToImage(s,"D:\\Desktop\\test1.jpg");//base64字符串转换成图片
		Base64Utils.Base64ToImage(strs,"D:\\Desktop\\test1.jpg");//base64字符串转换成图片
		
	}
 
	/**
	 * 本地图片转换成base64字符串
	 * @param imgFile	图片本地路径
	 * @return
	 *
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:40:46
	 */
	public static String ImageToBase64ByLocal(String imgFile) {// 将图片文件转化为字节数组字符串，并对其进行Base64编码处理
 
 
		InputStream in = null;
		byte[] data = null;
 
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFile);
			
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
 
		return encoder.encode(data);// 返回Base64编码过的字节数组字符串
	}
 
	
	
	/**
	 * 在线图片转换成base64字符串
	 * 
	 * @param imgURL	图片线上路径
	 * @return
	 *
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:43:18
	 */
	public static String ImageToBase64ByOnline(String imgURL) {
		ByteArrayOutputStream data = new ByteArrayOutputStream();
		try {
			// 创建URL
			URL url = new URL(imgURL);
			byte[] by = new byte[1024];
			// 创建链接
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(5000);
			InputStream is = conn.getInputStream();
			// 将内容读取内存中
			int len = -1;
			while ((len = is.read(by)) != -1) {
				data.write(by, 0, len);
			}
			// 关闭流
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		return encoder.encode(data.toByteArray());
	}
	
	
	/**
	 * base64字符串转换成图片
	 * @param imgStr		base64字符串
	 * @param imgFilePath	图片存放路径
	 * @return
	 *
	 * @author ZHANGJL
	 * @dateTime 2018-02-23 14:42:17
	 */
	public static boolean Base64ToImage(String imgStr,String imgFilePath) { // 对字节数组字符串进行Base64解码并生成图片
 
		if (imgStr.isEmpty()) // 图像数据为空
			return false;
 
		BASE64Decoder decoder = new BASE64Decoder();
		try {
			// Base64解码
			byte[] b = decoder.decodeBuffer(imgStr);
			for (int i = 0; i < b.length; ++i) {
				if (b[i] < 0) {// 调整异常数据
					b[i] += 256;
				}
			}
//			SimpleDateFormat sdf = new SimpleDateFormat("YMMddHHmmss");
//			Date d = new Date();
//			String date = sdf.format(d);
//			String pictureName = uuid(24);
			
//			File f = new File(imgFilePath);
//			imgFilePath = imgFilePath + "\\" +date + pictureName + ".jpg";
//			if(!f.exists()) {
//				f.mkdir();
//			}
 
			OutputStream out = new FileOutputStream(imgFilePath);
			out.write(b);
			out.flush();
			out.close();
 
			return true;
		} catch (Exception e) {
			System.err.println(e);
			return false;
		}
 
	}
	
	/**
	 * 产生随机字符串
	 * @param length
	 * @return
	 */
	public static String uuid(int length) {
		 //产生随机数
	    Random random=new Random();
	    StringBuffer sb=new StringBuffer();
	    //循环length次
	    for(int i=0; i<length; i++){
	      //产生0-2个随机数，既与a-z，A-Z，0-9三种可能
	      int number=random.nextInt(3);
	      long result=0;
	      switch(number){
	      //如果number产生的是数字0；
	      case 0:
	        //产生A-Z的ASCII码
	        result=Math.round(Math.random()*25+65);
	        //将ASCII码转换成字符
	        sb.append(String.valueOf((char)result));
	      break;
	      case 1:
	          //产生a-z的ASCII码
		      result=Math.round(Math.random()*25+97);
		      sb.append(String.valueOf((char)result));
	        break;
	        case 2:
	          //产生0-9的数字
	          sb.append(String.valueOf(new Random().nextInt(10)));
	        break; 
	      }
	    }
	    return sb.toString();
	  }
 
	
}