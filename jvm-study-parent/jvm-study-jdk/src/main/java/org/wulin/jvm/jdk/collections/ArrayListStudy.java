package org.wulin.jvm.jdk.collections;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ArrayListStudy {
	ArrayList<Integer> list = new ArrayList<Integer>();
	
	@Test
	public void addTest() {
		list.add(1);
		list.add(2);
		list.add(3);
		list.add(4);
		list.add(5);
		list.add(6);
		list.add(7);
		list.add(8);
		list.add(9);
		list.add(10);
		list.add(11);
		list.add(12);
		
		list.add(6, 15);
	}
	
	@Test
	public void getTest() {
		addTest();
		
		Integer integer = list.get(5);
		Assert.assertEquals(5d, (double)integer,10D);
	}
	
	@Test
	public void listToArray() {
		List<Long> list = new ArrayList<>();
		list.add(1L);
		
		Long[] array = list.toArray(new Long[] {});
		System.out.println(array);
	}
}
