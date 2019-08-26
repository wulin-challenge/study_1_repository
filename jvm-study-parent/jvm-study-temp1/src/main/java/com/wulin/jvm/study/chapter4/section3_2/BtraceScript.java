package com.wulin.jvm.study.chapter4.section3_2;

import com.sun.btrace.annotations.Kind;
import com.sun.btrace.annotations.Location;
import com.sun.btrace.annotations.OnMethod;
import com.sun.btrace.annotations.Return;
import com.sun.btrace.annotations.Self;

/**
 * Btrace 脚本
 * @author wulin
 *
 */
public class BtraceScript {

	@OnMethod(clazz="com.wulin.jvm.study.chapter4.section3_2.VisualVMOFBTraceTest",
			  method="add",
			  location=@Location(Kind.RETURN))
	public static void func(@Self com.wulin.jvm.study.chapter4.section3_2.VisualVMOFBTraceTest instance,int a,int b,@Return int result) {
		println("调用堆栈:");
		jstack();
		println(strcat("方法参数A:",str(a)));
		println(strcat("方法参数B:",str(b)));
		println(strcat("方法结果:",str(result)));
	}

	private static String strcat(String string, Object str) {
		return null;
	}

	private static Object str(int b) {
		return null;
	}

	private static void jstack() {
		
	}
	private static void println(String string) {
		
	}
	
}
