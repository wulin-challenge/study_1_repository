package com.wulin.jvm.study.temp1;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试内存溢出,并将内存溢出输出到文件中,然后使用 eclipse memory analysis 工具进行分析,vm args 配置如下: 
 * -Xms20m -Xmx20m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=F:/resources/temp/temp9/jvm
 */
public class HeapOOM {
	
	/**
	 * 创建一个空的内部类,用于创建测试对象
	 * @author wulin
	 *
	 */
	static class OOMObject{}
	
	public static void main(String[] args) {
		List<OOMObject> list = new ArrayList<OOMObject>();
		
		while(true) {
			list.add(new OOMObject());
		}
	}
	
}
