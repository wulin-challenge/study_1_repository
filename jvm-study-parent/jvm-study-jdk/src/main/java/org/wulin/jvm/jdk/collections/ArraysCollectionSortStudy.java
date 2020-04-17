package org.wulin.jvm.jdk.collections;

import java.util.Arrays;

import org.junit.Test;

/**
 * Arrays.sort 和 Collections.sort 实现原理和区别？
 * @author wulin
 *
 */
public class ArraysCollectionSortStudy {
	private int[] arrays = new int[] {1,3,2};

	@Test
	public void arraysSortTest() {
		Arrays.sort(arrays);
		
		System.out.println(arrays);
	}
}
