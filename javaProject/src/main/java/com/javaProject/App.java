package com.javaProject;

import java.util.Iterator;
import java.util.Set;

import com.alibaba.fastjson.*;

/**
 * Hello world!
 *
 */
public class App 
{
    @SuppressWarnings("static-access")
	public static void main( String[] args ){
    	
    	RunnableTest rat = new RunnableTest();
    	RunnableTest2 rat2 = new RunnableTest2();
    	//在方法内写 Rannable ra = new Rannable 必须先启动方法，再启动线程
    	//写在内部的好处是每个方法都可以有属于自己的线程，则两个方法可以同时执行
    	rat.printLn();
    	new Thread(rat.ra).start();
    	rat.printLn2();
    	new Thread(rat.ra).start();
    	
    	new Thread(rat2).start();
    	
    	
    	
    	
    	
    	
    	
    	// json串(以自己的为准)
    	String str = "[{id:58},{id:25},{name:\"张山\"}]";
    	String st = "{\"0\":{\"codeId\":\"一\",\"codeName\":\"任务一\"},\"1\":{\"codeId\":\"二\",\"codeName\":\"任务二\"}}";
    	JSONArray js = new JSONArray();
    	js.add("50");
    	js.add("100");
    	JSONObject jo = new JSONObject();
    	jo.put("name", "loi");
    	js.add(jo);
    	
    	System.out.println("jsonObj: "+jo);
    	System.out.println("jsonArr: "+js);
    	
    	//str转JSONArray 并取出所有的键与值
    		JSONArray jsonArr = JSONArray.parseArray(str);
    		Iterator<Object> it = jsonArr.iterator();
    		while(it.hasNext()) {
    			JSONObject job = (JSONObject) it.next();
    			Set<String> keySet = job.keySet();
    			for (String key : keySet) {
    				System.out.println("key： " + key);
//    				JSONObject ss = job.getJSONObject(key);
    				Object value = job.get(key);
    				System.out.println("value: "+value);
        		}
    			
    		}
    		System.out.println();
    		//str转JSONObject
    		JSONObject jsonObject = JSONObject.parseObject(st);  //jsonObj 的值可以是json类型
    		//keySet()方法获取key的Set集合
    		Set<String> keySet = jsonObject.keySet();
    		//对Set集合遍历
    		for (String key : keySet) {
    		    //打印出jsonObject中的子元素
    			System.out.println("key="+key);
    		    System.out.println("value="+jsonObject.getJSONObject(key));
    		}
    			


    			
    }
}
