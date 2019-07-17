package com;

public class finalTest {

	public static void main(String[] args) {
		print();
	}
	
	public static void print() {
		
		for (int i = 0; i < 10; i++) {
			final String id = String.valueOf(i);
			System.out.println(id);
		}
	}
}
