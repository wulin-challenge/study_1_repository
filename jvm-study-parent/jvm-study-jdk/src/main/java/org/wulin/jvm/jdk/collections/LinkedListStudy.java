package org.wulin.jvm.jdk.collections;

import java.util.LinkedList;

import org.junit.Test;

import junit.framework.Assert;

public class LinkedListStudy {
	
	private LinkedList<String> list = new LinkedList<String>();
	
	@Test
	public void addTest() {
		list.add("1");
		list.add("2");
		list.add("3");
		list.add("4");
		list.add("5");
		
	}
	
	@Test
	public void getTest() {
		addTest();
		
		String node = list.get(2);
		Assert.assertEquals("3", node);
	}

}
