package org.wulin.jvm.jdk.unsafe;

import java.lang.reflect.Field;

import sun.misc.Unsafe;

/**
 * unsafe工具类
 * @author wulin
 *
 */
public class UnsafeUtil {
	private static final Unsafe UNSAFE;
	
	static {
		Unsafe unsafeTemb = null;
		try {
			Field  getUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            getUnsafe.setAccessible(true);
            unsafeTemb = (Unsafe) getUnsafe.get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
		UNSAFE = unsafeTemb;
	}
	
	/**
	 * 得到unsafe
	 * @return
	 * @throws Throwable
	 */
	public static Unsafe getUnsafe(){
		return UNSAFE;
	}
}
