package com.jdk8NewTimeGet;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class NewTimeGet {

	public static void main(String[] args) {
		
		/**获取时间转字符串*/
		DateTimeFormatter[] formatters = new DateTimeFormatter[]{
		// 直接使用常量创建DateTimeFormatter格式器
        DateTimeFormatter.ISO_LOCAL_DATE,
        DateTimeFormatter.ISO_LOCAL_TIME,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        // 使用本地化的不同风格来创建DateTimeFormatter格式器
        DateTimeFormatter.ofLocalizedDateTime(FormatStyle.FULL, FormatStyle.MEDIUM),
        DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG),
        
        // 根据模式字符串来创建DateTimeFormatter格式器
//        DateTimeFormatter.ofPattern("Gyyyy%%MMM%%dd HH:mm:ss")// 这个最常用，G代表公元Y MMM代表中文月份
        DateTimeFormatter.ofPattern("Y-MM-dd HH:mm:ss")//年份前面加 G 代表公元   一个Y 或 四个 y：年,MMM代表中文月份
		};
		
		 //获取时间
		OffsetDateTime date1 = Instant.now().atOffset(ZoneOffset.ofHours(8));//获取北京时间
		
		//获取时间
		LocalDateTime date = LocalDateTime.now();
		//获取时间加转换
		String date3 = LocalDateTime.now().format(formatters[5]);
		System.out.println(date3);
		
		//转换时间
		for (int i = 0; i < formatters.length; i++) {
			System.out.println(date.format(formatters[i]));
		    System.out.println(formatters[i].format(date));
		    System.out.println(i + "----------------------------------");
		}
		
		
		/**字符串解析为时间*/
		// 定义一个任意格式的日期时间字符串
		String str1 = "2014==04==12 01时06分09秒";
		// 根据需要解析的日期、时间字符串定义解析所用的格式器
		DateTimeFormatter fomatter1 = DateTimeFormatter
		    .ofPattern("yyyy==MM==dd HH时mm分ss秒");
		// 执行解析
		LocalDateTime dt1 = LocalDateTime.parse(str1, fomatter1);
		System.out.println(dt1); // 输出 2014-04-12T01:06:09
		// ---下面代码再次解析另一个字符串---
		String str2 = "2014$$$四月$$$13 20小时";
		DateTimeFormatter fomatter2 = DateTimeFormatter
		    .ofPattern("yyy$$$MMM$$$dd HH小时");
		LocalDateTime dt2 = LocalDateTime.parse(str2, fomatter2);
		System.out.println(dt2); // 输出 2014-04-13T20:00
		
		
	}
}
