package org.wulin.jvm.jdk.collections;

import java.util.concurrent.ConcurrentHashMap;

import org.junit.Test;

public class ConcurrentHashMapStudy {
	
	private ConcurrentHashMap<String,String> map = new ConcurrentHashMap<String,String>();
	
	@Test
	public void putTest() {
		new ConcurrentHashMap<String,String>();
		map.put("11", "aa");
		map.put("11", "cc");
		map.put("222", "b");
		map.put("3", "c");
	}
	
	@Test
	public void getTest() {
		putTest();
		String val = map.get("2");
		System.out.println(val);
	}

}
