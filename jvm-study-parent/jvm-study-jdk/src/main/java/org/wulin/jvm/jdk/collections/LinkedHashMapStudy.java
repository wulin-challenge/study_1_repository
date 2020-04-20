package org.wulin.jvm.jdk.collections;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

public class LinkedHashMapStudy {
	private Map<String,Object> map = new LinkedHashMap<String,Object>();
	
	@Test
	public void put() {
		map.put("11", "11a");
		map.put("22", "22a");
		map.put("33", "33a");
	}
}
