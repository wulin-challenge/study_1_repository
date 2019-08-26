package com.wulin.jvm.study.temp2;

/**
 * VM 参数: -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 
 * -XX:PretenureSizeThreshold=3145728 -XX:MaxTenuringThreshold=15 -XX:+UseParNewGC
 * 
 * <p> -XX:MaxTenuringThreshold 设置对象从年轻代 晋升为老年代的年龄,本次测试值为 1 和 15.
 * <p> -XX:+UseParNewGC : 打开次开关后,使用ParNew + Serial Old 的收集器组合进行内存回收
 * 
 * @author wulin
 *
 */
public class TenuringThresholdTest {
	
	private static final int _1MB = 1024 * 1024;
	
	public static void main(String[] args) {
		testTenuringThreshold();
	}

	public static void testTenuringThreshold() {
		byte[] allocation1,allocation2,allocation3;
		
		allocation1 = new byte[_1MB /4];
		//什么时候进入老年代取决于 XX:MaxTenuringThreshold 设置
		allocation2 = new byte[4 * _1MB];
		allocation3 = new byte[4 * _1MB];
		allocation3 = null;
		allocation3 = new byte[4 * _1MB];
	}
}
