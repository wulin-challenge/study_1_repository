package com.wulin.jvm.study.temp1;

/**
 * 测试 虚拟机栈和本地方法栈OOM测试,vm args 配置如下: 
 * -Xss128K
 * @author wulin
 *
 */
public class JavaVMStackSOF {

	private int stackLength = 1;
	
	public void stackLeak() {
		stackLength ++;
		stackLeak();
	}
	
	public static void main(String[] args) {
		JavaVMStackSOF oom = new JavaVMStackSOF();
		
		try {
			oom.stackLeak();
		} catch (Throwable e) {
			System.out.println("stack length: "+oom.stackLength);
			throw e;
		}
		
		
	}
}
