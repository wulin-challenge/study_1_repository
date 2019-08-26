package com.wulin.jvm.study.temp1;

import java.lang.reflect.Field;

/**
 * VM Args: -Xmx20M -XX:MaxDirectMemorySize=10M
 * @author wulin
 *
 */
public class DirectMemoryOOM {
	
	private static final int _1MB = 1024 * 1024;
	
	public static void main(String[] args) throws IllegalArgumentException, IllegalAccessException {
		Field unsafeField = sun.misc.Unsafe.class.getDeclaredFields()[0];
		
		unsafeField.setAccessible(true);
		sun.misc.Unsafe unsafe = (sun.misc.Unsafe)unsafeField.get(null);
		
		while(true) {
			unsafe.allocateMemory(_1MB);
		}
	}

}
