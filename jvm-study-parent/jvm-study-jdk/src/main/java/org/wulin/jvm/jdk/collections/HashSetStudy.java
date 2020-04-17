package org.wulin.jvm.jdk.collections;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class HashSetStudy {

	@Test
	public void hashSetTest() {
		Set<String> set = new HashSet<String>();
		set.add("abc");
		set.add("def");
		System.out.println();
	}
}
