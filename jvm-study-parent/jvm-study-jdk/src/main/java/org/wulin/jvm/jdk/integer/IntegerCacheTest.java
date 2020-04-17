package org.wulin.jvm.jdk.integer;

import java.util.HashMap;
import java.util.Map;

/**
 * Integer的缓存问题导致  System.out.println(Integer.valueOf("128") == num); // false
 * @author wulin
 *
 */
public class IntegerCacheTest {
	
	static Map<String,String> first = new HashMap<String,String>();
	
	public int pageCount() {
		return 128;
	}
	
	public void testNum(Integer num) {
		System.out.println(first.get("count")+"//"+num);
		
		System.out.println(Integer.parseInt(first.get("count")) == num); // true
		System.out.println(Integer.valueOf(first.get("count")) == num); // false
		//缓存问题
		System.out.println(Integer.valueOf("128") == num); // false
	}
	
	public static void main(String[] args) {
		IntegerCacheTest test = new IntegerCacheTest();
		final int str = test.pageCount();
		
		first.put("count", 128+"");
		test.testNum(str);
		
		//没有想明白,切记在没有想明白之前,暂时不要使用Integer.valueOf将一个数字型的字符串转为int型,也最好不要int 与 Integer混用
		System.out.println(Integer.valueOf("128") == str); // true
	}
}
