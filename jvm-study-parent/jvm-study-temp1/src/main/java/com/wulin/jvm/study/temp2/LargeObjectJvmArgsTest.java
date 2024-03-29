package com.wulin.jvm.study.temp2;

/**
 * 测试jvm参数:
 * <p>
 * VM 参数: -verbose:gc -Xms20M -Xmx20M -Xmn10M -XX:+PrintGCDetails -XX:SurvivorRatio=8 
 * -XX:PretenureSizeThreshold=3145728 -XX:+UseParNewGC
 * 
 * <p> 详解:
 * <p> -verbose:gc: 中参数-verbose:gc 表示输出虚拟机中GC的详细情况.
 * <pre>
 * 使用后输出如下:
 *	[Full GC 168K->97K(1984K)， 0.0253873 secs]
 *	
 *	 解读如下:
 *　　箭头前后的数据168K和97K分别表示垃圾收集GC前后所有存活对象使用的内存容量，说明有168K-97K=71K的对象容量被回收，
 *   括号内的数据1984K为堆内存的总容量，收集所需要的时间是0.0253873秒（这个时间在每次执行的时候会有所不同）
 * </pre>
 * 
 * <p> -XX:PretenureSizeThreshold=3145728 : 令大于这个设置值的对象直接在老年代分配.
 * <p> -XX:+UseParNewGC : 打开次开关后,使用ParNew + Serial Old 的收集器组合进行内存回收
 * 
 * @author wubo
 *
 */
public class LargeObjectJvmArgsTest {
	
	private static final int _1MB = 1024*1024;
	
	public static void testAllocation() {
		byte[] allocation;
		
		allocation = new byte[4 * _1MB]; // 直接分表在老年代中
	}
	
	public static void main(String[] args) {
		testAllocation();
	}

}
