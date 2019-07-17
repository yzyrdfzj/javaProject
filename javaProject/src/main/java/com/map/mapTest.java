package com.map;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class mapTest {

	public static void main(String[] args) {
		Map<Integer,List<Integer>> map = new HashMap<Integer, List<Integer>>();
		List<Integer> list3 = new ArrayList<>();
		list3.add(125);
		list3.add(126);
		list3.add(127);
		List<Integer> list5 = new ArrayList<>();
		list5.add(201);
		list5.add(202);
		list5.add(203);
		
		map.put(3, list3);
		map.put(5, list5);
		
		
		
		if(map.containsValue(list3.contains(125))) {
			System.out.println("进入判断");
		} else {
			System.out.println("未进入判断");
		}
		
		
	}
}
