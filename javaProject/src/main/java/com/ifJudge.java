package com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ifJudge {
	public static void main(String[] args) {
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
        List<Integer> list3 = new ArrayList<Integer>();//操作
        list3.add(1027);
        list3.add(1026);
        list3.add(1025);
        list3.add(1024);
        map.put(3, list3);
        List<Integer> list5 = new ArrayList<Integer>();//事件
        //开关门
        list5.add(22);
        list5.add(20);
        list5.add(31);
        list5.add(21);
        list5.add(32);
        list5.add(19);
        list5.add(92);
        list5.add(93);
        //刷脸
        list5.add(112);
        list5.add(105);
        list5.add(75);
        list5.add(76);
        //读卡器
        list5.add(6);
        list5.add(7);
        list5.add(8);
        list5.add(9);
        map.put(5, list5);
        
        int a = 5;
        int b = 112;
//        if( (map.containsKey(3)&&list3.contains(b)) || (map.containsKey(5)&&list5.contains(b)) ) {
//        	System.err.println("进入括号里了");
//        }
        
        List<Integer> list = map.get(a);
        for (Integer intr : list) {
			if(intr == b ) {
				System.err.println("进入括号里了");
			}
		}
        
//        if() {
//        
//        }
	}

}
