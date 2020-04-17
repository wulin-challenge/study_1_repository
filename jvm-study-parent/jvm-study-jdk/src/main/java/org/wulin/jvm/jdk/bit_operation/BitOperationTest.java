package org.wulin.jvm.jdk.bit_operation;

import org.junit.Test;

/**
 * 参考文章: 
 * <p> https://frank-lam.github.io/fullstack-tutorial/#/JavaArchitecture/01-Java%E5%9F%BA%E7%A1%80?id=_4-%E4%BD%8D%E8%BF%90%E7%AE%97%E7%AC%A6
 * <p> 上述连接失效,则使用该连接
 * <p> https://github.com/wulin-challenge/fullstack-tutorial/blob/master/notes/JavaArchitecture/01-Java%E5%9F%BA%E7%A1%80.md
 * @author wulin
 *
 */
public class BitOperationTest {
	private int a = 5;
//	private int a = -4;
	
	/**
	 * 2进制转10进制,10进制转2进制
	 */
	@Test
	public void _2To10BinarySystem() {
		int two = Integer.parseInt("101",2);// 2进制
		int eight = Integer.parseInt("101", 8);// 8进制
		String binaryString = Integer.toBinaryString(5);
		System.out.println("二进制转为十进制: " + two);
		System.out.println("八进制转为十进制: " + eight);
		System.out.println("十进制转二进制: "+binaryString);
	}
	/**
	 * 测试左位移
	 */
	@Test
	public void move() {
		System.out.println("a的当前值"+a+",a变量的2进制数:"+Integer.toBinaryString(a));
		int c = a << 2;
		System.out.println("a的当前值"+a+",a左移两位的2进制数:"+Integer.toBinaryString(c));
		int d = a >> 2;
		System.out.println("a的当前值"+a+",a右移两位的2进制数:"+Integer.toBinaryString(d));
		int e = a >>> 2;
		System.out.println("a的当前值"+a+",a 无符号右移两位的2进制数:"+Integer.toBinaryString(e));
		
	}

}
