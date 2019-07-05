package com.file;

import org.apache.commons.lang.ArrayUtils;

//移除数组元素
public class removeArrayElement {

	public static void main(String[] args) {
		remove();
	}

	public static void remove() {
		String[] str = { "aaa", "bbb", "ccc", "ddd", "eee" };

		String a = "";
		for (int i = 0; i < str.length; i++) {
			a = a + "," + str[i];
		}
		//System.out.println(a);

		
		Object[] remove = ArrayUtils.remove(str, 0);
//		String b = "";
//		for (int i = 0; i < str.length; i++) {
//			b = b + "," + str[i];
//		}
//		System.out.println(b);
for (Object object : remove) {
	System.out.println(object);
}
	
	}

}
