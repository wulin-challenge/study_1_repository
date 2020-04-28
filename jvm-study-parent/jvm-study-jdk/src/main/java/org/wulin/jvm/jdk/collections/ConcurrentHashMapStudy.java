package org.wulin.jvm.jdk.collections;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class ConcurrentHashMapStudy {
	
	private ConcurrentHashMap<String,Object> stringMap = new ConcurrentHashMap<String,Object>();
	

	@Test
	public void putTest() {
		new ConcurrentHashMap<String,String>();
		stringMap.put("11", "aa");
		stringMap.put("11", "cc");
		stringMap.put("222", "b");
		stringMap.put("3", "c");
	}
	
	/**
	 * 扩容测试
	 */
	@Test
	public void putCapacityExpansionTest() {
		stringMap.put("1", 1);
		stringMap.put("2", 2);
		
		//Aa,BB,11 的hash值是一样的
		
		stringMap.put("Aa", 3);
		stringMap.put("BB", 4);
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
		stringMap.put("17", 17);
		stringMap.put("18", 18);
		stringMap.put("19", 19);
	}
	
	@Test
	public void getTest() {
		putTest();
		Object val = stringMap.get("2");
		System.out.println(val);
	}

}
