package org.wulin.jvm.jdk.sort.test;

import org.junit.Test;
import org.wulin.jvm.jdk.sort.Bubble;

public class BubbleTest {
	
	private Integer[] numbers = new Integer[] {5,1,4,3,2};
	
	@Test
	public void testBubbleSort() {
		Bubble<Integer> sorter = new Bubble<>();
		sorter.sort(numbers);
		System.out.println("1--->"+numbers);
	}
	

}
