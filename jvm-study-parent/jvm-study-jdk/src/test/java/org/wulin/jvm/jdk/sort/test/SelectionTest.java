package org.wulin.jvm.jdk.sort.test;

import org.junit.Test;
import org.wulin.jvm.jdk.sort.Selection;

public class SelectionTest {
	private Integer[] numbers = new Integer[] {5,1,4,3,2};
	
	@Test
	public void testSelectionSort() {
		Selection<Integer> sorter = new Selection<>();
		sorter.sort(numbers);
		System.out.println();
	}

}
