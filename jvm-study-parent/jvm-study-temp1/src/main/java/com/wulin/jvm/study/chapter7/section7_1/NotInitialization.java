package com.wulin.jvm.study.chapter7.section7_1;

/**
 * 非主动使用类字段演示
 * @author wulin
 *
 */
public class NotInitialization {
	public static void main(String[] args) {
		System.out.println(SubClass.value);
	}

}
