package org.wulin.jvm.jdk.sort.test;

import org.junit.Test;
import org.wulin.jvm.jdk.sort.Shell;

public class ShellTest {
	
	private Integer[] numbers = new Integer[] {5,1,4,3,2,10,8,9,6,7,12,11};
	
	@Test
	public void testShellSort() {
		Shell<Integer> sorter = new Shell<>();
		sorter.sort(numbers);
		System.out.println();
	}

}
