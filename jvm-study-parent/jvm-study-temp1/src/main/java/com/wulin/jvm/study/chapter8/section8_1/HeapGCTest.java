package com.wulin.jvm.study.chapter8.section8_1;

/**
 * vm 参数: -verbose:gc
 * @author wulinThinkPad
 *
 */
public class HeapGCTest {
	
	public static void main(String[] args) {
		// 添加了 { ... } 作用域范围就可以实现gc的回收,没有则不行
//		{
//		byte[] placeholder = new byte[1024 * 1024 * 64];
//		}
		byte[] placeholder = new byte[1024 * 1024 * 64];
		//帮助gc,更常用的写法是 placeholder = null;这种写法更能保证在没有 {...}作用域括号的情况下也能正确回收
		
		int i = 0;
		System.gc();
	}

}
