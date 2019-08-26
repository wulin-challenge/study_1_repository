package org.wulin.jvm.jdk.collections;

import java.util.HashMap;

import org.junit.Test;

public class HashMapStudy {
	HashMap<String,Object> stringMap = new HashMap<String,Object>();
	
	@Test
	public void keyHashTest() {
		String key = "1";
		int h = h = key.hashCode();
//		int k = h >>> 16;
		int k = 50;
		int j = h ^ k;
		System.out.println(j);
		
		int a = 15;
		int b = 49;
		
		int c = a & b;
		int c1 = a & 15;
		int c2 = a & 1;
		int c3 = a & 0;
		int c4 = 1 & 0;
		int c5 = 1 & 15;
		int c6 = 3 & 15;
		int c7 = 4 & 15;
		int c8 = 5 & 15;
		System.out.println(c);
	}
	
	@Test
	public void putTest() {
		stringMap.put("1", 1);
		stringMap.put("2", 2);
		
		//Aa,BB 的hash值是一样的
		
		stringMap.put("Aa", 33);
		stringMap.put("BB", 44);
		
		stringMap.put("3", 3);
		stringMap.put("4", 4);
		stringMap.put("5", 5);
		stringMap.put("6", 6);
		stringMap.put("7", 7);
		stringMap.put("8", 8);
		stringMap.put("9", 9);
		stringMap.put("10", 10);
		stringMap.put("11", 11);
		stringMap.put("12", 12);
		stringMap.put("13", 13);
		stringMap.put("14", 14);
		stringMap.put("15", 15);
		stringMap.put("16", 16);
	}
	
	@Test
	public void getTest() {
		putTest();
		
		Object object = stringMap.get("BB");
		System.out.println(object);
	}
	
}
