package com.wulin.jvm.study.chapter7.section7_1;

/**
 * 被动使用类字段演示一:
 * 通过类引用父类的静态字段,不会导致子类初始化
 * @author ThinkPad
 *
 */
public class SuperClass {
	static {
		System.out.println("SuperClass init!");
	}
	
	public static int value = 123;

}
