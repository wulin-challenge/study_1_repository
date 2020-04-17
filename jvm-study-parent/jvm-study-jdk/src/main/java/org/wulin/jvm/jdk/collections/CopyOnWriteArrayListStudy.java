package org.wulin.jvm.jdk.collections;

import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;

public class CopyOnWriteArrayListStudy {
	
	private CopyOnWriteArrayList<Integer> list = new CopyOnWriteArrayList<>();

	@Test
	public void addTest() {
		list.add(1);
		list.add(1);
		
		Integer integer = list.get(0);
		System.out.println(integer);
	}
}
