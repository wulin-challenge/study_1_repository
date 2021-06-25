package org.wulin.jvm.jdk.collections;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

public class Jdk8StreamTest {
	
	@Test
	public void flatStreamTest() {
		
		List<String> words = Arrays.asList("Hello", "World");
		List<String> chars = 
		words.stream().map(word -> word.split("")).flatMap(Arrays::stream).distinct().collect(Collectors.toList());
//		 
//		
//		List<String> charList = words
//				.stream()
//				.map(word -> word.split(""))
//				.flatMap(Arrays::stream).distinct()
//				.collect(Collectors.toList());
		
		
	}
	
	@Test
	public void findAnyTest() {
		List<Integer> num = Arrays.asList(1, 2, 4, 5, 6);
		num.parallelStream()
		.filter(n -> n > 2)
		.findAny()
		//.findFirst()
		.ifPresent(System.out::println);
	}

}
