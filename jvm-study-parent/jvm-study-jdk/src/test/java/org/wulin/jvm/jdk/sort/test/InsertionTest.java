package org.wulin.jvm.jdk.sort.test;

import org.junit.Test;
import org.wulin.jvm.jdk.sort.Insertion;

public class InsertionTest {
	private Integer[] numbers = new Integer[] {5,1,4,3,2};
	
	@Test
	public void testInsertionSort() {
		Insertion<Integer> sorter = new Insertion<>();
		sorter.sort(numbers);
		System.out.println();
		
	}

}
