package org.wulin.jvm.jdk.sort.test;

import org.junit.Test;
import org.wulin.jvm.jdk.sort.Up2DownMergeSort;

public class Up2DownMergeSortTest {

//	private Integer[] numbers = new Integer[] {5,1,4,3,2,10,8,9,6,7,12,11};
	private Integer[] numbers = new Integer[] {5,1,4};
	
	@Test
	public void testUp2DownMergeSort() {
		Up2DownMergeSort<Integer> sorter = new Up2DownMergeSort<>();
		sorter.sort(numbers);
		System.out.println();
	}
}
